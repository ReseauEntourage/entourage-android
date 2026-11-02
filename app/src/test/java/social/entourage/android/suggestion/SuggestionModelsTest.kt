package social.entourage.android.suggestion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SuggestionModelsTest {

    private fun suggestion(typeKey: String, reasons: List<SuggestionReason> = emptyList(), isolated: Boolean = false) =
        NextStepSuggestion(
            id = "s", typeKey = typeKey, title = "t", cta = SuggestionCta.JOIN,
            reasons = reasons, isolatedAudience = isolated,
        )

    @Test
    fun unknownTypeIsDroppedWithoutCrashing() {
        val payload = SuggestionPayload(
            hero = suggestion("hologram"),
            explorer = listOf(
                ExplorerAccess("a", "hologram", "x", tab = ExplorerTab.EVENTS),
                ExplorerAccess("b", "event", "y", tab = ExplorerTab.EVENTS),
            ),
        ).sanitized()
        assertNull(payload.hero)
        assertEquals(listOf("b"), payload.explorer.map { it.id })
    }

    @Test
    fun knownTypeIsKept() {
        assertNotNull(SuggestionPayload(hero = suggestion("group")).sanitized().hero)
    }

    @Test
    fun explorerIsBoundedToFour() {
        val accesses = (1..6).map { ExplorerAccess("$it", "event", "l", tab = ExplorerTab.EVENTS) }
        assertEquals(4, SuggestionPayload(explorer = accesses).sanitized().explorer.size)
    }

    @Test
    fun reasonsAreCappedAtThree() {
        val reasons = ReasonKind.entries.map { SuggestionReason(it) }
        assertEquals(3, suggestion("event", reasons).displayableReasons().size)
    }

    @Test
    fun socialProofIsHiddenForIsolatedAudience() {
        val reasons = listOf(SuggestionReason(ReasonKind.SOCIAL_PROOF), SuggestionReason(ReasonKind.PROXIMITY, "1 km"))
        val shown = suggestion("group", reasons, isolated = true).displayableReasons()
        assertEquals(listOf(ReasonKind.PROXIMITY), shown.map { it.kind })
        assertEquals(2, suggestion("group", reasons, isolated = false).displayableReasons().size)
    }
}
