package social.entourage.android.api.model.notification

import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
data class Translation(
    @SerialName("translation")
    val translation: String? = null,
    @SerialName("original")
    val original: String? = null,
    @SerialName("from_lang")
    val fromLang: String? = null,
    @SerialName("to_lang")
    val toLang: String? = null
) : Serializable