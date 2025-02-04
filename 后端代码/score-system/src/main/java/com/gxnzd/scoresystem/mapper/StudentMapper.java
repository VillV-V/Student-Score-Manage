package com.gxnzd.scoresystem.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnzd.scoresystem.entity.Student;
import com.gxnzd.scoresystem.utils.UserSqlProvider;
import com.gxnzd.scoresystem.vo.StudentVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;

public interface StudentMapper extends BaseMapper<Student> {

    @Select("select s.*, c.class_name, m.major_name, s1.school_name from student s " +
            "join class c on s.class_id = c.class_id " +
            "join major m on c.major_id = m.major_id " +
            "join school s1 on m.school_id = s1.school_id " +
            "where s.student_id = #{id} " +
            "order by s.student_id asc")
    List<StudentVo> getStudentById(@Param("id") Long id);

    @Select("select s.*, c.class_name, m.major_name, s1.school_name from student s " +
            "join class c on s.class_id = c.class_id " +
            "join major m on c.major_id = m.major_id " +
            "join school s1 on m.school_id = s1.school_id " +
            "order by s.student_id asc")
    List<Student> getStudentList();

//    @Select("select s.*, c.class_name, m.major_name, s1.school_name from student s " +
//            "join class c on s.class_id = c.class_id " +
//            "join major m on c.major_id = m.major_id " +
//            "join school s1 on m.school_id = s1.school_id " +
//            "order by s.student_id asc")
    @SelectProvider(type = UserSqlProvider.class, method = "searchStudent")
    IPage<StudentVo> getStudentPage(Page<Student> page, @Param("ew") QueryWrapper<Student> wrapper);

}
