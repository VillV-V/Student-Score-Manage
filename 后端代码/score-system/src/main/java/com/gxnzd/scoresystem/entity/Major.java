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
@TableName("major")
public class Major {

    @TableId(type = IdType.AUTO)
    @ExcelProperty("专业编号")
    private Integer majorId;

    @ExcelProperty("专业名称")
    private String majorName;

    @ExcelIgnore
    private Integer schoolId;

    @TableField(exist = false)
    @ExcelProperty("所属学院")
    private String schoolName;
}
