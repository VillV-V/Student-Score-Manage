package com.gxnzd.scoresystem.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gxnzd.scoresystem.entity.StuClass;
import com.gxnzd.scoresystem.utils.UserSqlProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

public interface StuClassMapper extends BaseMapper<StuClass> {

    @Select("select c.*, m.major_name from class c " +
            "join major m on c.major_id = m.major_id " +
            "order by c.class_id asc")
    List<StuClass> getStuClassList();

//    @Select("select c.*, m.major_name from class c " +
//            "join major m on c.major_id = m.major_id " +
//            "order by c.class_id asc")
    @SelectProvider(type = UserSqlProvider.class, method = "searchClass")
    IPage<StuClass> getStuClassPage(IPage<StuClass> page, @Param("ew") QueryWrapper<StuClass> wrapper);

    @Select("select c.*, m.major_name from class_course cc " +
            "join class c on cc.class_id = c.class_id " +
            "join major m on c.major_id = m.major_id " +
            "where cc.teacher_id =  #{id} " +
            "order by c.class_id asc")
    List<StuClass> getStuClassByIdPage(@Param("id") Long id);

}
