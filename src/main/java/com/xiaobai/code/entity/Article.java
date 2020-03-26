package com.xiaobai.code.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

/**
 * 资源（Article）实体
 */
@Entity
@Table(name = "article")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer","handler","fieldHandler"})
public class Article implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer articleId;          //资源id

    @NotEmpty(message = "资源名称不能为空")
    @Column(length = 200)
    private String name;                //资源名称

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishDate;           //发布时间

    @Transient
    private String publishDateStr;     //发布时间字符串

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;                //用户id

    @ManyToOne
    @JoinColumn(name = "arcTypeId")
    private ArcType arcType;           //资源类型id

    private boolean isFree;       //是否免费资源

    private Integer points;            //积分

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;            //内容

    @Transient
    private String contentNoTag;       //资源内容 网页标签 lucene分词用到

    @Column(length = 200)
    private String download;           //下载地址

    @Column(length = 10)
    private String password;           //密码

    private boolean isHot=false;       //是否热门资源

    private Integer state;             //状态：1未审核2审核通过3审核驳回

    @Column(length = 200)
    private String reason;             //驳回原因

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkDate;          //审核时间

    private Integer click;             //点击数

    @Column(length = 200)
    private String keywords;           //关键字

    @Column(length = 200)
    private String description;        //描述

    private boolean isUseful=true;     //资源链接是否有效

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public String getPublishDateStr() {
        return publishDateStr;
    }

    public void setPublishDateStr(String publishDateStr) {
        this.publishDateStr = publishDateStr;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArcType getArcType() {
        return arcType;
    }

    public void setArcType(ArcType arcType) {
        this.arcType = arcType;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentNoTag() {
        return contentNoTag;
    }

    public void setContentNoTag(String contentNoTag) {
        this.contentNoTag = contentNoTag;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isHot() {
        return isHot;
    }

    public void setHot(boolean hot) {
        isHot = hot;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public Integer getClick() {
        return click;
    }

    public void setClick(Integer click) {
        this.click = click;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUseful() {
        return isUseful;
    }

    public void setUseful(boolean useful) {
        isUseful = useful;
    }

    @Override
    public String toString() {
        return "Article{" +
                "articleId=" + articleId +
                ", name='" + name + '\'' +
                ", publishDate=" + publishDate +
                ", publishDateStr='" + publishDateStr + '\'' +
                ", user=" + user +
                ", arcType=" + arcType +
                ", isFree=" + isFree +
                ", points=" + points +
                ", content='" + content + '\'' +
                ", contentNoTag='" + contentNoTag + '\'' +
                ", download='" + download + '\'' +
                ", password='" + password + '\'' +
                ", isHot=" + isHot +
                ", state=" + state +
                ", reason='" + reason + '\'' +
                ", checkDate=" + checkDate +
                ", click=" + click +
                ", keywords='" + keywords + '\'' +
                ", description='" + description + '\'' +
                ", isUseful=" + isUseful +
                '}';
    }
}
