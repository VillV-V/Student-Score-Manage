package com.gxnzd.scoresystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxnzd.scoresystem.entity.Admin;
import com.gxnzd.scoresystem.entity.User;
import com.gxnzd.scoresystem.mapper.LoginMapper;
import com.gxnzd.scoresystem.service.LoginService;
import com.gxnzd.scoresystem.utils.CommonResult;
import com.gxnzd.scoresystem.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class LoginServiceImpl extends ServiceImpl<LoginMapper, User> implements LoginService {

    @Autowired
    private LoginMapper loginMapper;

    @Override
    public User adminLogin(String username) {
        return loginMapper.adminLogin(username);
    }

    @Override
    public User teacherLogin(Long teacherId) {
        return loginMapper.teacherLogin(teacherId);
    }

    @Override
    public User studentLogin(Long studentId) {
        return loginMapper.studentLogin(studentId);
    }


}
