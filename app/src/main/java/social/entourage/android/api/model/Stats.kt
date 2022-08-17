package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Stats(
    @field:SerializedName("entourage_count") private var entourageCount: Int,
    @field:SerializedName("actions_count") var actionsCount: Int,
    @field:SerializedName("events_count") var eventsCount: Int,
    @field:SerializedName("good_waves_participation") var isGoodWavesValidated: Boolean,
    @field:SerializedName("contribution_creation_count") var contribCreationCount: Int,
    @field:SerializedName("outings_count") var outingsCount: Int,
    @field:SerializedName("neighborhoods_count") var neighborhoodsCount: Int,
    @field:SerializedName("ask_for_help_creation_count") var askCreationCount: Int
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