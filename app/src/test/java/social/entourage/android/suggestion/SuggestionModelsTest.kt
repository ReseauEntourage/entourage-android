package social.entourage.android.suggestion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SuggestionModelsTest {

    private fun suggestion(typeKey: String, reasons: List<SuggestionReason> = emptyList()) =
        NextStepSuggestion(id = "s", typeKey = typeKey, title = "t", cta = SuggestionCta.JOIN, reasons = reasons)

    @Test
    fun unknownTypeIsDroppedWithoutCrashing() {
        val payload = SuggestionPayload(
            hero = suggestion("hologram"),
            explorer = listOf(
                ExplorerAccess("a", "hologram", "x", tab = ExplorerTab.EVENTS),
                ExplorerAccess("b", "outing", "y", tab = ExplorerTab.EVENTS),
            ),
        ).sanitized()
        assertNull(payload.hero)
        assertEquals(listOf("b"), payload.explorer.map { it.id })
    }

    @Test
    fun everyEngineKindIsAKnownType() {
        listOf(
            "outing", "neighborhood", "action", "content_path", "smalltalk", "poi", "post_message", "become_ambassador",
        ).forEach { assertNotNull(it, SuggestionType.fromKey(it)) }
    }

    @Test
    fun knownTypeIsKept() {
        assertNotNull(SuggestionPayload(hero = suggestion("neighborhood")).sanitized().hero)
    }

    @Test
    fun explorerIsBoundedToFour() {
        val accesses = (1..6).map { ExplorerAccess("$it", "outing", "l", tab = ExplorerTab.EVENTS) }
        assertEquals(4, SuggestionPayload(explorer = accesses).sanitized().explorer.size)
    }

    @Test
    fun reasonsAreTheEngineSentencesCappedAtThree() {
        val reasons = (1..5).map { SuggestionReason("near_you", "phrase $it") }
        assertEquals(listOf("phrase 1", "phrase 2", "phrase 3"), suggestion("outing", reasons).displayableReasons().map { it.text })
    }

    @Test
    fun emptyReasonIsNotDisplayed() {
        val reasons = listOf(SuggestionReason("near_you", " "), SuggestionReason("popular", "D'autres personnes y participent déjà."))
        assertEquals(listOf("popular"), suggestion("outing", reasons).displayableReasons().map { it.code })
    }

    @Test
    fun reasonIconDependsOnTheCodeWithADefaultForUnknownCodes() {
        assertEquals("📍", SuggestionReason("near_you", "x").icon)
        assertEquals("✅", SuggestionReason("code_du_futur", "x").icon)
    }
}

class SuggestionExplorerUiTest {
    private val access = ExplorerAccess("a", "outing", "l", tab = ExplorerTab.EVENTS)

    @Test
    fun zeroAccessHidesTheWholeZone() {
        org.junit.Assert.assertEquals(
            ExplorerUi.Hidden,
            SuggestionUiState(loading = false, payload = SuggestionPayload(explorer = emptyList())).toExplorerUi(),
        )
    }

    @Test
    fun loadingWithoutDataShowsSkeletonAndKeepsContentWhileRefreshing() {
        org.junit.Assert.assertEquals(ExplorerUi.Loading, SuggestionUiState().toExplorerUi())
        val refreshing = SuggestionUiState(loading = true, payload = SuggestionPayload(explorer = listOf(access)))
        org.junit.Assert.assertEquals(ExplorerUi.Ready(listOf(access)), refreshing.toExplorerUi())
    }

    @Test
    fun errorWithoutDataHidesTheZone() {
        org.junit.Assert.assertEquals(ExplorerUi.Hidden, SuggestionUiState(loading = false, payload = null).toExplorerUi())
    }
}
