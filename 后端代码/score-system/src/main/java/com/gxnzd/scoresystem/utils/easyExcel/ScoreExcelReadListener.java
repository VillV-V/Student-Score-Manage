package com.gxnzd.scoresystem.utils.easyExcel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxnzd.scoresystem.entity.*;
import com.gxnzd.scoresystem.service.*;
import com.gxnzd.scoresystem.utils.CalculateScore;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ScoreExcelReadListener extends AnalysisEventListener<Score> {

    private ScoreSeivice scoreSeivice;
    private StuClassService stuClassService;
    private CourseService courseService;
    private SCourseService sCourseService;
    private QuantitativeService quantitativeService;
    private Long id;

    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 100;

    /**
     * 缓存的数据
     */
    private List<Score> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

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
    private Integer courseId = null;
    private String qName = null;

    public ScoreExcelReadListener(Long id,ScoreSeivice scoreSeivice, StuClassService stuClassService, CourseService courseService, SCourseService sCourseService, QuantitativeService quantitativeService) {
        this.id = id;
        this.scoreSeivice = scoreSeivice;
        this.stuClassService = stuClassService;
        this.courseService = courseService;
        this.sCourseService = sCourseService;
        this.quantitativeService = quantitativeService;
    }

    @Override
    public void invoke(Score score, AnalysisContext analysisContext) {
        totalRows += 1;
        log.info("解析到一条数据:{}", score);
        int rowIndex = analysisContext.readRowHolder().getRowIndex();
        Long studentId = score.getStudentId();
        String className = score.getClassName();
        String courseName = score.getCourseName();
        Float uScore = score.getUsualScore();
        if(uScore == null) {
            uScore = 0f;
            score.setUsualScore(uScore);
        } else if (uScore > 100) {
            uScore = 100f;
            score.setUsualScore(uScore);
        }
        Float sScore = score.getSkillScore();
        if(sScore == null) {
            sScore = 0f;
            score.setSkillScore(sScore);
        } else if (sScore > 100) {
            sScore = 100f;
            score.setSkillScore(sScore);
        }
        Float dScore = score.getDisScore();
        if(dScore == null) {
            dScore = 0f;
            score.setDisScore(dScore);
        } else if (dScore > 100) {
            dScore = 100f;
            score.setDisScore(dScore);
        }
        // 只有全部校验通过的对象才能被添加到下一步
        if (classValid(rowIndex, className) && studentValid(rowIndex, studentId) &&
                courseValid(rowIndex, courseName, studentId) && scoreValid(rowIndex, uScore, sScore, dScore)) {
            score.setQName(this.qName);
            CalculateScore calculateScore = new CalculateScore(quantitativeService, score);
            score.setCourseId(this.courseId);
            score.setTotalScore(calculateScore.calculate());
            this.classId = null;
            this.courseId = null;
            this.qName = null;
            cachedDataList.add(score);  //有重复插入的问题，待解决
        }
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            addOrUpdateScore(cachedDataList);
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
            addOrUpdateScore(cachedDataList);
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

    public void addOrUpdateScore(List<Score> cachedDataList) {
        for (Score score : cachedDataList) {
            QueryWrapper<Score> wrapper = new QueryWrapper<>();
            wrapper.eq("course_id", score.getCourseId()).eq("student_id", score.getStudentId());
            Score s = scoreSeivice.getOne(wrapper);
            if (s == null) {
                scoreSeivice.save(score);
            } else {
                score.setSId(s.getSId());
                scoreSeivice.updateById(score);
            }
        }
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
                errorMsgList.add("第 " + (rowIndex+1) + " 行，该班级不存在");
                return Boolean.FALSE;
            }
            this.classId = stuClass.getClassId();
        }
        return Boolean.TRUE;
    }

    /**
     * 学号的校验
     *
     * @param rowIndex 行数
     * @param studentId     学号
     */
    private Boolean studentValid(Integer rowIndex, Long studentId) {
        if (studentId == null) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，学号不能为空");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 课程的校验
     *
     * @param rowIndex 行数
     * @param courseName     课程名称
     */
    private Boolean courseValid(Integer rowIndex, String courseName, Long studentId) {
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
            wrapper1.eq("course_id", course.getCourseId()).eq("class_id", this.classId);
            SCourse sCourse = sCourseService.getOne(wrapper1);
            if (sCourse == null) {
                errorMsgList.add("第 " + (rowIndex+1) + " 行，未选择该课程");
                return Boolean.FALSE;
            } else if (id.toString().length() > 1) {
                if(!id.equals(sCourse.getTeacherId())) {
                    errorMsgList.add("第 " + (rowIndex+1) + " 行，不是该班该课程的任课老师");
                    return Boolean.FALSE;
                }
            }
            QueryWrapper<Quantitative> wrapper2 = new QueryWrapper<>();
            wrapper2.eq("q_id", course.getQId());
            Quantitative q = quantitativeService.getOne(wrapper2);
            this.qName = q.getQName();
        }
        return Boolean.TRUE;
    }

    /**
     * 成绩的校验
     *
     * @param rowIndex 行数
     * @param uScore     成绩
     */
    private Boolean scoreValid(Integer rowIndex, float uScore, float sScore, float dScore) {
        if (uScore < 0) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，课堂表现成绩不能小于0");
            return Boolean.FALSE;
        } else if (sScore < 0) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，技能成绩不能小于0");
            return Boolean.FALSE;
        } else if (dScore < 0) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，纪律成绩不能小于0");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}
