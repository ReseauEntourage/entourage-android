@file:UseSerializers(ApiDateSerializer::class)

package social.entourage.android.api.model

import java.io.Serializable
import java.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.UseSerializers
import social.entourage.android.api.json.ApiDateSerializer

@KSerializable
data class EventMetadata(

    @SerialName("previous_at")
    val previousAt: Date? = null,

    @SerialName("place_name")
    val placeName: String? = null,

    @SerialName("street_address")
    val streetAddress: String? = null,

    @SerialName("starts_at")
    val startsAt: Date? = null,

    @SerialName("place_limit")
    val placeLimit: Int? = null,

    @SerialName("landscape_thumbnail_url")
    val landscapeThumbnailUrl: String? = null,

    @SerialName("portrait_thumbnail_url")
    val portraitThumbnailUrl: String? = null,

    @SerialName("display_address")
    val displayAddress: String? = null,

    @SerialName("ends_at")
    val endsAt: Date? = null,

    @SerialName("google_place_id")
    val googlePlaceId: String? = null,

    @SerialName("portrait_url")
    val portraitUrl: String? = null,


    @SerialName("landscape_url")
    val landscapeUrl: String? = null,

    @SerialName("reserved_female")
    val reserved_female: Boolean? = null,

    @SerialName("pmr")
    val pmr: Boolean? = null,

    @SerialName("kids_friendly")
    val kidsFriendly: Boolean? = null,

    @SerialName("unsubscribed_participants_offer_help")
    var unsubscribedParticipantsOfferHelp: Int? = 0,

    @SerialName("unsubscribed_participants_ask_for_help")
    var unsubscribedParticipantsAskForHelp: Int? = 0,

    @SerialName("unsubscribed_participants_female")
    var unsubscribedParticipantsFemale: Int? = 0) : Serializable

@KSerializable
data class ActionMetadata(

    @SerialName("city")
    val city: String? = null,

    @SerialName("display_address")
    val displayAddress: String? = null) : Serializable