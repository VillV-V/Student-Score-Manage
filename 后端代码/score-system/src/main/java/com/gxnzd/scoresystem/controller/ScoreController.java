package com.gxnzd.scoresystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gxnzd.scoresystem.entity.Quantitative;
import com.gxnzd.scoresystem.entity.Score;
import com.gxnzd.scoresystem.service.*;
import com.gxnzd.scoresystem.utils.CalculateScore;
import com.gxnzd.scoresystem.utils.CommonResult;
import com.gxnzd.scoresystem.utils.easyExcel.ScoreExcelReadListener;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ScoreController {

    @Autowired
    private ScoreSeivice scoreSeivice;
    @Autowired
    private SCourseService sCourseService;
    @Autowired
    private QuantitativeService quantitativeService;
    @Autowired
    private StuClassService stuClassService;
    @Autowired
    private CourseService courseService;

    //获取所有成绩
    @SaCheckRole("admin")
    @GetMapping("/getAllScoreList")
    public CommonResult<Map<String, Object>> getAllScoreList() {
        List<Score> scoreList = scoreSeivice.getAllScoreList();
        if (scoreList != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", scoreList.size());
            map.put("records", scoreList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取该班级所有课程的成绩
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @GetMapping("/getScoreListByClassId")
    public CommonResult<Map<String, Object>> getScoreListByClassId(@PathVariable int classId) {
        List<Score> scoreList = scoreSeivice.getScoreListByClassId(classId);
        if (scoreList != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", scoreList.size());
            map.put("records", scoreList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //按学号获取成绩
    @SaCheckRole(value = {"admin", "student"},mode = SaMode.OR)
    @GetMapping("/getScoreListByStuId")
    public CommonResult<Map<String, Object>> getScoreListByStuId(@RequestParam Long studentId) {
        List<Score> scoreList = scoreSeivice.getScoreListByStuId(studentId);
        if (scoreList != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", scoreList.size());
            map.put("records", scoreList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //按班级和课程获取成绩
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @GetMapping("/getScoreListByCCId")
    public CommonResult<Map<String, Object>> getScoreListByCCId(@RequestParam int classId, int courseId) {
        List<Score> scoreList = scoreSeivice.getScoreListByCCId(classId, courseId);
        if (scoreList != null) {
            Score score = scoreList.get(0);
            Map<String, Object> label = null;
            if (score != null) {
                label = new HashMap<>();
                QueryWrapper<Quantitative> wrapper = new QueryWrapper<>();
                wrapper.eq("q_name", score.getQName());
                Quantitative q = quantitativeService.getOne(wrapper);
                label.put("uPLabel", q.getUsualScore());
                label.put("sALabel", q.getSkillScore());
                label.put("dALabel", q.getDisScore());
            }
            Map<String, Object> map = new HashMap<>();
            map.put("total", scoreList.size());
            map.put("records", scoreList);
            map.put("label", label);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取成绩排名
    @SaCheckRole(value = {"admin", "teacher", "student"},mode = SaMode.OR)
    @GetMapping("/getScoreRank")
    public CommonResult<Map<String, Object>> getScoreRank(@RequestParam int classId, int courseId) {
        List<Score> scoreList = scoreSeivice.getScoreListByCCId(classId, courseId);
        if (scoreList != null) {
            List<Score> list = scoreList.stream().sorted(Comparator.comparing(Score::getTotalScore).reversed()).toList();
            List<Map<String, Object>> list1 = new ArrayList<>();
            int rank = 1;
            for (Score score : list) {
                Map<String, Object> temp = new HashMap<>();
                temp.put("studentName", score.getStudentName());
                temp.put("totalScore", rank);
                list1.add(temp);
                rank++;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("total", list1.size());
            map.put("records", list1);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //新增成绩信息
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @PostMapping("/addScore")
    public CommonResult<String> addScore(@RequestBody Score score) {
        int result = scoreSeivice.addScore(score);
        if(result > 0) {
            return new CommonResult<>(200, "添加成功", null);
        }
        return new CommonResult<>(500, "添加失败", null);
    }

    //修改成绩信息
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @PutMapping("/updateScore")
    public CommonResult<String> updateScore(@RequestBody Score score) {
        if(score.getUsualScore() > 100) {
            score.setUsualScore(100f);
        }
        if(score.getSkillScore() > 100) {
            score.setSkillScore(100f);
        }
        if(score.getDisScore() > 100) {
            score.setDisScore(100f);
        }
        CalculateScore calculateScore = new CalculateScore(quantitativeService, score);
        float totalScore = calculateScore.calculate();
        score.setTotalScore(totalScore);
        boolean result = scoreSeivice.saveOrUpdate(score);
        if(result) {
            return new CommonResult<>(200, "修改成功", null);
        }
        return new CommonResult<>(500, "修改失败", null);
    }

    //删除成绩信息
    @DeleteMapping("/delScore/{id}")
    public CommonResult<String> delScore(@PathVariable int id) {
        int result = scoreSeivice.deleteScore(id);
        if(result > 0) {
            return new CommonResult<>(200, "添加成功", null);
        }
        return new CommonResult<>(500, "添加失败", null);
    }

    //删除成绩信息
    @DeleteMapping("/delBatchesScore")
    public CommonResult<String> delBatchesScore(@RequestBody Integer[] ids) {
        int result = scoreSeivice.delBatchesScore(Arrays.asList(ids));
        if(result > 0) {
            return new CommonResult<>(200, "添加成功", null);
        }
        return new CommonResult<>(500, "添加失败", null);
    }

    //导入
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @PostMapping("/importScore")
    public CommonResult<Map<String, Object>> importFile(MultipartFile file, @RequestParam Long id) throws IOException {
        try {
            ScoreExcelReadListener serl = new ScoreExcelReadListener(id,scoreSeivice,stuClassService,courseService,sCourseService,quantitativeService);
            EasyExcel.read(file.getInputStream(), Score.class, serl).doReadAll();
            return new CommonResult<>(200, "导入成功", serl.getResult());
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, "导入失败", null);
        }
    }

    //导出
    //导出各班级所有选课的所有学生成绩
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @GetMapping("/exportScore")
    public CommonResult<String> exportFile(@RequestParam Long id, HttpServletResponse response) throws IOException {
        List<Score> scoreList = scoreSeivice.getAllScoreList();
        String strId = String.valueOf(id);
        if(strId.length() > 1) {
            scoreList = scoreSeivice.getAllScoreListByTId(id);
        } else {
            scoreList = scoreSeivice.getAllScoreList();
        }
        try {
            // 设置响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 设置编码格式
            response.setCharacterEncoding("utf-8");
            // 设置URLEncoder.encode 防止中文乱码
            String fileName = URLEncoder.encode("课程成绩表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 写出Excel
            EasyExcel.write(response.getOutputStream(), Score.class)
                    .inMemory(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet("课程成绩表")
                    .doWrite(scoreList);
            return new CommonResult<>(200, "导出成功", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new CommonResult<>(500, "导出失败", null);
    }

    //导出该班级所有选课的成绩
    @SaCheckRole("admin")
    @GetMapping("/exportScoreByCId")
    public CommonResult<String> exportFileByCId(@RequestParam int classId, Long id, HttpServletResponse response) throws IOException {
        List<Score> scoreList = null;
        String strId = String.valueOf(id);
        if(strId.length() > 1) {
            scoreList = scoreSeivice.getAllScoreListByCTd(classId, id);
        } else {
            scoreList = scoreSeivice.getScoreListByClassId(classId);
        }
        String className = scoreList.get(0).getClassName();
        try {
            // 设置响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 设置编码格式
            response.setCharacterEncoding("utf-8");
            // 设置URLEncoder.encode 防止中文乱码
            String fileName = URLEncoder.encode(className+"课程成绩表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 写出Excel
            EasyExcel.write(response.getOutputStream(), Score.class)
                    .inMemory(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet(className+"课程成绩表")
                    .doWrite(scoreList);
            return new CommonResult<>(200, "导出成功", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CommonResult<>(500, "导出失败", null);
    }

    //导出该班级该课程的所有学生成绩
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @GetMapping("/exportScoreByCCId")
    public CommonResult<String> exportFileByCCId(@RequestParam int classId, int courseId, Long id, HttpServletResponse response) throws IOException {
        List<Score> scoreList = null;
        String strId = String.valueOf(id);
        if(strId.length() > 1) {
            scoreList = scoreSeivice.getAllScoreListByCCTId(classId, courseId, id);
        } else {
            scoreList = scoreSeivice.getScoreListByCCId(classId, courseId);
        }
        String className = scoreList.get(0).getClassName() + "《" + scoreList.get(0).getCourseName() + "》";
        try {
            // 设置响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 设置编码格式
            response.setCharacterEncoding("utf-8");
            // 设置URLEncoder.encode 防止中文乱码
            String fileName = URLEncoder.encode(className+"课程成绩表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 写出Excel
            EasyExcel.write(response.getOutputStream(), Score.class)
                    .inMemory(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet(className+"课程成绩表")
                    .doWrite(scoreList);
            return new CommonResult<>(200, "导出成功", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CommonResult<>(500, "导出失败", null);
    }

    //导出该班级该课程的所有学生成绩
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @GetMapping("/exportScoreByCourseId")
    public CommonResult<String> exportFileByCourseId(@RequestParam int courseId, Long id, HttpServletResponse response) throws IOException {
        List<Score> scoreList = null;
        String strId = String.valueOf(id);
        if(strId.length() > 1) {
            scoreList = scoreSeivice.getScoreListByCTId(courseId, id);
        } else {
            scoreList = scoreSeivice.getScoreListByCourseId(courseId);
        }
        String className = "《" + scoreList.get(0).getCourseName() + "》";
        try {
            // 设置响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 设置编码格式
            response.setCharacterEncoding("utf-8");
            // 设置URLEncoder.encode 防止中文乱码
            String fileName = URLEncoder.encode(className+"课程成绩表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 写出Excel
            EasyExcel.write(response.getOutputStream(), Score.class)
                    .inMemory(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet(className+"课程成绩表")
                    .doWrite(scoreList);
            return new CommonResult<>(200, "导出成功", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CommonResult<>(500, "导出失败", null);
    }

    //导出该学生所有课程成绩
    @SaCheckRole(value = {"admin", "teacher", "student"},mode = SaMode.OR)
    @GetMapping("/exportAllScoreBySCId")
    public CommonResult<String> exportFileByStuId(@RequestParam Long studentId, HttpServletResponse response) throws IOException {
        List<Score> scoreList = scoreSeivice.getScoreListByStuId(studentId);
        String name = scoreList.get(0).getStudentName();
        try {
            // 设置响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 设置编码格式
            response.setCharacterEncoding("utf-8");
            // 设置URLEncoder.encode 防止中文乱码
            String fileName = URLEncoder.encode(name+"课程成绩表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 写出Excel
            EasyExcel.write(response.getOutputStream(), Score.class)
                    .inMemory(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet(name+"课程成绩表")
                    .doWrite(scoreList);
            return new CommonResult<>(200, "导出成功", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CommonResult<>(500, "导出失败", null);
    }

}
