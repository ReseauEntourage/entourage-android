package social.entourage.android.api.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class Image() : Parcelable {
    @SerializedName("id")
    var id: Int? = null

    @SerializedName("title")
    var title: String? = null

    @SerializedName("image_url")
    var imageUrl: String? = null

    @SerializedName("portrait_url")
    var portraitUrl: String? = null

    @SerializedName("landscape_url")
    var landscapeUrl: String? = null


    var isSelected: Boolean? = false

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        title = parcel.readString()
        imageUrl = parcel.readString()
        portraitUrl = parcel.readString()
        landscapeUrl = parcel.readString()
        isSelected = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(title)
        parcel.writeString(imageUrl)
        parcel.writeString(portraitUrl)
        parcel.writeString(landscapeUrl)
        parcel.writeValue(isSelected)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Image> {
        override fun createFromParcel(parcel: Parcel): Image {
            return Image(parcel)
        }

        override fun newArray(size: Int): Array<Image?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "Image(id=$id, title=$title, imageUrl=$imageUrl, portraitUrl=$portraitUrl, landscapeUrl=$landscapeUrl, isSelected=$isSelected)"
    }
}