package com.xiaobai.code.repository;

import com.xiaobai.code.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 评论CommentRepository接口
 */

public interface CommentRepository extends JpaSpecificationExecutor<Comment>, JpaRepository<Comment,Integer> {

    /**
     * 删除指定资源的所有评论
     */
    @Query(value = "delete from comment where article_id=?1",nativeQuery = true)
    @Modifying
    public void deleteCommentByArticleId(Integer articleId);
}
