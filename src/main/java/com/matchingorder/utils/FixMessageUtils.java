package com.matchingorder.utils;

import lombok.extern.slf4j.Slf4j;
import quickfix.*;
import quickfix.field.ApplVerID;
import quickfix.fix42.Message;

import java.util.List;

@Slf4j
public class FixMessageUtils {
    public static void sendMessage(SessionID sessionID, Message message) {
        try {
            Session session = Session.lookupSession(sessionID);
            if (session == null) {
                throw new SessionNotFound(sessionID.toString());
            }

            DataDictionaryProvider dataDictionaryProvider = session.getDataDictionaryProvider();
            if (dataDictionaryProvider != null) {
                try {
                    dataDictionaryProvider.getApplicationDataDictionary(
                            getApplVerID(session, message)).validate(message, true);
                } catch (Exception e) {
                    LogUtil.logThrowable(sessionID, "Outgoing message failed validation: "
                            + e.getMessage(), e);
                    log.error("Send message {} to session: {} throws exception", LogUtils.formatFixMessageLog(message), sessionID, e);
                    return;
                }
            }

            session.send(message);
            log.info("Send message to client successfully with msg: {}", LogUtils.formatFixMessageLog(message));
        } catch (SessionNotFound e) {
            log.error("Session {} not found", sessionID);
        }
    }

    public static void sendMessageToMultiClient(List<SessionID> listSessions, Message message) {
        listSessions.forEach(session -> {
            sendMessage(session, message);
        });
    }

    private static ApplVerID getApplVerID(Session session, Message message) {
        String beginString = session.getSessionID().getBeginString();
        if (FixVersions.BEGINSTRING_FIX42.equals(beginString)) {
            return new ApplVerID(ApplVerID.FIX42);
        } else {
            return MessageUtils.toApplVerID(beginString);
        }
    }
}
