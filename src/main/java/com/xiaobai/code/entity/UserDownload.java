package com.xiaobai.code.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户下载实体
 */
@Entity
@Table(name="userDownload")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer","handler","fieldHandler"})
public class UserDownload implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userDownloadId;             //用户下载id

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date downloadDate;                  //下载时间

    @ManyToOne
    @JoinColumn(name = "articleId")
    private Article article;                    //下载资源id

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;                         //下载资源id

    public Integer getUserDownloadId() {
        return userDownloadId;
    }

    public void setUserDownloadId(Integer userDownloadId) {
        this.userDownloadId = userDownloadId;
    }

    public Date getDownloadDate() {
        return downloadDate;
    }

    public void setDownloadDate(Date downloadDate) {
        this.downloadDate = downloadDate;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
