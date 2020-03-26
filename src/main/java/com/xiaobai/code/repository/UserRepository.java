package com.xiaobai.code.repository;

import com.xiaobai.code.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 用户repository层接口
 */
public interface UserRepository extends JpaRepository<User,Integer>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户信息
     */
    @Query(value = "select * from user where user_name=?1",nativeQuery = true)
    public User findByUserName(String userName);

    /**
     * 根据邮箱查找用户信息
     */
    @Query(value = "select * from user where email=?1",nativeQuery = true)
    public User findByEmail(String email);

    /**
     * 今日用户注册数
     */
    @Query(value = "select count(*) from user where TO_DAYS(registration_date)=TO_DAYS(NOW());",nativeQuery = true)
    public Integer todayRegister();

    /**
     * 今日用户登录数
     */
    @Query(value = "select count(*) from user where TO_DAYS(lately_login_time)=TO_DAYS(NOW());",nativeQuery = true)
    public Integer todayLogin();
}

