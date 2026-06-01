package social.entourage.android.comment

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.MimeTypeMap
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

            // 1. On récupère l'extension du fichier depuis l'URL (ex: "jpg", "png")
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(url)

            // 2. On cherche le type MIME correspondant (ex: "image/jpeg").
            // Si l'URL n'a pas d'extension claire, on met "image/jpeg" par défaut.
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension) ?: "image/jpeg"

            // 3. On applique l'extension dynamiquement au nom du fichier
            val finalExtension = if (fileExtension.isNotEmpty()) fileExtension else "jpg"
            val fileName = "Entourage_${timeStamp}.${finalExtension}"

            request.setTitle(getString(R.string.download_image_title))
            request.setDescription(getString(R.string.download_image_description))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            // C'est cette ligne qui indique au système Android avec quelles applications ouvrir le fichier
            request.setMimeType(mimeType)

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
