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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SDExcelReadListener extends AnalysisEventListener<ScoreDetail> {

    private ScoreSeivice scoreSeivice;
    private ScoreDetailService scoreDetailService;
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
    private List<ScoreDetail> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

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

    public SDExcelReadListener(Long id, ScoreSeivice scoreSeivice, ScoreDetailService scoreDetailService, StuClassService stuClassService, CourseService courseService, SCourseService sCourseService, QuantitativeService quantitativeService) {
        this.id = id;
        this.scoreSeivice = scoreSeivice;
        this.scoreDetailService = scoreDetailService;
        this.stuClassService = stuClassService;
        this.courseService = courseService;
        this.sCourseService = sCourseService;
        this.quantitativeService = quantitativeService;
    }

    @Override
    public void invoke(ScoreDetail sd, AnalysisContext analysisContext) {
        totalRows += 1;
        log.info("解析到一条数据:{}", sd);
        int rowIndex = analysisContext.readRowHolder().getRowIndex();
        Long studentId = sd.getStudentId();
        String className = sd.getClassName();
        String courseName = sd.getCourseName();
        LocalDate date = sd.getDate();
        String usualItem = sd.getUsualItem();
        String skillItem = sd.getSkillItem();
        String disItem = sd.getDisItem();
        String remark = sd.getSdRemark();
        // 只有全部校验通过的对象才能被添加到下一步
        if (classValid(rowIndex, className) && studentValid(rowIndex, studentId) &&
                courseValid(rowIndex, courseName, studentId) &&
                scoreValid(rowIndex, usualItem, skillItem, disItem) &&
                remarkValid(rowIndex, remark)) {
            sd.setQName(this.qName);
            CalculateScore calculateScore = new CalculateScore(quantitativeService, sd);
            sd.setCourseId(this.courseId);
            if(!dateValid(rowIndex, date)) {
                return;
            }
            ScoreDetail scoreDetail = calculateScore.calculateDetail();
            sd.setUsualScore(scoreDetail.getUsualScore());
            sd.setSkillScore(scoreDetail.getSkillScore());
            sd.setDisScore(scoreDetail.getDisScore());
            sd.setTotalScore(scoreDetail.getTotalScore());
            this.classId = null;
            this.courseId = null;
            this.qName = null;
            cachedDataList.add(sd);  //有重复插入的问题，待解决
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

    public void addOrUpdateScore(List<ScoreDetail> cachedDataList) {
        for (ScoreDetail sd : cachedDataList) {
            QueryWrapper<ScoreDetail> wrapper = new QueryWrapper<>();
            wrapper.eq("course_id", sd.getCourseId()).eq("student_id", sd.getStudentId())
                    .eq("date", sd.getDate()).eq("sd_remark", sd.getSdRemark());
            ScoreDetail sd1 = scoreDetailService.getOne(wrapper);
            // 获取当前日期和时间
            LocalDateTime currentDateTime = LocalDateTime.now();
            // 自定义格式化（包括日期和时间）
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = currentDateTime.format(formatter);
            if (sd1 == null) {
                sd.setCreateTime(formattedDateTime);
                sd.setUpdateTime(formattedDateTime);
                scoreDetailService.save(sd);
                updateScore(sd);
            } else {
                sd.setSdId(sd1.getSdId());
                sd.setUpdateTime(formattedDateTime);
                scoreDetailService.updateById(sd);
                updateScore(sd);
            }
        }
    }

    //数据变化时更新平时成绩总表
    public int updateScore(ScoreDetail sd) {
        List<ScoreDetail> sdList = scoreDetailService.getSDListBySCId(sd.getStudentId(), sd.getCourseId());
        if(!sdList.isEmpty()) {
            float usualScore = 0;
            float skillScore = 0;
            float disScore = 0;
            float totalScore = 0;
            for (ScoreDetail sd1 : sdList) {
                CalculateScore calculateScore = new CalculateScore(quantitativeService, sd1);
                ScoreDetail scoreDetail = calculateScore.calculateDetail();
                usualScore += scoreDetail.getUsualScore();
                skillScore += scoreDetail.getSkillScore();
                disScore += scoreDetail.getDisScore();
            }
            QueryWrapper<Score> wrapper = new QueryWrapper<>();
            wrapper.eq("student_id", sd.getStudentId());
            wrapper.eq("course_id", sd.getCourseId());
            Score score = scoreSeivice.getOne(wrapper);
            if(score == null) {
                score = new Score();
                score.setStudentId(sd.getStudentId());
                score.setCourseId(sd.getCourseId());
            }
            if(usualScore > 100) {
                usualScore = 100f;
            }
            if(skillScore > 100) {
                skillScore = 100f;
            }
            if(disScore > 100) {
                disScore = 100f;
            }
            score.setQName(sd.getQName());
            score.setUsualScore(usualScore);
            score.setSkillScore(skillScore);
            score.setDisScore(disScore);
            CalculateScore cs = new CalculateScore(quantitativeService, score);
            score.setTotalScore(cs.calculate());
            if (scoreSeivice.saveOrUpdate(score)) {
                return 1;
            }
        } else {
            QueryWrapper<Score> wrapper = new QueryWrapper<>();
            wrapper.eq("student_id", sd.getStudentId());
            wrapper.eq("course_id", sd.getCourseId());
            Score score = scoreSeivice.getOne(wrapper);
            if(score == null) {
                score = new Score();
                score.setStudentId(sd.getStudentId());
                score.setCourseId(sd.getCourseId());
            }
            score.setUsualScore((float) 0);
            score.setSkillScore((float) 0);
            score.setDisScore((float) 0);
            score.setTotalScore((float) 0);
            if (scoreSeivice.saveOrUpdate(score)) {
                return 1;
            }
        }
        return 0;
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
     * 上课日期的校验
     *
     * @param rowIndex 行数
     * @param date     上课日期
     */
    private Boolean dateValid(Integer rowIndex, LocalDate date) {
        if (date == null) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，上课日期不能为空");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 成绩明细的校验
     *
     * @param rowIndex 行数
     * @param usualItem     成绩
     */
    private Boolean scoreValid(Integer rowIndex, String usualItem, String skillItem, String disItem) {
        if (usualItem == null) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，课堂表现成绩明细不能为空");
            return Boolean.FALSE;
        } else if (skillItem == null) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，技能成绩明细不能为空");
            return Boolean.FALSE;
        } else if (disItem == null) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，纪律成绩明细不能为空");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 备注的校验
     *
     * @param rowIndex 行数
     * @param remark     上课日期
     */
    private Boolean remarkValid(Integer rowIndex, String remark) {
        if (remark == null) {
            errorMsgList.add("第 " + (rowIndex+1) + " 行，备注不能为空");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}
