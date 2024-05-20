package com.matchingorder.utils;

import com.matchingorder.common.config.InitialStateConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class FileUtils {

    public static InitialStateConfiguration getLatestSnapshotId(Set<String> filesPath, String exchangeCoreName) {
        long max_id = 0;
        long baseSeq = 0;
        for (String path: filesPath) {
            if (path.endsWith(".ecs")) {
                String[] parts = path.split("_");
                long id = Long.parseLong(parts[2]);
                if (id > max_id) max_id =id;
            }
        }

        log.info("Load SnapshotId: {}", max_id);

        for (String path : filesPath) {
            if (path.endsWith(".ecj")) {
                String[] parts = path.split("_");
                long snapshotId = Long.parseLong(parts[2]);
                if (snapshotId == max_id) {
                    String[] baseString = parts[3].split("\\.");
                    baseSeq = Long.parseLong(baseString[0]);
                    log.info("Load BaseSeq: {}", baseSeq);
                }
            }
        }
        return new InitialStateConfiguration(exchangeCoreName, max_id, baseSeq, 0);
    }
}
