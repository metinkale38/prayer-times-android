package com.metinkale.prayerapp.hadis;

import java.util.*;

class Shuffled {
    private static Random random = new Random(5);
    private static List<Integer> list = new ArrayList<>();

    public static Collection<Integer> getList() {
        if (list.isEmpty()) {
            int c = SqliteHelper.get().getCount();
            for (int i = 1; i <= (c + 1); i++) {
                list.add(i);
            }

            Collections.shuffle(list, random);
            list.remove((Integer) list.size());
        }

        return list;
    }

}
