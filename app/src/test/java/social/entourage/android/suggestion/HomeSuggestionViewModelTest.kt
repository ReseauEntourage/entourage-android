package social.entourage.android.suggestion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * « Plus tard » : le moteur n'est prévenu qu'après le snackbar, jamais après « Annuler ».
 * Mesure d'affichage : un seul « affiché » par card et par affichage de la home, comme iOS.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeSuggestionViewModelTest {

    private class FakeProvider(private val replacement: NextStepSuggestion?) : SuggestionProvider {
        var fetches = 0
        val dismissed = mutableListOf<String>()
        val reported = mutableListOf<SuggestionInteraction>()

        override suspend fun fetch(): SuggestionPayload {
            fetches++
            return SuggestionPayload(hero = suggestion("first", 1))
        }

        override suspend fun dismiss(suggestion: NextStepSuggestion): NextStepSuggestion? {
            dismissed += suggestion.id
            return replacement
        }

        override suspend fun report(interaction: SuggestionInteraction) {
            reported += interaction
        }
    }

    companion object {
        fun suggestion(id: String, engineId: Int) = NextStepSuggestion(
            id = id, typeKey = SuggestionType.GROUP.key, title = id, cta = SuggestionCta.JOIN, engineId = engineId,
        )

        /** Une autre piste du moteur : pas d'id de prochain pas, sa cible porte l'identité. */
        fun otherTrack(targetId: Int) = NextStepSuggestion(
            id = "other-neighborhood-$targetId", typeKey = SuggestionType.GROUP.key, title = "t",
            cta = SuggestionCta.JOIN, targetId = targetId,
        )

        fun access(targetId: Int) = otherTrack(targetId).let {
            ExplorerAccess(id = it.id, typeKey = it.typeKey, label = it.title, tab = ExplorerTab.GROUPS, suggestion = it)
        }
    }

    /** Événements Firebase émis, dans l'ordre. */
    private val logged = mutableListOf<Pair<String, Map<String, Any>>>()

    private fun viewModel(provider: SuggestionProvider) =
        HomeSuggestionViewModel(provider) { event, params -> logged += event to params }

    private fun loggedCount(event: String) = logged.count { it.first == event }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun laterThenSnackbarExpiryDoesNotBringTheSameCardBack() {
        // Le moteur ne propose pas de suivante et `current` resert la même card : elle ne doit pas réapparaître.
        val provider = FakeProvider(replacement = null)
        val vm = viewModel(provider)
        vm.refresh()
        vm.later(vm.state.value.payload!!.hero!!)
        vm.confirmLater()
        assertNull(vm.state.value.payload?.hero)
        vm.refresh()
        assertNull(vm.state.value.payload?.hero)
    }

    @Test
    fun laterAloneDoesNotCallTheEngineButHidesTheCard() {
        val provider = FakeProvider(suggestion("second", 2))
        val vm = HomeSuggestionViewModel(provider)
        vm.refresh()
        vm.later(suggestion("first", 1))
        assertNull(vm.state.value.payload?.hero)
        assertEquals(emptyList<String>(), provider.dismissed)
    }

    @Test
    fun undoBringsTheCardBackWithoutCallingTheEngine() {
        val provider = FakeProvider(suggestion("second", 2))
        val vm = HomeSuggestionViewModel(provider)
        vm.refresh()
        val first = vm.state.value.payload!!.hero!!
        vm.later(first)
        vm.undoLater()
        assertEquals(first, vm.state.value.payload?.hero)
        vm.confirmLater() // rien à confirmer après l'annulation
        assertEquals(emptyList<String>(), provider.dismissed)
    }

    @Test
    fun confirmedLaterIgnoresOnceAndShowsTheNextSuggestionWithoutASecondFetch() {
        val provider = FakeProvider(suggestion("second", 2))
        val vm = HomeSuggestionViewModel(provider)
        vm.refresh()
        vm.later(vm.state.value.payload!!.hero!!)
        vm.confirmLater()
        assertEquals(listOf("first"), provider.dismissed)
        assertEquals("second", vm.state.value.payload?.hero?.id)
        assertEquals(1, provider.fetches)
    }

    @Test
    fun confirmedLaterWithoutReplacementRefreshes() {
        val provider = FakeProvider(replacement = null)
        val vm = HomeSuggestionViewModel(provider)
        vm.refresh()
        vm.later(vm.state.value.payload!!.hero!!)
        vm.confirmLater()
        assertEquals(2, provider.fetches)
    }

    @Test
    fun refreshDuringTheUndoMessageDoesNotBringTheCardBack() {
        val provider = FakeProvider(suggestion("second", 2))
        val vm = HomeSuggestionViewModel(provider)
        vm.refresh()
        vm.later(vm.state.value.payload!!.hero!!)
        vm.refresh()
        assertNull(vm.state.value.payload?.hero)
        assertEquals(1, provider.fetches)
    }

    @Test
    fun reportedInteractionsReachTheProvider() {
        val provider = FakeProvider(null)
        val vm = HomeSuggestionViewModel(provider)
        vm.report(SuggestionInteraction.WHY_OPENED, suggestion("first", 1))
        assertEquals(listOf(SuggestionInteraction("why_opened", nextStepId = 1)), provider.reported)
    }

    // ------------------------------------------------------------ mesure d'affichage

    @Test
    fun heroDisplayedIsSentOnceForTheSameCardWhateverTheNumberOfAttachments() {
        val provider = FakeProvider(null)
        val vm = viewModel(provider)
        val first = suggestion("first", 1)
        vm.heroDisplayed(first)
        vm.heroDisplayed(first) // item détaché puis rattaché
        vm.heroDisplayed(first.copy())
        assertEquals(1, loggedCount("view_home_suggestion_hero"))
        assertEquals(listOf(SuggestionInteraction("displayed", nextStepId = 1)), provider.reported)
    }

    @Test
    fun heroDisplayedFirebaseEventCarriesTypeAndTheNextStepIdAsText() {
        val vm = viewModel(FakeProvider(null))
        vm.heroDisplayed(suggestion("next-step-1", 1))
        assertEquals(
            listOf("view_home_suggestion_hero" to mapOf<String, Any>("type" to "neighborhood", "id" to "1")),
            logged,
        )
    }

    @Test
    fun anotherHeroIsMeasuredAgain() {
        val provider = FakeProvider(null)
        val vm = viewModel(provider)
        vm.heroDisplayed(suggestion("first", 1))
        vm.heroDisplayed(suggestion("second", 2))
        assertEquals(2, loggedCount("view_home_suggestion_hero"))
        assertEquals(
            listOf(SuggestionInteraction("displayed", nextStepId = 1), SuggestionInteraction("displayed", nextStepId = 2)),
            provider.reported,
        )
    }

    @Test
    fun theHomeAppearingAgainMeasuresTheHeroAgainOnce() {
        val provider = FakeProvider(null)
        val vm = viewModel(provider)
        val first = suggestion("first", 1)
        vm.heroDisplayed(first)
        vm.onHomeResumed()
        vm.heroDisplayed(first)
        vm.heroDisplayed(first)
        assertEquals(2, loggedCount("view_home_suggestion_hero"))
        assertEquals(2, provider.reported.size)
    }

    @Test
    fun noDisplayedForTheCardRestoredByUndo() {
        val provider = FakeProvider(suggestion("second", 2))
        val vm = viewModel(provider)
        vm.refresh()
        val first = vm.state.value.payload!!.hero!!
        vm.heroDisplayed(first)
        vm.later(first)
        vm.undoLater()
        vm.heroDisplayed(vm.state.value.payload!!.hero!!) // la card revient dans la composition
        assertEquals(1, loggedCount("view_home_suggestion_hero"))
        assertEquals(listOf(SuggestionInteraction("displayed", nextStepId = 1)), provider.reported)
    }

    @Test
    fun theNextCardAfterAConfirmedLaterIsMeasured() {
        val provider = FakeProvider(suggestion("second", 2))
        val vm = viewModel(provider)
        vm.refresh()
        val first = vm.state.value.payload!!.hero!!
        vm.heroDisplayed(first)
        vm.later(first)
        vm.confirmLater()
        vm.heroDisplayed(vm.state.value.payload!!.hero!!)
        assertEquals(2, loggedCount("view_home_suggestion_hero"))
        assertEquals(
            listOf(SuggestionInteraction("displayed", nextStepId = 1), SuggestionInteraction("displayed", nextStepId = 2)),
            provider.reported,
        )
    }

    @Test
    fun explorerViewIsSentOncePerAppearanceWithTheNumberOfRows() {
        val vm = viewModel(FakeProvider(null))
        val rows = listOf(access(10), access(11))
        vm.explorerDisplayed(rows)
        vm.explorerDisplayed(rows)
        assertEquals(listOf("view_home_explorer" to mapOf<String, Any>("count" to 2)), logged)
        vm.onHomeResumed()
        vm.explorerDisplayed(rows)
        assertEquals(2, loggedCount("view_home_explorer"))
    }

    @Test
    fun explorerDisplayedIsSentOncePerTrackId() {
        val provider = FakeProvider(null)
        val vm = viewModel(provider)
        vm.explorerDisplayed(listOf(access(10), access(11)))
        vm.explorerDisplayed(listOf(access(10), access(11)))
        vm.explorerDisplayed(listOf(access(10), access(11), access(12))) // une nouvelle piste seulement
        assertEquals(
            listOf(10, 11, 12).map { SuggestionInteraction("displayed", itemKind = "neighborhood", targetId = it) },
            provider.reported,
        )
    }

    @Test
    fun explorerTracksAreMeasuredAgainAfterTheHomeAppearsAgain() {
        val provider = FakeProvider(null)
        val vm = viewModel(provider)
        vm.explorerDisplayed(listOf(access(10)))
        vm.onHomeResumed()
        vm.explorerDisplayed(listOf(access(10)))
        assertEquals(2, provider.reported.size)
    }

    @Test
    fun explorerRowWithoutATrackIsCountedButNotReported() {
        val provider = FakeProvider(null)
        val vm = viewModel(provider)
        val tabOnly = ExplorerAccess(id = "x", typeKey = SuggestionType.CONTENT.key, label = "t", tab = ExplorerTab.CONTENT)
        vm.explorerDisplayed(listOf(tabOnly, access(10)))
        assertEquals(listOf("view_home_explorer" to mapOf<String, Any>("count" to 2)), logged)
        assertEquals(listOf(SuggestionInteraction("displayed", itemKind = "neighborhood", targetId = 10)), provider.reported)
    }
}
