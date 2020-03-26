package com.xiaobai.code.controller.admin;

import com.xiaobai.code.entity.User;
import com.xiaobai.code.service.UserService;
import com.xiaobai.code.util.Consts;
import com.xiaobai.code.util.CryptographyUtil;
import com.xiaobai.code.util.StringUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员--用户管理
 */
@Controller
@RequestMapping("/admin/user")
public class UserAdminController {

    @Autowired
    private UserService userService;

    @ResponseBody
    @RequestMapping(value = "/list")
    @RequiresPermissions("分页查询用户信息列表")
    public Map<String,Object> list(User user, @RequestParam(value = "latelyLoginTimes",required = false)String latelyLoginTimes,
                                   @RequestParam(value ="page",required = false)Integer page,
                                   @RequestParam(value ="pageSize",required = false)Integer pageSize){
        Map<String ,Object> map =new HashMap<>();
        String s_blatelyLoginTime = null;                               //开始时间
        String s_elatelyLoginTime = null;                               //结束时间
        if(StringUtil.isNotEmpty(latelyLoginTimes)){
            String[] str = latelyLoginTimes.split(" - ");        //拆分时间段
            s_blatelyLoginTime = str[0];
            s_elatelyLoginTime = str[1];
        }
        map.put("data",userService.list(user,s_blatelyLoginTime,s_elatelyLoginTime,page,pageSize, Sort.Direction.DESC,"registrationDate"));
        map.put("total",userService.getCount(user,s_blatelyLoginTime,s_elatelyLoginTime));
        map.put("errorNo",0);
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/updateVipState")
    @RequiresPermissions("修改用户vip状态")
    public Map<String,Object> updateVipState(Integer userId,Boolean isVip){
        Map<String ,Object> map =new HashMap<>();
        User oldUser = userService.findById(userId);
        oldUser.setVip(isVip);
        userService.save(oldUser);
        map.put("success",true);
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/updateUserState")
    @RequiresPermissions("修改用户封禁状态")
    public Map<String,Object> updateUserState(Integer userId,Boolean isOff){
        Map<String ,Object> map =new HashMap<>();
        User oldUser = userService.findById(userId);
        oldUser.setOff(isOff);
        userService.save(oldUser);
        map.put("success",true);
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/resetPassword")
    @RequiresPermissions("重置用户密码")
    public Map<String,Object> resetPassword(Integer userId ){
        Map<String ,Object> map =new HashMap<>();
        User oldUser = userService.findById(userId);
        oldUser.setPassword(CryptographyUtil.md5(Consts.PASSWORD,CryptographyUtil.SALT));
        userService.save(oldUser);
        map.put("errorNo",0);
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/updateVipGrade")
    @RequiresPermissions("修改用户vip等级")
    public Map<String,Object> updateVipGrade(User user ){
        Map<String ,Object> map =new HashMap<>();
        User oldUser = userService.findById(user.getUserId());
        oldUser.setVipGrade(user.getVipGrade());
        userService.save(oldUser);
        map.put("errorNo",0);
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/updatePoints")
    @RequiresPermissions("充值积分")
    public Map<String,Object> updatePoints(User user ){
        Map<String ,Object> map =new HashMap<>();
        User oldUser = userService.findById(user.getUserId());
        oldUser.setPoints(oldUser.getPoints()+user.getPoints());
        userService.save(oldUser);
        map.put("errorNo",0);
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/modifyPassword")
    @RequiresPermissions("管理员修改登录密码")
    public Map<String,Object> modifyPassword(String oldPassword, String newPassword, HttpSession session){
        User user = (User)session.getAttribute(Consts.CURRENT_USER);
        Map<String,Object> map = new HashMap<>();
        if(!user.getPassword().equals(CryptographyUtil.md5(oldPassword,CryptographyUtil.SALT))){
            map.put("success",false);
            map.put("errorInfo","原始密码有误");
            return map;
        }
        User oldUser = userService.findById(user.getUserId());
        oldUser.setPassword(CryptographyUtil.md5(newPassword,CryptographyUtil.SALT));
        userService.save(oldUser);
        map.put("success",true);
        return map;
    }

    @RequestMapping(value = "/logout")
    @RequiresPermissions("安全退出")
    public String logout(){
        SecurityUtils.getSubject().logout();

        return "redirect:/admin/login.html";
    }
}
