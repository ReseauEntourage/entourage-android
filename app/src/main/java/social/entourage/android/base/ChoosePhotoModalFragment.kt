package social.entourage.android.base

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.takusemba.cropme.OnCropListener
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentChoosePhotoModalBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class ChoosePhotoModalFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentChoosePhotoModalBinding? = null
    val binding: NewFragmentChoosePhotoModalBinding get() = _binding!!

    var photoFileUri: Uri? = null
    var pickingPhoto:Boolean = false
    var takingPhoto:Boolean = false
    private var mCurrentPhotoPath: String? = null

    val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri.let { urii ->
                photoFileUri = urii
                loadPickedImage(urii)
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
            if (photoFileUri != null) {
                loadPickedImage(photoFileUri)
                return@registerForActivityResult
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentChoosePhotoModalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleDeleteImage()
        handleTakePictureButton()
        handleImportPictureButton()
        handleCloseButton()
        handleValidateButton()
        setStyle()
        setCropView()
    }

    fun setStyle(){
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        setCancelable(false)
    }

    fun setCropView(){
        binding.cropView.visibility = View.INVISIBLE
        binding.cropView.addOnCropListener(object : OnCropListener {
            override fun onSuccess(bitmap: Bitmap) {
                try {
                    saveBitmap(bitmap)
                    setFragmentResult(
                        Const.REQUEST_KEY_CHOOSE_PHOTO,
                        bundleOf(
                            Const.CHOOSE_PHOTO to photoFileUri
                        )
                    )
                    dismiss()
                } catch (e: IOException) {
                    Toast.makeText(activity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(e: Exception) {
                try {
                    Toast.makeText(activity, R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show()
                } catch (e2: IOException) {
                    Timber.w(e2)
                }
            }
        })
    }

    private fun saveBitmap(bitmap: Bitmap) {
        try {
            binding.cropView.setBitmap(bitmap)
            var saveUri = photoFileUri
            if (pickingPhoto) {
                saveUri = createImageFile()  // Creates new file if picking from gallery
            }
            saveUri?.let {
                Utils.saveBitmapToFileWithUrl(bitmap, it, requireContext())
            }
        } catch (e: Exception) {
            Timber.w(e)
        }
    }


    private fun handleImportPictureButton() {
        binding.importPicture.root.setOnClickListener {
            pickPhoto()
            this.pickingPhoto = true
            this.takingPhoto = false
        }
    }

    private fun handleTakePictureButton() {
        binding.takePicture.root.setOnClickListener {
            takePhoto()
            this.takingPhoto = true
            this.pickingPhoto = false
        }
    }

    private fun showTakePhotoActivity() {
        try {
            try {
                photoFileUri = createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(
                    activity,
                    R.string.user_photo_error_photo_path,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            if (photoFileUri != null) {
                resultLauncher.launch(photoFileUri)
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.user_photo_error_no_camera, Toast.LENGTH_SHORT)
                .show()
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): Uri? {
        // Create an image file name
        var locale = LanguageManager.getLocaleFromPreferences(requireContext())
        val timeStamp =
            SimpleDateFormat(Const.DATE_FORMAT_FILE_NAME, locale).format(Date())
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

    private fun showChoosePhotoActivity() {
        getContent.launch("image/*")
    }

    private fun loadPickedImage(uri: Uri?) {
        uri?.let {
            binding.cropView.visibility = View.VISIBLE
            binding.image.visibility = View.GONE
            binding.addPhoto.visibility = View.GONE
            binding.deletePhotoLayout.visibility = View.VISIBLE
            binding.cropView.setUri(it)
            photoFileUri = it
        }
    }

    private fun handleDeleteImage() {
        binding.deletePicture.root.setOnClickListener {
            binding.cropView.visibility = View.GONE
            binding.image.visibility = View.VISIBLE
            binding.addPhoto.visibility = View.VISIBLE
            binding.deletePhotoLayout.visibility = View.GONE
        }
    }

    private fun handleValidateButton() {
        binding.validatePicture.root.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_FEED_NEW_POST_VALIDATE_PIC)
            try {
                if(binding.cropView.isOffFrame()){
                    Toast.makeText(activity, R.string.user_photo_error_zoom_in, Toast.LENGTH_SHORT)
                        .show()
                }else{
                    binding.cropView.crop()
                }
            } catch(e: Exception) {
                Timber.e(e)
            }
        }
    }


    private fun handleCloseButton() {
        binding.header.hbsIconCross.setOnClickListener {
            dismiss()
        }
    }

    private val permCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                showTakePhotoActivity()
            } else {
                Toast.makeText(
                    activity,
                    R.string.user_photo_error_camera_permission,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val permReqChoosePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                showChoosePhotoActivity()
            } else {
                Toast.makeText(
                    activity,
                    R.string.user_photo_error_camera_permission,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun hasPermission(
        context: Context,
        permission: String
    ): Boolean =
        ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun takePhoto() {
        activity?.let {
            if (hasPermission(
                    activity as Context,
                    Manifest.permission.CAMERA
                )
            ) {
                showTakePhotoActivity()
            } else {
                permCameraLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun pickPhoto() {
        activity?.let {
            if (hasPermission(
                    activity as Context,
                    readMediaPermission
                )
            ) {
                showChoosePhotoActivity()
            } else {
                permReqChoosePhotoLauncher.launch(readMediaPermission)
            }
        }
    }


    companion object {
        const val TAG = "ChooseGalleryPhotoModalFragment"
        val readMediaPermission =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_EXTERNAL_STORAGE
            else
                Manifest.permission.READ_MEDIA_IMAGES

        fun newInstance(): ChoosePhotoModalFragment {
            return ChoosePhotoModalFragment()
        }
    }
}