package social.entourage.android.groups.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import social.entourage.android.groups.presentation.GroupPresentationComposer.Intent

class GroupPresentationComposerTest {

    private val strings = GroupPresentationComposer.Strings(
        greeting = "Bonjour !",
        introWithName = { "Moi, c'est $it." },
        interestsLine = { "J'aime : $it." },
        aboutLine = { it },
        talk = "discuter",
        events = "événements",
        help = "entraide",
        intentsIntro = "Je suis là pour :",
        closing = "À bientôt !",
    )

    @Test
    fun `message complet avec nom, centres d'intérêt, mot libre et intentions`() {
        val text = GroupPresentationComposer.compose(
            strings, "Clara", listOf("Cuisine", "Jardinage"), "J'adore les balades.",
            linkedSetOf(Intent.HELP, Intent.TALK),
        )
        assertEquals(
            "Bonjour !\n\nMoi, c'est Clara.\n\nJ'aime : cuisine, jardinage.\n\nJ'adore les balades." +
                "\n\nJe suis là pour :\n• discuter\n• entraide\n\nÀ bientôt !",
            text,
        )
    }

    @Test
    fun `profil vide et aucune intention donnent un message court`() {
        val text = GroupPresentationComposer.compose(strings, "  ", emptyList(), null, emptySet())
        assertEquals("Bonjour !\n\nÀ bientôt !", text)
        assertFalse(text.contains("•"))
    }

    @Test
    fun `les intentions suivent un ordre stable`() {
        val text = GroupPresentationComposer.compose(
            strings, null, emptyList(), "", linkedSetOf(Intent.HELP, Intent.EVENTS, Intent.TALK),
        )
        assertTrue(text.indexOf("discuter") < text.indexOf("événements"))
        assertTrue(text.indexOf("événements") < text.indexOf("entraide"))
    }
}
