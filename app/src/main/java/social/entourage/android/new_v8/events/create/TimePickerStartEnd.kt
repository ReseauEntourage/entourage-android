package social.entourage.android.new_v8.events.create

import android.app.TimePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TimePicker
import androidx.constraintlayout.widget.ConstraintLayout
import social.entourage.android.R
import social.entourage.android.databinding.NewTimePickerStartEndBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


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
        isFocusableInTouchMode = false
        isClickable = true
        isFocusable = false
        binding.startTime.setOnClickListener {
            val endTimeString = binding.endTime.text.toString()
            val sdf = SimpleDateFormat(format, Locale.FRANCE)
            val myCalendar = Calendar.getInstance()

            val endTime = try {
                sdf.parse(endTimeString)
            } catch (e: ParseException) {
                null
            }

            if (endTime != null) {
                myCalendar.time = endTime
            }

            val startTime = if (endTime != null) (myCalendar
                .get(Calendar.HOUR_OF_DAY) - 3) else myCalendar
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
        isFocusableInTouchMode = false
        isClickable = true
        isFocusable = false
        binding.endTime.setOnClickListener {
            val startTimeString = binding.startTime.text.toString()
            val sdf = SimpleDateFormat(format, Locale.FRANCE)
            val myCalendar = Calendar.getInstance()

            val startTime = try {
                sdf.parse(startTimeString)
            } catch (e: ParseException) {
                null
            }

            if (startTime != null) {
                myCalendar.time = startTime
            }

            val endTime = if (startTime != null) (myCalendar
                .get(Calendar.HOUR_OF_DAY) + 3) else myCalendar
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
                val timePicker: TimePicker
                show()
            }
        }
    }

    fun checkTimeValidity(): Boolean? {
        val sdf = SimpleDateFormat(context.getString(R.string.events_time), Locale.FRANCE)
        val startTime = try {
            sdf.parse(binding.startTime.text.toString())
        } catch (e: ParseException) {
            null
        }
        val endTime = try {
            sdf.parse(binding.endTime.text.toString())
        } catch (e: ParseException) {
            null
        }
        return endTime?.after(startTime)
    }
}