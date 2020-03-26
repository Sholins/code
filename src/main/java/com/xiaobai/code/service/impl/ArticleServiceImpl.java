package com.xiaobai.code.service.impl;

import com.xiaobai.code.entity.ArcType;
import com.xiaobai.code.entity.Article;
import com.xiaobai.code.lucene.ArticleIndex;
import com.xiaobai.code.repository.ArticleRespository;
import com.xiaobai.code.run.StartupRunner;
import com.xiaobai.code.service.ArcTypeService;
import com.xiaobai.code.service.ArticleService;
import com.xiaobai.code.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资源Service实现类
 */
@Transactional
@Service("articleService")
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleRespository articleRespository;

    @Autowired
    private ArcTypeService arcTypeService;

    @Autowired
    private ArticleIndex articleIndex;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Autowired
    @Lazy
    private StartupRunner startupRunner;

    private RedisSerializer redisSerializer = new StringRedisSerializer();

    @Override
    public List<Article> list(Article s_article, String nickname, String s_bpublishDate, String s_epublishDate, Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        Page<Article> pageArticle = articleRespository.findAll(new Specification<Article>() {
            @Override
            public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return getPredicate(root, criteriaBuilder, s_bpublishDate, s_epublishDate, nickname, s_article);
            }
        }, PageRequest.of(page-1,pageSize,direction,properties));
        return pageArticle.getContent();
    }

    @Override
    public Long getCount(Article s_article, String nickname, String s_bpublishDate, String s_epublishDate) {
        Long count = articleRespository.count(new Specification<Article>() {
            @Override
            public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return getPredicate(root, criteriaBuilder, s_bpublishDate, s_epublishDate, nickname, s_article);
            }
        });
        return count;
    }

    /**
     * 查询条件
     * @param root
     * @param criteriaBuilder
     * @param s_bpublishDate
     * @param s_epublishDate
     * @param nickname
     * @param s_article
     * @return
     */
    private Predicate getPredicate(Root<Article> root, CriteriaBuilder criteriaBuilder, String s_bpublishDate, String s_epublishDate, String nickname, Article s_article) {
        Predicate predicate = criteriaBuilder.conjunction();
        if (StringUtil.isNotEmpty(s_bpublishDate)) {
            predicate.getExpressions().add(criteriaBuilder.greaterThanOrEqualTo(root.get("publishDate").as(String.class), s_bpublishDate));
        }
        if (StringUtil.isNotEmpty(s_epublishDate)) {
            predicate.getExpressions().add(criteriaBuilder.lessThanOrEqualTo(root.get("publishDate").as(String.class), s_epublishDate));
        }
        if (StringUtil.isNotEmpty(nickname)) {
            predicate.getExpressions().add(criteriaBuilder.like(root.get("user").get("nickname"), "%" + nickname + "%"));
        }
        if (s_article != null) {
            if (StringUtil.isNotEmpty(s_article.getName())) {         //标题
                predicate.getExpressions().add(criteriaBuilder.like(root.get("name"), "%" + s_article.getName() + "%"));
            }
            if (s_article.getArcType() != null && s_article.getArcType().getArcTypeId() != null) {         //用户类型
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("arcType").get("arcTypeId"), s_article.getArcType().getArcTypeId()));
            }
            if (s_article.getUser() != null && s_article.getUser().getUserId() != null) {         //用户信息
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"), s_article.getUser().getUserId()));
            }
            if (s_article.isHot()) {         //是否是热门
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("isHot"), 1));
            }
            if (!s_article.isUseful()) {         //资源链接是否有效
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("isUseful"), false));
            }
            if (s_article.getState() != null) {         //审核状态
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("state"), s_article.getState()));
            }
        }
        return predicate;
    }

    @Override
    public void save(Article article) {
        if (article.getState()==2){       //审核通过
            if(redisTemplate.opsForValue().get("article_"+article.getArticleId())==null){ //redis无资源
                redisTemplate.setKeySerializer(redisSerializer);
                redisTemplate.opsForValue().set("article_"+article.getArticleId(),article);
                redisTemplate.opsForList().rightPush("article_type_"+article.getArcType().getArcTypeId(),article.getArticleId());
                redisTemplate.opsForList().leftPush("allarticleId",article.getArticleId());
            }
            articleIndex.addIndex(article);         //添加索引
        }else{
            if(redisTemplate.hasKey("article_"+article.getArticleId())){
                redisTemplate.opsForList().remove("article_type_"+article.getArcType().getArcTypeId(),0,article.getArticleId());
                redisTemplate.opsForList().remove("allarticleId",0,article.getArticleId());
                redisTemplate.delete("article_"+article.getArticleId());
                //articleIndex.updateIndex(article);      //更新索引
            }
            articleIndex.deleteIndex(String.valueOf(article.getArticleId()));   //删除索引
        }
        articleRespository.save(article);
//        if(redisTemplate.opsForValue().get("article_"+article.getArticleId())!=null){
//            redisTemplate.opsForList().remove("article_type_"+article.getArcType().getArcTypeId(),0,article.getArticleId());
//            redisTemplate.opsForList().remove("allarticleId",0,article.getArticleId());
//            redisTemplate.delete("article_"+article.getArticleId());
//            articleIndex.deleteIndex(String.valueOf(article.getArticleId()));   //删除索引
//        }
//        if (article.getState()==2){                 //把审核通过的并且redis中没有的资源放进redis缓存中
//            redisTemplate.setKeySerializer(redisSerializer);
//            redisTemplate.opsForValue().set("article_"+article.getArticleId(),article);
//            redisTemplate.opsForList().rightPush("article_type_"+article.getArcType().getArcTypeId(),article.getArticleId());
//            redisTemplate.opsForList().leftPush("allarticleId",article.getArticleId());
//            articleIndex.addIndex(article);                                     //添加索引
//        }
        startupRunner.loadDate();               //刷新缓存
    }

    @Override
    public void delete(Integer id) {
        redisTemplate.opsForList().remove("allarticleId",0,id);
        int arcTypeId = articleRespository.getOne(id).getArcType().getArcTypeId();
        redisTemplate.opsForList().remove("article_type_"+arcTypeId,0,id);
        redisTemplate.delete("article_"+id);                                            //删除redis缓存
        if (String.valueOf(id) != null||String.valueOf(id) !="") {
            articleIndex.deleteIndex(String.valueOf(id));                                    //删除索引
        }
        articleRespository.deleteById(id);
        startupRunner.loadDate();               //刷新缓存
    }

    @Override
    public Article getById(Integer id) {
        Article article = articleRespository.getOne(id);
        return article;
    }

    @Override
    public List<Article> findByUser(Integer userId) {
        List<Article> articles = articleRespository.findByUser(userId);
        return articles;
    }

    @Override
    public Map<String, Object> list(String type, Integer page, Integer pageSize) {
        //1.初始化redis模板  初始化返回值
        redisTemplate.setKeySerializer(redisSerializer);
        ValueOperations<Object,Object> opsForValue = redisTemplate.opsForValue();
        ListOperations<Object,Object> opsForList = redisTemplate.opsForList();

        Map<String,Object> map = new HashMap<>();
        List<Article> tempList = new ArrayList<>();
        //2.判断redis有没有资源列表
        Boolean flag = redisTemplate.hasKey("allarticleId");
        //3.如果redis里没有资源列表，去数据库中查询
        if (!flag) {
            //3.1遍历资源列表
            List<Article> listStatePass = listStatePass();
            for (Article article:listStatePass){
                //3.2将每个资源放入redis中
                opsForValue.set("article_"+article.getArticleId(),article);
                //3.3将每个资源id推入redis的allArticleId列表
                opsForList.rightPush("allarticleId",article.getArticleId());
                //3.4遍历资源类型列表，将该资源推入相应的redis资源类型列表
                List<ArcType> arcTypeList = arcTypeService.listAll(Sort.Direction.ASC,"sort");
                for(ArcType arcType:arcTypeList){
                    if(article.getArcType().getArcTypeId().intValue() == arcType.getArcTypeId().intValue()){
                        opsForList.rightPush("article_type_"+arcType.getArcTypeId(),article.getArticleId());
                    }
                }
            }
        }
        //4.分页资源列表并返回当前页
        long start = (page-1)*pageSize;
        long end = pageSize*page-1;
        List idList;
        Long count;
        if("all".equals(type)){
            idList = opsForList.range("allarticleId",start,end);
            count = opsForList.size("allarticleId");
        }else {
            idList = opsForList.range("article_type_"+type,start,end);
            count = opsForList.size("article_type_"+type);
        }
        for(Object id:idList){
            tempList.add((Article) opsForValue.get("article_"+id));
        }
        map.put("data",tempList);
        map.put("count",count);
        return map;
    }

    @Override
    public List<Article> listStatePass() {
        return articleRespository.findAll(new Specification<Article>() {
            @Override
            public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("state"),2));
                return predicate;
            }
        },Sort.by(Sort.Direction.DESC,"publishDate"));
    }

    @Override
    public void updateClick(Integer articleId) {
        articleRespository.updateClick(articleId);
        Article article = articleRespository.getOne(articleId);
        if(article.getState()==2) {          //把审核通过的资源放到redis
            redisTemplate.setKeySerializer(redisSerializer);
            redisTemplate.opsForValue().set("article_" + article.getArticleId(), article);
        }
    }

    @Override
    public Integer todayArticle() {
        return articleRespository.todayArticle();
    }

    @Override
    public Integer unAudit() {
        return articleRespository.unAudit();
    }

    @Override
    public List<Article> getHotArticle(Integer n) {
        return articleRespository.getHotArticle(n);
    }

    @Override
    public List<Article> getNewArticle(Integer n) {
        return articleRespository.getNewArticle(n);
    }

    @Override
    public List<Article> getRandomArticle(Integer n) {
        return articleRespository.getRandomArticle(n);
    }
}
