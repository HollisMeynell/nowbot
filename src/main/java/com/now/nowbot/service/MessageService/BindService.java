package com.now.nowbot.service.MessageService;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.now.nowbot.config.Permission;
import com.now.nowbot.dao.BindDao;
import com.now.nowbot.model.BinUser;
import com.now.nowbot.service.OsuGetService;
import com.now.nowbot.throwable.serviceException.BindException;
import com.now.nowbot.util.ASyncMessageUtil;
import com.now.nowbot.util.QQMsgUtil;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.options.MutableDataSet;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.At;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

@Service("bind")
public class BindService implements MessageService {
    public static final Map<Long, bind> BIND_MSG_MAP = new ConcurrentHashMap<>();
    private static Logger log = LoggerFactory.getLogger(BindService.class);
    OsuGetService osuGetService;
    BindDao bindDao;
    @Autowired
    public BindService(OsuGetService osuGetService, BindDao bindDao) {
        this.osuGetService = osuGetService;
        this.bindDao = bindDao;
    }

    @Override
    public void HandleMessage(MessageEvent event, Matcher matcher) throws Throwable {
        var from = event.getSubject();
        if (Permission.isSupper(event.getSender().getId())) {
            At at = QQMsgUtil.getType(event.getMessage(), At.class);
            if (matcher.group("un") != null && at != null) {
                unbin(at.getTarget());
            }
            if (at != null) {
                // 只有管理才有权力@人绑定,提示就不改了
                from.sendMessage("请发送绑定用户名");
                var lock = ASyncMessageUtil.getLock(from.getId(), event.getSender().getId());
                var s = ASyncMessageUtil.getEvent(lock);//阻塞,注意超时判空
                if (s != null) {
                    String Oname = s.getMessage().contentToString();
                    Long id;
                    try {
                        id = osuGetService.getOsuId(Oname);
                    } catch (Exception e) {
                        from.sendMessage("未找到osu用户"+Oname);
                        return;
                    }
                    try {
                        var buser = bindDao.getUserLiteFromOsuid(id);
                        if (buser.getQq() == null) {
                            from.sendMessage("正在为" + at.getTarget() + "绑定 >>(" + id + ")" + Oname);
                            buser.setQq(at.getTarget());
                            bindDao.update(buser);
                            from.sendMessage("绑定成功");
                        }else {
                            from.sendMessage(buser.getOsuName() + "已绑定在QQ " + at.getTarget() + " ,确定是否覆盖,回复'确定'生效");
                            s = ASyncMessageUtil.getEvent(lock);
                            if (s != null && s.getMessage().contentToString().startsWith("确定")) {
                                buser.setQq(at.getTarget());
                                bindDao.update(buser);
                                from.sendMessage("绑定成功");
                            }else {
                                from.sendMessage("已取消");
                            }
                        }
                    } catch (BindException e) {
                        from.sendMessage("正在为" + at.getTarget() + "绑定 >>(" + id + ")" + Oname);
                        bindDao.saveUser(at.getTarget(), Oname, id);
                        from.sendMessage("绑定成功");
                    }
                    return;
                } else {
                    from.sendMessage("超时或错误,结束接受");
                    return;
                }
            }
        }else if (matcher.group("un") != null){
            from.sendMessage("解绑请联系管理员");
            return;
        }
        var name = matcher.group("name");
        if (name != null){
            long d;
            try {
                 d = osuGetService.getOsuId(name);
            } catch (Exception e) {
                from.sendMessage("未找到osu用户"+name);
                return;
            }
            BinUser nuser = null;
            try {
                nuser = bindDao.getUser(event.getSender().getId());
            } catch (BindException e) {
                //未绑定
            }
            if (nuser != null){
                throw new BindException(BindException.Type.BIND_Client_AlreadyBound);
            }
            try {
                var buser = bindDao.getUserFromOsuid(d);
                from.sendMessage(name + " 已绑定 (" + buser.getQq() + ") ,若绑定错误,请联系管理员");
            } catch (BindException e) {
                bindDao.saveUser(event.getSender().getId(), name, d);
                from.sendMessage("正在为" + event.getSender().getId() + "绑定 >>(" + d + ")" + name);
            }
            return;
        }
        //将当前毫秒时间戳作为 key
        long timeMillis = System.currentTimeMillis();
        //群聊验证是否绑定
        if ((event instanceof GroupMessageEvent)) {
            BinUser user = null;
            try {
                user = bindDao.getUser(event.getSender().getId());
            } catch (BindException e) {
                //<<<<<<<<
//                return;
            }
            if (user == null || user.getAccessToken() == null){
                //未绑定或未完全绑定
            }else {
                from.sendMessage("您已绑定("+user.getOsuID()+")"+user.getOsuName()+",确认是否重新绑定,回复'ok'");
                var lock = ASyncMessageUtil.getLock(from.getId(), event.getSender().getId());
                var s = ASyncMessageUtil.getEvent(lock);
                if(s !=null && s.getMessage().contentToString().trim().equalsIgnoreCase("OK")){
                }else {
                    return;
                }
            }

            //---------------
            String state = event.getSender().getId() + "+" + timeMillis;
            //将消息回执作为 value
            state = osuGetService.getOauthUrl(state);
            var send = new At(event.getSender().getId()).plus(state);
            var receipt = from.sendMessage(send);
            //默认110秒后撤回
            receipt.recallIn(110 * 1000);
            //此处在 controller.msgController 处理
            BIND_MSG_MAP.put(timeMillis, new bind(timeMillis, receipt, event.getSender().getId()));
            //---------------
//            throw new BindException(BindException.Type.BIND_Client_AlreadyBound);
        }else {
            //私聊不验证是否绑定
            String state = event.getSender().getId() + "+" + timeMillis;
            var receipt = from.sendMessage(osuGetService.getOauthUrl(state));
            receipt.recallIn(110 * 1000);
            BIND_MSG_MAP.put(timeMillis, new bind(timeMillis, receipt, event.getSender().getId()));
        }
    }

