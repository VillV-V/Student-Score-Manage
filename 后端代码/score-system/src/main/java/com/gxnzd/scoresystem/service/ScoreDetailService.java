package com.gxnzd.scoresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxnzd.scoresystem.entity.ScoreDetail;

import java.util.List;

public interface ScoreDetailService extends IService<ScoreDetail> {

    List<ScoreDetail> getSDListByClassId(int classId);   //按班级编号获取所有课程的学生成绩

    List<ScoreDetail> getSDListByCourseId(int courseId);   //按课程编号获取该课程所有学生的成绩

    List<ScoreDetail> getSDListByStuId(Long studentId);   //按学生编号获取该生所有课程的成绩

    List<ScoreDetail> getSDListBySCId(Long studentId, int courseId);   //按学生编号和课程编号获取该生课程的成绩

    List<ScoreDetail> getSDListByCCId(int classId, int courseId);   //按编号获取该班该课程的成绩

    int addScoreDetail(ScoreDetail sd);

    int updateScoreDetail(ScoreDetail sd);

    int deleteScoreDetail(int id);

    int delBatchesScoreDetail(List<Integer> ids);
    
}
