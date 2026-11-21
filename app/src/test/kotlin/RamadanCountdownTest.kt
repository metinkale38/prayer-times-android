import com.metinkale.prayerapp.vakit.RamadanCountdownTarget
import com.metinkale.prayerapp.vakit.ramadanCountdownTarget
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class RamadanCountdownTest {
    private val date = LocalDateTime.of(2027, 2, 10, 0, 0)
    private val previousIftar = date.minusDays(1).withHour(18).withMinute(45)
    private val fajr = date.withHour(5).withMinute(30)
    private val iftar = date.withHour(18).withMinute(45)
    private val nextFajr = fajr.plusDays(1)

    @Test
    fun beforeImsakCountsToImsak() {
        val result = ramadanCountdownTarget(
            now = date.withHour(3),
            previousIftar = previousIftar,
            fajr = fajr,
            iftar = iftar,
            nextFajr = nextFajr
        )
        result shouldBe RamadanCountdownTarget(fajr, false)
    }

    @Test
    fun atImsakCountsToIftar() {
        val result = ramadanCountdownTarget(
            now = fajr,
            previousIftar = previousIftar,
            fajr = fajr,
            iftar = iftar,
            nextFajr = nextFajr
        )
        result shouldBe RamadanCountdownTarget(iftar, true)
    }

    @Test
    fun duringDayCountsToIftar() {
        val result = ramadanCountdownTarget(
            now = date.withHour(12),
            previousIftar = previousIftar,
            fajr = fajr,
            iftar = iftar,
            nextFajr = nextFajr
        )
        result shouldBe RamadanCountdownTarget(iftar, true)
    }

    @Test
    fun atIftarCountsToNextImsak() {
        val result = ramadanCountdownTarget(
            now = iftar,
            previousIftar = previousIftar,
            fajr = fajr,
            iftar = iftar,
            nextFajr = nextFajr
        )
        result shouldBe RamadanCountdownTarget(nextFajr, false)
    }

    @Test
    fun afterIftarCountsToNextImsak() {
        val result = ramadanCountdownTarget(
            now = date.withHour(23),
            previousIftar = previousIftar,
            fajr = fajr,
            iftar = iftar,
            nextFajr = nextFajr
        )
        result shouldBe RamadanCountdownTarget(nextFajr, false)
    }

    @Test
    fun afterMidnightBeforeImsakCountsToImsak() {
        val result = ramadanCountdownTarget(
            now = date.withHour(1),
            previousIftar = previousIftar,
            fajr = fajr,
            iftar = iftar,
            nextFajr = nextFajr
        )
        result shouldBe RamadanCountdownTarget(fajr, false)
    }

    @Test
    fun lateIftarAfterMidnightWinsOverImsak() {
        val lateIftar = date.withHour(0).withMinute(30)
        val earlyFajr = date.withHour(2)

        val result = ramadanCountdownTarget(
            now = date.withHour(0).withMinute(10),
            previousIftar = lateIftar,
            fajr = earlyFajr,
            iftar = iftar,
            nextFajr = nextFajr
        )
        result shouldBe RamadanCountdownTarget(lateIftar, true)
    }
}
