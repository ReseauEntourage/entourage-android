package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Stats (
        @field:SerializedName("tour_count") var tourCount: Int,
        @field:SerializedName("encounter_count") var encounterCount: Int,
        @field:SerializedName("entourage_count") private var entourageCount: Int,
        @field:SerializedName("actions_count") var actionsCount: Int,
        @field:SerializedName("events_count") var eventsCount: Int,
        @field:SerializedName("good_waves_participation") var isGoodWavesValidated: Boolean): Serializable {

    fun getActionCount(): Int {
        return entourageCount + tourCount
    }

    val isEngaged: Boolean
        get() = (tourCount>0
                    ||encounterCount>0
                    ||entourageCount>0
                    ||actionsCount>0
                    ||eventsCount>0
                    ||isGoodWavesValidated)

    companion object {
        private const val serialVersionUID = -90028118L
    }
}