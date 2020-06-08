package social.entourage.android.user.edit.photo

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import social.entourage.android.api.tape.Events.OnPhotoChosen
import social.entourage.android.tools.BusProvider
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
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureIntent.clipData = ClipData.newUri(contentResolver, "A photo", photoFileUri)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                if (photoFileUri != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri)
                }
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            } catch (e: NullPointerException) {
                Timber.e(e)
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null && intent.data != null) {
                    BusProvider.instance.post(OnPhotoChosen(intent.data))
                    return
                }
                mCurrentPhotoPath?.let { BusProvider.instance.post(OnPhotoChosen(Uri.fromFile(File(it)))) }
            }
            finish()
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