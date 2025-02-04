package com.gxnzd.scoresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxnzd.scoresystem.entity.Score;

import java.util.List;

public interface ScoreSeivice extends IService<Score> {

    List<Score> getScoreListByClassId(int classId);   //按班级编号获取所有课程的学生成绩

    List<Score> getScoreListByCourseId(int courseId);   //按课程编号获取该课程所有学生的成绩

    List<Score> getScoreListByCTId(int courseId, Long teacherId);   //按课程编号获取该教师所任课程的所有学生成绩

    List<Score> getScoreListByStuId(Long studentId);   //按学生编号获取该生所有课程的成绩

    Score getScoreListBySCId(Long studentId, int courseId);   //按学生编号和课程编号获取该生课程的成绩

    List<Score> getScoreListByCCId(int classId, int courseId);   //按班级编号和课程编号获取该班该课程的成绩

    List<Score> getAllScoreList();   //获取所有班级的所有选课成绩

    List<Score> getAllScoreListByTId(Long teacherId);   //获取该教师所授课程的所有成绩

    List<Score> getAllScoreListByCTd(int classId, Long teacherId);

    List<Score> getAllScoreListByCCTId(int classId, int courseId, Long teacherId);

    int addScore(Score score);

    int updateScore(Score score);

    int deleteScore(int id);

    int delBatchesScore(List<Integer> ids);

}
