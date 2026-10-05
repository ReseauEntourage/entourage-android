package social.entourage.android.home

import org.junit.Assert.assertEquals
import org.junit.Test

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
}
