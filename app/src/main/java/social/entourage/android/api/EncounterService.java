package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import social.entourage.android.api.model.map.Encounter;

public interface EncounterService {

    @POST("/login.json")
    void create(@Body Encounter encounter, Callback<EncounterResponse> callback);
}
