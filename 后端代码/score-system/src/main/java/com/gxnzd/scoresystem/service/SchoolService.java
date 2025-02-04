package com.gxnzd.scoresystem.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gxnzd.scoresystem.entity.School;

import java.util.List;

public interface SchoolService extends IService<School> {

    //获取所有学院信息
    List<School> getSchoolList();

    IPage<School> getSchoolPage(Page<School> page, QueryWrapper<School> wrapper);

    //添加学院
    int addSchool(School school);

    //修改学院信息
    int updateSchool(School school);

    int delSchool(int id);

    //批量删除学院
    int delBatchesSchool(List<Integer> ids);
    
}
