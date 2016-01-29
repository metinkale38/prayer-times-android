package com.metinkale.prayerapp.hadis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

class Shuffled
{
    private static Random random = new Random(5);
    private static List<Integer> list = new ArrayList<>();

    public static List<Integer> getList()
    {
        if(list.isEmpty())
        {
            int c = SqliteHelper.get().getCount();
            for(int i = 1; i <= c + 1; i++)
            {
                list.add(i);
            }

            Collections.shuffle(list, random);
            list.remove((Integer) list.size());
        }

        return list;
    }

}
