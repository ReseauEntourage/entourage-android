package social.entourage.android.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import social.entourage.android.api.model.map.Encounter;

public interface EncounterRequest {

    @POST("tours/{tour_id}/encounters.json")
    Call<EncounterResponse> create( @Path("tour_id") long tourId, @Body Encounter.EncounterWrapper encounterWrapper);

    @PATCH("tours/{tour_id}/encounters/{encounter_id}")
    Call<EncounterResponse> edit( @Path("tour_id") long tourId, @Path("encounter_id") long encounterId, @Body Encounter.EncounterWrapper encounterWrapper);
}
