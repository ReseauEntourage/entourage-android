package social.entourage.android.badges

import android.os.Parcel
import android.os.Parcelable
import social.entourage.android.tools.utils.readParcelableCompat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
data class ApiBadge(
    @SerialName("name") val name: String = "",
    @SerialName("active") val active: Boolean = false,
    @SerialName("awarded_at") val awardedAt: String? = null,
    @SerialName("metadata") val metadata: ApiBadgeMetadata? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readParcelableCompat<ApiBadgeMetadata>(ApiBadgeMetadata::class.java.classLoader)
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

@KSerializable
data class ApiBadgeMetadata(
    @SerialName("target") val target: Int? = null,
    @SerialName("current") val current: Int? = null
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
