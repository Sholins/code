package com.xiaobai.code.repository;

import com.xiaobai.code.entity.UserDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 用户资源下载repository层接口
 */
public interface UserDownloadRepository extends JpaRepository<UserDownload,Integer>, JpaSpecificationExecutor<UserDownload> {
    /**
     * 查询某个用户下载某个资源的次数
      */
    @Query(value = "select count(*) from user_download where user_id=?1 and article_id=?2",nativeQuery = true)
    public Integer getCountByUidAndByAid(Integer userId,Integer articleId);
}
