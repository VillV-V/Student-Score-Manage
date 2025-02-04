package com.gxnzd.scoresystem.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentVo implements Serializable {

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

    @TableField
    @ExcelIgnore
    private Integer total;
}
