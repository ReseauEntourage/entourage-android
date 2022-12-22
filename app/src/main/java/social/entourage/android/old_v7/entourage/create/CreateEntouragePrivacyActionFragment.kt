package social.entourage.android.old_v7.entourage.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.v7_fragment_create_entourage_privacy.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment

/**
 * Dialog Fragment for editing the entourage action privacy
 */
class CreateEntouragePrivacyActionFragment  : BaseDialogFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private var isPublic = true
    private var mListener: CreateEntourageListener? = null
    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {args ->
            isPublic = args.getBoolean(KEY_ENTOURAGE_PUBLIC)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.v7_fragment_create_entourage_privacy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title_close_button?.setOnClickListener { onCloseClicked() }
        initializeView()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showKeyboard()
    }

    override val slideStyle: Int
        get() = R.style.CustomDialogFragmentFromRight

    fun setListener(mListener: CreateEntourageListener?) {
        this.mListener = mListener
    }

    // ----------------------------------
    // Interactions handling
    // ----------------------------------
    fun onCloseClicked() {
        mListener = null
        dismiss()
    }

    // ----------------------------------
    // Private Methods
    // ----------------------------------
    private fun initializeView() {
        ui_layout_privacyAction_public?.setOnClickListener {
            mListener?.onPrivacyChanged(true)
            isPublic = true
            changeView()
            onCloseClicked()
        }

        ui_layout_privacyAction_private?.setOnClickListener {
            mListener?.onPrivacyChanged(false)
            isPublic = false
            changeView()
            onCloseClicked()
        }

        changeView()
    }

    fun changeView() {
        if (isPublic) {
            ui_iv_button_public?.background = ResourcesCompat.getDrawable(resources,R.drawable.carousel_bullet_filled,null)
            ui_iv_button_private?.background = ResourcesCompat.getDrawable(resources,R.drawable.circle_white_plain,null)
        }
        else {
            ui_iv_button_private?.background = ResourcesCompat.getDrawable(resources,R.drawable.carousel_bullet_filled,null)
            ui_iv_button_public?.background = ResourcesCompat.getDrawable(resources,R.drawable.circle_white_plain,null)
        }
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        val TAG: String? = CreateEntouragePrivacyActionFragment::class.java.simpleName
        private const val KEY_ENTOURAGE_PUBLIC = "KEY_ENTOURAGE_PUBLIC"

        fun newInstance(isPublic: Boolean): CreateEntouragePrivacyActionFragment {
            val fragment = CreateEntouragePrivacyActionFragment()
            val args = Bundle()
            args.putBoolean(KEY_ENTOURAGE_PUBLIC,isPublic)
            fragment.arguments = args
            return fragment
        }
    }
}