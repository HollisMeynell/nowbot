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
                // ????????????????????????@?????????,??????????????????
                from.sendMessage("????????????????????????");
                var lock = ASyncMessageUtil.getLock(from.getId(), event.getSender().getId());
                var s = ASyncMessageUtil.getEvent(lock);//??????,??????????????????
                if (s != null) {
                    String Oname = s.getMessage().contentToString();
                    Long id;
                    try {
                        id = osuGetService.getOsuId(Oname);
                    } catch (Exception e) {
                        from.sendMessage("?????????osu??????"+Oname);
                        return;
                    }
                    try {
                        var buser = bindDao.getUserLiteFromOsuid(id);
                        if (buser.getQq() == null) {
                            from.sendMessage("?????????" + at.getTarget() + "?????? >>(" + id + ")" + Oname);
                            buser.setQq(at.getTarget());
                            bindDao.update(buser);
                            from.sendMessage("????????????");
                        }else {
                            from.sendMessage(buser.getOsuName() + "????????????QQ " + at.getTarget() + " ,??????????????????,??????'??????'??????");
                            s = ASyncMessageUtil.getEvent(lock);
                            if (s != null && s.getMessage().contentToString().startsWith("??????")) {
                                buser.setQq(at.getTarget());
                                bindDao.update(buser);
                                from.sendMessage("????????????");
                            }else {
                                from.sendMessage("?????????");
                            }
                        }
                    } catch (BindException e) {
                        from.sendMessage("?????????" + at.getTarget() + "?????? >>(" + id + ")" + Oname);
                        bindDao.saveUser(at.getTarget(), Oname, id);
                        from.sendMessage("????????????");
                    }
                    return;
                } else {
                    from.sendMessage("???????????????,????????????");
                    return;
                }
            }
        }else if (matcher.group("un") != null){
            from.sendMessage("????????????????????????");
            return;
        }
        var name = matcher.group("name");
        if (name != null){
            long d;
            try {
                 d = osuGetService.getOsuId(name);
            } catch (Exception e) {
                from.sendMessage("?????????osu??????"+name);
                return;
            }
            BinUser nuser = null;
            try {
                nuser = bindDao.getUser(event.getSender().getId());
            } catch (BindException e) {
                //?????????
            }
            if (nuser != null){
                throw new BindException(BindException.Type.BIND_Client_AlreadyBound);
            }
            try {
                var buser = bindDao.getUserFromOsuid(d);
                from.sendMessage(name + " ????????? (" + buser.getQq() + ") ,???????????????,??????????????????");
            } catch (BindException e) {
                bindDao.saveUser(event.getSender().getId(), name, d);
                from.sendMessage("?????????" + event.getSender().getId() + "?????? >>(" + d + ")" + name);
            }
            return;
        }
        //?????????????????????????????? key
        long timeMillis = System.currentTimeMillis();
        //????????????????????????
        if ((event instanceof GroupMessageEvent)) {
            BinUser user = null;
            try {
                user = bindDao.getUser(event.getSender().getId());
            } catch (BindException e) {
                //<<<<<<<<
//                return;
            }
            if (user == null || user.getAccessToken() == null){
                //???????????????????????????
            }else {
                from.sendMessage("????????????("+user.getOsuID()+")"+user.getOsuName()+",????????????????????????,??????'ok'");
                var lock = ASyncMessageUtil.getLock(from.getId(), event.getSender().getId());
                var s = ASyncMessageUtil.getEvent(lock);
                if(s !=null && s.getMessage().contentToString().trim().equalsIgnoreCase("OK")){
                }else {
                    return;
                }
            }

            //---------------
            String state = event.getSender().getId() + "+" + timeMillis;
            //????????????????????? value
            state = osuGetService.getOauthUrl(state);
            var send = new At(event.getSender().getId()).plus(state);
            var receipt = from.sendMessage(send);
            //??????110????????????
            receipt.recallIn(110 * 1000);
            //????????? controller.msgController ??????
            BIND_MSG_MAP.put(timeMillis, new bind(timeMillis, receipt, event.getSender().getId()));
            //---------------
//            throw new BindException(BindException.Type.BIND_Client_AlreadyBound);
        }else {
            //???????????????????????????
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
                                
                #?????? ????????????
                [[text]]
                color=[15,66,48] #???????????????????????????
                styel=1     # 0??? 1?????? 2?????? 3????????????
                size=50     #???????????? ???????????? ????????????????????????
                text="321\\n"
                [[text]]
                text="?????????\\n"
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
                # ??????
                                
                ## ?????????
                                
                **?????????** sbyte short int long float double ***??????????????????*** decimal
                                
                **?????????** byte ushort uint ulong char *char????????????UTF-16*""");
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