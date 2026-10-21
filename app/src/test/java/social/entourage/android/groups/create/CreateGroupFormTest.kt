package social.entourage.android.groups.create

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateGroupFormTest {

    private fun validForm() = CreateGroupForm(
        name = "Voisins",
        description = "Un groupe",
        latitude = 48.85,
        longitude = 2.35,
        address = "Paris",
        interests = mutableListOf("sport"),
        imageId = 3,
        photoUrl = "https://example.org/a.jpg",
    )

    @Test
    fun emptyFormReportsEveryField() {
        val errors = CreateGroupForm().validate()
        assertEquals(CreateGroupField.values().toSet(), errors.keys)
    }

    @Test
    fun validFormHasNoError() {
        assertTrue(validForm().validate().isEmpty())
    }

    @Test
    fun minimumLengthsAreUnchanged() {
        // Nom : 2 caractères minimum ; description : 1 caractère minimum.
        assertTrue(validForm().apply { name = "A" }.validate().containsKey(CreateGroupField.NAME))
        assertFalse(validForm().apply { name = "AB" }.validate().containsKey(CreateGroupField.NAME))
        assertFalse(validForm().apply { description = "x" }.validate().containsKey(CreateGroupField.DESCRIPTION))
        assertTrue(validForm().apply { description = "  " }.validate().containsKey(CreateGroupField.DESCRIPTION))
    }

    @Test
    fun editionDoesNotRequireLocationNorNewPhoto() {
        val errors = CreateGroupForm(name = "Voisins", description = "Un groupe", interests = mutableListOf("sport"))
            .validate(isEdition = true)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun otherInterestIsOnlySentWhenSelected() {
        val form = validForm().apply { otherInterest = "Jardinage" }
        assertNull(form.toGroup().otherInterest)
        form.interests.add(CreateGroupForm.OTHER_INTEREST)
        assertEquals("Jardinage", form.toGroup().otherInterest)
    }

    @Test
    fun updateBodyKeepsTheLegacyKeys() {
        val body = validForm().apply { welcomeMessage = "Bienvenue " }.toUpdateBody()
        @Suppress("UNCHECKED_CAST")
        val edited = body["neighborhood"] as Map<String, Any>
        assertEquals(
            setOf("name", "description", "welcome_message", "neighborhood_image_id", "interests"),
            edited.keys
        )
        assertEquals("Bienvenue", edited["welcome_message"])
    }
}
