package social.entourage.android.events.create

import android.content.Context
import timber.log.Timber
import social.entourage.android.api.json.AppJson

/**
 * Brouillon local de création d'événement : un seul par utilisateur, stocké sur l'appareil dans
 * les SharedPreferences (la clé porte l'identifiant utilisateur, donc un autre compte sur le même
 * appareil ne voit pas le brouillon). Jamais utilisé en édition.
 */
object CreateEventDraftStore {

    private const val PREFS_NAME = "create_event_draft"
    private const val KEY_PREFIX = "draft_user_"

    private fun key(userId: Int) = "$KEY_PREFIX$userId"

    fun save(context: Context, userId: Int, form: CreateEventForm) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(key(userId), AppJson.encode(form))
            .apply()
    }

    fun load(context: Context, userId: Int): CreateEventForm? {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(key(userId), null) ?: return null
        return try {
            AppJson.decode<CreateEventForm>(json)
        } catch (e: IllegalArgumentException) { // SerializationException en est une sous-classe
            Timber.e(e, "Brouillon d'événement illisible, ignoré")
            null
        }
    }

    fun delete(context: Context, userId: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(key(userId))
            .apply()
    }
}
