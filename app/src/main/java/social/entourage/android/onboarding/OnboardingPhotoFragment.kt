package social.entourage.android.onboarding

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_onboarding_photo.*
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_FIRSTNAME = "firstname"

open class OnboardingPhotoFragment : BaseDialogFragment(), PhotoEditDelegate, ActivityCompat.OnRequestPermissionsResultCallback {

    protected var pickedImageUri: Uri? = null
    protected var pickedImageEditedUri: Uri? = null
    protected var mCurrentPhotoPath: String? = null
    protected var photoSource = 0

    private var firstname: String? = null

    private var callback:OnboardingCallback? = null

    protected var isFromProfile = false

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            firstname = it.getString(ARG_FIRSTNAME)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback?.updateButtonNext(false)
        setupViews()

        if (isFromProfile) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_PROFILE_CHOOSE_PHOTO)
        }
        else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_ONBOARDING_CHOOSE_PHOTO)
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("On resume Fragment ? $pickedImageUri")
        // Check if we are returning from photo picker
        // i.e. the pickedImageUri is set
        if (pickedImageUri != null) {
            // Check if we have reading rights
            if (PermissionChecker.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PICK_AND_CROP_IMAGE_PERMISSION_CODE)
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
        user_edit_title_layout?.visibility = View.GONE
        ui_onboard_photo_tv_title?.text = String.format(getString(R.string.onboard_photo_title),firstname)
        ui_onboard_photo_tv_description?.text = getString(R.string.onboard_photo_description)

        ui_bt_pick?.setOnClickListener {
            // write permission is used to store the cropped image before upload
            if (PermissionChecker.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PICK_AND_CROP_IMAGE_PERMISSION_CODE)
            } else {
                showChoosePhotoActivity()
            }
        }

        ui_bt_take?.setOnClickListener {
            if (PermissionChecker.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_DENIED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            } else {
                if (PermissionChecker.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_DENIED) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_STORAGE_PERMISSION_CODE)
                } else {
                    showTakePhotoActivity()
                }
            }
        }
    }

    open fun showChoosePhotoActivity() {
        if (isFromProfile) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_UPLOAD_PHOTO)
        }
        else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_UPLOAD_PHOTO)
        }
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, null), PICK_IMAGE_REQUEST)
    }

    open fun showTakePhotoActivity() {
        if (isFromProfile) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_TAKE_PHOTO)
        }
        else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_TAKE_PHOTO)
        }

        // Ensure that there's a camera activity to handle the intent
        try {
            // Create the File where the photo should go
            var photoFileUri: Uri? = null
            try {
                photoFileUri = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Toast.makeText(activity, R.string.user_photo_error_photo_path, Toast.LENGTH_SHORT).show()
            }
            // Continue only if the File was successfully created
            if (photoFileUri != null) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri)
                takePictureIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
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
        return FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName + ".fileprovider", image)
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
    // Returns methods
    //**********//**********//**********

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        Timber.d("Return Act return $requestCode - result $resultCode - intent ? ${intent?.data}")
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && intent != null && intent.data != null) {
            photoSource = requestCode
            val uri = intent.data
            Timber.d("Return Image REQUEST uri ? $uri")
            if (PermissionChecker.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                pickedImageUri = uri
                Timber.d("Return Image REQUEST ici")
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PICK_AND_CROP_IMAGE_PERMISSION_CODE)
            } else {
                Timber.d("Return Image REQUEST LA")
                loadPickedImage(uri)
            }
            return
        }

        if (requestCode == TAKE_PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {
            photoSource = requestCode
            if (intent != null && intent.data != null) {
                showNextStep(intent.data)
                return
            }
            mCurrentPhotoPath?.let { showNextStep(Uri.fromFile(File(it))) }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                if (PermissionChecker.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_DENIED) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_STORAGE_PERMISSION_CODE)
                } else {
                    showTakePhotoActivity()
                }
            } else {
                Toast.makeText(activity, R.string.user_photo_error_camera_permission, Toast.LENGTH_LONG).show()
            }
            return
        }
        if (requestCode == PICK_AND_CROP_IMAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                if (pickedImageUri != null) {
                    loadPickedImage(pickedImageUri)
                } else {
                    showChoosePhotoActivity()
                }
            } else {
                Toast.makeText(activity, R.string.user_photo_error_read_permission, Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == WRITE_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                showTakePhotoActivity()
            } else {
                Toast.makeText(activity, R.string.user_photo_error_read_permission, Toast.LENGTH_LONG).show()
            }
        }
    }

    //**********//**********//**********
    // PhotoEditDelegate
    //**********//**********//**********

    override fun onPhotoEdited(photoURI: Uri?, photoSource: Int) {
        pickedImageEditedUri = photoURI

        ui_onboard_photo_image?.let {
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
        const val PICK_AND_CROP_IMAGE_PERMISSION_CODE = 3
        const val WRITE_STORAGE_PERMISSION_CODE = 4
        const val CAMERA_PERMISSION_CODE = 5

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