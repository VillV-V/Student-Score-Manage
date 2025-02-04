package com.gxnzd.scoresystem.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("school")
public class School {

    @TableId(type = IdType.AUTO)
    @ExcelProperty("学院编号")
    private Integer schoolId;

    @ExcelProperty("学院名称")
    private String schoolName;

}
