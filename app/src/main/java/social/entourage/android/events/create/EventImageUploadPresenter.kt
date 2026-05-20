package social.entourage.android.events.create

import android.os.Handler
import android.os.Looper
import timber.log.Timber
import java.io.File

interface EventImageUploadView {
    fun onUploadError()
    fun onUploadSuccess(uploadKey: String)
}

class EventImageUploadPresenter(
    private val view: EventImageUploadView,
    private val uploadRepository: EventImageUploadRepository
) : PrepareEventImageUploadRepository.Callback, EventImageUploadRepository.Callback {

    private val prepareUploadRepository = PrepareEventImageUploadRepository(this)
    private var file: File? = null
    private var uploadKey: String? = null

    init {
        uploadRepository.setCallback(this)
    }

    fun uploadPhoto(file: File) {
        this.file = file
        prepareUploadRepository.prepareUpload()
    }

    override fun onPrepareUploadSuccess(uploadKey: String, presignedUrl: String) {
        this.uploadKey = uploadKey
        file?.let { uploadRepository.uploadFile(it, presignedUrl) }
    }

    override fun onUploadSuccess() {
        if (file?.delete() != true) {
            Timber.d("Failed to delete the temporary photo file")
        }
        uploadKey?.let {
            Handler(Looper.getMainLooper()).post { view.onUploadSuccess(it) }
        }
    }

    override fun onRepositoryError() {
        Handler(Looper.getMainLooper()).post { view.onUploadError() }
    }
}
