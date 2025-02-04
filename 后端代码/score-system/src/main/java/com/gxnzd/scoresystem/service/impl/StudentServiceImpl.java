package com.gxnzd.scoresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxnzd.scoresystem.entity.Student;
import com.gxnzd.scoresystem.mapper.StudentMapper;
import com.gxnzd.scoresystem.service.StudentService;
import com.gxnzd.scoresystem.vo.StudentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public List<StudentVo> getStudentById(Long id) {
        return studentMapper.getStudentById(id);
    }

    @Override
    public List<Student> getStudentList() {
        return studentMapper.getStudentList();
    }

    @Override
    public IPage<StudentVo> getStudentPage(Page<Student> page, QueryWrapper<Student> wrapper) {
        return studentMapper.getStudentPage(page, wrapper);
    }

    @Override
    public int addStudent(Student student) {
        return studentMapper.insert(student);
    }

    @Override
    public int updateStudent(Student student) {
        return studentMapper.updateById(student);
    }

    @Override
    public int updatePassword(Student student) {
        return studentMapper.updateById(student);
    }

    @Override
    public int delStudent(Long id) {
        return studentMapper.deleteById(id);
    }

    @Override
    public int delBatchesStudent(List<Long> ids) {
        return studentMapper.deleteBatchIds(ids);
    }

}
