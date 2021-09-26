package com.now.nowbot.service.MessageService;


import com.now.nowbot.util.SkiaUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.skija.*;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;

@Service("ping")
public class PingService implements MessageService{
    @Override
//    @CheckPermission(roles = {"we","are","winner"})
    public void HandleMessage(MessageEvent event, Matcher matcher) throws Throwable {
        Contact from = event.getSubject();
        byte[] date = null;
        try (Surface surface = Surface.makeRasterN32Premul(500,180)){
            Canvas canvas = surface.getCanvas();
            Typeface face = SkiaUtil.getTorusRegular();

            canvas.clear(Color.makeRGB(0,169,248));
            Font x = new Font(face, 100);
            TextLine t = TextLine.make("PONG!",x);
            canvas.drawTextLine(t,(500-t.getWidth())/2, t.getHeight(),new Paint().setARGB(255,192,219,288));

            date = surface.makeImageSnapshot().encodeToData().getBytes();
        }
        if (date != null) from.sendMessage(ExternalResource.uploadAsImage(ExternalResource.create(date), from)).recallIn(2000);

    }
}