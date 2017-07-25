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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by metin on 06.02.2017.
 */
public class Cache {
    static File folder = new File(System.getProperty("user.home") + "/citiesfetcher-cache/");

    static {
        folder.mkdir();
    }

    public static void remove(String key) {
        new File(folder.getAbsolutePath() + "/" + key.hashCode()).delete();
    }


    public static void put(String key, String data) {
        try {
            PrintWriter out = new PrintWriter(folder.getAbsolutePath() + "/" + key.hashCode(), "UTF-8");
            out.write(data);
            out.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    static String get(String key) {
        if (!new File(folder, "" + key.hashCode()).exists()) return null;
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(folder.getAbsolutePath() + "/" + key.hashCode()));
            if (encoded.length == 0) return null;
            return new String(encoded, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
