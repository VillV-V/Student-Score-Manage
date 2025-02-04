package com.gxnzd.scoresystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnzd.scoresystem.entity.Teacher;
import com.gxnzd.scoresystem.service.SchoolService;
import com.gxnzd.scoresystem.service.TeacherService;
import com.gxnzd.scoresystem.utils.CommonResult;
import com.gxnzd.scoresystem.utils.easyExcel.TeacherExcelReadListener;
import com.gxnzd.scoresystem.vo.TeacherVo;
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
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private SchoolService schoolService;

    //根据教师工号获取教师个人信息
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @GetMapping("/getTeacherById/{teacherId}")
    public CommonResult<Map<String, Object>> getTeacherById(@PathVariable Long teacherId) {
        List<TeacherVo> teacher = teacherService.getTeacherById(teacherId);
        if(teacher != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", teacher.size());
            map.put("records", teacher);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取所有教师信息分页
    @SaCheckRole("admin")
    @GetMapping("/getTeacherPage")
    public CommonResult<IPage<TeacherVo>> getTeacherPage(@RequestParam int currentPage, int size, String keyword) {
        Page<Teacher> page = new Page<>(currentPage, size);
        QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
        if(keyword != null && !keyword.isEmpty()) {
            if(isNumber(keyword)) {
                wrapper.like("teacher_id", Long.parseLong(keyword));
            } else {
                wrapper.like("teacher_name", keyword)
                        .or().like("gender", keyword)
                        .or().like("title", keyword)
                        .or().like("telephone", keyword)
                        .or().like("email", keyword)
                        .or().like("s.school_name", keyword);
            }
        }
        IPage<TeacherVo> teacherList = teacherService.getTeacherPage(page, wrapper);
        if(!teacherList.getRecords().isEmpty()) {
            return new CommonResult<>(200, "获取信息成功", teacherList);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取所有教师信息
    @SaCheckRole("admin")
    @GetMapping("/getTeacherList")
    public CommonResult<Map<String, Object>> getTeacherList() {
        List<Teacher> teacherList = teacherService.getTeacherList();
        if(!teacherList.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            List<TeacherVo> teacherVoList = new ArrayList<>();
            for(Teacher teacher : teacherList) {
                TeacherVo teacherVo = new TeacherVo();
                BeanUtils.copyProperties(teacher, teacherVo);
                teacherVoList.add(teacherVo);
            }
            map.put("total", teacherVoList.size());
            map.put("records", teacherVoList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取教师分组列表，按学院分组
    @SaCheckRole("admin")
    @GetMapping("/getTeacherGroupList")
    public CommonResult<Map<String, Object>> getTeacherGroupList() {
        List<TeacherVo> teacherVos = teacherService.getTeacherGroupList();
        if(!teacherVos.isEmpty()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for(TeacherVo teacherVo : teacherVos) {
                Map<String, Object> map = new HashMap<>();
                String schoolName = teacherVo.getSchoolName();
                String[] arr = teacherVo.getTeacherName().split(",");
                List<Map<String, Object>> list1 = new ArrayList<>();
                for(String s : arr) {
                    Long id = Long.parseLong(s.split("-")[0]);
                    Map<String, Object> tMap = new HashMap<>();
                    tMap.put("teacherId", id);
                    tMap.put("teacherName", s);
                    list1.add(tMap);
                }
                map.put("schoolName", schoolName);
                map.put("teacherList", list1);
                list.add(map);
            }
            Map<String, Object> map2 = new HashMap<>();
            map2.put("records", list);
            return new CommonResult<>(200, "获取信息成功", map2);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //添加教师
    @SaCheckRole("admin")
    @PostMapping("/addTeacher")
    public CommonResult<Integer> addTeacher(@RequestBody Teacher teacher) {
        int result = teacherService.addTeacher(teacher);
        if(result > 0) {
            return new CommonResult<>(200, "添加成功", null);
        }
        return new CommonResult<>(500, "添加失败", null);
    }

    //修改教师个人信息
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @PutMapping("/updateTeacher")
    public CommonResult<Integer> updateTeacher(@RequestBody Teacher teacher) {
        int result = teacherService.updateTeacher(teacher);
        if(result > 0) {
            return new CommonResult<>(200, "修改成功", null);
        }
        return new CommonResult<>(500, "修改失败", null);
    }

    //修改密码
    @SaCheckRole(value = {"admin", "teacher"},mode = SaMode.OR)
    @PutMapping("/updateTeaPwd")
    public CommonResult<Integer> updatePassword(@RequestBody Teacher teacher) {
        Teacher teacher1 = teacherService.getById(teacher.getTeacherId());
        if(teacher1.getPassword().equals(teacher.getPassword())) {
            return new CommonResult<>(500, "修改失败，新密码不能与原密码相同！", null);
        }
        int result = teacherService.updatePassword(teacher);
        if(result > 0) {
            return new CommonResult<>(200, "修改密码成功", null);
        }
        return new CommonResult<>(500, "修改密码失败", null);
    }

    //删除教师
    @SaCheckRole("admin")
    @DeleteMapping("/delTeacher/{id}")
    public CommonResult<Long> delTeacher(@PathVariable String id) {
        int result = teacherService.deleteTeacher(Long.parseLong(id));
        if(result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //批量删除教师
    @SaCheckRole("admin")
    @DeleteMapping("/delBatchesTeacher")
    public CommonResult<Long> delBatchesTeacher(@RequestBody Long[] ids) {
        int result = teacherService.delBatchesTeacher(Arrays.asList(ids));
        if(result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //导入
    @SaCheckRole("admin")
    @PostMapping("/importTeacher")
    public CommonResult<Map> importFile(MultipartFile file) throws IOException {
        try {
            TeacherExcelReadListener terl = new TeacherExcelReadListener(schoolService,teacherService);
            EasyExcel.read(file.getInputStream(), Teacher.class, terl).doReadAll();
            return new CommonResult<>(200, "导入成功", terl.getResult());
        } catch (Exception e) {
            return new CommonResult<>(500, "导入失败", null);
        }
    }

    //导出
    @SaCheckRole("admin")
    @GetMapping("/exportTeacher")
    public CommonResult<String> exportFile(HttpServletResponse response) throws IOException {
        List<Teacher> teacherList = teacherService.getTeacherList();
        List<TeacherVo> teacherVoList = new ArrayList<>();
        for(Teacher teacher : teacherList) {
            TeacherVo teacherVo = new TeacherVo();
            BeanUtils.copyProperties(teacher, teacherVo);
            teacherVoList.add(teacherVo);
        }
        try {
            // 设置响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 设置编码格式
            response.setCharacterEncoding("utf-8");
            // 设置URLEncoder.encode 防止中文乱码
            String fileName = URLEncoder.encode("教师信息表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 写出Excel
            EasyExcel.write(response.getOutputStream(), TeacherVo.class)
                    .inMemory(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet("教师信息表")
                    .doWrite(teacherVoList);
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
