package com.gxnzd.scoresystem.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnzd.scoresystem.entity.Major;
import com.gxnzd.scoresystem.utils.UserSqlProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

public interface MajorMapper extends BaseMapper<Major> {

    @Select("select m.*, s.school_name from major m " +
            "join school s on m.school_id = s.school_id " +
            "order by m.major_id asc")
    List<Major> getMajorList();

//    @Select("select m.*, s.school_name from major m " +
//            "join school s on m.school_id = s.school_id " +
//            "order by m.major_id asc")
    @SelectProvider(type = UserSqlProvider.class, method = "searchMajor")
    IPage<Major> getMajorPage(Page<Major> page, @Param("ew") QueryWrapper<Major> wrapper);

}
