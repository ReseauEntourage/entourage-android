package social.entourage.android.old_v7.user.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.v7_fragment_user_report.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.api.model.UserReport
import social.entourage.android.api.model.UserReportWrapper
import social.entourage.android.base.BaseDialogFragment

/**
 * User Report Fragment
 */
class UserReportFragment : BaseDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var userId = 0
    private var sending = false

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt(User.KEY_USER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.v7_fragment_user_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showKeyboard()
        user_report_close_button.setOnClickListener { onCloseClicked() }
        user_report_send_button.setOnClickListener { onSendClicked() }
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
                    Toast.makeText(
                        context,
                        R.string.user_report_error_reason_empty,
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }
            }
            return true
        }

    private fun sendReport() {
        sending = true
        val reason = user_report_reason_edittext?.text.toString()
        val call = get().apiModule.userRequest.reportUser(
            userId,
            UserReportWrapper(UserReport(reason, arrayListOf()))
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(activity, R.string.user_report_success, Toast.LENGTH_SHORT)
                        .show()
                    if (!isStopped) {
                        dismiss()
                    }
                } else {
                    Toast.makeText(
                        activity,
                        R.string.user_report_error_send_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    sending = false
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(activity, R.string.user_report_error_send_failed, Toast.LENGTH_SHORT)
                    .show()
                sending = false
            }
        })
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        val TAG: String? = UserReportFragment::class.java.simpleName

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