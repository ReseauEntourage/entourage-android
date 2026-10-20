package social.entourage.android.events.create
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable


enum class Recurrence(val value: Int) {
    NO_RECURRENCE(0),

    EVERY_WEEK(7),

    EVERY_TWO_WEEKS(14),
}

@KSerializable
data class Metadata(

    @SerialName("street_address")
    var streetAddress: String? = "",

    @SerialName("place_name")
    var placeName: String? = "",

    @SerialName("starts_at")
    var startsAt: String? = null,

    @SerialName("place_limit")
    var placeLimit: Int? = null,

    @SerialName("display_address")
    val displayAddress: String? = null,

    @SerialName("ends_at")
    var endsAt: String? = null,

    @SerialName("google_place_id")
    var googlePlaceId: String? = "",

    @SerialName("reserved_female")
    var reserved_female: Boolean? = null,

    @SerialName("pmr")
    var pmr: Boolean? = null,

    @SerialName("kids_friendly")
    var kidsFriendly: Boolean? = null) {
    fun endsAt(value: String) = apply {
        endsAt = value
    }

    fun startsAt(value: String) = apply {
        startsAt = value
    }

    fun streetAddress(value: String?) = apply {
        streetAddress = value
    }

    fun placeLimit(value: Int?) = apply {
        placeLimit = value
    }

    override fun toString(): String {
        return "EventMetadata(streetAddress=$streetAddress, startsAt=$startsAt, placeLimit=$placeLimit, displayAddress=$displayAddress, endsAt=$endsAt, googlePlaceId=$googlePlaceId, reserved_female=$reserved_female, pmr=$pmr, kidsFriendly=$kidsFriendly)"
    }

}

@KSerializable
data class CreateEvent(
    @SerialName("metadata")
    var metadata: Metadata? = Metadata(),

    @SerialName("description")
    var description: String? = null,

    @SerialName("title")
    var title: String? = null,

    @SerialName("event_url")
    var eventUrl: String? = null,

    @SerialName("online")
    var online: Boolean? = null,

    @SerialName("latitude")
    var latitude: Double? = null,

    @SerialName("longitude")
    var longitude: Double? = null,

    @SerialName("other_interest")
    var otherInterest: String? = null,

    @SerialName("interests")
    var interests: MutableList<String> = mutableListOf(),

    @SerialName("entourage_image_id")
    var entourageImageId: Int? = null,

    @SerialName("image_url")
    var imageUrl: String? = null,

    @SerialName("neighborhood_ids")
    var neighborhoodIds: MutableList<Int> = mutableListOf(),

    @SerialName("recurrency")
    var recurrence: Int? = null,

    var displayAddress: String? = null) {
    fun title(value: String) = apply {
        title = value
    }

    fun online(value: Boolean) = apply {
        online = value
    }

    fun eventUrl(value: String?) = apply {
        eventUrl = value
    }

    fun otherInterest(value: String) = apply {
        otherInterest = value
    }

    fun interests(value: MutableList<String>) = apply {
        interests = value
    }

    fun entourageImageId(value: Int?) = apply {
        entourageImageId = value
    }

    fun imageUrl(value: String?) = apply {
        imageUrl = value
    }

    fun description(value: String) = apply {
        description = value
    }

    fun neighborhoodIds(value: MutableList<Int>) = apply {
        neighborhoodIds = value
    }

    fun recurrence(value: Int) = apply {
        recurrence = value
    }

    fun latitude(value: Double) = apply {
        latitude = value
    }

    fun longitude(value: Double) = apply {
        longitude = value
    }

    override fun toString(): String {
        return "CreateEvent(metadata=$metadata, description=$description, title=$title, eventUrl=$eventUrl, online=$online, latitude=$latitude, longitude=$longitude, otherInterest=$otherInterest, interests=$interests, entourageImageId=$entourageImageId, imageUrl=$imageUrl, neighborhoodIds=$neighborhoodIds, recurrence=$recurrence)"
    }

}
