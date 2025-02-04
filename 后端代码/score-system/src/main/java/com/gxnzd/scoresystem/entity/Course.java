package com.gxnzd.scoresystem.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    @ExcelProperty("课程编号")
    private Integer courseId;

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelProperty("课程类型")
    private String type;

    @ExcelIgnore
    private Integer schoolId;

    @TableField(exist = false)
    @ExcelProperty("所属学院")
    private String schoolName;

    @ExcelProperty("总课时")
    private Integer classPeriod;

    @ExcelProperty("实践课时")
    private Integer practicalClassPeriod;

    @ExcelIgnore
    private Integer qId;

    @TableField(exist = false)
    @ExcelProperty("成绩量化标准")
    private String qName;

}
