package com.ssafy.yumyum.util;

import java.util.UUID;

public final class IdGenerator {

    private IdGenerator() {
    }

    public static String next(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
