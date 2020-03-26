package com.xiaobai.code.controller;

import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.xiaobai.code.entity.*;
import com.xiaobai.code.lucene.ArticleIndex;
import com.xiaobai.code.service.*;
import com.xiaobai.code.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.apache.tomcat.util.bcel.Const;
import org.aspectj.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.swing.*;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ArticleIndex articleIndex;

    @Autowired
    private UserDownloadService userDownloadService;

    @Autowired
    private MessageService messageService;

    @Resource
    private JavaMailSender mailSender;

    @Value("${imgFilePath}")
    private String imgFilePath;

    /**
     * 用户注册
     */
    @ResponseBody
    @RequestMapping("/register")
    public Map<String,Object> register(@Valid User user, BindingResult bindingResult){
        Map<String ,Object> map = new HashMap<>();
        if (bindingResult.hasErrors()){
            map.put("success",false);
            map.put("errorInfo",bindingResult.getFieldError().getDefaultMessage());
        }else if(userService.findByUserName(user.getUserName())!=null){
            map.put("success",false);
            map.put("errorInfo","用户名已存在，请更换");
        }else if(userService.findByEmail(user.getEmail())!=null){
            map.put("success",false);
            map.put("errorInfo","邮箱已存在，请更换");
        }else{
            user.setPassword(CryptographyUtil.md5(user.getPassword(),CryptographyUtil.SALT));
            user.setRegistrationDate(new Date());
            user.setLatelyLoginTime(new Date());
            user.setHeadPortrait("tou.jpg");
            userService.save(user);
            map.put("success",true);
        }
        return map;
    }

    /**
     * 用户登录
     */
    @ResponseBody
    @RequestMapping("/login")
    public Map<String,Object> login(User user, HttpSession httpSession){
        Map<String,Object> map =new HashMap<>();
        if(StringUtil.isEmpty(user.getUserName())){
            map.put("success",false);
            map.put("errorInfo","请输入用户名");
        }else if(StringUtil.isEmpty(user.getPassword())){
            map.put("success",false);
            map.put("errorInfo","请输入密码");
        }else {
            Subject subject = SecurityUtils.getSubject();
            UsernamePasswordToken token = new UsernamePasswordToken(user.getUserName(),
                    CryptographyUtil.md5(user.getPassword(),CryptographyUtil.SALT));
            try {
                subject.login(token);           //登录验证
                String userName = (String) SecurityUtils.getSubject().getPrincipal();
                User currentUser = userService.findByUserName(userName);
                if (currentUser.isOff()) {
                    map.put("success",false);
                    map.put("errorInfo","该用户已经封禁，请联系管理员");
                    subject.logout();
                }else{
                    currentUser.setLatelyLoginTime(new Date());
                    userService.save(currentUser);
                    //未读消息数放入session
                    Integer messageCount = messageService.getMessageCount(currentUser.getUserId());
                    currentUser.setMessageCount(messageCount);
                    //失效资源数放入session
                    Article article = new Article();
                    article.setUseful(false);
                    article.setUser(currentUser);
                    httpSession.setAttribute(Consts.UN_USEFUL_ARTICLE_COUNT,articleService.getCount(article,null,null,null));
                    httpSession.setAttribute(Consts.CURRENT_USER,currentUser);
                    map.put("success",true);
                }
            }catch (Exception e){
                e.printStackTrace();
                map.put("success",false);
                map.put("errorInfo","用户名或密码错误");
            }
        }
        return map;
    }

    /**
     * 发送邮件
     */
    @ResponseBody
    @RequestMapping("/sendEmail")
    public Map<String,Object> sendEmail(String email, HttpSession httpSession){
        Map<String,Object> map =new HashMap<>();
        if(StringUtil.isEmpty(email)){
            map.put("success",false);
            map.put("errorInfo","请输入邮箱地址");
            return map;
        }
        User user = userService.findByEmail(email);
        if (user == null) {
            map.put("success",false);
            map.put("errorInfo","邮箱地址不存在");
            return map;
        }
        String mailCode = StringUtil.getSixRandom();
        //发送邮件
        SimpleMailMessage message = new SimpleMailMessage();         //消息构造器
        message.setFrom("1255879874@qq.com");                        //发件人
        message.setTo(email);                                        //收件人
        message.setSubject("Java资源分享网-用户找回密码");             //主题
        message.setText("您本次的验证码是："+mailCode);                //正文内容
        mailSender.send(message);
        System.out.println(mailCode);
        //验证码存到session中去
        httpSession.setAttribute(Consts.MAIL_CODE_NAME,mailCode);
        httpSession.setAttribute(Consts.USER_ID_NAME,user.getUserId());
        map.put("success",true);
        return map;
    }

    /**
     * 邮箱验证码判断
     */
    @ResponseBody
    @RequestMapping("/checkYzm")
    public Map<String,Object> checkYzm(String yzm, HttpSession httpSession){
        Map<String,Object> map =new HashMap<>();
        if(StringUtil.isEmpty(yzm)){
            map.put("success",false);
            map.put("errorInfo","请输入验证码");
            return map;
        }
        String mailCode = (String) httpSession.getAttribute(Consts.MAIL_CODE_NAME);
        Integer userId = (Integer) httpSession.getAttribute(Consts.USER_ID_NAME);

        if(!yzm.equals(mailCode)){
            map.put("success",false);
            map.put("errorInfo","验证码有误，请重新输入！");
            return map;
        }
        //给用户重置密码为123456
        User user = userService.findById(userId);
        user.setPassword(CryptographyUtil.md5(Consts.PASSWORD,CryptographyUtil.SALT));
        userService.save(user);
        map.put("success",true);
        return map;
    }

    /**
     * 资源跳转
     */
    @GetMapping("/articleManage")
    public String articleManager(){
        return "/user/articleManage";
    }

    /**
     * 按条件分页查询资源
     * @param s_articel   条件
     * @param page        当前页
     * @param pageSize    每页数量
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("/articleList")
    public Map<String,Object> articleList(Article s_articel, @RequestParam(value ="page",required = false)Integer page,
                                          @RequestParam(value ="limit",required = false)Integer pageSize, HttpSession session ){
        Map<String,Object> map = new HashMap<>();
        User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
        s_articel.setUser(currentUser);
        map.put("data",articleService.list(s_articel,null,null,null,page,pageSize, Sort.Direction.DESC,"publishDate"));
        map.put("count",articleService.getCount(s_articel,null,null,null));         //总记录数
        map.put("code",0);
        return map;
    }

    /**
     * 进入资源发布页面
     */
    @GetMapping("toAddArticle")
    public String toAddArticle(){
        return "/user/AddArticle";
    }

    /**
     * 添加或修改资源
     */
    @ResponseBody
    @RequestMapping("/saveArticle")
    public Map<String,Object> saveArticle(Article article,HttpSession session) throws IOException {
        Map<String,Object> map = new HashMap<>();
        if (article.getPoints() < 0 || article.getPoints() > 10) {
            map.put("success",false);
            map.put("errorInfo","积分超出正常区间！");
            return map;
        }
        if(!CheckShareLinkEnableUtil.check(article.getDownload())){
            map.put("success",false);
            map.put("errorInfo","百度云分享链接已经失效，请重新发布！");
            return map;
        }
        User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
        if(article.getArticleId()==null){                               //添加资源
            article.setPublishDate(new Date());
            article.setUser(currentUser);
            if(article.getPoints()==0){
                article.setFree(true);                                  //积分为0 设置免费
            }
            article.setState(1);                                        //未审核状态
            article.setClick(new Random().nextInt(150)+50);     //设置点击数为50~200
            articleService.save(article);
            map.put("success",true);
        }else {                                                                     //修改资源
            Article oldArticle = articleService.getById(article.getArticleId());    //获取实体
            if(oldArticle.getUser().getUserId().intValue()==currentUser.getUserId().intValue()){
                oldArticle.setName(article.getName());
                oldArticle.setPublishDate(new Date());
                oldArticle.setArcType(article.getArcType());
                oldArticle.setDownload(article.getDownload());
                oldArticle.setPassword(article.getPassword());
                oldArticle.setKeywords(article.getKeywords());
                oldArticle.setDescription(article.getDescription());
                oldArticle.setContent(article.getContent());
                oldArticle.setState(1);                             //用户点击修改后则需要重新审核，状态变为待审核
                articleService.save(oldArticle);
                map.put("success",true);
            }
        }
        return map;
    }

    /**
     * 检查资源是否属于当前用户
     * @return
     */
    @ResponseBody
    @RequestMapping("/checkArticleUser")
    public Map<String,Object> checkArticleUser(Integer articleId,HttpSession session){
        Map<String,Object> map = new HashMap<>();
        User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
        Article article = articleService.getById(articleId);
        if (article.getUser().getUserId().intValue() == currentUser.getUserId().intValue()) {
            map.put("success",true);
        }else {
            map.put("success",false);
            map.put("errorInfo","您不是资源所有者，不能修改！");
        }
        return map;
    }

    /**
     * 进入修改资源页面
     */
    @GetMapping("/toEditArticle/{articleId}")
    public ModelAndView toEditArticle(@PathVariable(value="articleId",required = true)Integer articleId){
        ModelAndView mav = new ModelAndView();
        Article article = articleService.getById(articleId);
        mav.addObject("article",article);
        mav.setViewName("/user/editArticle");
        return mav;
    }

    /**
     * 根据articleId删除资源
     */
    @ResponseBody
    @RequestMapping("/articleDelete")
    public Map<String, Object> articleDelete(Integer articleId,HttpSession session){
        Map<String,Object> map = new HashMap<>();
        User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
        Article article = articleService.getById(articleId);
        if (article.getUser().getUserId().intValue() == currentUser.getUserId().intValue()) {
            commentService.deleteCommentByArticleId(articleId);//----------------------------------------------删除资源前应该先删除评论
            articleService.delete(articleId);
            articleIndex.deleteIndex(String.valueOf(articleId));            //把资源从lucene中删除
            map.put("success",true);
        }else {
            map.put("success",false);
            map.put("errorInfo","您不是资源所有者，不能删除！");
        }
        return map;
    }

    /**
     * 评论自己的资源
     */
    @ResponseBody
    @PostMapping(value = "/addComment")
    public Map<String,Object> addComment(Comment comment, HttpSession session){
        Map<String,Object> map = new HashMap<>();
        comment.setContent(StringUtil.esc(comment.getContent()));
        comment.setCommentDate(new Date());
        comment.setState(1);
        comment.setUser((User) session.getAttribute(Consts.CURRENT_USER));
        commentService.save(comment);
        map.put("success",true);
        return map;
    }

    /**
     * 判断某资源是否被当前用户下载过
     */
    @ResponseBody
    @RequestMapping("/userDownloadExits")
    public boolean userDownloadExits(Integer articleId,HttpSession session){
        User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
        Integer count = userDownloadService.getCountByUidAndByAid(currentUser.getUserId(),articleId);
        if(count>0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 判断用户是否有足够的积分下载资源
     */
    @ResponseBody
    @RequestMapping("/userPoints")
    public boolean userPoints(Integer points,HttpSession session){
        User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
        Integer userPoints = currentUser.getPoints();
        if(userPoints>points){             //积分足够
            return true;
        }else{                              //积分不够
            return false;
        }
    }

    /**
     * 跳转到用户下载页面
     */
    @RequestMapping("/userDownloadPage/{articleId}")
    public ModelAndView userDownloadPage(@PathVariable("articleId")Integer articleId,HttpSession session){
        Article article = articleService.getById(articleId);
        //查不到或审核不通过直接返回
        if(article==null||article.getState().intValue()!=2){
            return null;
        }
        User userCurrent = (User) session.getAttribute(Consts.CURRENT_USER);
        Integer count = userDownloadService.getCountByUidAndByAid(userCurrent.getUserId(),articleId);
        if (count == 0) {                                                       //未下载过
            if (!article.isFree()) {
                if (userCurrent.getPoints() - article.getPoints() < 0) {        //积分不够
                    return null;
                }
                //扣除积分，并存入数据库
                userCurrent.setPoints(userCurrent.getPoints() - article.getPoints());
                userService.save(userCurrent);
                //资源分享者获得相应积分
                User articleUser = article.getUser();
                articleUser.setPoints(articleUser.getPoints()+article.getPoints());
                userService.save(articleUser);
            }
            //保存用户下载相关信息
            UserDownload userDownload = new UserDownload();
            userDownload.setArticle(article);
            userDownload.setUser(userCurrent);
            userDownload.setDownloadDate(new Date());
            userDownloadService.save(userDownload);
        }
        ModelAndView mav = new ModelAndView();
        mav.addObject("article",article);
        mav.setViewName("/article/DownloadPage");
        return mav;
    }

    /**
     * 跳转到vip用户下载页面
     */
    @RequestMapping("/userVipDownloadPage/{articleId}")
    public ModelAndView userVipDownloadPage(@PathVariable("articleId")Integer articleId,HttpSession session){
        Article article = articleService.getById(articleId);
        //资源查不到或审核未通过,直接返回
        if(article==null||article.getState().intValue()!=2){
            return null;
        }
        User userCurrent = (User) session.getAttribute(Consts.CURRENT_USER);
        //用户不是VIP,直接返回
        if(!userCurrent.isVip()){
            return null;
        }
        Integer count = userDownloadService.getCountByUidAndByAid(userCurrent.getUserId(),articleId);
        if (count == 0) {              //未下载过
            //保存用户下载相关信息
            UserDownload userDownload = new UserDownload();
            userDownload.setArticle(article);
            userDownload.setUser(userCurrent);
            userDownload.setDownloadDate(new Date());
            userDownloadService.save(userDownload);
        }
        ModelAndView mav = new ModelAndView();
        mav.addObject("article",article);
        mav.setViewName("/article/DownloadPage");
        return mav;
    }

    /**
     * 判断当前用户是否是VIP
     */
    @ResponseBody
    @RequestMapping("/isVIP")
    public boolean isVIP(HttpSession session){
        User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
        return currentUser.isVip();
    }

    /**
     * 进入失效资源页面
     */
    @GetMapping("toUnUserfulArticle")
    public String toUnUserfulArticle(HttpSession session){
        this.unUsefulArticleCount(session);
        return "/user/unUserfulArticle";
    }

    /**
     * 获取失效资源数
     */
    public void unUsefulArticleCount(HttpSession session){
        User user = (User) session.getAttribute(Consts.CURRENT_USER);
        Article s_article = new Article();
        s_article.setUseful(false);
        s_article.setUser(user);
        session.setAttribute(Consts.UN_USEFUL_ARTICLE_COUNT,articleService.getCount(s_article,null,null,null));
    }

    /**
     * 修改百度云分享链接
     */
    @ResponseBody
    @RequestMapping("/modifyArticleShareLink")
    public Map<String,Object> modifyArticleShareLink(Article article,HttpSession session) throws IOException {
        Map<String,Object> map = new HashMap<>();
        if(CheckShareLinkEnableUtil.check(article.getDownload())){
            Article oldArticle = articleService.getById(article.getArticleId());
            User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
            if(oldArticle.getUser().getUserId().intValue()==currentUser.getUserId().intValue()){    //修改百度云链接
                oldArticle.setDownload(article.getDownload());
                oldArticle.setPassword(article.getPassword());
                oldArticle.setUseful(true);
                articleService.save(oldArticle);
                map.put("success",true);
                this.unUsefulArticleCount(session);
            }else{
                map.put("success",false);
                map.put("errorInfo","对不起！您不是资源所有者，没有权限修改此信息！");
            }
        }else {
            map.put("success",false);
            map.put("errorInfo","百度云分享链接已经失效，请重新发布！");
        }
        return map;
    }

    /**
     * 进入已下载资源页面
     */
    @GetMapping("/toHaveDownload/{currentPage}")
    public ModelAndView toHaveDownload(@PathVariable(value = "currentPage",required = false)Integer currentPage,HttpSession session){
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
        //已下载资源的列表
        Page<UserDownload> page = userDownloadService.listAll(currentUser.getUserId(),currentPage,Consts.PAGE_SIZE, Sort.Direction.DESC,"downloadDate");
        mav.addObject("userDownloadList",page.getContent());
        //分页代码
        mav.addObject("pageStr", HTMLUtil.getPagation("/user/toHaveDownload",page.getTotalPages(),currentPage,"您目前没有下载过任何资源！"));
        mav.setViewName("/user/haveDownload");
        return mav;
    }

    /**
     * 进入我的消息页面
     */
    @GetMapping("/userMessage/{currentPage}")
    public ModelAndView userMessage(@PathVariable(value = "currentPage",required = false)Integer currentPage,HttpSession session){
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
        //进入页面默认阅读了所有消息
        if(currentUser.getMessageCount()==null||currentUser.getMessageCount()>0){
            messageService.updateState(currentUser.getUserId());
            currentUser.setMessageCount(0);
        }
        session.setAttribute(Consts.CURRENT_USER,currentUser);
        //当前用户消息总数
        Integer userMessageCount = messageService.getCount(currentUser.getUserId()).intValue();
        //当前用户消息列表
        Page<Message> messagePage = messageService.list(currentUser.getUserId(),currentPage,Consts.PAGE_SIZE, Sort.Direction.DESC,"publishDate");
        mav.addObject("messageList",messagePage.getContent());
        //分页代码
        mav.addObject("pageStr", HTMLUtil.getPagation("/user/userMessage",userMessageCount,currentPage,"目前没有任何消息！"));
        mav.setViewName("/user/userMessage");
        return mav;
    }

    /**
     * 进入我的主页
     */
    @GetMapping("/home")
    public ModelAndView home(HttpSession session){
        ModelAndView mav = new ModelAndView();
        User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
        //当前用户下载的资源列表
        Page<UserDownload> userDownloadPage = userDownloadService.listAll(currentUser.getUserId(),1,Consts.PAGE_SIZE, Sort.Direction.DESC,"downloadDate");
        if(userDownloadPage.getTotalElements()>0){
            mav.addObject("userDownloadList",userDownloadPage.getContent());
        }
        //当前用户的消息列表
        Page<Message> messagePage = messageService.list(currentUser.getUserId(),1,Consts.PAGE_SIZE, Sort.Direction.DESC,"publishDate");
        if(messagePage.getTotalElements()>0){
            mav.addObject("messageList",messagePage.getContent());
        }
        //分页代码
        mav.addObject("pageStr", HTMLUtil.getPagation("/user/userMessage",messagePage.getTotalPages(),1,"目前没有任何消息！"));
        mav.setViewName("/user/home");
        return mav;
    }

    /**
     * 上传头像
     */
    @ResponseBody
    @RequestMapping("/updateHeadPortrait")
    public Map<String,Object> updateHeadPortrait(MultipartFile file, HttpSession session) throws Exception {
        Map<String,Object> map = new HashMap<>();
        if(!file.isEmpty()){
            //获取文件名(带后缀)
            String fileName = file.getOriginalFilename();
            //获取文件的后缀名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            //新文件名
            String newFileName = DateUtil.getCurrentDateStr()+suffixName;
            FileUtils.copyInputStreamToFile(file.getInputStream(),new File(imgFilePath+newFileName));
            map.put("success",true);
            map.put("imgName",newFileName);
            //把头像放到session和数据库
            User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
            currentUser.setHeadPortrait(newFileName);
            userService.save(currentUser);
            session.setAttribute(Consts.CURRENT_USER,currentUser);
            List<Article> articles = articleService.findByUser(currentUser.getUserId());
            for(int i = 0;i<articles.size();i++){
                articleIndex.updateIndex(articles.get(i));
            }
        }
        return map;
    }

    /**
     * 用户中心
     */
    @GetMapping("/userCenter")
    public ModelAndView userCenter(HttpSession session){
        ModelAndView mav = new ModelAndView();
        this.unUsefulArticleCount(session);
        User currentUser = (User) session.getAttribute(Consts.CURRENT_USER);
        mav.addObject(Consts.CURRENT_USER,currentUser);
        mav.setViewName("/user/userCenter");
        return mav;
    }

    /**
     * 修改基本信息
     */
    @ResponseBody
    @RequestMapping("/userUpdate")
    public Map<String,Object> userUpdate(User user,HttpSession session){
        Map<String,Object> map = new HashMap<>();
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        currentUser.setNickname(user.getNickname());                //修改昵称
        currentUser.setSex(user.getSex());                          //修改性别
        userService.save(currentUser);
        session.setAttribute(Consts.CURRENT_USER,currentUser);      //添加到session
        List<Article> articles = articleService.findByUser(currentUser.getUserId());
        for(int i = 0;i<articles.size();i++){
            articleIndex.updateIndex(articles.get(i));
        }
        map.put("success",true);
        return map;
    }

    /**
     * 修改密码
     */
    @ResponseBody
    @RequestMapping("/updatePassword")
    public Map<String,Object> updatePassword(String oldPassword,String newPassword,HttpSession session){
        Map<String,Object> map = new HashMap<>();
        User currentUser = (User)session.getAttribute(Consts.CURRENT_USER);
        if (currentUser.getPassword().equals(CryptographyUtil.md5(oldPassword,CryptographyUtil.SALT))) {
            User oldUser = userService.findById(currentUser.getUserId());
            oldUser.setPassword(CryptographyUtil.md5(newPassword,CryptographyUtil.SALT));
            userService.save(oldUser);
            session.setAttribute(Consts.CURRENT_USER,oldUser);      //添加到session
            map.put("success",true);
        }else {
            map.put("errorInfo","原始密码有误！");
        }
        return map;
    }

}
