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

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("scoredetail")
public class ScoreDetail {

    @TableId(type = IdType.AUTO)  //声明主键，类型自增
    @ExcelIgnore
    private Integer sdId;

    @ExcelProperty("课程编号")
    private Integer courseId;

    @TableField(exist = false)
    @ExcelProperty("课程名称")
    private String courseName;

    @TableField(exist = false)
    @ExcelProperty("成绩量化标准")
    private String qName;

    @TableField(exist = false)
    @ExcelIgnore
    private Integer classId;

    @TableField(exist = false)
    @ExcelProperty("班级名称")
    private String className;

    @ExcelProperty("学号")
    private Long studentId;

    @TableField(exist = false)
    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("上课日期")
    private LocalDate date;

//    @ExcelProperty("课时名称")
//    private String item;

    @ExcelProperty("课堂表现成绩明细")
    private String usualItem;

    @ExcelProperty("课堂表现成绩")
    private Float usualScore;

    @ExcelProperty("技能成绩明细")
    private String skillItem;

    @ExcelProperty("技能成绩")
    private Float skillScore;

    @ExcelProperty("纪律成绩明细")
    private String disItem;

    @ExcelProperty("纪律成绩")
    private Float disScore;

    @ExcelIgnore
    private Float totalScore;

    @ExcelProperty("备注")
    private String sdRemark;

    @ExcelProperty("创建时间")
    private String createTime;

    @ExcelProperty("更新时间")
    private String updateTime;

}
