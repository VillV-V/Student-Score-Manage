package com.gxnzd.scoresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxnzd.scoresystem.entity.Teacher;
import com.gxnzd.scoresystem.mapper.TeacherMapper;
import com.gxnzd.scoresystem.service.TeacherService;
import com.gxnzd.scoresystem.vo.TeacherVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Override
    public List<TeacherVo> getTeacherById(Long id) {
        return teacherMapper.getTeacherById(id);
    }

    @Override
    public List<Teacher> getTeacherList() {
        return teacherMapper.getTeacherList();
    }

    @Override
    public List<TeacherVo> getTeacherGroupList() {
        return teacherMapper.getTeacherGroupList();
    }

    @Override
    public IPage<TeacherVo> getTeacherPage(Page<Teacher> page, QueryWrapper<Teacher> wrapper) {
        return teacherMapper.getTeacherPage(page, wrapper);
    }

    @Override
    public int addTeacher(Teacher teacher) {
        return teacherMapper.insert(teacher);
    }

    @Override
    public int updateTeacher(Teacher teacher) {
        return teacherMapper.updateById(teacher);
    }

    @Override
    public int updatePassword(Teacher teacher) {
        return teacherMapper.updateById(teacher);
    }

    @Override
    public int deleteTeacher(Long id) {
        return teacherMapper.deleteById(id);
    }

    @Override
    public int delBatchesTeacher(List<Long> ids) {
        return teacherMapper.deleteBatchIds(ids);
    }

}
