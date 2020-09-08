package com.xb.util;

import com.mysql.jdbc.StringUtils;
import com.xb.entity.User;
import com.xb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author cjj
 * @date 2020/8/31
 * @description
 */
@Component
public class LoginUserUtil {

    private static RedisTemplate redisTemplate;

    private static HttpSession session;

    private static HttpServletRequest request;

    private static UserService userService;

    /*
     * Spring容器会在类加载后自动注入这个方法的参数，并执行一遍方法。
     * @param userService
     */
    @Autowired
    public void setUserService(UserService userService) {
        LoginUserUtil.userService = userService;
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        LoginUserUtil.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setSession(HttpSession session) {
        LoginUserUtil.session = session;
    }

    @Autowired
    public void setRequest(HttpServletRequest request) {
        LoginUserUtil.request = request;
    }

    public static User getLoginUser() {
        //先从session获取
        Object userId = session.getAttribute("userId");

        if (userId != null) {
            //获取redis中的缓存
            User user = (User) redisTemplate.opsForValue().get("loginUser:" + userId);

            //如果redis中的过期
            if (user == null){
                //从数据库中查询一份
                user=userService.findById(Long.parseLong(userId.toString()));
                //重新给redis加载一份
                redisTemplate.opsForValue().set("loginUser:" + userId,user, 7, TimeUnit.DAYS);
            }
            return user;
        }

        //再从cookie中获取
        Cookie cookie = getCookie("userId");

        if (cookie!=null){
            //先从redis中获取
            User user = (User) redisTemplate.opsForValue().get("loginUser:" + cookie.getValue());
            if (user==null){
                //表示redis过期
                user = userService.findById(Long.parseLong(cookie.getValue()));
                //重新给redis加载一份
                redisTemplate.opsForValue().set("loginUser:" + cookie.getValue(), user, 7, TimeUnit.DAYS);
            }
            return user;
        }
        return null;
    }

    private static Cookie getCookie(String cookieName) {
        if (request.getCookies()!=null && !StringUtils.isNullOrEmpty(cookieName)){
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)){
                    return cookie;
                }
            }
        }
        return null;
    }

    //获取当前登录人的id
    public static Long getId(){
        if (getLoginUser()!=null) {
            return getLoginUser().getId();
        }
        return null;
    }

    public static String dateToStr(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}
