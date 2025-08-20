package social.entourage.android.events.details

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.setFragmentResult
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Status
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentSettingsModalBinding
import social.entourage.android.events.EventsPresenter
import social.entourage.android.events.create.CreateEventActivity
import social.entourage.android.events.create.Recurrence
import social.entourage.android.groups.details.rules.GroupRulesActivity
import social.entourage.android.profile.myProfile.InterestsAdapter
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog

class SettingsModalFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentSettingsModalBinding? = null
    val binding: NewFragmentSettingsModalBinding get() = _binding!!

    private var event: Events? = null
    private val interestsList = ArrayList<String>()
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentSettingsModalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getEventInformation()
        setView()
        updateView()
        viewWithRole()
        handleCloseButton()
        handleRulesButton()
        handleEditEvent()
        handleEditRecurrenceEvent()
        handleShareButton()
        handleReportEvent()
        handleCancelEvent()
        handleLeaveEvent()
        MetaDataRepository.metaData.observe(viewLifecycleOwner, ::handleMetaData)
        eventPresenter.eventCanceled.observe(viewLifecycleOwner, ::onEventChanged)
        eventPresenter.isEventUpdated.observe(viewLifecycleOwner, ::onEventChanged)
        eventPresenter.hasUserLeftEvent.observe(viewLifecycleOwner, ::handleLeftResponse)
    }

    private fun getEventInformation() {
        event = arguments?.getSerializable(Const.EVENT_UI) as? Events
    }

    private fun setView() {
        with(binding) {
            header.title = getString(R.string.event_settings)
            header.iconCross.setOnClickListener { dismiss() }

            notificationNewMessages.root.visibility = View.GONE

            notificationAll.label = getString(R.string.event_notification_all)
            notificationNewEvent.label = getString(R.string.notification_new_publications)
            notificationNewMembers.label = getString(R.string.notification_new_members)

            edit.label.text = getString(R.string.edit_event_information)
            rules.label.text = getString(R.string.rules_event)
            share.label.text = getString(R.string.share_event_settings)
            editRecurrence.label.text = getString(R.string.modify_recurrence)

            report.text = getString(R.string.report_event)
            cancel.text = getString(R.string.cancel_event)
            leave.text = getString(R.string.leave_event)

            TextViewCompat.setTextAppearance(notificationAll.tvLabel, R.style.left_courant_bold_black)

            val me = EntourageApplication.me(requireContext())
            if (me?.roles?.isEmpty() == true) {
                editRecurrence.root.isVisible = false
            }
        }
    }

    private fun updateView() {
        with(binding) {
            rules.divider.visibility = View.GONE
            edit.divider.visibility = View.GONE
            editRecurrence.divider.visibility = View.GONE
            notificationNewMembers.divider.visibility = View.GONE

            event?.let { e ->
                name.text = e.title
                membersNumberLocation.text = getString(
                    R.string.members_location,
                    e.membersCount,
                    e.metadata?.displayAddress
                )
                initializeInterests()
            }
        }
    }

    private fun initializeInterests() {
        with(binding) {
            if (interestsList.isEmpty()) {
                interests.visibility = View.GONE
            } else {
                interests.visibility = View.VISIBLE
                interests.layoutManager = FlexboxLayoutManager(context).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.CENTER
                }
                interests.adapter = InterestsAdapter(interestsList)
            }
        }
    }

    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        val eventInterests = event?.interests.orEmpty()
        tags?.interests?.forEach { tag ->
            if (eventInterests.contains(tag.id)) {
                tag.name?.let { interestsList.add(it) }
            }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }

    private fun handleCloseButton() {
        binding.header.iconCross.setOnClickListener { dismiss() }
    }

    private fun handleRulesButton() {
        binding.rules.layout.setOnClickListener {
            val intent = Intent(context, GroupRulesActivity::class.java)
            intent.putExtra(Const.RULES_TYPE, Const.RULES_EVENT)
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
            val intent = Intent(context, social.entourage.android.events.EditRecurrenceActivity::class.java).apply {
                putExtras(
                    bundleOf(
                        Const.EVENT_ID to event?.id,
                        Const.EVENT_DATE to event?.metadata?.startsAt,
                        Const.RECURRENCE to event?.recurrence
                    )
                )
            }
            startActivityForResult(intent, 0)
            dismiss()
        }
    }

    private fun handleShareButton() {
        binding.share.layout.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_OPTION_SHARED)
            event?.let { e ->
                val shareTitle = getString(R.string.share_title_event)
                val url = "https://${BuildConfig.DEEP_LINKS_URL}/app/outings/${e.uuid_v2}"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "$shareTitle\n${e.title}:\n$url")
                }
                startActivity(Intent.createChooser(shareIntent, getString(R.string.about_share_app)))
            }
        }
    }

    private fun handleReportEvent() {
        val modal = event?.id?.let { eventId ->
            event?.descriptionTranslations?.fromLang?.let { DataLanguageStock.updatePostLanguage(it) }
            val description = event?.description.orEmpty()
            ReportModalFragment.newInstance(
                eventId,
                Const.DEFAULT_VALUE,
                ReportTypes.REPORT_EVENT,
                false,
                false,
                false,
                contentCopied = description
            )
        }
        binding.report.setOnClickListener {
            AnalyticsEvents.logEvent("Action_EventOption_Report")
            modal?.show(parentFragmentManager, ReportModalFragment.TAG)
        }
    }

    private fun viewWithRole() {
        val hasRecurrence = event?.recurrence != null && event?.recurrence != Recurrence.NO_RECURRENCE.value
        with(binding) {
            val meId = EntourageApplication.me(context)?.id
            val isAuthor = meId == event?.author?.userID

            if (isAuthor) {
                if (event?.status == Status.OPEN) {
                    edit.root.visibility = View.VISIBLE
                    cancel.visibility = View.VISIBLE
                }
                editGroupDivider.visibility = View.VISIBLE
                editRecurrence.root.isVisible = hasRecurrence
                editRecurrenceDivider.isVisible = hasRecurrence
            }

            if (event?.member == true) {
                if (event?.status == Status.OPEN && !isAuthor) {
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
                    getString(R.string.cancel_event_continue)
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
                getString(R.string.exit)
            ) {
                event?.id?.let { id -> eventPresenter.leaveEvent(id) }
            }
        }
    }

    private fun cancelEventWithoutRecurrence() {
        event?.id?.let { id -> eventPresenter.cancelEvent(id) }
    }

    private fun cancelEventWithRecurrence() {
//        event?.id?.let { id ->
//            val editedEvent = hashMapOf<String, Any>(
//                "status" to Status.CLOSED.value,
//                "recurrency" to Recurrence.NO_RECURRENCE.value
//            )
//            val body = hashMapOf<String, Any>("outing" to editedEvent)
//            eventPresenter.updateEvent(id, body)
//        }
    }

    private fun showAlertDialogCancelEventWithRecurrence() {
        val custom = LayoutInflater.from(requireContext())
            .inflate(R.layout.new_custom_alert_dialog_cancel_event, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(custom).create()

        val btnYes = custom.findViewById<Button>(R.id.yes)
        val radioGroup = custom.findViewById<RadioGroup>(R.id.recurrence)
        val cancelOneEvent = custom.findViewById<RadioButton>(R.id.one_event)
        val cancelAllEvents = custom.findViewById<RadioButton>(R.id.all_events_recurrent)

        btnYes.isEnabled = false
        btnYes.background = requireContext().getDrawable(R.drawable.btn_shape_light_orange)

        radioGroup.setOnCheckedChangeListener { group, _ ->
            btnYes.isEnabled = group.checkedRadioButtonId != -1
            if (btnYes.isEnabled) {
                btnYes.background = requireContext().getDrawable(R.drawable.btn_shape_orange_alert_dialog)
            }
        }

        btnYes.text = getString(R.string.cancel_event)
        btnYes.setOnClickListener { dialog.dismiss() }

        custom.findViewById<ImageButton>(R.id.btn_cross).apply {
            visibility = View.VISIBLE
            setOnClickListener { dialog.dismiss() }
        }

        custom.findViewById<TextView>(R.id.title).text = getString(R.string.event_cancel_recurrent_event)

        custom.findViewById<Button>(R.id.yes).setOnClickListener {
            if (cancelOneEvent.isChecked) cancelEventWithoutRecurrence()
            if (cancelAllEvents.isChecked) cancelEventWithRecurrence()
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun onEventChanged(done: Boolean) {
        if (done) {
            setFragmentResult(Const.REQUEST_KEY_SHOULD_REFRESH, bundleOf(Const.SHOULD_REFRESH to true))
            RefreshController.shouldRefreshEventFragment = true
            dismiss()
        }
    }

    private fun handleLeftResponse(left: Boolean) {
        if (left) {
            setFragmentResult(Const.REQUEST_KEY_SHOULD_REFRESH, bundleOf(Const.SHOULD_REFRESH to true))
            dismiss()
            activity?.finish()
        }
    }

    companion object {
        const val TAG = "GroupDetailsFragment"
        fun newInstance(event: Events): SettingsModalFragment {
            return SettingsModalFragment().apply {
                arguments = Bundle().apply { putSerializable(Const.EVENT_UI, event) }
            }
        }
    }
}
