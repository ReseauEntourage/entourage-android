package social.entourage.android.old_v7.user.edit.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.takusemba.cropme.OnCropListener
import kotlinx.android.synthetic.main.fragment_photo_edit.*
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.old_v7.tools.UtilsV7
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.rotate
import social.entourage.android.user.edit.photo.PhotoChooseInterface
import timber.log.Timber
import java.io.File
import java.io.IOException

class PhotoEditFragment : BaseDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var mListener: PhotoChooseInterface? = null
    private var photoUri: Uri? = null
    private var photoSource = 0

    private var currentAngle = 0f
    private var photoFile: File? = null

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is PhotoChooseInterface) {
            context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            photoUri = it.getParcelable(PHOTO_PARAM)
            photoSource = it.getInt(PHOTO_SOURCE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_SCREEN_09_9)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (context != null) {
            photo_edit_progressBar?.indeterminateDrawable?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white), PorterDuff.Mode.SRC_ATOP)
        }

        photoUri?.let{
            crop_view.setUri(it)
        }

        crop_view.addOnCropListener(object : OnCropListener {
            override fun onSuccess(bitmap: Bitmap) {
                photo_edit_progressBar?.visibility = View.GONE
                try {
                    saveBitmap(bitmap)
                    updateProfilePicture()
                } catch (e: IOException) {
                    Toast.makeText(activity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(e: Exception) {
                Timber.e(e)
                Toast.makeText(activity, R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show()
                photo_edit_progressBar?.visibility = View.GONE
                photo_edit_fab_button?.isEnabled = true
            }
        })

        photo_edit_back_button.setOnClickListener {
            dismiss()
        }

        photo_edit_rotate_button.setOnClickListener {
            rotateImage()
        }

        photo_edit_fab_button.setOnClickListener {
            photo_edit_fab_button?.isEnabled = false
            photo_edit_progressBar?.visibility = View.VISIBLE
            crop_view.crop()
        }
    }

    // ----------------------------------
    // Button handling
    // ----------------------------------
    fun onPhotoSent(success: Boolean): Boolean {
        photo_edit_fab_button?.let {
            if (success && !it.isEnabled && !isStopped) {
                //getContext().getContentResolver().delete(photoUri, "", null);
                dismiss()
                return true
            } else {
                it.isEnabled = true
            }
        }
        return false
    }

    private fun rotateImage() {
        currentAngle += ROTATE_DEGREES_STEP
        photoUri?.let { photoUri ->
            activity?.contentResolver?.let { contentResolver ->
                saveBitmap(Utils.getBitmapFromUri(photoUri, contentResolver).rotate(currentAngle))
            }
        }
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_USER_ROTATE_PHOTO)
    }

    private fun saveBitmap(bitmap: Bitmap) {
        crop_view.setBitmap(bitmap)
        photoFile = Utils.saveBitmapToFile(bitmap, photoFile)
    }

    private fun updateProfilePicture() {
        mListener?.onPhotoChosen(Uri.fromFile(photoFile), photoSource)
    }


    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.photo_edit"
        private const val PHOTO_PARAM = "social.entourage.android.photo_param"
        private const val PHOTO_SOURCE = "social.entourage.android.photo_source"
        private const val ROTATE_DEGREES_STEP = -90f

        fun newInstance(photoUri: Uri, photoSource: Int): PhotoEditFragment {
            val fragment = PhotoEditFragment()
            val args = Bundle()
            args.putParcelable(PHOTO_PARAM, photoUri)
            args.putInt(PHOTO_SOURCE, photoSource)
            fragment.arguments = args
            return fragment
        }
    }
}