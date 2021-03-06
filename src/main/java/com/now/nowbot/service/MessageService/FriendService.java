package com.now.nowbot.service.MessageService;

import com.fasterxml.jackson.databind.JsonNode;
import com.now.nowbot.dao.BindDao;
import com.now.nowbot.service.OsuGetService;
import com.now.nowbot.throwable.TipsException;
import com.now.nowbot.util.Panel.ACardBuilder;
import com.now.nowbot.util.Panel.FriendPanelBuilder;
import com.now.nowbot.util.PanelUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.skija.EncodedImageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.regex.Matcher;

@Service("friend")
public class FriendService implements MessageService{
//    static final ThreadPoolExecutor threads = new ThreadPoolExecutor(0, 12, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(256));
    private static final Logger log = LoggerFactory.getLogger(FriendService.class);

    BindDao bindDao;
    OsuGetService osuGetService;
    @Autowired
    public FriendService(OsuGetService osuGetService,BindDao bindDao){
        this.osuGetService = osuGetService;
        this.bindDao = bindDao;
    }
    @Override
    public void HandleMessage(MessageEvent event, Matcher matcher) throws Throwable {
        var from = event.getSubject();

        var user = bindDao.getUser(event.getSender().getId());
//        var user = bindDao.getUser(2480557535L); 调试代码

        //拿到参数,默认1-24个
        int n1 = 0,n2=0;
        boolean doRandom = true;
        if (matcher.group("m") == null){
            n2 = matcher.group("n") == null? 23 : Integer.parseInt(matcher.group("n"));
        }else {
            doRandom = false;
            n1 = Integer.parseInt(matcher.group("n"));
            n2 = Integer.parseInt(matcher.group("m"));
            if(n1 > n2) {n1 ^= n2; n2 ^= n1; n1 ^= n2;}
        }
        if (n2 == 0 || 100 < n2-n1 ){
            throw new TipsException("参数范围错误!");
        }

        var allFriend = osuGetService.getFrendList(user);
        final var p = new FriendPanelBuilder();
        //构造自己的卡片
        var infoMe = osuGetService.getPlayerInfo(user);
        var card = new ACardBuilder(PanelUtil.getBgUrl(null/*"自定义路径"*/,infoMe.getCoverUrl(),true));
        card.drawA1(infoMe.getAvatarUrl())
                .drawA2(PanelUtil.getFlag(infoMe.getCountry().countryCode()))
                .drawA3(infoMe.getUsername());
        if (infoMe.getSupportLeve() != 0){
            card.drawA2(PanelUtil.OBJECT_CARD_SUPPORTER);
        }
        card.drawB3("")
                .drawB2(infoMe.getCountry().countryCode() + "#" + infoMe.getStatustucs().getCountryRank())
                .drawB1("U" + infoMe.getId())
                .drawC2(infoMe.getStatustucs().getAccuracy() + "% Lv." +
                        infoMe.getStatustucs().getLevelCurrent() +
                        "(" + infoMe.getStatustucs().getLevelProgress() + "%)")
                .drawC1(infoMe.getStatustucs().getPp(0) + "PP");

        p.drawBanner(PanelUtil.getBanner(user));
        p.mainCard(card.build());
       int[] index = null;
       if (doRandom) {
           //构造随机数组
           index = new int[allFriend.size()];
           for (int i = 0; i < index.length; i++) {
               index[i] = i;
           }
           for (int i = 0; i < index.length; i++) {
               int rand = rand(i,index.length);
               if (rand != 1) {
                   int temp = index[rand];
                   index[rand] = index[i];
                   index[i] = temp;
               }
           }
       }
        //好友绘制
        for (int i = n1; i <= n2 && i < allFriend.size(); i++) {
            try {
                JsonNode infoO;
                if (doRandom){
                    infoO = allFriend.get(index[i]);
                }else {
                    infoO = allFriend.get(i);
                }

                var cardO = new ACardBuilder(PanelUtil.getBgUrl(null,infoO.findValue("url").asText(),true));
                cardO.drawA1(infoO.findValue("avatar_url").asText())
                        .drawA2(PanelUtil.getFlag(infoO.findValue("country_code").asText()))
                        .drawA3(infoO.findValue("username").asText());
                if (infoO.findValue("is_supporter").asBoolean(false)){
                    cardO.drawA2(PanelUtil.OBJECT_CARD_SUPPORTER);
                }
                //对bot特殊处理
                if(infoO.findValue("is_bot").asBoolean(false)){
                    cardO.drawB1("U" + infoO.findValue("id").asText("NaN")).drawC1("Bot");
                } else {
                    cardO.drawB2("#" + infoO.findValue("global_rank").asText("0"))
                            .drawB1("U" + infoO.findValue("id").asText("NaN"))
                            .drawC2(infoO.findValue("hit_accuracy").asText().substring(0, 4) + "% Lv." +
                                    infoO.findValue("current").asText("NaN") +
                                    "(" + infoO.findValue("progress").asText("NaN") + "%)")
                            .drawC1(infoO.findValue("pp").asInt() + "PP");
                }
                p.addFriendCard(cardO.build());
            } catch (Exception e) {
                log.error("卡片加载第{}个失败,数据为\n{}",i,allFriend.get(i).toString(),e);
            }
        }

        from.sendMessage(from.uploadImage(ExternalResource.create(p.build().encodeToData(EncodedImageFormat.JPEG,80).getBytes())));
        card.build().close();
        p.build().close();
    }
    static final Random random = new Random();
    static int rand(int min, int max){
        return min + random.nextInt(max-min);
    }

}
