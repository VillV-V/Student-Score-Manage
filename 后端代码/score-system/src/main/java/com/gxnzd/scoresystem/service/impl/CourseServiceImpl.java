package com.gxnzd.scoresystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxnzd.scoresystem.entity.Course;
import com.gxnzd.scoresystem.mapper.CourseMapper;
import com.gxnzd.scoresystem.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public List<Course> getCourseById(int id) {
        return courseMapper.getCourseById(id);
    }

    @Override
    public IPage<Course> getCourseByTId(Page<Course> page, QueryWrapper<Course> wrapper) {
        return courseMapper.getCourseByTId(page, wrapper);
    }

    @Override
    public List<Course> getCourseList() {
        return courseMapper.getCourseList();
    }

    @Override
    public List<Course> getCourseGroupList() {
        return courseMapper.getCourseGroupList();
    }

    @Override
    public IPage<Course> getCoursePage(Page<Course> page, QueryWrapper<Course> wrapper) {
        return courseMapper.getCoursePage(page, wrapper);
    }

    @Override
    public int addCourse(Course course) {
        return courseMapper.insert(course);
    }

    @Override
    public int updateCourse(Course course) {
        return courseMapper.updateById(course);
    }

    @Override
    public int delCourse(int id) {
        return courseMapper.deleteById(id);
    }

    @Override
    public int delBatchesCourse(List<Integer> ids) {
        return courseMapper.deleteBatchIds(ids);
    }

}
