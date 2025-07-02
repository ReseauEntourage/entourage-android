package social.entourage.android.groups.details

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
import social.entourage.android.BuildConfig
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentSettingsModalBinding
import social.entourage.android.groups.GroupModel
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.details.rules.GroupRulesActivity
import social.entourage.android.groups.edit.EditGroupActivity
import social.entourage.android.profile.myProfile.InterestsAdapter
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog

class GroupDetailsFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentSettingsModalBinding? = null
    val binding: NewFragmentSettingsModalBinding get() = _binding!!
    private var group: GroupModel? = null
    private var interestsList: ArrayList<String> = ArrayList()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentSettingsModalBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_GROUP_OPTION_SHOW
        )
        return binding.root
    }

    /*AnalyticsEvents.logEvent(AnalyticsEvents.GROUP_SHARED)
            val shareTitle = getString(R.string.share_title_group)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareTitle + "\n" + group?.name + ": " + "\n" + createShareUrl())
            }
            startActivity(Intent.createChooser(shareIntent, "Partager l'URL via"))*/

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
        groupPresenter.hasUserLeftGroup.observe(requireActivity(), ::hasUserLeftGroup)
        setView()
    }

    private fun setView() {
        binding.header.title = getString(R.string.group_settings)
        binding.notificationAll.label = getString(R.string.group_notification_all)
        binding.share.label = getString(R.string.group_share)
        binding.notificationNewEvent.label = getString(R.string.notification_new_event)
        binding.notificationNewMessages.label = getString(R.string.notification_new_messages)
        binding.notificationNewMembers.label = getString(R.string.notification_new_members)
        binding.edit.label = getString(R.string.edit_group_information)
        binding.rules.label = getString(R.string.rules_group)
        binding.report.text = getString(R.string.report_group)

        if (group?.admin == true) {
            binding.leave.visibility = View.GONE
        }

        binding.leave.text = getString(R.string.leave_group)
        handleShareButton()
    }

    private fun createShareUrl():String{
        val deepLinksHostName = BuildConfig.DEEP_LINKS_URL
        return "https://" + deepLinksHostName + "/app/neighborhoods/" + group?.uuid_v2
    }

    private fun handleShareButton(){
        binding.share.profileSettingsItemLayout.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GROUP_SHARE)
            val shareTitle = getString(R.string.share_title_group)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareTitle + "\n" + group?.name + ": " + "\n" + createShareUrl())
            }
            startActivity(Intent.createChooser(shareIntent, "Partager l'URL via"))
        }
    }




    private fun viewWithRole() {
        if (group?.admin == true) {
            binding.edit.root.visibility = View.VISIBLE
            binding.editGroupDivider.visibility = View.VISIBLE
            binding.leave.visibility = View.GONE
        }
        if (group?.member == true) {
            binding.leave.visibility = View.VISIBLE
            //TODO a remettre dans le futur pour les notifs
//            binding.notificationAll.root.visibility = View.VISIBLE
//            binding.notificationNewMembers.root.visibility = View.VISIBLE
//            binding.notificationNewEvent.root.visibility = View.VISIBLE
//            binding.notificationNewMessages.root.visibility = View.VISIBLE
//            binding.notifyMe.visibility = View.VISIBLE
//            binding.notifyDivider.visibility = View.VISIBLE
        }
    }

    private fun updateView() {
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        binding.rules.profileSettingsItemDivider.visibility = View.GONE
        binding.edit.profileSettingsItemDivider.visibility = View.GONE
        binding.notificationNewMembers.notificationsCardDivider.visibility = View.GONE
        TextViewCompat.setTextAppearance(
            binding.notificationAll.notificationsCardTvLabel,
            R.style.left_courant_bold_black
        )
        group?.let {
            binding.name.text = it.name
            binding.membersNumberLocation.text = String.format(
                getString(R.string.members_location),
                it.members_count,
                it.address
            )
            initializeInterests()
        }
    }

    private fun handleCloseButton() {
        binding.header.hbsIconCross.setOnClickListener {
            dismiss()
        }
    }

    private fun handleRulesButton() {
        binding.rules.profileSettingsItemLayout.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_OPTION_RULES
            )
            val intent = Intent(context, GroupRulesActivity::class.java)
            intent.putExtra(Const.RULES_TYPE, Const.RULES_GROUP)
            startActivityForResult(intent, 0)
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
        binding.edit.root.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_OPTION_EDIT_GROUP
            )
            val intent = Intent(context, EditGroupActivity::class.java)
            intent.putExtra(Const.GROUP_ID, group?.id)
            startActivityForResult(intent, 0)
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

    private fun handleLeaveGroup() {
        binding.leave.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_OPTION_QUIT
            )
            CustomAlertDialog.showWithCancelFirst(
                requireContext(),
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
            group?.id?.let {
                val fromLang = group?.descriptionTranslations?.fromLang
                if (fromLang != null) {
                    DataLanguageStock.updatePostLanguage(fromLang)
                }
                var description = group?.description ?: ""

                ReportModalFragment.newInstance(
                    it,
                    Const.DEFAULT_VALUE, ReportTypes.REPORT_GROUP, false, false,false, contentCopied = description)
            }
        binding.report.setOnClickListener {
            reportGroupBottomDialogFragment?.show(parentFragmentManager, ReportModalFragment.TAG)
        }
        AnalyticsEvents.logEvent(
            AnalyticsEvents.ACTION_GROUP_OPTION_REPORT
        )
    }

    companion object {
        const val TAG = "GroupDetailsFragment"
        fun newInstance(group: GroupModel): GroupDetailsFragment {
            val fragment = GroupDetailsFragment()
            val args = Bundle()
            args.putParcelable(Const.GROUP_UI, group)
            fragment.arguments = args
            return fragment
        }
    }

}