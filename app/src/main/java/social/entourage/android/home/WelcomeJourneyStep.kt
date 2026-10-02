package social.entourage.android.home

import androidx.collection.ArrayMap
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication

enum class WelcomeJourneyState { TODO, DONE, SKIPPED }

/**
 * Les 4 étapes du parcours de bienvenue. Les événements `doneEvent` / `skippedEvent`
 * arrivent dans `summary.events`, `apiStep` est l'argument de POST users/onboarding_step_skipped.
 */
enum class WelcomeJourneyStep(
    val index: Int,
    val doneEvent: String,
    val skippedEvent: String,
    val apiStep: String
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
        "papotages"
    );

    /** "Fait" l'emporte sur "passé" : une étape passée puis découverte et terminée devient DONE. */
    fun stateOf(events: List<String>?): WelcomeJourneyState = when {
        events?.contains(doneEvent) == true -> WelcomeJourneyState.DONE
        events?.contains(skippedEvent) == true -> WelcomeJourneyState.SKIPPED
        else -> WelcomeJourneyState.TODO
    }

    companion object {
        fun fromIndex(index: Int): WelcomeJourneyStep? = entries.firstOrNull { it.index == index }

        fun statesOf(events: List<String>?): List<WelcomeJourneyState> =
            entries.map { it.stateOf(events) }
    }
}

object WelcomeJourneyApi {
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
