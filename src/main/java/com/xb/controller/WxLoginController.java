package com.xb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xb.entity.Result;
import com.xb.entity.User;
import com.xb.service.UserService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author cjj
 * @date 2020/9/4
 * @description
 */
@RestController
public class WxLoginController {

    @Value("${wx.appId}")
    private String appId;

    @Value("${wx.appSecret}")
    private String appSecret;

    @Value("${wx.redirect_uri}")
    private String redirect_uri;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpSession session;

    @Autowired
    private RedisTemplate redisTemplate;

    /*
    *@date 2020/9/4
    *@param []
    *@return void
    *@description 用户发起微信登录请求认证
    */
    @RequestMapping("/to_wxLogin")
    public void to_wxLogin() throws Exception{
        //首先请求微信登录认证，获取code
        String url="https://open.weixin.qq.com/connect/qrconnect?" +
                "appid="+ appId +
                "&redirect_uri="+redirect_uri +
                "&response_type=code" +
                "&scope=snsapi_login";
        //重定向到微信指定的地址进行微信登录授权，授权成功后返回code
        response.sendRedirect(url);
    }

    /*
    *@date 2020/9/4
    *@param []
    *@return void
    *@description 用户确认授权后，返回一个code，定向到指定的回调域
    */
    @RequestMapping("/wx_login")
    public void wx_login() throws Exception{
        //用户扫码成功后携带过来的code
        String code = request.getParameter("code");

        String url="https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid="+appId +
                "&secret="+appSecret +
                "&code="+code +
                "&grant_type=authorization_code";

        //获取到access_token和openid等数据
         /*{
            "access_token":"ACCESS_TOKEN", 接口调用凭证
            "expires_in":7200,
            "refresh_token":"REFRESH_TOKEN",
            "openid":"OPENID", 授权用户唯一标识
            "scope":"SCOPE",
            "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
        }*/
        HashMap info = auth(url);

        url = "https://api.weixin.qq.com/sns/userinfo?" +
                "access_token=" + info.get("access_token") +
                "&openid=" + info.get("openid");

        //获取用户信息
        /*{
            "openid":"OPENID",
            "nickname":"NICKNAME",
            "sex":1,
            "province":"PROVINCE",
            "city":"CITY",
            "country":"COUNTRY",
            "headimgurl": "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
            "privilege":[
            "PRIVILEGE1",
            "PRIVILEGE2"
            ],
            "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"

          }*/
        HashMap userInfo = getUserInfo(url);

        // 根据微信的openid查询此用户原来有没有使用过微信登录
        User user = userService.findByWxOpenid(info.get("openid").toString());

        //说明是第一次登录
        if (user==null){
            user = new User();

            // 用户的头像
            user.setPic(userInfo.get("headimgurl").toString());

            // 性别
            user.setGender(userInfo.get("sex").toString());
            // 用户的昵称
            user.setRealName(userInfo.get("nickname").toString());

            // 随机用户名(11位随机字符串)
            user.setUsername(UUID.randomUUID().toString().substring(36 - 15));

            user.setWxOpenid(info.get("openid").toString());

            // 注册一个新的用户
            userService.save(user);
        }

        //将该微信用户d的id存入session
        session.setAttribute("userId",user.getId());
        //存入redis
        redisTemplate.opsForValue().set("loginUser:" + user.getId(), user, 7, TimeUnit.DAYS);

        response.sendRedirect("/html/wx_login_info.html");
    }

    /**
     * 认证
     * 获取AccessToken、openid等数据
     * @param url
     * @return
     */
    private HashMap auth(String url) {

        try {
            // 创建一个http Client请求
            CloseableHttpClient client = HttpClients.createDefault();

            HttpGet httpGet = new HttpGet(url);

            HttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();       // 获取响应数据(json)

            if (entity != null) {
                String result = EntityUtils.toString(entity, Charset.forName("UTF8"));

                return new ObjectMapper().readValue(result, HashMap.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 请求微信对外提供的接口获取用户信息
     * @param url
     * @return
     */
    private HashMap getUserInfo(String url) {

        try {
            CloseableHttpClient client = HttpClients.createDefault();

            HttpGet httpGet = new HttpGet(url);

            CloseableHttpResponse response = client.execute(httpGet);

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String result = EntityUtils.toString(entity, Charset.forName("UTF8"));

                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(result, HashMap.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    /*
    *@date 2020/9/4
    *@param []
    *@return com.xb.entity.Result
    *@description 获取微信登录用户的信息
    */
    @PostMapping("/getWxLoginInfo")
    public Result getWxLoginInfo() throws IOException {

        Object userId = session.getAttribute("userId");

        Map returnMap = new HashMap<>();

        returnMap.put("loginUser", redisTemplate.opsForValue().get("loginUser:" + userId));
        returnMap.put("userId", userId);

        return new Result(true, "获取微信登录信息成功", returnMap);
    }
}
