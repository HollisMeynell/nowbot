package com.now.nowbot.dao;

import com.now.nowbot.config.PermissionType;
import com.now.nowbot.entity.PermissionLite;
import com.now.nowbot.entity.QQID;
import com.now.nowbot.mapper.PermissionMapper;
import com.now.nowbot.mapper.QQIDMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PermissionDao {
    @Autowired
    PermissionMapper permMapper;
    @Autowired
    QQIDMapper qqMapper;

    public List<Long> getQQList(String service, PermissionType type){
        var perm = permMapper.getByServiceAndType(service, type);
        if(perm == null){
            perm = permMapper.save(new PermissionLite(service, type));
        }
        return qqMapper.getByPermissionId(perm.getId());
    }
    public void addGroup(String service, PermissionType type, Long id){
        Long pid = permMapper.getId(service, type);
        var data = new QQID();
        data.setGroup(true);
        data.setPermissionId(pid);
        data.setQQ(id);
        qqMapper.saveAndFlush(data);
    }
    public void delGroup(String service, PermissionType type, Long id){
        Long pid = permMapper.getId(service, type);
        qqMapper.deleteQQIDByPermissionIdAndIsGroupAndQQ(pid,true,id);
    }
    public void addFriend(String service, PermissionType type, Long id){
        Long pid = permMapper.getId(service, type);
        var data = new QQID();
        data.setGroup(false);
        data.setPermissionId(pid);
        data.setQQ(id);
        qqMapper.saveAndFlush(data);
    }
    public void delFriend(String service, PermissionType type, Long id){
        Long pid = permMapper.getId(service, type);
        qqMapper.deleteQQIDByPermissionIdAndIsGroupAndQQ(pid,false,id);
    }
}
