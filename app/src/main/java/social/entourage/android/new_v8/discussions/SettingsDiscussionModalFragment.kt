package social.entourage.android.new_v8.discussions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentSettingsDiscussionModalBinding
import social.entourage.android.new_v8.RefreshController
import social.entourage.android.new_v8.discussions.members.MembersConversationFragment
import social.entourage.android.new_v8.models.Conversation
import social.entourage.android.new_v8.report.ReportModalFragment
import social.entourage.android.new_v8.report.ReportTypes
import social.entourage.android.new_v8.user.UserProfileActivity
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils

class SettingsDiscussionModalFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentSettingsDiscussionModalBinding? = null
    val binding: NewFragmentSettingsDiscussionModalBinding get() = _binding!!

    private val discussionPresenter : DiscussionsPresenter by lazy { DiscussionsPresenter() }

    var isOneToOne = true
    var userId:Int? = null
    var conversationId:Int? = null
    var isCreator = false
    var username:String? = null
    var imBlocker = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            isOneToOne = it.getBoolean(ARG_121, false)
            userId = it.getInt(ARG_USERID)
            conversationId = it.getInt(ARG_CONVID)
            username = it.getString(ARG_NAME)
            imBlocker = it.getBoolean(ARG_BLOCKED,false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentSettingsDiscussionModalBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleCloseButton()
        handleButtons()
        setView()

        if (!isOneToOne && conversationId != null) {
            discussionPresenter.detailConversation.observe(this,::updateConversation)
            discussionPresenter.getDetailConversation(conversationId!!)
        }

        discussionPresenter.hasUserLeftConversation.observe(this,::updateLeaveConversation)
        discussionPresenter.hasBlockUser.observe(this,::showPopValidateBlockUser)
    }

    private fun updateLeaveConversation(hasLeft:Boolean) {
        if (hasLeft) {
            RefreshController.shouldRefreshFragment = true
            (activity as? DetailConversationActivity)?.finish()
            dismiss()
        }
    }

    private fun updateConversation(detailConversation: Conversation?) {
        detailConversation?.let {
            isCreator = it.isCreator ?: false
        }
        updateInputs()
    }

    private fun setView() {

        updateInputs()

        binding.header.title = getString(R.string.discussion_settings_title)
        if (isOneToOne) {
            binding.profile.label = getString(R.string.discussion_settings_profil) //discussion_settings_members

            binding.layoutBlock.isVisible = !imBlocker
            binding.block.text = getString(R.string.discussion_block_title)
            binding.blockSub.text = String.format(getString(R.string.discussion_block_subtitle),username)
        }
        else {
            binding.layoutBlock.isVisible = false
            binding.profile.label = getString(R.string.discussion_settings_members)
        }
        binding.report.text = getString(R.string.discussion_settings_signal)
    }

    private fun updateInputs() {
        if (isOneToOne || isCreator) {
            binding.quit.layout.isVisible = false
        }
        else {
            binding.quit.layout.isVisible = true
            binding.quit.arrow.isVisible = false
            binding.quit.label = getString(R.string.discussion_settings_quit)
        }
    }

    private fun handleCloseButton() {
        binding.header.iconCross.setOnClickListener {
            dismiss()
        }
    }

    private fun handleButtons() {
        binding.profile.layout.setOnClickListener {
            if (isOneToOne) {
                startActivity(
                    Intent(requireContext(), UserProfileActivity::class.java).putExtra(
                        Const.USER_ID, userId
                    ))
            }
            else {
                MembersConversationFragment.newInstance(conversationId).show(childFragmentManager,"")
            }
        }

        binding.layoutReport.setOnClickListener {
            conversationId?.let {
                val reportGroupBottomDialogFragment = ReportModalFragment.newInstance(it,Const.DEFAULT_VALUE, ReportTypes.REPORT_CONVERSATION)
                reportGroupBottomDialogFragment.show(parentFragmentManager,ReportModalFragment.TAG)
            }
        }

        binding.quit.layout.setOnClickListener {
            Utils.showAlertDialogButtonClicked(
                requireContext(),
                getString(R.string.leave_conversation),
                getString(R.string.leave_conversation_dialog_content),
                getString(R.string.exit),
            ) {
                conversationId?.let {
                    discussionPresenter.leaveConverstion(it)
                }
            }
        }

        binding.layoutBlock.setOnClickListener {
            val desc = String.format(getString(R.string.params_block_user_conv_pop_message,username))
            Utils.showAlertDialogButtonClickedWithCrossClose(
                requireContext(),
                getString(R.string.params_block_user_conv_pop_title),
                desc,
                getString(R.string.params_block_user_conv_pop_bt_cancel),
                getString(R.string.params_block_user_conv_pop_bt_quit), showCross = false, onNo = {}, onYes = {
                    //TODO: la suite
                    userId?.let {
                        discussionPresenter.blockUser(it)
                    }
                }
            )
        }
    }


    private fun showPopValidateBlockUser(isBlocked:Boolean) {
        val title = String.format(getString(R.string.params_block_user_conv_pop_validate_title,username))
        Utils.showAlertDialogButtonClicked(
            requireContext(),
            title,
            getString(R.string.params_block_user_conv_pop_validate_subtitle),
            getString(R.string.params_block_user_conv_pop_validate_bt),
            {
                (context as? DetailConversationActivity)?.updateDiscussion()
                dismiss()
            },null
        )
    }


    companion object {
        private val ARG_121 = "oneToOne"
        private val ARG_USERID = "userid"
        private val ARG_CONVID = "conversationid"
        private val ARG_NAME = "username"
        private val ARG_BLOCKED = "imBlocker"
        const val TAG = "SettingsDiscussionModalFragment"
        fun newInstance(userId:Int?,conversationId:Int?,isOneToOne:Boolean, username:String?,imBlocker:Boolean? = null): SettingsDiscussionModalFragment {
            val fragment = SettingsDiscussionModalFragment()
            val args = Bundle()
            args.putBoolean(ARG_121,isOneToOne)
            args.putInt(ARG_USERID,userId ?: 0)
            args.putInt(ARG_CONVID,conversationId ?: 0)
            args.putString(ARG_NAME,username)
            args.putBoolean(ARG_BLOCKED,imBlocker ?: false)
            fragment.arguments = args
            return fragment
        }
    }
}