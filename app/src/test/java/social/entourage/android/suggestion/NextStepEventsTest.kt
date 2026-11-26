package social.entourage.android.suggestion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Événements Firebase « Prochain Pas » (EN-9892) : nom et paramètres, et ce qui ne doit pas être loggué. */
@OptIn(ExperimentalCoroutinesApi::class)
class NextStepEventsTest {

    private class ScriptedProvider(
        var hero: NextStepSuggestion?,
        var explorer: List<ExplorerAccess> = emptyList(),
        var fetchFails: Boolean = false,
        var dismissFails: Boolean = false,
        var next: NextStepSuggestion? = null,
    ) : SuggestionProvider {
        override suspend fun fetch(): SuggestionPayload {
            if (fetchFails) error("réseau")
            return SuggestionPayload(hero = hero, explorer = explorer)
        }

        override suspend fun dismiss(suggestion: NextStepSuggestion): NextStepSuggestion? {
            if (dismissFails) error("réseau")
            return next
        }

        override suspend fun report(interaction: SuggestionInteraction) = Unit
    }

    private val logged = mutableListOf<Pair<String, Map<String, Any>>>()

    private fun vm(provider: SuggestionProvider) =
        HomeSuggestionViewModel(provider) { event, params -> logged += event to params }

    private fun events(name: String) = logged.filter { it.first == name }.map { it.second }

    private fun outing(engineId: Int = 5, targetId: Int? = 42) = NextStepSuggestion(
        id = "next-step-$engineId", typeKey = SuggestionType.EVENT.key, title = "t", cta = SuggestionCta.PARTICIPATE,
        targetId = targetId, engineId = engineId,
    )

    private fun explorerRow(kind: SuggestionType, targetId: Int?) = NextStepSuggestion(
        id = "other-${kind.key}-$targetId", typeKey = kind.key, title = "t", cta = SuggestionCta.JOIN, targetId = targetId,
    ).let { ExplorerAccess(id = it.id, typeKey = it.typeKey, label = "l", tab = ExplorerTab.GROUPS, suggestion = it) }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    // ------------------------------------------------------------ later / cancelled / ignored

    @Test
    fun laterTappedCarriesKindAndTargetIdAsLong() {
        val vm = vm(ScriptedProvider(outing()))
        vm.refresh()
        vm.later(outing())
        assertEquals(listOf(mapOf<String, Any>("kind" to "outing", "target_id" to 42L)), events("next_step_later_tapped"))
        assertTrue(events("next_step_ignored").isEmpty())
    }

    @Test
    fun cancelLogsCancelledAndNeverIgnored() {
        val provider = ScriptedProvider(outing())
        val vm = vm(provider)
        vm.refresh()
        vm.later(outing())
        vm.undoLater()
        vm.confirmLater() // rien à confirmer après l'annulation
        assertEquals(listOf(mapOf<String, Any>("kind" to "outing", "target_id" to 42L)), events("next_step_later_cancelled"))
        assertTrue(events("next_step_ignored").isEmpty())
    }

    @Test
    fun undoWithoutLaterLogsNothing() {
        vm(ScriptedProvider(null)).undoLater()
        assertTrue(logged.isEmpty())
    }

    @Test
    fun ignoredIsLoggedAfterTheEngineAnswersAndOnlyOnce() {
        val vm = vm(ScriptedProvider(outing(), next = outing(engineId = 6)))
        vm.refresh()
        vm.later(outing())
        assertTrue(events("next_step_ignored").isEmpty()) // pas avant l'expiration du snackbar
        vm.confirmLater()
        vm.confirmLater()
        assertEquals(listOf(mapOf<String, Any>("kind" to "outing", "target_id" to 42L)), events("next_step_ignored"))
    }

    @Test
    fun ignoredIsNotLoggedWhenTheEngineCallFails() {
        val vm = vm(ScriptedProvider(outing(), dismissFails = true))
        vm.refresh()
        vm.later(outing())
        vm.confirmLater()
        assertTrue(events("next_step_ignored").isEmpty())
    }

    @Test
    fun ignoredIsLoggedEvenWhenTheEngineHasNoNextSuggestion() {
        val vm = vm(ScriptedProvider(outing(), next = null))
        vm.refresh()
        vm.later(outing())
        vm.confirmLater()
        assertEquals(1, events("next_step_ignored").size)
    }

