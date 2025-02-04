package com.gxnzd.scoresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxnzd.scoresystem.entity.Admin;
import com.gxnzd.scoresystem.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface LoginMapper extends BaseMapper<User> {

    @Select("select id, username, password from admin where username = #{username}")
    User adminLogin(@Param("username") String username);

    @Select("select teacher_id, teacher_name, password from teacher where teacher_id = #{username}")
    User teacherLogin(@Param("username") Long username);

    @Select("select student_id, student_name, password from student where student_id = #{username}")
    User studentLogin(@Param("username") Long username);

}
