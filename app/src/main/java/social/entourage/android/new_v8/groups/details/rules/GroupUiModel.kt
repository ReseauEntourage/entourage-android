package social.entourage.android.new_v8.groups.details.rules

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import social.entourage.android.new_v8.models.Address


data class GroupUiModel(
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("members_count")
    var members_count: Int? = null,
    @SerializedName("address")
    var address: Address? = null,
    @SerializedName("interests")
    var interests: MutableList<String> = mutableListOf(),
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,

        )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeValue(members_count)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "GroupUiModel(id=$id, name=$name, members_count=$members_count, address=$address, interests=$interests)"
    }

    companion object CREATOR : Parcelable.Creator<GroupUiModel> {
        override fun createFromParcel(parcel: Parcel): GroupUiModel {
            return GroupUiModel(parcel)
        }

        override fun newArray(size: Int): Array<GroupUiModel?> {
            return arrayOfNulls(size)
        }
    }
}
