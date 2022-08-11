package social.entourage.android.new_v8.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.util.*

class EventUiModel(
    @SerializedName("id")
    var id: Int?,

    @SerializedName("name")
    var name: String? = null,

    @SerializedName("members_count")
    var members_count: Int? = null,

    @SerializedName("address")
    var address: String? = null,

    @SerializedName("interests")
    var interests: MutableList<String> = mutableListOf(),

    @SerializedName("description")
    var description: String? = null,

    @SerializedName("members")
    var members: MutableList<GroupMember>? = mutableListOf(),

    @SerializedName("member")
    var member: Boolean = false,
    var admin: Boolean = false,
    var online: Boolean? = false,
    val metadata: Metadata? = null,
    val eventUrl: String? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val recurrence: Int? = null,
    var neighborhoods: MutableList<GroupEvent>? = mutableListOf(),
    val location: Address? = null,

    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as Int,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        TODO("interests"),
        parcel.readString(),
        TODO("members"),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeValue(members_count)
        parcel.writeString(address)
        parcel.writeString(description)
        parcel.writeByte(if (member) 1 else 0)
        parcel.writeByte(if (admin) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EventUiModel> {
        override fun createFromParcel(parcel: Parcel): EventUiModel {
            return EventUiModel(parcel)
        }

        override fun newArray(size: Int): Array<EventUiModel?> {
            return arrayOfNulls(size)
        }
    }

}