package com.gxnzd.scoresystem.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.gxnzd.scoresystem.entity.Admin;
import com.gxnzd.scoresystem.entity.User;
import com.gxnzd.scoresystem.mapper.AdminMapper;
import com.gxnzd.scoresystem.service.LoginService;
import com.gxnzd.scoresystem.utils.CheckCode;
import com.gxnzd.scoresystem.utils.CommonResult;
import com.gxnzd.scoresystem.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private LoginService loginService;
    private String code = "";

    //生成验证码
    @GetMapping("/getCode")
    public CommonResult<String> getCheckCode() throws IOException {
        CheckCode checkCode = new CheckCode(100,40);
//        String imgUrl = "http://dummyimage.com/100x40/dcdfe6/000000.png&text=" + this.code;
        String imgUrl = checkCode.generateCaptchaImageBase64();
        this.code = checkCode.getCode().toLowerCase();
        System.out.println("登录验证码"+this.code);
        return new CommonResult<>(200, "获取验证码成功", imgUrl);
    }

    //登录验证
    @PostMapping("/login")
    public CommonResult<UserVo> login(@RequestBody User user, HttpServletRequest request) {
        if(!this.code.equals(user.getCode().toLowerCase())) {
            return new CommonResult<>(500, "验证码错误", null);
        }
        if(user.getRole().equals("admin")){
            return adminLogin(user, request);
        } else if (user.getRole().equals("teacher")) {
            return teacherLogin(user, request);
        } else {
            return studentLogin(user, request);
        }
    }

    public CommonResult<UserVo> adminLogin(User user, HttpServletRequest request) {
        User user1 = loginService.adminLogin(user.getUsername());
        if(user1 != null) {
            if (user.getUsername().equals(user1.getUsername()) && user.getPassword().equals(user1.getPassword())) {
                UserVo userVo = new UserVo();
                userVo.setId(Long.valueOf(user1.getId()));
                userVo.setUsername(user1.getUsername());
                String[] arr = {"admin"};
                userVo.setRoles(arr);
                HttpSession session = request.getSession();  // 获取当前请求的 Session
                session.setAttribute("userInfo", userVo);  // 设置 Session 数据
                StpUtil.login(user1.getId());
                String token = StpUtil.getTokenValue();
                userVo.setToken(token);
                return new CommonResult<>(200, "登录成功", userVo);
            }
        } else {
            return new CommonResult<>(500, "该用户不存在！", null);
        }
        return new CommonResult<>(500, "登录失败，请检查您的账号和密码！", null);
    }

    public CommonResult<UserVo> teacherLogin(User user, HttpServletRequest request) {
        User user1 = loginService.teacherLogin(Long.valueOf(user.getUsername()));
        if(user1 != null) {
            if (user.getUsername().equals(String.valueOf(user1.getTeacherId())) && user.getPassword().equals(user1.getPassword())) {
                UserVo userVo = new UserVo();
                userVo.setId(user1.getTeacherId());
                userVo.setUsername(user1.getTeacherName());
                String[] arr = {"teacher"};
                userVo.setRoles(arr);
                HttpSession session = request.getSession();  // 获取当前请求的 Session
                session.setAttribute("userInfo", userVo);  // 设置 Session 数据
                StpUtil.login(user1.getTeacherId());
                String token = StpUtil.getTokenValue();
                userVo.setToken(token);
                return new CommonResult<>(200, "登录成功", userVo);
            }
        } else {
            return new CommonResult<>(500, "该用户不存在！", null);
        }
        return new CommonResult<>(500, "登录失败，请检查您的账号和密码！", null);
    }

    public CommonResult<UserVo> studentLogin(User user, HttpServletRequest request) {
        User user1 = loginService.studentLogin(Long.valueOf(user.getUsername()));
        if(user1 != null) {
            if (user.getUsername().equals(String.valueOf(user1.getStudentId())) && user.getPassword().equals(user1.getPassword())) {
                UserVo userVo = new UserVo();
                userVo.setId(user1.getStudentId());
                userVo.setUsername(user1.getStudentName());
                String[] arr = {"student"};
                userVo.setRoles(arr);
                HttpSession session = request.getSession();  // 获取当前请求的 Session
                session.setAttribute("userInfo", userVo);  // 设置 Session 数据
                StpUtil.login(user1.getStudentId());
                String token = StpUtil.getTokenValue();
                userVo.setToken(token);
                return new CommonResult<>(200, "登录成功", userVo);
            }
        } else {
            return new CommonResult<>(500, "该用户不存在！", null);
        }
        return new CommonResult<>(500, "登录失败，请检查您的账号和密码！", null);
    }

    //注销登录
    @GetMapping("/logout")
    public CommonResult<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        StpUtil.logout();
        return new CommonResult<>(200,"注销成功",null);
    }

    //获取用户详情
    @SaCheckLogin
    @PostMapping("/users/info")
    public CommonResult<Map<String, Object>> userInfo(@RequestBody UserVo userVo) {
        System.out.println(StpUtil.getRoleList());
        Map<String, Object> map = new HashMap<>();
        map.put("id", userVo.getId());
        map.put("username", userVo.getUsername());
        map.put("roles", userVo.getRoles());
        return new CommonResult<>(200, "获取用户信息成功", map);
    }

    //获取管理员信息
    @SaCheckRole("admin")
    @GetMapping("getAdminInfo/{id}")
    public CommonResult<Map<String, Object>> getAdminInfo(@PathVariable Integer id) {
        Admin admin = adminMapper.getAdminById(id);
        if (admin != null) {
            admin.setPassword("");
            List<Admin> admins = new ArrayList<>();
            admins.add(admin);
            Map<String, Object> map = new HashMap<>();
            map.put("total", admins.size());
            map.put("records", admins);
            return new CommonResult<>(200, "获取信息成功", map);
        }
        return new CommonResult<>(500, "获取信息失败", null);
    }

    //修改管理员信息
    @SaCheckRole("admin")
    @PostMapping("upAdminInfo")
    public CommonResult<String> upAdminInfo(@RequestBody Admin admin) {
        int result = adminMapper.updateById(admin);
        if(result > 0) {
            return new CommonResult<>(200, "修改成功", null);
        }
        return new CommonResult<>(500, "修改失败", null);
    }

    //修改管理员密码
    @SaCheckRole("admin")
    @PostMapping("upAdminPwd")
    public CommonResult<String> upAdminPwd(@RequestBody Admin admin) {
        Admin admin1 = adminMapper.selectById(admin.getId());
        if (admin1.getPassword().equals(admin.getPassword())) {
            return new CommonResult<>(500, "修改失败，新密码不能与原密码相同！", null);
        }
        int result = adminMapper.updateById(admin);
        if(result > 0) {
            return new CommonResult<>(200, "修改成功", null);
        }
        return new CommonResult<>(500, "修改失败", null);
    }

}
