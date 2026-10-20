@file:UseSerializers(ApiDateSerializer::class)

package social.entourage.android.api.model.guide

import com.google.android.gms.maps.model.LatLng
import social.entourage.android.api.model.TimestampedObject
import java.io.Serializable
import java.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.UseSerializers
import social.entourage.android.api.json.ApiDateSerializer

@KSerializable
class Poi : TimestampedObject(), Serializable {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    override var id: Long = 0
    var name: String? = null
    var description: String? = null

    var address: String? = null
    var phone: String? = null
    var website: String? = null
    var email: String? = null
    var audience: String? = null

    @SerialName("category_id")
    var categoryId = 0
    var longitude = 0.0
    var latitude = 0.0

    var partner_id:Int? = null

    @SerialName("category_ids")
    var categories = ArrayList<Int>()

    var isSoliguide = false
    @SerialName("hours")
    var openTimeTxt:String? = null
    @SerialName("languages")
    var languagesTxt:String? = null
    @SerialName("source_url")
    var soliguideUrl:String? = null
    @SerialName("source_category_id")
    var soliguideId:Int? = null

    var source = ""
    var uuid = ""

    @SerialName("air_conditioned")
    var airConditioned: Boolean? = null
    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------
    val position: LatLng
        get() = LatLng(latitude, longitude)

    val title : String?
        get() = name

    // ----------------------------------
    // Timestamp methods
    // ----------------------------------
    override val timestamp: Date?
        get() = null

    override val type: Int
        get() = GUIDE_POI

    override fun hashString(): String {
        return HASH_STRING_HEAD + uuid
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is Poi) false else uuid == other.uuid
    }

    companion object {
        private const val serialVersionUID = 7523482427596761716L
        private const val HASH_STRING_HEAD = "Poi-"
    }
}