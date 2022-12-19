package social.entourage.android.user.edit.photo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.takusemba.cropme.OnCropListener
import kotlinx.android.synthetic.main.fragment_onboarding_edit_photo.*
import social.entourage.android.R
import social.entourage.android.old_v7.tools.UtilsV7
import social.entourage.android.tools.rotate
import timber.log.Timber
import java.io.File
import java.io.IOException

private const val PHOTO_PARAM = "social.entourage.android.photo_param"
private const val PHOTO_SOURCE = "social.entourage.android.photo_source"


class OnboardingEditPhotoFragment : DialogFragment() {
    private val ROTATE_DEGREES_STEP = -90f
    private var currentAngle = 0f

    private var mListener: PhotoEditDelegate? = null
    private var photoUri: Uri? = null
    private var photoSource = 0
    private var photoFile: File? = null

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

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (dialog == null) {
            showsDialog = false
        }

        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.CustomDialogFragmentFromRight
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.attributes?.windowAnimations = R.style.CustomDialogFragmentFromRight
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
        context?.let { context ->
            ui_photo_edit_progressBar?.indeterminateDrawable?.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_ATOP)
        }

        photoUri?.let {
            try {
                crop_view?.setUri(it)

            } catch(e: IOException) {
                Timber.e(e)
            }
        }

        crop_view?.addOnCropListener(object : OnCropListener {
            override fun onSuccess(bitmap: Bitmap) {
                ui_photo_edit_progressBar?.visibility = View.GONE
                try {
                    saveBitmap(bitmap)
                    updateProfilePicture()
                } catch (e: IOException) {
                    Toast.makeText(activity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(e: Exception) {
                try {
                    Toast.makeText(activity, R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show()
                    ui_photo_edit_progressBar?.visibility = View.GONE
                    ui_edit_photo_validate?.isEnabled = true
                } catch (e2: IOException) {
                    Timber.w(e2)
                }
            }
        })

        ui_edit_photo_cancel?.setOnClickListener {
            dismiss()
        }

        ui_photo_edit_bt_rotate?.setOnClickListener {
            rotateImage()
        }

        ui_edit_photo_validate?.setOnClickListener {
            ui_edit_photo_validate?.isEnabled = false
            ui_photo_edit_progressBar?.visibility = View.VISIBLE
            crop_view?.crop()
        }
    }

    private fun rotateImage() {
        currentAngle += ROTATE_DEGREES_STEP
        photoUri?.let { photoUri->
            try {
                activity?.contentResolver?.let { contentResolver ->
                    saveBitmap(UtilsV7.getBitmapFromUri(photoUri, contentResolver).rotate(currentAngle))
                }
            } catch(e: IOException) {
                Timber.e(e)
            }
        }
    }

    private fun saveBitmap(bitmap: Bitmap) {
        crop_view?.setBitmap(bitmap)
        photoFile = UtilsV7.saveBitmapToFile(bitmap, photoFile)
    }

    private fun updateProfilePicture() {
        mListener?.onPhotoEdited(Uri.fromFile(photoFile), photoSource)
        dismissAllowingStateLoss()
    }

    fun setCallback(callback: PhotoEditDelegate) {
        mListener = callback
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        const val TAG = "social.entourage.android.onboarding.OnboardingEditPhotoFragment"

        fun newInstance(photoUri: Uri?, photoSource: Int) =
            OnboardingEditPhotoFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PHOTO_PARAM, photoUri)
                    putInt(PHOTO_SOURCE, photoSource)
                }
            }
    }
}
