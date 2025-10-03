package social.entourage.android.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Events
import social.entourage.android.databinding.NewFragmentSettingsDiscussionModalBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.discussions.imageviewier.ImageListActivity
import social.entourage.android.discussions.members.MembersConversationFragment
import social.entourage.android.events.EventsPresenter
import social.entourage.android.events.create.CreateEventActivity
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.details.rules.GroupRulesActivity
import social.entourage.android.members.MembersActivity
import social.entourage.android.members.MembersType
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.small_talks.SmallTalkViewModel
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog

enum class SheetMode { GROUP, EVENT, DISCUSSION_ONE_TO_ONE, DISCUSSION_GROUP, MESSAGE_ACTIONS }

class ActionSheetFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentSettingsDiscussionModalBinding? = null
    private val binding get() = _binding!!

    private val discussionPresenter by lazy { DiscussionsPresenter() }
    private val eventsPresenter by lazy { EventsPresenter() }
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private lateinit var smallTalkViewModel: SmallTalkViewModel

    // Args
    private var mode: SheetMode = SheetMode.GROUP
    private var userId: Int = 0
    private var conversationId: Int = 0
    private var groupId: Int = 0
    private var eventId: Int = 0
    private var username: String? = null
    private var imBlocker = false
    private var canManageParticipants: Boolean = false
    private var eventTitle: String? = null
    private var eventParticipantsCount: Int = 0
    private var eventAddress: String? = null
    private var forceShowEdit: Boolean = false

    // Message actions
    private var messageId: Int = 0
    private var messageHtml: String? = null
    private var isMyMessage: Boolean = false
    private var isEventContext: Boolean = false
    private var isGroupContext: Boolean = false

    // Pour “Modifier l’événement”
    private var eventObj: Events? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mode = SheetMode.valueOf(getString(ARG_MODE)!!)
            userId = getInt(ARG_USER_ID)
            conversationId = getInt(ARG_CONV_ID)
            groupId = getInt(ARG_GROUP_ID)
            eventId = getInt(ARG_EVENT_ID)
            username = getString(ARG_USERNAME)
            imBlocker = getBoolean(ARG_BLOCKED, false)
            canManageParticipants = getBoolean(ARG_CAN_MANAGE_PARTICIPANTS, false)
            eventTitle = getString(ARG_EVENT_TITLE)
            eventParticipantsCount = getInt(ARG_EVENT_PARTICIPANTS, 0)
            eventAddress = getString(ARG_EVENT_ADDRESS)
            forceShowEdit = getBoolean(ARG_FORCE_SHOW_EDIT, false)

            messageId = getInt(ARG_MESSAGE_ID, 0)
            messageHtml = getString(ARG_MESSAGE_HTML)
            isMyMessage = getBoolean(ARG_IS_MY_MESSAGE, false)
            isEventContext = getBoolean(ARG_IS_EVENT_CONTEXT, false)
            isGroupContext = getBoolean(ARG_IS_GROUP_CONTEXT, false)

            @Suppress("DEPRECATION")
            if (containsKey(Const.EVENT_UI)) {
                (getSerializable(Const.EVENT_UI) as? Events)?.let { ev ->
                    eventObj = ev
                    eventId = ev.id ?: eventId
                    if (eventTitle.isNullOrBlank()) eventTitle = ev.title
                    if (eventParticipantsCount == 0) eventParticipantsCount = ev.membersCount ?: 0
                    if (eventAddress.isNullOrBlank()) eventAddress = ev.metadata?.displayAddress
                }
            }
        }
        smallTalkViewModel = SmallTalkViewModel(EntourageApplication.get())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = NewFragmentSettingsDiscussionModalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        setupClicks()
    }

    private fun configureUI() {
        binding.header.title = getString(R.string.discussion_settings_title)
        binding.header.iconBack?.isVisible = false
        binding.header.hbsIconCross.setOnClickListener { dismiss() }

        // Masquer l’item “edit” par défaut (on ne l’affichera que si eventObj != null ou forceShowEdit = true)
        binding.edit.profileSettingsItemLayout.isVisible = false

        when (mode) {
            SheetMode.DISCUSSION_ONE_TO_ONE -> {
                binding.profile.setLabel(getString(R.string.discussion_settings_profil))
                binding.profile.profileSettingsItemSubLabel.visibility = View.GONE
                binding.layoutBlock.isVisible = !imBlocker
                binding.block.text = getString(R.string.discussion_block_title)
                binding.blockSub.text = getString(R.string.discussion_block_subtitle, username)
                binding.quit.profileSettingsItemLayout.isVisible = false
                binding.eventInfo.isVisible = false
                binding.rules.profileSettingsItemLayout.isVisible = false
                binding.photos.profileSettingsItemLayout.isVisible = false
            }
            SheetMode.DISCUSSION_GROUP -> {
                binding.profile.setLabel(getString(R.string.discussion_settings_members))
                binding.profile.profileSettingsItemSubLabel.visibility = View.GONE
                binding.layoutBlock.isVisible = false
                binding.quit.profileSettingsItemLayout.isVisible = true
                binding.quit.setLabel(getString(R.string.discussion_settings_quit))
                binding.quit.profileSettingsItemLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                binding.eventInfo.isVisible = false
                binding.rules.profileSettingsItemLayout.isVisible = false
                binding.photos.profileSettingsItemLayout.isVisible = false
            }
            SheetMode.GROUP -> {
                binding.profile.setLabel(getString(R.string.discussion_settings_members))
                binding.profile.profileSettingsItemSubLabel.visibility = View.GONE
                binding.layoutBlock.isVisible = false
                binding.quit.profileSettingsItemLayout.isVisible = true
                binding.quit.setLabel(getString(R.string.leave_group))
                binding.quit.profileSettingsItemLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                binding.eventInfo.isVisible = false
                binding.rules.profileSettingsItemLayout.isVisible = false
                binding.photos.profileSettingsItemLayout.isVisible = false
            }
            SheetMode.EVENT -> {
                if (canManageParticipants) {
                    binding.profile.setLabel(getString(R.string.event_manage_participants_title))
                    binding.profile.setSubLabel(getString(R.string.event_manage_participants_subtitle))
                    binding.profile.profileSettingsItemSubLabel.visibility = View.VISIBLE
                } else {
                    binding.profile.setLabel(getString(R.string.see_members))
                    binding.profile.profileSettingsItemSubLabel.visibility = View.GONE
                }
                binding.layoutBlock.isVisible = false
                binding.quit.profileSettingsItemLayout.isVisible = true
                binding.quit.setLabel(getString(R.string.leave_event))
                binding.quit.profileSettingsItemLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                binding.eventInfo.isVisible = true

                binding.eventTitle.text = eventTitle.orEmpty()
                binding.eventTitle.isVisible = eventTitle?.isNotBlank() == true

                val count = eventParticipantsCount
                binding.eventParticipants.isVisible = count > 0
                if (count > 0) {
                    binding.eventParticipants.text = resources.getQuantityString(R.plurals.participants_count, count, count)
                }

                binding.eventAddress.text = eventAddress.orEmpty()
                binding.eventAddress.isVisible = eventAddress?.isNotBlank() == true

                binding.rules.profileSettingsItemLayout.isVisible = true
                binding.rules.setLabel(getString(R.string.event_params_cgu_title))
                binding.photos.profileSettingsItemLayout.isVisible = true
                binding.photos.setLabel("Voir les photos")

                // Afficher “Modifier l’événement” si on a l’objet complet OU si on force via flag
                if (eventObj != null || forceShowEdit) {
                    binding.edit.profileSettingsItemLayout.isVisible = true
                    binding.edit.setLabel(getString(R.string.edit_event_information))
                }
            }
            SheetMode.MESSAGE_ACTIONS -> {
                binding.header.title = "Actions du message"
                binding.profile.setLabel("Copier le texte")
                binding.profile.profileSettingsItemSubLabel.visibility = View.GONE
                binding.report.text = "Signaler le message"
                binding.layoutReport.isVisible = !isMyMessage
                binding.quit.profileSettingsItemLayout.isVisible = isMyMessage
                binding.quit.setLabel("Supprimer mon message")
                binding.quit.profileSettingsItemLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                binding.layoutBlock.isVisible = false
                binding.eventInfo.isVisible = false
                binding.rules.profileSettingsItemLayout.isVisible = false
                binding.photos.profileSettingsItemLayout.isVisible = false
                binding.edit.profileSettingsItemLayout.isVisible = false
            }
        }

        if (mode != SheetMode.MESSAGE_ACTIONS) {
            binding.report.text = getString(R.string.discussion_settings_signal)
        }
    }

    private fun setupClicks() {
        binding.profile.profileSettingsItemLayout.setOnClickListener {
            when (mode) {
                SheetMode.DISCUSSION_ONE_TO_ONE -> {
                    ProfileFullActivity.isMe = false
                    ProfileFullActivity.userId = userId.toString()
                    startActivity(Intent(requireContext(), ProfileFullActivity::class.java).putExtra(Const.USER_ID, userId))
                }
                SheetMode.EVENT -> {
                    MembersConversationFragment.isFromDiscussion = false
                    val isAnimator = EntourageApplication.get().me()?.roles?.isNotEmpty() == true
                    startActivity(
                        Intent(requireContext(), MembersActivity::class.java).apply {
                            putExtra("ID", eventId)
                            putExtra("TYPE", MembersType.EVENT.code)
                            putExtra("ROLE", isAnimator)
                        }
                    )
                    dismiss()
                }
                SheetMode.DISCUSSION_GROUP, SheetMode.GROUP -> {
                    MembersConversationFragment.isFromDiscussion = true
                    MembersConversationFragment.newInstance(conversationId).show(parentFragmentManager, "")
                }
                SheetMode.MESSAGE_ACTIONS -> {
                    val plain = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(messageHtml.orEmpty(), Html.FROM_HTML_MODE_LEGACY).toString()
                    } else {
                        @Suppress("DEPRECATION") Html.fromHtml(messageHtml.orEmpty()).toString()
                    }
                    val cm = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                            as android.content.ClipboardManager
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("message", plain))
                    dismiss()
                }
            }
        }

        binding.rules.profileSettingsItemLayout.setOnClickListener {
            startActivity(Intent(requireContext(), GroupRulesActivity::class.java).putExtra(Const.RULES_TYPE, Const.RULES_EVENT))
            dismiss()
        }

        // Edit: si eventObj != null → passe EVENT_UI; sinon fallback EVENT_ID (si ton Activity sait éditer depuis l’ID)
        binding.edit.profileSettingsItemLayout.setOnClickListener {
            eventObj?.let { ev ->
                startActivity(Intent(requireContext(), CreateEventActivity::class.java).putExtra(Const.EVENT_UI, ev))
                dismiss()
                return@setOnClickListener
            }
            startActivity(Intent(requireContext(), CreateEventActivity::class.java).putExtra(Const.EVENT_ID, eventId))
            dismiss()
        }

        binding.photos.profileSettingsItemLayout.setOnClickListener {
            if (mode == SheetMode.EVENT && conversationId > 0) {
                startActivity(Intent(requireContext(), ImageListActivity::class.java).putExtra("conversation_id", conversationId))
                dismiss()
            }
        }

        binding.layoutReport.setOnClickListener {
            when (mode) {
                SheetMode.GROUP -> {
                    ReportModalFragment.newInstance(
                        id = groupId, groupId = groupId, reportType = ReportTypes.REPORT_GROUP,
                        isFromMe = false, isConv = false, isOneToOne = false, contentCopied = "", openDirectSignal = true
                    ).show(parentFragmentManager, ReportModalFragment.TAG)
                }
                SheetMode.EVENT -> {
                    ReportModalFragment.newInstance(
                        id = eventId, groupId = eventId, reportType = ReportTypes.REPORT_EVENT,
                        isFromMe = false, isConv = false, isOneToOne = false, contentCopied = "", openDirectSignal = true
                    ).show(parentFragmentManager, ReportModalFragment.TAG)
                }
                SheetMode.DISCUSSION_ONE_TO_ONE, SheetMode.DISCUSSION_GROUP -> {
                    ReportModalFragment.newInstance(
                        id = conversationId, groupId = Const.DEFAULT_VALUE, reportType = ReportTypes.REPORT_CONVERSATION,
                        isFromMe = false, isConv = true, isOneToOne = (mode == SheetMode.DISCUSSION_ONE_TO_ONE),
                        contentCopied = "", openDirectSignal = true
                    ).show(parentFragmentManager, ReportModalFragment.TAG)
                }
                SheetMode.MESSAGE_ACTIONS -> {
                    val plain = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(messageHtml.orEmpty(), Html.FROM_HTML_MODE_LEGACY).toString()
                    } else {
                        @Suppress("DEPRECATION") Html.fromHtml(messageHtml.orEmpty()).toString()
                    }
                    val isConversationContext = !isEventContext && !isGroupContext
                    if (isConversationContext) {
                        val isSmallTalk = DetailConversationActivity.isSmallTalkMode
                        val convOrSmallTalkId = if (isSmallTalk) DetailConversationActivity.smallTalkId else conversationId
                        ReportModalFragment.newInstance(
                            id = convOrSmallTalkId, groupId = Const.DEFAULT_VALUE, reportType = ReportTypes.REPORT_CONVERSATION,
                            isFromMe = isMyMessage, isConv = true, isOneToOne = false, contentCopied = plain,
                            openDirectSignal = true, isSmallTalk = isSmallTalk
                        ).show(parentFragmentManager, ReportModalFragment.TAG)
                    } else {
                        val (containerId, rType) = when {
                            isEventContext -> eventId to ReportTypes.REPORT_POST_EVENT
                            isGroupContext -> groupId to ReportTypes.REPORT_POST
                            else -> 0 to ReportTypes.REPORT_POST
                        }
                        ReportModalFragment.newInstance(
                            id = messageId, groupId = containerId, reportType = rType,
                            isFromMe = isMyMessage, isConv = false, isOneToOne = false, contentCopied = plain,
                            openDirectSignal = true
                        ).show(parentFragmentManager, ReportModalFragment.TAG)
                    }
                }
            }
        }

        binding.quit.profileSettingsItemLayout.setOnClickListener {
            when (mode) {
                SheetMode.DISCUSSION_GROUP -> {
                    if (DetailConversationActivity.isSmallTalkMode) {
                        CustomAlertDialog.showWithCancelFirst(
                            requireContext(), getString(R.string.leave_conversation),
                            getString(R.string.leave_conversation_dialog_content), getString(R.string.exit)
                        ) { smallTalkViewModel.leaveSmallTalk(conversationId.toString()); dismiss() }
                    } else {
                        CustomAlertDialog.showWithCancelFirst(
                            requireContext(), getString(R.string.leave_conversation),
                            getString(R.string.leave_conversation_dialog_content), getString(R.string.exit)
                        ) { discussionPresenter.leaveConverstion(conversationId); dismiss() }
                    }
                }
                SheetMode.GROUP -> {
                    CustomAlertDialog.showWithCancelFirst(
                        requireContext(), getString(R.string.leave_group),
                        getString(R.string.leave_group_dialog_content), getString(R.string.exit)
                    ) { dismiss() }
                }
                SheetMode.EVENT -> {
                    CustomAlertDialog.showWithCancelFirst(
                        requireContext(), getString(R.string.leave_event),
                        getString(R.string.leave_event_dialog_content), getString(R.string.exit)
                    ) { eventsPresenter.leaveEvent(eventId); dismiss(); activity?.finish() }
                }
                SheetMode.MESSAGE_ACTIONS -> {
                    if (messageId == 0) return@setOnClickListener
                    when {
                        isEventContext && eventId != 0 -> eventsPresenter.deletedEventPost(eventId, messageId)
                        isGroupContext && groupId != 0 -> groupPresenter.deletedGroupPost(groupId, messageId)
                        else -> discussionPresenter.deleteMessage(conversationId, messageId)
                    }
                    (activity as? DetailConversationActivity)?.reloadView()
                    dismiss()
                }
                else -> Unit
            }
        }

        binding.layoutBlock.setOnClickListener {
            if (mode == SheetMode.DISCUSSION_ONE_TO_ONE) {
                val desc = getString(R.string.params_block_user_conv_pop_message, username)
                CustomAlertDialog.showButtonClickedWithCrossClose(
                    requireContext(),
                    getString(R.string.params_block_user_conv_pop_title),
                    desc,
                    getString(R.string.params_block_user_conv_pop_bt_cancel),
                    getString(R.string.params_block_user_conv_pop_bt_quit),
                    showCross = false,
                    onNo = {},
                    onYes = { discussionPresenter.blockUser(userId) }
                )
            }
        }
    }

    companion object {
        private const val ARG_MODE = "mode"
        private const val ARG_USER_ID = "userId"
        private const val ARG_CONV_ID = "convId"
        private const val ARG_GROUP_ID = "groupId"
        private const val ARG_EVENT_ID = "eventId"
        private const val ARG_USERNAME = "username"
        private const val ARG_BLOCKED = "blocked"
        private const val ARG_CAN_MANAGE_PARTICIPANTS = "canManageParticipants"
        private const val ARG_EVENT_TITLE = "eventTitle"
        private const val ARG_EVENT_PARTICIPANTS = "eventParticipants"
        private const val ARG_EVENT_ADDRESS = "eventAddress"
        private const val ARG_MESSAGE_ID = "messageId"
        private const val ARG_MESSAGE_HTML = "messageHtml"
        private const val ARG_IS_MY_MESSAGE = "isMyMessage"
        private const val ARG_IS_EVENT_CONTEXT = "isEventContext"
        private const val ARG_IS_GROUP_CONTEXT = "isGroupContext"
        private const val ARG_FORCE_SHOW_EDIT = "forceShowEdit"

        var isSignable = false

        fun newGroup(groupId: Int) = ActionSheetFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MODE, SheetMode.GROUP.name)
                putInt(ARG_GROUP_ID, groupId)
            }
        }

        fun newMessageActions(
            conversationId: Int,
            groupId: Int = 0,
            eventId: Int = 0,
            messageId: Int,
            messageHtml: String?,
            isMyMessage: Boolean,
            isEventContext: Boolean,
            isGroupContext: Boolean
        ) = ActionSheetFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MODE, SheetMode.MESSAGE_ACTIONS.name)
                putInt(ARG_CONV_ID, conversationId)
                putInt(ARG_GROUP_ID, groupId)
                putInt(ARG_EVENT_ID, eventId)
                putInt(ARG_MESSAGE_ID, messageId)
                putString(ARG_MESSAGE_HTML, messageHtml)
                putBoolean(ARG_IS_MY_MESSAGE, isMyMessage)
                putBoolean(ARG_IS_EVENT_CONTEXT, isEventContext)
                putBoolean(ARG_IS_GROUP_CONTEXT, isGroupContext)
            }
        }

        // 1) “ID only” — peut afficher Modifier via forceShowEdit (fallback EVENT_ID au clic)
        fun newEvent(
            eventId: Int,
            conversationId: Int,
            canManageParticipants: Boolean = false,
            eventTitle: String? = null,
            participantsCount: Int = 0,
            eventAddress: String? = null,
            forceShowEdit: Boolean = false
        ) = ActionSheetFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MODE, SheetMode.EVENT.name)
                putInt(ARG_EVENT_ID, eventId)
                putInt(ARG_CONV_ID, conversationId)
                putBoolean(ARG_CAN_MANAGE_PARTICIPANTS, canManageParticipants)
                putString(ARG_EVENT_TITLE, eventTitle)
                putInt(ARG_EVENT_PARTICIPANTS, participantsCount)
                putString(ARG_EVENT_ADDRESS, eventAddress)
                putBoolean(ARG_FORCE_SHOW_EDIT, forceShowEdit)
            }
        }

        // 2) “Full object” — bouton Modifier opérationnel (EVENT_UI)
        fun newEvent(
            event: Events,
            conversationId: Int,
            canManageParticipants: Boolean = false
        ) = ActionSheetFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MODE, SheetMode.EVENT.name)
                putInt(ARG_CONV_ID, conversationId)
                putBoolean(ARG_CAN_MANAGE_PARTICIPANTS, canManageParticipants)
                putString(ARG_EVENT_TITLE, event.title)
                putInt(ARG_EVENT_PARTICIPANTS, event.membersCount ?: 0)
                putString(ARG_EVENT_ADDRESS, event.metadata?.displayAddress)
                putInt(ARG_EVENT_ID, event.id ?: 0)
                putSerializable(Const.EVENT_UI, event)
            }
        }

        fun newDiscussion(
            conversationId: Int,
            isOneToOne: Boolean,
            userId: Int,
            username: String?,
            blocked: Boolean
        ) = ActionSheetFragment().apply {
            arguments = Bundle().apply {
                putString(
                    ARG_MODE,
                    if (isOneToOne) SheetMode.DISCUSSION_ONE_TO_ONE.name else SheetMode.DISCUSSION_GROUP.name
                )
                putInt(ARG_CONV_ID, conversationId)
                putInt(ARG_USER_ID, userId)
                putString(ARG_USERNAME, username)
                putBoolean(ARG_BLOCKED, blocked)
            }
        }
    }
}
