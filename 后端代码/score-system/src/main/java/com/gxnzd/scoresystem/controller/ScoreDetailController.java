package com.gxnzd.scoresystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxnzd.scoresystem.entity.Score;
import com.gxnzd.scoresystem.entity.ScoreDetail;
import com.gxnzd.scoresystem.service.*;
import com.gxnzd.scoresystem.utils.CalculateScore;
import com.gxnzd.scoresystem.utils.CommonResult;
import com.gxnzd.scoresystem.utils.easyExcel.SDExcelReadListener;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ScoreDetailController {

    @Autowired
    private ScoreSeivice scoreSeivice;
    @Autowired
    private ScoreDetailService scoreDetailService;
    @Autowired
    private QuantitativeService quantitativeService;
    @Autowired
    private SCourseService sCourseService;
    @Autowired
    private StuClassService stuClassService;
    @Autowired
    private CourseService courseService;

    //按班级获取成绩明细
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @GetMapping("/getSDListByClassId")
    public CommonResult<Map<String, Object>> getScoreListByClassId(@PathVariable int classId) {
        List<ScoreDetail> scoreList = scoreDetailService.getSDListByClassId(classId);
        if (scoreList != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", scoreList.size());
            map.put("records", scoreList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //按班级和课程获取成绩明细
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @GetMapping("/getSDListByCCId")
    public CommonResult<Map<String, Object>> getScoreListByCCId(@RequestParam int classId, int courseId) {
        List<ScoreDetail> sdList = scoreDetailService.getSDListByCCId(classId, courseId);
        if (sdList != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", sdList.size());
            map.put("records", sdList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //按学号和课程编号获取成绩明细
    @SaCheckRole(value = {"admin", "teacher", "student"},mode = SaMode.OR)
    @GetMapping("/getSDListBySCId")
    public CommonResult<Map<String, Object>> getScoreListBySCId(@RequestParam Long studentId, int courseId) {
        List<ScoreDetail> sdList = scoreDetailService.getSDListBySCId(studentId, courseId);
        if (sdList != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", sdList.size());
            map.put("records", sdList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //新增成绩明细信息
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @PostMapping("/addScoreDetail")
    public CommonResult<String> addScoreDetail(@RequestBody ScoreDetail sd) {
        CalculateScore calculateScore = new CalculateScore(quantitativeService, sd);
        ScoreDetail scoreDetail = calculateScore.calculateDetail();
        sd.setUsualScore(scoreDetail.getUsualScore());
        sd.setSkillScore(scoreDetail.getSkillScore());
        sd.setDisScore(scoreDetail.getDisScore());
        sd.setTotalScore(scoreDetail.getTotalScore());

        // 获取当前日期和时间
        LocalDateTime currentDateTime = LocalDateTime.now();
        // 自定义格式化（包括日期和时间）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);
        sd.setCreateTime(formattedDateTime);
        sd.setUpdateTime(formattedDateTime);

        int result = scoreDetailService.addScoreDetail(sd);
        if(result > 0) {
            int num = updateScore(sd);
            if(num != 0) {
                return new CommonResult<>(200, "添加成功，平时成绩总表更新成功", null);
            }
            return new CommonResult<>(200, "添加成功，平时成绩总表更新失败", null);
        }
        return new CommonResult<>(500, "添加失败", null);
    }

    //修改成绩明细信息
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @PutMapping("/updateScoreDetail")
    public CommonResult<String> updateScoreDetail(@RequestBody ScoreDetail sd) {
        CalculateScore calculateScore = new CalculateScore(quantitativeService, sd);
        ScoreDetail scoreDetail = calculateScore.calculateDetail();
        sd.setUsualScore(scoreDetail.getUsualScore());
        sd.setSkillScore(scoreDetail.getSkillScore());
        sd.setDisScore(scoreDetail.getDisScore());
        sd.setTotalScore(scoreDetail.getTotalScore());

        // 获取当前日期和时间
        LocalDateTime currentDateTime = LocalDateTime.now();
        // 自定义格式化（包括日期和时间）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);
        sd.setUpdateTime(formattedDateTime);
        int result = scoreDetailService.updateScoreDetail(sd);
        if(result > 0) {
            int num = updateScore(sd);
            if(num != 0) {
                return new CommonResult<>(200, "更新成功，平时成绩总表更新成功", null);
            }
            return new CommonResult<>(200, "更新成功，平时成绩总表更新失败", null);
        }
        return new CommonResult<>(500, "更新失败", null);
    }

    //删除成绩明细信息
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @DeleteMapping("/delScoreDetail/{id}")
    public CommonResult<String> delScoreDetail(@PathVariable int id) {
        ScoreDetail sd = scoreDetailService.getById(id);
        int result = scoreDetailService.deleteScoreDetail(id);
        if(result > 0) {
            int num = updateScore(sd);
            if(num != 0) {
                return new CommonResult<>(200, "删除成功，平时成绩总表更新成功", null);
            }
            return new CommonResult<>(200, "删除成功，平时成绩总表更新失败", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //批量删除成绩明细信息
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @DeleteMapping("/delBatchesScoreDetail")
    public CommonResult<String> delBatchesScoreDetail(@RequestBody Integer[] ids) {
        List<ScoreDetail> sdList = new ArrayList<>();
        for (Integer id : ids) {
            ScoreDetail sd = scoreDetailService.getById(id);
            sdList.add(sd);
        }
        int result = scoreDetailService.delBatchesScoreDetail(Arrays.asList(ids));
        if(result > 0) {
            int num = 0;
            for(ScoreDetail sd : sdList) {
                num += updateScore(sd);
            }
            if(num != 0) {
                return new CommonResult<>(200, "删除成功，平时成绩总表更新成功", null);
            }
            return new CommonResult<>(200, "删除成功，平时成绩总表更新失败", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //导入
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @PostMapping("/importSD")
    public CommonResult<Map<String, Object>> importFile(MultipartFile file, @RequestParam Long id) throws IOException {
        try {
            SDExcelReadListener sderl = new SDExcelReadListener(id,scoreSeivice,scoreDetailService,stuClassService,courseService,sCourseService,quantitativeService);
            EasyExcel.read(file.getInputStream(), ScoreDetail.class, sderl).doReadAll();
            return new CommonResult<>(200, "导入成功", sderl.getResult());
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, "导入失败", null);
        }
    }

    //导出
    //导出该课程该学生的成绩明细
    @SaCheckRole(value = {"admin", "teacher", "student"},mode = SaMode.OR)
    @GetMapping("/exportSDBySCId")
    public CommonResult<String> exportFile(@RequestParam Long studentId, Integer courseId , HttpServletResponse response) throws IOException {
        List<ScoreDetail> sdList = scoreDetailService.getSDListBySCId(studentId, courseId);
        try {
            // 设置响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 设置编码格式
            response.setCharacterEncoding("utf-8");
            String name = sdList.get(0).getStudentName() + "《" + sdList.get(0).getCourseName() + "》平时成绩明细表";
            // 设置URLEncoder.encode 防止中文乱码
            String fileName = URLEncoder.encode(name, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 写出Excel
            EasyExcel.write(response.getOutputStream(), ScoreDetail.class)
                    .inMemory(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet(name)
                    .doWrite(sdList);
            return new CommonResult<>(200, "导出成功", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CommonResult<>(500, "导出失败", null);
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

}
