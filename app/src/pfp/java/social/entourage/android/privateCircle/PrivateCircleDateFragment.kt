package social.entourage.android.privateCircle

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.CalendarView.OnDateChangeListener
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.ChatMessage.ChatMessageWrapper
import social.entourage.android.api.model.VisitChatMessage
import social.entourage.android.api.model.VisitChatMessage.VisitChatMessageWrapper
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.base.EntourageDialogFragment
import java.text.DateFormat
import java.util.*

/**
 * A [EntourageDialogFragment] subclass, to select the date of a neighborhood visit
 * Use the [PrivateCircleDateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PrivateCircleDateFragment  // ----------------------------------
// LIFECYCLE
// ----------------------------------
    : EntourageDialogFragment(), OnDateChangeListener {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @JvmField
    @BindView(R.id.title_action_button)
    var actionButton: TextView? = null

    @JvmField
    @BindView(R.id.privatecircle_date_today)
    var dateTodayTextView: TextView? = null

    @JvmField
    @BindView(R.id.privatecircle_date_today_checkBox)
    var dateTodayCB: CheckBox? = null

    @JvmField
    @BindView(R.id.privatecircle_date_yesterday)
    var dateYesterdayTextView: TextView? = null

    @JvmField
    @BindView(R.id.privatecircle_date_yesterday_checkBox)
    var dateYesterdayCB: CheckBox? = null

    @JvmField
    @BindView(R.id.privatecircle_date_other)
    var dateOtherTextView: TextView? = null

    @JvmField
    @BindView(R.id.privatecircle_date_other_checkBox)
    var dateOtherCB: CheckBox? = null

    @JvmField
    @BindView(R.id.privatecircle_calendarView)
    var calendarView: CalendarView? = null
    private var selectedRow = ROW_TODAY
    private var entourageId: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        entourageId = arguments?.getLong(FeedItem.KEY_FEEDITEM_ID, 0) ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_privatecircle_date, container, false)
        ButterKnife.bind(this, v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureView()
    }

    private fun configureView() {
        //initialise the calendar with current day - 2 days
        val now = System.currentTimeMillis()
        val other = now - 2 * 24 * 60 * 60 * 1000
        calendarView!!.date = other
        calendarView!!.setOnDateChangeListener(this)
        calendarView!!.visibility = View.GONE
        //select the today by default
        selectRow(selectedRow, true)
    }

    // ----------------------------------
    // Buttons Handling
    // ----------------------------------
    @OnClick(R.id.title_close_button)
    fun onCloseButtonClicked() {
        dismiss()
    }

    @OnClick(R.id.title_action_button)
    fun onNextButtonClicked() {
        var visitDate: Date? = null
        when (selectedRow) {
            ROW_TODAY -> visitDate = Date()
            ROW_YESTERDAY -> visitDate = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
            ROW_OTHER -> visitDate = Date(calendarView!!.date)
        }
        visitDate?.let { sendVisitDate(it) }
    }

    @OnClick(R.id.privatecircle_date_today_holder, R.id.privatecircle_date_yesterday_holder, R.id.privatecircle_date_other_holder, R.id.privatecircle_date_today_checkBox, R.id.privatecircle_date_yesterday_checkBox, R.id.privatecircle_date_other_checkBox)
    fun onRowClicked(view: View) {
        val tag = view.tag as String? ?:return
        val position = tag.toInt()
        if (position != selectedRow) {
            selectRow(selectedRow, false)
            selectedRow = position
            selectRow(selectedRow, true)
            calendarView!!.visibility = if (position == ROW_OTHER) View.VISIBLE else View.GONE
        } else {
            if (view is CheckBox) {
                view.isChecked = true
            }
        }
    }

    // ----------------------------------
    // Rows Handling
    // ----------------------------------
    private fun selectRow(row: Int, select: Boolean) {
        when (row) {
            ROW_TODAY -> {
                if (select) {
                    dateTodayTextView!!.setTypeface(dateTodayTextView!!.typeface, Typeface.BOLD)
                } else {
                    dateTodayTextView!!.typeface = Typeface.create(dateTodayTextView!!.typeface, Typeface.NORMAL)
                }
                dateTodayCB!!.isChecked = select
            }
            ROW_YESTERDAY -> {
                if (select) {
                    dateYesterdayTextView!!.setTypeface(dateYesterdayTextView!!.typeface, Typeface.BOLD)
                } else {
                    dateYesterdayTextView!!.typeface = Typeface.create(dateYesterdayTextView!!.typeface, Typeface.NORMAL)
                }
                dateYesterdayCB!!.isChecked = select
            }
            ROW_OTHER -> {
                if (select) {
                    dateOtherTextView!!.setTypeface(dateOtherTextView!!.typeface, Typeface.BOLD)
                } else {
                    dateOtherTextView!!.typeface = Typeface.create(dateOtherTextView!!.typeface, Typeface.NORMAL)
                }
                dateOtherCB!!.isChecked = select
            }
        }
    }

    // ----------------------------------
    // CalendarView.OnDateChangeListener
    // ----------------------------------
    override fun onSelectedDayChange(view: CalendarView, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar[year, month] = dayOfMonth
        view.date = calendar.timeInMillis
        dateOtherTextView!!.text = DateFormat.getDateInstance().format(calendar.time)
    }

    // ----------------------------------
    // API Calls
    // ----------------------------------
    private fun sendVisitDate(visitDate: Date) {
        actionButton!!.isEnabled = false
        val request = EntourageApplication.get().entourageComponent.privateCircleRequest
        val visitChatMessage = VisitChatMessage(VisitChatMessage.TYPE_VISIT, visitDate)
        val call = request.visitMessage(entourageId, VisitChatMessageWrapper(visitChatMessage))
        call?.enqueue(object : Callback<ChatMessageWrapper?> {
            override fun onResponse(call: Call<ChatMessageWrapper?>, response: Response<ChatMessageWrapper?>) {
                if (response.isSuccessful) {
                    Toast.makeText(EntourageApplication.get(), R.string.privatecircle_visit_sent_ok, Toast.LENGTH_SHORT).show()
                    if (!isStateSaved && isAdded) {
                        // pop to the menu, if the screen it is still displayed
                        dismiss()
                        val privateCircleChooseFragment = parentFragmentManager.findFragmentByTag(PrivateCircleChooseFragment.TAG) as PrivateCircleChooseFragment?
                        privateCircleChooseFragment?.dismiss()
                    }
                } else {
                    if (context != null) {
                        Toast.makeText(context, R.string.privatecircle_visit_sent_error, Toast.LENGTH_SHORT).show()
                    }
                }
                actionButton!!.isEnabled = true
            }

            override fun onFailure(call: Call<ChatMessageWrapper?>, t: Throwable) {
                if (context != null) {
                    Toast.makeText(context, R.string.privatecircle_visit_sent_error, Toast.LENGTH_SHORT).show()
                }
                actionButton!!.isEnabled = true
            }
        })
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        @JvmField
        val TAG = PrivateCircleDateFragment::class.java.simpleName
        private const val ROW_TODAY = 0
        private const val ROW_YESTERDAY = 1
        private const val ROW_OTHER = 2

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param entourageId Entourage ID.
         * @return A new instance of fragment PrivateCircleDateFragment.
         */
        @JvmStatic
        fun newInstance(entourageId: Long): PrivateCircleDateFragment {
            val fragment = PrivateCircleDateFragment()
            val args = Bundle()
            args.putLong(FeedItem.KEY_FEEDITEM_ID, entourageId)
            fragment.arguments = args
            return fragment
        }
    }
}