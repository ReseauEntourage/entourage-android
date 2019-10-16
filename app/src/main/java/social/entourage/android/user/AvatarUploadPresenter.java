package social.entourage.android.user;

import android.os.Handler;
import android.os.Looper;

import javax.inject.Inject;
import java.io.File;

import timber.log.Timber;

public class AvatarUploadPresenter implements PrepareAvatarUploadRepository.Callback, AvatarUploadRepository.Callback {
    private final AvatarUpdatePresenter presenter;
    private AvatarUploadView activity;
    private PrepareAvatarUploadRepository prepareAvatarUploadRepository;
    private AvatarUploadRepository avatarUploadRepository;
    private File file;
    private String avatarKey;

    @Inject
    public AvatarUploadPresenter(AvatarUploadView activity,
                                 PrepareAvatarUploadRepository prepareAvatarUploadRepository,
                                 AvatarUploadRepository avatarUploadRepository,
                                 AvatarUpdatePresenter presenter) {
        this.activity = activity;
        this.prepareAvatarUploadRepository = prepareAvatarUploadRepository;
        this.prepareAvatarUploadRepository.setCallback(this);
        this.avatarUploadRepository = avatarUploadRepository;
        this.avatarUploadRepository.setCallback(this);
        this.presenter = presenter;
    }

    public void uploadPhoto(File file) {
        this.file = file;
        prepareAvatarUploadRepository.prepareUpload();
    }

    @Override
    public void onPrepareUploadSuccess(String avatarKey, String presignedUrl) {
        this.avatarKey = avatarKey;
        avatarUploadRepository.uploadFile(file, presignedUrl);
    }

    @Override
    public void onUploadSuccess() {
        // Delete the temporary file
        if (!file.delete()) {
            // Failed to delete the file
            Timber.d("Failed to delete the temporary photo file");
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                presenter.updateUserPhoto(avatarKey);
            }
        });
    }

    @Override
    public void onRepositoryError() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                activity.onUploadError();
            }
        });
    }
}
