package social.entourage.android.user

import android.os.Handler
import android.os.Looper
import timber.log.Timber
import java.io.File

class AvatarUploadPresenter constructor(private val activity: AvatarUploadView,
                                        avatarUploadRepository: AvatarUploadRepository,
                                        presenter: AvatarUpdatePresenter
) : PrepareAvatarUploadRepository.Callback, AvatarUploadRepository.Callback {
    private val presenter: AvatarUpdatePresenter
    private val avatarUploadRepository: AvatarUploadRepository
    private val prepareAvatarUploadRepository: PrepareAvatarUploadRepository =
        PrepareAvatarUploadRepository(this)
    private var file: File? = null
    private var avatarKey: String? = null

    fun uploadPhoto(file: File) {
        this.file = file
        prepareAvatarUploadRepository.prepareUpload()
    }

    override fun onPrepareUploadSuccess(avatarKey: String, presignedUrl: String) {
        this.avatarKey = avatarKey
        file?.let {avatarUploadRepository.uploadFile(it, presignedUrl) }
    }

    override fun onUploadSuccess() {
        // Delete the temporary file
        if (file?.delete() != true) {
            // Failed to delete the file
            Timber.d("Failed to delete the temporary photo file")
        }
        avatarKey?.let { Handler(Looper.getMainLooper()).post { presenter.updateUserPhoto(it) } }
    }

    override fun onRepositoryError() {
        Handler(Looper.getMainLooper()).post { activity.onUploadError() }
    }

    init {
        this.avatarUploadRepository = avatarUploadRepository
        this.avatarUploadRepository.setCallback(this)
        this.presenter = presenter
    }
}