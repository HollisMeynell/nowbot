package com.now.nowbot.util;

import com.fasterxml.jackson.databind.JsonNode;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


public class MoliUtil {
    private static RestTemplate restTemplate;
    private static final String url = "https://i.mly.app/reply";
    private static final HttpHeaders headers = new HttpHeaders();
    public static record Friend(String sg, String name, Long id){};
    public static record Group(String sg, String name, Long id, String groupName, Long groupId){};

    public static Friend getFriend(MessageEvent e){
        return new Friend(e.getMessage().contentToString(), e.getSenderName(), e.getSender().getId());
    }
    public static Group getGroup(GroupMessageEvent e){
        return new Group(e.getMessage().contentToString(), e.getSenderName(), e.getSender().getId(), e.getGroup().getName(), e.getGroup().getId());
    }
    public static void init(RestTemplate restTemplate){
        MoliUtil.restTemplate = restTemplate;
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Api-Key", "438983az3zr8riuk");
        headers.set("Api-Secret", "vissf8jj");
    }

    public static String[] getMsg(Friend friend){
        post body = new post(friend.id(), friend.name());
        body.setContent(friend.sg());
        HttpEntity<post> httpEntity = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> c = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JsonNode.class);
        var data = c.getBody();
        if ("00000".equals(data.get("code").asText())) {
            var req = data.get("data");
            String[] strs = new String[req.size()];
            for (int i = 0; i < req.size(); i++) {
                strs[i] = req.get(i).get("content").asText();
            }
            return strs;
        }else {
            throw new RuntimeException(data.get("message").asText());
        }
    }
    public static String[] getMsg(Group group){
        post body = new post(group.id(), group.name(), group.groupId(), group.groupName());
        body.setContent(group.sg());
        HttpEntity<post> httpEntity = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> c = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JsonNode.class);
        var data = c.getBody();
        if ("00000".equals(data.get("code").asText())) {
            var req = data.get("data");
            String[] strs = new String[req.size()];
            for (int i = 0; i < req.size(); i++) {
                strs[i] = req.get(i).get("content").asText();
            }
            return strs;
        }else {
            throw new RuntimeException(data.get("message").asText());
        }
    }
    private static class post{
        String content;
        int type;
        Long from;
        String fromName;
        Long to;
        String toName;

        public post(Long from, String fromName) {
            this.content = "";
            this.type = 1;
            this.from = from;
            this.fromName = fromName;
            this.to = 0L;
            this.toName="";
        }

        public post(Long from, String fromName, Long to, String toName) {
            this.content = "";
            this.type = 2;
            this.from = from;
            this.fromName = fromName;
            this.to = to;
            this.toName = toName;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public int getType() {
            return type;
        }

        public Long getFrom() {
            return from;
        }

        public String getFromName() {
            return fromName;
        }

        public Long getTo() {
            return to;
        }

        public String getToName() {
            return toName;
        }
    }
}
