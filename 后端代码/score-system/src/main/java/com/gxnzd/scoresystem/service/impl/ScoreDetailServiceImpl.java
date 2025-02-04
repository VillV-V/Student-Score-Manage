package com.gxnzd.scoresystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxnzd.scoresystem.entity.ScoreDetail;
import com.gxnzd.scoresystem.mapper.ScoreDetailMapper;
import com.gxnzd.scoresystem.service.ScoreDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreDetailServiceImpl extends ServiceImpl<ScoreDetailMapper, ScoreDetail> implements ScoreDetailService {

    @Autowired
    private ScoreDetailMapper scoreDetailMapper;

    @Override
    public List<ScoreDetail> getSDListByClassId(int classId) {
        return scoreDetailMapper.getSDListByClassId(classId);
    }

    @Override
    public List<ScoreDetail> getSDListByCourseId(int courseId) {
        return scoreDetailMapper.getSDListByCourseId(courseId);
    }

    @Override
    public List<ScoreDetail> getSDListByStuId(Long studentId) {
        return scoreDetailMapper.getSDListByStuId(studentId);
    }

    @Override
    public List<ScoreDetail> getSDListBySCId(Long studentId, int courseId) {
        return scoreDetailMapper.getSDListBySCId(studentId, courseId);
    }

    @Override
    public List<ScoreDetail> getSDListByCCId(int classId, int courseId) {
        return scoreDetailMapper.getSDListByCCId(classId, courseId);
    }

    @Override
    public int addScoreDetail(ScoreDetail sd) {
        return scoreDetailMapper.insert(sd);
    }

    @Override
    public int updateScoreDetail(ScoreDetail sd) {
        return scoreDetailMapper.updateById(sd);
    }

    @Override
    public int deleteScoreDetail(int id) {
        return scoreDetailMapper.deleteById(id);
    }

    @Override
    public int delBatchesScoreDetail(List<Integer> ids) {
        return scoreDetailMapper.deleteBatchIds(ids);
    }
}
