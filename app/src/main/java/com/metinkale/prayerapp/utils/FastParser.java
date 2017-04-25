/*
 * Copyright (c) 2016 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.metinkale.prayerapp.utils;

/**
 * Created by metin on 05.03.2017.
 */

public class FastParser {
    /**
     * NO CHECKING IS DONE!!!
     *
     * @param s string
     * @return long value
     */
    public static int parseInt(String s) {
        if (s.startsWith("-")) {
            return -parseInt(s.substring(1));
        }
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            val *= 10;
            val += (s.charAt(i) - '0');
        }
        return val;
    }

    /**
     * NO CHECKING IS DONE!!! UNSAFE
     *
     * @param s string
     * @return double value
     */
    public static double parseDouble(String s) {
        if (s.startsWith("-")) {
            return -parseDouble(s.substring(1));
        }

        long full = 0;
        long dec = 0;
        long c = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (c == 0) {
                if (ch == '.' || ch == ',') {
                    c++;
                } else {
                    full *= 10;
                    full += (ch - '0');
                }
            } else {
                dec *= 10;
                dec += (ch - '0');
                c *= 10;
            }
        }
        return full + dec / (double) c;
    }
}
