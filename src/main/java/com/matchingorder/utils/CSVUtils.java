package com.matchingorder.utils;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CSVUtils {

        @Getter
        public static class Symbol {
            @CsvBindByPosition(position = 0)
            private String symbolId;

            @CsvBindByPosition(position = 1)
            private String symbolCode;
        }

        public static List<Symbol> importSymbolCSV(String filePath) {
            try {
                return (List<Symbol>) new CsvToBeanBuilder(new FileReader(filePath))
                        .withSkipLines(1)
                        .withType(Symbol.class)
                        .build().parse();

            } catch (Exception e) {
                log.error("CSV Utils throws exception: {}", e.getMessage());
            }

            return new ArrayList<>();
        }
}
