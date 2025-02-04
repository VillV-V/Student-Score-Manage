package com.gxnzd.scoresystem.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gxnzd.scoresystem.entity.Course;

import java.util.List;

public interface CourseService extends IService<Course> {

    //根据编号获取课程
    List<Course> getCourseById(int id);

    //获取教师的课程安排
    IPage<Course> getCourseByTId(Page<Course> page, QueryWrapper<Course> wrapper);

    //获取所有课程列表
    List<Course> getCourseList();

    List<Course> getCourseGroupList();

    IPage<Course> getCoursePage(Page<Course> page, QueryWrapper<Course> wrapper);

    //添加课程
    int addCourse(Course course);

    //修改课程信息
    int updateCourse(Course course);

    int delCourse(int id);

    //删除课程
    int delBatchesCourse(List<Integer> ids);

}
