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
@TableName("class")
public class StuClass {

    @TableId(type = IdType.AUTO)
    @ExcelProperty("班级编号")
    private Integer classId;

    @ExcelProperty("班级名称")
    private String className;

    @ExcelIgnore
    private Integer majorId;

    @TableField(exist = false)
    @ExcelProperty("所属专业")
    private String majorName;

    @ExcelProperty("年级")
    private String grade;

}
