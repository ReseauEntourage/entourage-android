package social.entourage.android.actions.create

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionSection
import social.entourage.android.api.model.Address

class CreateActionFormTest {

    private fun validForm(sectionId: String = ActionSection.clothes) = CreateActionForm(
        sectionId = sectionId,
        title = "Vêtements",
        description = "Des vêtements chauds",
        address = "Paris - 75004",
        latitude = 48.85,
        longitude = 2.35,
    )

    @Test
    fun emptyFormReportsEveryStepWithAField() {
        val errors = CreateActionForm().validate(isEdition = false, sharingStep = true)
        assertEquals(CreateActionField.values().toSet(), errors.keys)
    }

    @Test
    fun minimumLengthsAreUnchanged() {
        // Titre : 2 caractères minimum ; description : 1 caractère minimum.
        assertTrue(validForm().apply { title = "A" }.validate(false, false).containsKey(CreateActionField.TITLE))
        assertFalse(validForm().apply { title = "AB" }.validate(false, false).containsKey(CreateActionField.TITLE))
        assertFalse(validForm().apply { description = "x" }.validate(false, false).containsKey(CreateActionField.DESCRIPTION))
        assertTrue(validForm().apply { description = " " }.validate(false, false).containsKey(CreateActionField.DESCRIPTION))
    }

    @Test
    fun sharingAnswerIsOnlyRequiredOnCreationWhenTheStepExists() {
        val form = validForm(ActionSection.social)
        assertTrue(form.validate(isEdition = false, sharingStep = true).containsKey(CreateActionField.SHARING))
        assertTrue(form.validate(isEdition = false, sharingStep = false).isEmpty())
        assertTrue(form.validate(isEdition = true, sharingStep = true).isEmpty())
        form.shareInGroup = false
        assertTrue(form.validate(isEdition = false, sharingStep = true).isEmpty())
    }

    @Test
    fun stepListAddsAndRemovesTheSharingStep() {
        val viewModel = CreateActionViewModel()
        viewModel.initialize(demand = false, edited = null)
        val base = listOf(CreateActionStep.CATEGORY, CreateActionStep.DESCRIPTION, CreateActionStep.LOCATION)

        // Sans groupe par défaut, ou pour une autre catégorie : 3 étapes.
        viewModel.form.sectionId = ActionSection.social
        assertEquals(base, viewModel.steps)
        viewModel.onDefaultGroupLoaded("Voisins de Paris")
        viewModel.form.sectionId = ActionSection.clothes
        assertEquals(base, viewModel.steps)

        // Catégorie « temps de partage » + groupe par défaut : l'étape de partage s'ajoute…
        viewModel.form.sectionId = ActionSection.social
        assertEquals(base + CreateActionStep.SHARING, viewModel.steps)
        assertEquals(4, viewModel.previewPageIndex)

        // … et se retire quand on change de catégorie.
        viewModel.form.sectionId = ActionSection.hygiene
        assertEquals(base, viewModel.steps)
        assertEquals(3, viewModel.previewPageIndex)
    }

    @Test
    fun autoPostFollowsTheSharingAnswerOnlyWhenTheStepExists() {
        val viewModel = CreateActionViewModel()
        viewModel.initialize(demand = true, edited = null)
        viewModel.onDefaultGroupLoaded("Voisins")
        viewModel.form.sectionId = ActionSection.social
        viewModel.form.shareInGroup = true
        assertTrue(viewModel.autoPost())
        viewModel.form.sectionId = ActionSection.services
        assertFalse(viewModel.autoPost())
    }

    @Test
    fun updateBodyOnlyContainsChangedFields() {
        val edited = Action(
            id = 12, uuid = "u-12", title = "Vêtements", description = "Des vêtements chauds",
            sectionName = ActionSection.clothes, location = Address(48.85, 2.35, "")
        )
        val unchanged = CreateActionForm.from(edited).toUpdateAction(edited, autoPost = false)
        assertNull(unchanged.title)
        assertNull(unchanged.description)
        assertNull(unchanged.sectionName)
        assertNull(unchanged.location)
        assertEquals(12, unchanged.id)
        assertEquals(false, unchanged.autoPostAtCreate)

        val changed = CreateActionForm.from(edited).apply {
            title = "Manteaux"
            latitude = 45.0
            longitude = 4.8
        }.toUpdateAction(edited, autoPost = true)
        assertEquals("Manteaux", changed.title)
        assertNull(changed.description)
        assertEquals(45.0, changed.location?.latitude)
        assertEquals(true, changed.autoPostAtCreate)
    }
}
