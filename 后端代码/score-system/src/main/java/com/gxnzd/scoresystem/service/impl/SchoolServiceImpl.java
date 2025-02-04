package com.gxnzd.scoresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxnzd.scoresystem.entity.School;
import com.gxnzd.scoresystem.mapper.SchoolMapper;
import com.gxnzd.scoresystem.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolServiceImpl extends ServiceImpl<SchoolMapper, School> implements SchoolService {

    @Autowired
    private SchoolMapper schoolMapper;

    @Override
    public List<School> getSchoolList() {
        return schoolMapper.selectList(null);
    }

    @Override
    public IPage<School> getSchoolPage(Page<School> page, QueryWrapper<School> wrapper) {
        return schoolMapper.selectPage(page, wrapper);
    }

    @Override
    public int addSchool(School school) {
        return schoolMapper.insert(school);
    }

    @Override
    public int updateSchool(School school) {
        return schoolMapper.updateById(school);
    }

    @Override
    public int delSchool(int id) {
        return schoolMapper.deleteById(id);
    }

    @Override
    public int delBatchesSchool(List<Integer> ids) {
        return schoolMapper.deleteBatchIds(ids);
    }


}
