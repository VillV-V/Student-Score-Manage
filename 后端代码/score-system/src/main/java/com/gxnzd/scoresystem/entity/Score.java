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

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("score")
public class Score {

    @TableId(type = IdType.AUTO)  //声明主键，类型自增
    @ExcelIgnore
    private Integer sId;

    @TableField(exist = false)
    @ExcelIgnore
    private Integer classId;

    @TableField(exist = false)
    @ExcelProperty("班级名称")
    private String className;

//    @ExcelProperty("课程编号")
    @ExcelIgnore
    private Integer courseId;

    @TableField(exist = false)
    @ExcelProperty("课程名称")
    private String courseName;

    @TableField(exist = false)
    @ExcelProperty("成绩量化标准")
    private String qName;

    @ExcelProperty("学号")
    private Long studentId;

    @TableField(exist = false)
    @ExcelProperty("姓名")
    private String studentName;

    @TableField(exist = false)
    @ExcelProperty("电话")
    private String telephone;

    @TableField(exist = false)
    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("课堂表现成绩")
    private Float usualScore;

    @ExcelProperty("技能成绩")
    private Float skillScore;

    @ExcelProperty("纪律成绩")
    private Float disScore;

    @ExcelProperty("平时成绩总分")
    private Float totalScore;

    @TableField(exist = false)
    @ExcelIgnore
    private List<Map<String, String>> label;

}
