package com.gxnzd.scoresystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnzd.scoresystem.entity.Quantitative;
import com.gxnzd.scoresystem.service.QuantitativeService;
import com.gxnzd.scoresystem.utils.CommonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QuantitativeController {

    @Autowired
    private QuantitativeService quantitativeService;

    //成绩量化标准全部数据
    @SaCheckRole("admin")
    @GetMapping("/getQuantitativeList")
    public CommonResult<Map<String, Object>> getQuantitativeList() {
        List<Quantitative> list = quantitativeService.getQuantitativeList();
        if (!list.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            map.put("total", list.size());
            map.put("records", list);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //分页数据
    @SaCheckRole("admin")
    @GetMapping("/getQPage")
    public CommonResult<IPage<Quantitative>> getCoursePage(@RequestParam int currentPage, int size, String keyword) {
        Page<Quantitative> page = new Page<>(currentPage, size);
        QueryWrapper<Quantitative> wrapper = new QueryWrapper<>();
        if(keyword != null && !keyword.isEmpty()) {
            if(isNumber(keyword)) {
                wrapper.like("q_id", Integer.parseInt(keyword))
                        .or().like("usual_score", Integer.parseInt(keyword))
                        .or().like("skill_score", Integer.parseInt(keyword))
                        .or().like("dis_score", Integer.parseInt(keyword));
            } else {
                wrapper.like("q_name", keyword)
                        .or().like("remark", keyword);
            }
        }
        IPage<Quantitative> courseList = quantitativeService.getQPage(page, wrapper);
        if(!courseList.getRecords().isEmpty()) {
            return new CommonResult<>(200, "获取信息成功", courseList);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    private boolean quantitativeValid(Quantitative quantitative) {
        QueryWrapper<Quantitative> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("q_name", quantitative.getQName());
        List<Quantitative> list = quantitativeService.list(wrapper1);
        return !list.isEmpty();
    }

    //添加
    @SaCheckRole("admin")
    @PostMapping("/addQuantitative")
    public CommonResult<String> addQuantitative(@RequestBody Quantitative quantitative) {
        if (quantitativeValid(quantitative)) return new CommonResult<>(500, "添加失败，已经存在该标准", null);
        int result = quantitativeService.addQuantitative(quantitative);
        if (result > 0) {
            return new CommonResult<>(200, "添加成功", null);
        }
        return new CommonResult<>(500, "添加失败", null);
    }

    //修改
    @SaCheckRole("admin")
    @PutMapping("/updateQuantitative")
    public CommonResult<String> updateQuantitative(@RequestBody Quantitative quantitative) {
        if (quantitative.getQName() != null) {
            if (quantitativeValid(quantitative)) return new CommonResult<>(500, "添加失败，已经存在该标准", null);
        }
        int result = quantitativeService.updateQuantitative(quantitative);
        if (result > 0) {
            return new CommonResult<>(200, "修改成功", null);
        }
        return new CommonResult<>(500, "修改失败", null);
    }

    //删除
    @SaCheckRole("admin")
    @DeleteMapping("/deleteQuantitative/{id}")
    public CommonResult<String> deleteQuantitative(@PathVariable Integer id) {
        int result = quantitativeService.deleteQuantitative(id);
        if (result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
    }

    //批量删除
    @SaCheckRole("admin")
    @DeleteMapping("/delBatchesQuantitative")
    public CommonResult<String> delBatchesQuantitative(@RequestBody Integer[] ids) {
        int result = quantitativeService.delBatchesQuantitative(Arrays.asList(ids));
        if (result > 0) {
            return new CommonResult<>(200, "删除成功", null);
        }
        return new CommonResult<>(500, "删除失败", null);
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
