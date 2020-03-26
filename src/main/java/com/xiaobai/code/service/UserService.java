package com.xiaobai.code.service;

import com.xiaobai.code.entity.Article;
import com.xiaobai.code.entity.User;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 用户service层接口
 */
public interface UserService {

    /**
     * 根据id获取用户信息
     */
    public User findById(Integer id);

    /**
     * 根据用户名查找用户信息
     */
    public User findByUserName(String userName);

    /**
     * 根据邮箱查找用户信息
     */
    public User findByEmail(String email);

    /**
     * 添加或修改用户信息
     */
    public void save(User user);

    /**
     * 根据条件获取用户总数
     */
    public Long getCount(User user, String s_blatelyLoginTime,String s_elatelyLoginTime);

    /**
     * 今日用户注册数
     */
    public Integer todayRegister();

    /**
     * 今日用户登录数
     */
    public Integer todayLogin();

    /**
     * 根据分页条件查询用户列表
     */
    public List<User> list(User user, String s_blatelyLoginTime, String s_elatelyLoginTime, Integer page, Integer pageSize, Sort.Direction direction, String...properties);

}
