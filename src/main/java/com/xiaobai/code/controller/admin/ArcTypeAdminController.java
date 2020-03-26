package com.xiaobai.code.controller.admin;

import com.xiaobai.code.entity.ArcType;
import com.xiaobai.code.run.StartupRunner;
import com.xiaobai.code.service.ArcTypeService;
import com.xiaobai.code.util.Consts;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-资源类型controller
 */
@RestController
@RequestMapping("/admin/arcType")
public class ArcTypeAdminController {

    @Autowired
    private ArcTypeService arcTypeService;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Autowired
    private StartupRunner startupRunner;

    @RequestMapping("/list")
    public Map<String,Object> list(@RequestParam(value = "page",required = false)Integer page,@RequestParam(value = "pageSize",required = false)Integer pageSize){
        Map<String ,Object> map =new HashMap<>();
        int count = arcTypeService.getCount().intValue();
        if(page==null&&pageSize==null){
            page = 1;
            pageSize = count>0?count:1;
        }
        map.put("data",arcTypeService.list(page,pageSize, Sort.Direction.ASC,"sort"));
        map.put("total",count);
        map.put("errorNo",0);
        return map;
    }

    /**
     * 根据id查询资源类型实体
     */
    @RequestMapping("/findById")
    @RequiresPermissions(value = "根据id查询资源类型实体")
    public Map<String,Object> findById(Integer arcTypeId){
        Map<String ,Object> map =new HashMap<>();
        map.put("data",arcTypeService.getById(arcTypeId));
        map.put("errorNo",0);
        return map;
    }

    /**
     * 添加或修改资源类型信息
     */
    @RequestMapping("/save")
    @RequiresPermissions(value = "添加或修改资源类型信息")
    public Map<String,Object> save(ArcType arcType){
        Map<String ,Object> map =new HashMap<>();
        arcTypeService.save(arcType);
        map.put("errorNo",0);
        return map;
    }
    /**
     * 根据id批量删除资源类型信息
     */
    @RequestMapping("/delete")
    @RequiresPermissions(value = "删除资源类型信息")
    public Map<String,Object> delete(@RequestParam(value = "arcTypeId") String ids){
        Map<String ,Object> map =new HashMap<>();
        String[] idsStr = ids.split(",");
        for (int i=0;i<idsStr.length;i++){
            arcTypeService.delete(Integer.parseInt(idsStr[i]));
        }
        redisTemplate.delete(Consts.ALL_ARC_TYPE_NAME);
        startupRunner.loadDate();
        map.put("errorNo",0);
        return map;
    }
}
