package social.entourage.android.events

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.databinding.ActivityEditRecurrenceBinding
import social.entourage.android.events.create.Recurrence
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.Const
import java.text.SimpleDateFormat
import java.util.Date

class EditRecurrenceActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditRecurrenceBinding

    var date: Date? = null
    var eventId: Int = Const.DEFAULT_VALUE
    var recurrence: Int = Const.DEFAULT_VALUE
    private val editedEvent: MutableMap<String, Any?> = mutableMapOf()
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditRecurrenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventId = intent.getIntExtra(Const.EVENT_ID, Const.DEFAULT_VALUE)
        recurrence = intent.getIntExtra(Const.RECURRENCE, Const.DEFAULT_VALUE)
        date = intent.getSerializableExtra(Const.EVENT_DATE) as Date?
        eventPresenter.isEventUpdated.observe(this, ::hasRecurrenceBeenChanged)

        setView()
        setRecurrence()
        validateEditRecurrence()
        cancel()
    }

    private fun hasRecurrenceBeenChanged(changed: Boolean) {
        if (changed) {
            RefreshController.shouldRefreshEventFragment = true
            finish()
        }
    }

    private fun setView() {
        val locale = LanguageManager.getLocaleFromPreferences(this)
        val sdf = SimpleDateFormat(getString(R.string.events_date), locale)
        binding.date.text = date?.let { it1 ->
            sdf.format(
                it1
            )
        }
        when (recurrence) {
            Recurrence.EVERY_WEEK.value -> binding.everyWeek.isChecked = true
            Recurrence.EVERY_TWO_WEEKS.value -> binding.everyTwoWeek.isChecked = true
            else -> binding.once.isChecked = true
        }
        updatePaddingTopForEdgeToEdge(binding.header.layout)
    }

    private fun validateEditRecurrence() {
        binding.validate.setOnClickListener {
            val event: ArrayMap<String, Any> = ArrayMap()
            if (editedEvent.isEmpty()) finish()
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
                R.id.once -> editedEvent["recurrency"] = Recurrence.NO_RECURRENCE.value
                R.id.every_week -> editedEvent["recurrency"] =
                    Recurrence.EVERY_WEEK.value
                R.id.every_two_week -> editedEvent["recurrency"] =
                    Recurrence.EVERY_TWO_WEEKS.value
            }
        }
    }
}