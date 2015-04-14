package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import social.entourage.android.api.model.EncounterWrapper;
import social.entourage.android.api.model.map.Encounter;

public interface EncounterService {

    @POST("/encounters.json")
    void create(@Body EncounterWrapper encounterWrapper, Callback<EncounterResponse> callback);
}
