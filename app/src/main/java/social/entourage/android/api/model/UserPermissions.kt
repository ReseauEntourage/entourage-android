package social.entourage.android.api.model

import java.io.Serializable

/**
 * Created by Jerome on 14/12/2021.
 */
class UserPermissions: HashMap<String, HashMap<String, Boolean>>(), Serializable {
    private val KEY_OUTING = "outing"
    private val KEY_CREATION = "creation"

    fun isEventCreationActive() : Boolean {
        val isEventCreationActive = this[KEY_OUTING]?.get(KEY_CREATION)
        return isEventCreationActive ?: false
    }
}