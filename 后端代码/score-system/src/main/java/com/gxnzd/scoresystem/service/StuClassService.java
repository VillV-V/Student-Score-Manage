package com.gxnzd.scoresystem.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gxnzd.scoresystem.entity.StuClass;

import java.util.List;

public interface StuClassService extends IService<StuClass> {

    //获取所有班级信息
    List<StuClass> getStuClassList();

    IPage<StuClass> getStuClassPage(IPage<StuClass> page, QueryWrapper<StuClass> wrapper);

    List<StuClass> getStuClassByIdPage(Long id);

    //添加班级
    int addStuClass(StuClass stuClass);

    //修改班级信息
    int updateStuClass(StuClass stuClass);

    //删除班级
    int delStuClass(int id);

    //批量删除班级
    int delBatchesStuClass(List<Integer> ids);

}
