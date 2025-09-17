package social.entourage.android.discussions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.api.model.Conversation
import social.entourage.android.databinding.NewFragmentSettingsDiscussionModalBinding
import social.entourage.android.discussions.members.MembersConversationFragment
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.small_talks.SmallTalkViewModel
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog

class SettingsDiscussionModalFragment : BottomSheetDialogFragment() {

    /* ──────────────── viewBinding ──────────────── */
    private var _binding: NewFragmentSettingsDiscussionModalBinding? = null
    private val binding get() = _binding!!

    /* ──────────────── ViewModels ──────────────── */
    private val discussionPresenter by lazy { DiscussionsPresenter() }
    private val smallTalkViewModel: SmallTalkViewModel by viewModels()

    /* ──────────────── Arguments ──────────────── */
    private var isOneToOne      = true
    private var userId: Int?    = null
    private var conversationId: Int? = null
    private var username: String? = null
    private var imBlocker       = false
    private var isCreator       = false      // (pour Discussions classiques uniquement)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            isOneToOne    = getBoolean(ARG_121, false)
            userId        = getInt(ARG_USERID)
            conversationId= getInt(ARG_CONVID)
            username      = getString(ARG_NAME)
            imBlocker     = getBoolean(ARG_BLOCKED, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentSettingsDiscussionModalBinding.inflate(inflater, container, false)
        return binding.root
    }

    /* ───────────────────────────── onViewCreated ───────────────────────────── */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* ---------- Observers et appels réseau selon le mode ---------- */
        if (isSmallTalk) {                                     // <── COMPANION VARIABLE
            // Pas de DiscussionsPresenter : uniquement SmallTalkViewModel
            smallTalkViewModel.smallTalkDetail.observe(this)   { updateInputs() }
            DetailConversationActivity.smallTalkId
                .takeIf { it.isNotBlank() }
                ?.let { smallTalkViewModel.getSmallTalk(it) }
        } else {
            discussionPresenter.detailConversation.observe(this, ::updateConversation)
            discussionPresenter.hasUserLeftConversation.observe(this, ::updateLeaveConversation)
            discussionPresenter.hasBlockUser.observe(this, ::showPopValidateBlockUser)
            conversationId?.let { discussionPresenter.getDetailConversation(it) }
        }

