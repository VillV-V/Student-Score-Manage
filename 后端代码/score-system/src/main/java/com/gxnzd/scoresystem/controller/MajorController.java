package com.gxnzd.scoresystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnzd.scoresystem.entity.Major;
import com.gxnzd.scoresystem.service.MajorService;
import com.gxnzd.scoresystem.service.SchoolService;
import com.gxnzd.scoresystem.utils.CommonResult;
import com.gxnzd.scoresystem.utils.easyExcel.MajorExcelReadListener;
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
public class MajorController {
    
    @Autowired
    private MajorService majorService;
    @Autowired
    private SchoolService schoolService;

    //获取所有专业信息
    @SaCheckRole("admin")
    @GetMapping("/getMajorList")
    public CommonResult<Map<String, Object>> getMajorList() {
        List<Major> majorList = majorService.getMajorList();
        if(!majorList.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", majorList.size());
            map.put("records", majorList);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //获取专业分页数据
    @SaCheckRole("admin")
    @GetMapping("/getMajorPage")
    public CommonResult<IPage<Major>> getMajorPage(@RequestParam int currentPage, int size, String keyword) {
        Page<Major> page = new Page<>(currentPage, size);
        QueryWrapper<Major> wrapper = new QueryWrapper<>();
        if(keyword != null && !keyword.isEmpty()) {
            if(isNumber(keyword)) {
                wrapper.like("major_id", Integer.parseInt(keyword));
            } else {
                wrapper.like("major_name", keyword)
                        .or().like("s.school_name", keyword);
            }
        }
        IPage<Major> majorList = majorService.getMajorPage(page, wrapper);
        if (!majorList.getRecords().isEmpty()) {
            return new CommonResult<>(200, "获取信息成功", majorList);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    private boolean majorValid(Major major) {
        QueryWrapper<Major> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("major_name", major.getMajorName());
        List<Major> list = majorService.list(wrapper1);
        return !list.isEmpty();
    }

    //添加专业
    @SaCheckRole("admin")
    @PostMapping("/addMajor")
    public CommonResult<Integer> addMajor(@RequestBody Major major) {
        if (majorValid(major)) return new CommonResult<>(500, "添加失败，已经存在该专业", null);
        int result = majorService.addMajor(major);
        if(result > 0) {
            return new CommonResult<>(200, "添加成功", null);
        }
        return new CommonResult<>(500, "添加失败", null);
    }

    //修改专业信息
    @SaCheckRole("admin")
    @PutMapping("/updateMajor")
    public CommonResult<Integer> updateMajor(@RequestBody Major major) {
        if (major.getMajorName() != null) {
            if (majorValid(major)) return new CommonResult<>(500, "添加失败，已经存在该专业", null);
        }
        int result = majorService.updateMajor(major);
        if(result > 0) {
            return new CommonResult<>(200, "修改成功", null);
        }
        return new CommonResult<>(500, "修改失败", null);
    }

    //删除专业
    @SaCheckRole("admin")
    @DeleteMapping("/delMajor/{id}")
    public CommonResult<Integer> delMajor(@PathVariable int id) {
        int result = majorService.delMajor(id);
        if(result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //批量删除专业
    @SaCheckRole("admin")
    @DeleteMapping("/delBatchesMajor")
    public CommonResult<Integer> delMajor(@RequestBody Integer[] ids) {
        int result = majorService.delBatchesMajor(Arrays.asList(ids));
        if(result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //导入
    @SaCheckRole("admin")
    @PostMapping("/importMajor")
    public CommonResult<Map<String, Object>> importFile(MultipartFile file) throws IOException {
        try {
            MajorExcelReadListener merl = new MajorExcelReadListener(schoolService, majorService);
            EasyExcel.read(file.getInputStream(), Major.class, merl).doReadAll();
            return new CommonResult<>(200, "导入成功", merl.getResult());
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, "导入失败", null);
        }
    }

    //导出
    @SaCheckRole("admin")
    @GetMapping("/exportMajor")
    public CommonResult<String> exportFile(HttpServletResponse response) throws IOException {
        List<Major> majorList = majorService.getMajorList();
        try {
            // 设置响应类型
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // 设置编码格式
            response.setCharacterEncoding("utf-8");
            // 设置URLEncoder.encode 防止中文乱码
            String fileName = URLEncoder.encode("专业信息表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            // 设置响应头
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            // 写出Excel
            EasyExcel.write(response.getOutputStream(), Major.class)
                    .inMemory(true)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet("专业信息表")
                    .doWrite(majorList);
            return new CommonResult<>(200, "导出成功", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CommonResult<>(500, "导出失败", null);
    }

    //判断字符串是否可以转换整数
    public static boolean isNumber(String str) {
        try {
            Long.parseLong(str);  // 尝试将字符串解析为整数
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
}
