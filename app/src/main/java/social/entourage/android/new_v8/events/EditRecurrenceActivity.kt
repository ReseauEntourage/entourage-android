package social.entourage.android.new_v8.events

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.new_rules_item.*
import social.entourage.android.R
import social.entourage.android.databinding.NewActivityEditRecurrenceBinding
import social.entourage.android.new_v8.events.create.Recurrence
import social.entourage.android.new_v8.utils.Const
import java.text.SimpleDateFormat
import java.util.*

class EditRecurrenceActivity : AppCompatActivity() {
    lateinit var binding: NewActivityEditRecurrenceBinding

    var date: Date? = null
    var eventId: Int = Const.DEFAULT_VALUE
    private val editedEvent: MutableMap<String, Any?> = mutableMapOf()
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_edit_recurrence
        )
        eventId = intent.getIntExtra(Const.EVENT_ID, Const.DEFAULT_VALUE)
        date = intent.getSerializableExtra(Const.EVENT_DATE) as Date?
        eventPresenter.isEventUpdated.observe(this, ::hasRecurrenceBeenChanged)

        setView()
        setRecurrence()
        validateEditRecurrence()
        cancel()
    }

    private fun hasRecurrenceBeenChanged(changed: Boolean) {
        if (changed) finish()
    }

    private fun setView() {
        val sdf = SimpleDateFormat(getString(R.string.events_date), Locale.FRANCE)
        binding.date.text = date?.let { it1 ->
            sdf.format(
                it1
            )
        }
    }

    private fun validateEditRecurrence() {
        binding.validate.setOnClickListener {
            val event: ArrayMap<String, Any> = ArrayMap()
            if (eventId != Const.DEFAULT_VALUE) {
                event["outing"] = editedEvent
                eventPresenter.updateEvent(eventId, event)
            }
        }
    }

    private fun cancel() {
        binding.previous.setOnClickListener {
            finish()
        }
        binding.header.iconBack.setOnClickListener {
            finish()
        }
    }

    private fun setRecurrence() {
        binding.recurrence.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.once -> editedEvent["recurrency"] = null
                R.id.every_week -> editedEvent["recurrency"] =
                    Recurrence.EVERY_WEEK.value
                R.id.every_two_week -> editedEvent["recurrency"] =
                    Recurrence.EVERY_TWO_WEEKS.value
            }
        }
    }
}