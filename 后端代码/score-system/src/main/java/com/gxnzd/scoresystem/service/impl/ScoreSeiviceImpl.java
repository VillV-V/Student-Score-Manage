package com.gxnzd.scoresystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxnzd.scoresystem.entity.Score;
import com.gxnzd.scoresystem.mapper.ScoreMapper;
import com.gxnzd.scoresystem.service.ScoreSeivice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreSeiviceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreSeivice {

    @Autowired
    private ScoreMapper scoreMapper;

    @Override
    public List<Score> getScoreListByClassId(int classId) {
        return scoreMapper.getScoreListByClassId(classId);
    }

    @Override
    public List<Score> getScoreListByCourseId(int courseId) {
        return scoreMapper.getScoreListByCourseId(courseId);
    }

    @Override
    public List<Score> getScoreListByCTId(int courseId, Long teacherId) {
        return scoreMapper.getScoreListByCTId(courseId, teacherId);
    }

    @Override
    public List<Score> getScoreListByStuId(Long studentId) {
        return scoreMapper.getScoreListByStuId(studentId);
    }

    @Override
    public Score getScoreListBySCId(Long studentId, int courseId) {
        return scoreMapper.getScoreListBySCId(studentId, courseId);
    }

    @Override
    public List<Score> getScoreListByCCId(int classId, int courseId) {
        return scoreMapper.getScoreListByCCId(classId, courseId);
    }

    @Override
    public List<Score> getAllScoreList() {
        return scoreMapper.getAllScoreList();
    }

    @Override
    public List<Score> getAllScoreListByTId(Long teacherId) {
        return scoreMapper.getAllScoreListByTId(teacherId);
    }

    @Override
    public List<Score> getAllScoreListByCTd(int classId, Long teacherId) {
        return scoreMapper.getAllScoreListByCTId(classId, teacherId);
    }

    @Override
    public List<Score> getAllScoreListByCCTId(int classId, int courseId, Long teacherId) {
        return scoreMapper.getAllScoreListByCCTId(classId, courseId, teacherId);
    }

    @Override
    public int addScore(Score score) {
        return scoreMapper.insert(score);
    }

    @Override
    public int updateScore(Score score) {
        return scoreMapper.updateById(score);
    }

    @Override
    public int deleteScore(int id) {
        return scoreMapper.deleteById(id);
    }

    @Override
    public int delBatchesScore(List<Integer> ids) {
        return scoreMapper.deleteBatchIds(ids);
    }
}
