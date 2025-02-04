package com.gxnzd.scoresystem.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnzd.scoresystem.entity.Teacher;
import com.gxnzd.scoresystem.utils.UserSqlProvider;
import com.gxnzd.scoresystem.vo.TeacherVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

public interface TeacherMapper extends BaseMapper<Teacher> {

    @Select("select t.*, s.school_name " +
            "from teacher t join school s on t.school_id = s.school_id " +
            "where t.teacher_id = #{id}")
    List<TeacherVo> getTeacherById(@Param("id") Long id);

    @Select("select t.*, s.school_name " +
            "from teacher t join school s on t.school_id = s.school_id " +
            "order by t.teacher_id asc")
    List<Teacher> getTeacherList();

    @Select("SELECT s.school_name, GROUP_CONCAT(t.teacher_id,'-',t.teacher_name ORDER BY teacher_name) AS teacher_name " +
            "FROM teacher t " +
            "JOIN school s ON t.school_id = s.school_id " +
            "GROUP BY t.school_id;")
    List<TeacherVo> getTeacherGroupList();

    // 自定义分页查询
//    @Select("select t.*, s.school_name " +
//            "from teacher t " +
//            "join school s on t.school_id = s.school_id " +
//            "order by t.teacher_id asc")
    @SelectProvider(type = UserSqlProvider.class, method = "searchTeacher")
    IPage<TeacherVo> getTeacherPage(Page<Teacher> page, @Param("ew") QueryWrapper<Teacher> wrapper);

}
