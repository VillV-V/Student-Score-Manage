package com.gxnzd.scoresystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnzd.scoresystem.entity.StuClass;
import com.gxnzd.scoresystem.service.MajorService;
import com.gxnzd.scoresystem.service.StuClassService;
import com.gxnzd.scoresystem.utils.CommonResult;
import com.gxnzd.scoresystem.utils.easyExcel.StuClassExcelReadListener;
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
public class StuClassController {
    
    @Autowired
    private StuClassService stuClassService;
    @Autowired
    private MajorService majorService;

    //获取所有班级信息
    @SaCheckRole("admin")
    @GetMapping("/getStuClassList")
    public CommonResult<Map<String, Object>> getStuClassList() {
        List<StuClass> stuClassList = stuClassService.getStuClassList();
        if(!stuClassList.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", stuClassList.size());
            map.put("records", stuClassList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取班级信息分页
    @SaCheckRole("admin")
    @GetMapping("/getStuClassPage")
    public CommonResult<IPage<StuClass>> getStuClassPage(@RequestParam int currentPage, int size, String keyword) {
        Page<StuClass> page = new Page<>(currentPage, size);
        QueryWrapper<StuClass> wrapper = new QueryWrapper<>();
        if(keyword != null && !keyword.isEmpty()) {
            if(isNumber(keyword)) {
                wrapper.like("class_id", Integer.parseInt(keyword))
                        .or().like("grade", keyword);
            } else {
                wrapper.like("class_name", keyword)
                        .or().like("m.major_name", keyword);
            }
        }
        IPage<StuClass> stuClassList = stuClassService.getStuClassPage(page, wrapper);
        if(stuClassList != null) {
            return new CommonResult<>(200, "获取信息成功", stuClassList);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //根据教师工号获取班级信息
    @SaCheckRole("teacher")
    @GetMapping("/getStuClassByIdPage")
    public CommonResult<Map<String, Object>> getStuClassByIdPage(@RequestParam Long teacherId) {
        List<StuClass> stuClassList = stuClassService.getStuClassByIdPage(teacherId);
        if(stuClassList != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", stuClassList.size());
            map.put("records", stuClassList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    private boolean stuClassValid(StuClass stuClass) {
        QueryWrapper<StuClass> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("class_name", stuClass.getClassName());
        List<StuClass> list = stuClassService.list(wrapper1);
        return !list.isEmpty();
    }

    //添加班级
    @SaCheckRole("admin")
    @PostMapping("/addStuClass")
    public CommonResult<Integer> addStuClass(@RequestBody StuClass stuClass) {
        if (stuClassValid(stuClass)) return new CommonResult<>(500, "添加失败，已经存在该班级", null);
        int result = stuClassService.addStuClass(stuClass);
        if(result > 0) {
            return new CommonResult<>(200, "添加成功", null);
        }
        return new CommonResult<>(500, "添加失败", null);
    }

    //修改班级信息
    @SaCheckRole("admin")
    @PutMapping("/updateStuClass")
    public CommonResult<Integer> updateStuClass(@RequestBody StuClass stuClass) {
        if (stuClass.getClassName() != null) {
            if (stuClassValid(stuClass)) return new CommonResult<>(500, "添加失败，已经存在该班级", null);
        }
        int result = stuClassService.updateStuClass(stuClass);
        if(result > 0) {
            return new CommonResult<>(200, "修改成功", null);
        }
        return new CommonResult<>(500, "修改失败", null);
    }

    //删除班级
    @SaCheckRole("admin")
    @DeleteMapping("/delStuClass/{id}")
    public CommonResult<Integer> delStuClass(@PathVariable int id) {
        int result = stuClassService.delStuClass(id);
        if(result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //批量删除班级
    @SaCheckRole("admin")
    @DeleteMapping("/delBatchesStuClass")
    public CommonResult<Integer> delBatchesStuClass(@RequestBody Integer[] ids) {
        int result = stuClassService.delBatchesStuClass(Arrays.asList(ids));
        if(result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //导入
    @SaCheckRole("admin")
    @PostMapping("/importStuClass")
    public CommonResult<Map<String, Object>> importFile(MultipartFile file) throws IOException {
        try {
            StuClassExcelReadListener serl = new StuClassExcelReadListener(stuClassService, majorService);
            EasyExcel.read(file.getInputStream(), StuClass.class, serl).doReadAll();
            return new CommonResult<>(200, "导入成功", serl.getResult());
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, "导入失败", null);
        }
    }

    //导出
    @SaCheckRole("admin")
    @GetMapping("/exportStuClass")
    public CommonResult<String> exportFile(HttpServletResponse response) throws IOException {
        List<StuClass> stuClassList = stuClassService.getStuClassList();
        try {
            // 设置响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 设置编码格式
            response.setCharacterEncoding("utf-8");
            // 设置URLEncoder.encode 防止中文乱码
            String fileName = URLEncoder.encode("班级信息表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 写出Excel
            EasyExcel.write(response.getOutputStream(), StuClass.class)
                    .inMemory(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet("班级信息表")
                    .doWrite(stuClassList);
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
