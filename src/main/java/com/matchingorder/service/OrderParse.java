package com.matchingorder.service;

import com.matchingorder.IEventsHandler;
import com.matchingorder.common.CommonResource;
import com.matchingorder.common.OrderAction;
import com.matchingorder.common.OrderType;
import com.matchingorder.common.api.ApiCancelOrder;
import com.matchingorder.common.api.ApiPlaceOrder;
import com.matchingorder.utils.CustomExecutionReport;
import com.matchingorder.utils.PriceUtils;
import com.matchingorder.utils.Snowflake;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelRequest;

import java.math.BigDecimal;

import static quickfix.field.Side.BUY;
import static quickfix.field.Side.SELL;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderParse {

    private final CommonResource commonResource;

    private final Snowflake snowflake;

    public ExecutionReport parseNewOrderExecutionReport(IEventsHandler.OrderEvent orderEvent) {
        // price required in order type limit
        BigDecimal ordPrice = BigDecimal.valueOf(orderEvent.price);
        CustomExecutionReport executionReportDto = CustomExecutionReport.builder()
                .accountId(orderEvent.takerUid)
                .orderId(orderEvent.externalOrder) // required field
                .clOrdId(String.valueOf(orderEvent.takerOrderId))
                .ordType(new OrdType(OrdType.LIMIT))
                .execId(orderEvent.externalOrder) // required field
                .execTransType(new ExecTransType(ExecTransType.NEW)) // required field
                .execType(new ExecType(ExecType.NEW)) // required field
                .ordStatus(new OrdStatus(OrdStatus.NEW))
                .symbol(commonResource.mapSymbols.getKey(orderEvent.symbol))
                .securityExchange("USAH")
                .orderQty(orderEvent.totalVolume)
                .side(orderEvent.takerAction.equals(OrderAction.ASK) ? new Side(Side.SELL) : new Side(BUY))
                .currencyCode("USD")
                .ordPrice(ordPrice)
                .leaveQty(orderEvent.totalVolume) // required field
                .cumQty(0L) // required field
                .avgPrice(BigDecimal.valueOf(new AvgPx((0)).getValue())) // required field
                .build();
        return executionReportDto.buildExecutionReport();
    }

    @SneakyThrows
    public ApiPlaceOrder parserApiPlaceOrder(NewOrderSingle newOrder) {
        String symbol = newOrder.getSymbol().getValue();
        long account = Long.parseLong(newOrder.getAccount().getValue());
        String clOrderId = newOrder.getClOrdID().getValue();
        long price = PriceUtils.convertPriceToLong(BigDecimal.valueOf(newOrder.getPrice().getValue()));
        long qty = (long) newOrder.getOrderQty().getValue();
        OrderAction action = getOrderAction(newOrder.getSide().getValue());
        int symbolId = commonResource.getSymbolId(symbol);

        return ApiPlaceOrder.builder()
                .uid(account)
                .orderId(Long.parseLong(clOrderId))
                .externalOrderId(snowflake.nextId())
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
        int symbolId = commonResource.getSymbolId(symbol);

        return ApiCancelOrder.builder()
                .uid(account)
                .orderId(Long.parseLong(request.getOrigClOrdID().getValue()))
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
                .execId(orderEvent.externalOrder) // required field
                .execTransType(new ExecTransType(ExecTransType.CANCEL)) // required field
                .execType(new ExecType(ExecType.CANCELED)) // required field
                .ordStatus(new OrdStatus(OrdStatus.CANCELED))
                .symbol(commonResource.mapSymbols.getKey(orderEvent.symbol))
                .securityExchange("USAH")
                .orderQty(orderEvent.totalVolume)
                .side(orderEvent.takerAction.equals(OrderAction.ASK) ? new Side(Side.SELL) : new Side(BUY))
                .currencyCode("USD")
                .leaveQty(orderEvent.totalVolume) // required field
                .cumQty(orderEvent.fillVolume) // required field
                .avgPrice(BigDecimal.valueOf(new AvgPx((0)).getValue())) // required field
                .build();

        return executionReportDto.buildExecutionReport();
    }

    public ExecutionReport parseOrderTradeExecutionReport(IEventsHandler.TradeEvent tradeEvent) {
        ExecType execType = new ExecType(ExecType.FILL);
        OrdStatus ordStatus = new OrdStatus(OrdStatus.FILLED);
        if (tradeEvent.directOrder.size - tradeEvent.directOrder.filled > 0) {
            execType = new ExecType(ExecType.PARTIAL_FILL);
            ordStatus = new OrdStatus(OrdStatus.PARTIALLY_FILLED);
        }

        CustomExecutionReport executionReportDto = CustomExecutionReport.builder()
                .accountId(tradeEvent.takerUid)
                .orderId(tradeEvent.directOrder.externalOrderId)
                .clOrdId(String.valueOf(tradeEvent.takerOrderId))
                .execId(tradeEvent.directOrder.externalOrderId) // required field
                .execTransType(new ExecTransType(ExecTransType.NEW))
                .execType(execType) // required field
                .ordStatus(ordStatus)
                .symbol(commonResource.mapSymbols.getKey(tradeEvent.symbol))
                .securityExchange("USAH")
                .orderQty(tradeEvent.directOrder.size)
                .lastShares(BigDecimal.valueOf(tradeEvent.totalVolume))
                .side(tradeEvent.takerAction.equals(OrderAction.ASK) ? new Side(Side.SELL) : new Side(BUY))
                .currencyCode("USD")
                .leaveQty(tradeEvent.directOrder.size - tradeEvent.directOrder.filled) // required field
                .cumQty(tradeEvent.directOrder.filled) // required field
                .avgPrice(BigDecimal.valueOf(tradeEvent.directOrder.price)) // required field
                .build();

        return executionReportDto.buildExecutionReport();
    }

    public ExecutionReport parseOrderTradeExecutionReport(IEventsHandler.Trade trade, IEventsHandler.TradeEvent tradeEvent) {
        ExecType execType = new ExecType(ExecType.FILL);
        OrdStatus ordStatus = new OrdStatus(OrdStatus.FILLED);
        if (trade.directOrder.size - trade.directOrder.filled > 0) {
            execType = new ExecType(ExecType.PARTIAL_FILL);
            ordStatus = new OrdStatus(OrdStatus.PARTIALLY_FILLED);
        }

        CustomExecutionReport executionReportDto = CustomExecutionReport.builder()
                .accountId(trade.makerUid)
                .orderId(trade.directOrder.externalOrderId)
                .clOrdId(String.valueOf(trade.makerOrderId))
                .execId(trade.directOrder.externalOrderId) // required field
                .execTransType(new ExecTransType(ExecTransType.NEW))
                .execType(execType) // required field
                .ordStatus(ordStatus)
                .symbol(commonResource.mapSymbols.getKey(tradeEvent.symbol))
                .securityExchange("USAH")
                .orderQty(trade.directOrder.size)
                .lastShares(BigDecimal.valueOf(trade.directOrder.filled))
                .side(tradeEvent.takerAction.equals(OrderAction.ASK) ? new Side(BUY) : new Side(SELL))
                .currencyCode("USD")
                .leaveQty(trade.directOrder.size - trade.directOrder.filled) // required field
                .cumQty(trade.directOrder.filled) // required field
                .avgPrice(BigDecimal.valueOf(trade.directOrder.price)) // required field
                .build();

        return executionReportDto.buildExecutionReport();
    }
}
