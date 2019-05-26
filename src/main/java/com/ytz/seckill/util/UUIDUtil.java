package com.ytz.seckill.util;

import java.util.UUID;

public class UUIDUtil {
    //生成随机的uuid
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
