package com.matchingorder.service;

import com.matchingorder.utils.LogUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import quickfix.Message;
import quickfix.field.MsgType;

@Service
@Slf4j
@AllArgsConstructor
public class RoutingService {

    private final OrderService orderService;

    public void processFIXRequestMessage(Message message) {
        try {
            switch (message.getHeader().getString(MsgType.FIELD)) {
                case MsgType.ORDER_SINGLE:
                    orderService.handleNewOrderSingle(message);
                    break;
                case MsgType.ORDER_CANCEL_REQUEST:
                    orderService.handleOrderCancelRequest(message);
                    break;
                case MsgType.ORDER_STATUS_REQUEST:
                    orderService.handleOrderStatusRequest(message);
                    break;

                default:
                    log.warn("UnsupportedMessageType with msg: {}", LogUtils.formatFixMessageLog(message));
            }
        } catch (Exception e) {
            log.error("Handle process FIXRequestMessage failed with msg: {}", LogUtils.formatFixMessageLog(message), e);
        }
    }
}
