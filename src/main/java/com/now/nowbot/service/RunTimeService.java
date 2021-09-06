package com.now.nowbot.service;


import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.At;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

@Service
public class RunTimeService {
    private static final Logger log = LoggerFactory.getLogger(RunTimeService.class);
    @Autowired
    Bot bot;
    @Scheduled(cron = "0 0 0 * * *")
    public void sleep(){
        bot.getGroups().forEach(group -> {
            var gs = group.getMembers();
            try {
                var n = gs.getOrFail(1529813731L);
                group.sendMessage(new At(n.getId()).plus("快去睡啦"));
            } catch (Exception e) {
            }
        });
    }
    @Scheduled(cron = "0 0/30 8-18 * * *")
    public void alive(){
        var m = ManagementFactory.getMemoryMXBean();
        var nm = m.getNonHeapMemoryUsage();
        var t = ManagementFactory.getThreadMXBean();
        var z = ManagementFactory.getMemoryPoolMXBeans();
        log.info("非堆 已申请 {}M 已使用 {}M ",
                nm.getCommitted()/1024/1024,
                nm.getUsed()/1024/1024
        );
        log.info("堆内存上限{}M,当前内存占用{}M, 已使用{}M\n当前线程数 {} ,守护线程 {} ,峰值线程 {}",
                m.getHeapMemoryUsage().getMax()/1024/1024,
                m.getHeapMemoryUsage().getCommitted()/1024/1024,
                m.getHeapMemoryUsage().getUsed()/1024/1024,
                t.getThreadCount(),
                t.getDaemonThreadCount(),
                t.getPeakThreadCount()
        );
        for (var pool : z){
            log.info("vm内存 {} 已申请 {}M 已使用 {}M ",
                    pool.getName(),
                    pool.getUsage().getCommitted()/1024/1024,
                    pool.getUsage().getUsed()/1024/1024
            );
        }
    }
}