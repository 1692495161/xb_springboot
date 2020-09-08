package com.xb.entity;

import java.io.Serializable;

/**
 * @author cjj
 * @date 2020/8/31
 * @description 前后端通用响应实体
 */
public class Result implements Serializable {

    // 请求是否成功
    private Boolean success;

    // 响应给前端的消息
    private String msg;

    // 响应给前端的数据
    private Object obj;

    public Result() {
    }

    public Result(Boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public Result(Boolean success, String msg, Object obj) {
        this.success = success;
        this.msg = msg;
        this.obj = obj;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
