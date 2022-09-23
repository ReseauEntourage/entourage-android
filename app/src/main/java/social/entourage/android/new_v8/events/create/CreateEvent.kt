package social.entourage.android.new_v8.events.create

import com.google.gson.annotations.SerializedName

enum class Recurrence(val value: Int) {
    NO_RECURRENCE(0),

    EVERY_WEEK(7),

    EVERY_TWO_WEEKS(14),
}

data class Metadata(

    @field:SerializedName("street_address")
    var streetAddress: String? = "",

    @field:SerializedName("place_name")
    var placeName: String? = "",

    @field:SerializedName("starts_at")
    var startsAt: String? = null,

    @field:SerializedName("place_limit")
    var placeLimit: Int? = null,

    @field:SerializedName("display_address")
    val displayAddress: String? = null,

    @field:SerializedName("ends_at")
    var endsAt: String? = null,

    @field:SerializedName("google_place_id")
    var googlePlaceId: String? = "",
) {
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
        return "Metadata(streetAddress=$streetAddress, startsAt=$startsAt, placeLimit=$placeLimit, displayAddress=$displayAddress, endsAt=$endsAt, googlePlaceId=$googlePlaceId)"
    }


}

data class CreateEvent(
    @field:SerializedName("metadata")
    var metadata: Metadata? = Metadata(),

    @field:SerializedName("description")
    var description: String? = null,

    @field:SerializedName("title")
    var title: String? = null,

    @field:SerializedName("event_url")
    var eventUrl: String? = null,

    @field:SerializedName("online")
    var online: Boolean? = null,

    @field:SerializedName("latitude")
    var latitude: Double? = null,

    @field:SerializedName("longitude")
    var longitude: Double? = null,

    @SerializedName("other_interest")
    var otherInterest: String? = null,

    @SerializedName("interests")
    var interests: MutableList<String> = mutableListOf(),

    @SerializedName("entourage_image_id")
    var entourageImageId: Int? = null,

    @SerializedName("neighborhood_ids")
    var neighborhoodIds: MutableList<Int> = mutableListOf(),

    @SerializedName("recurrency")
    var recurrence: Int? = null,

    var displayAddress: String? = null,


    ) {
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
        return "CreateEvent(metadata=$metadata, description=$description, title=$title, eventUrl=$eventUrl, online=$online, latitude=$latitude, longitude=$longitude, otherInterest=$otherInterest, interests=$interests, entourageImageId=$entourageImageId, neighborhoodIds=$neighborhoodIds, recurrence=$recurrence)"
    }

}
