package com.gxnzd.scoresystem.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gxnzd.scoresystem.entity.Student;
import com.gxnzd.scoresystem.vo.StudentVo;

import java.util.List;

public interface StudentService extends IService<Student> {
    
    //获取学生个人信息
    List<StudentVo> getStudentById(Long id);

    //获取所有学生信息
    List<Student> getStudentList();

    IPage<StudentVo> getStudentPage(Page<Student> page, QueryWrapper<Student> wrapper);

    //添加学生
    int addStudent(Student student);

    //修改学生个人信息
    int updateStudent(Student student);

    //修改密码
    int updatePassword(Student student);

    //删除学生
    int delStudent(Long id);

    //批量删除学生
    int delBatchesStudent(List<Long> ids);

}
