package com.gxnzd.scoresystem.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonResult<T> {
    private int code;
    private String message;
    private T data;

    public CommonResult() {}

    public CommonResult(int code, String msg, T data) {
        this.code = code;
        this.message = msg;
        this.data = data;
    }

}
