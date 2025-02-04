package com.gxnzd.scoresystem.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Integer id;

    private String username;

    private Long teacherId;

    private Long studentId;

    private String teacherName;

    private String studentName;

    private String password;

    @TableField(exist = false)
    private String role;

    @TableField(exist = false)
    private String code;

}
