package com.xiaobai.code.controller;

import com.xiaobai.code.service.ArcTypeService;
import com.xiaobai.code.service.ArticleService;
import com.xiaobai.code.util.Consts;
import com.xiaobai.code.util.HTMLUtil;
import com.xiaobai.code.util.StringUtil;
import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * 根路径及其他处理
 */
@Controller
public class IndexController {

    @Autowired
    private ArcTypeService arcTypeService;

    @Autowired
    private ArticleService articleService;
    /**
     * 首页
     */
    @RequestMapping(value = "/")
    public ModelAndView index(){
        ModelAndView mav = new ModelAndView();
        mav.setViewName("index");
        //类型的html代码
        List arcTypeList = arcTypeService.listAll(Sort.Direction.ASC,"sort");
        mav.addObject("arcTypeStr", HTMLUtil.getArcTypeStr("all",arcTypeList));
        //资源列表
        Map<String, Object> map = articleService.list("all",1, Consts.PAGE_SIZE);
        mav.addObject("articleList",map.get("data"));
        //分页代码
        mav.addObject("pageStr",HTMLUtil.getPagation("/article/all",Integer.parseInt(String.valueOf(map.get("count"))),1,"该分类还没有数据..."));
        return mav;
    }

    /**
     * 购买VIP
     */
    @RequestMapping("buyVIP")
    public String buyVIP(){
        return "/buyVIP";
    }
    /**
     * 赚积分
     */
    @RequestMapping("zjf")
    public String zjf(){
        return "/zjf";
    }
}
