package com.xb.controller;

import com.xb.entity.Dept;
import com.xb.entity.PageResult;
import com.xb.entity.Result;
import com.xb.entity.User;
import com.xb.service.DeptService;
import com.xb.service.UserService;
import com.xb.util.LoginUserUtil;
import com.xb.webSocket.XBWebSocket;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author cjj
 * @date 2020/8/31
 * @description
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    HttpSession session;

    @Autowired
    HttpServletRequest request;

    @Autowired
    DeptService deptService;

    /*
     *@date 2020/8/31
     *@param [email]
     *@return com.xb.entity.User
     *@description 注册时校验邮箱是否已经绑定过
     */
    @GetMapping("/checkEmail/{email}")
    public Result findByEmail(@PathVariable String email) {
        User user = userService.findByEmail(email);

        if (user != null) {
            return new Result(false, "邮箱已注册了");
        }
        return new Result(true, "邮箱还未注册");
    }

    /*
     *@date 2020/8/31
     *@param [username]
     *@return com.xb.entity.Result
     *@description  注册时校验用户名是否存在
     */
    @GetMapping("/checkUsername/{username}")
    public Result findByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);

        if (user != null) {
            return new Result(false, "该用户名已注册了");
        }
        return new Result(true, "该用户名还未注册");
    }

    /*
     *@date 2020/8/31
     *@param [user]
     *@return com.xb.entity.Result
     *@description  用户注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userService.save(user);
        return new Result(true, "注册成功");
    }

    /*
     *@date 2020/8/31
     *@param [email]
     *@return com.xb.entity.Result
     *@description 发送邮箱，获取验证码
     */
    @PostMapping("/sendEmail/{email}")
    public Result sendEmail(@PathVariable String email) {
        //生成验证码
        String code = RandomStringUtils.randomAlphanumeric(4);
        System.out.println(code);

        //存入redis缓存
        redisTemplate.opsForValue().set("updatePassword:" + email, code);

        return new Result(true, "发送成功", code);
    }

    /*
     *@date 2020/8/31
     *@param [code, user]
     *@return com.xb.entity.Result
     *@description  重置密码
     */
    @PutMapping("/updatePassword/{code}")
    public Result updatePassword(@PathVariable String code, @RequestBody User user) {
        //从redis中获取发送的验证码
        String redisCode = (String) redisTemplate.opsForValue().get("updatePassword:" + user.getEmail());
        if (code.equalsIgnoreCase(redisCode)) {
            userService.updatePassword(user.getEmail(), user.getPassword());

            //密码修改成功后需要删除redis
            redisTemplate.delete("loginUser:"+LoginUserUtil.getId());
            //清空session
            session.invalidate();


            return new Result(true, "修改成功");
        }
        return new Result(false, "验证码错误");
    }

    /*
     *@date 2020/8/31
     *@param [user, checkCode, remember]
     *@return com.xb.entity.Result
     *@description  用户登录
     */
    @PostMapping("/login/{checkCode}")
    public Result login(@RequestBody User user, @PathVariable String checkCode) {
        //从session中获取验证码
        String sessionCode = (String) session.getAttribute("safeCode");

        if (!checkCode.equalsIgnoreCase(sessionCode)) {
            return new Result(false, "验证码错误");
        }
        //读取数据库
        User dbUser = userService.findByUsername(user.getUsername());
        //数据库用户不存在或者密码不正确
        if (dbUser == null || !user.getPassword().equals(dbUser.getPassword())) {
            return new Result(false, "用户名或密码错误");
        }

        //免密登录
        /*if (remember) {
            //7天免登陆，存入redis
            redisTemplate.opsForValue().set("loginUser:" + dbUser.getId(), dbUser, 7, TimeUnit.DAYS);
        } else {*/
            //如果没勾选，有效期为30分钟
            redisTemplate.opsForValue().set("loginUser:" + dbUser.getId(), dbUser, 30, TimeUnit.MINUTES);
        //}

        //把userid存入session
        session.setAttribute("userId", dbUser.getId());

        //更新登录时间
        userService.updateLoginTime(dbUser.getId());

        //将密码置空
        dbUser.setPassword(null);
        //组装成map返回前端
        Map returnMap = new HashMap<>();
        returnMap.put("userId", dbUser.getId());
        returnMap.put("loginUser", dbUser);

        // 群发消息提醒
        XBWebSocket.sendMessageNotUser(dbUser.getId().longValue(),dbUser.getRealName()+"刚刚上线啦！赶快去撩TA");

        return new Result(true, "登录成功", returnMap);
    }

    /*
     *@date 2020/9/1
     *@param [searchMap, page]
     *@return com.xb.entity.Result
     *@description  用户列表搜索+分页，以及当前登录用户关注的用户id
     */
    @PostMapping("/search/{page}")
    public Result search(@RequestBody Map searchMap, @PathVariable Integer page) {
        //如果username为null，赋值为空串
        if (searchMap.get("username") == null) {
            searchMap.put("username", "");
        }

        //根据username模糊查询
        Page<User> pageData = userService.findPage("%" + searchMap.get("username").toString() + "%", page);
        //获取分页对象集
        PageResult<User> userPageResult = new PageResult<>(pageData.getTotalPages(), pageData.getContent());

        //查询当前登录用户关注的用户id
        List<Integer> ids = userService.findFocus(LoginUserUtil.getLoginUser().getId());

        //将查询到的分页对象集和用户关注的id集封装成map返回给前端
        Map returnMap = new HashMap<>();
        returnMap.put("userPageResult", userPageResult);
        returnMap.put("userFocus", ids);

        // 搜索条件回显
        returnMap.put("username", searchMap.get("username"));

        return new Result(true, "查询成功", returnMap);
    }

    /*
     *@date 2020/9/1
     *@param [userId]
     *@return com.xb.entity.Result
     *@description  根据id查询用户
     */
    @GetMapping("/{userId}")
    public Result detail(@PathVariable Long userId) {

        //更新用户的被看数
        //查看自己时被看数不增加
        if (LoginUserUtil.getLoginUser().getId() != userId) {
            userService.updateLook(userId);
        }

        User user = userService.findByUserId(userId);

        //根据id查询该用户被关注数量
        Integer focusCount = userService.countFocusByUserId(userId);

        Map returnMap = new HashMap();
        returnMap.put("user", user);
        returnMap.put("focusCount", focusCount);

        //将用户详情以及用户的关注数量封装成map集合返回前端
        return new Result(true, "查询成功", returnMap);
    }


    /*
     *@date 2020/9/1
     *@param [userId]
     *@return com.xb.entity.Result
     *@description 关注与取消关注的操作
     */
    @PostMapping("/focus/{focusId}")
    public Result focus(@PathVariable Long focusId) {
        Long userId = LoginUserUtil.getLoginUser().getId();
        //不能关注自己
        if (userId == focusId) {
            return new Result(false, "您不能对自己操作");
        }

        //首先判断是否已关注(true为已关注，false为未关注)
        Boolean ifFocus = userService.isFocus(userId, focusId);

        if (ifFocus) {
            //已关注，取关
            userService.unFocus(userId, focusId);
            return new Result(true, "取关成功");
        }

        //否则添加关注
        userService.focus(userId, focusId);

        //单发信息，关注之后推送
        XBWebSocket.sendMessage(focusId, LoginUserUtil.getLoginUser().getRealName() + "刚刚关注了您！");

        return new Result(true, "关注成功");
    }

    /*
     *@date 2020/9/1
     *@param [userId, user]
     *@return com.xb.entity.Result
     *@description 保存用户的修改信息
     */
    @PutMapping("/{userId}")
    public Result updateUser(@PathVariable Long userId, @RequestBody User user) {
        //查询数据库的登录用户信息
        User dbUser = userService.findById(userId);
        //根据登录用户的部门id查询到部门
        Dept dept = deptService.findById(user.getDeptId());

        dbUser.setRealName(user.getRealName());
        dbUser.setDeptName(dept.getName());
        dbUser.setDeptId(user.getDeptId());
        dbUser.setPhone(user.getPhone());
        dbUser.setAge(user.getAge());
        dbUser.setGender(user.getGender());
        dbUser.setIsSecret(user.getIsSecret());

        userService.update(dbUser);

        //更新redis中的登录用户的信息
        redisTemplate.opsForValue().set("loginUser:" + dbUser.getId(), dbUser, 30, TimeUnit.MINUTES);

        return new Result(true, "修改成功", dbUser);
    }

    /*
    *@date 2020/9/1
    *@param [picFile]
    *@return com.xb.entity.Result
    *@description 更换用户的头像路径
    */
    @PostMapping("/changePic")
    public Result changePic(MultipartFile picFile) throws IOException {
        //获取文件上传时的名称
        String originalFilename = picFile.getOriginalFilename();
        //获取文件的后缀类型
        String type = originalFilename.substring(originalFilename.lastIndexOf("."));
        //组建新的文件名称
        String fileName = UUID.randomUUID() + type;

        //获取文件的磁盘路径
        String dirPath = "D:/workspace/upload/" + fileName;

        //将文件写出到本地磁盘
        picFile.transferTo(new File(dirPath));

        //获取文件的网络虚拟地址
        String url = "http://localhost:8080/upload/" + fileName;

        //修改数据库的图片路径
        userService.updatePicUrl(LoginUserUtil.getLoginUser().getId(), url);

        //需要更新redis里的缓存数据
        User loginUser = LoginUserUtil.getLoginUser();
        loginUser.setPic(url);
        redisTemplate.opsForValue().set("loginUser:" + loginUser.getId(), loginUser, 30, TimeUnit.MINUTES);

        //把修改好的图片路径传给前端
        return new Result(true, "上传成功", url);
    }

    /*
    *@date 2020/9/1
    *@param [picFile]
    *@return com.xb.entity.Result
    *@description 查询登录用户的关注列表+分页
    */
    @GetMapping("/findFocusPage/{page}")
    public Result findFocusPage(@PathVariable Integer page){

        Long id = LoginUserUtil.getLoginUser().getId();
        Page<User> pageData=userService.findFocusPage(id,page);

        return new Result(true,"查询成功",
                new PageResult<>(pageData.getTotalPages(),pageData.getContent()));
    }

    /*
    *@date 2020/9/1
    *@param [userId]
    *@return com.xb.entity.Result
    *@description 根据用户id查询是否私密
    */
    @GetMapping("/isSecret/{userId}")
    public Result isSecret(@PathVariable Long userId) {
        if (LoginUserUtil.getLoginUser().getId()!=userId){
            if ("0".equals(userService.findIsSecretById(userId))) {
                return new Result(false, "对方设置了私密");
            }
        }
        return new Result(true, "查询成功");
    }

    /*
    *@date 2020/9/3
    *@param [deptId]
    *@return com.xb.entity.Result
    *@description 根据部门id查询用户
    */
    @GetMapping("/findUserByDeptId/{deptId}")
    public Result findUserByDeptId(@PathVariable Long deptId) {

        List<User> userList=userService.findUserByDeptId(deptId);

        return new Result(true, "查询成功",userList);
    }

    /*
    *@date 2020/9/4
    *@param []
    *@return com.xb.entity.Result
    *@description 退出登录，请出session和redis,在前端请出cookie
    */
    @PostMapping("/logout")
    public Result logout(){

        //群发消息，宣布下线
        XBWebSocket.sendMessageNotUser(LoginUserUtil.getId(), LoginUserUtil.getLoginUser().getRealName() + "刚刚下线了！");

        //清空redis
        redisTemplate.delete("loginUser:"+session.getAttribute("userId"));

        //清空session
        session.invalidate();

        return new Result(true, "退出成功");
    }
}
