package com.matchingorder.event;

import com.matchingorder.IEventsHandler;
import com.matchingorder.common.CommonResource;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Builder
public class EngineEvent implements IEventsHandler {

    private final CommonResource commonResource;

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
}
