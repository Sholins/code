package com.xiaobai.code.service.impl;

import com.xiaobai.code.entity.User;
import com.xiaobai.code.repository.UserRepository;
import com.xiaobai.code.run.StartupRunner;
import com.xiaobai.code.service.UserService;
import com.xiaobai.code.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * 用户serviec实现类
 */
@Service("UserService")
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private StartupRunner startupRunner;

    /**
     * 根据id获取用户信息
     */
    @Override
    public User findById(Integer id) {
        return userRepository.getOne(id);
    }

    /**
     * 根据用户名查找用户信息
     */
    @Override
    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    /**
     * 根据邮箱查找用户信息
     */
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 添加或修改用户信息
     */
    @Override
    public void save(User user) {
        userRepository.save(user);

    }

    /**
     *根据条件获取用户总数
     */
    @Override
    public Long getCount(User user, String s_blatelyLoginTime, String s_elatelyLoginTime) {
        return userRepository.count(new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if (StringUtil.isNotEmpty(s_blatelyLoginTime)) {
                    predicate.getExpressions().add(criteriaBuilder.greaterThanOrEqualTo(root.get("latelyLoginTime").as(String.class),s_blatelyLoginTime));
                }
                if (StringUtil.isNotEmpty(s_elatelyLoginTime)) {
                    predicate.getExpressions().add(criteriaBuilder.lessThanOrEqualTo(root.get("latelyLoginTime").as(String.class),s_elatelyLoginTime));
                }
                if (user != null) {
                    if(StringUtil.isNotEmpty(user.getSex())){
                        predicate.getExpressions().add(criteriaBuilder.equal(root.get("sex"),user.getSex()));
                    }
                    if(StringUtil.isNotEmpty(user.getUserName())){
                        predicate.getExpressions().add(criteriaBuilder.like(root.get("userName"),"%"+user.getUserName()+"%"));
                    }
                }
                return predicate;
            }
        });
    }

    /**
     * 今日用户注册数
     * @return
     */
    @Override
    public Integer todayRegister() {
        return userRepository.todayRegister();
    }

    /**
     * 今日用户登录数
     * @return
     */
    @Override
    public Integer todayLogin() {
        return userRepository.todayLogin();
    }

    /**
     * 根据分页条件查询用户列表
     */
    @Override
    public List<User> list(User user, String s_blatelyLoginTime, String s_elatelyLoginTime, Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        Page<User> userPage = userRepository.findAll(new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                if (StringUtil.isNotEmpty(s_blatelyLoginTime)) {
                    predicate.getExpressions().add(criteriaBuilder.greaterThanOrEqualTo(root.get("latelyLoginTime").as(String.class),s_blatelyLoginTime));
                }
                if (StringUtil.isNotEmpty(s_elatelyLoginTime)) {
                    predicate.getExpressions().add(criteriaBuilder.lessThanOrEqualTo(root.get("latelyLoginTime").as(String.class),s_elatelyLoginTime));
                }
                if (user != null) {
                    if(StringUtil.isNotEmpty(user.getSex())){
                        predicate.getExpressions().add(criteriaBuilder.equal(root.get("sex"),user.getSex()));
                    }
                    if(StringUtil.isNotEmpty(user.getUserName())){
                        predicate.getExpressions().add(criteriaBuilder.like(root.get("userName"),"%"+user.getUserName()+"%"));
                    }
                }
                return predicate;
            }
        }, PageRequest.of(page-1,pageSize,direction,properties));
        return userPage.getContent();
    }
}
