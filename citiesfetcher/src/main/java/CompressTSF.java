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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;

/**
 * Created by metin on 27.02.2017.
 */

public class CompressTSF {
    public static void main(String args[]) throws IOException {
        new File("cities").delete();
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("cities"));
        BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream("cities.tsv")));
        String line;
        while ((line = is.readLine()) != null) {
            if (line.isEmpty()) continue;
            MyFastTokenizer st = new MyFastTokenizer(line, "\t");
            os.writeInt(st.nextInt());
            os.writeInt(st.nextInt());
            os.writeFloat((float) st.nextDouble());
            os.writeFloat((float) st.nextDouble());
            os.writeUTF(st.nextString());
            os.writeUTF(st.nextString());
        }
        os.close();
        is.close();
    }

    private static class MyFastTokenizer {
        private final String delim;
        private final int dsize;
        private String str;
        private int start = 0;

        public MyFastTokenizer(String str, String delim) {
            this.str = str;
            this.delim = delim;
            this.dsize = delim.length();
        }

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
            return parseDouble(str);
        }

        public int nextInt() {
            String str = nextString();
            if (str.isEmpty()) return 0;
            return parseInt(str);
        }

        /**
         * NO CHECKING IS DONE!!!
         *
         * @param s string
         * @return long value
         */
        private static int parseInt(String s) {
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
        private static double parseDouble(String s) {
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

}