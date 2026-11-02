package social.entourage.android.home

import androidx.collection.ArrayMap
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.OnboardingJourney

enum class WelcomeJourneyState { TODO, DONE, SKIPPED }

/**
 * Les 4 étapes du parcours de bienvenue. Les événements `doneEvent` / `skippedEvent`
 * arrivent dans `summary.events`, `apiStep` est l'argument de POST users/onboarding_step_skipped.
 */
enum class WelcomeJourneyStep(
    val index: Int,
    val doneEvent: String,
    val skippedEvent: String,
    val apiStep: String,
    /** Autres événements qui valident aussi l'étape (étape 4 : `first_event`, inscription à un événement quelconque). */
    val alsoDoneEvents: List<String> = emptyList()
) {
    VIDEO(
        1,
        "onboarding.resource.welcome_watched",
        "onboarding.resource.welcome_watched_skipped",
        "welcome_watched"
    ),
    NATIONAL_GROUP(
        2,
        "onboarding.neighborhood.national",
        "onboarding.neighborhood.national_skipped",
        "neighborhood_national"
    ),
    WEBINAR(
        3,
        "onboarding.outing.webinar_or_first_steps",
        "onboarding.outing.webinar_or_first_steps_skipped",
        "webinar_or_first_steps"
    ),
    PAPOTAGES(
        4,
        "onboarding.outing.papotages",
        "onboarding.outing.papotages_skipped",
        "papotages",
        listOf("onboarding.outing.first_event")
    );

    /** "Fait" l'emporte sur "passé" : une étape passée puis découverte et terminée devient DONE. */
    fun stateOf(events: List<String>?): WelcomeJourneyState = when {
        events?.contains(doneEvent) == true || alsoDoneEvents.any { events?.contains(it) == true } ->
            WelcomeJourneyState.DONE
        events?.contains(skippedEvent) == true -> WelcomeJourneyState.SKIPPED
        else -> WelcomeJourneyState.TODO
    }

    companion object {
        fun fromIndex(index: Int): WelcomeJourneyStep? = entries.firstOrNull { it.index == index }

        fun statesOf(events: List<String>?): List<WelcomeJourneyState> =
            entries.map { it.stateOf(events) }

        /**
         * Quand le back porte `onboarding_journey.steps`, il fait foi ; une clé absente ou inconnue retombe
         * sur les événements. Sans `onboarding_journey` (back pas déployé), seuls les événements comptent.
         */
        fun statesOf(events: List<String>?, journey: OnboardingJourney?): List<WelcomeJourneyState> {
            val steps = journey?.steps
            return entries.map { step ->
                when (steps?.get(step.apiStep)) {
                    "done" -> WelcomeJourneyState.DONE
                    "skipped" -> WelcomeJourneyState.SKIPPED
                    "todo" -> WelcomeJourneyState.TODO
                    else -> step.stateOf(events)
                }
            }
        }
    }
}

object WelcomeJourneyApi {
    /** Appel réseau « Masquer le parcours » (le back garde la raison `hidden`). Remplaçable (tests e2e). */
    var hideHandler: (onResult: (Boolean) -> Unit) -> Unit = ::hideViaApi

    private fun hideViaApi(onResult: (Boolean) -> Unit) {
        EntourageApplication.get().apiModule.userRequest.hideOnboardingJourney()
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    onResult(response.isSuccessful)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onResult(false)
                }
            })
    }

    /**
     * Appel réseau pour passer une étape. Remplaçable (tests e2e) afin de ne pas modifier
     * l'état réel du compte de test.
     */
    var skipHandler: (step: WelcomeJourneyStep, onResult: (Boolean) -> Unit) -> Unit = ::skipViaApi

    private fun skipViaApi(step: WelcomeJourneyStep, onResult: (Boolean) -> Unit) {
        val body = ArrayMap<String, Any>().apply { put("step", step.apiStep) }
        EntourageApplication.get().apiModule.userRequest.skipOnboardingStep(body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    onResult(response.isSuccessful)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onResult(false)
                }
            })
    }
}

/**
 * Décision d'affichage du parcours quand le back porte son état (`onboarding_journey`). Sans
 * `onboarding_journey` (ou `removed` absent) le back n'est pas déployé : on garde les règles locales.
 */
object WelcomeJourneyServerState {
    /** Le back décide quand `removed` est présent. */
    fun isServerDriven(journey: OnboardingJourney?): Boolean = journey?.removed != null

    fun isRemoved(journey: OnboardingJourney?): Boolean = journey?.removed == true

    /** Félicitations à la sortie « terminé » : seulement si au moins une étape a été faite (pas toutes passées). */
    fun shouldCongratulateOnRemoval(journey: OnboardingJourney?, states: List<WelcomeJourneyState>, shownThisSession: Boolean): Boolean =
        isRemoved(journey) && journey?.removedReason == "completed" && shownThisSession &&
            states.any { it == WelcomeJourneyState.DONE }

    /** Reporte localement une étape passée (mise à jour optimiste) sans écraser une étape déjà faite. */
    fun withSkipped(journey: OnboardingJourney?, apiStep: String) {
        val steps = journey?.steps ?: return
        if (steps[apiStep] != "done") journey.steps = steps + (apiStep to "skipped")
    }
}

/** Règle front : après 14 jours sans connexion, les étapes restantes sont passées automatiquement. */
object WelcomeJourneyInactivity {
    const val DELAY_DAYS = 14
    private const val DAY_MILLIS = 24L * 60 * 60 * 1000

    /** `lastConnectionMillis` est null (ou <= 0) à la toute première connexion : on ne passe rien. */
    fun shouldAutoSkip(lastConnectionMillis: Long?, nowMillis: Long): Boolean {
        if (lastConnectionMillis == null || lastConnectionMillis <= 0L) return false
        return nowMillis - lastConnectionMillis >= DELAY_DAYS * DAY_MILLIS
    }
}
