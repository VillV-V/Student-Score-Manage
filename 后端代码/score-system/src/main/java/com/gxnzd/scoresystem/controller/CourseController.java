package com.gxnzd.scoresystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnzd.scoresystem.entity.Course;
import com.gxnzd.scoresystem.service.CourseService;
import com.gxnzd.scoresystem.service.QuantitativeService;
import com.gxnzd.scoresystem.service.SchoolService;
import com.gxnzd.scoresystem.utils.CommonResult;
import com.gxnzd.scoresystem.utils.easyExcel.CourseExcelReadListener;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class CourseController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private QuantitativeService quantitativeService;
    @Autowired
    private SchoolService schoolService;

    //根据编号获取课程
    @SaCheckRole("admin")
    @GetMapping("/getCourseById/{id}")
    public CommonResult<List<Course>> getCourseById(@PathVariable String id) {
        List<Course> courseList = courseService.getCourseById(Integer.parseInt(id));
        if(!courseList.isEmpty()) {
            return new CommonResult<>(200, "获取信息成功", courseList);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //根据教师编号获取教师所授课程
    @SaCheckRole("teacher")
    @GetMapping("/getCourseByTId")
    public CommonResult<IPage<Course>> getCourseByTId(@RequestParam int currentPage, int size, Long teacherId, String keyword) {
        Page<Course> page = new Page<>(currentPage, size);
        //条件构造器
        QueryWrapper<Course> wrapper = new QueryWrapper<>();
        wrapper.eq("cc.teacher_id", teacherId);
        if(keyword != null && !keyword.isEmpty()) {
            if(isNumber(keyword)) {
                wrapper.like("c.course_id", Integer.parseInt(keyword))
                        .or().like("c.class_period", Integer.parseInt(keyword))
                        .or().like("c.practical_class_period", Integer.parseInt(keyword));
            } else {
                wrapper.like("c.course_name", keyword)
                        .or().like("c.type", keyword)
                        .or().like("s.school_name", keyword)
                        .or().like("q.q_name", keyword);
            }
        }
        //分页好的数据
        IPage<Course> courseList = courseService.getCourseByTId(page, wrapper);
        if(!courseList.getRecords().isEmpty()) {
            return new CommonResult<>(200, "获取信息成功", courseList);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取所有课程列表
    @SaCheckRole("admin")
    @GetMapping("/getCourseList")
    public CommonResult<Map<String, Object>> getCourseList() {
        List<Course> courseList = courseService.getCourseList();
        if(!courseList.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", courseList.size());
            map.put("records", courseList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取课程分组数据，按学院分组
    @SaCheckRole("admin")
    @GetMapping("/getCourseGroupList")
    public CommonResult<Map<String, Object>> getCourseGroupList() {
        List<Course> courses = courseService.getCourseGroupList();
        if(!courses.isEmpty()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for(Course course : courses) {
                Map<String, Object> map = new HashMap<>();
                String schoolName = course.getSchoolName();
                String[] arr = course.getCourseName().split(",");
                List<Map<String, Object>> list1 = new ArrayList<>();
                for(String s : arr) {
                    Long id = Long.parseLong(s.split("-")[0]);
                    Map<String, Object> tMap = new HashMap<>();
                    tMap.put("courseId", id);
                    tMap.put("courseName", s);
                    list1.add(tMap);
                }
                map.put("schoolName", schoolName);
                map.put("courseList", list1);
                list.add(map);
            }
            Map<String, Object> map2 = new HashMap<>();
            map2.put("records", list);
            return new CommonResult<>(200, "获取信息成功", map2);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //课程分页
    @SaCheckRole("admin")
    @GetMapping("/getCoursePage")
    public CommonResult<IPage<Course>> getCoursePage(@RequestParam int currentPage, int size, String keyword) {
        Page<Course> page = new Page<>(currentPage, size);
        QueryWrapper<Course> wrapper = new QueryWrapper<>();
        if(keyword != null && !keyword.isEmpty()) {
            if(isNumber(keyword)) {
                wrapper.like("course_id", Integer.parseInt(keyword))
                        .or().like("class_period", Integer.parseInt(keyword))
                        .or().like("practical_class_period", Integer.parseInt(keyword));
            } else {
                wrapper.like("course_name", keyword)
                        .or().like("type", keyword)
                        .or().like("s.school_name", keyword)
                        .or().like("q.q_name", keyword);
            }
        }
        IPage<Course> courseList = courseService.getCoursePage(page, wrapper);
        if(!courseList.getRecords().isEmpty()) {
            return new CommonResult<>(200, "获取信息成功", courseList);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //课程校验，判断是否已经存在该课程
    private boolean courseValid(Course course) {
        QueryWrapper<Course> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("course_name", course.getCourseName());
        List<Course> list = courseService.list(wrapper1);
        return !list.isEmpty();
    }

    //添加课程
    @SaCheckRole("admin")
    @PostMapping("/addCourse")
    public CommonResult<Integer> addCourse(@RequestBody Course course) {
        if (courseValid(course)) return new CommonResult<>(500, "添加失败，已经存在该课程", null);
        int result = courseService.addCourse(course);
        if(result > 0) {
            return new CommonResult<>(200, "添加成功", null);
        }
        return new CommonResult<>(500, "添加失败", null);
    }

    //修改课程信息
    @SaCheckRole("admin")
    @PutMapping("/updateCourse")
    public CommonResult<Integer> updateCourse(@RequestBody Course course) {
        if (course.getCourseName() != null) {
            if (courseValid(course)) return new CommonResult<>(500, "添加失败，已经存在该课程", null);
        }
        if(course.getSchoolId() == 0) {
            course.setSchoolId(null);
        }
        int result = courseService.updateCourse(course);
        if(result > 0) {
            return new CommonResult<>(200, "修改成功", null);
        }
        return new CommonResult<>(500, "修改失败", null);
    }

    //删除课程
    @SaCheckRole("admin")
    @DeleteMapping("/delCourse/{id}")
    public CommonResult<Integer> delCourse(@PathVariable String id) {
        int result = courseService.delCourse(Integer.parseInt(id));
        if(result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //批量删除课程
    @SaCheckRole("admin")
    @DeleteMapping("/delBatchesCourse")
    public CommonResult<Integer> delBatchesCourse(@RequestBody Integer[] ids) {
        int result = courseService.delBatchesCourse(Arrays.asList(ids));
        if(result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //导入
    @SaCheckRole("admin")
    @PostMapping("/importCourse")
    public CommonResult<Map<String, Object>> importFile(MultipartFile file) throws SQLException {
        try {
            CourseExcelReadListener cerl = new CourseExcelReadListener(courseService, schoolService, quantitativeService);
            EasyExcel.read(file.getInputStream(), Course.class, cerl).doReadAll();
            return new CommonResult<>(200, "导入成功", cerl.getResult());
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, "导入失败", null);
        }
    }

    //导出
    @SaCheckRole("admin")
    @GetMapping("/exportCourse")
    public CommonResult<String> exportFile(HttpServletResponse response) throws IOException {
        List<Course> courseList = courseService.getCourseList();
        try {
            // 设置响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 设置编码格式
            response.setCharacterEncoding("utf-8");
            // 设置URLEncoder.encode 防止中文乱码
            String fileName = URLEncoder.encode("课程信息表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 写出Excel
            EasyExcel.write(response.getOutputStream(), Course.class)
                    .inMemory(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet("课程信息表")
                    .doWrite(courseList);
            return new CommonResult<>(200, "导出成功", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CommonResult<>(500, "导出失败", null);
    }

    public static boolean isNumber(String str) {
        try {
            Long.parseLong(str);  // 尝试将字符串解析为整数
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
