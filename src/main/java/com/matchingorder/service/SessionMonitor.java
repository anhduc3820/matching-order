package com.matchingorder.service;

import com.matchingorder.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import quickfix.Group;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.StringField;
import quickfix.field.Account;
import quickfix.fix42.NewOrderList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service to handle logon session and cache to send message to client
 */
@Component
@Slf4j
public class SessionMonitor {
    private final ConcurrentMap<String, SessionID> sessionMap = new ConcurrentHashMap<>();

    public void registerSession(final String accountId, final SessionID session) {
        sessionMap.put(accountId, session);
    }

    public SessionID getSessionFromTargetCompID(final String accountId) {
        return sessionMap.get(accountId);
    }

    public void processRegisterSession(Message message, SessionID sessionID) {
        try {
            if (message instanceof NewOrderList) {
                NewOrderList newOrderList = (NewOrderList) message;
                int tag = newOrderList.getNoOrders().getTag();
                List<Group> listGroup = newOrderList.getGroups(tag);
                for (Group group : listGroup) {
                    String accountId = group.getField(new StringField(Account.FIELD)).getValue();
                    registerSession(accountId, sessionID);
                }
            }

            String accountId = message.getField(new StringField(Account.FIELD)).getValue();
            registerSession(accountId, sessionID);
        } catch (Exception e) {
            log.error("Failed to processRegisterSession with message: {}", LogUtils.formatFixMessageLog(message), e);
        }
    }

    public List<SessionID> getAllSessionIds() {
        List<SessionID> listSessions = new ArrayList<>();
        sessionMap.forEach((k, v) -> listSessions.add(v));
        return listSessions;
    }

    public void removeSession(final String accountId, final SessionID session) {
        sessionMap.remove(accountId, session);
    }

    public boolean validateClientId(String clientId) {
        SessionID sessionID =  sessionMap.get(clientId);
        if (sessionID == null) {
            log.warn("Not found session for client with account_id={}", clientId);
            return false;
        }

        return true;
    }
}
