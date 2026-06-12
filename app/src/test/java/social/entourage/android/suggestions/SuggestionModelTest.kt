package social.entourage.android.suggestions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import social.entourage.android.api.model.Suggestion
import social.entourage.android.api.model.SuggestionMetadata
import social.entourage.android.api.model.SuggestionReason
import social.entourage.android.api.model.SuggestionsMeta
import social.entourage.android.api.model.SuggestionsResponse

class SuggestionModelTest {

    // --- Suggestion ---

    @Test
    fun `Suggestion default values are null or empty`() {
        val s = Suggestion()
        assertNull(s.id)
        assertNull(s.type)
        assertNull(s.title)
        assertNull(s.distance)
        assertNull(s.metadata)
        assertNull(s.cta)
        assertNull(s.score)
        assertTrue(s.reasons.isEmpty())
    }

    @Test
    fun `Suggestion holds provided values`() {
        val reason = SuggestionReason(icon = "location", text = "À 1.2 km de chez vous")
        val meta = SuggestionMetadata(startsAt = "2026-06-20T14:00:00Z", location = "75011")
        val s = Suggestion(
            id = "outing_42",
            type = "outing",
            title = "Café solidaire",
            distance = 1.2,
            metadata = meta,
            cta = "participate",
            score = 0.87,
            reasons = listOf(reason)
        )

        assertEquals("outing_42", s.id)
        assertEquals("outing", s.type)
        assertEquals("Café solidaire", s.title)
        assertEquals(1.2, s.distance!!, 0.001)
        assertEquals("participate", s.cta)
        assertEquals(0.87, s.score!!, 0.001)
        assertEquals(1, s.reasons.size)
        assertEquals("location", s.reasons[0].icon)
        assertEquals("À 1.2 km de chez vous", s.reasons[0].text)
    }

    // --- SuggestionReason ---

    @Test
    fun `SuggestionReason default values are null`() {
        val r = SuggestionReason()
        assertNull(r.icon)
        assertNull(r.text)
    }

    @Test
    fun `SuggestionReason holds icon and text`() {
        val r = SuggestionReason(icon = "star", text = "Populaire près de vous")
        assertEquals("star", r.icon)
        assertEquals("Populaire près de vous", r.text)
    }

    // --- SuggestionMetadata ---

    @Test
    fun `SuggestionMetadata default values are null`() {
        val m = SuggestionMetadata()
        assertNull(m.startsAt)
        assertNull(m.location)
    }

    @Test
    fun `SuggestionMetadata holds provided values`() {
        val m = SuggestionMetadata(startsAt = "2026-06-20T14:00:00Z", location = "75011")
        assertEquals("2026-06-20T14:00:00Z", m.startsAt)
        assertEquals("75011", m.location)
    }

    // --- SuggestionsMeta ---

    @Test
    fun `SuggestionsMeta default values`() {
        val m = SuggestionsMeta()
        assertEquals(1, m.currentPage)
        assertEquals(1, m.totalPages)
        assertEquals(0, m.totalCount)
    }

    @Test
    fun `SuggestionsMeta holds pagination values`() {
        val m = SuggestionsMeta(currentPage = 2, totalPages = 3, totalCount = 25)
        assertEquals(2, m.currentPage)
        assertEquals(3, m.totalPages)
        assertEquals(25, m.totalCount)
    }

    // --- SuggestionsResponse ---

    @Test
    fun `SuggestionsResponse default values`() {
        val r = SuggestionsResponse()
        assertNull(r.lifecycleSegment)
        assertTrue(r.suggestions.isEmpty())
        assertNull(r.meta)
    }

    @Test
    fun `SuggestionsResponse holds full payload`() {
        val suggestion = Suggestion(id = "outing_42", type = "outing", title = "Café solidaire")
        val meta = SuggestionsMeta(currentPage = 1, totalPages = 3, totalCount = 25)
        val response = SuggestionsResponse(
            lifecycleSegment = "active",
            suggestions = listOf(suggestion),
            meta = meta
        )

        assertEquals("active", response.lifecycleSegment)
        assertEquals(1, response.suggestions.size)
        assertEquals("outing_42", response.suggestions[0].id)
        assertEquals(1, response.meta?.currentPage)
        assertEquals(3, response.meta?.totalPages)
        assertEquals(25, response.meta?.totalCount)
    }

    @Test
    fun `SuggestionsResponse with multiple suggestions preserves order`() {
        val s1 = Suggestion(id = "s1", score = 0.9)
        val s2 = Suggestion(id = "s2", score = 0.7)
        val s3 = Suggestion(id = "s3", score = 0.5)
        val response = SuggestionsResponse(suggestions = listOf(s1, s2, s3))

        assertEquals(3, response.suggestions.size)
        assertEquals("s1", response.suggestions[0].id)
        assertEquals("s3", response.suggestions[2].id)
    }
}
