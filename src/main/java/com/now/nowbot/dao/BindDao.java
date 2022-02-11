package com.now.nowbot.dao;

import com.now.nowbot.entity.OsuBindUserLite;
import com.now.nowbot.entity.OsuNameToIdLite;
import com.now.nowbot.mapper.BindMapper;
import com.now.nowbot.mapper.OsuFindNameMapper;
import com.now.nowbot.model.BinUser;
import com.now.nowbot.model.enums.OsuMode;
import com.now.nowbot.throwable.TipsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BindDao {
    Logger log = LoggerFactory.getLogger(BindDao.class);
    BindMapper bindMapper;
    OsuFindNameMapper osuFindNameMapper;

    @Autowired
    public BindDao(BindMapper mapper, OsuFindNameMapper nameMapper) {
        bindMapper = mapper;
        osuFindNameMapper = nameMapper;
    }

    public BinUser getUser(Long qq) throws TipsException {
        var liteData = bindMapper.getByQq(qq);
        if (liteData == null) throw new TipsException("当前用户未绑定");
        return fromLite(liteData);
    }

    public BinUser getBindUser(String name) {
        var id = getOsuId(name);
        if (id == null) return null;
        var data = bindMapper.getByOsuId(id);
        return fromLite(data);
    }

    public void saveUser(BinUser user) {
        var data = new OsuBindUserLite(user);
        if (data.getMainMode() == null) data.setMainMode(OsuMode.OSU);
        bindMapper.save(data);
    }

    public void updateToken(Long uid, String accessToken, String refreshToken, Long time) {
        bindMapper.updateToken(uid, accessToken, refreshToken, time);
    }

    public void updateMod(Long uid, OsuMode mode) {
        bindMapper.updateMode(uid, mode);
    }

    public boolean unBind(BinUser user) {
        try {
            bindMapper.unBind(user.getOsuID());
            return true;
        } catch (Exception e) {
            log.error("unbind error", e);
            return false;
        }
    }

    public Long getQQ(Long uid) {
        return bindMapper.getqq(uid);
    }

    //todo 一会切换到name表
    public Long getOsuId(String name) {
        var uid = osuFindNameMapper.getFirstByNameOrderByIndex(name.toUpperCase()).getUid();
        return uid;
    }


    public void removeOsuNameToId(Long osuId) {
        osuFindNameMapper.deleteByUid(osuId);
    }

    public void saveOsuNameToId(Long id, String... name) {
        if (name.length == 0) return;
        for (int i = 0; i < name.length; i++) {
            var x = new OsuNameToIdLite(id, name[i], i);
            osuFindNameMapper.save(x);
        }
    }

    public static BinUser fromLite(OsuBindUserLite osuBindUserLite) {
        if (osuBindUserLite == null) return null;
        var data = osuBindUserLite;
        var buser = new BinUser();
        buser.setQq(data.getQq());
        buser.setOsuID(data.getOsuId());
        buser.setOsuName(data.getOsuName());
        buser.setAccessToken(data.getAccessToken());
        buser.setRefreshToken(data.getRefreshToken());
        buser.setTime(data.getTime());
        buser.setMode(data.getMainMode());
        return buser;
    }

    public static OsuBindUserLite fromModel(BinUser user) {
        if (user == null) return null;
        return new OsuBindUserLite(user);
    }
}