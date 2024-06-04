package com.matchingorder.service;

import com.matchingorder.IEventsHandler;
import com.matchingorder.common.CommonResource;
import com.matchingorder.common.OrderAction;
import com.matchingorder.common.OrderType;
import com.matchingorder.common.api.ApiCancelOrder;
import com.matchingorder.common.api.ApiPlaceOrder;
import com.matchingorder.utils.CustomExecutionReport;
import com.matchingorder.utils.LogUtils;
import com.matchingorder.utils.PriceUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelRequest;

import java.math.BigDecimal;

import static quickfix.field.Side.BUY;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderParse {

    private final CommonResource commonResource;

    public ExecutionReport parseNewOrderExecutionReport(IEventsHandler.OrderEvent orderEvent) {
        // price required in order type limit
        BigDecimal ordPrice = BigDecimal.valueOf(orderEvent.price);
        CustomExecutionReport executionReportDto = CustomExecutionReport.builder()
                .accountId(orderEvent.takerUid)
                .orderId(orderEvent.externalOrder) // required field
                .clOrdId(String.valueOf(orderEvent.takerOrderId))
                .ordType(new OrdType(OrdType.LIMIT))
                .execId(1234) // required field
                .execTransType(new ExecTransType(ExecTransType.NEW)) // required field
                .execType(new ExecType(ExecType.NEW)) // required field
                .ordStatus(new OrdStatus(OrdStatus.NEW))
                .symbol("AAPL")
                .securityExchange("TEST")
                .orderQty(orderEvent.totalVolume)
                .side(orderEvent.takerAction.equals(OrderAction.ASK) ? new Side(Side.SELL) : new Side(BUY))
                .currencyCode("USD")
                .ordPrice(ordPrice)
                .leaveQty(0L) // required field
                .cumQty(0L) // required field
                .avgPrice(BigDecimal.valueOf(new AvgPx((0)).getValue())) // required field
                .build();
        return executionReportDto.buildExecutionReport();
    }

    @SneakyThrows
    public ApiPlaceOrder parserApiPlaceOrder(NewOrderSingle newOrder) {
        String symbol = newOrder.getSymbol().getValue();
        long account = Long.parseLong(newOrder.getAccount().getValue());
        long clOrderId = Long.parseLong(newOrder.getClOrdID().getValue());
        long price = PriceUtils.convertPriceToLong(BigDecimal.valueOf(newOrder.getPrice().getValue()));
        long qty = (long) newOrder.getOrderQty().getValue();
        OrderAction action = getOrderAction(newOrder.getSide().getValue());
        int symbolId = commonResource.getSymbolId(symbol);

        return ApiPlaceOrder.builder()
                .uid(account)
                .orderId(clOrderId)
                .price(price)
                .reservePrice(price)
                .size(qty)
                .action(action)
                .orderType(OrderType.GTC)
                .symbol(symbolId)
                .build();
    }

    @SneakyThrows
    public ApiCancelOrder parserApiCancelOrder(OrderCancelRequest request) {
        String symbol = request.getSymbol().getValue();
        long account = Long.parseLong(request.getAccount().getValue());
        long orderId = Long.parseLong(request.getOrigClOrdID().getValue());
        int symbolId = commonResource.getSymbolId(symbol);

        return ApiCancelOrder.builder()
                .uid(account)
                .orderId(orderId)
                .symbol(symbolId)
                .build();
    }

    public OrderAction getOrderAction(char side) {
        switch (side) {
            case BUY:
                return OrderAction.BID;

            default:
                return OrderAction.ASK;
        }
    }

    public ExecutionReport parseOrderCancelExecutionReport(IEventsHandler.OrderEvent orderEvent) {
        CustomExecutionReport executionReportDto = CustomExecutionReport.builder()
                .accountId(orderEvent.takerUid)
                .orderId(orderEvent.externalOrder)
                .clOrdId(String.valueOf(orderEvent.takerOrderId))
                .execId(0) // required field
                .execTransType(new ExecTransType(ExecTransType.CANCEL)) // required field
                .execType(new ExecType(ExecType.CANCELED)) // required field
                .ordStatus(new OrdStatus(OrdStatus.CANCELED))
                .symbol("AAPL")
                .securityExchange("USAH")
                .orderQty(0L)
                .side(orderEvent.takerAction.equals(OrderAction.ASK) ? new Side(Side.SELL) : new Side(BUY))
                .currencyCode("USD")
                .leaveQty(0L) // required field
                .cumQty(0L) // required field
                .avgPrice(BigDecimal.valueOf(new AvgPx((0)).getValue())) // required field
                .build();

        return executionReportDto.buildExecutionReport();
    }
}
