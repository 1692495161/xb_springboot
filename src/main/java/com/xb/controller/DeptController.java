package com.xb.controller;


import com.xb.entity.Result;
import com.xb.entity.User;
import com.xb.service.DeptService;
import com.xb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * @author cjj
 * @date 2020/8/31
 * @description
 */
@RestController
@RequestMapping("/dept")
public class DeptController {

    @Autowired
    DeptService deptService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    HttpSession session;

    @Autowired
    HttpServletRequest request;

    @Autowired
    UserService userService;

    /*
     *@date 2020/9/1
     *@param []
     *@return com.xb.entity.Result
     *@description  查询所有的部门
     */
    @GetMapping
    public Result findDeptAll() {
        return new Result(true, "查询成功", deptService.findAll());
    }

    /*
     *@date 2020/9/1
     *@param []
     *@return com.xb.entity.Result
     *@description 查询全部部门信息  部门信息+部门人数
     */
    @GetMapping("/findAllDept")
    public Result findAllDept() {
        //查询所有的部门信息以及部门人数，封装成map
        List<Map> deptData = deptService.findAllDept();

        for (Map dept : deptData) {
            //获取部门id
            long deptId = Long.parseLong(dept.get("id").toString());
            //根据部门id查询属于该部门的用户
            List<User> userList = userService.findByDeptId(deptId);
            //将获取到的用户封装到对应的dept部门中
            dept.put("userList", userList);
        }
        //将获取到的 部门信息+部门人数+部门用户返回前端
        return new Result(true, "查询成功", deptData);
    }
}
