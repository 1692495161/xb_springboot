package com.xb;

import com.xb.code.GenerateImageCodeServlet;
import com.xb.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author cjj
 * @date 2020/8/31
 * @description
 */
@Configuration
public class XBConfiguration extends WebMvcConfigurerAdapter {

    /*
    *@date 2020/8/31
    *@param []
    *@return org.springframework.boot.web.servlet.ServletRegistrationBean
    *@description  验证码
    */
    @Bean
    public ServletRegistrationBean myServlet(){
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(new GenerateImageCodeServlet(), "/generateCode");
        return registrationBean;
    }

    @Value("${file.requestPath}")
    private String requestPath;

    @Value("${file.dir}")
    private String dir;

    /**
     * 映射/upload下的路径取磁盘找
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(requestPath).addResourceLocations("file:"+dir);
    }

    //将拦截器注入
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册一个拦截器
        InterceptorRegistration interceptor = registry.addInterceptor(loginInterceptor);

        //拦截规则
        interceptor.addPathPatterns("/**");

        //放行规则
        interceptor.excludePathPatterns("/user/checkEmail/**")
                .excludePathPatterns("/user/checkUsername/**")
                .excludePathPatterns("/user/register/**")
                .excludePathPatterns("/user/sendEmail/**")
                .excludePathPatterns("/user/login/**")
                .excludePathPatterns("/user/logout/**")
                .excludePathPatterns("/generateCode/**")
                .excludePathPatterns("/to_wxLogin/**")
                .excludePathPatterns("/wx_login/**")
                .excludePathPatterns("/generateCode/**")
                .excludePathPatterns("/index.html")
                .excludePathPatterns("/gorget.html")
                .excludePathPatterns("/register.html")
                .excludePathPatterns("/bootstrap/**")
                .excludePathPatterns("/css/**")
                .excludePathPatterns("/fonts/**")
                .excludePathPatterns("/img/**")
                .excludePathPatterns("/js/**")
                .excludePathPatterns("/vendor/**")
                .excludePathPatterns("/assets/**")
                .excludePathPatterns("/upload/**")
                .excludePathPatterns("/xb_socket/**");
    }

    /*
    *@date 2020/9/5
    *@param []
    *@return org.springframework.web.socket.server.standard.ServerEndpointExporter
    *@description 开启webSocket支持
    */
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }
}
