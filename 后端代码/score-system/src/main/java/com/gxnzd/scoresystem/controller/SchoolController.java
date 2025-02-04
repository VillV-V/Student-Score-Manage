package com.gxnzd.scoresystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnzd.scoresystem.entity.School;
import com.gxnzd.scoresystem.service.SchoolService;
import com.gxnzd.scoresystem.utils.CommonResult;
import com.gxnzd.scoresystem.utils.easyExcel.SchoolExcelReadListener;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SchoolController {
    
    @Autowired
    private SchoolService schoolService;

    //获取学院分页信息
    @SaCheckRole("admin")
    @GetMapping("/getSchoolPage")
    public CommonResult<IPage<School>> getSchoolList(@RequestParam int currentPage, int size, String keyword) {
        Page<School> page = new Page<>(currentPage, size);
        QueryWrapper<School> wrapper = new QueryWrapper<>();
        if(keyword != null && !keyword.isEmpty()) {
            if(isNumber(keyword)) {
                wrapper.like("school_id", Integer.parseInt(keyword));
            } else {
                wrapper.like("school_name", keyword);
            }
        }
        IPage<School> schoolList = schoolService.getSchoolPage(page, wrapper);
        if (!schoolList.getRecords().isEmpty()) {
            return new CommonResult<>(200, "获取信息成功", schoolList);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取学院全部信息
    @SaCheckRole("admin")
    @GetMapping("/getSchoolList")
    public CommonResult<Map<String, Object>> getSchoolList() {
        List<School> schoolList = schoolService.getSchoolList();
        if(!schoolList.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", schoolList.size());
            map.put("records", schoolList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    private boolean schoolValid(School school) {
        QueryWrapper<School> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("school_name", school.getSchoolName());
        List<School> list = schoolService.list(wrapper1);
        return !list.isEmpty();
    }

    //添加学院
    @SaCheckRole("admin")
    @PostMapping("/addSchool")
    public CommonResult<Integer> addSchool(@RequestBody School school) {
        if (schoolValid(school)) return new CommonResult<>(500, "添加失败，已经存在该学院", null);
        int result = schoolService.addSchool(school);
        if(result > 0) {
            return new CommonResult<>(200, "添加成功", null);
        }
        return new CommonResult<>(500, "添加失败", null);
    }

    //修改学院信息
    @SaCheckRole("admin")
    @PutMapping("/updateSchool")
    public CommonResult<Integer> updateSchool(@RequestBody School school) {
        if (schoolValid(school)) return new CommonResult<>(500, "添加失败，已经存在该学院", null);
        int result = schoolService.updateSchool(school);
        if(result > 0) {
            return new CommonResult<>(200, "修改成功", null);
        }
        return new CommonResult<>(500, "修改失败", null);
    }

    //删除学院
    @SaCheckRole("admin")
    @DeleteMapping("/delSchool/{id}")
    public CommonResult<Integer> delSchool(@PathVariable String id) {
        try {
            int result = schoolService.delSchool(Integer.parseInt(id));
            if(result > 0) {
                return new CommonResult<>(200, "删除成功", null);
            }
        } catch (Exception e) {
            return new CommonResult<>(500, "删除失败", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //批量删除学院
    @SaCheckRole("admin")
    @DeleteMapping("/delBatchesSchool")
    public CommonResult<Integer> delBatchesSchool(@RequestBody Integer[] ids) {
        int result = schoolService.delBatchesSchool(Arrays.asList(ids));
        if(result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //导入
    @SaCheckRole("admin")
    @PostMapping("/importSchool")
    public CommonResult<Map<String, Object>> importFile(MultipartFile file) throws IOException {
        try {
            SchoolExcelReadListener serl = new SchoolExcelReadListener(schoolService);
            EasyExcel.read(file.getInputStream(), School.class, serl).doReadAll();
            return new CommonResult<>(200, "导入成功", serl.getResult());
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, "导入失败", null);
        }
    }

    //导出
    @SaCheckRole("admin")
    @GetMapping("/exportSchool")
    public CommonResult<String> exportFile(HttpServletResponse response) throws IOException {
        List<School> schoolList = schoolService.getSchoolList();
        try {
            // 设置响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 设置编码格式
            response.setCharacterEncoding("utf-8");
            // 设置URLEncoder.encode 防止中文乱码
            String fileName = URLEncoder.encode("学院信息表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 写出Excel
            EasyExcel.write(response.getOutputStream(), School.class)
                    .inMemory(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet("学院信息表")
                    .doWrite(schoolList);
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
