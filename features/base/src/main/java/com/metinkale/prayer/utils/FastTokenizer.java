/*
 * Copyright (c) 2013-2019 Metin Kale
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
 */

package com.metinkale.prayer.utils;

import androidx.annotation.NonNull;

public class FastTokenizer {
    @NonNull
    private final String delim;
    private final int dsize;
    private final String str;
    private int start = 0;

    public FastTokenizer(String str, @NonNull String delim) {
        this.str = str;
        this.delim = delim;
        this.dsize = delim.length();
    }

    @NonNull
    public String nextString() {
        int size = str.indexOf(delim, start);
        if (size < 0) {
            size = str.length();
        }
        try {
            return str.substring(start, size);
        } finally {
            start = size + dsize;
        }
    }

    public double nextDouble() {
        String str = nextString();
        if (str.isEmpty()) return 0;
        return FastParser.parseDouble(str);
    }

    public int nextInt() {
        String str = nextString();
        if (str.isEmpty()) return 0;
        return FastParser.parseInt(str);
    }


}
