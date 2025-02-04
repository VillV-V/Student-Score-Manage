package com.gxnzd.scoresystem.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gxnzd.scoresystem.entity.Quantitative;

import java.util.List;

public interface QuantitativeService extends IService<Quantitative> {

    List<Quantitative> getQuantitativeList();

    IPage<Quantitative> getQPage(Page<Quantitative> page, QueryWrapper<Quantitative> wrapper);

    int addQuantitative(Quantitative quantitative);

    int updateQuantitative(Quantitative quantitative);

    int deleteQuantitative(int id);

    int delBatchesQuantitative(List<Integer> ids);

}
