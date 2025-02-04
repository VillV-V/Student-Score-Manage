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

@Data   //自动生成所有参数的get()、set()方法
@AllArgsConstructor   //自动生成包含所有参数的构造方法
@NoArgsConstructor    //自动生成无参的构造方法
@TableName("teacher")
public class Teacher {
    @TableId(type = IdType.AUTO)  //声明主键，类型自增
    @ExcelProperty("教师工号")
    private Long teacherId;

    @ExcelProperty("姓名")
    private String teacherName;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("职称")
    private String title;

    @ExcelProperty("电话")
    private String telephone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelIgnore  // 这个字段会被忽略，不会导出或导入
    private Integer schoolId;

    @TableField(exist = false)
    @ExcelProperty("所属学院")
    private String schoolName;

    @ExcelProperty("密码")
    private String password;
}
