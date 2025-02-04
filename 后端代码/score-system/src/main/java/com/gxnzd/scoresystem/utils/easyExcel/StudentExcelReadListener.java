package com.gxnzd.scoresystem.utils.easyExcel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxnzd.scoresystem.entity.StuClass;
import com.gxnzd.scoresystem.entity.Student;
import com.gxnzd.scoresystem.service.StuClassService;
import com.gxnzd.scoresystem.service.StudentService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class StudentExcelReadListener extends AnalysisEventListener<Student> {

    private StuClassService stuClassService;

    private StudentService studentService;

    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 100;

    /**
     * 创建一个Pattern对象，使用正则表达式校验手机号格式
     */
    private static final Pattern PHONE_REGEX = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 创建一个Pattern对象，使用正则表达式校验密码格式
     */
    private static final Pattern PWD_REGEX = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{6,16}$");

    /**
     * 创建一个Pattern对象，使用正则表达式校验邮箱格式
     */
    private static final Pattern Email_REGEX = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * 缓存的数据
     */
    private List<Student> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    /**
     * Excel总行数
     */
    private int totalRows = 0;

    /**
     * 通过校验的总数
     */
    private int successRows = 0;

    /**
     * 错误信息列表
     */
    private final List<String> errorMsgList = new ArrayList<>(BATCH_COUNT);

    private Integer classId = null;

    public StudentExcelReadListener(StuClassService stuClassService, StudentService studentService) {
        this.stuClassService = stuClassService;
        this.studentService = studentService;
    }

    @Override
    public void invoke(Student student, AnalysisContext analysisContext) {
        totalRows += 1;
        log.info("解析到一条数据:{}", student);
        int rowIndex = analysisContext.readRowHolder().getRowIndex();
        Long studentId = student.getStudentId();
        String studentName = student.getStudentName();
        String gender = student.getGender();
        String telephone = student.getTelephone();
        String email = student.getEmail();
        student.setClassId(null);
        String className = student.getClassName();
        String password = student.getPassword();

        // 只有全部校验通过的对象才能被添加到下一步
        //如果学号为空则为新增，需进行所有校验
        if(studentId == null) {
            allValid(student, rowIndex, studentName, gender, telephone, email, className, password);
        } else {
            if (!studentIdValid(rowIndex, studentId)) {
                allValid(student, rowIndex, studentName, gender, telephone, email, className, password);
            } else {
                // 任意一项有值时也要进行校验
                if(!StringUtils.isBlank(studentName)) {
                    if(!nameValid(rowIndex, studentName)) {
                        return;
                    }
                }
                if(!StringUtils.isBlank(gender)) {
                    if(!genderValid(rowIndex, gender)) {
                        return;
                    }
                }
                if(!StringUtils.isBlank(telephone)) {
                    if(!phoneValid(rowIndex, telephone)) {
                        return;
                    }
                }
                if(!StringUtils.isBlank(email)) {
                    if(!emailValid(rowIndex, email)) {
                        return;
                    }
                }
                if(!StringUtils.isBlank(className)) {
                    if(!classValid(rowIndex, className)) {
                        return;
                    }
                }
                if(!StringUtils.isBlank(password)) {
                    if(!passwordValid(rowIndex, password)) {
                        return;
                    }
                }

                if(classId != null) {
                    student.setClassId(classId);
                }
                cachedDataList.add(student);
                this.classId = null;
            }
        }

        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            studentService.saveOrUpdateBatch(cachedDataList);
            successRows += cachedDataList.size();
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        successRows += cachedDataList.size();
        log.info("所有数据解析完成！全部校验通过的数据有{}条", successRows);
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库saveData();
        if(!cachedDataList.isEmpty()) {
            studentService.saveOrUpdateBatch(cachedDataList);
        }
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        if (exception instanceof RuntimeException) {
            throw exception;
        }
        int index = context.readRowHolder().getRowIndex() + 1;
        errorMsgList.add("第 " + index + " 行解析错误");
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        int totalRows = context.readSheetHolder().getApproximateTotalRowNumber() - 1;
        int maxNum = 500;
        if (totalRows > maxNum) {
            errorMsgList.add("数据量过大,单次最多上传500条");
            throw new RuntimeException("数据量过大,单次最多上500条");
        }
    }

    public Map<String, Object> getResult() {
        Map<String, Object> map = new HashMap<>();
        map.put("totalRows", totalRows);
        map.put("successRows", successRows);
        map.put("errorMsgList", errorMsgList);
        return map;
    }

    //全部校验
    private void allValid(Student student, int rowIndex, String studentName, String gender, String telephone, String email, String className, String password) {
        if (nameValid(rowIndex, studentName) && genderValid(rowIndex, gender) && phoneValid(rowIndex, telephone)
                && emailValid(rowIndex, email) && classValid(rowIndex, className) && passwordValid(rowIndex, password)) {
            if(classId != null) {
                student.setClassId(classId);
            }
            cachedDataList.add(student);
            this.classId = null;
        }
    }

    /**
     * 学号的校验
     *
     * @param rowIndex 行数
     * @param studentId     学号
     */
    private Boolean studentIdValid(Integer rowIndex, Long studentId) {
        Student student = studentService.getOne(new QueryWrapper<Student>().eq("student_id", studentId));
        if(student == null) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 姓名的校验
     *
     * @param rowIndex 行数
     * @param name     姓名
     */
    private Boolean nameValid(Integer rowIndex, String name) {
        if (StringUtils.isBlank(name)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，姓名不能为空");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private Boolean phoneValid(int rowIndex, String phone) {
        if (StringUtils.isBlank(phone)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，手机号不能为空");
            return Boolean.FALSE;
        }
        Matcher matcher = PHONE_REGEX.matcher(phone);
        if (!matcher.matches()) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，手机号不合法");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 性别的校验
     *
     * @param rowIndex 行数
     * @param gender   性别
     */
    private Boolean genderValid(int rowIndex, String gender) {
        if (StringUtils.isBlank(gender)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，性别不能为空");
            return Boolean.FALSE;
        }
        if (gender.equals("男") || gender.equals("女")) {
            return Boolean.TRUE;
        }
        errorMsgList.add("第 " + (rowIndex+1) + " 行，性别只能为男或女");
        return Boolean.FALSE;
    }

    /**
     * 所属班级校验
     *
     * @param rowIndex 行数
     * @param className  所属
     */
    private Boolean classValid(int rowIndex, String className) {
        // 校验所属班级是否为空
        if (StringUtils.isBlank(className)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，所属班级不能为空");
            return Boolean.FALSE;
        } else {
            Map<String,Integer> map = stuClassService.getStuClassList().stream().collect(Collectors.toMap(StuClass::getClassName,StuClass::getClassId));
            if (map.get(className) == null) {
                errorMsgList.add("第 " + (rowIndex+1) + " 行，不存在该班级");
                return Boolean.FALSE;
            }
            this.classId = map.get(className);
        }
        return Boolean.TRUE;
    }

    /**
     * 密码校验
     *
     * @param rowIndex 行数
     * @param password      密码
     */
    private Boolean passwordValid(int rowIndex, String password) {
        if (StringUtils.isBlank(password)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，密码不能为空");
            return Boolean.FALSE;
        }
        // 校验密码是否合法
        Matcher matcher = PWD_REGEX.matcher(password);
        if (!matcher.matches()) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，密码不合法，密码应在6-16位且包含字母和数字");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 邮箱的校验
     *
     * @param rowIndex 行数
     * @param email    邮箱
     */
    private Boolean emailValid(int rowIndex, String email) {
        // 校验邮箱是否为空
        if (StringUtils.isBlank(email)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，邮箱不能为空");
            return Boolean.FALSE;
        }
        // 校验邮箱是否合法
        Matcher matcher = Email_REGEX.matcher(email);
        if (!matcher.matches()) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，邮箱不合法");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}
