package social.entourage.android.api.model.tour

import android.location.Address
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import social.entourage.android.api.model.TimestampedObject
import java.io.Serializable
import java.util.*

class Encounter : TimestampedObject(), Serializable {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    override var id: Long = 0
    var tourId: String? = null

    @SerializedName("date")
    var creationDate: Date? = null
    var longitude = 0.0
    var latitude = 0.0

    @SerializedName("user_id")
    var userId = 0

    @SerializedName("user_name")
    var userName: String? = null
        get() = if (field == null) "" else field

    @SerializedName("street_person_name")
    var streetPersonName: String? = null
    var message: String? = null

    @Transient
    var address: Address? = null

    @Expose(serialize = false)
    var isMyEncounter = false

    @Expose(serialize = false)
    var isReadOnly = false

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------
    override val timestamp: Date?
        get() = creationDate

    override fun hashString(): String {
        return HASH_STRING_HEAD + id
    }

    override fun equals(other: Any?): Boolean {
        return !(other == null || other.javaClass != this.javaClass) && id == (other as Encounter).id
    }

    override val type: Int
        get() = ENCOUNTER

    companion object {
        private const val serialVersionUID = -274671974155989518L
        private const val HASH_STRING_HEAD = "Encounter-"
    }
}