        setView()
        handleCloseButton()
        handleButtons()
    }

    /* ──────────────────────────  UI helpers  ────────────────────────── */

    private fun setView() {
        updateInputs()
        binding.header.title = getString(R.string.discussion_settings_title)

        if (isOneToOne) {                       // 1–1
            binding.profile.profileSettingsItemLabel.text    = getString(R.string.discussion_settings_profil)
            binding.layoutBlock.isVisible = !imBlocker
            binding.block.text       = getString(R.string.discussion_block_title)
            binding.blockSub.text    = getString(R.string.discussion_block_subtitle, username)
        } else {                               // groupe
            binding.profile.profileSettingsItemLabel.text = getString(R.string.discussion_settings_members)
            binding.layoutBlock.isVisible = false
        }

        binding.report.text = getString(R.string.discussion_settings_signal)

        // cas « plusieurs personnes » (flag global utilisé ailleurs)
        if (isSeveralPersonneInConversation) binding.layoutBlock.isVisible = false
        // Dans un SmallTalk on ne propose pas encore le signalement ni le blocage
        if (isSmallTalk) {
            binding.layoutReport.isVisible = false
            binding.layoutBlock.isVisible  = false
        }
    }

    private fun updateInputs() {
        val mustHideQuit = isCreator || isOneToOne || isEvent
        binding.quit.profileSettingsItemLayout.isVisible = !mustHideQuit
        binding.quit.profileSettingsItemArrow.isVisible  = !mustHideQuit
        if (!mustHideQuit) binding.quit.profileSettingsItemLabel.text = getString(R.string.discussion_settings_quit)
    }

    /* ─────────────────────────── Observers ─────────────────────────── */

    private fun updateLeaveConversation(hasLeft: Boolean) {
        if (hasLeft) {
            RefreshController.shouldRefreshFragment = true
            (activity as? DetailConversationActivity)?.finish()
            dismiss()
        }
    }

    private fun updateConversation(detailConversation: Conversation?) {
        detailConversation?.let { isCreator = it.isCreator ?: false }
        updateInputs()
    }

    /* ─────────────────────────── Listeners ─────────────────────────── */

    private fun handleCloseButton() {
        binding.header.hbsIconCross.setOnClickListener {
            dismiss()
            isSeveralPersonneInConversation = false
        }
    }

    private fun handleButtons() {

        /* ▶️ Profil (1-1) ou liste des membres (groupe) */
        binding.profile.profileSettingsItemLayout.setOnClickListener {
            if (isOneToOne) {
                ProfileFullActivity.isMe = false
                ProfileFullActivity.userId = userId.toString()
                startActivity(
                    Intent(requireContext(), ProfileFullActivity::class.java)
                        .putExtra(Const.USER_ID, userId)
                )
            } else {
                if (isSmallTalk) {
                    MembersConversationFragment
                        .newInstance(DetailConversationActivity.smallTalkId.toInt())
                        .show(childFragmentManager, "")
                } else {
                    MembersConversationFragment
                        .newInstance(conversationId)
                        .show(childFragmentManager, "")
                }
            }
        }

        /* ▶️ Signalement (DISCUSSONS UNIQUEMENT) */
        binding.layoutReport.setOnClickListener {
            if (!isSmallTalk) {
                conversationId?.let { convId ->
                    val meId = EntourageApplication.get().me()?.id
                    ReportModalFragment.newInstance(
                        convId,
                        Const.DEFAULT_VALUE,
                        ReportTypes.REPORT_CONVERSATION,
                        meId == userId,
                        false,
                        false,
                        contentCopied = ""
                    ).show(parentFragmentManager, ReportModalFragment.TAG)
                }
            }
        }

        /* ▶️ Quitter la conversation */
        binding.quit.profileSettingsItemLayout.setOnClickListener {
            CustomAlertDialog.showWithCancelFirst(
                requireContext(),
                getString(R.string.leave_conversation),
                getString(R.string.leave_conversation_dialog_content),
                getString(R.string.exit)
            ) {
                if (isSmallTalk) {
                    smallTalkViewModel.leaveSmallTalk(DetailConversationActivity.smallTalkId)
                    updateLeaveConversation(true)               // fermeture immédiate
                } else {
                    conversationId?.let { discussionPresenter.leaveConverstion(it) }
                }
            }
        }

        /* ▶️ Bloquer l’utilisateur (discussions privées uniquement) */
        binding.layoutBlock.setOnClickListener {
            if (isSmallTalk || isOneToOne.not()) return@setOnClickListener
            val desc = getString(
                R.string.params_block_user_conv_pop_message,
                username
            )
            CustomAlertDialog.showButtonClickedWithCrossClose(
                requireContext(),
                getString(R.string.params_block_user_conv_pop_title),
                desc,
                getString(R.string.params_block_user_conv_pop_bt_cancel),
                getString(R.string.params_block_user_conv_pop_bt_quit),
                showCross = false,
                onNo = {},
                onYes = { userId?.let { discussionPresenter.blockUser(it) } }
            )
        }
    }

    private fun showPopValidateBlockUser(isBlocked: Boolean) {
        val title = getString(
            R.string.params_block_user_conv_pop_validate_title,
            username
        )
        CustomAlertDialog.showOnlyOneButton(
            requireContext(),
            title,
            getString(R.string.params_block_user_conv_pop_validate_subtitle),
            getString(R.string.params_block_user_conv_pop_validate_bt)
        ) {
            (context as? DetailConversationActivity)?.updateDiscussion()
            dismiss()
        }
    }

    /* ───────────────────────── Companion ───────────────────────── */

    companion object {
        private const val ARG_121    = "oneToOne"
        private const val ARG_USERID = "userid"
        private const val ARG_CONVID = "conversationid"
        private const val ARG_NAME   = "username"
        private const val ARG_BLOCKED= "imBlocker"

        /* Variables « globales » déjà présentes : */
        const val TAG = "SettingsDiscussionModalFragment"
        var isSeveralPersonneInConversation = false
        var isEvent   = false
        var isSmallTalk = false          // ← Flag défini depuis l’extérieur avant l’affichage

        /** Signature inchangée – vous continuez d’appeler cette méthode comme avant. */
        fun newInstance(
            userId: Int?,
            conversationId: Int?,
            isOneToOne: Boolean,
            username: String?,
            imBlocker: Boolean? = null
        ): SettingsDiscussionModalFragment {
            val fragment = SettingsDiscussionModalFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(ARG_121 , isOneToOne)
                putInt   (ARG_USERID, userId ?: 0)
                putInt   (ARG_CONVID, conversationId ?: 0)
                putString(ARG_NAME  , username)
                putBoolean(ARG_BLOCKED, imBlocker ?: false)
            }
            return fragment
        }
    }
}
