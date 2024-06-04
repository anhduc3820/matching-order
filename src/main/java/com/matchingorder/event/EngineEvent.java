package com.matchingorder.event;

import com.matchingorder.IEventsHandler;
import com.matchingorder.common.CommonResource;
import com.matchingorder.service.OrderParse;
import com.matchingorder.service.SessionMonitor;
import com.matchingorder.utils.FixMessageUtils;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import quickfix.Message;
import quickfix.SessionID;

@Service
@Slf4j
@Builder
public class EngineEvent implements IEventsHandler {

    private final CommonResource commonResource;

    private final SessionMonitor sessionMonitor;

    private final OrderParse orderParse;

    @Override
    public void commandResult(ApiCommandResult commandResult) {
        log.info("COMMAND RESULT: {}", commandResult);
    }

    @Override
    public void tradeEvent(TradeEvent tradeEvent) {
        log.info("TRADE EVENT: {}", tradeEvent);
    }

    @Override
    public void rejectEvent(RejectEvent rejectEvent) {
        log.info("REJECT EVENT: {}", rejectEvent);
    }

    @Override
    public void reduceEvent(ReduceEvent reduceEvent) {
        log.info("REDUCE EVENT: {}", reduceEvent);
    }

    @Override
    public void orderBook(OrderBook orderBook) {
        log.info("ORDER BOOK EVENT: {}", orderBook);
    }

    @Override
    public void orderEvent(OrderEvent orderEvent) {
        log.info("ORDER EVENT: {}", orderEvent);
        switch (orderEvent.state) {
            case PLACE_ORDER:
                sendMessageToClient(String.valueOf(orderEvent.takerUid), orderParse.parseNewOrderExecutionReport(orderEvent));
                break;
            case CANCEL_ORDER:
                sendMessageToClient(String.valueOf(orderEvent.takerUid), orderParse.parseOrderCancelExecutionReport(orderEvent));
                break;
        }
    }

    private void sendMessageToClient(String accountId, Message message) {
        SessionID sessionID = sessionMonitor.getSessionFromTargetCompID(accountId);
        if (sessionID == null) {
            log.warn("Session ID is null with accountId: {}", accountId);
            return;
        }
        FixMessageUtils.sendMessage(sessionID, (quickfix.fix42.Message) message);
    }
}
