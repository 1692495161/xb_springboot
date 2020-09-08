package com.xb.controller;

import com.xb.entity.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author cjj
 * @date 2020/7/17
 * @description
 */
@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler
    @ResponseBody
    public Result getException(Exception e){
        e.printStackTrace();
        return new Result(false,e.getMessage(),null);
    }
}
