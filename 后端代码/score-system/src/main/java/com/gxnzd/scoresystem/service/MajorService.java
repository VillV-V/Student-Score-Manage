package com.gxnzd.scoresystem.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gxnzd.scoresystem.entity.Major;

import java.util.List;

public interface MajorService extends IService<Major> {

    //获取所有专业信息
    List<Major> getMajorList();

    IPage<Major> getMajorPage(Page<Major> page, QueryWrapper<Major> wrapper);

    //添加专业
    int addMajor(Major major);

    //修改专业信息
    int updateMajor(Major major);

    //删除专业
    int delMajor(int id);

    //批量删除专业
    int delBatchesMajor(List<Integer> ids);

    //导入
    boolean saveOrUpdateMajor(List<Major> majors);

}
