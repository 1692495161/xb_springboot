package com.xb.interceptor;

import com.xb.util.LoginUserUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author cjj
 * @date 2020/9/4
 * @description
 */
//编写一个拦截器
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    //在方法执行前调用
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取到请求的URL
        StringBuffer url = request.getRequestURL();
        System.out.println(url);

        if (LoginUserUtil.getLoginUser()==null){
            //重定向到登录页面
            response.sendRedirect( "/index.html");
            return false;
        }
        return true;
    }
}
