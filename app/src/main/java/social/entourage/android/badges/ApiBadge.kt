package social.entourage.android.badges

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class ApiBadge(
    @SerializedName("name") val name: String,
    @SerializedName("awarded_at") val awardedAt: String?,
    @SerializedName("metadata") val metadata: ApiBadgeMetadata?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readParcelable(ApiBadgeMetadata::class.java.classLoader)
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(awardedAt)
        parcel.writeParcelable(metadata, flags)
    }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<ApiBadge> {
        override fun createFromParcel(parcel: Parcel) = ApiBadge(parcel)
        override fun newArray(size: Int): Array<ApiBadge?> = arrayOfNulls(size)
    }
}

data class ApiBadgeMetadata(
    @SerializedName("target") val target: Int?,
    @SerializedName("current") val current: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(target)
        parcel.writeValue(current)
    }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<ApiBadgeMetadata> {
        override fun createFromParcel(parcel: Parcel) = ApiBadgeMetadata(parcel)
        override fun newArray(size: Int): Array<ApiBadgeMetadata?> = arrayOfNulls(size)
    }
}
