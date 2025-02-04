package com.gxnzd.scoresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxnzd.scoresystem.entity.ScoreDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ScoreDetailMapper extends BaseMapper<ScoreDetail> {

    @Select("select s.student_id, s.student_name, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, sd.sd_id, sd.date, " +
            "ifnull(sd.usual_item, '无') as usual_item, ifnull(sd.usual_score, 0) as usual_score, " +
            "ifnull(sd.skill_item, '无') as skill_item, ifnull(sd.skill_score, 0) as skill_score, " +
            "ifnull(sd.dis_item, '无') as dis_item, ifnull(sd.dis_score, 0) as dis_score, " +
            "ifnull(sd.total_score, 0) as total_score, ifnull(sd.sd_remark, '无') as sd_remark, " +
            "ifnull(sd.create_time, '无') as create_time, ifnull(sd.update_time , '无') as update_time " +
            "from class_course cc " +
            "join student s on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join scoredetail sd on c1.course_id = sd.course_id and s.student_id = sd.student_id " +
            "where cc.class_id = #{classId}" +
            "order by cc.course_id asc, s.student_id asc")
    List<ScoreDetail> getSDListByClassId(@Param("classId") int classId);   //按班级编号获取该班所有课程的学生成绩明细

    @Select("select s.student_id, s.student_name, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, sd.sd_id, sd.date, " +
            "ifnull(sd.usual_item, '无') as usual_item, ifnull(sd.usual_score, 0) as usual_score, " +
            "ifnull(sd.skill_item, '无') as skill_item, ifnull(sd.skill_score, 0) as skill_score, " +
            "ifnull(sd.dis_item, '无') as dis_item, ifnull(sd.dis_score, 0) as dis_score, " +
            "ifnull(sd.total_score, 0) as total_score, ifnull(sd.sd_remark, '无') as sd_remark, " +
            "ifnull(sd.create_time, '无') as create_time, ifnull(sd.update_time , '无') as update_time " +
            "from class_course cc " +
            "join student s on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join scoredetail sd on c1.course_id = sd.course_id and s.student_id = sd.student_id " +
            "where cc.course_id = #{courseId}" +
            "order by cc.course_id asc, s.student_id asc")
    List<ScoreDetail> getSDListByCourseId(@Param("courseId") int courseId);   //按课程编号获取该课程所有学生的成绩明细

    @Select("select s.student_id, s.student_name, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, sd.sd_id, sd.date, " +
            "ifnull(sd.usual_item, '无') as usual_item, ifnull(sd.usual_score, 0) as usual_score, " +
            "ifnull(sd.skill_item, '无') as skill_item, ifnull(sd.skill_score, 0) as skill_score, " +
            "ifnull(sd.dis_item, '无') as dis_item, ifnull(sd.dis_score, 0) as dis_score, " +
            "ifnull(sd.total_score, 0) as total_score, ifnull(sd.sd_remark, '无') as sd_remark, " +
            "ifnull(sd.create_time, '无') as create_time, ifnull(sd.update_time , '无') as update_time " +
            "from class_course cc " +
            "join student s on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join scoredetail sd on c1.course_id = sd.course_id and s.student_id = sd.student_id " +
            "where cc.student_id = #{studentId}" +
            "order by cc.course_id asc, s.student_id asc")
    List<ScoreDetail> getSDListByStuId(@Param("studentId") Long studentId);   //按学生编号获取该生所有课程的成绩明细

    @Select("select sd.*, s.student_id, s.student_name, s.class_id, c.course_id, c.course_name, c1.class_name, q.q_name " +
            "from scoredetail sd " +
            "join student s on sd.student_id = s.student_id " +
            "join course c on sd.course_id = c.course_id " +
            "join class c1 on s.class_id = c1.class_id " +
            "join quantitative q on c.q_id = q.q_id " +
            "where sd.student_id = #{studentId} and sd.course_id = #{courseId} " +
            "order by sd.sd_id asc")
    List<ScoreDetail> getSDListBySCId(@Param("studentId") Long studentId, @Param("courseId") int courseId);  //按学生编号和课程编号获取该生该课程的成绩明细

    @Select("select s.student_id, s.student_name, c.class_id, c.class_name, " +
            "c1.course_id, c1.course_name, q.q_name, sd.sd_id, sd.date, " +
            "ifnull(sd.usual_item, '无') as usual_item, ifnull(sd.usual_score, 0) as usual_score, " +
            "ifnull(sd.skill_item, '无') as skill_item, ifnull(sd.skill_score, 0) as skill_score, " +
            "ifnull(sd.dis_item, '无') as dis_item, ifnull(sd.dis_score, 0) as dis_score, " +
            "ifnull(sd.total_score, 0) as total_score, ifnull(sd.sd_remark, '无') as sd_remark, " +
            "ifnull(sd.create_time, '无') as create_time, ifnull(sd.update_time , '无') as update_time " +
            "from class_course cc " +
            "join student s on cc.class_id = s.class_id " +
            "join class c on cc.class_id = c.class_id " +
            "join course c1 on cc.course_id = c1.course_id " +
            "join quantitative q on c1.q_id = q.q_id " +
            "left join scoredetail sd on c1.course_id = sd.course_id and s.student_id = sd.student_id " +
            "where cc.class_id = #{classId} and cc.course_id = #{courseId} " +
            "order by cc.course_id asc, s.student_id asc")
    List<ScoreDetail> getSDListByCCId(@Param("classId") int classId, @Param("courseId") int courseId);   //按班级编号和课程编号获取该班该课程的成绩明细

}
