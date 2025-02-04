package com.gxnzd.scoresystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnzd.scoresystem.entity.Student;
import com.gxnzd.scoresystem.service.StuClassService;
import com.gxnzd.scoresystem.service.StudentService;
import com.gxnzd.scoresystem.utils.CommonResult;
import com.gxnzd.scoresystem.utils.easyExcel.StudentExcelReadListener;
import com.gxnzd.scoresystem.vo.StudentVo;
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
public class StudentController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private StuClassService stuClassService;

    //根据学号获取学生信息
    @SaCheckRole(value = {"admin", "student"},mode = SaMode.OR)
    @GetMapping("/getStudentById/{studentId}")
    public  CommonResult<Map<String, Object>> getStudentById(@PathVariable Long studentId) {
        List<StudentVo> student = studentService.getStudentById(studentId);
        if(student != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", student.size());
            map.put("records", student);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //根据班级获取学生信息
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @GetMapping("/getStudentByClassId/{id}")
    public  CommonResult<Map<String, Object>> getStudentByClassId(@PathVariable String id) {
        QueryWrapper<Student> wrapper = new QueryWrapper<>();
        wrapper.eq("class_id", Long.parseLong(id));
        List<Student> students = studentService.list(wrapper);
        if(students != null) {
            Map<String, Object> map = new HashMap<>();
            List<StudentVo> studentVoList = new ArrayList<>();
            for(Student student : students) {
                StudentVo studentVo = new StudentVo();
                BeanUtils.copyProperties(student, studentVo);
                studentVoList.add(studentVo);
            }
            map.put("total", studentVoList.size());
            map.put("records", studentVoList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取所有学生信息
    @SaCheckRole("admin")
    @GetMapping("/getStudentList")
    public CommonResult<Map<String, Object>> getStudentList() {
        List<Student> studentList = studentService.getStudentList();
        if(!studentList.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            List<StudentVo> studentVoList = new ArrayList<>();
            for(Student student : studentList) {
                StudentVo studentVo = new StudentVo();
                BeanUtils.copyProperties(student, studentVo);
                studentVoList.add(studentVo);
            }
            map.put("total", studentVoList.size());
            map.put("records", studentVoList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取学生信息分页
    @SaCheckRole("admin")
    @GetMapping("/getStudentPage")
    public CommonResult<IPage<StudentVo>> getStudentPage(@RequestParam int currentPage, int size, String keyword) {
        Page<Student> page = new Page<>(currentPage, size);
        QueryWrapper<Student> wrapper = new QueryWrapper<>();
        if(keyword != null && !keyword.isEmpty()) {
            if(isNumber(keyword)) {
                wrapper.like("student_id", Long.parseLong(keyword));
            } else {
                wrapper.like("student_name", keyword)
                        .or().like("gender", keyword)
                        .or().like("telephone", keyword)
                        .or().like("email", keyword)
                        .or().like("c.class_name", keyword)
                        .or().like("m.major_name", keyword)
                        .or().like("s1.school_name", keyword);
            }
        }
        IPage<StudentVo> studentList = studentService.getStudentPage(page, wrapper);
        if(!studentList.getRecords().isEmpty()) {
            return new CommonResult<>(200, "获取信息成功", studentList);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //添加学生
    @SaCheckRole("admin")
    @PostMapping("/addStudent")
    public CommonResult<Integer> addStudent(@RequestBody Student student) {
        int result = studentService.addStudent(student);
        if(result > 0) {
            return new CommonResult<>(200, "添加成功", null);
        }
        return new CommonResult<>(500, "添加失败", null);
    }

    //修改学生个人信息
    @SaCheckRole(value = {"admin", "student"},mode = SaMode.OR)
    @PutMapping("/updateStudent")
    public CommonResult<Integer> updateStudent(@RequestBody Student student) {
        int result = studentService.updateStudent(student);
        if(result > 0) {
            return new CommonResult<>(200, "修改成功", null);
        }
        return new CommonResult<>(500, "修改失败", null);
    }

    //修改密码
    @SaCheckRole(value = {"admin", "student"},mode = SaMode.OR)
    @PutMapping("/updateStuPwd")
    public CommonResult<Integer> updatePassword(@RequestBody Student student) {
        Student student1 = studentService.getById(student.getStudentId());
        if(student1.getPassword().equals(student.getPassword())) {
            return new CommonResult<>(500, "修改失败，新密码不能与原密码相同！", null);
        }
        int result = studentService.updatePassword(student);
        if(result > 0) {
            return new CommonResult<>(200, "修改密码成功", null);
        }
        return new CommonResult<>(500, "修改密码失败", null);
    }

    //删除学生
    @SaCheckRole("admin")
    @DeleteMapping("/delStudent/{id}")
    public CommonResult<Long> delStudent(@PathVariable String id) {
        int result = studentService.delStudent(Long.valueOf(id));
        if(result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //批量删除学生
    @SaCheckRole("admin")
    @DeleteMapping("/delBatchesStudent")
    public CommonResult<Long> delBatchesStudent(@RequestBody Long[] ids) {
        int result = studentService.delBatchesStudent(Arrays.asList(ids));
        if(result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //导入
    @SaCheckRole("admin")
    @PostMapping("/importStudent")
    public CommonResult<Map<String, Object>> importFile(MultipartFile file) throws IOException {
        try {
            StudentExcelReadListener serl = new StudentExcelReadListener(stuClassService, studentService);
            EasyExcel.read(file.getInputStream(), Student.class, serl).doReadAll();
            return new CommonResult<>(200, "导入成功", serl.getResult());
        } catch (Exception e) {
            return new CommonResult<>(500, "导入失败", null);
        }
    }

    //导出
    @SaCheckRole("admin")
    @GetMapping("/exportStudent")
    public CommonResult<String> exportFile(HttpServletResponse response) throws IOException {
        List<Student> studentList = studentService.getStudentList();
        List<StudentVo> studentVoList = new ArrayList<>();
        for(Student student : studentList) {
            StudentVo studentVo = new StudentVo();
            BeanUtils.copyProperties(student, studentVo);
            studentVoList.add(studentVo);
        }
        try {
            // 设置响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 设置编码格式
            response.setCharacterEncoding("utf-8");
            // 设置URLEncoder.encode 防止中文乱码
            String fileName = URLEncoder.encode("学生信息表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 写出Excel
            EasyExcel.write(response.getOutputStream(), StudentVo.class)
                    .inMemory(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet("学生信息表")
                    .doWrite(studentVoList);
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
