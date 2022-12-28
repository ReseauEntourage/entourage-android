package social.entourage.android.events.create

import android.app.TimePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import social.entourage.android.R
import social.entourage.android.databinding.NewTimePickerStartEndBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

const val HOURS_START_END_INTERVAL = 3

class TimePickerStartEnd @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var _binding: NewTimePickerStartEndBinding? = null
    val binding: NewTimePickerStartEndBinding get() = _binding!!

    init {
        init()
    }

    private fun init() {
        _binding = NewTimePickerStartEndBinding.inflate(LayoutInflater.from(context), this, true)
        startTimeTransformIntoTimePicker(context.getString(R.string.events_time))
        endTimeTransformIntoTimePicker(context.getString(R.string.events_time))
    }

    fun isEndDateValid(): Boolean {
        return binding.endTime.text.isNotEmpty()
    }

    fun isStartTimeValid(): Boolean {
        return binding.startTime.text.isNotEmpty()
    }

    fun getStartTime(): String {
        return binding.startTime.text.toString()
    }

    fun getEndTime(): String {
        return binding.endTime.text.toString()
    }

    fun setStartTime(time: String?) {
        binding.startTime.setText(time)
    }

    fun setEndTime(time: String?) {
        binding.endTime.setText(time)
    }

    fun getStartEditText(): EditText {
        return binding.startTime
    }

    fun getEndEditText(): EditText {
        return binding.endTime
    }

    private fun startTimeTransformIntoTimePicker(format: String) {
        binding.startTime.setOnClickListener {
            val endTimeString = binding.endTime.text.toString()
            val sdf = SimpleDateFormat(format, Locale.FRANCE)
            val myCalendar = Calendar.getInstance()
            myCalendar.add(Calendar.MINUTE,5)
            val endTime = parseTime(format, endTimeString)

            if (endTime != null) {
                myCalendar.time = endTime
            }

            val startTime = if (endTime != null) (myCalendar
                .get(Calendar.HOUR_OF_DAY) - HOURS_START_END_INTERVAL) else myCalendar
                .get(Calendar.HOUR_OF_DAY)

            val timePickerOnDataSetListener =
                TimePickerDialog.OnTimeSetListener { _, hours, minute ->
                    myCalendar.set(Calendar.HOUR_OF_DAY, hours)
                    myCalendar.set(Calendar.MINUTE, minute)
                    binding.startTime.setText(sdf.format(myCalendar.time))
                }

            TimePickerDialog(
                context, timePickerOnDataSetListener, startTime, myCalendar.get(Calendar.MINUTE),
                true
            ).run {
                show()
            }
        }
    }

    private fun endTimeTransformIntoTimePicker(format: String) {
        binding.endTime.setOnClickListener {
            val startTimeString = binding.startTime.text.toString()
            val sdf = SimpleDateFormat(format, Locale.FRANCE)
            val myCalendar = Calendar.getInstance()

            val startTime = parseTime(format, startTimeString)

            if (startTime != null) {
                myCalendar.time = startTime
            }

            val endTime = if (startTime != null) (myCalendar
                .get(Calendar.HOUR_OF_DAY) + HOURS_START_END_INTERVAL) else myCalendar
                .get(Calendar.HOUR_OF_DAY)

            val timePickerOnDataSetListener =
                TimePickerDialog.OnTimeSetListener { _, hours, minute ->
                    myCalendar.set(Calendar.HOUR_OF_DAY, hours)
                    myCalendar.set(Calendar.MINUTE, minute)
                    binding.endTime.setText(sdf.format(myCalendar.time))
                }

            TimePickerDialog(
                context, timePickerOnDataSetListener, endTime, myCalendar.get(Calendar.MINUTE),
                true
            ).run {
                show()
            }
        }
    }

    fun checkTimeValidity(): Boolean? {
        val startTime =
            parseTime(context.getString(R.string.events_time), binding.startTime.text.toString())
        val endTime =
            parseTime(context.getString(R.string.events_time), binding.endTime.text.toString())
        return endTime?.after(startTime)
    }

    private fun parseTime(format: String, time: String): Date? {
        val sdf = SimpleDateFormat(format, Locale.FRANCE)
        return try {
            sdf.parse(time)
        } catch (e: ParseException) {
            null
        }
    }
}