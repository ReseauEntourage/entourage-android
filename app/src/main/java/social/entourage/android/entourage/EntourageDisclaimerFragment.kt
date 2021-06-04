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
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.base.BaseDialogFragment

class EntourageDisclaimerFragment : BaseDialogFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------


    private var mListener: OnFragmentInteractionListener? = null
    private var isFromNeo = false
    private var tagAnalyticNeoName = ""
    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        val groupType: String? = arguments?.getString(KEY_GROUP_TYPE, null)
        arguments?.let { _args ->
            isFromNeo = _args.getBoolean(ISFROMNEO,false)
            tagAnalyticNeoName = _args.getString(TAGANALYTICS,"")
        }

        return inflater.inflate(
                if (BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) R.layout.fragment_outing_disclaimer else R.layout.fragment_entourage_disclaimer,
                container,
                false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        entourage_disclaimer_text_chart?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_DISCLAIMER_LINK)
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
                val _tag:String
                if (isFromNeo) {
                    _tag = String.format(AnalyticsEvents.ACTION_NEOFEEDACT_AcceptCGU_X,tagAnalyticNeoName)
                }
                else {
                    _tag = AnalyticsEvents.EVENT_ENTOURAGE_DISCLAIMER_ACCEPT
                }
                AnalyticsEvents.logEvent(_tag)

                // trigger the accept after a delay
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({ onOkClicked() }, 1000)
            }
        }
        title_close_button?.setOnClickListener {onCloseClicked()}
        entourage_disclaimer_ok_button?.setOnClickListener  {onOkClicked()}
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

    override val slideStyle: Int
        get() = R.style.CustomDialogFragmentSlide

    // ----------------------------------
    // Button handling
    // ----------------------------------
    fun onCloseClicked() {
        val _tag:String
        if (isFromNeo) {
            _tag = String.format(AnalyticsEvents.ACTION_NEOFEEDACT_CancelCGU_X,tagAnalyticNeoName)
        }
        else {
            _tag = AnalyticsEvents.EVENT_ENTOURAGE_DISCLAIMER_CLOSE
        }
        AnalyticsEvents.logEvent(_tag)
        dismiss()
    }

    fun onOkClicked() {
        if (entourage_disclaimer_switch?.isChecked == true) {
            //inform the listener that the user accepted the CGU
            mListener?.onEntourageDisclaimerAccepted(this)
        } else if(activity != null){
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

        private const val ISFROMNEO = "isFromNeo"
        private const val TAGANALYTICS = "tagAnalytics"

        fun newInstance(groupType: String,tagAnalyticName:String,isFromNeo:Boolean): EntourageDisclaimerFragment {
            val fragment = EntourageDisclaimerFragment()
            val args = Bundle()
            args.putString(KEY_GROUP_TYPE, groupType)
            args.putString(TAGANALYTICS,tagAnalyticName)
            args.putBoolean(ISFROMNEO,isFromNeo)
            fragment.arguments = args
            return fragment
        }
    }
}