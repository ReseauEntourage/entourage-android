package social.entourage.android.new_v8.events.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentSettingsModalBinding
import social.entourage.android.new_v8.events.EventsPresenter
import social.entourage.android.new_v8.models.SettingUiModel
import social.entourage.android.new_v8.profile.myProfile.InterestsAdapter
import social.entourage.android.new_v8.report.ReportModalFragment
import social.entourage.android.new_v8.report.ReportTypes
import social.entourage.android.new_v8.utils.Const


class SettingsModalFragment : BottomSheetDialogFragment() {


    private var _binding: NewFragmentSettingsModalBinding? = null
    val binding: NewFragmentSettingsModalBinding get() = _binding!!

    private var event: SettingUiModel? = null
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
        handleLeaveEvent()
        // eventPresenter.hasUserLeftEvent.observe(requireActivity(), ::hasUserLeftEvent)
        setView()
    }


    private fun setView() {
        binding.notificationNewMessages.root.visibility = View.GONE
        binding.header.title = getString(R.string.event_settings)
        binding.notificationAll.label = getString(R.string.event_notification_all)
        binding.notificationNewEvent.label = getString(R.string.notification_new_publications)
        binding.notificationNewMembers.label = getString(R.string.notification_new_members)
        binding.edit.label = getString(R.string.edit_event_information)
        binding.rules.label = getString(R.string.rules_event)
        binding.report.text = getString(R.string.report_event)
        binding.leave.text = getString(R.string.cancel_event)
        binding.editRecurrence.label = getString(R.string.modify_recurrence)
    }


    private fun getEventInformation() {
        event = arguments?.getParcelable(Const.EVENT_UI)
    }

    private fun handleCloseButton() {
        binding.header.iconBack.setOnClickListener {
            dismiss()
        }
    }

    private fun handleRulesButton() {
        binding.rules.layout.setOnClickListener {

        }
    }

    private fun handleEditEvent() {

    }

    private fun hasUserLeftEvent(hasLeft: Boolean) {

    }

    private fun updateView() {
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        binding.rules.divider.visibility = View.GONE
        binding.edit.divider.visibility = View.GONE
        binding.editRecurrence.divider.visibility = View.GONE
        binding.notificationNewMembers.divider.visibility = View.GONE
        TextViewCompat.setTextAppearance(
            binding.notificationAll.tvLabel,
            R.style.left_courant_bold_black
        )
        event?.let {
            binding.name.text = it.name
            binding.membersNumberLocation.text = String.format(
                getString(R.string.members_location),
                it.members_count,
                it.address
            )
            initializeInterests()
        }
    }

    private fun initializeInterests() {
        if (interestsList.isEmpty()) binding.interests.visibility = View.GONE
        else {
            binding.interests.visibility = View.VISIBLE
            binding.interests.apply {
                val layoutManagerFlex = FlexboxLayoutManager(context)
                layoutManagerFlex.flexDirection = FlexDirection.ROW
                layoutManagerFlex.justifyContent = JustifyContent.CENTER
                layoutManager = layoutManagerFlex
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
        if (event?.admin == true) {
            binding.edit.root.visibility = View.VISIBLE
            binding.editGroupDivider.visibility = View.VISIBLE
            binding.editRecurrence.root.visibility = View.VISIBLE
            binding.editRecurrenceDivider.visibility = View.VISIBLE
            binding.leave.visibility = View.GONE
        }
        if (event?.member == true) {
            binding.leave.visibility = View.VISIBLE
            binding.notificationAll.root.visibility = View.VISIBLE
            binding.notificationNewMembers.root.visibility = View.VISIBLE
            binding.notificationNewEvent.root.visibility = View.VISIBLE
            binding.notificationNewMessages.root.visibility = View.VISIBLE
            binding.notifyMe.visibility = View.VISIBLE
            binding.notifyDivider.visibility = View.VISIBLE
        }
    }

    private fun handleLeaveEvent() {
        binding.leave.setOnClickListener {

        }
    }

    companion object {
        const val TAG = "SettingsModalFragment"
        fun newInstance(event: SettingUiModel): SettingsModalFragment {
            val fragment = SettingsModalFragment()
            val args = Bundle()
            args.putParcelable(Const.EVENT_UI, event)
            fragment.arguments = args
            return fragment
        }
    }
}