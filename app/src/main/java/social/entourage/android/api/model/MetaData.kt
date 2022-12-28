package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

data class EventMetadata(

    @field:SerializedName("previous_at")
    val previousAt: Date? = null,

    @field:SerializedName("place_name")
    val placeName: String? = null,

    @field:SerializedName("street_address")
    val streetAddress: String? = null,

    @field:SerializedName("starts_at")
    val startsAt: Date? = null,

    @field:SerializedName("place_limit")
    val placeLimit: Int? = null,

    @field:SerializedName("landscape_thumbnail_url")
    val landscapeThumbnailUrl: String? = null,

    @field:SerializedName("portrait_thumbnail_url")
    val portraitThumbnailUrl: String? = null,

    @field:SerializedName("display_address")
    val displayAddress: String? = null,

    @field:SerializedName("ends_at")
    val endsAt: Date? = null,

    @field:SerializedName("google_place_id")
    val googlePlaceId: String? = null,

    @field:SerializedName("portrait_url")
    val portraitUrl: String? = null,

    @field:SerializedName("landscape_url")
    val landscapeUrl: String? = null
) : Serializable

data class ActionMetadata(

    @field:SerializedName("city")
    val city: String? = null,

    @field:SerializedName("display_address")
    val displayAddress: String? = null,

) : Serializable