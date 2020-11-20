package social.entourage.android.api.model.guide

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.google.maps.android.clustering.ClusterItem
import social.entourage.android.api.model.TimestampedObject
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class Poi : TimestampedObject(), Serializable, ClusterItem {
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

    @SerializedName("category_id")
    var categoryId = 0
    var longitude = 0.0
    var latitude = 0.0

    var partner_id:Int? = null

    @SerializedName("category_ids")
    var categories = ArrayList<Int>()

    var isSoliguide = false
    @SerializedName("hours")
    var openTimeTxt:String? = null
    @SerializedName("languages")
    var languagesTxt:String? = null
    @SerializedName("source_url")
    var soliguideUrl:String? = null

    var source = ""
    var uuid = ""
    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------
    override fun getPosition(): LatLng {
        return LatLng(latitude, longitude)
    }

    override fun getTitle(): String? {
        return name
    }

    override fun getSnippet(): String? {
        return null
    }

    // ----------------------------------
    // Timestamp methods
    // ----------------------------------
    override val timestamp: Date?
        get() = null

    override val type: Int
        get() = GUIDE_POI

    override fun hashString(): String {
        return HASH_STRING_HEAD + id
    }

    companion object {
        private const val serialVersionUID = 7523482427596761716L
        private const val HASH_STRING_HEAD = "Poi-"
    }
}