package social.entourage.android.report

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.actions.ActionsPresenter
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.TagMetaData
import social.entourage.android.api.model.Tags
import social.entourage.android.databinding.NewFragmentReportBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.events.EventsPresenter
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.details.feed.CallbackReportFragment
import social.entourage.android.small_talks.SmallTalkViewModel
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.user.UserPresenter

enum class ReportTypes(val code: Int) {
    REPORT_USER(0),
    REPORT_GROUP(1),
    REPORT_POST(2),
    REPORT_COMMENT(3),
    REPORT_EVENT(4),
    REPORT_CONTRIB(5),
    REPORT_DEMAND(6),
    REPORT_CONVERSATION(7),
    REPORT_POST_EVENT(8)
}

class ReportModalFragment : BottomSheetDialogFragment() {

    private var signalList: MutableList<TagMetaData> = ArrayList()
    private var _binding: NewFragmentReportBinding? = null
    val binding: NewFragmentReportBinding get() = _binding!!

    private var selectedSignalsIdList: MutableList<String> = mutableListOf()

    private val userPresenter: UserPresenter by lazy { UserPresenter() }
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
    private val actionPresenter: ActionsPresenter by lazy { ActionsPresenter() }
    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
    private val smallTalkViewModel: SmallTalkViewModel by viewModels()

    private var reportedId: Int? = Const.DEFAULT_VALUE
    private var groupId: Int? = Const.DEFAULT_VALUE
    private var reportType: Int? = Const.DEFAULT_VALUE

