package com.xiaobai.code.repository;

import com.xiaobai.code.entity.ArcType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


/**
 * 资源类型Repository
 */
public interface ArcTypeRespository extends JpaRepository<ArcType,Integer>, JpaSpecificationExecutor<ArcType> {

}
