package com.hrconnect.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ChatConfig {

    /** Dong ho rieng cua Chat (dat ten rieng de khong dung voi bean Clock cua module khac). Test co the thay bang Clock.fixed(...). */
    @Bean("chatClock")
    public Clock chatClock() {
        return Clock.systemUTC();
    }
}
