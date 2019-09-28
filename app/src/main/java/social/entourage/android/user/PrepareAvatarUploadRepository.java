package social.entourage.android.user;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import retrofit2.Call;
import social.entourage.android.api.UserRequest;
import timber.log.Timber;

import javax.inject.Inject;

public class PrepareAvatarUploadRepository implements retrofit2.Callback<PrepareAvatarUploadRepository.Response> {

    private Callback callback;
    private UserRequest userRequest;

    @Inject
    public PrepareAvatarUploadRepository(final UserRequest userRequest) {
        this.userRequest = userRequest;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    void prepareUpload() {
        Request request = new Request("image/jpeg");
        userRequest.prepareAvatarUpload(request).enqueue(this);
    }


    @Override
    public void onResponse(@NonNull Call<Response> call, @NonNull retrofit2.Response<Response> response) {
        if (response.isSuccessful() && response.body() != null) {
            this.callback.onPrepareUploadSuccess(response.body().avatarKey, response.body().presignedUrl);
        } else {
            this.callback.onRepositoryError();
        }
    }

    @Override
    public void onFailure(@NonNull Call<Response> call, @NonNull Throwable t) {
        Timber.d(t);
        this.callback.onRepositoryError();
    }

    public interface Callback {
        void onPrepareUploadSuccess(String avatarKey, String presignedUrl);

        void onRepositoryError();
    }

    public class Request {
        private String content_type;

        private Request(String contentType) {
            this.content_type = contentType;
        }
    }

    public class Response {
        @SerializedName("avatar_key")
        String avatarKey;

        @SerializedName("presigned_url")
        private String presignedUrl;

        public Response(String avatar_key, String presigned_url) {
            this.avatarKey = avatar_key;
            this.presignedUrl = presigned_url;
        }
    }
}
