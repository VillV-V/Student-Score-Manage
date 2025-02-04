package com.gxnzd.scoresystem.utils.easyExcel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxnzd.scoresystem.entity.Course;
import com.gxnzd.scoresystem.entity.SCourse;
import com.gxnzd.scoresystem.entity.StuClass;
import com.gxnzd.scoresystem.entity.Teacher;
import com.gxnzd.scoresystem.service.CourseService;
import com.gxnzd.scoresystem.service.SCourseService;
import com.gxnzd.scoresystem.service.StuClassService;
import com.gxnzd.scoresystem.service.TeacherService;
import com.gxnzd.scoresystem.vo.SCourseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SCourseExcelReadListener extends AnalysisEventListener<SCourseVo> {

    private SCourseService sCourseService;

    private StuClassService stuClassService;

    private TeacherService teacherService;

    private CourseService courseService;

    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 100;

    /**
     * 缓存的数据
     */
    private List<SCourse> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

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

    private Integer classId = 0;
    private Integer courseId = 0;

    public SCourseExcelReadListener(SCourseService sCourseService, StuClassService stuClassService, TeacherService teacherService, CourseService courseService) {
        this.sCourseService = sCourseService;
        this.stuClassService = stuClassService;
        this.teacherService = teacherService;
        this.courseService = courseService;
    }

    @Override
    public void invoke(SCourseVo sCourseVo, AnalysisContext analysisContext) {
        totalRows += 1;
        log.info("解析到一条数据:{}", sCourseVo);
        int rowIndex = analysisContext.readRowHolder().getRowIndex();
        Integer id = sCourseVo.getId();
        sCourseVo.setClassId(null);
        String className = sCourseVo.getClassName();
        sCourseVo.setCourseId(null);
        String courseName = sCourseVo.getCourseName();
        Long teacherId = sCourseVo.getTeacherId();
        String date = sCourseVo.getDate();

        // 只有全部校验通过的对象才能被添加到下一步
        if(id == null) {
            allValid(sCourseVo, rowIndex, className, courseName, teacherId, date);
        } else {
            if(!sCourseIdValid(rowIndex, id)) {
                allValid(sCourseVo, rowIndex, className, courseName, teacherId, date);
            } else {
                // 任意一项有值时也要进行校验
                if(!StringUtils.isBlank(className)) {
                    if(!classValid(rowIndex, className)) {
                        return;
                    }
                }
                if(!StringUtils.isBlank(courseName)) {
                    if(!courseValid(rowIndex, courseName)) {
                        return;
                    }
                }
                if(teacherId != null) {
                    if(!teacherValid(rowIndex, teacherId)) {
                        return;
                    }
                }
                if(!StringUtils.isBlank(date)) {
                    if(!dateValid(rowIndex, date)) {
                        return;
                    }
                }

                if(classId != 0 && courseId != 0) {
                    sCourseVo.setClassId(classId);
                    sCourseVo.setCourseId(courseId);
                }
                SCourse sCourse = new SCourse();
                BeanUtils.copyProperties(sCourseVo, sCourse);
                cachedDataList.add(sCourse);
                this.classId = 0;
                this.courseId = 0;
            }
        }
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            sCourseService.saveOrUpdateBatch(cachedDataList);
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
            sCourseService.saveOrUpdateBatch(cachedDataList);
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
    private void allValid(SCourseVo sCourseVo, int rowIndex, String className, String courseName, Long teacherId, String date) {
        if (classValid(rowIndex, className) && courseValid(rowIndex, courseName)
                && teacherValid(rowIndex, teacherId) && dateValid(rowIndex, date)) {
            if(classId != 0 && courseId != 0) {
                sCourseVo.setClassId(classId);
                sCourseVo.setCourseId(courseId);
            }
            SCourse sCourse = new SCourse();
            BeanUtils.copyProperties(sCourseVo, sCourse);
            cachedDataList.add(sCourse);
            this.classId = 0;
            this.courseId = 0;
        }
    }

    /**
     * 选课记录编号的校验
     *
     * @param rowIndex 行数
     * @param id     选课记录编号
     */
    private Boolean sCourseIdValid(Integer rowIndex, Integer id) {
        SCourse sCourse =sCourseService.getOne(new QueryWrapper<SCourse>().eq("id", id));
        if(sCourse == null) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 班级的校验
     *
     * @param rowIndex 行数
     * @param className     班级名称
     */
    private Boolean classValid(Integer rowIndex, String className) {
        if (StringUtils.isBlank(className)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，班级名称不能为空");
            return Boolean.FALSE;
        } else {
            QueryWrapper<StuClass> wrapper = new QueryWrapper<>();
            wrapper.eq("class_name", className);
            StuClass stuClass = stuClassService.getOne(wrapper);
            if (stuClass == null) {
                errorMsgList.add("第 " + (rowIndex+1) + " 行，不存在该班级");
                return Boolean.FALSE;
            }
            this.classId = stuClass.getClassId();
        }
        return Boolean.TRUE;
    }

    /**
     * 课程的校验
     *
     * @param rowIndex 行数
     * @param courseName     课程名称
     */
    private Boolean courseValid(Integer rowIndex, String courseName) {
        if (StringUtils.isBlank(courseName)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，课程名称不能为空");
            return Boolean.FALSE;
        } else {
            QueryWrapper<Course> wrapper = new QueryWrapper<>();
            wrapper.eq("course_name", courseName);
            Course course = courseService.getOne(wrapper);
            if (course == null) {
                errorMsgList.add("第 " + (rowIndex+1) + " 行，不存在该课程");
                return Boolean.FALSE;
            }
            this.courseId = course.getCourseId();
            QueryWrapper<SCourse> wrapper1 = new QueryWrapper<>();
            wrapper1.eq("class_id", this.classId);
            wrapper1.eq("course_id", course.getCourseId());
            List<SCourse> list = sCourseService.list(wrapper1);
            if (!list.isEmpty()) {
                errorMsgList.add("第 " + (rowIndex+1) + " 行，已经选择过该课程");
                return Boolean.FALSE;
            }

        }
        return Boolean.TRUE;
    }

    /**
     * 教师的校验
     *
     * @param rowIndex 行数
     * @param teacherId     教师工号
     */
    private Boolean teacherValid(Integer rowIndex, Long teacherId) {
        if (teacherId == null) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，教师工号不能为空");
            return Boolean.FALSE;
        } else {
            QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
            wrapper.eq("teacher_id", teacherId);
            Teacher teacher = teacherService.getOne(wrapper);
            if (teacher == null) {
                errorMsgList.add("第 " + (rowIndex+1) + " 行，不存在该教师");
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 学期的校验
     *
     * @param rowIndex 行数
     * @param date     学期
     */
    private Boolean dateValid(Integer rowIndex, String date) {
        if (StringUtils.isBlank(date)) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，学期不能为空");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}
