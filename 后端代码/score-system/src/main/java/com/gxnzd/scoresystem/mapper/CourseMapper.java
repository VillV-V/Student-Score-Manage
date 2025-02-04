package com.gxnzd.scoresystem.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gxnzd.scoresystem.entity.Course;
import com.gxnzd.scoresystem.utils.UserSqlProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

public interface CourseMapper extends BaseMapper<Course> {

    @Select("select c.*, s.school_name, q.q_name from course c " +
            "join school s on c.school_id = s.school_id " +
            "join quantitative q on c.q_id = q.q_id " +
            "where c.course_id = #{id} " +
            "order by c.course_id asc")
    List<Course> getCourseById(@Param("id") int id);

//    @Select("select DISTINCT c.*, s.school_name, q.q_name " +
//            "from class_course cc " +
//            "join course c on cc.course_id = c.course_id " +
//            "join school s on c.school_id = s.school_id " +
//            "join quantitative q on c.q_id = q.q_id " +
//            "where cc.teacher_id = #{teacherId} " +
//            "order by c.course_id asc")
    @SelectProvider(type = UserSqlProvider.class, method = "searchCourseByTId")
    IPage<Course> getCourseByTId(Page<Course> page, @Param("ew") QueryWrapper<Course> wrapper);  //获取教师的课程安排

    @Select("select c.*, q.q_id, q.q_name, s.school_id, s.school_name from course c " +
            "join quantitative q on c.q_id = q.q_id " +
            "join school s on c.school_id = s.school_id " +
            "order by c.course_id asc")
    List<Course> getCourseList();

    @Select("SELECT s.school_name, GROUP_CONCAT(c.course_id,'-',c.course_name ORDER BY course_name) AS course_name " +
            "FROM course c " +
            "JOIN school s ON c.school_id = s.school_id " +
            "GROUP BY c.school_id")
    List<Course> getCourseGroupList();

//    @Select("select c.*, q.q_id, q.q_name, s.school_id, s.school_name from course c " +
//            "join quantitative q on c.q_id = q.q_id " +
//            "join school s on c.school_id = s.school_id " +
//            "order by c.course_id asc")
    @SelectProvider(type = UserSqlProvider.class, method = "searchCourse")
    IPage<Course> getCoursePage(Page<Course> page, @Param("ew") QueryWrapper<Course> wrapper);

}