    private void unbin(Long qqId) throws BindException {
        if (qqId == null) throw new BindException(BindException.Type.BIND_Me_NoBind);
        BinUser user = bindDao.getUser(qqId);
        if (user == null) {
            throw new BindException(BindException.Type.BIND_Me_NoBind);
        }

        if (bindDao.unBind(user)) {
            throw new BindException(BindException.Type.BIND_Client_RelieveBindSuccess);
        } else {
            throw new BindException(BindException.Type.BIND_Client_RelieveBindFailed);
        }
    }

    public record bind(Long key, MessageReceipt<Contact> receipt, Long qq) {
    }

    public static void main(String[] args) {
        String text = """
                name = "wiki"
                size = 56
                color=[255,255,255]
                                
                #注释 字体大小
                [[text]]
                color=[15,66,48] #没有就使用默认颜色
                styel=1     # 0无 1加粗 2斜体 3加粗斜体
                size=50     #此段大小 底端对齐 没有使用默认字号
                text="321\\n"
                [[text]]
                text="第二段\\n"
                """;


        var mp = new TomlMapper();
        try {
            var o = mp.readValue(text, p.class);
            System.out.println(o.name);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
// markdown to image
        MutableDataSet options = new MutableDataSet();
        options.setFrom(ParserEmulationProfile.MARKDOWN);
        options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Node document = parser.parse("""
                # 类型
                                
                ## 值类型
                                
                **有符号** sbyte short int long float double ***十进制浮点数*** decimal
                                
                **无符号** byte ushort uint ulong char *char默认代表UTF-16*""");
        String html = renderer.render(document);
        System.out.println(html);
    }

}
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class p {
    String name;
    Integer size;
    Integer[] color;
    List<d> text;
}
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class d{
    Integer[] color;
    Integer styel;
    Integer size;
    String text;
}