    @Test
    fun targetIdIsOmittedForATypeWithoutTarget() {
        val smallTalk = NextStepSuggestion(
            id = "next-step-9", typeKey = SuggestionType.SMALL_TALK.key, title = "", cta = SuggestionCta.TRY, engineId = 9,
        )
        val vm = vm(ScriptedProvider(smallTalk))
        vm.later(smallTalk)
        assertEquals(listOf(mapOf<String, Any>("kind" to "smalltalk")), events("next_step_later_tapped"))
    }

    // ------------------------------------------------------------ realised (tap)

    @Test
    fun poiAndAmbassadorHeroAreRealisedByTap() {
        val poi = outing().copy(typeKey = SuggestionType.PLACE.key, targetId = 77)
        val ambassador = outing().copy(typeKey = SuggestionType.AMBASSADOR.key, targetId = null)
        assertTrue(SuggestionAnalytics.isRealisedByTap(poi))
        assertTrue(SuggestionAnalytics.isRealisedByTap(ambassador))
        assertEquals(
            mapOf<String, Any>("kind" to "poi", "target_id" to 77L, "realisation_trigger" to "tap"),
            SuggestionAnalytics.realisedByTapParams(poi),
        )
        assertEquals(
            mapOf<String, Any>("kind" to "become_ambassador", "realisation_trigger" to "tap"),
            SuggestionAnalytics.realisedByTapParams(ambassador),
        )
    }

    @Test
    fun otherKindsAndExplorerTracksAreNotRealisedByTap() {
        assertFalse(SuggestionAnalytics.isRealisedByTap(outing()))
        // Une autre piste (pas d'id de prochain pas) n'est pas la suggestion principale.
        assertFalse(SuggestionAnalytics.isRealisedByTap(outing().copy(typeKey = SuggestionType.PLACE.key, engineId = null)))
    }

    // ------------------------------------------------------------ explorer

    @Test
    fun explorerTapCarriesHeroPositionAndTappedTrack() {
        val rows = listOf(
            explorerRow(SuggestionType.GROUP, 10), explorerRow(SuggestionType.EVENT, 11), explorerRow(SuggestionType.PLACE, 12),
        )
        val vm = vm(ScriptedProvider(outing(), explorer = rows))
        vm.refresh()
        vm.explorerTapped(rows[1])
        assertEquals(
            listOf(
                mapOf<String, Any>(
                    "kind" to "outing", "target_id" to 42L, "position" to 2,
                    "explorer_kind" to "outing", "explorer_target_id" to 11L,
                ),
            ),
            events("next_step_explorer_tapped"),
        )
    }

    @Test
    fun explorerTapWithoutHeroOmitsKindAndTargetId() {
        val rows = listOf(explorerRow(SuggestionType.GROUP, 10))
        val vm = vm(ScriptedProvider(null, explorer = rows))
        vm.refresh()
        vm.explorerTapped(rows[0])
        assertEquals(
            listOf(mapOf<String, Any>("position" to 1, "explorer_kind" to "neighborhood", "explorer_target_id" to 10L)),
            events("next_step_explorer_tapped"),
        )
    }

    // ------------------------------------------------------------ null

    @Test
    fun emptyCurrentLogsNoCandidates() {
        vm(ScriptedProvider(null)).refresh()
        assertEquals(listOf(mapOf<String, Any>("reason" to "no_candidates")), events("next_step_null"))
    }

    @Test
    fun failingCurrentLogsError() {
        vm(ScriptedProvider(null, fetchFails = true)).refresh()
        assertEquals(listOf(mapOf<String, Any>("reason" to "error")), events("next_step_null"))
    }

    @Test
    fun aServedCardIsNotNull() {
        vm(ScriptedProvider(outing())).refresh()
        assertTrue(events("next_step_null").isEmpty())
    }

    @Test
    fun aCardAlreadyPutOffThisSessionIsAllExcluded() {
        val provider = ScriptedProvider(outing(), next = null)
        val vm = vm(provider)
        vm.refresh()
        vm.later(outing())
        vm.confirmLater() // le moteur n'a pas de suivante : refresh, qui resert la même card
        assertEquals(listOf(mapOf<String, Any>("reason" to "all_excluded")), events("next_step_null"))
    }
}
