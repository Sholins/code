package com.xiaobai.code.controller.admin;

import com.xiaobai.code.service.ArticleService;
import com.xiaobai.code.service.UserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 管理员登录界面
 */
@Controller
public class IndexAdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private ArticleService articleService;
    /**
     * 管理员登陆成功，跳转主页
     */
    @RequiresPermissions("进入管理员页面")
    @RequestMapping("/toAdminUserCenterPage")
    public String toAdminUserCenterPage(){
        return "/admin/index";
    }
    /**
     * 管理员登陆成功，跳转主页
     */
    @RequiresPermissions("进入管理员页面")
    @RequestMapping("/defaultIndex")
    public ModelAndView defaultIndex(){
        ModelAndView mav = new ModelAndView();
        mav.addObject("userNum",userService.getCount(null,null,null));
        mav.addObject("todayRegister",userService.todayRegister());
        mav.addObject("todayLogin",userService.todayLogin());
        mav.addObject("todayArticle",articleService.todayArticle());
        mav.addObject("unAudit",articleService.unAudit());
        mav.setViewName("/admin/default");
        return mav;
    }
}
