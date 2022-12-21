package social.entourage.android.new_v8.events.details

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.collection.ArrayMap
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.setFragmentResult
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentSettingsModalBinding
import social.entourage.android.new_v8.RefreshController
import social.entourage.android.new_v8.events.EditRecurrenceActivity
import social.entourage.android.new_v8.events.EventsPresenter
import social.entourage.android.new_v8.events.create.CreateEventActivity
import social.entourage.android.new_v8.events.create.Recurrence
import social.entourage.android.new_v8.groups.details.rules.GroupRulesActivity
import social.entourage.android.new_v8.models.Events
import social.entourage.android.new_v8.models.Status
import social.entourage.android.new_v8.profile.myProfile.InterestsAdapter
import social.entourage.android.new_v8.report.ReportModalFragment
import social.entourage.android.new_v8.report.ReportTypes
import social.entourage.android.new_v8.utils.CustomAlertDialog
import social.entourage.android.new_v8.utils.Const

class SettingsModalFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentSettingsModalBinding? = null
    val binding: NewFragmentSettingsModalBinding get() = _binding!!

    private var event: Events? = null
    private var interestsList: ArrayList<String> = ArrayList()
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentSettingsModalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getEventInformation()
        handleCloseButton()
        handleRulesButton()
        handleEditEvent()
        updateView()
        viewWithRole()
        handleReportEvent()
        handleCancelEvent()
        handleLeaveEvent()
        handleEditRecurrenceEvent()
        eventPresenter.eventCanceled.observe(viewLifecycleOwner, ::hasEventBeenCanceled)
        eventPresenter.isEventUpdated.observe(viewLifecycleOwner, ::hasEventBeenCanceled)
        eventPresenter.hasUserLeftEvent.observe(viewLifecycleOwner, ::handleLeftResponse)
        setView()
    }

    private fun handleLeftResponse(left: Boolean) {
        if (left) {
            setFragmentResult(
                Const.REQUEST_KEY_SHOULD_REFRESH,
                bundleOf(Const.SHOULD_REFRESH to true)
            )
            dismiss()
        }
    }

    private fun setView() {
        with(binding) {
            notificationNewMessages.root.visibility = View.GONE
            header.title = getString(R.string.event_settings)
            notificationAll.label = getString(R.string.event_notification_all)
            notificationNewEvent.label = getString(R.string.notification_new_publications)
            notificationNewMembers.label = getString(R.string.notification_new_members)
            edit.label = getString(R.string.edit_event_information)
            rules.label = getString(R.string.rules_event)
            report.text = getString(R.string.report_event)
            cancel.text = getString(R.string.cancel_event)
            leave.text = getString(R.string.leave_event)
            editRecurrence.label = getString(R.string.modify_recurrence)
        }
    }

    private fun getEventInformation() {
        event = arguments?.getSerializable(Const.EVENT_UI) as Events
    }

    private fun handleCloseButton() {
        binding.header.iconCross.setOnClickListener {
            dismiss()
        }
    }

    private fun handleRulesButton() {
        binding.rules.layout.setOnClickListener {
            val intent = Intent(context, GroupRulesActivity::class.java)
            intent.putExtra(Const.RULES_TYPE,Const.RULES_EVENT)
            startActivityForResult(intent, 0)
        }
    }

    private fun handleEditEvent() {
        binding.edit.root.setOnClickListener {
            val intent = Intent(context, CreateEventActivity::class.java)
            intent.putExtra(Const.EVENT_UI, event)
            startActivityForResult(intent, 0)
            dismiss()
        }
    }

    private fun handleEditRecurrenceEvent() {
        binding.editRecurrence.root.setOnClickListener {
            val intent = Intent(context, EditRecurrenceActivity::class.java)
            intent.putExtras(
                bundleOf(
                    Const.EVENT_ID to event?.id,
                    Const.EVENT_DATE to event?.metadata?.startsAt,
                    Const.RECURRENCE to event?.recurrence
                )
            )
            startActivityForResult(intent, 0)
            dismiss()
        }
    }

    private fun hasEventBeenCanceled(canceled: Boolean) {
        if (canceled) {
            setFragmentResult(
                Const.REQUEST_KEY_SHOULD_REFRESH,
                bundleOf(Const.SHOULD_REFRESH to true)
            )
            RefreshController.shouldRefreshEventFragment = true
            dismiss()
        }
    }

    private fun updateView() {
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        with(binding) {
            rules.divider.visibility = View.GONE
            edit.divider.visibility = View.GONE
            editRecurrence.divider.visibility = View.GONE
            notificationNewMembers.divider.visibility = View.GONE
            TextViewCompat.setTextAppearance(
                notificationAll.tvLabel,
                R.style.left_courant_bold_black
            )
            event?.let {
                name.text = it.title
                membersNumberLocation.text = String.format(
                    getString(R.string.members_location),
                    it.membersCount,
                    it.metadata?.displayAddress
                )
                initializeInterests()
            }
        }
    }

    private fun initializeInterests() {
        if (interestsList.isEmpty()) binding.interests.visibility = View.GONE
        else {
            with(binding.interests) {
                visibility = View.VISIBLE
                layoutManager = FlexboxLayoutManager(context).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.CENTER
                }
                adapter = InterestsAdapter(interestsList)
            }
        }
    }

    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        event?.let {
            val eventInterests = it.interests
            tags?.interests?.forEach { interest ->
                if (eventInterests.contains(interest.id)) interest.name?.let { it ->
                    interestsList.add(
                        it
                    )
                }
            }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }

    private fun handleReportEvent() {
        val reportGroupBottomDialogFragment =
            event?.id?.let {
                ReportModalFragment.newInstance(
                    it,
                    Const.DEFAULT_VALUE, ReportTypes.REPORT_EVENT
                )
            }
        binding.report.setOnClickListener {
            reportGroupBottomDialogFragment?.show(parentFragmentManager, ReportModalFragment.TAG)
        }
    }

    private fun viewWithRole() {
        val eventWithNoRecurrence =
            event?.recurrence != null && event?.recurrence != Recurrence.NO_RECURRENCE.value

        with(binding) {
            if (EntourageApplication.me(context)?.id == event?.author?.userID) {
                if (event?.status == Status.OPEN) {
                    edit.root.visibility = View.VISIBLE
                    cancel.visibility = View.VISIBLE
                }
                editGroupDivider.visibility = View.VISIBLE
                editRecurrence.root.isVisible = eventWithNoRecurrence
                editRecurrenceDivider.isVisible = eventWithNoRecurrence
            }
            if (event?.member == true) {
//                notificationAll.root.visibility = View.VISIBLE
//                notificationNewMembers.root.visibility = View.VISIBLE
//                notificationNewEvent.root.visibility = View.VISIBLE
//                notificationNewMessages.root.visibility = View.VISIBLE
//                notifyMe.visibility = View.VISIBLE
//                notifyDivider.visibility = View.VISIBLE
                if (event?.status == Status.OPEN && event?.author?.userID != EntourageApplication.me(
                        activity
                    )?.id
                ) {
                    leave.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun handleCancelEvent() {
        binding.cancel.setOnClickListener {
            if (event?.recurrence == null) {
                CustomAlertDialog.show(
                    requireContext(),
                    getString(R.string.cancel_event),
                    getString(R.string.event_cancel_subtitle_pop),
                    getString(R.string.back)
                ) {
                    cancelEventWithoutRecurrence()
                }
            } else {
                showAlertDialogCancelEventWithRecurrence()

            }
        }
    }

    private fun handleLeaveEvent() {
        binding.leave.setOnClickListener {
            CustomAlertDialog.showWithCancelFirst(
                requireContext(),
                getString(R.string.leave_event),
                getString(R.string.leave_event_dialog_content),
                getString(R.string.exit),
            ) {
                event?.id?.let { id ->
                    eventPresenter.leaveEvent(id)
                }
            }
        }
    }

    private fun cancelEventWithoutRecurrence() {
        event?.id?.let { id ->
            eventPresenter.cancelEvent(id)
        }
    }

    private fun cancelEventWithRecurrence() {
        event?.id?.let {
            val editedEvent: MutableMap<String, Any> = mutableMapOf()
            val event: ArrayMap<String, Any> = ArrayMap()
            editedEvent["status"] = Status.CLOSED.value
            editedEvent["recurrency"] = Recurrence.NO_RECURRENCE.value
            event["outing"] = editedEvent
            eventPresenter.updateEvent(it, event)
        }
    }

    private fun showAlertDialogCancelEventWithRecurrence() {
        val layoutInflater = LayoutInflater.from(requireContext())
        val customDialog: View =
            layoutInflater.inflate(R.layout.new_custom_alert_dialog_cancel_event, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(customDialog)
        val alertDialog = builder.create()
        with(customDialog.findViewById<Button>(R.id.yes)) {
            text = getString(R.string.back)
            setOnClickListener {
                alertDialog.dismiss()
            }
        }
        customDialog.findViewById<TextView>(R.id.title).text =
            getString(R.string.event_cancel_recurrent_event)
        val cancelOneEvent = customDialog.findViewById<RadioButton>(R.id.one_event)
        val cancelAllEvents =
            customDialog.findViewById<RadioButton>(R.id.all_events_recurrent)
        customDialog.findViewById<Button>(R.id.yes).setOnClickListener {
            if (cancelOneEvent.isChecked) cancelEventWithoutRecurrence()
            if (cancelAllEvents.isChecked) cancelEventWithRecurrence()
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    companion object {
        const val TAG = "SettingsModalFragment"
        fun newInstance(event: Events): SettingsModalFragment {
            val fragment = SettingsModalFragment()
            val args = Bundle()
            args.putSerializable(Const.EVENT_UI, event)
            fragment.arguments = args
            return fragment
        }
    }
}