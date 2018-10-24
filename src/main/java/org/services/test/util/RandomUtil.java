package org.services.test.util;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomUtil {
    public static <T> T getRandomElementInList(List<T> list) {
        // shuffle 打乱顺序
        Collections.shuffle(list);
        return list.get(0);
    }

    public static boolean getRandomTrueOrFalse() {
        Random random = new Random();
        if (random.nextInt(10) < 5) {
            return true;
        } else {
            return false;
        }
    }
}
