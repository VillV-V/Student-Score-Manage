package com.gxnzd.scoresystem.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxnzd.scoresystem.entity.SCourse;
import com.gxnzd.scoresystem.vo.SCourseVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SCourseMapper  extends BaseMapper<SCourse> {

    @Select("select cc.*, c1.class_id, c1.class_name, c2.course_id, c2.course_name, q.q_name, t.teacher_id, t.teacher_name " +
            "from class_course cc " +
            "join class c1 on cc.class_id = c1.class_id " +
            "join course c2 on cc.course_id = c2.course_id " +
            "join quantitative q on c2.q_id = q.q_id " +
            "join teacher t on cc.teacher_id = t.teacher_id " +
            "where cc.class_id = #{id} " +
            "order by cc.course_id asc")
    List<SCourse> getSCourseListByClassId(@Param("id") int id);

    @Select("select cc.*, c1.class_id, c1.class_name, c2.course_id, c2.course_name, q.q_name, t.teacher_id, t.teacher_name " +
            "from class_course cc " +
            "join class c1 on cc.class_id = c1.class_id " +
            "join course c2 on cc.course_id = c2.course_id " +
            "join quantitative q on c2.q_id = q.q_id " +
            "join teacher t on cc.teacher_id = t.teacher_id " +
            "where cc.class_id = #{classId} and cc.teacher_id = #{teacherId} " +
            "order by cc.course_id asc")
    List<SCourse> getSCourseListById(@Param("classId") int classId, @Param("teacherId") Long teacherId);

    @Select("select cc.*, c1.class_id, c1.class_name, c2.course_id, c2.course_name, q.q_name, t.teacher_id, t.teacher_name " +
            "from class_course cc " +
            "join class c1 on cc.class_id = c1.class_id " +
            "join course c2 on cc.course_id = c2.course_id " +
            "join quantitative q on c2.q_id = q.q_id " +
            "join teacher t on cc.teacher_id = t.teacher_id " +
            "where cc.teacher_id = #{teacherId} " +
            "order by cc.course_id asc")
    List<SCourse> getSCourseListByTId(@Param("teacherId") Long teacherId);

    @Select("select cc.*, c1.class_name, c1.grade, c2.course_name, t.teacher_name " +
            "from class_course cc " +
            "join class c1 on cc.class_id = c1.class_id " +
            "join course c2 on cc.course_id = c2.course_id " +
            "join teacher t on cc.teacher_id = t.teacher_id " +
            "order by c1.class_name ")
    List<SCourseVo> getSCourseList();

    @Select("select cc.*, c1.class_id, c1.class_name, c2.course_id, c2.course_name, q.q_name, t.teacher_id, t.teacher_name " +
            "from class_course cc " +
            "join class c1 on cc.class_id = c1.class_id " +
            "join course c2 on cc.course_id = c2.course_id " +
            "join quantitative q on c2.q_id = q.q_id " +
            "join teacher t on cc.teacher_id = t.teacher_id " +
            "where cc.course_id = #{courseId} " +
            "order by cc.class_id asc")
    List<SCourse> getSCourseListByCId(@Param("courseId") int courseId);  //查询已选课程的班级信息

    @Select("select cc.*, c1.class_id, c1.class_name, c2.course_id, c2.course_name, q.q_name, t.teacher_id, t.teacher_name " +
            "from class_course cc " +
            "join class c1 on cc.class_id = c1.class_id " +
            "join course c2 on cc.course_id = c2.course_id " +
            "join quantitative q on c2.q_id = q.q_id " +
            "join teacher t on cc.teacher_id = t.teacher_id " +
            "where cc.course_id = #{courseId} and cc.teacher_id = #{teacherId} " +
            "order by cc.class_id asc")
    List<SCourse> getSCourseListByCTId(@Param("courseId") int courseId, @Param("teacherId") Long teacherId);  //查询该教师任课的已选课程的班级信息

}
