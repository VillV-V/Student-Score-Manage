package com.gxnzd.scoresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxnzd.scoresystem.entity.StuClass;
import com.gxnzd.scoresystem.mapper.StuClassMapper;
import com.gxnzd.scoresystem.service.StuClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StuClassServiceImpl extends ServiceImpl<StuClassMapper, StuClass> implements StuClassService {

    @Autowired
    private StuClassMapper stuClassMapper;

    @Override
    public List<StuClass> getStuClassList() {
        return stuClassMapper.getStuClassList();
    }

    @Override
    public IPage<StuClass> getStuClassPage(IPage<StuClass> page, QueryWrapper<StuClass> wrapper) {
        return stuClassMapper.getStuClassPage(page, wrapper);
    }

    @Override
    public List<StuClass> getStuClassByIdPage(Long id) {
        return stuClassMapper.getStuClassByIdPage(id);
    }

    @Override
    public int addStuClass(StuClass stuClass) {
        return stuClassMapper.insert(stuClass);
    }

    @Override
    public int updateStuClass(StuClass stuClass) {
        return stuClassMapper.updateById(stuClass);
    }

    @Override
    public int delStuClass(int id) {
        return stuClassMapper.deleteById(id);
    }

    @Override
    public int delBatchesStuClass(List<Integer> ids) {
        return stuClassMapper.deleteBatchIds(ids);
    }

}
