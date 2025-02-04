package com.gxnzd.scoresystem.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("admin")
public class Admin {

    private Integer id;

    private String username;

    private String password;

    private Long teacherId;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String gender;

    @TableField(exist = false)
    private String title;

    @TableField(exist = false)
    private String telephone;

    @TableField(exist = false)
    private String email;

    @TableField(exist = false)
    private String schoolName;

}
