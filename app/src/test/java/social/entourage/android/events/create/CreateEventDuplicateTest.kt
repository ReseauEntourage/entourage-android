package social.entourage.android.events.create

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import social.entourage.android.api.model.EventMetadata
import social.entourage.android.api.model.Events
import java.util.Date

class CreateEventDuplicateTest {

    private val source = Events(
        title = "Café des voisins",
        description = "Venez nombreux partager un moment convivial autour d'un café.",
        online = true,
        eventUrl = "https://www.entourage.social/",
        recurrence = Recurrence.EVERY_WEEK.value,
        interests = mutableListOf("cuisine"),
        metadata = EventMetadata(
            startsAt = Date(),
            endsAt = Date(),
            placeLimit = 12,
            reserved_female = true,
            landscapeUrl = "https://example.org/photo.jpg",
        ),
    )

    @Test
    fun duplicate_keepsContentButClearsDatesAndRecurrence() {
        val viewModel = CreateEventViewModel()
        viewModel.initialize(edited = null, draft = null, preselectedGroupId = null, duplicated = source)

        val form = viewModel.form
        assertEquals("Café des voisins", form.title)
        assertEquals(source.description, form.description)
        assertTrue(form.online)
        assertEquals("https://www.entourage.social/", form.eventUrl)
        assertEquals(12, form.placeLimit)
        assertTrue(form.reservedFemale)
        assertEquals(listOf("cuisine"), form.interests)
        assertNull(form.date)
        assertNull(form.startTime)
        assertNull(form.endTime)
        assertEquals(Recurrence.NO_RECURRENCE.value, form.recurrence)
        assertTrue(form.neighborhoodIds.isEmpty())
    }

    @Test
    fun duplicate_isACreationNotAnEdition() {
        val viewModel = CreateEventViewModel()
        viewModel.initialize(edited = null, draft = null, preselectedGroupId = null, duplicated = source)

        assertFalse(viewModel.isEdition)
        assertNull(viewModel.editedEvent)
        assertEquals("https://example.org/photo.jpg", viewModel.duplicatedPhotoUrl)
    }
}
