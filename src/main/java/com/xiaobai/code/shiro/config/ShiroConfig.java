package com.xiaobai.code.shiro.config;

import com.xiaobai.code.shiro.realm.MyRealm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * shiro配置
 */

@Configuration
public class ShiroConfig {

    @Bean
    public ShiroFilterFactoryBean shiroFilter(DefaultWebSecurityManager securityManager){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        //必须设置securityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //如果不设置，默认会自动寻找工程根目录下的login.jsp页面
        shiroFilterFactoryBean.setLoginUrl("/");

        //拦截器
        Map<String,String> filterChainDefinitionMap = new LinkedHashMap<>();
        //过滤链的定义，从上往下执行，一般将/**放在最下面
        //authc 所有的url必须认证通过才能访问  anon所有的url可以匿名访问
        /**
         * 配置不会拦截的链接，顺序判断
         */
        filterChainDefinitionMap.put("/","anon");
        filterChainDefinitionMap.put("/static/**","anon");
        filterChainDefinitionMap.put("/ueditor/**","anon");
        filterChainDefinitionMap.put("/upload/**","anon");
        filterChainDefinitionMap.put("/comment/**","anon");
        filterChainDefinitionMap.put("/user/register.html","anon");
        filterChainDefinitionMap.put("/user/login.html","anon");
        filterChainDefinitionMap.put("/user/findPassword.html","anon");
        filterChainDefinitionMap.put("/user/register","anon");
        filterChainDefinitionMap.put("/user/login","anon");
        filterChainDefinitionMap.put("/user/sendEmail","anon");
        filterChainDefinitionMap.put("/user/checkYzm","anon");

        //购买VIP
        filterChainDefinitionMap.put("/buyVIP","anon");
        //赚积分
        filterChainDefinitionMap.put("/zjf","anon");

        filterChainDefinitionMap.put("/article/**","anon");

        //管理员后台登陆
        filterChainDefinitionMap.put("/admin/login.html","anon");

        //配置退出拦截器，具体的突出代码shiro已经实现
        filterChainDefinitionMap.put("/user/logout","logout");

        filterChainDefinitionMap.put("/**","authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }
    @Bean
    public DefaultWebSecurityManager securityManager(){
        DefaultWebSecurityManager securityManager =new DefaultWebSecurityManager();
        //设置realm
        securityManager.setRealm(myRealm());
        return securityManager;
    }

    /**
     * 身份认证
     * @return
     */
    @Bean
    public MyRealm myRealm(){
        MyRealm myRealm = new MyRealm();
        return myRealm;
    }
    /**
     * shiro生命周期处理器
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return  new LifecycleBeanPostProcessor();
    }
    /**
     * 开启shiro的注解，借助SpringAOP扫描使用shiro注解的类，并在必要的时候进行安全逻辑验证
     * 配置DefaultAdvisorAutoProxyCreator，AuthorizationAttributeSourceAdvisor实现
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(){
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor =new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
        return authorizationAttributeSourceAdvisor;
    }
}
