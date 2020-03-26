package com.xiaobai.code.repository;

import com.xiaobai.code.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * 消息Respository
 */
public interface MessageRespository extends JpaRepository<Message,Integer>, JpaSpecificationExecutor<Message> {

    /**
     * 查询用户下所有未查看的消息数
     */
    @Query(value = "select count(*) from message where is_see=false and user_id = ?1",nativeQuery = true)
    public Integer getMessageCount(Integer userId);

    /**
     * 查看消息,修改查看状态
     */
    @Query(value = "update message set is_see = true where user_id = ?1",nativeQuery = true)
    @Modifying
    public Integer updateState(Integer userId);
}
