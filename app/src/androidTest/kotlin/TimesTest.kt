import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.metinkale.prayer.App
import com.metinkale.prayer.Preferences
import com.metinkale.prayer.base.BuildConfig
import com.metinkale.prayer.times.MainActivity
import com.metinkale.prayer.times.R
import com.metinkale.prayer.times.times.Times
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
class TimesTest {

    @get:Rule
    var permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setUp() {
        PreferenceManager.getDefaultSharedPreferences(App.get()).edit().clear().commit()
        Times.Companion.deleteAll()

        Preferences.SHOW_INTRO = false
        Preferences.CHANGELOG_VERSION = BuildConfig.CHANGELOG_VERSION
    }

    @Test
    fun testDiyanet() {
        ActivityScenario.launch(MainActivity::class.java)

        onView(withText("Cities")).perform(click())

        onView(withId(R.id.addCity)).perform(click())

        waitForDialog()


        onView(withSubstring("City Chooser")).perform(click())
        waitForDialog()
        onView(withText("Diyanet")).perform(click())
        waitForDialog()
        onView(withText("Turkey")).perform(click())
        waitForDialog()
        onView(withText("Kayseri")).perform(click())
        waitForDialog()
        onView(withText("Develi")).perform(click())

        onView(withText("Cities")).check(matches(ViewMatchers.isDisplayed()))
        onView(withSubstring("Develi")).perform(click())

        onView(withId(R.id.fajrTime)).check(matches(not(withText("00:00"))))
    }

    private fun waitForDialog() {
        var count = 0
        onView(withId(R.id.progressDlg)).let {

            while (ViewMatchers.isDisplayed().matches(it)) {
                Thread.sleep(100)
                count++

                if (count > 50) throw RuntimeException("waiting for ProgressDialog for more than 5s")
            }
        }
    }
}