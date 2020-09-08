package com.xb.dao;

import com.xb.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * @author cjj
 * @date 2020/8/31
 * @description
 */
public interface UserDao extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {

    User findByEmail(String email);

    User findByUsername(String username);

    @Query("update User u set u.password=?2 where u.email=?1" )
    @Modifying
    void updatePassword(String email, String password);

    @Query("update User u set u.loginTime=?2 where u.id=?1")
    @Modifying
    void updateLoginTime(Long userId, Date date);

    Page<User> findByUsernameLike(String username, Pageable pageable);

    @Query("select uf.userFocusId from UserFocus uf where uf.userId=?1 ")
    List<Integer> findFocus(Long userId);

    @Query("select count(1) from UserFocus uf where uf.userFocusId=?1")
    Integer countFocusByUserId(Long userId);

    @Query("update User u set u.look=u.look+1 where u.id=?1")
    @Modifying
    void updateLook(Long userId);

    @Query("select count(1) from UserFocus uf where uf.userId=?1 and uf.userFocusId=?2")
    Integer isFocus(Long userId,Long focusId);

    @Query("delete from UserFocus uf where uf.userId=?1 and uf.userFocusId=?2")
    @Modifying
    void deleteFocus(Long userId, Long focusId);

    //原生SQL：nativeQuery
    @Query(value = "insert into userfocus values (?1,?2)",nativeQuery = true)
    @Modifying
    void insertFocus(Long userId, Long focusId);

    @Query("update User u set u.pic=?2 where u.id=?1")
    @Modifying
    void updatePicUrl(Long userId, String url);

    @Query("select u from UserFocus uf left join User u on uf.userFocusId=u.id where uf.userId=?1")
    Page<User> findFocusPage(Long userId, Pageable of);

    @Query("select u.isSecret from User u where u.id=?1")
    String findIsSecretById(Long userId);

    List<User> findByDeptId(long deptId);

    User findByWxOpenid(String openid);
}
