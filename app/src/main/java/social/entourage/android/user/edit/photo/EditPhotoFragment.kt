package social.entourage.android.user.edit.photo

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.databinding.FragmentOnboardingPhotoBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.onboarding.onboard.OnboardingEditPhotoFragment
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

open class EditPhotoFragment : BaseDialogFragment(), PhotoEditInterface {
    private var _binding: FragmentOnboardingPhotoBinding? = null
    protected val binding: FragmentOnboardingPhotoBinding
        get() = _binding ?: throw IllegalStateException("Trying to access the binding outside of the view lifecycle.")
    private val readMediaPermission: String = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_EXTERNAL_STORAGE else Manifest.permission.READ_MEDIA_IMAGES
    private var pickedImageUri: Uri? = null
    protected var pickedImageEditedUri: Uri? = null
    private var mCurrentPhotoPath: String? = null
    private var photoSource = 0

    protected var analyticsEventView: String? = null
    protected var analyticsEventActionGallery: String? = null
    protected var analyticsEventActionPhoto: String? = null

    // Create the File where the photo should go
    var photoFileUri: Uri? = null

    val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { urii ->
                photoSource = PICK_IMAGE_REQUEST
                if (PermissionChecker.checkSelfPermission(
                        requireActivity(),
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
            if(isGranted) {
                showTakePhotoActivity()
            } else {
                Toast.makeText(
                    activity,
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
                    activity,
                    R.string.user_photo_error_read_permission,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        analyticsEventView?.let { AnalyticsEvents.logEvent(it)}

    }

    override fun onResume() {
        super.onResume()
        Timber.d("On resume Fragment ? $pickedImageUri")
        // Check if we are returning from photo picker
        // i.e. the pickedImageUri is set
        if (pickedImageUri != null) {
            // Check if we have reading rights
            if (PermissionChecker.checkSelfPermission(
                    requireActivity(),
                    readMediaPermission
                ) != PermissionChecker.PERMISSION_GRANTED
            ) {
                requestReadPicturePermissionLauncher.launch(readMediaPermission)
            } else {
                // Proceed to next step
                loadPickedImage(pickedImageUri)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the photo path
        outState.putString(KEY_PHOTO_PATH, mCurrentPhotoPath)
    }

    //**********//**********//**********
    // methods
    //**********//**********//**********

    open fun setupViews() {

        binding.buttonGallery?.setOnClickListener {
            // write permission is used to store the cropped image before upload
            if (PermissionChecker.checkSelfPermission(
                    requireActivity(),
                    readMediaPermission
                ) != PermissionChecker.PERMISSION_GRANTED
            ) {
                requestReadPicturePermissionLauncher.launch(readMediaPermission)
            } else {
                showChoosePhotoActivity()
            }
        }

        binding.buttonTakePicture?.setOnClickListener {
            if (PermissionChecker.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PermissionChecker.PERMISSION_DENIED
            ) {
                requestTakePicturePermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                showTakePhotoActivity()
            }
        }
    }

    open fun showChoosePhotoActivity() {
        analyticsEventActionGallery?.let {AnalyticsEvents.logEvent(it)}
        getContent.launch("image/jpeg")
    }

    open fun showTakePhotoActivity() {
        analyticsEventActionPhoto?.let{AnalyticsEvents.logEvent(it)}

        // Ensure that there's a camera activity to handle the intent
        try {
            try {
                photoFileUri = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Toast.makeText(activity, R.string.user_photo_error_photo_path, Toast.LENGTH_SHORT)
                    .show()
            }
            // Continue only if the File was successfully created
            if (photoFileUri != null) {
                resultLauncher.launch(photoFileUri)
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.user_photo_error_no_camera, Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    protected fun createImageFile(): Uri? {
        // Create an image file name
        var locale = LanguageManager.getLocaleFromPreferences(requireContext())
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", locale).format(Date())
        val imageFileName = "ENTOURAGE_" + timeStamp + "_"
        val storageDir = File(requireContext().filesDir, "images")
        if (!storageDir.exists()) {
            if (!storageDir.mkdir()) {
                throw IOException()
            }
        }
        val image = File(storageDir, "$imageFileName.jpg")
        mCurrentPhotoPath = image.absolutePath
        return FileProvider.getUriForFile(
            requireContext(),
            requireContext().applicationContext.packageName + ".fileprovider",
            image
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Nettoie le binding lorsque la vue est détruite
    }

    private fun showNextStep(photoUri: Uri?) {
        if (photoUri == null) {
            Toast.makeText(activity, R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show()
            return
        }
        val fragment = OnboardingEditPhotoFragment.newInstance(photoUri, photoSource)
        fragment.setCallback(this)

        fragment.show(parentFragmentManager, OnboardingEditPhotoFragment.TAG)
    }

    //**********//**********//**********
    // PhotoEditInterface
    //**********//**********//**********

    override fun onPhotoEdited(photoURI: Uri?, photoSource: Int) {
        pickedImageEditedUri = photoURI

        binding.imageProfile.let {
            if (pickedImageEditedUri != null) {
                Glide.with(this)
                    .load(pickedImageEditedUri)
                    .placeholder(R.drawable.ic_user_photo)
                    .circleCrop()
                    .into(it)
            } else {
                Glide.with(this)
                    .load(R.drawable.ic_user_photo)
                    .circleCrop()
                    .into(it)
            }
        }
    }

    //**********//**********//**********
    // Private methods
    //**********//**********//**********

    private fun loadPickedImage(uri: Uri?) {
        showNextStep(uri)
        pickedImageUri = null
    }


    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val PICK_IMAGE_REQUEST = 1
        const val TAKE_PHOTO_REQUEST = 2

        const val KEY_PHOTO_PATH = "social.entourage.android.photo_path"
    }
}