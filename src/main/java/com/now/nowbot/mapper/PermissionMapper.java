package com.now.nowbot.mapper;


import com.now.nowbot.config.Permission;
import com.now.nowbot.entity.PermissionLite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PermissionMapper extends JpaRepository<PermissionLite, Long> , JpaSpecificationExecutor<PermissionLite> {
    @Query("select p.id from PermissionLite p where p.service = :service and p.type = :type")
    public Integer getId(@Param("service") String service, @Param("type") Permission.TYPE type);

    public PermissionLite getByServiceAndType(String service, Permission.TYPE type);
}
