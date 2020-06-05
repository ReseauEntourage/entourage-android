package social.entourage.android.user.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.api.model.UserReport
import social.entourage.android.api.model.UserReport.UserReportWrapper
import social.entourage.android.base.EntourageDialogFragment

/**
 * User Report Fragment
 */
class UserReportFragment  // ----------------------------------
// LIFECYCLE
// ----------------------------------
    : EntourageDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var userId = 0

    @BindView(R.id.user_report_reason_edittext)
    var reasonEditText: EditText? = null
    private var sending = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            userId = arguments!!.getInt(User.KEY_USER_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_report, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showKeyboard()
    }

    override fun getSlideStyle(): Int {
        return R.style.CustomDialogFragmentSlide
    }

    // ----------------------------------
    // ONCLICK CALLBACKS
    // ----------------------------------
    @OnClick(R.id.user_report_close_button)
    fun onCloseClicked() {
        dismiss()
    }

    @OnClick(R.id.user_report_send_button)
    fun onSendClicked() {
        if (sending) return
        if (isValid) {
            sendReport()
        }
    }// The reason cannot be empty

    // ----------------------------------
    // Private methods
    // ----------------------------------
    private val isValid: Boolean
        private get() {
            val reason = reasonEditText!!.text.toString()
            if (reason.trim { it <= ' ' }.length == 0) {
                // The reason cannot be empty
                Toast.makeText(context, R.string.user_report_error_reason_empty, Toast.LENGTH_SHORT).show()
                return false
            }
            return true
        }

    private fun sendReport() {
        val userRequest = get().entourageComponent.userRequest ?: return
        sending = true
        val reason = reasonEditText!!.text.toString()
        val call = userRequest.reportUser(userId, UserReportWrapper(UserReport(reason)))
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    Toast.makeText(activity, R.string.user_report_success, Toast.LENGTH_SHORT).show()
                    if (!isStopped) {
                        dismiss()
                    }
                } else {
                    Toast.makeText(activity, R.string.user_report_error_send_failed, Toast.LENGTH_SHORT).show()
                    sending = false
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Toast.makeText(activity, R.string.user_report_error_send_failed, Toast.LENGTH_SHORT).show()
                sending = false
            }
        })
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        val TAG = UserReportFragment::class.java.simpleName

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param userId The id of the reported user.
         * @return A new instance of fragment UserReportFragment.
         */
        fun newInstance(userId: Int): UserReportFragment {
            val fragment = UserReportFragment()
            val args = Bundle()
            args.putInt(User.KEY_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }
}