package com.gxnzd.scoresystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnzd.scoresystem.entity.SCourse;
import com.gxnzd.scoresystem.entity.StuClass;
import com.gxnzd.scoresystem.service.CourseService;
import com.gxnzd.scoresystem.service.SCourseService;
import com.gxnzd.scoresystem.service.StuClassService;
import com.gxnzd.scoresystem.service.TeacherService;
import com.gxnzd.scoresystem.utils.CommonResult;
import com.gxnzd.scoresystem.utils.easyExcel.SCourseExcelReadListener;
import com.gxnzd.scoresystem.vo.SCourseVo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api")
public class SCourseController {

    @Autowired
    private SCourseService sCourseService;
    @Autowired
    private StuClassService stuClassService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private TeacherService teacherService;

    //获取所有选课信息
    @SaCheckRole("admin")
    @GetMapping("/getSCourseListByCId")
    //查询已选课程的班级信息
    public CommonResult<Map<String, Object>> getSCourseListByCId(@RequestParam int courseId) {
        List<SCourse> sCourseList = sCourseService.getSCourseListByCId(courseId);
        if(sCourseList != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", sCourseList.size());
            map.put("records", sCourseList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //查询已选课程的班级信息
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @GetMapping("/getSCourseListByCTId")
    public CommonResult<Map<String, Object>> getSCourseListByCTId(@RequestParam int courseId, Long teacherId) {
        List<SCourse> sCourseList = sCourseService.getSCourseListByCTId(courseId, teacherId);
        if(sCourseList != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", sCourseList.size());
            map.put("records", sCourseList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取教师所有授课信息
    @SaCheckRole("teacher")
    @GetMapping("/getSCourseListByTId")
    public CommonResult<Map<String, Object>> getSCourseListByTId(@RequestParam Long teacherId) {
        List<SCourse> sCourseList =sCourseService.getSCourseListByTId(teacherId);
        if(sCourseList != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", sCourseList.size());
            map.put("records", sCourseList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //按班级获取教师授课信息
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @GetMapping("/getSCourseListById")
    public CommonResult<Map<String, Object>> getSCourseListById(@RequestParam int classId, Long teacherId) {
        List<SCourse> sCourseList;
        if (teacherId != null) {
            sCourseList = sCourseService.getSCourseListById(classId, teacherId);
        } else {
            sCourseList = sCourseService.getSCourseListByClassId(classId);
        }
        if(sCourseList != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", sCourseList.size());
            map.put("records", sCourseList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取所有选课信息分页
    @SaCheckRole("admin")
    @GetMapping("/getSCoursePage")
    public CommonResult<Map<String, Object>> getSCoursePage(@RequestParam int currentPage, int size) {
        Page<StuClass> page = new Page<>(currentPage, size);
        QueryWrapper<StuClass> wrapper = new QueryWrapper<>();
        IPage<StuClass> stuClassList = stuClassService.getStuClassPage(page, wrapper);
        if(stuClassList != null) {
            List<SCourseVo> sCourseVoList = new ArrayList<>();
            for (StuClass stuClass : stuClassList.getRecords()) {
                SCourseVo sCourseVo = new SCourseVo();
                BeanUtils.copyProperties(stuClass, sCourseVo);
                //根据班级编号获取对应的选课记录
                List<SCourse> sCourseList = sCourseService.getSCourseListByClassId(sCourseVo.getClassId());
                sCourseVo.setScourseList(sCourseList);
                sCourseVoList.add(sCourseVo);
            }
            if(!sCourseVoList.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                map.put("total", sCourseVoList.size());
                map.put("records", sCourseVoList);
                return new CommonResult<>(200, "获取信息成功", map);
            }
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //添加选课
    @SaCheckRole("admin")
    @PostMapping("/addSCourse")
    public CommonResult<Integer> addSCourse(@RequestBody SCourse sCourse) {
        if (courseValid(sCourse)) return new CommonResult<>(500, "添加失败，已经选择过该课程", null);
        int result = sCourseService.addSCourse(sCourse);
        if(result > 0) {
            return new CommonResult<>(200, "添加成功", null);
        }
        return new CommonResult<>(500, "添加失败", null);
    }

    //修改选课信息
    @SaCheckRole("admin")
    @PutMapping("/updateSCourse")
    public CommonResult<Integer> updateSCourse(@RequestBody SCourse sCourse) {
        if (sCourse.getCourseId() != null) {
            if (courseValid(sCourse)) return new CommonResult<>(500, "添加失败，已经选择过该课程", null);
        }
        int result = sCourseService.updateSCourse(sCourse);
        if(result > 0) {
            return new CommonResult<>(200, "修改成功", null);
        }
        return new CommonResult<>(500, "修改失败", null);
    }

    private boolean courseValid(SCourse sCourse) {
        QueryWrapper<SCourse> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("class_id", sCourse.getClassId());
        wrapper1.eq("course_id", sCourse.getCourseId());
        List<SCourse> list = sCourseService.list(wrapper1);
        return !list.isEmpty();
    }

    //删除选课
    @SaCheckRole("admin")
    @DeleteMapping("/delSCourse/{id}")
    public CommonResult<Integer> delSCourse(@PathVariable int id) {
        int result = sCourseService.delSCourse(id);
        if(result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //批量删除选课
    @SaCheckRole("admin")
    @DeleteMapping("/delBatchesSCourse")
    public CommonResult<Integer> delBatchesSCourse(@RequestBody Integer[] ids) {
        int result = sCourseService.delBatchesSCourse(Arrays.asList(ids));
        if(result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //导入
    @SaCheckRole("admin")
    @PostMapping("/importSCourse")
    public CommonResult<Map<String, Object>> importFile(MultipartFile file) throws IOException {
        try {
            SCourseExcelReadListener scerl = new SCourseExcelReadListener(sCourseService, stuClassService, teacherService, courseService);
            EasyExcel.read(file.getInputStream(), SCourseVo.class, scerl).doReadAll();
            return new CommonResult<>(200, "导入成功", scerl.getResult());
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, "导入失败", null);
        }
    }

    //导出
    @SaCheckRole("admin")
    @GetMapping("/exportSCourse")
    public CommonResult<String> exportFile(HttpServletResponse response) throws IOException {
        List<SCourseVo> sCourseList = sCourseService.getSCourseList();
        try {
            // 设置响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 设置编码格式
            response.setCharacterEncoding("utf-8");
            // 设置URLEncoder.encode 防止中文乱码
            String fileName = URLEncoder.encode("选课信息表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 写出Excel
            EasyExcel.write(response.getOutputStream(), SCourseVo.class)
                    .inMemory(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet("选课信息表")
                    .doWrite(sCourseList);
            return new CommonResult<>(200, "导出成功", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CommonResult<>(500, "导出失败", null);
    }
    
}
