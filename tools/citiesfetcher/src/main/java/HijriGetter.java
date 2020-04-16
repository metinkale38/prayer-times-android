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

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

public class HijriGetter {
    
    //QUICK AND DIRTY :)
    public static void main(String[] args) {
        for (int y = 2014; y <= 2022; y++) {
            String url = "https://www2.diyanet.gov.tr/DinHizmetleriGenelMudurlugu/Sayfalar/" + y + "DiniGunlerListesi.aspx";
            if (y >= 2016)
                url = url.replace("DiniGunlerListesi", URLEncoder.encode("DiniGünlerListesi")); // well done diyanet, see u/ü :)
            String data = HTTP.get(url);
            data = data.substring(data.indexOf("<tbody>"));
            data = data.substring(0, data.indexOf("</tbody>")).replace("<td", "µ<td").replace("<tr", "$<tr").replace("&#160;", "");
            data = data.replaceAll("<[^>]*>", "").replaceAll("\\s+", "").replace(" ", "").replace("$", "\n");
            data = data.substring(data.indexOf("\n") + 1);
            data = data.substring(data.indexOf("\n") + 1);
            data = data.substring(data.indexOf("\n") + 1);
            data = data.replace("CEMAZİYEVVEL", "C.EVVEL");
            data = data.replace("CEMAZİYELAHİR", "C.AHİR");
            data = data.replace("REBİÜLEVVEL", "R.EVVEL");
            data = data.replace("REBİÜLAHİR", "R.AHİR");
            data = data.replace("\u200B", "");
            String[] rows = data.split("\n");
            
            List<String> greg =
                    Arrays.asList("OCAK", "ŞUBAT", "MART", "NİSAN", "MAYIS", "HAZİRAN", "TEMMUZ", "AĞUSTOS", "EYLÜL", "EKİM", "KASIM", "ARALIK");
            List<String> hijri =
                    Arrays.asList("MUHARREM", "SAFER", "R.EVVEL", "R.AHİR", "C.EVVEL", "C.AHİR", "RECEB", "ŞABAN", "RAMAZAN", "ŞEVVAL", "ZİLKADE",
                            "ZİLHİCCE");
            for (String row : rows) {
                String[] cols = row.split("µ");
                if (!rzero(cols[1]).equals("1"))
                    continue;
                System.out.print(rzero(cols[1]) + "\t");
                System.out.print((hijri.indexOf(cols[2]) + 1) + "\t");
                if (hijri.indexOf(cols[2]) == -1) {
                    System.err.println(cols[2]);
                }
                System.out.print(rzero(cols[3]) + "\t");
                System.out.print(rzero(cols[4]) + "\t");
                System.out.print((greg.indexOf(cols[5].split("-")[0]) + 1) + "\t");
                if (greg.indexOf(cols[5].split("-")[0]) == -1) {
                    System.err.println(cols[5].split("-")[0]);
                }
                System.out.println(cols[5].split("-")[1]);
            }
        }
        
    }
    
    static String rzero(String str) {
        while (str.charAt(0) == '0') {
            str = str.substring(1);
        }
        return str;
    }
}
    
    
