package social.entourage.android.badges

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class ApiBadge(
    @SerializedName("name") val name: String,
    @SerializedName("active") val active: Boolean = false,
    @SerializedName("awarded_at") val awardedAt: String?,
    @SerializedName("metadata") val metadata: ApiBadgeMetadata?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readParcelable(ApiBadgeMetadata::class.java.classLoader)
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeByte(if (active) 1 else 0)
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
        if (parcel.readByte() != 0.toByte()) parcel.readInt() else null,
        if (parcel.readByte() != 0.toByte()) parcel.readInt() else null
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (target != null) 1 else 0)
        if (target != null) parcel.writeInt(target)
        parcel.writeByte(if (current != null) 1 else 0)
        if (current != null) parcel.writeInt(current)
    }
    override fun describeContents() = 0
    companion object CREATOR : Parcelable.Creator<ApiBadgeMetadata> {
        override fun createFromParcel(parcel: Parcel) = ApiBadgeMetadata(parcel)
        override fun newArray(size: Int): Array<ApiBadgeMetadata?> = arrayOfNulls(size)
    }
}