    private var title: String = ""
    private var isEventComment = false
    private var isGroupComment = false
    private var callback: CallbackReportFragment? = null
    private var isFromMe: Boolean? = false
    private var isMyLanguage: Boolean? = false
    private var isNotTranslatable: Boolean? = false
    private var isFromConv: Boolean? = false
    private var isOneToOne: Boolean? = false
    private var isSmallTalk: Boolean = false
    private var dismissCallback: onDissmissFragment? = null
    private var contentCopied: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeInterests()
        readArgs()
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)

        // Observers
        userPresenter.isUserReported.observe(requireActivity(), ::handleReportResponse)
        groupPresenter.isGroupReported.observe(requireActivity(), ::handleReportResponse)
        groupPresenter.isPostReported.observe(requireActivity(), ::handleReportResponse)
        eventPresenter.isEventReported.observe(requireActivity(), ::handleReportResponse)
        eventPresenter.isEventPostReported.observe(requireActivity(), ::handleReportResponse)
        actionPresenter.isActionReported.observe(requireActivity(), ::handleReportResponse)

        discussionsPresenter.isConversationReported.observe(requireActivity(), ::handleReportResponse)
        discussionsPresenter.isConversationDeleted.observe(requireActivity(), ::handleDeletedResponse)
        discussionsPresenter.isMessageDeleted.observe(requireActivity(), ::handleDeletedResponse)
        smallTalkViewModel.messageDeleteResult.observe(requireActivity(), ::handleDeletedResponse)

        setupViewStep1()
        handleCloseButton()
        setStartView()

        // Drapeaux d’aide UI
        when (reportType) {
            ReportTypes.REPORT_POST_EVENT.code -> isEventComment = true
            ReportTypes.REPORT_POST.code -> isGroupComment = true
        }

        // Ouvre directement l’étape de signalement si demandé
        if (arguments?.getBoolean(ARG_OPEN_DIRECT_SIGNAL, false) == true ||
            reportType == ReportTypes.REPORT_USER.code ||
            reportType == ReportTypes.REPORT_GROUP.code ||
            reportType == ReportTypes.REPORT_EVENT.code ||
            reportType == ReportTypes.REPORT_CONVERSATION.code
        ) {
            setAfterChoose()
            setView()
        }

        setPeekHeight(0.7)
    }

    fun setDismissCallback(callback: onDissmissFragment) {
        this.dismissCallback = callback
    }

    private fun getWindowHeight(): Int {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = requireActivity().display
            display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = requireActivity().windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(displayMetrics)
        }
        return displayMetrics.heightPixels
    }

    private fun setPeekHeight(ratio: Double) {
        dialog?.setOnShowListener {
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                val layoutParams = bottomSheet.layoutParams
                val windowHeight = getWindowHeight()
                if (layoutParams != null) {
                    layoutParams.height = (windowHeight * ratio).toInt()
                }
                behavior.peekHeight = (windowHeight * ratio).toInt()
                bottomSheet.layoutParams = layoutParams
                val coordinatorLayout = bottomSheet.parent as CoordinatorLayout
                coordinatorLayout.parent.requestLayout()
            }
        }
    }

    fun setCallback(callback: CallbackReportFragment) {
        this.callback = callback
    }

    fun setAfterChoose() {
        val anims = listOf(
            ObjectAnimator.ofFloat(binding.layoutChooseSignal, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(binding.layoutChooseSuppress, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(binding.layoutChooseCopy, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(binding.layoutChooseTranslate, "alpha", 1f, 0f)
        )
        anims.forEach { it.duration = 100 }

        fun hideAndResize(v: View) {
            v.visibility = View.GONE
            val view = binding.root
            val behavior = BottomSheetBehavior.from(view.parent as View)
            val peekheight = view.height
            behavior.peekHeight = peekheight
            setPeekHeight(0.7)
        }

        anims[0].addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                binding.layoutChooseSignal.visibility = View.GONE
                binding.next.visibility = View.VISIBLE
            }
        })
        anims[1].addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                hideAndResize(binding.layoutChooseSuppress)
            }
        })
        anims[2].addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                hideAndResize(binding.layoutChooseCopy)
            }
        })
        anims[3].addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                hideAndResize(binding.layoutChooseTranslate)
            }
        })

        anims.forEach { it.start() }
    }

    fun setStartView() {
        // Renseigne les flags UI (sûr, pas d'unresolved ref)
        getIsFromMe()
        getIsNotTranslatable()
        getContentCopied()
        getIsMyLanguage()
        getIsFromConv()
        getIsOneToOne()

        // Copier
        if (contentCopied.isNullOrEmpty()) {
            binding.layoutChooseCopy.visibility = View.GONE
        } else {
            binding.layoutChooseCopy.setOnClickListener {
                AnalyticsEvents.logEvent(AnalyticsEvents.Clic_CopyPaste_Settings)
                val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(requireContext().getString(R.string.copied_text), contentCopied)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, context?.getString(R.string.copied_text), Toast.LENGTH_SHORT).show()
                DataLanguageStock.deleteContentToCopy()
                onClose()
                dismiss()
            }
        }

        // Traduire (pertinent seulement pour un message/comm)
        binding.layoutChooseTranslate.visibility = View.VISIBLE
        val isCommentType =
            reportType == ReportTypes.REPORT_COMMENT.code || reportType == ReportTypes.REPORT_POST_EVENT.code
        binding.layoutChooseTranslate.visibility =
            if (isCommentType &&
                DataLanguageStock.postLanguage != DataLanguageStock.userLanguage &&
                isFromMe != true &&
                isNotTranslatable != true
            ) View.VISIBLE else View.GONE

        // Les types conversation / group / event ouvrent direct la 2e vue
        if (reportType == ReportTypes.REPORT_GROUP.code ||
            reportType == ReportTypes.REPORT_EVENT.code ||
            reportType == ReportTypes.REPORT_CONVERSATION.code
        ) {
            setAfterChoose()
            setView()
            return
        }

        if (isFromMe == false) {
            binding.layoutChooseSuppress.visibility = View.GONE
            binding.layoutChooseSignal.visibility = View.VISIBLE
            binding.next.visibility = View.GONE
        } else {
            binding.layoutChooseSuppress.visibility = View.VISIBLE
            binding.layoutChooseSignal.visibility = View.GONE
            binding.next.visibility = View.GONE
        }

        var logEventTitleView = AnalyticsEvents.POST_SUPPRESSED
        var logEventTitleClick = AnalyticsEvents.SUPPRESS_CLICK
        binding.header.title = getString(R.string.title_param_post)

        if (isFromConv == true) {
            binding.header.title = if (isOneToOne == true) {
                logEventTitleView = AnalyticsEvents.Delete_mess
                logEventTitleClick = AnalyticsEvents.Click_delete_mess
                getString(R.string.title_param_message)
            } else {
                logEventTitleView = AnalyticsEvents.Delete_comm
                logEventTitleClick = AnalyticsEvents.Click_delete_comm
                getString(R.string.title_param_comment)
            }
        }

        if (this.isFromConv == true) {
            binding.titleSupressPost.text = if (isOneToOne == true)
                getString(R.string.discussion_choose_supress_message)
            else
                getString(R.string.discussion_choose_supress_commentary)
        }

        binding.layoutChooseSuppress.setOnClickListener {
            AnalyticsEvents.logEvent(logEventTitleView)
            if (this.isFromConv == true) {
                val (title, message, btn) =
                    if (isOneToOne == true)
                        Triple(getString(R.string.discussion_supress_the_message),
                            getString(R.string.discussion_ask_supress_message),
                            getString(R.string.discussion_button_supress))
                    else
                        Triple(getString(R.string.discussion_supress_the_comment),
                            getString(R.string.discussion_ask_supress_comment),
                            getString(R.string.discussion_button_supress))

                CustomAlertDialog.showWithCancelFirst(
                    requireContext(),
                    title, message, btn,
                    { /* cancel */ },
                    {
                        AnalyticsEvents.logEvent(logEventTitleClick)
                        deleteMessage()
                        dismissCallback?.reloadView()
                        onClose()
                        dismiss()
                    }
                )
            } else {
                CustomAlertDialog.showWithCancelFirst(
                    requireContext(),
                    getString(R.string.discussion_supress_the_post),
                    getString(R.string.discussion_ask_supress),
                    getString(R.string.discussion_button_supress),
                    { /* cancel */ },
                    {
                        AnalyticsEvents.logEvent(logEventTitleClick)
                        deleteMessage()
                        dismissCallback?.reloadView()
                    }
                )
            }
        }

        if (reportType == ReportTypes.REPORT_DEMAND.code || reportType == ReportTypes.REPORT_CONTRIB.code) {
            binding.tvChooseSignal.text = getString(R.string.discussion_entraide_signal)
        }
        binding.layoutChooseSignal.setOnClickListener {
            setAfterChoose()
            setView()
        }
        binding.layoutChooseTranslate.setOnClickListener {
            reportedId?.let { id ->
                dismissCallback?.translateView(id)
                callback?.onTranslatePost(id)
            }
            onClose()
            dismiss()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onClose()
    }

    fun setEventComment() { isEventComment = true }
    fun setGroupComment() { isGroupComment = true }

    private fun setView() {
        binding.next.visibility = View.VISIBLE
        binding.secondLayoutSignal.visibility = View.VISIBLE
        title = getString(
            when (reportType) {
                ReportTypes.REPORT_USER.code -> R.string.report_member
                ReportTypes.REPORT_GROUP.code -> R.string.report_group
                ReportTypes.REPORT_POST.code -> R.string.report_post
                ReportTypes.REPORT_COMMENT.code -> R.string.report_comment
                ReportTypes.REPORT_EVENT.code -> R.string.report_eventt
                ReportTypes.REPORT_DEMAND.code -> R.string.action_report_demand
                ReportTypes.REPORT_CONTRIB.code -> R.string.action_report_contrib
                ReportTypes.REPORT_CONVERSATION.code -> R.string.discussion_report_conversation
                ReportTypes.REPORT_POST_EVENT.code -> R.string.report_post
                else -> R.string.report_member
            }
        )
        if (isEventComment) title = getString(R.string.report_comment)
        binding.header.title = title
    }

    private fun handleReportResponse(success: Boolean) {
        if (!isAdded) return
        if (success) {
            when (reportType) {
                ReportTypes.REPORT_EVENT.code -> AnalyticsEvents.logEvent("Action_EventOption_ReportConfirmation")
                ReportTypes.REPORT_CONTRIB.code -> AnalyticsEvents.logEvent("Action__Contrib__Report_Confirmation")
                ReportTypes.REPORT_DEMAND.code -> AnalyticsEvents.logEvent("Action__Demand__Report_Confirmation")
                ReportTypes.REPORT_GROUP.code -> AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GROUP_REPORT)
            }
            CustomAlertDialog.showOnlyOneButton(
                requireContext(),
                title,
                getString(R.string.report_sent),
                getString(R.string.button_OK)
            )
        } else {
            showToast(getString(R.string.user_report_error_send_failed))
        }
        dismiss()
    }

    private fun handleDeletedResponse(success: Boolean) {
        if (isAdded) {
            if (success) {
                showToast(getString(R.string.delete_success_send))
            } else {
                showToast(getString(R.string.delete_error_send_failed))
            }
            callback?.onSuppressPost(reportedId ?: 0)
            onClose()
            dismiss()
        }
    }

    private fun handleMetaData(tags: Tags?) {
        signalList.clear()
        tags?.signals?.let { signalList.addAll(it) }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun readArgs() {
        reportedId = arguments?.getInt(Const.REPORTED_ID)
        groupId = arguments?.getInt(Const.GROUP_ID)
        reportType = arguments?.getInt(Const.REPORT_TYPE)
        isFromMe = arguments?.getBoolean(Const.IS_FROM_ME)
        contentCopied = arguments?.getString(Const.CONTENT_COPIED)
        isMyLanguage = arguments?.getBoolean(Const.IS_MY_LANGUAGE)
        isNotTranslatable = arguments?.getBoolean(Const.IS_NOT_TRANSLATABLE)
        isFromConv = arguments?.getBoolean(Const.IS_FROM_CONV)
        isOneToOne = arguments?.getBoolean(Const.IS_ONE_TO_ONE)
        isSmallTalk = arguments?.getBoolean(ARG_IS_SMALL_TALK, false) ?: false
    }

    private fun initializeInterests() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ReportListAdapter(signalList, object : OnItemCheckListener {
                override fun onItemCheck(item: TagMetaData) { item.id?.let { selectedSignalsIdList.add(it) } }
                override fun onItemUncheck(item: TagMetaData) { selectedSignalsIdList.remove(item.id) }
            })
        }
    }

    private fun setupViewStep1() {
        binding.mandatory.text = getString(R.string.mandatory)
        binding.divider.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.VISIBLE
        binding.next.visibility = View.VISIBLE
        binding.next.setOnClickListener { if (selectedSignalsIdList.isNotEmpty()) setupViewStep2() }
        binding.message.visibility = View.GONE
        binding.back.visibility = View.GONE
        binding.send.visibility = View.GONE
    }

    private fun setupViewStep2() {
        binding.mandatory.text = getString(R.string.optional)
        binding.message.visibility = View.VISIBLE
        binding.back.visibility = View.VISIBLE
        binding.send.visibility = View.VISIBLE
        binding.back.setOnClickListener { setupViewStep1() }

        binding.send.setOnClickListener {
            reportedId?.let { id ->
                when (reportType) {
                    ReportTypes.REPORT_USER.code -> userPresenter.sendReport(
                        id, binding.message.text.toString(), selectedSignalsIdList
                    )
                    ReportTypes.REPORT_GROUP.code -> groupPresenter.sendReport(
                        id, binding.message.text.toString(), selectedSignalsIdList
                    )
                    ReportTypes.REPORT_POST.code,
                    ReportTypes.REPORT_COMMENT.code -> groupId?.let { gid ->
                        groupPresenter.sendReportPost(
                            gid, id, binding.message.text.toString(), selectedSignalsIdList
                        )
                    }
                    ReportTypes.REPORT_POST_EVENT.code -> groupId?.let { eid ->
                        eventPresenter.sendPostReport(
                            eid, id, binding.message.text.toString(), selectedSignalsIdList
                        )
                    }
                    ReportTypes.REPORT_EVENT.code -> eventPresenter.sendReport(
                        id, binding.message.text.toString(), selectedSignalsIdList
                    )
                    ReportTypes.REPORT_CONTRIB.code -> actionPresenter.sendReport(
                        id, binding.message.text.toString(), selectedSignalsIdList, false
                    )
                    ReportTypes.REPORT_DEMAND.code -> actionPresenter.sendReport(
                        id, binding.message.text.toString(), selectedSignalsIdList, true
                    )
                    ReportTypes.REPORT_CONVERSATION.code -> {
                        // classique ou SmallTalk (même méthode ici)
                        discussionsPresenter.sendReport(
                            id, binding.message.text.toString(), selectedSignalsIdList
                        )
                    }
                }
            }
        }
        binding.divider.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.next.visibility = View.GONE
    }

    fun deleteMessage() {
        reportedId?.let { id ->
            when (reportType) {
                ReportTypes.REPORT_POST.code -> groupId?.let { gid -> groupPresenter.deletedGroupPost(gid, id) }
                ReportTypes.REPORT_COMMENT.code -> groupId?.let { gid ->
                    if (isGroupComment) {
                        groupPresenter.deletedGroupPost(gid, id)
                    }
                    if (DetailConversationActivity.isSmallTalkMode) {
                        smallTalkViewModel.deleteChatMessage(DetailConversationActivity.smallTalkId.toString(), id.toString())
                    } else {
                        discussionsPresenter.deleteMessage(gid, id)
                    }
                }
                ReportTypes.REPORT_POST_EVENT.code -> groupId?.let { eid -> eventPresenter.deletedEventPost(eid, id) }
                else -> { /* no-op */ }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun onClose() {
        selectedSignalsIdList.clear()
    }

    private fun handleCloseButton() {
        binding.header.hbsIconCross.setOnClickListener {
            onClose(); dismiss()
        }
    }

    // --- Helpers pour éviter les "unresolved reference" ---
    private fun getIsFromMe() { /* déjà chargé via readArgs() */ }
    private fun getIsNotTranslatable() { /* déjà chargé via readArgs() */ }
    private fun getIsMyLanguage() { /* déjà chargé via readArgs() */ }
    private fun getIsFromConv() { /* déjà chargé via readArgs() */ }
    private fun getIsOneToOne() { /* déjà chargé via readArgs() */ }
    private fun getContentCopied() {
        if (contentCopied.isNullOrEmpty()) {
            val cached = DataLanguageStock.contentToCopy
            if (!cached.isNullOrEmpty()) contentCopied = cached
        }
    }

    companion object {
        const val TAG = "ReportModalFragment"
        private const val ARG_OPEN_DIRECT_SIGNAL = "open_direct_signal"
        private const val ARG_IS_SMALL_TALK = "is_small_talk"

        // ✅ Signature unique et simple: reportType = enum (pas d’Int)
        fun newInstance(
            id: Any,
            groupId: Int,
            reportType: ReportTypes,
            isFromMe: Boolean,
            isConv: Boolean,
            isOneToOne: Boolean,
            contentCopied: String,
            isMyLanguage: Boolean? = null,
            isNotTranslatable: Boolean? = null,
            openDirectSignal: Boolean = false,
            isSmallTalk: Boolean = false
        ): ReportModalFragment {
            val fragment = ReportModalFragment()
            val args = Bundle().apply {
                putInt(Const.REPORTED_ID, id as Int)
                putInt(Const.GROUP_ID, groupId)
                putBoolean(Const.IS_FROM_ME, isFromMe)
                putString(Const.CONTENT_COPIED, contentCopied)
                isMyLanguage?.let { putBoolean(Const.IS_MY_LANGUAGE, it) }
                isNotTranslatable?.let { putBoolean(Const.IS_NOT_TRANSLATABLE, it) }
                putInt(Const.REPORT_TYPE, reportType.code)
                putBoolean(Const.IS_FROM_CONV, isConv)
                putBoolean(Const.IS_ONE_TO_ONE, isOneToOne)
                putBoolean(ARG_OPEN_DIRECT_SIGNAL, openDirectSignal)
                putBoolean(ARG_IS_SMALL_TALK, isSmallTalk)
            }
            fragment.arguments = args
            return fragment
        }
    }
}

interface onDissmissFragment {
    fun reloadView()
    fun translateView(id: Int)
}

public object DataLanguageStock {
    var userLanguage: String = "default"
    var postLanguage: String = "default"
    var contentToCopy: String = ""

    fun updateUserLanguage(language: String) { userLanguage = language }
    fun updatePostLanguage(language: String) { postLanguage = language }
    fun updateContentToCopy(content: String) { contentToCopy = content }
    fun deleteContentToCopy() { contentToCopy = "" }
}
