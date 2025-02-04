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
@TableName("class_course")
public class SCourse {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer classId;

    @TableField(exist = false)
    private String className;

    private Integer courseId;

    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String qName;

    private Long teacherId;

    @TableField(exist = false)
    private String teacherName;

    private String date;

}
