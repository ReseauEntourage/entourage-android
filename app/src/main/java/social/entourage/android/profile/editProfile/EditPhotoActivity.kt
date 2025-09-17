package social.entourage.android.profile.editProfile

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityEditPhotoBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.onboarding.onboard.OnboardingEditPhotoFragment
import social.entourage.android.profile.ProfilePresenter
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.user.AvatarUploadPresenter
import social.entourage.android.user.AvatarUploadRepository
import social.entourage.android.user.AvatarUploadView
import social.entourage.android.user.edit.photo.PhotoEditInterface
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditPhotoActivity : BaseActivity(), PhotoEditInterface, AvatarUploadView {

    private lateinit var binding: ActivityEditPhotoBinding

    private var pickedImageUri: Uri? = null
    protected var pickedImageEditedUri: Uri? = null
    private var photoSource = 0
    private var mCurrentPhotoFile: File? = null
    private lateinit var avatarUploadPresenter: AvatarUploadPresenter

    private val readMediaPermission: String = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_EXTERNAL_STORAGE else Manifest.permission.READ_MEDIA_IMAGES

    val profilePresenter: ProfilePresenter by lazy { ProfilePresenter() }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            photoSource = PICK_IMAGE_REQUEST
            if (PermissionChecker.checkSelfPermission(this, readMediaPermission) != PermissionChecker.PERMISSION_GRANTED) {
                pickedImageUri = it
                requestReadPicturePermissionLauncher.launch(readMediaPermission)
            } else {
                loadPickedImage(it)
            }
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && mCurrentPhotoFile != null) {
            photoSource = TAKE_PHOTO_REQUEST
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                mCurrentPhotoFile!!
            )
            showNextStep(uri)
        } else {
            Toast.makeText(this, R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show()
        }
    }

    private val requestTakePicturePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showTakePhotoActivity()
        } else {
            Toast.makeText(this, R.string.user_photo_error_camera_permission, Toast.LENGTH_LONG).show()
        }
    }

    private val requestReadPicturePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickedImageUri?.let { loadPickedImage(it) } ?: showChoosePhotoActivity()
        } else {
            Toast.makeText(this, R.string.user_photo_error_read_permission, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        avatarUploadPresenter = AvatarUploadPresenter(this, AvatarUploadRepository(), profilePresenter)

        setupViews()
        updateUserView()
        setBackButton()
    }

    private fun setupViews() {
        if (!isFromSmallTalk) {
            binding.progressViewBar.visibility = View.GONE
            binding.buttonSmalltalkContinu.visibility = View.GONE
            binding.buttonSmalltalkPrevious.visibility = View.GONE
        } else {
            AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__SMALLTALK__PHOTO)
            binding.header.layout.visibility = View.GONE
            binding.title.text = getString(R.string.edit_photo_title)
            binding.subtitle.text = getString(R.string.edit_photo_subtitle)
        }

        binding.buttonSmalltalkPrevious.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.CLIC__SMALLTALK__PHOTO_PREVIOUS)
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.buttonSmalltalkContinu.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.CLIC__SMALLTALK__PHOTO_FINISH)
            setResult(RESULT_OK)
            finish()
        }

        binding.buttonGallery.setOnClickListener {
            if (PermissionChecker.checkSelfPermission(this, readMediaPermission) != PermissionChecker.PERMISSION_GRANTED) {
                requestReadPicturePermissionLauncher.launch(readMediaPermission)
            } else {
                showChoosePhotoActivity()
            }
        }

        binding.buttonTakePicture.setOnClickListener {
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_DENIED) {
                requestTakePicturePermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                showTakePhotoActivity()
            }
        }
    }

    private fun showChoosePhotoActivity() {
        getContent.launch("image/jpeg")
    }

    private fun showTakePhotoActivity() {
        try {
            val imageFile = createImageFile()
            mCurrentPhotoFile = imageFile
            val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", imageFile)
            resultLauncher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.user_photo_error_no_camera, Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, R.string.user_photo_error_photo_path, Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "ENTOURAGE_$timeStamp.jpg"
        val storageDir = File(filesDir, "images")
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw IOException("Failed to create image directory")
        }
        return File(storageDir, imageFileName)
    }

    private fun showNextStep(photoUri: Uri) {
        val fragment = OnboardingEditPhotoFragment.newInstance(photoUri, photoSource)
        fragment.setCallback(this)
        binding.frameLayout.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment, OnboardingEditPhotoFragment.TAG)
            .commit()
    }

    private fun updateUserView() {
        val user = EntourageApplication.me(this)
        user?.avatarURL?.let { avatarURL ->
            Glide.with(this)
                .load(Uri.parse(avatarURL))
                .placeholder(R.drawable.ic_user_photo_small)
                .circleCrop()
                .into(binding.imageProfile)
        } ?: run {
            binding.imageProfile.setImageResource(R.drawable.ic_user_photo_small)
        }
    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.header.iconBack.postDelayed({
                binding.progressBar.visibility = View.GONE
                isFromSmallTalk = false
                finish()
            }, 2000)
        }
    }

    private fun loadPickedImage(uri: Uri?) {
        uri?.let { showNextStep(it) }
        pickedImageUri = null
    }

    override fun onPhotoEdited(photoURI: Uri?, photoSource: Int) {
        pickedImageEditedUri = photoURI

        // Affichage dans l'imageView avec Glide
        Glide.with(this)
            .load(photoURI)
            .placeholder(R.drawable.ic_user_photo)
            .circleCrop()
            .into(binding.imageProfile)

        // Copier le fichier à partir du URI dans un fichier temporaire pour l’upload
        photoURI?.let { uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("upload_temp_", ".jpg", cacheDir)
                tempFile.outputStream().use { output ->
                    inputStream?.copyTo(output)
                }

                avatarUploadPresenter.uploadPhoto(tempFile)

            } catch (e: Exception) {
                Timber.e(e, "Erreur lors de la conversion Uri -> File pour l'upload")
                Toast.makeText(this, "Erreur pendant l'upload de la photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onUploadError() {
        Toast.makeText(this, "Erreur lors du chargement de la photo", Toast.LENGTH_LONG).show()
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1
        const val TAKE_PHOTO_REQUEST = 2
        var isFromSmallTalk = false
    }
}
