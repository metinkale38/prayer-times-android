/*
 * Copyright (c) 2013-2017 Metin Kale
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;

/**
 * Created by metin on 27.02.2017.
 */

public class CompressTSF {
    public static void main(String args[]) throws IOException {
        File[] files = new File("app/src/main/res/raw").listFiles((dir, name) -> name.endsWith(".tsv"));

        for (File file : files) {
            System.out.println("Compress " + file.getAbsolutePath());
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath() + ".oos"));
            BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsolutePath())));
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

    }

    private static class MyFastTokenizer {
        private final String delim;
        private final int dsize;
        private String str;
        private int start = 0;

        MyFastTokenizer(String str, String delim) {
            this.str = str;
            this.delim = delim;
            this.dsize = delim.length();
        }

        String nextString() {
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

        double nextDouble() {
            String str = nextString();
            if (str.isEmpty()) return 0;
            return FastParser.parseDouble(str);
        }

        int nextInt() {
            String str = nextString();
            if (str.isEmpty()) return 0;
            return FastParser.parseInt(str);
        }


    }


}