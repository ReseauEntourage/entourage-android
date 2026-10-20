package social.entourage.android.e2e

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.not
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.profile.MyProfileFullActivity

/**
 * Scénario E2E "connecté" : page « Mon profil ». Vérifie que le squelette de chargement
 * (`layout_profile_skeleton`) laisse la place au profil réel une fois ses données arrivées : le profil
 * (nom, bouton Modifier) est affiché et le squelette n'est plus visible. Lecture seule.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ProfileScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("profil")

    /** Attend (jusqu'à [timeoutMs]) que [check] ne lève plus, puis relance l'assertion pour remonter l'échec. */
    private fun waitUntil(timeoutMs: Long = 30_000, check: () -> Unit) {
        val deadline = System.currentTimeMillis() + timeoutMs
        var last: Throwable? = null
        while (System.currentTimeMillis() < deadline) {
            try {
                check()
                return
            } catch (e: NoMatchingViewException) {
                last = e
            } catch (e: AssertionError) {
                last = e
            }
            Thread.sleep(500)
        }
        throw last ?: AssertionError("Délai dépassé")
    }

    @Test
    fun leSqueletteLaisseLaPlaceAuProfil() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scenario = ActivityScenario.launch<MyProfileFullActivity>(
            Intent(context, MyProfileFullActivity::class.java)
        )
        scenario.onActivity { activity -> setUp(activity) }

        // Le profil réel finit par s'afficher (nom + bouton Modifier)…
        waitUntil { onView(withId(R.id.button_modify)).check(matches(isDisplayed())) }
        val firstName = EntourageApplication.get().me()?.firstName
        if (firstName.isNullOrBlank()) fail("E2E: le compte de test n'a pas de prénom")
        onView(withId(R.id.tv_name)).check(matches(isDisplayed()))
        screenshot.shoot("profil_charge")

        // …et le squelette n'est plus visible.
        onView(withId(R.id.profile_skeleton)).check(matches(not(isDisplayed())))
        scenario.close()
    }
}
