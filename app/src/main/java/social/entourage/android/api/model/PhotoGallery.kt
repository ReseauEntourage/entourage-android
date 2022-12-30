package social.entourage.android.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Jr on 21/05/2021.
 */

/*****
 * Model
 */

class PhotoGallery : Serializable {
    @SerializedName("landscape_url")
    var url_image_landscape:String = ""
    @SerializedName("portrait_url")
    var url_image_portrait:String = ""
    @SerializedName("landscape_small_url")
    var url_image_landscape_light:String = ""
}