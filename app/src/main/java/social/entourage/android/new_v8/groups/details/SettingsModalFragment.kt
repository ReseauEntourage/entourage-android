package social.entourage.android.new_v8.groups.details

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.setFragmentResult
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentSettingsModalBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.groups.details.rules.GroupRulesActivity
import social.entourage.android.new_v8.groups.edit.EditGroupActivity
import social.entourage.android.new_v8.models.GroupUiModel
import social.entourage.android.new_v8.profile.myProfile.InterestsAdapter
import social.entourage.android.new_v8.report.ReportModalFragment
import social.entourage.android.new_v8.report.ReportTypes
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.tools.log.AnalyticsEvents

class SettingsModalFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentSettingsModalBinding? = null
    val binding: NewFragmentSettingsModalBinding get() = _binding!!
    private var group: GroupUiModel? = null
    private var interestsList: ArrayList<String> = ArrayList()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentSettingsModalBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_GROUP_OPTION_SHOW)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getGroupInformation()
        handleCloseButton()
        handleRulesButton()
        handleEditGroup()
        updateView()
        viewWithRole()
        handleReportGroup()
        handleLeaveGroup()
        handleReportGroup()
        groupPresenter.hasUserLeftGroup.observe(requireActivity(), ::hasUserLeftGroup)
    }


    private fun viewWithRole() {
        if (group?.admin == true) {
            binding.editGroup.root.visibility = View.VISIBLE
            binding.editGroupDivider.visibility = View.VISIBLE
            binding.leaveGroup.visibility = View.GONE
        }
        if (group?.member == true) {
            binding.leaveGroup.visibility = View.VISIBLE
            binding.notificationAll.root.visibility = View.VISIBLE
            binding.notificationNewMembers.root.visibility = View.VISIBLE
            binding.notificationNewEvent.root.visibility = View.VISIBLE
            binding.notificationNewMessages.root.visibility = View.VISIBLE
            binding.notifyMe.visibility = View.VISIBLE
            binding.notifyDivider.visibility = View.VISIBLE
        }
    }


    private fun updateView() {
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        binding.rules.divider.visibility = View.GONE
        binding.editGroup.divider.visibility = View.GONE
        binding.notificationNewMembers.divider.visibility = View.GONE
        TextViewCompat.setTextAppearance(
            binding.notificationAll.label,
            R.style.left_courant_bold_black
        )
        group?.let {
            binding.groupName.text = it.name
            binding.groupMembersNumberLocation.text = String.format(
                getString(R.string.members_location),
                it.members_count,
                it.address?.displayAddress
            )
            initializeInterests()
        }
    }

    private fun handleCloseButton() {
        binding.header.iconBack.setOnClickListener {
            dismiss()
        }
    }

    private fun handleRulesButton() {
        binding.rules.layout.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_OPTION_RULES)
            startActivity(Intent(context, GroupRulesActivity::class.java))
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
        group?.let {
            val groupInterests = it.interests
            tags?.interests?.forEach { interest ->
                if (groupInterests.contains(interest.id)) interest.name?.let { it ->
                    interestsList.add(
                        it
                    )
                }
            }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }

    private fun getGroupInformation() {
        group = arguments?.getParcelable(Const.GROUP_UI)
    }

    private fun handleEditGroup() {
        binding.editGroup.root.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_OPTION_EDIT_GROUP)
            val intent = Intent(context, EditGroupActivity::class.java)
            intent.putExtra(Const.GROUP_ID, group?.id)
            startActivity(intent)
            dismiss()
        }
    }

    private fun hasUserLeftGroup(hasLeft: Boolean) {
        if (hasLeft) {
            setFragmentResult(
                Const.REQUEST_KEY_SHOULD_REFRESH,
                bundleOf(Const.SHOULD_REFRESH to true)
            )
            dismiss()
        }
    }

    private fun handleReportGroup() {
        binding.reportGroup.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_OPTION_REPORT)
        }
    }


    private fun handleLeaveGroup() {
        binding.leaveGroup.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_OPTION_QUIT)
            Utils.showAlertDialogButtonClicked(
                requireView(),
                getString(R.string.leave_group),
                getString(R.string.leave_group_dialog_content),
                getString(R.string.exit),
            ) {
                group?.let {
                    it.id?.let { id -> groupPresenter.leaveGroup(id) }
                }
            }
        }
    }

    private fun handleReportGroup() {
        val reportGroupBottomDialogFragment =
            group?.id?.let { ReportModalFragment.newInstance(it,
                Const.DEFAULT_VALUE ,ReportTypes.REPORT_GROUP) }
        binding.reportGroup.setOnClickListener {
            reportGroupBottomDialogFragment?.show(parentFragmentManager, ReportModalFragment.TAG)
        }
    }

    companion object {
        const val TAG = "SettingsModalFragment"
        fun newInstance(group: GroupUiModel): SettingsModalFragment {
            val fragment = SettingsModalFragment()
            val args = Bundle()
            args.putParcelable(Const.GROUP_UI, group)
            fragment.arguments = args
            return fragment
        }
    }
}