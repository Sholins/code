package com.xiaobai.code.service;

import com.xiaobai.code.entity.Link;
import org.springframework.data.domain.Sort.Direction;

import java.util.List;
import java.util.Map;

/**
 * 资源service层
 */
public interface LinkService {

    /**
     * 分页查询友情链接信息列表
     * @return
     */
    public List<Link> list(Integer page, Integer pageSize, Direction direction, String... properties);

    /**
     * 获取总记录数
     * @return
     */
    public  Long getCount();

    /**
     * 查询所有
     * @return
     */
    public List<Link> listAll(Direction direction,String... properties);

    /**
     * 添加或修改友情链接信息
     * @param link
     */
    public void save(Link link);

    /**
     * 根据id删除友情链接
     * @param linkId    友情链接id
     */
    public void delete(Integer linkId);

    /**
     * 根据id获取友情链接信息
     * @param linkId   友情链接id
     * @return
     */
    public Link getById(Integer linkId);


}
