package social.entourage.android.new_v8.groups.details.posts

import android.Manifest
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentChoosePhotoModalBinding
import social.entourage.android.onboarding.OnboardingPhotoFragment
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


    val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri.let { urii ->
                Timber.d("Return Image REQUEST uri ? $urii")
                if (PermissionChecker.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PermissionChecker.PERMISSION_GRANTED
                ) {
                    Timber.d("Return Image REQUEST ici")
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        OnboardingPhotoFragment.PICK_AND_CROP_IMAGE_PERMISSION_CODE
                    )
                } else {
                    Timber.d("Return Image REQUEST LA")
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
            if (photoFileUri != null) {
                loadPickedImage(photoFileUri)
                return@registerForActivityResult
            }
            mCurrentPhotoPath?.let { loadPickedImage(Uri.fromFile(File(it))) }
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
    }

    private fun handleTakePictureButton() {
        binding.importPicture.root.setOnClickListener {
            // write permission is used to store the cropped image before upload
            if (PermissionChecker.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PermissionChecker.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    OnboardingPhotoFragment.PICK_AND_CROP_IMAGE_PERMISSION_CODE
                )
            } else {
                showChoosePhotoActivity()
            }
        }
    }

    private fun handleImportPictureButton() {
        binding.takePicture.root.setOnClickListener {
            if (PermissionChecker.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PermissionChecker.PERMISSION_DENIED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    OnboardingPhotoFragment.CAMERA_PERMISSION_CODE
                )
            } else {
                if (PermissionChecker.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PermissionChecker.PERMISSION_DENIED
                ) {
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        OnboardingPhotoFragment.WRITE_STORAGE_PERMISSION_CODE
                    )
                } else {
                    showTakePhotoActivity()
                }
            }
        }
    }

    private fun showTakePhotoActivity() {
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
    fun createImageFile(): Uri? {
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

    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            dismiss()
        }
    }


    companion object {
        const val TAG = "ChoosePhotoModalFragment"
        fun newInstance(): ChoosePhotoModalFragment {
            return ChoosePhotoModalFragment()
        }
    }
}