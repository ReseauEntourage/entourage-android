package social.entourage.android.e2e

import android.Manifest
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import social.entourage.android.actions.create.CreateActionActivity
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.tools.utils.Const

/**
 * Scenario E2E "connecte" : verifie que l'ecran de charte affiche avant la creation
 * d'une demande / d'une contribution montre bien le contenu differencie attendu
 * (titre du bandeau, sections limites/consentement) selon la variante.
 * L'ecran est ferme via le bouton retour : aucune demande ni contribution n'est creee.
 *
 * Cet ecran (`CreateActionCGUFragment`) est rendu en Jetpack Compose : Espresso ne peut
 * pas traverser le contenu d'un `ComposeView` via `onView(withId(...))`/`withText(...)`.
 * On utilise donc UiAutomator (`By.res`/`findObject`), en s'appuyant sur
 * `Modifier.semantics { testTagsAsResourceId = true }` + `Modifier.testTag(...)` poses
 * cote Compose (`CreateActionCharterScreen.kt`), qui exposent les tags "icon_back",
 * "accept" et "banner_title" comme des resource-id UiAutomator.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class CreateActionCharterScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("charte_creation_action")
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
    fun charteVarianteDemande() {
        checkNoPopUpOnHome()

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, CreateActionActivity::class.java).apply {
            putExtra(Const.IS_ACTION_DEMAND, true)
        }

        val scenario = ActivityScenario.launch<CreateActionActivity>(intent)
        device.wait(Until.hasObject(By.res("banner_title")), 5_000)
        screenshot.shoot("demande_charte")

        // Texte en dur (plutôt que R.string.action_cgu_ask_title) pour ne pas coupler cette
        // branche à des strings ajoutées côté develop tant qu'elles n'ont pas été resynchronisées.
        val bannerTitle = device.findObject(By.res("banner_title"))
        assertEquals("Vous sollicitez un coup de main", bannerTitle.text)

        device.findObject(By.res("icon_back")).click()
        scenario.close()
    }

    @Test
    fun charteVarianteContribution() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, CreateActionActivity::class.java).apply {
            putExtra(Const.IS_ACTION_DEMAND, false)
        }

        val scenario = ActivityScenario.launch<CreateActionActivity>(intent)
        device.wait(Until.hasObject(By.res("banner_title")), 5_000)
        screenshot.shoot("contribution_charte")

        val bannerTitle = device.findObject(By.res("banner_title"))
        assertEquals("Vous offrez un coup de main", bannerTitle.text)

        device.findObject(By.res("icon_back")).click()
        scenario.close()
    }
}
