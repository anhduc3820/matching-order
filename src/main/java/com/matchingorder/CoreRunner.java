package com.matchingorder;

import com.matchingorder.common.CommonResource;
import com.matchingorder.service.SymbolService;
import com.matchingorder.service.TestService;
import com.matchingorder.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import quickfix.SocketAcceptor;

@Slf4j
@Component
@AllArgsConstructor
public class CoreRunner implements CommandLineRunner {

    private final ExchangeCore exchangeCore;

    private final TestService testService;
    private final UserService userService;

    private final SymbolService symbolService;

    private final SocketAcceptor socketAcceptor;

    @Override
    public void run(String... args) throws Exception {
        exchangeCore.startup();
//        testService.testService();
        userService.loadUserSettings();
        symbolService.loadSymbolConfiguration();
        // start app
        socketAcceptor.start();
    }
}
