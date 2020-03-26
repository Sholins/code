package com.xiaobai.code.controller.admin;

import com.xiaobai.code.entity.Article;
import com.xiaobai.code.entity.Comment;
import com.xiaobai.code.entity.Message;
import com.xiaobai.code.service.CommentService;
import com.xiaobai.code.service.MessageService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员——评论管理
 */
@RestController
@RequestMapping(value = "/admin/comment")
public class CommentAdminController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private MessageService messageService;

    /**
     * 根据条件分页查询评论信息
     */
    @RequestMapping(value = "/list")
    @RequiresPermissions(value = "分页查询评论信息")
    public Map<String,Object> list(Comment s_comment, @RequestParam(value = "page",required = false)Integer page,
                                   @RequestParam(value = "pageSize",required = false)Integer pageSize){
        Map<String ,Object> map = new HashMap<>();
        map.put("data",commentService.list(s_comment,page,pageSize, Sort.Direction.DESC,"commentDate").getContent());
        map.put("total",commentService.getCount(s_comment));
        map.put("errorNo",0);
        return map;
    }
    /**
     * 根据id查看评论信息
     */
    @RequestMapping(value = "/findById")
    @RequiresPermissions(value = "查看评论信息")
    public Map<String,Object> findById(@RequestParam(value = "commentId") Integer commentId) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        Comment comment = commentService.getById(commentId);
        tempMap.put("name",comment.getArticle().getName());
        tempMap.put("content",comment.getContent());
        tempMap.put("description",comment.getArticle().getDescription());
        map.put("data",tempMap);
        map.put("errorNo",0);
        return map;
    }

    /**
     * 根据id批量删除评论信息
     */
    @RequestMapping(value = "/delete")
    @RequiresPermissions(value = "删除评论信息")
    public Map<String,Object> delete(@RequestParam(value = "commentId") String ids){
        Map<String ,Object> map =new HashMap<>();
        String[] idsStr = ids.split(",");
        for (int i=0;i<idsStr.length;i++){
            //todo 删除评论
            commentService.delete(Integer.parseInt(idsStr[i]));             //批量删除评论
        }
        map.put("errorNo",0);
        return map;
    }

    /**
     * 审核评论
     */
    @RequestMapping(value = "/updateState")
    @RequiresPermissions(value = "审核评论")
    public Map<String,Object> updateState(Comment comment){
        Map<String ,Object> map =new HashMap<>();
        Comment oldComment = commentService.getById(comment.getCommentId());
        Message message = new Message();
        message.setUser(oldComment.getUser());
        message.setPublishDate(new Date());
        if(comment.getState() == 1)
        {
            message.setContent("【<font color='green'>审核成功</font>】您对资源【<font color='blue'>"+oldComment.getArticle().getName()+"</font>】的评论审核成功！");
            oldComment.setState(1);
        }
        if(comment.getState() == 2)
        {
            message.setCause("");
            message.setContent("【<font color='red'>审核失败</font>】您对资源【<font color='blue'>"+oldComment.getArticle().getName()+"</font>】的评论审核未通过！");
            oldComment.setState(2);
        }
        messageService.save(message);
        commentService.save(oldComment);
        map.put("errorNo",0);
        return map;
    }

}
