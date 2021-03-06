package social.entourage.android.tour.join

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_tour_join_request_ok.*
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.tour.Tour

class TourJoinRequestFragment  : DialogFragment() {

    // ----------------------------------
    // PRIVATE MEMBERS
    // ----------------------------------
    private lateinit var tour: Tour
    private var viewModel: TourJoinRequestViewModel = TourJoinRequestViewModel()
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
        return inflater.inflate(R.layout.fragment_tour_join_request_ok, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val newTour = arguments?.getSerializable(FeedItem.KEY_FEEDITEM) as Tour?
        if(newTour == null) {
            dismiss()
            return
        }
        tour = newTour
        tour_join_request_ok_description?.setText(R.string.tour_join_request_ok_description_tour)
        view.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager? ?:return@setOnClickListener
            tour_join_request_ok_message?.windowToken?.let {
                imm.hideSoftInputFromWindow(it, 0)
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
        viewModel.requestResult.observe(viewLifecycleOwner, {
            when(it) {
                TourJoinRequestViewModel.REQUEST_ERROR ->{
                    Toast.makeText(context, R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show()
                }
                TourJoinRequestViewModel.REQUEST_OK ->{
                    Toast.makeText(context, R.string.tour_join_request_message_sent, Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        })
    }

    private fun onMessageSend() {
        tour_join_request_ok_message?.text?.let {
            if (it.isNotBlank()) {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_JOIN_REQUEST_SUBMIT)
                viewModel.sendMessage(it.toString(), tour)
            }
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "tour_join_request_message"
        fun newInstance(tour: Tour): TourJoinRequestFragment {
            val fragment = TourJoinRequestFragment()
            val args = Bundle()
            args.putSerializable(FeedItem.KEY_FEEDITEM, tour)
            fragment.arguments = args
            return fragment
        }
    }
}