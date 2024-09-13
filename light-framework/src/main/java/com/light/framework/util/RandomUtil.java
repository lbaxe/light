package com.light.framework.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
    private static final char[] letterAndDigits = "abcdefghijkmnopqrstuvwsyz0123456789".toCharArray();

    public static String randomLetterOrDigit(int length) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < length; j++)
            builder.append(letterAndDigits[ThreadLocalRandom.current().nextInt(letterAndDigits.length)]);
        return builder.toString();
    }
}
