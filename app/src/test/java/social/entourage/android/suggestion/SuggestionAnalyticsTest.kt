package social.entourage.android.suggestion

import org.junit.Assert.assertEquals
import org.junit.Test

/** Les paramètres Firebase suivent le format d'iOS, même si les `id` d'interface d'Android diffèrent. */
class SuggestionAnalyticsTest {

    private fun hero(kind: SuggestionType, engineId: Int, targetId: Int? = 12) = NextStepSuggestion(
        id = "next-step-$engineId", typeKey = kind.key, title = "t", cta = SuggestionCta.PARTICIPATE,
        targetId = targetId, engineId = engineId,
    )

    private fun other(kind: SuggestionType, targetId: Int?) = NextStepSuggestion(
        id = "other-${kind.key}-$targetId", typeKey = kind.key, title = "t", cta = SuggestionCta.PARTICIPATE,
        targetId = targetId,
    )

    @Test
    fun heroParamsCarryTheEngineKindAndTheNextStepIdAsText() {
        val s = hero(SuggestionType.EVENT, engineId = 42)
        assertEquals(mapOf("type" to "outing", "id" to "42"), SuggestionAnalytics.cardParams(s))
    }

    @Test
    fun heroIdIgnoresTheInterfaceIdAndTheTarget() {
        val s = hero(SuggestionType.GROUP, engineId = 7, targetId = 999)
        assertEquals("next-step-7", s.id)
        assertEquals("7", SuggestionAnalytics.cardId(s))
    }

    @Test
    fun otherTrackIdIsOthersKindAndTargetId() {
        val s = other(SuggestionType.EVENT, targetId = 31)
        assertEquals("others-outing-31", SuggestionAnalytics.cardId(s))
        assertEquals(mapOf("type" to "outing", "id" to "others-outing-31"), SuggestionAnalytics.cardParams(s))
    }

    @Test
    fun otherTrackWithoutTargetCountsForZero() {
        assertEquals("others-poi-0", SuggestionAnalytics.cardId(other(SuggestionType.PLACE, targetId = null)))
    }

    @Test
    fun otherTracksWithoutTheirOwnTargetCountForZeroWhateverTheAppKeeps() {
        // Mini-parcours : l'app garde l'id du premier contenu, iOS n'a pas de cible (0), comme `target_id`.
        assertEquals("others-content_path-0", SuggestionAnalytics.cardId(other(SuggestionType.CONTENT_PATH, targetId = 3)))
        assertEquals("others-smalltalk-0", SuggestionAnalytics.cardId(other(SuggestionType.SMALL_TALK, targetId = 5)))
        assertEquals("others-become_ambassador-0", SuggestionAnalytics.cardId(other(SuggestionType.AMBASSADOR, targetId = 8)))
    }

    @Test
    fun explorerTapCarriesTheTrackKindAndId() {
        val s = other(SuggestionType.GROUP, targetId = 5)
        val access = ExplorerAccess(
            id = s.id, typeKey = s.typeKey, label = "t", tab = ExplorerTab.GROUPS, suggestion = s,
        )
        assertEquals(mapOf("type" to "neighborhood", "id" to "others-neighborhood-5"), SuggestionAnalytics.accessParams(access))
    }

    @Test
    fun explorerRowWithoutATrackHasNoTarget() {
        val access = ExplorerAccess(id = "mock-x-content", typeKey = SuggestionType.CONTENT.key, label = "t", tab = ExplorerTab.CONTENT)
        assertEquals(mapOf("type" to "content", "id" to "others-content-0"), SuggestionAnalytics.accessParams(access))
    }

    @Test
    fun explorerViewCarriesTheNumberOfRows() {
        val accesses = listOf(
            ExplorerAccess(id = "a", typeKey = SuggestionType.GROUP.key, label = "a", tab = ExplorerTab.GROUPS),
            ExplorerAccess(id = "b", typeKey = SuggestionType.EVENT.key, label = "b", tab = ExplorerTab.EVENTS),
        )
        assertEquals(mapOf("count" to 2), SuggestionAnalytics.explorerViewParams(accesses))
    }
}
