package social.entourage.android.onboarding.onboard

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.takusemba.cropme.OnCropListener

import social.entourage.android.R
import social.entourage.android.databinding.FragmentOnboardingEditPhotoBinding
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.rotate
import social.entourage.android.user.edit.photo.PhotoEditInterface
import timber.log.Timber
import java.io.File
import java.io.IOException

class OnboardingEditPhotoFragment : DialogFragment() {
    private lateinit var binding: FragmentOnboardingEditPhotoBinding
    private val ROTATE_DEGREES_STEP = -90f
    private var currentAngle = 0f

    private var mListener: PhotoEditInterface? = null
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
        binding = FragmentOnboardingEditPhotoBinding.inflate(inflater, container, false)
        return binding.root
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

    fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    private fun setupViews() {
        context?.let { context ->
            binding.uiPhotoEditProgressBar.indeterminateDrawable?.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_ATOP)
        }

        photoUri?.let {
            try {
                binding.cropView.setUri(it)

            } catch(e: IOException) {
                Timber.e(e)
            }
        }

        binding.cropView.addOnCropListener(object : OnCropListener {
            override fun onSuccess(bitmap: Bitmap) {
                binding.uiPhotoEditProgressBar.visibility = View.GONE
                try {
                    val squareBitmap = cropToSquare(bitmap)
                    saveBitmap(squareBitmap)
                    updateProfilePicture()
                } catch (e: IOException) {
                    Toast.makeText(activity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(e: Exception) {
                try {
                    Toast.makeText(activity, R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show()
                    binding.uiPhotoEditProgressBar.visibility = View.GONE
                    binding.uiEditPhotoValidate.isEnabled = true
                } catch (e2: Exception) {
                    Timber.w(e2)
                }
            }
        })

        binding.uiEditPhotoCancel.setOnClickListener {
            dismiss()
        }

        binding.uiPhotoEditBtRotate.setOnClickListener {
            rotateImage()
        }

        binding.uiEditPhotoValidate.setOnClickListener {
            binding.uiEditPhotoValidate.isEnabled = false
            binding.uiPhotoEditProgressBar.visibility = View.VISIBLE
            try {
                binding.cropView.crop()
            } catch(e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun rotateImage() {
        currentAngle += ROTATE_DEGREES_STEP
        photoUri?.let { photoUri->
            try {
                activity?.contentResolver?.let { contentResolver ->
                    saveBitmap(Utils.getBitmapFromUri(photoUri, contentResolver).rotate(currentAngle))
                }
            } catch(e: IOException) {
                Timber.e(e)
            }
        }
    }

    private fun saveBitmap(bitmap: Bitmap) {
        binding.cropView.setBitmap(bitmap)
        photoFile = Utils.saveBitmapToFile(bitmap, photoFile, requireContext())
    }

    private fun updateProfilePicture() {
        mListener?.onPhotoEdited(Uri.fromFile(photoFile), photoSource)
        dismissAllowingStateLoss()
    }

    fun setCallback(callback: PhotoEditInterface) {
        mListener = callback
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        const val TAG = "social.entourage.android.onboarding.OnboardingEditPhotoFragment"
        private const val PHOTO_PARAM = "social.entourage.android.photo_param"
        private const val PHOTO_SOURCE = "social.entourage.android.photo_source"

        fun newInstance(photoUri: Uri?, photoSource: Int) =
            OnboardingEditPhotoFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PHOTO_PARAM, photoUri)
                    putInt(PHOTO_SOURCE, photoSource)
                }
            }
    }
}
