package edu.perphy.enger.util;

import com.google.common.primitives.Ints;

import java.util.HashSet;

/**
 * Created by perphy on 2016/4/15 0015.
 * 随机数工具类
 */
public class RandomUtils {
    public static int getRandom(int min, int max) {
        return (int) (Math.random() * max + min);
    }

    public static int[] getRandoms(int min, int max, int count) {
        int[] randoms = new int[count];
        for (int i = 0; i < count; i++) {
            randoms[i] = getRandom(min, max);
        }
        return randoms;
    }

    public static int[] getUniqueRandoms(int min, int max, int count) {
        HashSet<Integer> hs = new HashSet<>(count);
        while (hs.size() < count) {
            int random = getRandom(min, max);
            if (!hs.contains(random)) {
                hs.add(random);
            }
        }
        return Ints.toArray(hs);
    }
}
