package social.entourage.android.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import social.entourage.android.api.model.OnboardingJourney

class WelcomeJourneyStepTest {

    @Test
    fun `sans evenement toutes les etapes sont a faire`() {
        assertEquals(List(4) { WelcomeJourneyState.TODO }, WelcomeJourneyStep.statesOf(emptyList()))
        assertEquals(List(4) { WelcomeJourneyState.TODO }, WelcomeJourneyStep.statesOf(null))
    }

    @Test
    fun `les evenements skipped passent chaque etape a SKIPPED`() {
        val events = listOf(
            "onboarding.resource.welcome_watched_skipped",
            "onboarding.neighborhood.national_skipped",
            "onboarding.outing.webinar_or_first_steps_skipped",
            "onboarding.outing.papotages_skipped"
        )
        assertEquals(List(4) { WelcomeJourneyState.SKIPPED }, WelcomeJourneyStep.statesOf(events))
    }

    @Test
    fun `fait l'emporte sur passe`() {
        val events = listOf(
            "onboarding.neighborhood.national_skipped",
            "onboarding.neighborhood.national"
        )
        assertEquals(WelcomeJourneyState.DONE, WelcomeJourneyStep.NATIONAL_GROUP.stateOf(events))
    }

    @Test
    fun `chaque etape est independante des autres`() {
        val events = listOf("onboarding.outing.papotages")
        val states = WelcomeJourneyStep.statesOf(events)
        assertEquals(
            listOf(
                WelcomeJourneyState.TODO,
                WelcomeJourneyState.TODO,
                WelcomeJourneyState.TODO,
                WelcomeJourneyState.DONE
            ),
            states
        )
    }

    @Test
    fun `arguments de l'API de skip`() {
        assertEquals(
            listOf("welcome_watched", "neighborhood_national", "webinar_or_first_steps", "papotages"),
            WelcomeJourneyStep.entries.map { it.apiStep }
        )
    }

    private val day = 24L * 60 * 60 * 1000

    @Test
    fun `inactivite 14 jours declenche le skip automatique`() {
        val now = 100 * day
        assertEquals(false, WelcomeJourneyInactivity.shouldAutoSkip(null, now))
        assertEquals(false, WelcomeJourneyInactivity.shouldAutoSkip(0L, now))
        assertEquals(false, WelcomeJourneyInactivity.shouldAutoSkip(now - 13 * day, now))
        assertEquals(true, WelcomeJourneyInactivity.shouldAutoSkip(now - 14 * day, now))
        assertEquals(true, WelcomeJourneyInactivity.shouldAutoSkip(now - 40 * day, now))
    }

    private fun journey(
        removed: Boolean? = false,
        reason: String? = null,
        steps: Map<String, String>? = null
    ) = OnboardingJourney().apply {
        this.removed = removed
        removedReason = reason
        this.steps = steps
    }

    @Test
    fun `first_event valide l'etape 4 comme l'ancien evenement papotages`() {
        assertEquals(WelcomeJourneyState.DONE, WelcomeJourneyStep.PAPOTAGES.stateOf(listOf("onboarding.outing.first_event")))
        assertEquals(WelcomeJourneyState.DONE, WelcomeJourneyStep.PAPOTAGES.stateOf(listOf("onboarding.outing.papotages")))
        assertEquals(WelcomeJourneyState.TODO, WelcomeJourneyStep.WEBINAR.stateOf(listOf("onboarding.outing.first_event")))
    }

    @Test
    fun `les etapes du back font foi sur les evenements`() {
        val steps = mapOf(
            "welcome_watched" to "done",
            "neighborhood_national" to "skipped",
            "webinar_or_first_steps" to "todo",
            "papotages" to "done"
        )
        // l'événement dit « passée », le back dit « faite » : le back l'emporte
        val events = listOf("onboarding.neighborhood.national_skipped", "onboarding.outing.webinar_or_first_steps")
        assertEquals(
            listOf(
                WelcomeJourneyState.DONE,
                WelcomeJourneyState.SKIPPED,
                WelcomeJourneyState.TODO,
                WelcomeJourneyState.DONE
            ),
            WelcomeJourneyStep.statesOf(events, journey(steps = steps))
        )
    }

    @Test
    fun `cle absente ou inconnue ou journey absent retombent sur les evenements`() {
        val events = listOf("onboarding.resource.welcome_watched")
        val expected = WelcomeJourneyStep.statesOf(events)
        assertEquals(expected, WelcomeJourneyStep.statesOf(events, null))
        assertEquals(expected, WelcomeJourneyStep.statesOf(events, journey(steps = null)))
        assertEquals(expected, WelcomeJourneyStep.statesOf(events, journey(steps = mapOf("welcome_watched" to "inconnu"))))
    }

    @Test
    fun `le back decide seulement quand removed est present`() {
        assertFalse(WelcomeJourneyServerState.isServerDriven(null))
        assertFalse(WelcomeJourneyServerState.isServerDriven(journey(removed = null)))
        assertTrue(WelcomeJourneyServerState.isServerDriven(journey(removed = false)))
        assertFalse(WelcomeJourneyServerState.isRemoved(journey(removed = false)))
        assertTrue(WelcomeJourneyServerState.isRemoved(journey(removed = true, reason = "hidden")))
    }

    @Test
    fun `felicitations seulement si retire car termine, affiche pendant la session et toutes les etapes faites sans aucune passee`() {
        val done = List(4) { WelcomeJourneyState.DONE }
        val oneSkipped = listOf(
            WelcomeJourneyState.DONE, WelcomeJourneyState.SKIPPED, WelcomeJourneyState.DONE, WelcomeJourneyState.DONE
        )
        val allSkipped = List(4) { WelcomeJourneyState.SKIPPED }
        val completed = journey(removed = true, reason = "completed")
        assertTrue(WelcomeJourneyServerState.shouldCongratulateOnRemoval(completed, done, true))
        assertFalse(WelcomeJourneyServerState.shouldCongratulateOnRemoval(completed, oneSkipped, true))
        assertFalse(WelcomeJourneyServerState.shouldCongratulateOnRemoval(completed, allSkipped, true))
        assertFalse(WelcomeJourneyServerState.shouldCongratulateOnRemoval(completed, done, false))
        assertFalse(WelcomeJourneyServerState.shouldCongratulateOnRemoval(journey(removed = true, reason = "hidden"), done, true))
        assertFalse(WelcomeJourneyServerState.shouldCongratulateOnRemoval(journey(removed = true, reason = "inactivity"), done, true))
        assertFalse(WelcomeJourneyServerState.shouldCongratulateOnRemoval(journey(removed = false), done, true))
    }

    @Test
    fun `passer une etape en local ne ramene pas une etape faite en arriere`() {
        val j = journey(steps = mapOf("welcome_watched" to "done", "papotages" to "todo"))
        WelcomeJourneyServerState.withSkipped(j, "welcome_watched")
        WelcomeJourneyServerState.withSkipped(j, "papotages")
        assertEquals("done", j.steps!!["welcome_watched"])
        assertEquals("skipped", j.steps!!["papotages"])
        WelcomeJourneyServerState.withSkipped(null, "papotages")
    }
}
