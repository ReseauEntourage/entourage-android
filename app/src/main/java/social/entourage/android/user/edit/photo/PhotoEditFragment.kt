package social.entourage.android.user.edit.photo

import android.content.Context
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_photo_edit.*
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PhotoEditFragment  : BaseDialogFragment(), CropImageView.OnSetImageUriCompleteListener {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var mListener: PhotoChooseInterface? = null
    private var photoUri: Uri? = null
    private var photoSource = 0

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
        photo_edit_cropImageView?.setOnSetImageUriCompleteListener(this)
        photoUri?.let{
            photo_edit_progressBar?.visibility = View.VISIBLE
            photo_edit_cropImageView?.setImageUriAsync(it)
        }
        //TODO use new lib com.theartofdev.edmodo:android-image-cropper 2.8 when migrating to AndroidX but meanwhile use this trick to avoid a crash
        photo_edit_cropImageView?.cropShape = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) CropImageView.CropShape.RECTANGLE else CropImageView.CropShape.OVAL
        photo_edit_cropImageView?.setAspectRatio(1, 1)
        photo_edit_back_button.setOnClickListener { onBackClicked() }
        photo_edit_rotate_button.setOnClickListener { onRotateClicked() }
        photo_edit_fab_button.setOnClickListener { onOkClicked() }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
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

    fun onBackClicked() {
        dismiss()
    }

    fun onRotateClicked() {
        photo_edit_cropImageView?.rotateImage(ROTATE_DEGREES_STEP)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_USER_ROTATE_PHOTO)
    }

    // ----------------------------------
    // Upload handling
    // ----------------------------------
    fun onOkClicked() {
        photo_edit_fab_button?.isEnabled = false
        photo_edit_cropImageView?.setOnCropImageCompleteListener { _, result ->
            if (result.isSuccessful) {
                mListener?.onPhotoChosen(result.uri, photoSource)
            } else {
                Timber.e(result.error)
                Toast.makeText(activity, R.string.user_photo_error_no_photo, Toast.LENGTH_SHORT).show()
                photo_edit_fab_button?.isEnabled = true
            }
        }
        try {
            val croppedImageFile = createImageFile()
            photo_edit_cropImageView?.saveCroppedImageAsync(Uri.fromFile(croppedImageFile))
        } catch (e: IOException) {
            Toast.makeText(activity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show()
        }
    }

    // ----------------------------------
    // CropImageView.OnSetImageUriCompleteListener
    // ----------------------------------
    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        photo_edit_progressBar?.visibility = View.GONE
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(Date())
        val imageFileName = "ENTOURAGE_CROP_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                storageDir /* directory */
        )
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.photo_edit"
        private const val PHOTO_PARAM = "social.entourage.android.photo_param"
        private const val PHOTO_SOURCE = "social.entourage.android.photo_source"
        private const val ROTATE_DEGREES_STEP = -90
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