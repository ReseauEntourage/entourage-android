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

/** « Plus tard » : le moteur n'est prévenu qu'après le snackbar, jamais après « Annuler ». */
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
    }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

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
    fun reportedInteractionsReachTheProvider() {
        val provider = FakeProvider(null)
        val vm = HomeSuggestionViewModel(provider)
        vm.report(SuggestionInteraction.WHY_OPENED, suggestion("first", 1))
        assertEquals(listOf(SuggestionInteraction("why_opened", nextStepId = 1)), provider.reported)
    }
}
