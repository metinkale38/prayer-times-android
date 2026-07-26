import com.metinkale.prayer.date.HijriDate
import com.metinkale.prayer.date.HijriDay.LAST_RAMADAN
import com.metinkale.prayer.date.HijriDay.RAGAIB
import com.metinkale.prayer.date.HijriMonth.*
import com.metinkale.prayer.date.toHijriDate
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

class HijriTest {

    init {
        HijriDate.openTSVFile = { File("src/main/res/raw/hijri.tsv").inputStream() }
    }

    @Test
    fun testConversion() {
        HijriDate.of(1444, 5, 1).toLocalDate() shouldBe LocalDate.of(2022, 11, 25)
        HijriDate.of(1444, 10, 29).toLocalDate() shouldBe LocalDate.of(2023, 5, 19)
        LocalDate.of(2023, 5, 19).toHijriDate() shouldBe HijriDate.of(1444, 10, 29)
    }

    @Test
    fun testNow() {
        HijriDate.now().toLocalDate() shouldBe LocalDate.now()
    }

    @Test
    fun testPlus() {
        HijriDate.of(1444, 5, 5).plusDays(1) shouldBe HijriDate.of(1444, 5, 6)

        HijriDate.of(1444, 5, 1) shouldBe HijriDate.of(1444, 5, 1)
        HijriDate.of(1444, 5, 1).plusDays(1) shouldBe HijriDate.of(1444, 5, 2)
        HijriDate.of(1444, 5, 1).minusDays(1) shouldBe HijriDate.of(1444, 4, 29)

        HijriDate.of(1444, 4, 30).plusDays(1) shouldBe HijriDate.of(1444, 5, 2)
    }

    @Test
    fun testRegaib() {
        val list = HijriDate.getHolydaysForHijriYear(1444)
        list.first { it.second == RAGAIB }.first shouldBe HijriDate.of(1444, RAJAB, 4)
    }

    @Test
    fun testLastRamadan() {
        val list = HijriDate.getHolydaysForHijriYear(1444)
        list.first { it.second == LAST_RAMADAN }.first.month shouldBe RAMADAN
        list.first { it.second == LAST_RAMADAN }.first.plusDays(1).month shouldBe SHAWWAL
    }

    @Test
    fun testPrior1433() {
        LocalDate.of(1996, 8, 6).toHijriDate() shouldBe HijriDate.of(1417, RABIAL_AWWAL, 21)
        HijriDate.of(1417, RABIAL_AWWAL, 21).toLocalDate() shouldBe LocalDate.of(1996, 8, 6)
    }

}