package social.entourage.android.new_v8.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


data class SettingUiModel(
    @SerializedName("id")
    var id: Int? = null,
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
    var recurrence: Int? = 0,
    val status: Status? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeValue(members_count)
        parcel.writeString(description)
        parcel.writeByte(if (member) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SettingUiModel> {
        override fun createFromParcel(parcel: Parcel): SettingUiModel {
            return SettingUiModel(parcel)
        }

        override fun newArray(size: Int): Array<SettingUiModel?> {
            return arrayOfNulls(size)
        }
    }

}
