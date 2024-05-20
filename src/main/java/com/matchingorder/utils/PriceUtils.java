package com.matchingorder.utils;


import com.matchingorder.constant.CommonConstants;

import java.math.BigDecimal;

public class PriceUtils {
    public static long convertPriceToLong(BigDecimal price) {
        return price.multiply(new BigDecimal(CommonConstants.CONTRACT_SIZE)).longValue();
    }

    public static long convertStringToPrice(String price) {
        BigDecimal temp = new BigDecimal(price);
        return temp.multiply(new BigDecimal(CommonConstants.CONTRACT_SIZE)).longValue();
    }

    /**
     * Convert amount/balance from RPC to MTS internal amount
     * Because we multiply price with CONTRACT_SIZE so we have to multiply balance as well
     * @param amount - amount from RPC
     * @return long for store in MTS
     */
    public static long convertStringToBalance(String amount) {
        long mtsAmount = Long.parseLong(amount);
        return mtsAmount * CommonConstants.CONTRACT_SIZE;
    }

    /**
     * When return balance to RPC, we have to divided for CONTRACT_SIZE
     * @param mtsAmount - balance from MTS core
     * @return balance as RPC string
     */
    public static String convertMTSBalanceToRPCString(long mtsAmount) {
        return String.valueOf(mtsAmount / CommonConstants.CONTRACT_SIZE);
    }

    /**
     * When return price to RPC, we have to divided for CONTRACT_SIZE
     * @param price - price from MTS core
     * @return price as RPC string
     */
    public static String convertMTSPriceToRPCString(long price) {
        BigDecimal result = new BigDecimal(price).divide(BigDecimal.valueOf(CommonConstants.CONTRACT_SIZE), 3, BigDecimal.ROUND_FLOOR);
        return String.valueOf(result);
    }

}
