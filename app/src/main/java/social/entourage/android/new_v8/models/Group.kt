package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName

data class Group(
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("welcome_message")
    var welcomeMessage: String? = null,
    @SerializedName("ethics")
    var ethics: String? = null,
    @SerializedName("latitude")
    var latitude: Double? = null,
    @SerializedName("longitude")
    var longitude: Double? = null,
    @SerializedName("neighborhood_image_id")
    var neighborhoodImageId: Int? = null,
    @SerializedName("other_interest")
    var otherInterest: String? = null,
    @SerializedName("interests")
    var interests: MutableList<String> = mutableListOf(),
    @SerializedName("image_url")
    var imageUrl: String? = null,
    @SerializedName("members")
    var members: MutableList<GroupMember>? = mutableListOf(),
    @SerializedName("future_outings_count")
    var futureOutingsCount: Int? = null
) {

    fun name(value: String) = apply {
        name = value
    }

    fun description(value: String) = apply {
        description = value
    }

    fun welcomeMessage(value: String) = apply {
        welcomeMessage = value
    }

    fun ethics(value: String) = apply {
        ethics = value
    }

    fun latitude(value: Double) = apply {
        latitude = value
    }

    fun longitude(value: Double) = apply {
        longitude = value
    }

    fun neighborhoodImageId(value: Int?) = apply {
        neighborhoodImageId = value
    }

    fun otherInterest(value: String) = apply {
        otherInterest = value
    }

    fun interests(value: MutableList<String>) = apply {
        interests = value
    }

    override fun toString(): String {
        return "Group(id=$id, name=$name, description=$description, welcomeMessage=$welcomeMessage, ethics=$ethics, latitude=$latitude, longitude=$longitude, neighborhoodImageId=$neighborhoodImageId, otherInterest=$otherInterest, interests=$interests, imageUrl=$imageUrl)"
    }

}