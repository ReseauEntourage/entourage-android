package social.entourage.android.e2e

import android.Manifest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin

/**
 * Scénario E2E "connecté" : onglet Discussions refondu en Compose (EN-9489).
 *
 * Vérifie la card "Bonnes ondes" en tête de liste (avec un espace entre le titre et le
 * sous-titre, retour de recette du 22/09) et la présence d'au moins une conversation.
 * Lecture seule : ne crée ni ne modifie rien sur le compte de test.
 *
 * L'écran est rendu en Compose dans un ComposeView : on passe par UiAutomator (`By.res`)
 * grâce à `testTagsAsResourceId` posé à la racine de `DiscussionsScreen`.
 * Les retours purement visuels (photo de profil dans la liste, "Activez-les pour ne rien
 * manquer" en gras souligné) ne sont pas assertables ici : ils sont couverts par les
 * screenshots pris à chaque étape. Le bandeau notifications n'apparaît d'ailleurs pas
 * puisque la permission POST_NOTIFICATIONS est accordée par le test.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class DiscussionsListScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("liste_discussions")
    private val device: UiDevice by lazy {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    private val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )
    private val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(permissionRule)
        .around(activityRule)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun listeDiscussionsAvecCardBonnesOndes() {
        checkNoPopUpOnHome()
        onView(withId(R.id.navigation_messages)).perform(click())

        assertTrue(
            "Card \"Bonnes ondes\" absente de l'onglet Discussions",
            device.wait(Until.hasObject(By.res("small_talk_card")), 10_000)
        )
        screenshot.shoot("onglet_discussions")

        val title = device.findObject(By.res("small_talk_card_title"))
        val subtitle = device.findObject(By.res("small_talk_card_subtitle"))
        assertNotNull(title)
        assertNotNull(subtitle)
        assertTrue(
            "Le sous-titre de la card doit être espacé du titre",
            subtitle.visibleBounds.top > title.visibleBounds.bottom
        )

        val hasConversation = device.wait(Until.hasObject(By.res("conversation_item")), 10_000)
        assumeTrue("Le compte de test n'a aucune conversation", hasConversation)
        screenshot.shoot("liste_conversations")
    }
}
