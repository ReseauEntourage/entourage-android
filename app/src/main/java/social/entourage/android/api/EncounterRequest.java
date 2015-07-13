package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import social.entourage.android.api.model.EncounterWrapper;

public interface EncounterRequest {

    @POST("/encounters.json")
    void create(@Body EncounterWrapper encounterWrapper, Callback<EncounterResponse> callback);
}
