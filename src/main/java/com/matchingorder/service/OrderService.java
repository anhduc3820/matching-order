package com.matchingorder.service;

import com.matchingorder.ExchangeApi;
import com.matchingorder.ExchangeCore;
import com.matchingorder.common.CommonResource;
import com.matchingorder.common.api.ApiCancelOrder;
import com.matchingorder.common.api.ApiPlaceOrder;
import com.matchingorder.common.cmd.CommandResultCode;
import com.matchingorder.utils.FixMessageUtils;
import com.matchingorder.utils.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.fix42.*;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final CommonResource commonResources;

    private final SessionMonitor sessionMonitor;

    private final OrderParse orderParse;

    private final ExchangeCore exchangeCore;

    private boolean isInvalidSymbol(String symbol) {
        return symbol == null || symbol.isEmpty() || !commonResources.mapSymbols.containsKey(symbol);
    }

    public void handleNewOrderSingle(Message message) {
        try {
            // get exchange API for publishing commands
            ExchangeApi api = exchangeCore.getApi();

            log.info("Receiver newSingleOrderRequest: {}", LogUtils.formatFixMessageLog(message));
            NewOrderSingle newSingleOrderRequest = (NewOrderSingle) message;

            String symbol = newSingleOrderRequest.getSymbol().getValue();
            String account = newSingleOrderRequest.getAccount().getValue();
            if (isInvalidSymbol(symbol) || account.isEmpty())
                return;

            ApiPlaceOrder apiPlaceOrder = orderParse.parserApiPlaceOrder(newSingleOrderRequest);
            api.submitCommandAsync(apiPlaceOrder);
        } catch (Exception e) {
            log.error("Handle newSingleOrderRequest failed with message: {}", LogUtils.formatFixMessageLog(message), e);
        }
    }

    public void handleOrderCancelRequest(Message message) {
        try {
            // get exchange API for publishing commands
            ExchangeApi api = exchangeCore.getApi();

            log.info("Receiver OrderCancelRequest: {}", LogUtils.formatFixMessageLog(message));
            OrderCancelRequest orderCancelRequest = (OrderCancelRequest) message;

            String symbol = orderCancelRequest.getSymbol().getValue();
            String account = orderCancelRequest.getAccount().getValue();
            if (isInvalidSymbol(symbol) || account.isEmpty())
                return;

            ApiCancelOrder apiCancelOrder = orderParse.parserApiCancelOrder(orderCancelRequest);
            api.submitCommandAsync(apiCancelOrder);
        } catch (Exception e) {
            log.error("Handle OrderCancelRequest failed with message: {}", LogUtils.formatFixMessageLog(message), e);
        }
    }

    public void handleOrderStatusRequest(Message message) {
        try {
            log.info("Receiver OrderCancelRequest: {}", LogUtils.formatFixMessageLog(message));
            OrderStatusRequest orderStatusRequest = (OrderStatusRequest) message;
//            messagePublisher.publishQueue(commonResources.getTradeQueue(), orderStatusRequest.toString().getBytes(StandardCharsets.UTF_8));
//            log.info("Success publishQueue orderStatusRequest with message: {}", LogUtils.formatFixMessageLog(orderStatusRequest));
        } catch (Exception e) {
            log.error("Handle OrderCancelRequest failed with message: {}", LogUtils.formatFixMessageLog(message), e);
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
