
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.metinkale.prayer.App
import com.metinkale.prayer.times.MainActivity
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
class IntroTest {

    @Before
    fun setUp() {
        PreferenceManager.getDefaultSharedPreferences(App.get()).edit().clear().commit()

    }

    @Test
    fun testIntro() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withSubstring("English")).check(matches(isDisplayed()))
        onView(withSubstring("Türkçe")).check(matches(isDisplayed()))
        onView(withSubstring("Deutsch")).check(matches(isDisplayed()))

        onView(withSubstring("Türkçe")).perform(click())
        onView(withText("Dil")).check(matches(isDisplayed()))

        onView(withSubstring("Deutsch")).perform(click())
        onView(withText("Sprache")).check(matches(isDisplayed()))


        onView(withSubstring("English")).perform(click())
        onView(withText("Language")).check(matches(isDisplayed()))

        onView(withText("CONTINUE")).perform(click())
        onView(withSubstring("What's new?")).perform(click())

        onView(withText("CONTINUE")).perform(click())
        onView(withSubstring("Through the Drawer Menu")).perform(click())

        onView(withText("CONTINUE")).perform(click())
        onView(withSubstring("add as many cities")).perform(click())

        onView(withText("CONTINUE")).perform(click())
        onView(withSubstring("By swiping")).perform(click())

        onView(withText("CONTINUE")).perform(click())
        onView(withSubstring("metinkale38")).perform(click())

        onView(withText("DONE")).perform(click())

        onView(withText("Cities")).check(matches(isDisplayed()))
    }
}