package social.entourage.android.entourage

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_entourage_disclaimer.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.base.EntourageDialogFragment

class EntourageDisclaimerFragment : EntourageDialogFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private var mListener: OnFragmentInteractionListener? = null

    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        val groupType: String? = arguments?.getString(KEY_GROUP_TYPE, null)
        return inflater.inflate(
                if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) R.layout.fragment_outing_disclaimer else R.layout.fragment_entourage_disclaimer,
                container,
                false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        entourage_disclaimer_text_chart?.setOnClickListener {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_DISCLAIMER_LINK)
            val disclaimerURL = getString(R.string.disclaimer_link_public)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(disclaimerURL))
            try {
                startActivity(browserIntent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
            }
        }
        entourage_disclaimer_switch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_DISCLAIMER_ACCEPT)
                // trigger the accept after a delay
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({ onOkClicked() }, 1000)
            }
        }
        title_close_button.setOnClickListener {onCloseClicked()}
        entourage_disclaimer_ok_button.setOnClickListener  {onOkClicked()}
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnFragmentInteractionListener) {
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

    override fun getSlideStyle(): Int {
        return R.style.CustomDialogFragmentSlide
    }

    // ----------------------------------
    // Button handling
    // ----------------------------------
    fun onCloseClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_DISCLAIMER_CLOSE)
        dismiss()
    }

    fun onOkClicked() {
        if (entourage_disclaimer_switch?.isChecked == true) {
            //inform the listener that the user accepted the CGU
            mListener?.onEntourageDisclaimerAccepted(this)
        } else {
            Toast.makeText(activity, R.string.entourage_disclaimer_error_notaccepted, Toast.LENGTH_SHORT).show()
        }
    }

    // ----------------------------------
    // Listener
    // ----------------------------------
    interface OnFragmentInteractionListener {
        fun onEntourageDisclaimerAccepted(fragment: EntourageDisclaimerFragment?)
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        const val TAG = "social.entourage.android.entourage.disclaimer"
        private const val KEY_GROUP_TYPE = "social.entourage.android.KEY_GROUP_TYPE"
        fun newInstance(groupType: String?): EntourageDisclaimerFragment {
            val fragment = EntourageDisclaimerFragment()
            val args = Bundle()
            args.putString(KEY_GROUP_TYPE, groupType)
            fragment.arguments = args
            return fragment
        }
    }
}