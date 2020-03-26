package com.xiaobai.code.shiro.realm;


import com.xiaobai.code.entity.User;
import com.xiaobai.code.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

/**
 * 自定义realm
 */

public class MyRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    /**
     * 授权
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        String userName = (String) SecurityUtils.getSubject().getPrincipal();
        User user = userService.findByUserName(userName);
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        Set<String> roles = new HashSet<>();
        if("管理员".equals(user.getRoleName())){
            roles.add("管理员");
            authorizationInfo.addStringPermission("进入管理员页面");
            authorizationInfo.addStringPermission("根据id查询资源类型实体");
            authorizationInfo.addStringPermission("添加或修改资源类型信息");
            authorizationInfo.addStringPermission("删除资源类型信息");
            authorizationInfo.addStringPermission("分页查询资源信息列表");
            authorizationInfo.addStringPermission("删除资源信息");
            authorizationInfo.addStringPermission("查看资源信息");
            authorizationInfo.addStringPermission("审核资源");
            authorizationInfo.addStringPermission("修改是否热门资源");
            authorizationInfo.addStringPermission("修改是否免费资源");
            authorizationInfo.addStringPermission("生成所有资源索引");
            authorizationInfo.addStringPermission("分页查询评论信息");
            authorizationInfo.addStringPermission("分页查询用户信息列表");
            authorizationInfo.addStringPermission("查看资源信息");
            authorizationInfo.addStringPermission("查看评论信息");
            authorizationInfo.addStringPermission("删除评论信息");
            authorizationInfo.addStringPermission("审核评论");
            authorizationInfo.addStringPermission("修改用户vip状态");
            authorizationInfo.addStringPermission("修改用户vip等级");
            authorizationInfo.addStringPermission("修改用户封禁状态");
            authorizationInfo.addStringPermission("重置用户密码");
            authorizationInfo.addStringPermission("充值积分");
            authorizationInfo.addStringPermission("管理员修改登录密码");
            authorizationInfo.addStringPermission("安全退出");
            authorizationInfo.addStringPermission("分页查询友情链接列表");
            authorizationInfo.addStringPermission("根据linkId查询友情链接");
            authorizationInfo.addStringPermission("添加或修改友情链接");
            authorizationInfo.addStringPermission("批量删除友情链接");

        }
        authorizationInfo.setRoles(roles);
        return authorizationInfo ;
    }

    /**
     * 权限认证
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String userName = (String) authenticationToken.getPrincipal();
        User user = userService.findByUserName(userName);
        if(user==null){
            return null;
        }else{
            AuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(user.getUserName(),user.getPassword(),"xx");
            return authenticationInfo;
        }
    }
}
