package social.entourage.android.api.model.notification

import com.google.gson.annotations.SerializedName

data class Translation(
    @SerializedName("translation")
    val translation: String? = null,
    @SerializedName("original")
    val original: String? = null,
    @SerializedName("from_lang")
    val fromLang: String? = null,
    @SerializedName("to_lang")
    val toLang: String? = null
)