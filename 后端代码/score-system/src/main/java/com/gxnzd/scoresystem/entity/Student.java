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
@TableName("student")
public class Student {

    @TableId(type = IdType.AUTO)  //声明主键，类型自增
    @ExcelProperty("学号")
    private Long studentId;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("电话")
    private String telephone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelIgnore
    private Integer classId;

    @TableField(exist = false)
    @ExcelProperty("所属班级")
    private String className;

    @TableField(exist = false)
    @ExcelProperty("所属专业")
    private String majorName;

    @TableField(exist = false)
    @ExcelProperty("所属学院")
    private String schoolName;

    @ExcelProperty("密码")
    private String password;

}
