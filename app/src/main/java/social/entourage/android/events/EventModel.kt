@file:UseSerializers(ApiDateSerializer::class)

package social.entourage.android.events

import android.os.Parcel
import android.os.Parcelable
import social.entourage.android.api.model.*
import social.entourage.android.tools.utils.readListCompat
import social.entourage.android.tools.utils.readSerializableCompat
import social.entourage.android.api.model.feed.FeedItemAuthor
import java.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.UseSerializers
import social.entourage.android.api.json.ApiDateSerializer

@KSerializable
class EventModel(
    @SerialName("id")
    var id: Int? = null,

    @SerialName("name")
    var name: String? = null,
    @SerialName("author")
    var author: FeedItemAuthor? = null,

    @SerialName("members_count")
    var members_count: Int? = null,

    @SerialName("address")
    var address: String? = null,

    @SerialName("interests")
    var interests: MutableList<String> = mutableListOf(),

    @SerialName("description")
    var description: String? = null,

    @SerialName("members")
    var members: MutableList<GroupMember>? = mutableListOf(),

    @SerialName("member")
    var member: Boolean = false,
    var admin: Boolean = false,
    var online: Boolean? = false,
    val metadata: EventMetadata? = null,
    val eventUrl: String? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val recurrence: Int? = null,
    var neighborhoods: MutableList<GroupEvent>? = mutableListOf(),
    val location: Address? = null,
    val distance: Double? = null,
    val status: Status? = null,
    val previousAt: Date? = null) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as Int,
        parcel.readString(),
        parcel.readSerializableCompat<FeedItemAuthor>(FeedItemAuthor::class.java.classLoader),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        mutableListOf<String>().apply { parcel.readStringList(this)},
        parcel.readString(),
        mutableListOf<GroupMember>().apply { parcel.readListCompat(this, GroupMember::class.java.classLoader)},
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeSerializable(author)
        parcel.writeValue(members_count)
        parcel.writeString(address)
        parcel.writeStringList(interests)
        parcel.writeString(description)
        parcel.writeList(members)
        parcel.writeByte(if (member) 1 else 0)
        parcel.writeByte(if (admin) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EventModel> {
        override fun createFromParcel(parcel: Parcel): EventModel {
            return EventModel(parcel)
        }

        override fun newArray(size: Int): Array<EventModel?> {
            return arrayOfNulls(size)
        }
    }

}