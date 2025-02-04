package com.gxnzd.scoresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxnzd.scoresystem.entity.SCourse;
import com.gxnzd.scoresystem.mapper.SCourseMapper;
import com.gxnzd.scoresystem.service.SCourseService;
import com.gxnzd.scoresystem.vo.SCourseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SCourseServiceImpl extends ServiceImpl<SCourseMapper, SCourse> implements SCourseService {

    @Autowired
    private SCourseMapper sCourseMapper;

    @Override
    public List<SCourse> getSCourseListByClassId(int classId) {
        return sCourseMapper.getSCourseListByClassId(classId);
    }

    @Override
    public List<SCourse> getSCourseListById(int classId, Long teacherId) {
        return sCourseMapper.getSCourseListById(classId, teacherId);
    }

    @Override
    public List<SCourse> getSCourseListByTId(Long teacherId) {
        return sCourseMapper.getSCourseListByTId(teacherId);
    }

    @Override
    public List<SCourseVo> getSCourseList() {
        return sCourseMapper.getSCourseList();
    }

    @Override
    public List<SCourse> getSCourseListByCId(int courseId) {
        return sCourseMapper.getSCourseListByCId(courseId);
    }

    @Override
    public List<SCourse> getSCourseListByCTId(int courseId, Long teacherId) {
        return sCourseMapper.getSCourseListByCTId(courseId, teacherId);
    }

    @Override
    public IPage<SCourse> getSCoursePage(IPage<SCourse> page, QueryWrapper<SCourse> wrapper) {
        return sCourseMapper.selectPage(page, wrapper);
    }

    @Override
    public int addSCourse(SCourse sCourse) {
        return sCourseMapper.insert(sCourse);
    }

    @Override
    public int updateSCourse(SCourse sCourse) {
        return sCourseMapper.updateById(sCourse);
    }

    @Override
    public int delSCourse(int id) {
        return sCourseMapper.deleteById(id);
    }

    @Override
    public int delBatchesSCourse(List<Integer> ids) {
        return sCourseMapper.deleteBatchIds(ids);
    }
}
