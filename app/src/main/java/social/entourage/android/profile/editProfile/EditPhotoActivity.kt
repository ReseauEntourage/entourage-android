package social.entourage.android.profile.editProfile

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
import social.entourage.android.user.AvatarUpdatePresenter
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
    private var mCurrentPhotoPath: String? = null
    private var photoSource = 0
    private lateinit var avatarUploadPresenter: AvatarUploadPresenter

    private val readMediaPermission: String = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_EXTERNAL_STORAGE else Manifest.permission.READ_MEDIA_IMAGES
    var photoFileUri: Uri? = null
    val profilePresenter: ProfilePresenter by lazy { ProfilePresenter() }

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { urii ->
            photoSource = PICK_IMAGE_REQUEST
            if (PermissionChecker.checkSelfPermission(
                    this,
                    readMediaPermission
                ) != PermissionChecker.PERMISSION_GRANTED
            ) {
                pickedImageUri = urii
                requestReadPicturePermissionLauncher.launch(readMediaPermission)
            } else {
                loadPickedImage(urii)
            }
            return@let
        }
    }

    private val resultLauncher = registerForActivityResult(
        object : ActivityResultContracts.TakePicture() {
            override fun createIntent(
                context: Context,
                input: Uri
            ): Intent {
                val takePictureIntent = super.createIntent(context, input)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri)
                takePictureIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                return takePictureIntent
            }
        }) { success ->
        if (success) {
            photoSource = TAKE_PHOTO_REQUEST
            if (photoFileUri != null) {
                showNextStep(photoFileUri)
                return@registerForActivityResult
            }
            mCurrentPhotoPath?.let { showNextStep(Uri.fromFile(File(it))) }
        }
    }

    private val requestTakePicturePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                showTakePhotoActivity()
            } else {
                Toast.makeText(
                    this,
                    R.string.user_photo_error_camera_permission,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val requestReadPicturePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                if (pickedImageUri != null) {
                    loadPickedImage(pickedImageUri)
                } else {
                    showChoosePhotoActivity()
                }
            } else {
                Toast.makeText(
                    this,
                    R.string.user_photo_error_read_permission,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPhotoBinding.inflate(layoutInflater)
        avatarUploadPresenter = AvatarUploadPresenter(
            this, // Puisque EditPhotoActivity implémente AvatarUploadView
            AvatarUploadRepository(),
            profilePresenter
        )
        setContentView(binding.root)
        setupViews()
        updateUserView()
        setBackButton()
    }

    private fun setupViews() {
        if(!isFromSmallTalk){
            binding.progressViewBar.visibility = View.GONE
            binding.buttonSmalltalkContinu.visibility = View.GONE
            binding.buttonSmalltalkPrevious.visibility = View.GONE
        }else{
            AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__SMALLTALK__PHOTO)
            binding.header.layout.visibility = View.GONE
        }
        binding.buttonSmalltalkPrevious.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.CLIC__SMALLTALK__PHOTO_PREVIOUS)
            finish()
        }
        binding.buttonSmalltalkContinu.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.CLIC__SMALLTALK__PHOTO_FINISH)
            finish()
        }
        binding.buttonGallery.setOnClickListener {
            if (PermissionChecker.checkSelfPermission(
                    this,
                    readMediaPermission
                ) != PermissionChecker.PERMISSION_GRANTED
            ) {
                requestReadPicturePermissionLauncher.launch(readMediaPermission)
            } else {
                showChoosePhotoActivity()
            }
        }

        binding.buttonTakePicture.setOnClickListener {
            if (PermissionChecker.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PermissionChecker.PERMISSION_DENIED
            ) {
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
            photoFileUri = createImageFile()
            if (photoFileUri != null) {
                resultLauncher.launch(photoFileUri)
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.user_photo_error_no_camera, Toast.LENGTH_SHORT).show()
        } catch (ex: IOException) {
            Toast.makeText(this, R.string.user_photo_error_photo_path, Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    protected fun createImageFile(): Uri? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "ENTOURAGE_$timeStamp"
        val storageDir = File(this.filesDir, "images")
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw IOException("Failed to create directory for image file.")
        }
        val image = File(storageDir, "$imageFileName.jpg")
        mCurrentPhotoPath = image.absolutePath

        // Utilise le nom de package de l'application pour construire l'authority
        return FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", image)
    }


    private fun showNextStep(photoUri: Uri?) {
        if (photoUri == null) {
            Toast.makeText(this, R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show()
            return
        }
        val fragment = OnboardingEditPhotoFragment.newInstance(photoUri, photoSource)
        fragment.setCallback(this)
        binding.frameLayout.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment, OnboardingEditPhotoFragment.TAG)
            .commit()
    }

    private fun updateUserView() {
        val user = EntourageApplication.me(this) // Assume this method is adjusted to accept Activity context
        user?.avatarURL?.let { avatarURL ->
            Glide.with(this@EditPhotoActivity)
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

            // Crée un Handler pour exécuter le finish() après 2 secondes
            val delayMillis = 2000L
            binding.header.iconBack.postDelayed({
                binding.progressBar.visibility = View.GONE
                isFromSmallTalk = false
                finish()
            }, delayMillis)
        }
    }



    private fun loadPickedImage(uri: Uri?) {
        showNextStep(uri)
        pickedImageUri = null
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1
        const val TAKE_PHOTO_REQUEST = 2
        var isFromSmallTalk = false
    }

    override fun onPhotoEdited(photoURI: Uri?, photoSource: Int) {
        pickedImageEditedUri = photoURI
        photoURI?.path?.let { path ->
            Glide.with(this)
                .load(path)
                .placeholder(R.drawable.placeholder_user)
                .circleCrop()
                .into(binding.imageProfile)
            //Upload the photo to Amazon S3
            avatarUploadPresenter.uploadPhoto(File(path))
        }

        binding.imageProfile.let {
            if (pickedImageEditedUri != null) {
                Glide.with(this@EditPhotoActivity)
                    .load(pickedImageEditedUri)
                    .placeholder(R.drawable.ic_user_photo)
                    .circleCrop()
                    .into(it)
            } else {
                Glide.with(this@EditPhotoActivity)
                    .load(R.drawable.ic_user_photo)
                    .circleCrop()
                    .into(it)
            }
        }
    }

    override fun onUploadError() {
        // Affiche un message d'erreur à l'utilisateur
        Toast.makeText(this, "Erreur lors du chargement de la photo", Toast.LENGTH_LONG).show()
    }
}
