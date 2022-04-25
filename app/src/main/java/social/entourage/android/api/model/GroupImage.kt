package social.entourage.android.api.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class GroupImage() : Parcelable {
    @SerializedName("id")
    var id: Int? = null

    @SerializedName("title")
    var title: String? = null

    @SerializedName("image_url")
    var imageUrl: String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        title = parcel.readString()
        imageUrl = parcel.readString()
    }

    override fun toString(): String {
        return "GroupImage(id=$id, title=$title, imageUrl=$imageUrl)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(title)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GroupImage> {
        override fun createFromParcel(parcel: Parcel): GroupImage {
            return GroupImage(parcel)
        }

        override fun newArray(size: Int): Array<GroupImage?> {
            return arrayOfNulls(size)
        }
    }
}