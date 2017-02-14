import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by metin on 08.02.2017.
 */
public class MyProxy {
    private final String mName;
    private List<String> proxies = new ArrayList<>();
    private int current = -1;

    public MyProxy(String name) {
        mName = name;
    }

    public void loadProxyList() {
        try (BufferedReader br = new BufferedReader(new FileReader(mName))) {
            HashSet<String> set = new HashSet<>();
            String line = br.readLine();

            while (line != null) {
                set.add(line);
                line = br.readLine();
            }
            proxies.add("");
            proxies.addAll(set);
            current = (int) (Math.random() * proxies.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int size() {
        return proxies.size();
    }

    public void next() {
        if (current == -1) return;
        current++;
        if (current >= proxies.size()) current = 0;
        String proxy = proxies.get(current);
        System.out.printf("proxy(%d,%d): " + proxy + "\n", current, proxies.size());
    }

    public Proxy get() {
        if (proxies.size() == 0) return Proxy.NO_PROXY;
        if (current >= proxies.size()) current = 0;
        String proxy = proxies.get(current);
        if (proxy.isEmpty()) return Proxy.NO_PROXY;
        int port = Integer.parseInt(proxy.substring(proxy.indexOf(":") + 1));
        proxy = proxy.substring(0, proxy.indexOf(":"));

        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy, port));
    }

    public void remove() {
        if (current == -1) {
            loadProxyList();
            return;
        }
        proxies.remove(current);
        current--;
        if (proxies.size() == 0) loadProxyList();
    }
}
