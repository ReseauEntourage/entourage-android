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
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
 *
 * Depuis le retour de recette EN-9620, le lien "Lire la charte complète" et le bouton
 * "J'ai compris, je continue" terminent le contenu scrollable (plus de footer sticky) :
 * il faut faire défiler "charter_scroll" pour les atteindre, cf. [scrollCharterToAccept].
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

        assertCtaAtBottomOfScroll()
        screenshot.shoot("demande_charte_bas")

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

        assertCtaAtBottomOfScroll()
        screenshot.shoot("contribution_charte_bas")

        device.findObject(By.res("icon_back")).click()
        scenario.close()
    }

    /** Le lien vers la charte complète et le CTA sont atteignables en bas de la zone scrollable. */
    private fun assertCtaAtBottomOfScroll() {
        device.scrollCharterToAccept()
        assertNotNull(device.findObject(By.res("read_full_charter")))
    }
}

/**
 * Fait défiler la charte jusqu'au bouton "accept" (placé en fin de contenu scrollable
 * depuis EN-9620) et le retourne. Partagé avec [CreateActionFullFlowScenarioTest].
 */
internal fun UiDevice.scrollCharterToAccept(): UiObject2 {
    wait(Until.hasObject(By.res("charter_scroll")), 5_000)
    findObject(By.res("charter_scroll"))
        ?.scrollUntil(Direction.DOWN, Until.findObject(By.res("accept")))
    return checkNotNull(findObject(By.res("accept"))) {
        "Bouton \"accept\" introuvable après défilement de la charte"
    }
}
