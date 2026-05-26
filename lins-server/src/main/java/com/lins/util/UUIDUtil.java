package com.lins.util;

import java.util.Random;
import java.util.UUID;


/**
 * @version v2.3
 * @ClassName:UUIDUtil.java
 * @author: lins
 * @date: 2019年11月16日
 * @Description: UUIDUtil.java
 * @Copyright: 2017-2021 lins. All rights reserved.
 */
public class UUIDUtil {

    public static String getUUID() {
        return String.valueOf(UUID.randomUUID()).replace("-", "");
    }

    /**
     * 随机6位数字
     *
     * @return
     */
    public static String getRandomSix() {
        return "" + new Random().nextInt(999999);
    }

}
