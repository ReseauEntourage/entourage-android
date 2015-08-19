package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import social.entourage.android.api.model.map.Encounter;

public interface EncounterRequest {

    @Headers({"Accept: application/json"})
    @POST("/tours/{tour_id}/encounters.json")
    void create( @Path("tour_id") long tourId, @Body Encounter.EncounterWrapper encounterWrapper, Callback<EncounterResponse> callback);
}
