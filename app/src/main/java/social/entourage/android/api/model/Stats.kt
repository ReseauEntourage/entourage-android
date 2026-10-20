package social.entourage.android.api.model

import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
class Stats(
    @SerialName("entourage_count") private var entourageCount: Int = 0,
    @SerialName("actions_count") var actionsCount: Int = 0,
    @SerialName("events_count") var eventsCount: Int = 0,
    @SerialName("good_waves_participation") var isGoodWavesValidated: Boolean = false,
    @SerialName("contribution_creation_count") var contribCreationCount: Int = 0,
    @SerialName("outings_count") var outingsCount: Int = 0,
    @SerialName("neighborhoods_count") var neighborhoodsCount: Int = 0,
    @SerialName("ask_for_help_creation_count") var askCreationCount: Int = 0
) : Serializable {

    fun getActionCount(): Int {
        return entourageCount
    }

    val isEngaged: Boolean
        get() = (entourageCount > 0
                || actionsCount > 0
                || eventsCount > 0
                || isGoodWavesValidated)

    companion object {
        private const val serialVersionUID = -90028118L
    }
}