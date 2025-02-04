package com.gxnzd.scoresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxnzd.scoresystem.entity.Quantitative;
import com.gxnzd.scoresystem.mapper.QuantitativeMapper;
import com.gxnzd.scoresystem.service.QuantitativeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuantitativeServiceImpl extends ServiceImpl<QuantitativeMapper, Quantitative> implements QuantitativeService {

    @Autowired
    private QuantitativeMapper quantitativeMapper;

    @Override
    public List<Quantitative> getQuantitativeList() {
        return quantitativeMapper.selectList(null);
    }

    @Override
    public IPage<Quantitative> getQPage(Page<Quantitative> page, QueryWrapper<Quantitative> wrapper) {
        return quantitativeMapper.selectPage(page, wrapper);
    }

    @Override
    public int addQuantitative(Quantitative quantitative) {
        return quantitativeMapper.insert(quantitative);
    }

    @Override
    public int updateQuantitative(Quantitative quantitative) {
        return quantitativeMapper.updateById(quantitative);
    }

    @Override
    public int deleteQuantitative(int id) {
        return quantitativeMapper.deleteById(id);
    }

    @Override
    public int delBatchesQuantitative(List<Integer> ids) {
        return quantitativeMapper.deleteBatchIds(ids);
    }

}
