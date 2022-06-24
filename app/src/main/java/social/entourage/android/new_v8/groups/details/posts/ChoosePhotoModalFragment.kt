package social.entourage.android.new_v8.groups.details.posts

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
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentChoosePhotoModalBinding
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.tools.Utils
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ChoosePhotoModalFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentChoosePhotoModalBinding? = null
    val binding: NewFragmentChoosePhotoModalBinding get() = _binding!!

    var photoFileUri: Uri? = null
    var mCurrentPhotoPath: String? = null
    private var photoFile: File? = null


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
        handleBackButton()
        handleValidateButton()
    }

    private fun handleImportPictureButton() {
        binding.importPicture.root.setOnClickListener {
            pickPhoto()
        }
    }

    private fun handleTakePictureButton() {
        binding.takePicture.root.setOnClickListener {
            takePhoto()
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

    private fun saveBitmap(bitmap: Bitmap) {
        photoFile = Utils.saveBitmapToFile(bitmap, photoFile)
    }


    @Throws(IOException::class)
    fun createImageFile(): Uri? {
        // Create an image file name
        val timeStamp =
            SimpleDateFormat(Const.DATE_FORMAT_FILE_NAME, Locale.FRANCE).format(Date())
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
            binding.cropView.crop()
            Timber.e(photoFileUri.toString())
            setFragmentResult(
                Const.REQUEST_KEY_CHOOSE_PHOTO,
                bundleOf(
                    Const.CHOOSE_PHOTO to photoFileUri.toString()
                )
            )
            dismiss()
        }
    }

    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            dismiss()
        }
    }

    private
    val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                showTakePhotoActivity()
            }
        }

    private
    val permReqChoosePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                showChoosePhotoActivity()
            }
        }

    private fun hasPermissions(
        context: Context,
        permissions: Array<String>
    ): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }

    private fun takePhoto() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            showTakePhotoActivity()
        }
        activity?.let {
            if (hasPermissions(
                    activity as Context,
                    PERMISSIONS
                )
            ) {
                showTakePhotoActivity()
            } else {
                permReqLauncher.launch(
                    PERMISSIONS
                )
            }
        }
    }

    private fun pickPhoto() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            showChoosePhotoActivity()
        }
        activity?.let {
            if (hasPermissions(
                    activity as Context,
                    PERMISSIONS
                )
            ) {
                showChoosePhotoActivity()
            } else {
                permReqChoosePhotoLauncher.launch(
                    PERMISSIONS
                )
            }
        }
    }

    companion object {
        const val TAG = "ChoosePhotoModalFragment"
        var PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        fun newInstance(): ChoosePhotoModalFragment {
            return ChoosePhotoModalFragment()
        }
    }
}