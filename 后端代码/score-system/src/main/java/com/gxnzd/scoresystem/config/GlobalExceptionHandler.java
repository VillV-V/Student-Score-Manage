package com.gxnzd.scoresystem.config;

import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.util.SaResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 全局异常拦截
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)  // 设置HTTP状态码为401
    public SaResult handlerException(NotRoleException e) {
        e.printStackTrace();
        return SaResult.error("没有授权");
    }

}
