package com.matchingorder.service;

import com.matchingorder.ExchangeCore;
import com.matchingorder.common.api.ApiPersistState;
import com.matchingorder.common.cmd.CommandResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static com.matchingorder.constant.CommonConstants.SNAPSHOT_INTERVAL;


@Slf4j
@Service
public class SnapshotService {
    @Autowired
    private ExchangeCore exchangeCore;

    @Scheduled(fixedDelay = SNAPSHOT_INTERVAL)
    public void createSnapShot() {
        long snapshotId = System.currentTimeMillis();
        try {
            ApiPersistState command = ApiPersistState.builder()
                    .dumpId(snapshotId)
                    .build();

            log.info("Start make a snapshot of MTS snapshot_id={}", snapshotId);
            CompletableFuture<CommandResultCode> result = exchangeCore.getApi().submitCommandAsync(command);
            log.info("Finish make a snapshot of MTS, snapshot_id={}, result_code={}", snapshotId, result.get());
        } catch (Exception e) {
            log.error("Failed make a snapshot with snapshot_id={}", snapshotId, e);
        }
    }

}
