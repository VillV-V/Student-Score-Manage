package com.gxnzd.scoresystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("quantitative")
public class Quantitative {

    @TableId(type = IdType.AUTO)  //声明主键，类型自增
    private Integer qId;

    private String qName;

    private Integer usualScore;

    private Integer skillScore;

    private Integer disScore;

    private String remark;

}
