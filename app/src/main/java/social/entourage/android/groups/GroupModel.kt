package social.entourage.android.groups

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import social.entourage.android.api.model.GroupMember
import social.entourage.android.api.model.Status

data class GroupModel(
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("uuid_v2")
    var uuid_v2: String? = null,
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
    var recurrence: Int? = 0,
    val status: Status? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        mutableListOf<String>().apply { parcel.readStringList(this)},
        parcel.readString(),
        mutableListOf<GroupMember>().apply { parcel.readList(this, GroupMember::class.java.classLoader)},
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeValue(uuid_v2)
        parcel.writeValue(members_count)
        parcel.writeString(address)
        parcel.writeStringList(interests)
        parcel.writeString(description)
        parcel.writeList(members)
        parcel.writeByte(if (member) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GroupModel> {
        override fun createFromParcel(parcel: Parcel): GroupModel {
            return GroupModel(parcel)
        }

        override fun newArray(size: Int): Array<GroupModel?> {
            return arrayOfNulls(size)
        }
    }

}
