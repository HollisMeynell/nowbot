package com.now.nowbot.service.MessageService;

import com.alibaba.fastjson.JSONArray;
import com.now.nowbot.entity.BinUser;
import com.now.nowbot.service.OsuGetService;
import com.now.nowbot.util.BindingUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("bpht")
public class bphtService extends MsgSTemp implements MessageService{
    @Autowired
    OsuGetService osuGetService;
    bphtService(){
        super(Pattern.compile("^[!！]\\s?(?i)bpht([:：](?<mode>[osutaikchmn0-4]+))?(\\s+(?<name>[0-9a-zA-Z\\[\\]\\-_ ]*))?"),"bpht");
        setInfo("!bpht [name]获取bp榜单的前5及后5的成绩，以及平均bp，未绑定请带上name参数");
    }

    @Override
    public void HandleMessage(MessageEvent event, Matcher matcher) throws Throwable {
        var from = event.getSubject();
        BinUser nu = null;

        if(matcher.group("name") != null && !matcher.group("name").trim().equals("")){
            String name = matcher.group("name").trim();
                nu = new BinUser();
                nu.setOsuID(osuGetService.getOsuId(name));
                nu.setOsuName(name);
        }else {
            nu = BindingUtil.readUser(event.getSender().getId());
            if(nu.getOsuID() == 0){
                osuGetService.getPlayerOsuInfo(nu);
            }
        }
        JSONArray Bps;

        var mode = matcher.group("mode")==null?"null":matcher.group("mode").toLowerCase();
        switch (mode){
            case"null":
            case"osu":
            case"o":
            case"0":
            default:{
                if(nu.getAccessToken() != null){

                    Bps = osuGetService.getOsuBestMap(nu, 0,100);

                }else {
                    Bps = osuGetService.getOsuBestMap(nu.getOsuID(),0,100);
                }
                mode = "std";
            }break;
            case"taiko":
            case"t":
            case"1":{
                if(nu.getAccessToken() != null){
                    Bps = osuGetService.getTaikoBestMap(nu, 0,100);
                }else {
                    Bps = osuGetService.getTaikoBestMap(nu.getOsuID(),0,100);
                }
                mode = "taiko";
            }break;
            case"catch":
            case"c":
            case"2":{
                if(nu.getAccessToken() != null){
                    Bps = osuGetService.getBestMap(nu,"catch", 0,100);
                }else {
                    Bps = osuGetService.getBestMap(nu.getOsuID(),"catch",0,100);
                }
                mode = "ctb";
            }break;
            case"mania":
            case"m":
            case"3":{
                if(nu.getAccessToken() != null){
                    Bps = osuGetService.getBestMap(nu,"mania", 0,100);
                }else {
                    Bps = osuGetService.getBestMap(nu.getOsuID(),"mania",0,100);
                }
                mode = "mania";
            }break;
        }
        if(Bps.size() != 100){
            from.sendMessage(new At(event.getSender().getId()).plus("您的BP尚未填满，请打完后尝试"));
            return;
        }

        var dtbf = new StringBuffer(nu.getOsuName()).append('[').append(mode).append(']').append('\n');
        double pp = 0;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        for (int i = 0; i < Bps.size(); i++) {
            var jsb = Bps.getJSONObject(i);

            if(i<5 || i>94){
                dtbf.append("#")
                        .append(i+1)
                        .append(' ')
                        .append(decimalFormat.format(jsb.getFloatValue("pp")))
                        .append(' ')
                        .append(decimalFormat.format(100*(float)(50*jsb.getJSONObject("statistics").getIntValue("count_50") + 100 * jsb.getJSONObject("statistics").getIntValue("count_100") + 300 * jsb.getJSONObject("statistics").getIntValue("count_300")) / 300 / (jsb.getJSONObject("statistics").getIntValue("count_miss") + jsb.getJSONObject("statistics").getIntValue("count_50")+ jsb.getJSONObject("statistics").getIntValue("count_100") + jsb.getJSONObject("statistics").getIntValue("count_300"))))
                        .append('%')
                        .append(' ')
                        .append(jsb.getString("rank"));
                if(jsb.getJSONArray("mods").size() > 0){
                    for (int j = 0; j < jsb.getJSONArray("mods").size(); j++) {
                        dtbf.append(' ').append(jsb.getJSONArray("mods").getString(j));
                    }
                }
                dtbf.append('\n');
            }else if(i == 60) {
                dtbf.append("-------分割线-------\n");
            }
            pp += jsb.getFloatValue("pp");
        }

        dtbf.append("您的BP1与BP100的差为").append(decimalFormat.format(Bps.getJSONObject(0).getFloatValue("pp")-Bps.getJSONObject(99).getFloatValue("pp"))).append("\n");
        dtbf.append("您的平均BP为").append(decimalFormat.format(pp/100));

        from.sendMessage(dtbf.toString());
    }
}
