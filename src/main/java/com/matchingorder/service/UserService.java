package com.matchingorder.service;

import com.matchingorder.ExchangeApi;
import com.matchingorder.ExchangeCore;
import com.matchingorder.common.CommonResource;
import com.matchingorder.common.api.ApiAddUser;
import com.matchingorder.common.api.ApiAdjustUserBalance;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final ExchangeCore exchangeCore;

    private final CommonResource commonResource;

    @SneakyThrows
    public void loadUserSettings() {
        // get exchange API for publishing commands
        ExchangeApi api = exchangeCore.getApi();
        commonResource.getListAccount().forEach(uid -> {
            api.submitCommandAsync(ApiAddUser.builder()
                    .uid(uid)
                    .build());

            // first user deposits 20 LTC
            api.submitCommandAsync(ApiAdjustUserBalance.builder()
                    .uid(uid)
                    .currency(15)
                    .amount(9_000_000_000_000L)
                    .transactionId(1L)
                    .build());
        });
    }
}
