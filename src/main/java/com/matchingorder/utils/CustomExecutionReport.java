package com.matchingorder.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class CustomExecutionReport {
    private long accountId;
    private long orderId;
    private String clOrdId;
    private String clientId;
    private OrdType ordType;
    private String origClOrdId;
    private long execId;
    private ExecTransType execTransType;
    private ExecType execType;
    private OrdStatus ordStatus;
    private String symbol;
    private String securityExchange;
    private long orderQty;
    private Side side;
    private BigDecimal ordPrice;
    private long leaveQty;
    private long cumQty;
    private BigDecimal avgPrice;
    private String currencyCode;

    public ExecutionReport buildExecutionReport() {
        ExecutionReport executionReport = new ExecutionReport();
        executionReport.set(new Account(String.valueOf((accountId))));
        executionReport.set(new OrderID(String.valueOf(orderId)));
        if (origClOrdId != null) executionReport.set(new OrigClOrdID(origClOrdId));
        executionReport.set(new ClOrdID(clOrdId));
        if (clientId != null) executionReport.set(new ClientID(clientId));
        if (ordType != null) executionReport.set(ordType);
        if (ordPrice != null) executionReport.set(new Price(ordPrice.doubleValue()));
        executionReport.set(new ExecID(String.valueOf(execId)));
        executionReport.set(execTransType);
        executionReport.set(ordStatus);
        executionReport.set(execType);
        executionReport.set(new Symbol(symbol));
        executionReport.set(new OrderQty(orderQty));
        executionReport.set(side);
        if (currencyCode != null) executionReport.set(new Currency(currencyCode));
        executionReport.set(new LeavesQty(leaveQty));
        executionReport.set(new CumQty(cumQty));
        executionReport.set(new AvgPx(avgPrice.doubleValue()));
        executionReport.set(new SecurityExchange(securityExchange));
        executionReport.set(new TimeInForce(TimeInForce.DAY));
        executionReport.set(new TransactTime());
        return executionReport;
    }
}
