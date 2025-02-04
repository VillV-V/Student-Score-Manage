package com.gxnzd.scoresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxnzd.scoresystem.entity.Score;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ScoreMapper extends BaseMapper<Score> {

    @Select("select s.student_id, s.student_name, s.telephone, s.email, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, s1.s_id, " +
            "ifnull(s1.usual_score,0) as usual_score, ifnull(s1.skill_score,0) as skill_score, " +
            "ifnull(s1.dis_score,0) as dis_score, ifnull(s1.total_score,0) as total_score " +
            "from class_course cc " +
            "join student s on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join score s1 on c1.course_id = s1.course_id and s.student_id = s1.student_id " +
            "where cc.class_id = #{classId} " +
            "order by cc.course_id asc, s.student_id asc")
    List<Score> getScoreListByClassId(@Param("classId") int classId);   //按班级编号获取该班所有课程的学生成绩

    @Select("select s.student_id, s.student_name, s.telephone, s.email, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, s1.s_id, " +
            "ifnull(s1.usual_score,0) as usual_score, ifnull(s1.skill_score,0) as skill_score, " +
            "ifnull(s1.dis_score,0) as dis_score, ifnull(s1.total_score,0) as total_score " +
            "from class_course cc " +
            "join student s on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join score s1 on c1.course_id = s1.course_id and s.student_id = s1.student_id " +
            "where cc.course_id = #{courseId} " +
            "order by cc.class_id asc, s.student_id asc")
    List<Score> getScoreListByCourseId(@Param("courseId") int courseId);   //按课程编号获取该课程所有学生的成绩

    @Select("select s.student_id, s.student_name, s.telephone, s.email, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, s1.s_id, " +
            "ifnull(s1.usual_score,0) as usual_score, ifnull(s1.skill_score,0) as skill_score, " +
            "ifnull(s1.dis_score,0) as dis_score, ifnull(s1.total_score,0) as total_score " +
            "from class_course cc " +
            "join student s on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join score s1 on c1.course_id = s1.course_id and s.student_id = s1.student_id " +
            "where cc.course_id = #{courseId} and cc.teacher_id = #{teacherId} " +
            "order by cc.class_id asc, s.student_id asc")
    List<Score> getScoreListByCTId(@Param("courseId") int courseId, @Param("teacherId") Long teacherId);   //按课程编号获取该课程所有学生的成绩

    @Select("select s.student_id, s.student_name, s.telephone, s.email, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, s1.s_id, " +
            "ifnull(s1.usual_score,0) as usual_score, ifnull(s1.skill_score,0) as skill_score, " +
            "ifnull(s1.dis_score,0) as dis_score, ifnull(s1.total_score,0) as total_score " +
            "from student s " +
            "join class_course cc on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join score s1 on c1.course_id = s1.course_id and s.student_id = s1.student_id " +
            "where s.student_id = #{studentId} " +
            "order by cc.course_id asc")
    List<Score> getScoreListByStuId(@Param("studentId") Long studentId);   //按学生编号获取该生所有课程的成绩

    @Select("select s.student_id, s.student_name, s.telephone, s.email, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, s1.s_id, " +
            "ifnull(s1.usual_score,0) as usual_score, ifnull(s1.skill_score,0) as skill_score, " +
            "ifnull(s1.dis_score,0) as dis_score, ifnull(s1.total_score,0) as total_score " +
            "from class_course cc " +
            "join student s on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join score s1 on c1.course_id = s1.course_id and s.student_id = s1.student_id " +
            "where cc.class_id = #{classId} ")
    Score getScoreListBySCId(@Param("studentId") Long studentId, @Param("courseId") int courseId);   //按学生编号和课程编号获取该生课程的成绩

    @Select("select s.student_id, s.student_name, s.telephone, s.email, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, s1.s_id, " +
            "ifnull(s1.usual_score,0) as usual_score, ifnull(s1.skill_score,0) as skill_score, " +
            "ifnull(s1.dis_score,0) as dis_score, ifnull(s1.total_score,0) as total_score " +
            "from class_course cc " +
            "join student s on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join score s1 on c1.course_id = s1.course_id and s.student_id = s1.student_id " +
            "where cc.class_id = #{classId} and cc.course_id = #{courseId} " +
            "order by s.student_id asc")
    List<Score> getScoreListByCCId(@Param("classId") int classId, @Param("courseId") int courseId);   //按班级编号和课程编号获取该班该课程的成绩

    @Select("select s.student_id, s.student_name, s.telephone, s.email, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, s1.s_id, " +
            "ifnull(s1.usual_score,0) as usual_score, ifnull(s1.skill_score,0) as skill_score, " +
            "ifnull(s1.dis_score,0) as dis_score, ifnull(s1.total_score,0) as total_score " +
            "from class_course cc " +
            "join student s on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join score s1 on c1.course_id = s1.course_id and s.student_id = s1.student_id " +
            "order by c.class_id asc, cc.course_id asc, s.student_id asc")
    List<Score> getAllScoreList();   //获取所有班级的所有选课成绩

    @Select("select s.student_id, s.student_name, s.telephone, s.email, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, s1.s_id, " +
            "ifnull(s1.usual_score,0) as usual_score, ifnull(s1.skill_score,0) as skill_score, " +
            "ifnull(s1.dis_score,0) as dis_score, ifnull(s1.total_score,0) as total_score " +
            "from class_course cc " +
            "join student s on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join score s1 on c1.course_id = s1.course_id and s.student_id = s1.student_id " +
            "where cc.teacher_id = #{teacherId} " +
            "order by c.class_id asc, cc.course_id asc, s.student_id asc")
    List<Score> getAllScoreListByTId(@Param("teacherId") Long teacherId);   //获取该教师所授课程的所有成绩

    @Select("select s.student_id, s.student_name, s.telephone, s.email, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, s1.s_id, " +
            "ifnull(s1.usual_score,0) as usual_score, ifnull(s1.skill_score,0) as skill_score, " +
            "ifnull(s1.dis_score,0) as dis_score, ifnull(s1.total_score,0) as total_score " +
            "from class_course cc " +
            "join student s on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join score s1 on c1.course_id = s1.course_id and s.student_id = s1.student_id " +
            "where cc.class_id = #{classId} and cc.teacher_id = #{teacherId} " +
            "order by c.class_id asc, cc.course_id asc, s.student_id asc")
    List<Score> getAllScoreListByCTId(@Param("classId") int classId, @Param("teacherId") Long teacherId);   //获取该班该教师所授课程的所有成绩

    @Select("select s.student_id, s.student_name, s.telephone, s.email, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, s1.s_id, " +
            "ifnull(s1.usual_score,0) as usual_score, ifnull(s1.skill_score,0) as skill_score, " +
            "ifnull(s1.dis_score,0) as dis_score, ifnull(s1.total_score,0) as total_score " +
            "from class_course cc " +
            "join student s on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join score s1 on c1.course_id = s1.course_id and s.student_id = s1.student_id " +
            "where cc.class_id = #{classId} and cc.course_id = #{courseId} and cc.teacher_id = #{teacherId} " +
            "order by c.class_id asc, cc.course_id asc, s.student_id asc")
    List<Score> getAllScoreListByCCTId(@Param("classId") int classId, @Param("courseId") int courseId, @Param("teacherId") Long teacherId);   //获取该班该教师该课程的所有成绩

}
