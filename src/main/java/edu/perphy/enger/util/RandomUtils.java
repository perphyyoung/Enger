package edu.perphy.enger.util;

import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by perphy on 2016/4/15 0015.
 * 随机数工具类
 */
public class RandomUtils {
    public static int getRandom(int min, int max) {
        return new Random().nextInt(max) + min;
    }

    public static int[] getRandoms(int min, int max, int count) {
        int[] randoms = new int[count];
        for (int i = 0; i < count; i++) {
            randoms[i] = getRandom(min, max);
        }
        return randoms;
    }

    public static int[] getUniqueRandoms(int min, int max, int count) {
        ArrayList<Integer> al = new ArrayList<>(count);
        while (al.size() < count) {
            int random = getRandom(min, max);
            if (!al.contains(random)) {
                al.add(random);
            }
        }
        Collections.shuffle(al); // 貌似不太随机，再重新洗牌一次
        return Ints.toArray(al);
    }
}
