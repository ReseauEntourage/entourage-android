package social.entourage.android.user.edit.photo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import com.squareup.otto.Subscribe
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_photo_choose_source.*
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.tape.Events.OnPhotoChosen
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.tools.BusProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Deprecated(message="Migrate to class ChoosePhotoFragment", replaceWith = ReplaceWith("ChoosePhotoFragment", "social.entourage.android.user.edit.photo.ChoosePhotoFragment"))
class PhotoChooseSourceFragmentCompat : EntourageDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var mListener: PhotoChooseInterface? = null
    private var mCurrentPhotoPath: String? = null
    private var pickedImageUri: Uri? = null
    private var photoSource = 0
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            // Restore the photo path
            mCurrentPhotoPath = savedInstanceState.getString(KEY_PHOTO_PATH)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_09_6)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_choose_source, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photo_choose_back_button.setOnClickListener { onBackClicked() }
        photo_choose_ignore_button.setOnClickListener {onIgnoreClicked()}
        photo_choose_photo_button.setOnClickListener { onChoosePhotoClicked()}
        photo_choose_take_photo_button.setOnClickListener {onTakePhotoClicked()}

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PhotoChooseInterface) {
            mListener = context
        }
        BusProvider.instance.register(this)
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
        BusProvider.instance.unregister(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the photo path
        outState.putString(KEY_PHOTO_PATH, mCurrentPhotoPath)
    }

    override fun onResume() {
        super.onResume()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if(resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST-> {
                    intent?.data?.let { uri ->
                        photoSource = requestCode
                        if (PermissionChecker.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                            pickedImageUri = uri
                            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PICK_AND_CROP_IMAGE_PERMISSION_CODE)
                        } else {
                            loadPickedImage(uri)
                        }
                    }
                }
                TAKE_PHOTO_REQUEST-> {
                    photoSource = requestCode
                    intent?.data?.let {
                        showNextStep(it)
                    } ?: run {
                        mCurrentPhotoPath?.let { photoPath -> showNextStep(Uri.fromFile(File(photoPath))) }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                    if (PermissionChecker.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_STORAGE_PERMISSION_CODE)
                    } else {
                        showTakePhotoActivity()
                    }
                } else {
                    Toast.makeText(activity, R.string.user_photo_error_camera_permission, Toast.LENGTH_LONG).show()
                }
            }
            PICK_AND_CROP_IMAGE_PERMISSION_CODE-> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (pickedImageUri != null) {
                        loadPickedImage(pickedImageUri)
                    } else {
                        showChoosePhotoActivity()
                    }
                } else {
                    Toast.makeText(activity, R.string.user_photo_error_read_permission, Toast.LENGTH_LONG).show()
                }
            }
            WRITE_STORAGE_PERMISSION_CODE-> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showTakePhotoActivity()
                } else {
                    Toast.makeText(activity, R.string.user_photo_error_read_permission, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ----------------------------------
    // Button handling
    // ----------------------------------
    fun onBackClicked() {
        mListener?.onPhotoBack()
        dismiss()
    }

    fun onIgnoreClicked() {
        mListener?.onPhotoIgnore()
        dismiss()
    }

    fun onChoosePhotoClicked() {
        activity?.let {
            // write permission is used to store the cropped image before upload
            if (PermissionChecker.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PICK_AND_CROP_IMAGE_PERMISSION_CODE)
            } else {
                showChoosePhotoActivity()
            }
        }
    }

    fun onTakePhotoClicked() {
        activity?.let {
            if (CropImage.isExplicitCameraPermissionRequired(it)) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE)
            } else if (PermissionChecker.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_STORAGE_PERMISSION_CODE)
            } else {
                showTakePhotoActivity()
            }
        }
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------
    private fun loadPickedImage(uri: Uri?) {
        showNextStep(uri)
        pickedImageUri = null
    }

    private fun showChoosePhotoActivity() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_PHOTO_UPLOAD_SUBMIT)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            // Start a separate activity, to handle the issue with onActivityResult
            val intent = Intent(context, ChoosePhotoCompatActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } else {
            val intent = Intent()
            // Show only images, no videos or anything else
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(intent, null), PICK_IMAGE_REQUEST)
        }
    }

    private fun showTakePhotoActivity() {
        activity?.let {
            EntourageEvents.logEvent(EntourageEvents.EVENT_PHOTO_TAKE_SUBMIT)
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(it.packageManager) == null) {
                Toast.makeText(activity, R.string.user_photo_error_no_camera, Toast.LENGTH_SHORT).show()
            } else {
                // Create the File where the photo should go
                try {
                    val photoFileUri = createImageFile()
                    // Continue only if the File was successfully created
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                        // Start a separate activity, to handle the issue with onActivityResult
                        val intent = Intent(context, TakePhotoActivity::class.java)
                        intent.data = photoFileUri
                        if (mCurrentPhotoPath != null) {
                            intent.putExtra(TakePhotoActivity.KEY_PHOTO_PATH, mCurrentPhotoPath)
                        }
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    } else {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri)
                        takePictureIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
                    }
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Toast.makeText(activity, R.string.user_photo_error_photo_path, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): Uri {
        context?.let {
            // Create an image file name
            val storageDir = File(it.filesDir, "images")
            if (!storageDir.exists() && !storageDir.mkdir()) {
                // Failed to create the folder
                throw IOException()
            }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_", Locale.FRANCE).format(Date())
            val image = File(storageDir, "ENTOURAGE_$timeStamp.jpg")
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.absolutePath
            // Return the URI
            return FileProvider.getUriForFile(it, it.applicationContext.packageName + ".fileprovider", image)
        } ?: run { throw IOException() }
    }

    private fun showNextStep(photoUri: Uri?) {
        photoUri?.let {
            PhotoEditFragment.newInstance(it, photoSource).show(parentFragmentManager, PhotoEditFragment.TAG)
        } ?: run {
            Toast.makeText(activity, R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show()
        }
    }

    // ----------------------------------
    // Bus Listeners
    // ----------------------------------
    @Subscribe
    fun onPhotoChosen(event: OnPhotoChosen) {
        pickedImageUri = event.photoUri
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.photo_choose_source"
        const val PICK_IMAGE_REQUEST = 1
        const val TAKE_PHOTO_REQUEST = 2
        private const val PICK_AND_CROP_IMAGE_PERMISSION_CODE = 3
        private const val WRITE_STORAGE_PERMISSION_CODE = 4
        private const val KEY_PHOTO_PATH = "social.entourage.android.photo_path"
    }
}