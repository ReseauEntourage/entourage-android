package social.entourage.android.base

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.deeplinks.DeepLinksManager

/**
 * Base DialogFragment with no title and full screen
 * Created by mihaiionescu on 17/06/16.
 */
open class BaseDialogFragment : DialogFragment() {
    var isStopped = true
        private set

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.let {DeepLinksManager.handleCurrentDeepLink(it) }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (dialog == null) {
            //TODO should we use setShowsDialog(false) here
            showsDialog = false
        }
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = slideStyle
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            window.setBackgroundDrawable(backgroundDrawable)
            window.attributes?.windowAnimations = slideStyle
        }
        isStopped = false
    }

    override fun onStop() {
        super.onStop()
        isStopped = true
    }

    @get:StyleRes
    protected open val slideStyle: Int
        get() = R.style.CustomDialogFragmentFromRight

    protected open val backgroundDrawable: ColorDrawable?
        get() = ColorDrawable(Color.TRANSPARENT)

    protected fun showKeyboard() {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    /*protected fun hideKeyboard() {
        dialog?.currentFocus?.windowToken?.let { token ->
            (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(token, 0)
        }
    }*/
}