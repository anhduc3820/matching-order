package com.matchingorder.common;

import lombok.Getter;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
public class CommonResource {

    @Value("${mts.snapshot.folder}")
    private String snapshotFolder;

    @Value("${mts.core.exchange.name}")
    private String exchangeCoreName;

    @Value("${dict.file.fix42}")
    private String dictFileFix42;

    @Value("${accounts}")
    private List<Long> listAccount;

    @Value("${symbols}")
    private String pathSymbols;

    public DualHashBidiMap<String, Integer> mapSymbols = new DualHashBidiMap<>();

    public int getSymbolId(String symbol) {
        return mapSymbols.get(symbol);
    }

    public String getSymbolCode(String symbol) {
        return mapSymbols.getKey(symbol);
    }
}
