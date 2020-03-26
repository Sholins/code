package com.xiaobai.code.service.impl;

import com.xiaobai.code.entity.UserDownload;
import com.xiaobai.code.repository.UserDownloadRepository;
import com.xiaobai.code.service.UserDownloadService;
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

/**
 * 用户资源下载service实现类
 */
@Service("userDownloadService")
public class UserDownloadServiceImpl implements UserDownloadService {

    @Autowired
    private UserDownloadRepository userDownloadRepository;

    @Override
    public Integer getCountByUidAndByAid(Integer userId, Integer articleId) {
        return userDownloadRepository.getCountByUidAndByAid(userId,articleId);
    }

    @Override
    public Page<UserDownload> listAll(Integer userId, Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        return userDownloadRepository.findAll(new Specification<UserDownload>() {
            @Override
            public Predicate toPredicate(Root<UserDownload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(userId!=null){
                    predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"),userId));
                }
                return predicate;
            }
        }, PageRequest.of(page-1,pageSize,direction,properties));
    }

    @Override
    public Long getCount(Integer userId) {
        return userDownloadRepository.count(new Specification<UserDownload>() {
            @Override
            public Predicate toPredicate(Root<UserDownload> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if(userId!=null){
                    predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"),userId));
                }
                return predicate;
            }
        });
    }

    @Override
    public void save(UserDownload userDownload) {
        userDownloadRepository.save(userDownload);
    }
}
