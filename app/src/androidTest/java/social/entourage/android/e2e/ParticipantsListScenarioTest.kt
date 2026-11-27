package social.entourage.android.e2e

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.members.MembersActivity
import social.entourage.android.members.MembersType

/**
 * Scénario E2E : la liste des participants d'un événement reste visible quand le champ de recherche
 * prend le focus sans texte. Jusqu'à Android 8 inclus, le système donne ce focus dès l'ouverture de
 * l'écran : le passage en mode recherche au focus masquait alors la liste (écran vide).
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ParticipantsListScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("participants")
    private val api get() = EntourageApplication.get().apiModule

    @Before
    fun setUpScenario() {
        // Connexion + ressource d'attente réseau, avant toute hypothèse (tearDown la désenregistre).
        setUp(ApplicationProvider.getApplicationContext<Context>())
    }

    /** Un événement à venir qui a au moins un participant (l'organisateur en fait toujours partie). */
    private fun upcomingEventWithParticipants(): Int? =
        api.eventsRequest.getAllEvents(1, 10, 100, 48.8566, 2.3522, "future").execute().body()
            ?.allEvents
            ?.mapNotNull { it.id }
            ?.firstOrNull { eventId ->
                api.eventsRequest.getMembers(eventId, 1, 30).execute().body()?.users?.isNotEmpty() == true
            }

    @Test
    fun laListeResteVisibleQuandLaRechercheALeFocusSansTexte() {
        val eventId = upcomingEventWithParticipants()
        assumeTrue("Aucun événement à venir avec des participants pour le compte de test", eventId != null)

        MembersActivity.isFromReact = false
        val intent = Intent(ApplicationProvider.getApplicationContext(), MembersActivity::class.java)
            .putExtra("ID", eventId!!)
            .putExtra("TYPE", MembersType.EVENT.code)

        ActivityScenario.launch<MembersActivity>(intent).use {
            waitForView(R.id.recycler_view, timeoutMs = 15_000)
            screenshot.shoot("liste_des_participants")

            // Focus sans texte (ce que fait Android 8 à l'ouverture) : la liste reste affichée.
            onView(withId(R.id.search_bar)).perform(click(), closeSoftKeyboard())
            onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))
            screenshot.shoot("focus_sans_texte")

            // Une recherche masque la liste...
            onView(withId(R.id.search_bar)).perform(typeText("zzzz"), closeSoftKeyboard())
            onView(withId(R.id.recycler_view)).check(matches(withEffectiveVisibility(Visibility.GONE)))
            screenshot.shoot("recherche_en_cours")

            // ...et vider le champ la fait revenir.
            onView(withId(R.id.search_bar)).perform(clearText(), closeSoftKeyboard())
            onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))
            screenshot.shoot("recherche_videe")
        }
    }
}
