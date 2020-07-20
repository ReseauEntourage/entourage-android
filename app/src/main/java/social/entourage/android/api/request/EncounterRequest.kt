package social.entourage.android.api.request

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import social.entourage.android.api.model.tour.Encounter

class EncounterResponse(val encounter: Encounter)

class EncounterWrapper (var encounter: Encounter)

class EncounterListResponse (var encounters: List<Encounter>)

interface EncounterRequest {
    @POST("tours/{tour_id}/encounters.json")
    fun create(@Path("tour_id") tourId: String?, @Body encounterWrapper: EncounterWrapper): Call<EncounterResponse>

    @PATCH("encounters/{encounter_id}")
    fun update(@Path("encounter_id") encounterId: Long, @Body encounterWrapper: EncounterWrapper): Call<EncounterResponse>
}