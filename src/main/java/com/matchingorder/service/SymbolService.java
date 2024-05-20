package com.matchingorder.service;

import com.matchingorder.ExchangeApi;
import com.matchingorder.ExchangeCore;
import com.matchingorder.common.CommonResource;
import com.matchingorder.common.CoreSymbolSpecification;
import com.matchingorder.common.SymbolType;
import com.matchingorder.common.api.binary.BatchAddSymbolsCommand;
import com.matchingorder.utils.CSVUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class SymbolService {

    private final ExchangeCore exchangeCore;

    private final CommonResource commonResource;

    @SneakyThrows
    public void loadSymbolConfiguration() {
        // get exchange API for publishing commands
        ExchangeApi api = exchangeCore.getApi();

        List<CSVUtils.Symbol> symbolCsv = CSVUtils.importSymbolCSV(commonResource.getPathSymbols());
        List<CoreSymbolSpecification> symbolSpecs = new ArrayList<>();
        symbolCsv.forEach(symbol -> {
            // create symbol specification and publish it
            CoreSymbolSpecification symbolSpec = CoreSymbolSpecification.builder()
                    .symbolId(Integer.parseInt(symbol.getSymbolId()))         // symbol id
                    .symbolCode(symbol.getSymbolCode())
                    .type(SymbolType.CURRENCY_EXCHANGE_PAIR)
                    .baseCurrency(11)    // base = satoshi (1E-8)
                    .quoteCurrency(15)   // quote = litoshi (1E-8)
                    .baseScaleK(1_000_000L) // 1 lot = 1M satoshi (0.01 BTC)
                    .quoteScaleK(10_000L)   // 1 price step = 10K litoshi
                    .takerFee(1900L)        // taker fee 1900 litoshi per 1 lot
                    .makerFee(700L)         // maker fee 700 litoshi per 1 lot
                    .build();

            symbolSpecs.add(symbolSpec);
            commonResource.mapSymbols.put(symbol.getSymbolCode(), Integer.valueOf(symbol.getSymbolId()));
        });

        api.submitBinaryDataAsync(new BatchAddSymbolsCommand(symbolSpecs));
    }
}
