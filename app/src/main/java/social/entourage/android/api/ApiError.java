package social.entourage.android.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import retrofit2.Response;

import java.io.IOException;

public class ApiError {
    public final String code;

    private ApiError(String code) {
        this.code = code;
    }

    public static ApiError fromResponse(Response response) {
        ApiError error;

        try {
            error = new Gson().fromJson(response.errorBody().string(), ApiErrorContainer.class).error;
        } catch (IOException | JsonSyntaxException | NullPointerException e) {
            error = null;
        }

        if (error == null || error.code == null) {
            error = new ApiError("OTHER");
        }

        return error;
    }

    private static class ApiErrorContainer {
        private ApiError error;
    }
}
