package social.entourage.android.user.edit.photo

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_onboarding_photo.*
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.user.edit.photo.OnboardingEditPhotoFragment
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_FIRSTNAME = "firstname"

open class OnboardingPhotoFragment : BaseDialogFragment(), PhotoEditDelegate {

    private val readMediaPermission: String = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_EXTERNAL_STORAGE else Manifest.permission.READ_MEDIA_IMAGES
    private var pickedImageUri: Uri? = null
    private var pickedImageEditedUri: Uri? = null
    private var mCurrentPhotoPath: String? = null
    private var photoSource = 0

    private var firstname: String? = null

    private var callback: OnboardingCallback? = null

    protected var isFromProfile = false

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            firstname = it.getString(ARG_FIRSTNAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback?.updateButtonNext(false)

        if (isFromProfile) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_PROFILE_CHOOSE_PHOTO)
        } else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_ONBOARDING_CHOOSE_PHOTO)
        }

        setupViews()

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    //**********//**********//**********
    // methods
    //**********//**********//**********

    open fun setupViews() {

        import_picture?.setOnClickListener {
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

        take_picture?.setOnClickListener {
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
        if (isFromProfile) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_UPLOAD_PHOTO)
        } else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_UPLOAD_PHOTO)
        }
        getContent.launch("image/*")
    }

    open fun showTakePhotoActivity() {
        if (isFromProfile) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_TAKE_PHOTO)
        } else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_TAKE_PHOTO)
        }

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
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(Date())
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
    // PhotoEditDelegate
    //**********//**********//**********

    override fun onPhotoEdited(photoURI: Uri?, photoSource: Int) {
        pickedImageEditedUri = photoURI

        image_profile?.let {
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

        callback?.updateUserPhoto(pickedImageEditedUri)
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
        const val TAG = "social.entourage.android.onboarding.OnboardingPhotoFragment"
        const val PICK_IMAGE_REQUEST = 1
        const val TAKE_PHOTO_REQUEST = 2

        const val KEY_PHOTO_PATH = "social.entourage.android.photo_path"

        fun newInstance(firstName: String) =
            OnboardingPhotoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FIRSTNAME, firstName)
                }
            }
    }
}

//**********//**********//**********
// Interface
//**********//**********//**********
interface PhotoEditDelegate {
    fun onPhotoEdited(photoURI: Uri?, photoSource: Int)
}

interface OnboardingCallback {
    val errorMessage: MutableLiveData<String>
    fun updateUserPhoto(imageUri: Uri?)
    fun updateButtonNext(isValid: Boolean)
}