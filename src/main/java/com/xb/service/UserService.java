package com.xb.service;

import com.mysql.jdbc.StringUtils;
import com.xb.dao.UserDao;
import com.xb.entity.PageResult;
import com.xb.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author cjj
 * @date 2020/8/31
 * @description
 */
@Service
public class UserService {

    @Autowired
    UserDao userDao;

    //查看该邮箱是否已注册
    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    //根据用户名查看用户是否存在
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    //用户注册
    public void save(User user) {
        // 设置默认属性
        user.setIsSecret("0");          // 默认不私密
        user.setLook(0L);               // 默认0的查看数
        user.setRegisterTime(new Date());               // 注册时间

        // 设置默认头像
        if (StringUtils.isNullOrEmpty(user.getPic())) {
            user.setPic("https://www.baidu.com/favicon.ico");
        }
        userDao.save(user);
    }

    //根据邮箱重置用户的密码
    @Transactional
    public void updatePassword(String email, String password) {
        userDao.updatePassword(email, password);
    }

    //更新用户的登录时间
    @Transactional
    public void updateLoginTime(Long userId) {
        userDao.updateLoginTime(userId, new Date());
    }


    public User findById(long userId) {
        return userDao.findById(userId).get();
    }

    //条件查询用户列表+分页
    public Page<User> findPage(String username, Integer page) {
        return userDao.findByUsernameLike(username, PageRequest.of(page - 1, PageResult.PAGE_SIZE));
    }

    //获取登录用户关注的用户ID
    public List<Integer> findFocus(Long userId) {
        return userDao.findFocus(userId);
    }


    public User findByUserId(Long userId) {
        return userDao.findById(userId).get();
    }

    //查看用户被关注数
    public Integer countFocusByUserId(Long userId) {
        return userDao.countFocusByUserId(userId);
    }

    //跟更新被看数
    @Transactional
    public void updateLook(Long userId) {
        userDao.updateLook(userId);
    }


    //查询是否已关注某个用户
    public Boolean isFocus(Long userId,Long focusId) {
        return userDao.isFocus(userId,focusId) > 0 ? true : false;
    }

    //取消关注
    @Transactional
    public void unFocus(Long userId, Long focusId) {
        userDao.deleteFocus(userId,focusId);
    }

    //添加关注
    @Transactional
    public void focus(Long userId, Long focusId) {
        userDao.insertFocus(userId,focusId);
    }

    //保存用户的修改信息
    @Transactional
    public void update(User dbUser) {
        userDao.save(dbUser);
    }

    //修改用户的头像路径
    @Transactional
    public void updatePicUrl(Long userId, String url) {
        userDao.updatePicUrl(userId,url);
    }

    //查询关注的用户列表+分页
    public Page<User> findFocusPage(Long userId, Integer page) {
        return userDao.findFocusPage(userId,PageRequest.of(page-1,PageResult.PAGE_SIZE));
    }

    //根据用户查询是否私密
    public String findIsSecretById(Long userId) {
        return userDao.findIsSecretById(userId);
    }

    public List<User> findByDeptId(long deptId) {
        return userDao.findByDeptId(deptId);
    }

    public List<User> findUserByDeptId(Long deptId) {
        return userDao.findByDeptId(deptId);
    }


    public User findByWxOpenid(String openid) {
        return userDao.findByWxOpenid(openid);
    }
}
