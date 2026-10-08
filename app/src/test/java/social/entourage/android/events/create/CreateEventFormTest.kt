package social.entourage.android.events.create

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import social.entourage.android.R
import social.entourage.android.api.request.CreateEventWrapper
import java.time.LocalDate

class CreateEventFormTest {

    private val gson = Gson()
    private val today = LocalDate.of(2026, 9, 1)
    private val datePattern = "yyyy-MM-dd HH:mm:ss Z"

    private fun validForm() = CreateEventForm(
        title = "Discussion entre voisins",
        description = "Venez nombreux",
        entourageImageId = 12,
        date = "2026-09-23",
        startTime = "16:00",
        endTime = "18:00",
        address = "Place des Fêtes, Paris",
        interests = mutableListOf("cuisine"),
    )

    @Test
    fun validForm_hasNoError() {
        assertTrue(validForm().validate(today = today).isEmpty())
    }

    @Test
    fun emptyForm_reportsEveryRequiredField() {
        val errors = CreateEventForm().validate(today = today)
        assertEquals(R.string.create_event_error_name, errors[CreateEventField.NAME])
        assertEquals(R.string.create_event_error_description, errors[CreateEventField.DESCRIPTION])
        assertEquals(R.string.create_event_error_photo, errors[CreateEventField.PHOTO])
        assertEquals(R.string.create_event_error_date_empty, errors[CreateEventField.DATE])
        assertEquals(R.string.create_event_error_address, errors[CreateEventField.PLACE])
        assertEquals(R.string.create_event_error_categories, errors[CreateEventField.CATEGORIES])
    }

    @Test
    fun pastDate_isRejected_butAnUnchangedOneIsKept() {
        val form = validForm().apply { date = "2026-08-01" }
        assertEquals(
            R.string.create_event_error_date_past,
            form.validate(today = today)[CreateEventField.DATE]
        )
        assertNull(form.validate(today = today, unchangedPastDate = "2026-08-01")[CreateEventField.DATE])
    }

    @Test
    fun endBeforeStart_isRejected() {
        val form = validForm().apply { startTime = "18:00"; endTime = "16:00" }
        assertEquals(
            R.string.create_event_error_end_before_start,
            form.validate(today = today)[CreateEventField.END_TIME]
        )
    }

    @Test
    fun onlineEvent_needsAnHttpsLink() {
        val form = validForm().apply { online = true; eventUrl = "http://zoom.us/j/1" }
        assertEquals(R.string.create_event_error_link, form.validate(today = today)[CreateEventField.PLACE])
        form.eventUrl = "https://zoom.us/j/1"
        assertNull(form.validate(today = today)[CreateEventField.PLACE])
    }

    @Test
    fun placeLimitZero_isRejected() {
        val form = validForm().apply { placeLimit = 0 }
        assertEquals(
            R.string.create_event_error_place_limit,
            form.validate(today = today)[CreateEventField.PLACE_LIMIT]
        )
    }

    @Test
    fun photoBeingUploaded_blocksTheStep() {
        val form = validForm()
        assertEquals(
            R.string.create_event_error_photo_uploading,
            form.validate(isUploading = true, today = today)[CreateEventField.PHOTO]
        )
    }

    @Test
    fun switchingOnline_resetsTheWheelchairCard() {
        val form = validForm().apply { wheelchairAccessible = true; familyFriendly = true }
        form.setOnlineMode(true)
        assertFalse(form.wheelchairAccessible)
        assertTrue(form.familyFriendly)
    }

    @Test
    fun requestBody_carriesPmrAndKidsFriendly_andKeepsReservedFemale() {
        val form = validForm().apply {
            wheelchairAccessible = true
            familyFriendly = true
            reservedFemale = true
        }
        val json = gson.toJson(CreateEventWrapper(form.toCreateEvent(datePattern)))
        assertTrue(json.contains("\"pmr\":true"))
        assertTrue(json.contains("\"kids_friendly\":true"))
        assertTrue(json.contains("\"reserved_female\":true"))
    }

    @Test
    fun requestBody_sendsExplicitFalse_whenCardsUnselected() {
        val json = gson.toJson(CreateEventWrapper(validForm().toCreateEvent(datePattern)))
        assertTrue(json.contains("\"pmr\":false"))
        assertTrue(json.contains("\"kids_friendly\":false"))
    }

    @Test
    fun requestBody_sendsPmrFalse_whenOnline() {
        val form = validForm().apply { wheelchairAccessible = true; familyFriendly = true }
        form.setOnlineMode(true)
        val json = gson.toJson(CreateEventWrapper(form.toCreateEvent(datePattern)))
        assertTrue(json.contains("\"pmr\":false"))
        assertTrue(json.contains("\"kids_friendly\":true"))
    }

    @Test
    fun requestBody_isComplete() {
        val form = validForm().apply { placeLimit = 12; latitude = 48.88; longitude = 2.4; googlePlaceId = "abc" }
        val event = form.toCreateEvent(datePattern)
        assertEquals("Discussion entre voisins", event.title)
        assertEquals(false, event.online)
        assertEquals(12, event.entourageImageId)
        assertEquals(12, event.metadata?.placeLimit)
        assertEquals("Place des Fêtes, Paris", event.metadata?.streetAddress)
        assertEquals(48.88, event.latitude)
        assertEquals("abc", event.metadata?.googlePlaceId)
        assertTrue(event.metadata?.startsAt?.startsWith("2026-09-23 16:00:00") == true)
        assertTrue(event.metadata?.endsAt?.startsWith("2026-09-23 18:00:00") == true)
        assertNull(event.recurrence)
    }

    @Test
    fun editedEvent_sendsRecurrenceOnlyWhenChanged() {
        val form = validForm().apply { recurrence = Recurrence.EVERY_WEEK.value }
        assertNull(form.toCreateEvent(datePattern, isEdition = true, editedRecurrence = 7).recurrence)
        assertEquals(7, form.toCreateEvent(datePattern, isEdition = true, editedRecurrence = 0).recurrence)
    }

    @Test
    fun draft_keepsTheForm_butNotAnUnsentLocalPhoto() {
        val form = validForm().apply {
            reservedFemale = true
            wheelchairAccessible = true
            uploadKey = "key-123"
            localPhotoPath = "/cache/photo.jpg"
        }
        val restored = gson.fromJson(gson.toJson(form), CreateEventForm::class.java)
        assertEquals("Discussion entre voisins", restored.title)
        assertTrue(restored.wheelchairAccessible)
        assertTrue(restored.reservedFemale)
        assertEquals("key-123", restored.uploadKey)
        assertNull(restored.localPhotoPath)
    }
}
