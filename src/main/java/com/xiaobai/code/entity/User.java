package com.xiaobai.code.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体
 */
@Entity
@Table(name="user")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer","handler","fieldHandler"})
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;             //用户id

    @Column(length = 200)
    private String openId;              //qq唯一标识

    @NotEmpty(message = "昵称不能为空！")
    @Column(length = 200)
    private String nickname;           //昵称

    @NotEmpty(message = "请输入用户名！")
    @Column(length = 100)
    private String userName;           //用户名

    @NotEmpty(message = "请输入密码！")
    @Column(length = 100)
    private String password;           //密码

    @Email(message = "邮箱号码有误！")
    @NotEmpty(message = "请输入邮箱地址！")
    @Column(length = 100)
    private String email;               //邮箱地址

    @Column(length = 100)
    private String headPortrait;       //头像

    @Column(length = 50)
    private String sex;                 //性别

    private Integer points = 0;           //积分
    private boolean isVip = false;        //是否是VIP
    private Integer vipGrade = 0;         //vip等级
    private boolean isOff = false;        //是否被封禁
    private String roleName = "会员";     //角色名称 （管理员、会员）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date registrationDate;      //注册时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date latelyLoginTime;      //最近登录时间

    //@Transient表示不是数据库字段
    @Transient
    private Integer messageCount;       //消息数

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHeadPortrait() {
        return headPortrait;
    }

    public void setHeadPortrait(String headPortrait) {
        this.headPortrait = headPortrait;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }

    public Integer getVipGrade() {
        return vipGrade;
    }

    public void setVipGrade(Integer vipGrade) {
        this.vipGrade = vipGrade;
    }

    public boolean isOff() {
        return isOff;
    }

    public void setOff(boolean off) {
        isOff = off;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getLatelyLoginTime() {
        return latelyLoginTime;
    }

    public void setLatelyLoginTime(Date latelyLoginTime) {
        this.latelyLoginTime = latelyLoginTime;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", openId='" + openId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", headPortrait='" + headPortrait + '\'' +
                ", sex='" + sex + '\'' +
                ", points=" + points +
                ", isVip=" + isVip +
                ", vipGrade=" + vipGrade +
                ", isOff=" + isOff +
                ", roleName='" + roleName + '\'' +
                ", registrationDate=" + registrationDate +
                ", latelyLoginTime=" + latelyLoginTime +
                ", messageCount=" + messageCount +
                '}';
    }
}