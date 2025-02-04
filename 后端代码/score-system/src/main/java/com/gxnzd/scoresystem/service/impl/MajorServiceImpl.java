package com.gxnzd.scoresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxnzd.scoresystem.entity.Major;
import com.gxnzd.scoresystem.mapper.MajorMapper;
import com.gxnzd.scoresystem.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {

    @Autowired
    private MajorMapper majorMapper;

    @Override
    public List<Major> getMajorList() {
        return majorMapper.getMajorList();
    }

    @Override
    public IPage<Major> getMajorPage(Page<Major> page, QueryWrapper<Major> wrapper) {
        return majorMapper.getMajorPage(page, wrapper);
    }

    @Override
    public int addMajor(Major major) {
        return majorMapper.insert(major);
    }

    @Override
    public int updateMajor(Major major) {
        return majorMapper.updateById(major);
    }

    @Override
    public int delMajor(int id) {
        return majorMapper.deleteById(id);
    }

    @Override
    public int delBatchesMajor(List<Integer> ids) {
        return majorMapper.deleteBatchIds(ids);
    }

    @Override
    public boolean saveOrUpdateMajor(List<Major> majors) {
        return super.saveOrUpdateBatch(majors);
    }

}