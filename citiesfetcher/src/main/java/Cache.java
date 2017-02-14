import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by metin on 06.02.2017.
 */
public class Cache {
    static {
        new File("cache").mkdir();
    }

    public static void remove(String key) {
        new File("cache/" + key.hashCode()).delete();
    }


    public static void put(String key, String data) {
        try {
            PrintWriter out = new PrintWriter(System.getProperty("user.home") + "/cache/" + key.hashCode(), "UTF-8");
            out.write(data);
            out.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    static String get(String key) {
        if (!new File("cache/" + key.hashCode()).exists()) return null;
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get("cache/" + key.hashCode()));
            if (encoded.length == 0) return null;
            return new String(encoded, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
