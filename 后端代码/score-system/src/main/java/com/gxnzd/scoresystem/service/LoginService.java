package com.gxnzd.scoresystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxnzd.scoresystem.entity.Admin;
import com.gxnzd.scoresystem.entity.User;
import com.gxnzd.scoresystem.utils.CommonResult;
import com.gxnzd.scoresystem.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface LoginService extends IService<User> {

    User adminLogin(String username);

    User teacherLogin(Long teacherId);

    User studentLogin(Long studentId);

}
