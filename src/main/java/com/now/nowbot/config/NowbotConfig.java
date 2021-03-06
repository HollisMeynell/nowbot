package com.now.nowbot.config;

import com.now.nowbot.listener.MessageListener;
import com.now.nowbot.throwable.RequestException;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.utils.BotConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Configuration
public class NowbotConfig {
    private static final Logger log = LoggerFactory.getLogger(NowbotConfig.class);
    public static String RUN_PATH;
    public static String BOT_PATH;
    public static String BIN_PATH;
    public static String FONT_PATH;
    public static String BG_PATH;
    public static String IMGBUFFER_PATH;
    public static String OSU_ID;

    public static long QQ;
    public static String PASSWORD;
    public static boolean QQ_LOGIN;
    @Autowired
    public NowbotConfig (FileConfig fileConfig, QQConfig qqConfig){
        RUN_PATH = createDir(fileConfig.root);
        BOT_PATH = createDir(fileConfig.mirai);
        BIN_PATH = createDir(fileConfig.bind);
        FONT_PATH = createDir(fileConfig.font);
        BG_PATH = createDir(fileConfig.bgdir);
        IMGBUFFER_PATH = createDir(fileConfig.imgbuffer);
        OSU_ID = createDir(fileConfig.osuid);

        QQ = qqConfig.qq;
        PASSWORD = qqConfig.password;
        QQ_LOGIN = qqConfig.login;
    }

    @Bean
    public RestTemplate restTemplate() {
        var tempFactory = new OkHttp3ClientHttpRequestFactory();
        tempFactory.setConnectTimeout(3*60*1000);
        tempFactory.setReadTimeout(3*60*1000);
        var template = new RestTemplate(tempFactory);
        template.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response, HttpStatus statusCode) throws RequestException {
                throw new RequestException(response, statusCode);
            }
        });
        return template;
    }

    @Autowired
    MessageListener messageListener;
    @Bean
    public Bot bot(){
        //??????bot?????????
        BotConfiguration botConfiguration = new BotConfiguration();
        //????????????
        botConfiguration.setCacheDir(new File(BOT_PATH));
        botConfiguration.setHeartbeatStrategy(BotConfiguration.HeartbeatStrategy.STAT_HB);
        botConfiguration.setProtocol(BotConfiguration.MiraiProtocol.ANDROID_PAD);
        botConfiguration.setWorkingDir(new File(BOT_PATH));

        File logdir = new File(BOT_PATH+"log");
        if (!logdir.isDirectory()) logdir.mkdirs();
        botConfiguration.redirectBotLogToDirectory(logdir);
        botConfiguration.redirectNetworkLogToDirectory(logdir);
        botConfiguration.fileBasedDeviceInfo();
        botConfiguration.enableContactCache();
        botConfiguration.getContactListCache().setSaveIntervalMillis(60000*30);
        //?????????????????????bot
        Bot bot = BotFactory.INSTANCE.newBot(NowbotConfig.QQ,NowbotConfig.PASSWORD,botConfiguration);
        //???????????? messageListener????????????SimpleListenerHost???
//        bot.getEventChannel().registerListenerHost(messageListener);
        bot.getEventChannel().parentScope(messageListener).registerListenerHost(messageListener);
        return bot;
    }
    public static ApplicationContext applicationContext;
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public String createDir(String path){
        Path pt = Path.of(path);
        if(!Files.isDirectory(pt)) {
            try {
                Files.createDirectories(pt);
            } catch (IOException e) {
                log.error(BOT_PATH+"????????????",e);
            }
        }
        return path;
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}