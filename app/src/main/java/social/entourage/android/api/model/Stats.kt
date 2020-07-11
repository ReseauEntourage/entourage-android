package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Stats (
        @field:SerializedName("tour_count") var tourCount: Int,
        @field:SerializedName("encounter_count") var encounterCount: Int,
        @field:SerializedName("entourage_count") private var entourageCount: Int) : Serializable {

    fun getActionCount(): Int {
        return entourageCount + tourCount
    }

    companion object {
        private const val serialVersionUID = -90028118L
    }
}