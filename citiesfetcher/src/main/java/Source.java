
/**
 * Created by metin on 03.04.2016.
 */
public enum Source {
    Diyanet("Diyanet.gov.tr"),
    Fazilet("FaziletTakvimi.com"),
    IGMG("IGMG.org"),
    Semerkand("SemerkandTakvimi.com"),
    NVC("NamazVakti.com"),
    Morocco("habous.gov.ma"),
    Malaysia("e-solat.gov.my");

    public String text;

    Source(String text) {
        this.text = text;
    }

}