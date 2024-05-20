package com.matchingorder.service;

import com.matchingorder.utils.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import quickfix.*;
import quickfix.fix42.Heartbeat;

@Component
@Slf4j
@RequiredArgsConstructor
public class AcceptorRunner extends MessageCracker implements Application {
    private final SessionMonitor sessionMonitor;

    private final RoutingService routingService;

    // Init connection ~ TCP/Socket
    @Override
    public void onCreate(SessionID sessionId) {
        log.info("Session create: {}", sessionId);
    }

    // After handshake initiators <--> acceptor
    @Override
    public void onLogon(SessionID sessionId) {
        log.info("Session logon: {}", sessionId);
    }

    // Close connection
    @Override
    public void onLogout(SessionID sessionId) {
        log.info("Session logout : {}", sessionId);
    }

    // Admin message
    // Heartbeat,..
    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        if (message instanceof Heartbeat) {
            log.debug("Message to Admin: {}, from Session: {}", LogUtils.formatFixMessageLog(message), sessionId);
        } else log.info("Message to Admin: {}, from Session: {}", LogUtils.formatFixMessageLog(message), sessionId);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        if (message instanceof Heartbeat) {
            log.debug("Message from Admin: {}, from Session: {}", LogUtils.formatFixMessageLog(message), sessionId);
        } else log.info("Message from Admin: {}, from Session: {}", LogUtils.formatFixMessageLog(message), sessionId);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        log.info("Message to App: {}, from Session: {}", LogUtils.formatFixMessageLog(message), sessionId);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        crack(message, sessionId);
        log.info("Message from App: {}, from Session: {}", LogUtils.formatFixMessageLog(message), sessionId);
    }

    public void onMessage(Message message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        log.info("OnMessage: {}", LogUtils.formatFixMessageLog(message));
        // regist session by account
        sessionMonitor.processRegisterSession(message, sessionID);
        routingService.processFIXRequestMessage(message);
    }
}
