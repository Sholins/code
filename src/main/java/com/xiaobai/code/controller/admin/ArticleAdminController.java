package com.xiaobai.code.controller.admin;

import com.xiaobai.code.entity.Article;
import com.xiaobai.code.entity.Message;
import com.xiaobai.code.entity.User;
import com.xiaobai.code.lucene.ArticleIndex;
import com.xiaobai.code.service.ArticleService;
import com.xiaobai.code.service.CommentService;
import com.xiaobai.code.service.MessageService;
import com.xiaobai.code.util.StringUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 管理员-资源管理
 */
@RestController
@RequestMapping(value = "/admin/article")
public class ArticleAdminController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ArticleIndex articleIndex;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    /**
     * 根据条件查询资源信息列表
     * @param s_article     资源对象
     * @param nickname      昵称
     * @param publishDates  发布日期
     * @param page          当前页
     * @param pageSize      每页记录数
     * @return
     */
    @RequestMapping(value = "/list")
    @RequiresPermissions("分页查询资源信息列表")
    public Map<String,Object> list(Article s_article,
                                   @RequestParam(value = "nickname",required = false)String nickname,
                                   @RequestParam(value = "publishDates",required = false)String publishDates,
                                   @RequestParam(value = "page",required = false)Integer page,
                                   @RequestParam(value = "pageSize",required = false)Integer pageSize){
        Map<String ,Object> map =new HashMap<>();
        String s_bpublishDate = null;                               //开始时间
        String s_epublishDate = null;                               //结束时间
        if(StringUtil.isNotEmpty(publishDates)){
            String[] str = publishDates.split(" - ");        //拆分时间段
            s_bpublishDate = str[0];
            s_epublishDate = str[1];
        }
        map.put("data",articleService.list(s_article,nickname,s_bpublishDate,s_epublishDate,page,pageSize, Sort.Direction.DESC,"publishDate"));
        map.put("total",articleService.getCount(s_article,nickname,s_bpublishDate,s_epublishDate));
        map.put("errorNo",0);
        return map;
    }

    /**
     * 根据id查看资源信息
     */
    @RequestMapping(value = "/findById")
    @RequiresPermissions(value = "查看资源信息")
    public Map<String,Object> findById(@RequestParam(value = "articleId") Integer articleId) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        Article article = articleService.getById(articleId);
        tempMap.put("articleId",article.getArticleId());
        tempMap.put("name",article.getName());
        tempMap.put("arcType",article.getArcType().getArcTypeId());
        tempMap.put("points",article.getPoints());
        tempMap.put("content",article.getContent());
        tempMap.put("download",article.getDownload());
        tempMap.put("password",article.getPassword());
        tempMap.put("click",article.getClick());
        tempMap.put("keywords",article.getKeywords());
        tempMap.put("description",article.getDescription());
        map.put("data",tempMap);
        map.put("errorNo",0);
        return map;
    }

    /**
     * 根据id批量删除资源信息
     */
    @RequestMapping(value = "/delete")
    @RequiresPermissions(value = "删除资源信息")
    public Map<String,Object> delete(@RequestParam(value = "articleId") String ids){
        Map<String ,Object> map =new HashMap<>();
        String[] idsStr = ids.split(",");
        for (int i=0;i<idsStr.length;i++){
            commentService.deleteCommentByArticleId(Integer.parseInt(idsStr[i]));       //删除评论
            articleService.delete(Integer.parseInt(idsStr[i]));                         //批量删除资源
        }
        map.put("errorNo",0);
        return map;
    }

    /**
     * 审核资源
     */
    @RequestMapping(value = "/updateState")
    @RequiresPermissions(value = "审核资源")
    public Map<String,Object> updateState(Article article){
        Map<String ,Object> map =new HashMap<>();
        Article oldArticle = articleService.getById(article.getArticleId());        //查找到资源
        Message message = new Message();
        message.setUser(oldArticle.getUser());
        message.setPublishDate(new Date());
        oldArticle.setCheckDate(new Date());
        if(article.getState()==2){                                                  //审核通过
            message.setContent("【<font color='green'>审核成功</font>】您发布的资源【<font color='blue'>"+oldArticle.getName()+"</font>】审核成功！");
            oldArticle.setState(2);
        }else if(article.getState()==3){                                            //审核不通过
            message.setCause(oldArticle.getReason());
            message.setContent("【<font color='red'>审核失败</font>】您发布的资源【<font color='blue'>"+oldArticle.getName()+"</font>】审核失败，请修改后重新发布！");
            oldArticle.setState(3);
            oldArticle.setReason(article.getReason());                              //审核不通过原因
        }
        messageService.save(message);
        articleService.save(oldArticle);
        map.put("errorNo",0);
        return map;
    }

    /**
     * 生成所有资源索引
     */
    @ResponseBody
    @RequestMapping(value = "/genAllIndex")
    @RequiresPermissions(value = "生成所有资源索引")
    public boolean genAllIndex(){
        List<Article> articleList = articleService.listStatePass();
        if(articleList==null||articleList.size()==0){
            return false;
        }
        for (Article article:articleList){
            try {
                article.setContentNoTag(StringUtil.stripHtml(article.getContent()));
                articleIndex.addIndex(article);
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     *修改是否热门资源
     */
    @ResponseBody
    @RequestMapping(value = "/updateIsHot")
    @RequiresPermissions("修改是否热门资源")
    public Map<String,Object> updateIsHot(Integer articleId,Boolean isHot){
        Map<String ,Object> map =new HashMap<>();
        Article oldArticle = articleService.getById(articleId);
        oldArticle.setHot(isHot);
        articleService.save(oldArticle);
        map.put("success",true);
        return map;
    }

    /**
     *修改是否免费资源
     */
    @ResponseBody
    @RequestMapping(value = "/updateIsFree")
    @RequiresPermissions("修改是否免费资源")
    public Map<String,Object> updateIsFree(Integer articleId,Boolean isFree){
        Map<String ,Object> map =new HashMap<>();
        Article oldArticle = articleService.getById(articleId);
        oldArticle.setFree(isFree);
        articleService.save(oldArticle);
        map.put("success",true);
        return map;
    }
}
