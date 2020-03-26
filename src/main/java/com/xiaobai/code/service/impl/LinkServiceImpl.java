package com.xiaobai.code.service.impl;

import com.xiaobai.code.entity.Article;
import com.xiaobai.code.entity.Link;
import com.xiaobai.code.repository.LinkRepository;
import com.xiaobai.code.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("LinkService")
public class LinkServiceImpl implements LinkService {

    @Autowired
    private LinkRepository linkRepository;

    @Override
    public List<Link> list(Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        return linkRepository.findAll(PageRequest.of(page-1,pageSize,direction,properties)).getContent();
    }

    @Override
    public Long getCount() {
        return linkRepository.count();
    }

    @Override
    public List<Link> listAll(Sort.Direction direction, String... properties) {
        Sort sort = Sort.by(direction,properties);
        return linkRepository.findAll();
    }

    @Override
    public void save(Link link) {
        linkRepository.save(link);
    }

    @Override
    public void delete(Integer linkId) {
        linkRepository.deleteById(linkId);
    }

    @Override
    public Link getById(Integer linkId) {
        return linkRepository.getOne(linkId);
    }
}
