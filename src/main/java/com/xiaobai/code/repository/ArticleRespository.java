package com.xiaobai.code.repository;

import com.xiaobai.code.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 资源Repository
 */
public interface ArticleRespository extends JpaRepository<Article,Integer>, JpaSpecificationExecutor<Article> {

    /**
     * 点击数click +1
     */
    @Query(value = "update article set click=click+1 where article_id=?1",nativeQuery = true)
    @Modifying
    public void updateClick(Integer articleId);

    /**
     * 今日发布资源总数
     */
    @Query(value = "select count(*) from article where TO_DAYS(publish_date)=TO_DAYS(NOW());",nativeQuery = true)
    public Integer todayArticle();

    /**
     * 未审核资源总数
     */
    @Query(value = "select count(*) from article where state=1;",nativeQuery = true)
    public Integer unAudit();

    //根据用户id查找资源列表
    @Query(value = "select * from article where user_id=?1",nativeQuery = true)
    public List<Article> findByUser(Integer n);

    //10条热门资源
    @Query(value = "select * from article where state=2 order by click desc limit ?1",nativeQuery = true)
    public List<Article> getHotArticle(Integer n);
    //10条最新资源
    @Query(value = "select * from article where state=2 order by publish_date desc limit ?1",nativeQuery = true)
    public List<Article> getNewArticle(Integer n);
    //10条随机资源（热搜推荐）
    @Query(value = "select * from article where state=2 order by RAND() limit ?1",nativeQuery = true)
    public List<Article> getRandomArticle(Integer n);
}
