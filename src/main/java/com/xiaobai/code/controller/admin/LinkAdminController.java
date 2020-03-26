package com.xiaobai.code.controller.admin;

import com.xiaobai.code.entity.Link;
import com.xiaobai.code.run.StartupRunner;
import com.xiaobai.code.service.LinkService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/admin/link")
public class LinkAdminController {

    @Autowired
    private LinkService linkService;

    @Autowired
    private StartupRunner startupRunner;

    @RequestMapping("/list")
    @RequiresPermissions("分页查询友情链接列表")
    public Map<String,Object> list(@RequestParam(value = "page",required = false)Integer page,
                                   @RequestParam(value = "pageSize",required = false)Integer pageSize){
        Map<String,Object> map = new HashMap<>();
        map.put("data",linkService.list(page,pageSize, Sort.Direction.DESC,"sort"));
        map.put("total",linkService.getCount());
        map.put("errorNo",0);
        return map;
    }

    @RequestMapping("/findById")
    @RequiresPermissions("根据linkId查询友情链接")
    public Map<String,Object> findById(Integer linkId){
        Map<String,Object> map = new HashMap<>();
        map.put("data",linkService.getById(linkId));
        map.put("errorNo",0);
        return map;
    }

    @RequestMapping("/save")
    @RequiresPermissions("添加或修改友情链接")
    public Map<String,Object> save(Link link){
        Map<String,Object> map = new HashMap<>();
        linkService.save(link);
        startupRunner.loadDate();
        map.put("errorNo",0);
        return map;
    }

    @RequestMapping(value = "/delete")
    @RequiresPermissions(value = "批量删除友情链接")
    public Map<String,Object> delete(@RequestParam(value = "linkId") String ids){
        Map<String ,Object> map =new HashMap<>();
        String[] idsStr = ids.split(",");
        for (int i=0;i<idsStr.length;i++){
            linkService.delete(Integer.parseInt(idsStr[i]));             //批量删除资源
        }
        startupRunner.loadDate();
        map.put("errorNo",0);
        return map;
    }


}
