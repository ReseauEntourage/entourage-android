package social.entourage.android.e2e

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.beforeLogin.EntourageTestBeforeLogin
import social.entourage.android.onboarding.login.LoginActivity

/**
 * Scénario E2E "non connecté" : connexion avec le compte de test dédié.
 * Un screenshot est pris à chaque vue / action pour vérifier visuellement le déroulé.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ConnexionScenarioTest : EntourageTestBeforeLogin() {

    private val screenshot = E2EScreenshot("connexion")

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
        checkFirstConnectionScreen()
    }

    private fun closeAutofill() {
        activityRule.scenario.onActivity { activity ->
            closeAutofill(activity)
        }
    }

    @Test
    fun connexion() {
        screenshot.shoot("ecran_de_connexion")

        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone)).perform(
            ViewActions.typeText(E2ECredentials.PHONE),
            ViewActions.closeSoftKeyboard()
        )
        closeAutofill()
        screenshot.shoot("numero_de_telephone_saisi")

        Espresso.onView(ViewMatchers.withId(R.id.ui_login_et_code)).perform(
            ViewActions.typeText(E2ECredentials.PASSWORD),
            ViewActions.closeSoftKeyboard()
        )
        closeAutofill()
        screenshot.shoot("mot_de_passe_saisi")

        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_signup))
            .perform(ViewActions.click())
        Espresso.onIdle()
        screenshot.shoot("apres_clic_sur_connexion")

        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
        screenshot.shoot("connexion_reussie")
    }
}
