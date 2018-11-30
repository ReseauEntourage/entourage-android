package social.entourage.android.user;

import okhttp3.*;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

public class AvatarUploadRepository implements Callback {
    private OkHttpClient client;
    private Callback callback;

    @Inject
    public AvatarUploadRepository(OkHttpClient client) {
        this.client = client;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    void uploadFile(File file, String presignedUrl) {
        MediaType mediaType = MediaType.parse("image/jpeg");
        RequestBody requestBody = RequestBody.create(mediaType, file);
        Request request = new Request.Builder()
                .url(presignedUrl)
                .put(requestBody)
                .build();
        client.newCall(request).enqueue(this);
    }

    @Override
    public void onFailure(Call call, IOException e) {
        callback.onRepositoryError();
    }

    @Override
    public void onResponse(Call call, Response response) {
        if (response.isSuccessful()) {
            callback.onUploadSuccess();
        } else {
            callback.onRepositoryError();
        }
    }

    public interface Callback {
        void onUploadSuccess();

        void onRepositoryError();
    }
}
