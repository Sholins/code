package com.xiaobai.code.service.impl;

import com.xiaobai.code.entity.Article;
import com.xiaobai.code.entity.Comment;
import com.xiaobai.code.repository.CommentRepository;
import com.xiaobai.code.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

/**
 * 评论CommentServiceImpl实现类
 */
@Service("CommentService")
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    /**
     * 添加评论
     * @param comment
     */
    @Override
    public void save(Comment comment) {
        commentRepository.save(comment);
    }

    /**
     * 分页查询评论列表
     * @param s_comment
     * @param page
     * @param pageSize
     * @param direction
     * @param properties
     * @return
     */
    @Override
    public Page<Comment> list(Comment s_comment, Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        return commentRepository.findAll(new Specification<Comment>() {
            @Override
            public Predicate toPredicate(Root<Comment> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(s_comment!=null){
                    if (s_comment.getState() != null) {                 //审核状态
                        predicate.getExpressions().add(criteriaBuilder.equal(root.get("state"),s_comment.getState()));
                    }
                    if(s_comment.getArticle()!=null&&s_comment.getArticle().getArticleId()!=null){      //所属资源
                        predicate.getExpressions().add(criteriaBuilder.equal(root.get("article").get("articleId"),s_comment.getArticle().getArticleId()));
                    }
                }
                return predicate;
            }
        }, PageRequest.of(page-1,pageSize,direction,properties));
    }

    /**
     * 获取评论总数
     * @param s_comment
     * @return
     */
    @Override
    public Long getCount(Comment s_comment) {
        return commentRepository.count(new Specification<Comment>() {
            @Override
            public Predicate toPredicate(Root<Comment> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(s_comment!=null){
                    if (s_comment.getState() != null) {                 //审核状态
                        predicate.getExpressions().add(criteriaBuilder.equal(root.get("state"),s_comment.getState()));
                    }
                    if(s_comment.getArticle()!=null&&s_comment.getArticle().getArticleId()!=null){      //所属资源
                        predicate.getExpressions().add(criteriaBuilder.equal(root.get("article").get("articleId"),s_comment.getArticle().getArticleId()));
                    }
                }
                return predicate;
            }
        });
    }

    /**
     * 根据评论id查询评论信息
     * @param id    commentId
     * @return
     */
    @Override
    public Comment getById(Integer id) {
        return commentRepository.getOne(id);
    }

    /**
     * 根据id删除评论信息
     * @param id    commentId
     */
    @Override
    public void delete(Integer id) {
        commentRepository.deleteById(id);
    }

    /**
     * 根据资源id删除相应的评论
     */
    @Override
    public void deleteCommentByArticleId(Integer articleId) {
        commentRepository.deleteCommentByArticleId(articleId);
    }

}
