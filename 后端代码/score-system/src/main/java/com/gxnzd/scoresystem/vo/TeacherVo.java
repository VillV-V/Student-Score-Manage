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
public class TeacherVo implements Serializable {

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

    @ExcelProperty("所属学院")
    private String schoolName;

}
