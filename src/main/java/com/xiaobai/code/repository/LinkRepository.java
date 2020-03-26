package com.xiaobai.code.repository;

import com.xiaobai.code.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 友情链接Repository
 */
public interface LinkRepository extends JpaRepository<Link,Integer>, JpaSpecificationExecutor<Link> {
}
