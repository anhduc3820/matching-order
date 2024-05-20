package com.matchingorder.common.config;

import com.matchingorder.service.AcceptorRunner;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import quickfix.*;
import quickfix.fix42.MessageFactory;

@Configuration
public class AcceptorConfig {

    @Value("${executor.config.file}")
    private String executorConfigFile;

    @Autowired
    private AcceptorRunner acceptorRunner;

    @SneakyThrows
    @Bean
    public SessionSettings sessionSettings() {
        return new SessionSettings(executorConfigFile);
    }

    @Bean
    public MessageStoreFactory messageStoreFactory() {
        return new FileStoreFactory(sessionSettings());
    }

    @Bean
    public MessageFactory messageFactory() {
        return new MessageFactory();
    }

    @Bean
    public LogFactory logFactory() {
        return new FileLogFactory(sessionSettings());
    }

    @SneakyThrows
    @Bean
    public SocketAcceptor socketAcceptor() {
        return SocketAcceptor.newBuilder()
                .withApplication(acceptorRunner)
                .withLogFactory(logFactory())
                .withMessageFactory(messageFactory())
                .withMessageStoreFactory(messageStoreFactory())
                .withSettings(sessionSettings())
                .build();
    }
}
