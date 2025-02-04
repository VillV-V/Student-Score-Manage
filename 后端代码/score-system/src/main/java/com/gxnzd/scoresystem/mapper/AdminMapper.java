package com.gxnzd.scoresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxnzd.scoresystem.entity.Admin;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AdminMapper extends BaseMapper<Admin> {

    @Select("select a.*, t.*, s.school_name " +
            "from admin a " +
            "join teacher t on a.teacher_id = t.teacher_id " +
            "join school s on t.school_id = s.school_id " +
            "where a.id = #{id}")
    Admin getAdminById(@Param("id") int id);

}
