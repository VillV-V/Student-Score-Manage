package com.gxnzd.scoresystem.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.gxnzd.scoresystem.entity.SCourse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SCourseVo {

    @ExcelProperty("选课信息编号")
    private Integer id;

    @ExcelIgnore
    private Integer classId;

    @ExcelProperty("班级名称")
    private String className;

    @ExcelIgnore
    private List<SCourse> scourseList;

    @ExcelIgnore
    private Integer courseId;

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelIgnore
    private String schoolName;

    @ExcelIgnore
    private String qName;

    @ExcelProperty("教师工号")
    private Long teacherId;

    @ExcelProperty("教师姓名")
    private String teacherName;

    @ExcelProperty("学期")
    private String date;

}
