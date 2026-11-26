package social.entourage.android.suggestion

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R

/**
 * Zones « Votre prochain pas » de la home (EN-9473), rendues seules avec des données factices : aucune
 * dépendance au réseau ni au compte de test. Couvre le titre de zone, la card, ses boutons, le « Pourquoi »,
 * la zone Autres et le masquage quand le moteur ne propose rien.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class SuggestionZonesComposeTest {

    @get:Rule
    val compose = createComposeRule()

    private fun string(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    private val group = NextStepSuggestion(
        id = "next-step-1",
        typeKey = SuggestionType.GROUP.key,
        title = "Voisins Ménilmontant",
        cta = SuggestionCta.JOIN,
        targetId = 7,
        engineId = 1,
        reasons = listOf(SuggestionReason("near_you", "Ce groupe est dans votre quartier.")),
    )

    private val access = ExplorerAccess(
        id = "other-outing-9",
        typeKey = SuggestionType.EVENT.key,
        label = "Café solidaire du 11e",
        context = "Samedi 14 juin",
        tab = ExplorerTab.EVENTS,
    )

    private class Recorder {
        val cta = mutableListOf<String>()
        val later = mutableListOf<String>()
        val body = mutableListOf<String>()
        val shown = mutableListOf<String>()
        val whyOpened = mutableListOf<String>()
    }

    private fun showHero(ui: HeroUi, recorder: Recorder = Recorder()): Recorder {
        compose.setContent {
            SuggestionHeroZone(
                ui = ui,
                onCta = { recorder.cta += it.id },
                onLater = { recorder.later += it.id },
                onBody = { recorder.body += it.id },
                onShown = { recorder.shown += it.id },
                onWhyOpened = { recorder.whyOpened += it.id },
            )
        }
        return recorder
    }

    @Test
    fun heroAfficheLeTitreDeZoneLaCardEtSonBoutonDAction() {
        val recorder = showHero(HeroUi.Ready(group))

        compose.onNodeWithText(string(R.string.home_suggestion_hero_title)).assertIsDisplayed()
        compose.onNodeWithText("Voisins Ménilmontant").assertIsDisplayed()
        compose.onNodeWithText(string(R.string.home_suggestion_cta_join)).assertIsDisplayed()
        compose.waitForIdle()
        assertEquals("L'affichage est signalé une fois", listOf("next-step-1"), recorder.shown)
    }

    @Test
    fun heroUtiliseLeLibelleDeLaMaquette() {
        showHero(HeroUi.Ready(group))
        compose.onNodeWithText("Une idée de rencontre pour vous").assertIsDisplayed()
    }

    @Test
    fun toucherLeBoutonDActionOuvreLaSuggestion() {
        val recorder = showHero(HeroUi.Ready(group))
        compose.onNodeWithText(string(R.string.home_suggestion_cta_join)).performClick()
        assertEquals(listOf("next-step-1"), recorder.cta)
    }

    @Test
    fun plusTardTransmetLaSuggestion() {
        val recorder = showHero(HeroUi.Ready(group))
        compose.onNodeWithText(string(R.string.home_suggestion_later)).performClick()
        assertEquals(listOf("next-step-1"), recorder.later)
    }

    @Test
    fun pourquoiOuvreLaFeuilleDExplication() {
        val recorder = showHero(HeroUi.Ready(group))
        // Le bouton « ? » porte son libellé comme étiquette de clic (onClickLabel), pas comme contentDescription.
        val whyLabel = string(R.string.home_suggestion_why_button_description)
        compose.onNode(
            SemanticsMatcher("clic « $whyLabel »") {
                it.config.getOrNull(SemanticsActions.OnClick)?.label == whyLabel
            },
            useUnmergedTree = true,
        ).performClick()
        compose.onNodeWithText("Ce groupe est dans votre quartier.").assertIsDisplayed()
        assertEquals(listOf("next-step-1"), recorder.whyOpened)
    }

    @Test
    fun sansRaisonPasDeBoutonPourquoi() {
        showHero(HeroUi.Ready(group.copy(reasons = emptyList())))
        compose.onAllNodesWithContentDescription(string(R.string.home_suggestion_why_button_description))
            .assertCountEquals(0)
    }

    @Test
    fun heroMasqueAfficheRien() {
        showHero(HeroUi.Hidden)
        compose.onAllNodesWithText(string(R.string.home_suggestion_hero_title)).assertCountEquals(0)
    }

    @Test
    fun heroEnChargementAfficheLeTitreSansCard() {
        showHero(HeroUi.Loading)
        compose.onNodeWithText(string(R.string.home_suggestion_hero_title)).assertIsDisplayed()
        compose.onAllNodesWithText(string(R.string.home_suggestion_later)).assertCountEquals(0)
    }

    @Test
    fun zoneAutresAfficheSesLignesEtOuvreLaPiste() {
        val opened = mutableListOf<String>()
        val shown = mutableListOf<List<String>>()
        compose.setContent {
            SuggestionExplorerZone(
                ui = ExplorerUi.Ready(listOf(access)),
                onAccess = { opened += it.id },
                onShown = { list -> shown += list.map { it.id } },
            )
        }

        compose.onNodeWithText("D'autres occasions de se retrouver").assertIsDisplayed()
        // La ligne est lue d'un bloc (titre, contexte, « Ouvre la suggestion ») : le texte n'est que dans sa description.
        compose.onNodeWithContentDescription("Samedi 14 juin", substring = true).assertIsDisplayed()
        compose.onNodeWithContentDescription("Café solidaire du 11e", substring = true).performClick()
        compose.waitForIdle()
        assertEquals(listOf("other-outing-9"), opened)
        assertEquals(listOf(listOf("other-outing-9")), shown)
    }

    @Test
    fun zoneAutresMasqueeMasqueAussiLeTitre() {
        compose.setContent { SuggestionExplorerZone(ui = ExplorerUi.Hidden, onAccess = {}, onShown = {}) }
        compose.onAllNodesWithText(string(R.string.home_suggestion_explorer_title)).assertCountEquals(0)
    }

    @Test
    fun seuleLaZoneAutresRemonteQuandLeMoteurNAPasDeProchainPas() {
        compose.setContent {
            SuggestionHeroZone(ui = HeroUi.Hidden, onCta = {}, onLater = {}, onBody = {}, onShown = {})
            SuggestionExplorerZone(ui = ExplorerUi.Ready(listOf(access)), onAccess = {}, onShown = {})
        }
        compose.onAllNodesWithText(string(R.string.home_suggestion_hero_title)).assertCountEquals(0)
        compose.onNodeWithText(string(R.string.home_suggestion_explorer_title)).assertIsDisplayed()
    }
}
