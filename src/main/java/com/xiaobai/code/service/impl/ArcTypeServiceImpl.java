package com.xiaobai.code.service.impl;

import com.xiaobai.code.entity.ArcType;
import com.xiaobai.code.repository.ArcTypeRespository;
import com.xiaobai.code.run.StartupRunner;
import com.xiaobai.code.service.ArcTypeService;
import com.xiaobai.code.util.Consts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 资源类型ArcTypeService实现类
 */
@Service("arcTypeService")
public class ArcTypeServiceImpl implements ArcTypeService {

    @Autowired
    private ArcTypeRespository arcTypeRespository;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Autowired
    private StartupRunner startupRunner;

    @Override
    public List<ArcType> list(Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        Page<ArcType> arcTypePage = arcTypeRespository.findAll(PageRequest.of(page-1,pageSize,direction,properties));
        return arcTypePage.getContent();
    }

    @Override
    public List listAll(Sort.Direction direction, String... properties) {
        if(redisTemplate.hasKey(Consts.ALL_ARC_TYPE_NAME)){
            return redisTemplate.opsForList().range(Consts.ALL_ARC_TYPE_NAME,0,-1);
        }else{
            List list = arcTypeRespository.findAll(Sort.by(direction,properties));
            if (list != null && list.size() > 0) {
                for (int i=0;i<list.size();i++){
                    redisTemplate.opsForList().rightPush(Consts.ALL_ARC_TYPE_NAME,list.get(i));
                }
            }
            return list;
        }
    }

    @Override
    public Long getCount() {
        return arcTypeRespository.count();
    }

    @Override
    public void save(ArcType arcType) {
        boolean flag = false;
        if(arcType.getArcTypeId()==null){
            flag = true;
        }
        arcTypeRespository.save(arcType);
        if(flag){               //新增类型
            redisTemplate.opsForList().rightPush(Consts.ALL_ARC_TYPE_NAME,arcType);
        }else{                  //修改类型
            redisTemplate.delete(Consts.ALL_ARC_TYPE_NAME);
        }
        startupRunner.loadDate();
    }

    @Override
    public void delete(Integer id) {
        //todo 根据资源类型id删除资源
        arcTypeRespository.deleteById(id);
    }

    @Override
    public ArcType getById(Integer id) {
        return arcTypeRespository.getOne(id);
    }
}
