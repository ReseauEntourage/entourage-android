package social.entourage.android.user.edit.photo

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import social.entourage.android.api.tape.Events.OnPhotoChosen
import social.entourage.android.tools.BusProvider

@Deprecated(message="Only for Android 4.4")
class ChoosePhotoCompatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent()
        // Show only images, no videos or anything else
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, null), PICK_IMAGE_REQUEST)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            intent?.data?.let { uri -> BusProvider.instance.post(OnPhotoChosen(uri)) }
        }
        finish()
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}