package com.gxnzd.scoresystem.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gxnzd.scoresystem.entity.SCourse;
import com.gxnzd.scoresystem.vo.SCourseVo;

import java.util.List;

public interface SCourseService extends IService<SCourse> {

    //获取所有选课信息
    List<SCourse> getSCourseListByClassId(int classId);

    List<SCourse> getSCourseListById(int classId, Long teacherId);

    List<SCourse> getSCourseListByTId(Long teacherId);

    List<SCourseVo> getSCourseList();

    List<SCourse> getSCourseListByCId(int courseId);

    List<SCourse> getSCourseListByCTId(int courseId, Long teacherId);

    IPage<SCourse> getSCoursePage(IPage<SCourse> page, QueryWrapper<SCourse> wrapper);

    //添加选课
    int addSCourse(SCourse sCourse);

    //修改选课信息
    int updateSCourse(SCourse sCourse);

    //删除选课
    int delSCourse(int id);

    //批量删除选课
    int delBatchesSCourse(List<Integer> ids);
    
}
