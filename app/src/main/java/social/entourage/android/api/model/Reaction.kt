package social.entourage.android.api.model.notification

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Reaction: Serializable {
    @SerializedName("id")
    val id:Int = 0
    @SerializedName("key")
    val key:String? = null
    @SerializedName("image_url")
    val imageUrl:String? = null
}