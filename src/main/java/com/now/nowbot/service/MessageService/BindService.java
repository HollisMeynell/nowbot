package com.now.nowbot.service.MessageService;

import com.now.nowbot.config.Permission;
import com.now.nowbot.model.BinUser;
import com.now.nowbot.service.OsuGetService;
import com.now.nowbot.util.ASyncMessageUtil;
import com.now.nowbot.util.BindingUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.At;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

@Service("bind")
public class BindService implements MessageService {
    public static final Map<Long, MessageReceipt> BIND_MSG_MAP = new ConcurrentHashMap<>();
    @Autowired
    OsuGetService osuGetService;

    @Override
    public void HandleMessage(MessageEvent event, Matcher matcher) throws Throwable{

        if (Permission.isSupper(event.getSender().getId())){
            At at = (At) event.getMessage().stream().filter(it -> it instanceof At).findFirst().orElse(null);
            if (matcher.group("un") != null){
                var user = BindingUtil.readUser(at.getTarget());
                if (BindingUtil.unBind(user)){
                    event.getSubject().sendMessage("解除成功");
                }else {
                    event.getSubject().sendMessage("解除失败");
                }
            }
            if (at != null) {
                event.getSubject().sendMessage("请发送绑定用户名");
                var lock = ASyncMessageUtil.getLock(event.getSubject().getId(), event.getSender().getId());
                var s = ASyncMessageUtil.getEvent(lock);//阻塞,注意超时判空
                if (s != null) {
                    event.getSubject().sendMessage("正在为" + at.getTarget() + "绑定 >>" + s.getMessage().contentToString());
                }else {
                    event.getSubject().sendMessage("超时或错误,结束接受");
                }
                return;
            }
        }
        //将当前毫秒时间戳作为 key
        long d = System.currentTimeMillis();
        //群聊验证是否绑定
        if ((event.getSubject() instanceof Group)) {
            BinUser user = null;
            try {
                user = BindingUtil.readUser(event.getSender().getId());
            } catch (Exception e) {//未绑定时会出现file not find
                String state = event.getSender().getId() + "+" + d;
                //将消息回执作为 value
                var ra = event.getSubject().sendMessage(new At(event.getSender().getId()).plus(osuGetService.getOauthUrl(state)));
                //默认110秒后撤回
                ra.recallIn(110 * 1000);
                //此处在 controller.msgController 处理
                BIND_MSG_MAP.put(d, ra);
                return;
            }
            event.getSubject().sendMessage(new At(event.getSender().getId()).plus("您已绑定，若要修改绑定请私发bot绑定命令"));
            return;
        }
        //私聊不验证是否绑定
        String state = event.getSender().getId() + "+" + d;
        var e = event.getSubject().sendMessage(osuGetService.getOauthUrl(state));
        e.recallIn(110 * 1000);
        BIND_MSG_MAP.put(d, e);
        return;
    }
}
