cat << 'INNER_EOF' > app/src/main/java/social/entourage/android/comment/ImageZoomActivity.kt
package social.entourage.android.comment

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityImageZoomBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageZoomActivity : BaseActivity() {

    private lateinit var binding: ActivityImageZoomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageZoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl = intent.getStringExtra("image_url")

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.place_holder_large)
            .error(R.drawable.place_holder_large)
            .into(binding.fullscreenImage)

        binding.fullscreenImage.setOnClickListener {
            finish()
        }
        binding.btnQuitPhoto.setOnClickListener {
            finish()
        }
        binding.btnDownloadPhoto.setOnClickListener {
            imageUrl?.let { url ->
                downloadImage(url)
            }
        }
    }

    private fun downloadImage(url: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Entourage_$timeStamp.jpg"

            request.setTitle(getString(R.string.download_image_title))
            request.setDescription(getString(R.string.download_image_description))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(true)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(this, getString(R.string.download_started), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.download_error), Toast.LENGTH_SHORT).show()
        }
    }
}
INNER_EOF
