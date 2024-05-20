package com.matchingorder.common.config;

import com.matchingorder.ExchangeCore;
import com.matchingorder.SimpleEventsProcessor;
import com.matchingorder.common.CommonResource;
import com.matchingorder.common.config.*;
import com.matchingorder.event.EngineEvent;
import com.matchingorder.processors.journaling.DiskSerializationProcessor;
import com.matchingorder.processors.journaling.DiskSerializationProcessorConfiguration;
import com.matchingorder.utils.FileUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.matchingorder.processors.journaling.DiskSerializationProcessorConfiguration.LZ4_FAST;

@Configuration
@AllArgsConstructor
@Slf4j
public class ApplicationConfig {

    private CommonResource commonResource;

    @Bean
    public ExchangeCore exchangeCore() {
        InitialStateConfiguration configId = getSnapshotId(commonResource.getSnapshotFolder(), commonResource.getExchangeCoreName());
        return configExchangeCore(configId, commonResource);
    }

    private ExchangeCore configExchangeCore(InitialStateConfiguration configId, CommonResource commonResource) {

        // Config EventHandler
        EngineEvent eventHandler = EngineEvent.builder()
                .commonResource(commonResource)
                .build();

        SimpleEventsProcessor eventsProcessor = new SimpleEventsProcessor(eventHandler);

        // Config logging
        LoggingConfiguration logConf = LoggingConfiguration.builder()
                .loggingLevels(
                        EnumSet.of(LoggingConfiguration.LoggingLevel.LOGGING_MATCHING_DEBUG,
                                LoggingConfiguration.LoggingLevel.LOGGING_RISK_DEBUG,
                                LoggingConfiguration.LoggingLevel.LOGGING_WARNINGS))
                .build();

        // Config order processing
        // Currently disable RISK ENGINE and MARGIN TRADE, so any orders will be accepted.
        // We only test Matching Engine
        OrdersProcessingConfiguration orderProcessConf = OrdersProcessingConfiguration.builder()
                // TODO: Turn on riskEngine to validate data later
                .riskProcessingMode(OrdersProcessingConfiguration.RiskProcessingMode.FULL_PER_CURRENCY)
                .marginTradingMode(OrdersProcessingConfiguration.MarginTradingMode.MARGIN_TRADING_DISABLED)
                .build();

        // Config performance, order book implement
        // Latency performance will use OrderBookDirect implement
        PerformanceConfiguration perfConfig = PerformanceConfiguration.latencyPerformanceBuilder()
                .ringBufferSize(2048)
                .matchingEnginesNum(1)
                .riskEnginesNum(1)
                .build();

        // Config snapshot folders, enable journaling for ME
        long ONE_MEGABYTE = 1024 * 1024;
        DiskSerializationProcessorConfiguration diskConfig = DiskSerializationProcessorConfiguration.builder()
                .storageFolder(commonResource.getSnapshotFolder())
                .snapshotLz4CompressorFactory(LZ4_FAST)
                .journalFileMaxSize(4000 * ONE_MEGABYTE)
                .journalBufferSize(256 * 1024) // 256 KB - TODO calculate based on ringBufferSize
                .journalBatchCompressThreshold(2048)
                .journalLz4CompressorFactory(LZ4_FAST)
                .build();

        SerializationConfiguration serializationCfg = SerializationConfiguration.builder()
                .enableJournaling(true)
                .serializationProcessorFactory(exchangeCfg -> new DiskSerializationProcessor(exchangeCfg, diskConfig))
                .build();

        // In real matching engine, the ME will store all state from the past,
        // so we only need the same exchangeID for all sessions.
        // But with this customize engine, the ME will start a new fresh session, it don't care about
        // previous sessions, so we need to add datetime now() in the exchange to avoid conflict
        // journaling file and snapshot files.

        // Default exchange config
        ExchangeConfiguration conf = ExchangeConfiguration.defaultBuilder()
                .serializationCfg(serializationCfg)
                .ordersProcessingCfg(orderProcessConf)
                .loggingCfg(logConf)
                .performanceCfg(perfConfig)
                .initStateCfg(InitialStateConfiguration.fromSnapshotOnly(configId.getExchangeId(), configId.getSnapshotId(), configId.getSnapshotBaseSeq()))
                .build();

        return ExchangeCore.builder()
                .resultsConsumer(eventsProcessor)
                .exchangeConfiguration(conf)
                .build();
    }

    private InitialStateConfiguration getSnapshotId(String snapshotFolder, String exchangeCoreName) {
        try (Stream<Path> stream = Files.list(Paths.get(snapshotFolder))) {
            Set<String> setPaths = stream.filter(file -> !Files.isDirectory(file))
                  .map(Path::getFileName)
                  .map(Path::toString)
                  .collect(Collectors.toSet());

            return FileUtils.getLatestSnapshotId(setPaths, exchangeCoreName);
        } catch (Exception e) {
            log.error("Get snapshotId throws exception: {}", e.getMessage());
            return new InitialStateConfiguration(exchangeCoreName, 0, 0, 0);
        }
    }
}
