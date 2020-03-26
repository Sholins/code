package com.xiaobai.code.run;

import com.xiaobai.code.service.ArcTypeService;
import com.xiaobai.code.service.ArticleService;
import com.xiaobai.code.service.LinkService;
import com.xiaobai.code.service.UserService;
import com.xiaobai.code.util.Consts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;

/**
 * 启动服务加载数据
 */
@Component("StartupRunner")
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private ServletContext application;

    @Autowired
    private ArcTypeService arcTypeService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        loadDate();
    }

    /**
     * 加载数据到application缓存中
     */
    public void loadDate(){
        application.setAttribute(Consts.ARC_TYPE_LIST,arcTypeService.listAll(Sort.Direction.ASC,"sort"));       //所有资源分类
        application.setAttribute(Consts.LINK_LIST,linkService.listAll(Sort.Direction.ASC,"sort"));              //所有友情链接
        application.setAttribute(Consts.CLICK_ARTICLE,articleService.getHotArticle(Consts.ARTICLE_NUM));                    //热门资源
        application.setAttribute(Consts.NEW_ARTICLE,articleService.getNewArticle(Consts.ARTICLE_NUM));                      //最新资源
        application.setAttribute(Consts.RANDOM_ARTICLE,articleService.getRandomArticle(Consts.ARTICLE_NUM));                //随机资源（热搜推荐）
    }
}
