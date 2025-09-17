package social.entourage.android.onboarding.onboard

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
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.takusemba.cropme.OnCropListener
import social.entourage.android.R
import social.entourage.android.databinding.FragmentOnboardingEditPhotoBinding
import social.entourage.android.tools.rotate
import social.entourage.android.tools.utils.Utils
import social.entourage.android.user.edit.photo.PhotoEditInterface
import timber.log.Timber
import java.io.File
import java.io.IOException

class OnboardingEditPhotoFragment : DialogFragment() {

    private lateinit var binding: FragmentOnboardingEditPhotoBinding
    private var currentAngle = 0f

    private var mListener: PhotoEditInterface? = null
    private var photoUri: Uri? = null
    private var photoSource = 0
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            photoUri = it.getParcelable(PHOTO_PARAM)
            photoSource = it.getInt(PHOTO_SOURCE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnboardingEditPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
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

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    private fun setupViews() {
        context?.let {
            binding.uiPhotoEditProgressBar.indeterminateDrawable?.setColorFilter(
                ContextCompat.getColor(it, R.color.white),
                PorterDuff.Mode.SRC_ATOP
            )
        }

        try {
            photoUri?.let { binding.cropView.setUri(it) }
        } catch (e: IOException) {
            Timber.e(e)
        }

        binding.cropView.addOnCropListener(object : OnCropListener {
            override fun onSuccess(bitmap: Bitmap) {
                binding.uiPhotoEditProgressBar.visibility = View.GONE
                try {
                    val squareBitmap = cropToSquare(bitmap)
                    saveBitmapSecure(squareBitmap)
                    updateProfilePicture()
                } catch (e: IOException) {
                    Timber.e(e)
                    Toast.makeText(activity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(e: Exception) {
                Timber.w(e)
                activity?.let { activity->
                    Toast.makeText(activity, R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show()
                    binding.uiPhotoEditProgressBar.visibility = View.GONE
                    binding.uiEditPhotoValidate.isEnabled = true
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
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun rotateImage() {
        currentAngle += ROTATE_DEGREES_STEP
        photoUri?.let { uri ->
            try {
                activity?.contentResolver?.let { resolver ->
                    val rotatedBitmap = Utils.getBitmapFromUri(uri, resolver).rotate(currentAngle)
                    saveBitmapSecure(rotatedBitmap)
                }
            } catch (e: IOException) {
                Timber.e(e)
            }
        }
    }

    private fun saveBitmapSecure(bitmap: Bitmap) {
        // Sauvegarde dans filesDir pour éviter les problèmes de permission
        val fileName = "cropped_photo_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().filesDir, fileName)

        Utils.saveBitmapToFile(bitmap, file, requireContext())
        photoFile = file
        binding.cropView.setBitmap(bitmap)
    }

    private fun updateProfilePicture() {
        if (photoFile != null) {
            val safeUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile!!
            )
            mListener?.onPhotoEdited(safeUri, photoSource)
        }
        dismissAllowingStateLoss()
    }

    fun setCallback(callback: PhotoEditInterface) {
        mListener = callback
    }

    companion object {
        const val TAG = "social.entourage.android.onboarding.OnboardingEditPhotoFragment"
        private const val PHOTO_PARAM = "social.entourage.android.photo_param"
        private const val PHOTO_SOURCE = "social.entourage.android.photo_source"

        private const val ROTATE_DEGREES_STEP = -90f

        fun newInstance(photoUri: Uri?, photoSource: Int): OnboardingEditPhotoFragment {
            return OnboardingEditPhotoFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PHOTO_PARAM, photoUri)
                    putInt(PHOTO_SOURCE, photoSource)
                }
            }
        }
    }
}
