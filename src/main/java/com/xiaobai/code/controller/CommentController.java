package com.xiaobai.code.controller;

import com.xiaobai.code.entity.Comment;
import com.xiaobai.code.entity.User;
import com.xiaobai.code.service.CommentService;
import com.xiaobai.code.util.Consts;
import com.xiaobai.code.util.HTMLUtil;
import com.xiaobai.code.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 评论管理
 */
@Controller
@RequestMapping(value = "/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    /**
     * 评论提交保存
     * @param comment
     * @param session
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/add")
    public Map<String,Object> addComment(Comment comment, HttpSession session){
        Map<String,Object> map = new HashMap<>();
        comment.setContent(StringUtil.esc(comment.getContent()));
        comment.setCommentDate(new Date());
        comment.setState(0);
        comment.setUser((User) session.getAttribute(Consts.CURRENT_USER));
        commentService.save(comment);
        map.put("success",true);
        return map;
    }

    /**
     * 查询评论列表（前端页面显示）
     * @param comment
     * @param page
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/list")
    public Map<String,Object> list(Comment comment, @RequestParam(value = "page",required = false)Integer page) {
        comment.setState(1);
        Page<Comment> commentPage = commentService.list(comment,page,5, Sort.Direction.DESC,"commentDate");
        Map<String, Object> map = new HashMap<>();
        map.put("data", HTMLUtil.getCommentPageStr(commentPage.getContent()));      //评论的html代码
        map.put("total",commentPage.getTotalPages());                               //评论总页数
        return map;

    }
}
