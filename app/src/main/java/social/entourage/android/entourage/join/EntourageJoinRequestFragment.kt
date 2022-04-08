package social.entourage.android.entourage.join

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_entourage_join_request_ok.*
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.feed.FeedItem

class EntourageJoinRequestFragment  : DialogFragment() {

    // ----------------------------------
    // PRIVATE MEMBERS
    // ----------------------------------
    private lateinit var entourage: BaseEntourage
    private var viewModel: EntourageJoinRequestViewModel = EntourageJoinRequestViewModel()
    private var startedTyping = false

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.let {
            it.requestFeature(Window.FEATURE_NO_TITLE)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_entourage_join_request_ok, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val newFeedItem = arguments?.getSerializable(FeedItem.KEY_FEEDITEM) as BaseEntourage?
        if(newFeedItem == null) {
            dismiss()
            return
        }
        entourage = newFeedItem
        val descriptionTextId = if (entourage.isEvent()) R.string.tour_join_request_ok_description_outing
                else R.string.tour_join_request_ok_description_entourage
        tour_join_request_ok_description?.setText(descriptionTextId)
        view.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager? ?:return@setOnClickListener
            tour_join_request_ok_message?.windowToken?.let { token: IBinder ->
                imm.hideSoftInputFromWindow(token, 0)
            }
        }
        tour_join_request_ok_message?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty() && !startedTyping) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_JOIN_REQUEST_START)
                    startedTyping = true
                }
            }
        })
        tour_join_request_ok_message_button?.setOnClickListener {onMessageSend()}
        tour_join_request_ok_x_button?.setOnClickListener { dismiss() }
        viewModel.requestResult.observe(viewLifecycleOwner) {
            when (it) {
                EntourageJoinRequestViewModel.REQUEST_ERROR -> {
                    Toast.makeText(
                        context,
                        R.string.tour_join_request_message_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                EntourageJoinRequestViewModel.REQUEST_OK -> {
                    Toast.makeText(
                        context,
                        R.string.tour_join_request_message_sent,
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
            }
        }
    }

    private fun onMessageSend() {
        entourage.uuid?.let { uuid ->
            tour_join_request_ok_message?.text?.let {
                if (it.isNotBlank()) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_JOIN_REQUEST_SUBMIT)
                    viewModel.sendMessage(it.toString(), uuid)
                }
            }
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "entourage_join_request_message"
        fun newInstance(entourage: BaseEntourage): EntourageJoinRequestFragment {
            val fragment = EntourageJoinRequestFragment()
            val args = Bundle()
            args.putSerializable(FeedItem.KEY_FEEDITEM, entourage)
            fragment.arguments = args
            return fragment
        }
    }
}