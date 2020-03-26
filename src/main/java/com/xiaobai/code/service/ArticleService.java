package com.xiaobai.code.service;

import com.xiaobai.code.entity.Article;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

/**
 * 资源service层
 */
public interface ArticleService {

    /**
     * 根据分页条件查询资源列表
     * @param s_article             资源条件
     * @param nickname              昵称
     * @param s_bpublishDate        发布开始时间
     * @param s_epublishDate        发布结束时间
     * @param page                  当前页
     * @param pageSize              每页记录数
     * @param direction             排序规则
     * @param properties            排序字段
     * @return
     */
    public List<Article> list(Article s_article, String nickname, String s_bpublishDate, String s_epublishDate, Integer page, Integer pageSize, Direction direction,String...properties);

    /**
     * 根据条件获取总记录数
     * @param s_article             条件
     * @param nickname              昵称
     * @param s_bpublishDate        发布开始时间
     * @param s_epublishDate        发布结束时间
     * @return
     */
    public  Long getCount(Article s_article, String nickname, String s_bpublishDate, String s_epublishDate);

    /**
     * 添加或修改资源
     * @param article
     */
    public void save(Article article);

    /**
     * 根据id删除资源
     * @param id    资源id
     */
    public void delete(Integer id);

    /**
     * 根据id获取资源
     * @param id   资源id
     * @return
     */
    public Article getById(Integer id);

    //根据用户id查找资源列表
    public List<Article> findByUser(Integer userId);

    /**
     * 根据条件查询资源信息（前台）
     * @param type          类型id
     * @param page          当前页
     * @param pageSize      每页记录数
     * @return
     */
    public Map<String ,Object> list(String type,Integer page,Integer pageSize);

    /**
     * 查询审核通过的资源信息
     */
    public List<Article> listStatePass();

    /**
     * 点击数click +1
     */
    public void updateClick(Integer articleId);

    /**
     * 今日发布资源总数
     */
    public Integer todayArticle();

    /**
     * 未审核资源总数
     */
    public Integer unAudit();

    //n条热门资源
    public List<Article> getHotArticle(Integer n);
    //n条最新资源
    public List<Article> getNewArticle(Integer n);
    //n条随机资源（热搜推荐）
    public List<Article> getRandomArticle(Integer n);
}
