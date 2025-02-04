package com.gxnzd.scoresystem.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.gxnzd.scoresystem.entity.User;
import com.gxnzd.scoresystem.service.LoginService;
import com.gxnzd.scoresystem.service.StudentService;
import com.gxnzd.scoresystem.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private LoginService loginService;

    @Override
    public List<String> getPermissionList(Object o, String s) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String s) {
        List<String> roleList = new ArrayList<>();
        Long id = Long.parseLong((String) loginId);
        User user = null;
        if (id.toString().length() == 1) {
            roleList.add("admin");
        } else {
            user = loginService.teacherLogin(id);
            if (user != null) {
                roleList.add("teacher");
            } else {
                roleList.add("student");
            }
        }
        return roleList;
    }
}
