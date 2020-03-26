package com.xiaobai.code.service;

import com.xiaobai.code.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import javax.persistence.criteria.CriteriaBuilder;

/**
 * 用户消息Service
 */
public interface MessageService {

    /**
     * 根据userId分页查询消息
     */
    public Page<Message> list(Integer userId, Integer page, Integer pageSize, Sort.Direction direction, String...properties);

    /**
     *根据userId查询消息数
     */
    public  Long getCount(Integer userId);

    /**
     * 添加或修改消息
     */
    public void save(Message message);

    /**
     * 查询用户下所有未查看的消息数
     */
    public Integer getMessageCount(Integer userId);

    /**
     * 查看消息,修改查看状态
     */
    public Integer updateState(Integer userId);

}
