package com.gxnzd.scoresystem.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gxnzd.scoresystem.entity.Teacher;
import com.gxnzd.scoresystem.vo.TeacherVo;

import java.util.List;

public interface TeacherService extends IService<Teacher> {

    //获取教师个人信息
    List<TeacherVo> getTeacherById(Long id);

    //获取所有教师信息
    List<Teacher> getTeacherList();

    List<TeacherVo> getTeacherGroupList();

    IPage<TeacherVo> getTeacherPage(Page<Teacher> page, QueryWrapper<Teacher> wrapper);

    //添加教师
    int addTeacher(Teacher teacher);

    //修改教师个人信息
    int updateTeacher(Teacher teacher);

    //修改密码
    int updatePassword(Teacher teacher);

    //删除教师
    int deleteTeacher(Long id);

    //批量删除教师
    int delBatchesTeacher(List<Long> ids);

}
