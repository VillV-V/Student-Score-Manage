package com.gxnzd.scoresystem.utils.easyExcel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxnzd.scoresystem.entity.Course;
import com.gxnzd.scoresystem.entity.Quantitative;
import com.gxnzd.scoresystem.entity.School;
import com.gxnzd.scoresystem.service.CourseService;
import com.gxnzd.scoresystem.service.QuantitativeService;
import com.gxnzd.scoresystem.service.SchoolService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CourseExcelReadListener extends AnalysisEventListener<Course> {

    private CourseService courseService;

    private SchoolService schoolService;

    private QuantitativeService quantitativeService;

    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 100;

    /**
     * 缓存的数据
     */
    private List<Course> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    /**
     * Excel总行数
     */
    private int totalRows = 0;

    /**
     * 通过校验的总数
     */
    private int successRows = 0;

    private Integer schoolId = null;

    private Integer qId = null;

    /**
     * 错误信息列表
     */
    private final List<String> errorMsgList = new ArrayList<>(BATCH_COUNT);

    public CourseExcelReadListener(CourseService courseService, SchoolService schoolService, QuantitativeService quantitativeService) {
        this.courseService = courseService;
        this.schoolService = schoolService;
        this.quantitativeService = quantitativeService;
    }

    @Override
    public void invoke(Course course, AnalysisContext analysisContext) {
        totalRows += 1;
        log.info("解析到一条数据:{}", course);
        int rowIndex = analysisContext.readRowHolder().getRowIndex();
        Integer courseId = course.getCourseId();
        String courseName = course.getCourseName();
        String type = course.getType();
        course.setSchoolId(null);
        String schoolName = course.getSchoolName();
        int classPeriod = 0;
        if(course.getClassPeriod() != null) {
            classPeriod = course.getClassPeriod();
        }
        int practicalClassPeriod = 0;
        if(course.getPracticalClassPeriod() != null) {
            practicalClassPeriod = course.getPracticalClassPeriod();
        }
        String qName = course.getQName();

        // 只有全部校验通过的对象才能被添加到下一步
        // 课程编号为空时为新增课程，需进行所有校验
        if(courseId == null) {
            allValid(course, rowIndex, courseName, type, schoolName, classPeriod, practicalClassPeriod, qName);
        } else {
            if(!courseIdValid(rowIndex, courseId)) {
                allValid(course, rowIndex, courseName, type, schoolName, classPeriod, practicalClassPeriod, qName);
            } else {
                // 任意一项有值时也要进行校验
                if(!StringUtils.isBlank(courseName)) {
                    if(!nameValid(rowIndex, courseName)) {
                        return;
                    }
                }
                if(!StringUtils.isBlank(type)) {
                    if(!typeValid(rowIndex, type)) {
                        return;
                    }
                }
                if(!StringUtils.isBlank(schoolName)) {
                    if(!schoolValid(rowIndex, schoolName)) {
                        return;
                    }
                }
                if (classPeriod != 0 || practicalClassPeriod != 0) {
                    if(!periodValid(rowIndex, classPeriod, practicalClassPeriod)) {
                        return;
                    }
                }
                if (!StringUtils.isEmpty(qName)) {
                    if(!quantitativeValid(rowIndex, qName)) {
                        return;
                    }
                }

                if (schoolId != null) {
                    course.setSchoolId(schoolId);
                }
                if (qId != null) {
                    course.setQId(qId);
                }
                cachedDataList.add(course);
                this.schoolId = null;
                this.qId = null;
            }
        }


        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            courseService.saveOrUpdateBatch(cachedDataList);
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
            courseService.saveOrUpdateBatch(cachedDataList);
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

    //根据课时选择课程平时成绩量化标准
    private String changeQ (String courseName, String type, int cp, int pcp) {
        if(type.equals("综合实践")) {
            return "E";
        }
        if(courseName.contains("大学英语")) {
            return "F";
        }
        float num = (float) pcp / cp;
        if(num <= 0.1) {
            return "A";
        } else if(num > 0.1 && num <= 0.3) {
            return "B";
        } else if(num > 0.3 && num <= 0.5) {
            return "C";
        } else if(num > 0.5) {
            return "D";
        }
        return "";
    }

    //全部校验
    private void allValid(Course course, int rowIndex, String courseName, String type, String schoolName, int classPeriod, int practicalClassPeriod, String quantitative) {
        if (nameValid(rowIndex, courseName) && typeValid(rowIndex, type) && schoolValid(rowIndex, schoolName)
                && periodValid(rowIndex, classPeriod, practicalClassPeriod) && quantitativeValid(rowIndex, quantitative)) {
            if (schoolId != null) {
                course.setSchoolId(schoolId);
            }
            if (qId != null) {
                course.setQId(qId);
            }
            cachedDataList.add(course);
            this.schoolId = null;
            this.qId = null;
        }
    }

    /**
     * 课程编号的校验
     *
     * @param rowIndex 行数
     * @param courseId     课程编号
     */
    private Boolean courseIdValid(Integer rowIndex, Integer courseId) {
        Course course = courseService.getOne(new QueryWrapper<Course>().eq("course_id", courseId));
        if(course == null) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 课程名称的校验
     *
     * @param rowIndex 行数
     * @param name     课程名称
     */
    private Boolean nameValid(Integer rowIndex, String name) {
        if (StringUtils.isBlank(name)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，课程名称不能为空");
            return Boolean.FALSE;
        } else {
            QueryWrapper<Course> wrapper = new QueryWrapper<>();
            wrapper.eq("course_name", name);
            Course course = courseService.getOne(wrapper);
            if (course != null) {
                errorMsgList.add("第 " + (rowIndex+1) + " 行，该课程已经存在");
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 课程类型的校验
     *
     * @param rowIndex 行数
     * @param type   性别
     */
    private Boolean typeValid(int rowIndex, String type) {
        if (StringUtils.isBlank(type)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，课程类型不能为空");
            return Boolean.FALSE;
        }
        if (type.equals("公共基础必修课") || type.equals("公共基础选修课") || type.equals("专业基础课")
                || type.equals("专业核心课") || type.equals("专业选修（拓展）课") || type.equals("综合实践")) {
            return Boolean.TRUE;
        }
        errorMsgList.add("第 " + (rowIndex+1) + " 行，课程类型只能为公共基础必修课、公共基础选修课、专业基础课、专业核心课、专业选修（拓展）课、综合实践中的一种");
        return Boolean.FALSE;
    }

    /**
     * 所属学院校验
     *
     * @param rowIndex 行数
     * @param schoolName  所属学院
     */
    private Boolean schoolValid(int rowIndex, String schoolName) {
        // 校验所属学院是否为空
        if (StringUtils.isBlank(schoolName)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，所属学院不能为空");
            return Boolean.FALSE;
        } else {
            Map<String,Integer> map = schoolService.getSchoolList().stream().collect(Collectors.toMap(School::getSchoolName,School::getSchoolId));
            if (map.get(schoolName) == null) {
                errorMsgList.add("第 " + (rowIndex+1) + " 行，不存在该学院");
                return Boolean.FALSE;
            }
            this.schoolId = map.get(schoolName);
        }
        return Boolean.TRUE;
    }

    /**
     * 课时的校验
     *
     * @param rowIndex 行数
     * @param classPeriod   总课时
     * @param practicalClassPeriod   实践课时
     */
    private Boolean periodValid(int rowIndex, Integer classPeriod, Integer practicalClassPeriod) {
        if (classPeriod == 0) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，总课时不能为空");
            return Boolean.FALSE;
        } else if (practicalClassPeriod == 0) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，实践课时不能为空");
            return Boolean.FALSE;
        }
        if (classPeriod > practicalClassPeriod) {
            return Boolean.TRUE;
        }
        errorMsgList.add("第 " + (rowIndex+1) + " 行，总课时必须大于实践课时");
        return Boolean.FALSE;
    }

    /**
     * 课程平时成绩量化标准的校验
     *
     * @param rowIndex 行数
     * @param qName  课程平时成绩量化标准
     */
    private Boolean quantitativeValid(Integer rowIndex, String qName) {
        // 校验课程平时成绩量化标准是否为空
        if (StringUtils.isBlank(qName)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，课程平时成绩量化标准不能为空");
            return Boolean.FALSE;
        } else {
            Map<String,Integer> map = quantitativeService.getQuantitativeList().stream().collect(Collectors.toMap(Quantitative::getQName,Quantitative::getQId));
            if (map.get(qName) == null) {
                errorMsgList.add("第 " + (rowIndex+1) + " 行，不存在该成绩量化标准");
                return Boolean.FALSE;
            }
            this.qId = map.get(qName);
        }
        return Boolean.TRUE;
    }

}
