package social.entourage.android.onboarding

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.theartofdev.edmodo.cropper.CropImageView
import com.theartofdev.edmodo.cropper.CropImageView.OnSetImageUriCompleteListener
import kotlinx.android.synthetic.main.fragment_onboarding_edit_photo.*
import social.entourage.android.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private val PHOTO_PARAM = "social.entourage.android.photo_param"
private val PHOTO_SOURCE = "social.entourage.android.photo_source"


class OnboardingEditPhotoFragment : DialogFragment(), OnSetImageUriCompleteListener {
    private val ROTATE_DEGREES_STEP = -90
    private var mListener: PhotoEditDelegate? = null
    private var photoUri: Uri? = null
    private var photoSource = 0

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            photoUri = it.getParcelable(PHOTO_PARAM)
            photoSource = it.getInt(PHOTO_SOURCE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_edit_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (dialog == null) {
            showsDialog = false
        }

        super.onActivityCreated(savedInstanceState)
        val dialog = dialog
        if (dialog != null) {
            val window = dialog.window
            if (window != null && window.attributes != null) {
                window.attributes.windowAnimations = R.style.CustomDialogFragmentFromRight
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val window = dialog.window
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                if (window.attributes != null) {
                    window.attributes.windowAnimations = R.style.CustomDialogFragmentFromRight
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    private fun setupViews() {
        if (context != null) {
            ui_photo_edit_progressBar?.indeterminateDrawable?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white), PorterDuff.Mode.SRC_ATOP)
        }

        ui_photo_edit_cropImageView?.setOnSetImageUriCompleteListener(this)
        if (photoUri != null) {
            ui_photo_edit_progressBar?.visibility = View.VISIBLE
            ui_photo_edit_cropImageView?.setImageUriAsync(photoUri)
        }
        ui_photo_edit_cropImageView?.cropShape = CropImageView.CropShape.OVAL
        ui_photo_edit_cropImageView?.guidelines = CropImageView.Guidelines.OFF
        ui_photo_edit_cropImageView?.setAspectRatio(1, 1)

        ui_edit_photo_cancel?.setOnClickListener {
            dismiss()
        }

        ui_photo_edit_bt_rotate?.setOnClickListener {
            rotateImage()
        }

        ui_edit_photo_validate?.setOnClickListener {
            ui_edit_photo_validate?.isEnabled = false
            ui_photo_edit_cropImageView?.setOnCropImageCompleteListener { view, result ->
                if (result.isSuccessful) {
                    mListener?.onPhotoEdited(result.uri, photoSource)
                    dismiss()
                } else {
                    Toast.makeText(activity, R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show()
                    ui_edit_photo_validate.isEnabled = true
                }
            }
            try {
                val croppedImageFile: File? = createImageFile()
                ui_photo_edit_cropImageView?.saveCroppedImageAsync(Uri.fromFile(croppedImageFile))
            } catch (e: IOException) {
                Toast.makeText(activity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun rotateImage() {
        ui_photo_edit_cropImageView?.rotateImage(ROTATE_DEGREES_STEP)
    }

    fun setCallback(callback:PhotoEditDelegate) {
        mListener = callback
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(Date())
        val imageFileName = "ENTOURAGE_CROP_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        )
    }

    //**********//**********//**********
    // CropImageView.OnSetImageUriCompleteListener
    //**********//**********//**********

    override fun onSetImageUriComplete(view: CropImageView?, uri: Uri?, error: Exception?) {
        ui_photo_edit_progressBar?.visibility = View.GONE
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        const val TAG = "social.entourage.android.onboarding.OnboardingEditPhotoFragment"

        @JvmStatic
        fun newInstance(photoUri: Uri?, photoSource: Int) =
                OnboardingEditPhotoFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(PHOTO_PARAM, photoUri)
                        putInt(PHOTO_SOURCE, photoSource)
                    }
                }
    }
}
