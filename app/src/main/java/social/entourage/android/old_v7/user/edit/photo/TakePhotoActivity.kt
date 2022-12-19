package social.entourage.android.old_v7.user.edit.photo

import androidx.appcompat.app.AppCompatActivity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import social.entourage.android.api.tape.Events.OnPhotoChosen
import social.entourage.android.tools.EntBus
import timber.log.Timber
import java.io.File

class TakePhotoActivity : AppCompatActivity() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var mCurrentPhotoPath: String? = null

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the take photo intent
        intent?.let { intent ->
            val photoFileUri = intent.data
            mCurrentPhotoPath = intent.getStringExtra(KEY_PHOTO_PATH)
            try {
                val resultLauncher = registerForActivityResult(
                    object : ActivityResultContracts.TakePicture() {
                        override fun createIntent(
                            context: Context,
                            input: Uri
                        ): Intent {
                            val takePictureIntent = super.createIntent(context, input)
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                                takePictureIntent.clipData =
                                    ClipData.newUri(contentResolver, "A photo", photoFileUri)
                                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                if (photoFileUri != null) {
                                    takePictureIntent.putExtra(
                                        MediaStore.EXTRA_OUTPUT,
                                        photoFileUri
                                    )
                                }
                            }
                            return takePictureIntent
                        }
                    }) { success ->
                    if (success) {
                        photoFileUri.let { data ->
                            EntBus.post(data?.let { OnPhotoChosen(it) })
                            return@let
                        }
                        mCurrentPhotoPath?.let { EntBus.post(OnPhotoChosen(Uri.fromFile(File(it)))) }

                    }
                    finish()
                }
                resultLauncher.launch(photoFileUri)

            } catch (e: NullPointerException) {
                Timber.e(e)
            }
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        private const val REQUEST_TAKE_PHOTO = 2
        const val KEY_PHOTO_PATH = "social.entourage.android.photo_path"
    }
}