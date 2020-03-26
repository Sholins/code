package com.xiaobai.code.service.impl;

import com.xiaobai.code.entity.Message;
import com.xiaobai.code.repository.MessageRespository;
import com.xiaobai.code.run.StartupRunner;
import com.xiaobai.code.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

@Transactional
@Service("messageService")
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRespository messageRespository;

    @Autowired
    @Lazy
    private StartupRunner startupRunner;

    @Override
    public Page<Message> list(Integer userId, Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        return messageRespository.findAll(new Specification<Message>() {
            @Override
            public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(userId!=null){
                    predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"),userId));
                }
                return predicate;
            }
        }, PageRequest.of(page-1,pageSize, Sort.Direction.DESC,"publishDate"));
    }

    @Override
    public Long getCount(Integer userId) {
        return messageRespository.count(new Specification<Message>() {
            @Override
            public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(userId!=null){
                    predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"),userId));
                }
                return predicate;
            }
        });
    }

    @Override
    public void save(Message message) {
        messageRespository.save(message);
        startupRunner.loadDate();               //刷新缓存
    }

    /**
     * 查询用户下所有未查看的消息数
     */
    @Override
    public Integer getMessageCount(Integer userId) {
        return messageRespository.getMessageCount(userId);
    }

    /**
     * 查看消息,修改查看状态
     */
    @Override
    public Integer updateState(Integer userId) {
        return messageRespository.updateState(userId);
    }
}
