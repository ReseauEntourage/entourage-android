package social.entourage.android.entourage.information.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_user_report.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.R
import social.entourage.android.api.model.EntourageReport
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.entourage.information.EntourageInformationFragment

/**
 * Entourage Report Fragment
 */
class EntourageReportFragment  : BaseDialogFragment() {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var entourageId = 0
    private var sending = false
    private var isEvent = false
    private var isReportUser = false
    var parentFragment: EntourageInformationFragment? = null
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            entourageId = it.getInt(KEY_ID)
            isEvent = it.getBoolean(ISEVENT)
            isReportUser = it.getBoolean(ISREPORTUSER)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showKeyboard()
        val title = when {
            isReportUser -> getString(R.string.action_report_explanation_user)
            isEvent -> getString(R.string.event_report_explanation)
            else -> getString(R.string.action_report_explanation)
        }
        val description = when {
            isReportUser -> getString(R.string.action_report_reason_user)
            isEvent -> getString(R.string.event_report_reason)
            else -> getString(R.string.action_report_reason)
        }
        ui_report_tv_title?.text = title
        ui_report_tv_description?.text = description
        user_report_close_button.setOnClickListener  {onCloseClicked()}
        user_report_send_button.setOnClickListener {onSendClicked() }
    }

    override val slideStyle: Int
        get() = R.style.CustomDialogFragmentSlide

    // ----------------------------------
    // ONCLICK CALLBACKS
    // ----------------------------------
    fun onCloseClicked() {
        dismiss()
    }

    private fun onSendClicked() {
        if (sending) return
        if (isValid) {
            sendReport()
        }
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------
    private val isValid: Boolean
        get() {
            user_report_reason_edittext?.text?.toString()?.let { reason ->
                if (reason.isBlank()) {
                    // The reason cannot be empty
                    Toast.makeText(context, R.string.entourage_report_error_reason_empty, Toast.LENGTH_SHORT).show()
                    return false
                }
            }
            return true
        }

    private fun sendReport() {
        val userRequest = get().components.entourageRequest
        sending = true
        val reason = user_report_reason_edittext?.text.toString()
        val call = userRequest.reportEntourage(entourageId, EntourageReport.EntourageReportWrapper(EntourageReport(reason)))

        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(activity, R.string.entourage_report_error_send_failed, Toast.LENGTH_SHORT).show()
                sending = false
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    if (isReportUser) {
                        Toast.makeText(activity, R.string.entourage_user_report_success, Toast.LENGTH_SHORT).show()
                        parentFragment?.validateReport()
                        parentFragment = null
                    }
                    else {
                        Toast.makeText(activity, R.string.entourage_report_success, Toast.LENGTH_SHORT).show()
                    }

                    if (!isStopped) {
                        dismiss()
                    }
                } else {
                    Toast.makeText(activity, R.string.entourage_report_error_send_failed, Toast.LENGTH_SHORT).show()
                    sending = false
                }
            }
        })
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        val TAG = EntourageReportFragment::class.java.simpleName
        const val KEY_ID = "entourageId"
        const val ISEVENT = "isEvent"
        const val ISREPORTUSER = "isReportUser"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param entourageId The id of the entourage.
         * @return A new instance of fragment EntourageReportFragment.
         */
        fun newInstance(entourageId: Int,isEvent:Boolean,isReportUser:Boolean = false): EntourageReportFragment {
            val fragment = EntourageReportFragment()
            val args = Bundle()
            args.putInt(KEY_ID, entourageId)
            args.putBoolean(ISEVENT,isEvent)
            args.putBoolean(ISREPORTUSER,isReportUser)
            fragment.arguments = args
            return fragment
        }
    }
}