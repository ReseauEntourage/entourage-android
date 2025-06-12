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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.databinding.NewFragmentReportBinding
import social.entourage.android.actions.ActionsPresenter
import social.entourage.android.api.model.TagMetaData
import social.entourage.android.api.model.Tags
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.discussions.DetailConversationActivity.Companion.isSmallTalkMode
import social.entourage.android.discussions.DetailConversationActivity.Companion.smallTalkId
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.events.EventsPresenter
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.details.feed.CallbackReportFragment
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.user.UserPresenter
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import timber.log.Timber
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.small_talks.SmallTalkViewModel
import kotlin.getValue

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


class ReportModalFragment() : BottomSheetDialogFragment() {

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
    private var callback: CallbackReportFragment? = null
    private var isFromMe: Boolean? = false
    private var isMyLanguage: Boolean? = false
    private var isNotTranslatable: Boolean? = false
    private var isFromConv: Boolean? = false
    private var isOneToOne: Boolean? = false
    private var dismissCallback:onDissmissFragment? = null
    private var contentCopied:String? = null

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
        getReportedIdAndType()
        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        userPresenter.isUserReported.observe(requireActivity(), ::handleReportResponse)
        groupPresenter.isGroupReported.observe(requireActivity(), ::handleReportResponse)
        groupPresenter.isPostReported.observe(requireActivity(), ::handleReportResponse)
        eventPresenter.isEventReported.observe(requireActivity(), ::handleReportResponse)
        eventPresenter.isEventPostReported.observe(requireActivity(), ::handleReportResponse)
        actionPresenter.isActionReported.observe(requireActivity(), ::handleReportResponse)
        discussionsPresenter.isConversationReported.observe(requireActivity(), ::handleReportResponse)
        discussionsPresenter.isConversationDeleted.observe(requireActivity(), ::handleDeletedResponse)
        discussionsPresenter.isMessageDeleted.observe(requireActivity(),::handleDeletedResponse)
        groupPresenter.isPostDeleted.observe(requireActivity(),::handleDeletedResponse)
        eventPresenter.isEventDeleted.observe(requireActivity(),::handleDeletedResponse)

        setupViewStep1()
        handleCloseButton()
        setStartView()

        if(reportType == ReportTypes.REPORT_USER.code){
            setAfterChoose()
            setView()
        }
        //Use to force refresh layout
        setPeekHeight(0.7)
    }

    fun setDismissCallback(callback:onDissmissFragment){
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

    private fun setPeekHeight(ratio:Double){
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

    fun setCallback(callback:CallbackReportFragment){
        this.callback = callback
    }
    fun setAfterChoose(){

        val animSignal= ObjectAnimator.ofFloat(binding.layoutChooseSignal, "alpha", 1.0f,0.0F)
        val animSupress = ObjectAnimator.ofFloat(binding.layoutChooseSuppress, "alpha", 1.0f,0.0F)
        val animCopy = ObjectAnimator.ofFloat(binding.layoutChooseCopy, "alpha", 1.0f,0.0F)
        val animTranslate = ObjectAnimator.ofFloat(binding.layoutChooseTranslate, "alpha", 1.0f,0.0F)
        animSignal.duration = 100
        animSupress.duration = 100
        animCopy.duration = 100
        animTranslate.duration = 100
        animSignal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                binding.layoutChooseSignal.visibility = View.GONE
                binding.next.visibility = View.VISIBLE
            }
        })
        animSupress.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                binding.layoutChooseSuppress.visibility = View.GONE
                val view = binding.root
                val behavior = BottomSheetBehavior.from(view.parent as View)
                val peekheight = view.height
                behavior.peekHeight = peekheight
                setPeekHeight(0.7)
            }
        })
        animCopy.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                binding.layoutChooseCopy.visibility = View.GONE
                val view = binding.root
                val behavior = BottomSheetBehavior.from(view.parent as View)
                val peekheight = view.height
                behavior.peekHeight = peekheight
                setPeekHeight(0.7)
            }
        })
        animTranslate.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                binding.layoutChooseTranslate.visibility = View.GONE
                val view = binding.root
                val behavior = BottomSheetBehavior.from(view.parent as View)
                val peekheight = view.height
                behavior.peekHeight = peekheight
                setPeekHeight(0.7)
            }
        })
        animSignal.start()
        animSupress.start()
        animCopy.start()
        animTranslate.start()
    }
    fun setStartView(){
        getIsFromMe()
        getIsNotTranslatable()
        getContentCopied()
        getIsMyLanguage()
        getIsFromConv()
        getIsOneToOne()
        if(contentCopied == null || contentCopied.isNullOrEmpty()){
            binding.layoutChooseCopy.visibility = View.GONE
        }else{
            binding.layoutChooseCopy.setOnClickListener {
                AnalyticsEvents.logEvent(AnalyticsEvents.Clic_CopyPaste_Settings)
                val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(requireContext().getString(R.string.copied_text), contentCopied)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, context?.getString(R.string.copied_text), Toast.LENGTH_SHORT).show()
                true
                DataLanguageStock.deleteContentToCopy()
                onClose()
                dismiss()
            }
        }
            binding.layoutChooseTranslate.visibility = View.VISIBLE
        if(reportType == ReportTypes.REPORT_COMMENT.code || reportType == ReportTypes.REPORT_POST_EVENT.code){
            binding.layoutChooseTranslate.visibility = View.VISIBLE
        }else{
            binding.layoutChooseTranslate.visibility = View.GONE
        }
        if(DataLanguageStock.postLanguage == DataLanguageStock.userLanguage || (isFromMe == true)  ){
            binding.layoutChooseTranslate.visibility = View.GONE
        }else{
            binding.layoutChooseTranslate.visibility = View.VISIBLE
        }
        if(isNotTranslatable == true){
            binding.layoutChooseTranslate.visibility = View.GONE
        }

        if (reportType == ReportTypes.REPORT_GROUP.code){
            setAfterChoose()
            setView()
            return
        }
        if (reportType == ReportTypes.REPORT_EVENT.code){
            setAfterChoose()
            setView()
            return
        }
        if (reportType == ReportTypes.REPORT_CONVERSATION.code){
            setAfterChoose()
            setView()
            return
        }

        if(isFromMe == false){
            binding.layoutChooseSuppress.visibility = View.GONE
            binding.layoutChooseSignal.visibility = View.VISIBLE
            binding.next.visibility = View.GONE
        }else{
            binding.layoutChooseSuppress.visibility = View.VISIBLE
            binding.layoutChooseSignal.visibility = View.GONE
            binding.next.visibility = View.GONE
        }
        var logEventTitleView = AnalyticsEvents.POST_SUPPRESSED
        var logEventTitleClick = AnalyticsEvents.SUPPRESS_CLICK
        binding.header.title = getString(R.string.title_param_post)
        if(reportType == ReportTypes.REPORT_DEMAND.code || reportType == ReportTypes.REPORT_CONTRIB.code){
            binding.header.title = getString(R.string.action_title_header_signal_problem)
        }

        if(isFromConv == true){
            binding.header.title = getString(R.string.title_param_comment)
            logEventTitleView = AnalyticsEvents.Delete_comm
            logEventTitleClick = AnalyticsEvents.Click_delete_comm
            if(isOneToOne == true){
                logEventTitleView = AnalyticsEvents.Delete_mess
                logEventTitleClick = AnalyticsEvents.Click_delete_mess
                binding.header.title = getString(R.string.title_param_message)
            }
        }
        if(this.isFromConv == true){
            if(isOneToOne == true){
                binding.titleSupressPost.text = getString(R.string.discussion_choose_supress_message)
            }else{
                binding.titleSupressPost.text = getString(R.string.discussion_choose_supress_commentary)
            }
        }
        binding.layoutChooseSuppress.setOnClickListener {
            AnalyticsEvents.logEvent(logEventTitleView)
            if(this.isFromConv == true){
                var title = ""
                var message = ""
                var btnTitle = ""
                if(isOneToOne == true){
                    title = getString(R.string.discussion_supress_the_message)
                    message = getString(R.string.discussion_ask_supress_message)
                    btnTitle = getString(R.string.discussion_button_supress)
                }else{
                    title = getString(R.string.discussion_supress_the_comment)
                    message = getString(R.string.discussion_ask_supress_comment)
                    btnTitle = getString(R.string.discussion_button_supress)
                }

                CustomAlertDialog.showWithCancelFirst(
                    requireContext(),
                    title,
                    message,
                    btnTitle
                    ,{
                        //ON CANCEL DO NOTHING YET
                    },{
                        AnalyticsEvents.logEvent(logEventTitleClick)
                        deleteMessage()
                        dismissCallback?.reloadView()
                        onClose()
                        dismiss()
                    })
            }else{
                CustomAlertDialog.showWithCancelFirst(
                    requireContext(),
                    getString(R.string.discussion_supress_the_post),
                    getString(R.string.discussion_ask_supress),
                    getString(R.string.discussion_button_supress)
                    ,{
                        //ON CANCEL DO NOTHING YET
                    },{
                        AnalyticsEvents.logEvent(logEventTitleClick)
                        deleteMessage()
                        dismissCallback?.reloadView()

                    })
            }
        }
        if(reportType == ReportTypes.REPORT_DEMAND.code || reportType == ReportTypes.REPORT_CONTRIB.code){
            binding.tvChooseSignal.text = getString(R.string.discussion_entraide_signal)
        }
        binding.layoutChooseSignal.setOnClickListener {
            setAfterChoose()
            setView()
        }
        binding.layoutChooseTranslate.setOnClickListener {
            if(reportedId != null){
                dismissCallback?.translateView(reportedId!!)
                callback?.onTranslatePost(reportedId!!)
            }
            onClose()
            dismiss()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onClose()
    }

    fun setEventComment(){
        isEventComment = true
    }

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
        if(isEventComment){
            title = getString(R.string.report_comment)
        }
        binding.header.title = title

    }

    private fun handleReportResponse(success: Boolean) {
        if(isAdded){
            if(success){
                if(reportType == ReportTypes.REPORT_EVENT.code){
                    AnalyticsEvents.logEvent("Action_EventOption_ReportConfirmation")
                }else if(reportType == ReportTypes.REPORT_CONTRIB.code){
                    AnalyticsEvents.logEvent("Action__Contrib__Report_Confirmation")
                }else if(reportType == ReportTypes.REPORT_DEMAND.code){
                    AnalyticsEvents.logEvent("Action__Demand__Report_Confirmation")
                }else if (reportType == ReportTypes.REPORT_GROUP.code){
                    AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GROUP_REPORT)
                }
            }
            if (success) CustomAlertDialog.showOnlyOneButton(
                requireContext(),
                title,
                getString(R.string.report_sent),
                getString(R.string.button_OK)
            )
            else showToast(getString(R.string.user_report_error_send_failed))
            dismiss()
        }
    }

    private fun handleDeletedResponse(success: Boolean) {
        if(isAdded){
            if (success){
                showToast(getString(R.string.delete_success_send))
                callback?.onSuppressPost(reportedId!!)
                onClose()
                dismiss()
            }else{

                showToast(getString(R.string.delete_error_send_failed))
                callback?.onSuppressPost(reportedId!!)
                onClose()
                dismiss()
            }
        }
        dismiss()
    }

    private fun handleMetaData(tags: Tags?) {
        signalList.clear()
        tags?.signals?.let { signalList.addAll(it) }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun getReportedIdAndType() {
        reportedId = arguments?.getInt(Const.REPORTED_ID)
        groupId = arguments?.getInt(Const.GROUP_ID)
        reportType = arguments?.getInt(Const.REPORT_TYPE)
    }
    fun getIsFromMe(){
        isFromMe = arguments?.getBoolean(Const.IS_FROM_ME)
    }
    fun getContentCopied(){
        contentCopied = arguments?.getString(Const.CONTENT_COPIED)

    }

    fun getIsMyLanguage(){
        isMyLanguage = arguments?.getBoolean(Const.IS_MY_LANGUAGE)
    }
    fun getIsNotTranslatable(){

        isNotTranslatable = arguments?.getBoolean(Const.IS_NOT_TRANSLATABLE)
    }
    fun getIsFromConv(){
        isFromConv = arguments?.getBoolean(Const.IS_FROM_CONV)

    }
    fun getIsOneToOne(){
        isOneToOne = arguments?.getBoolean(Const.IS_ONE_TO_ONE)

    }

    private fun initializeInterests() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ReportListAdapter(signalList, object : OnItemCheckListener {
                override fun onItemCheck(item: TagMetaData) {
                    item.id?.let { selectedSignalsIdList.add(it) }
                }

                override fun onItemUncheck(item: TagMetaData) {
                    selectedSignalsIdList.remove(item.id)
                }
            })
        }
    }

    private fun setupViewStep1() {
        binding.mandatory.text = getString(R.string.mandatory)
        binding.divider.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.VISIBLE
        binding.next.visibility = View.VISIBLE
        binding.next.setOnClickListener {
            if (selectedSignalsIdList.isNotEmpty())
                setupViewStep2()
        }
        binding.message.visibility = View.GONE
        binding.back.visibility = View.GONE
        binding.send.visibility = View.GONE
    }

    private fun setupViewStep2() {
        binding.mandatory.text = getString(R.string.optional)
        binding.message.visibility = View.VISIBLE
        binding.back.visibility = View.VISIBLE
        binding.send.visibility = View.VISIBLE
        binding.back.setOnClickListener {
            setupViewStep1()
        }

        binding.send.setOnClickListener {
            reportedId?.let { id ->
                when (reportType) {
                    ReportTypes.REPORT_USER.code -> userPresenter.sendReport(
                        id,
                        binding.message.text.toString(),
                        selectedSignalsIdList
                    )
                    ReportTypes.REPORT_GROUP.code -> groupPresenter.sendReport(
                        id,
                        binding.message.text.toString(),
                        selectedSignalsIdList
                    )
                    ReportTypes.REPORT_POST.code, ReportTypes.REPORT_COMMENT.code -> groupId?.let { it ->
                        groupPresenter.sendReportPost(
                            it,
                            id,
                            binding.message.text.toString(),
                            selectedSignalsIdList
                        )
                    }
                    ReportTypes.REPORT_POST_EVENT.code-> groupId?.let { it ->
                        eventPresenter.sendPostReport(
                            it,
                            id,
                            binding.message.text.toString(),
                            selectedSignalsIdList
                        )
                    }

                    ReportTypes.REPORT_EVENT.code -> eventPresenter.sendReport(
                        id,
                        binding.message.text.toString(),
                        selectedSignalsIdList
                    )
                    ReportTypes.REPORT_CONTRIB.code -> actionPresenter.sendReport(
                        id,
                        binding.message.text.toString(),
                        selectedSignalsIdList,false
                    )
                    ReportTypes.REPORT_DEMAND.code -> actionPresenter.sendReport(
                        id,
                        binding.message.text.toString(),
                        selectedSignalsIdList, true
                    )
                    ReportTypes.REPORT_CONVERSATION.code -> discussionsPresenter.sendReport(
                        id,
                        binding.message.text.toString(),
                        selectedSignalsIdList
                    )
                    else -> R.string.report_member
                }
            }
        }
        binding.divider.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.next.visibility = View.GONE
    }

    fun deleteMessage(){
        reportedId?.let { id ->
            when (reportType) {
                ReportTypes.REPORT_POST.code -> groupId?.let { it ->
                    groupPresenter.deletedGroupPost(it, id)
                }
                ReportTypes.REPORT_COMMENT.code -> groupId?.let { it ->
                    if(DetailConversationActivity.isSmallTalkMode){
                        smallTalkViewModel.deleteChatMessage(smallTalkId, id.toString())
                    }else {
                        discussionsPresenter.deleteMessage(it, id) // ➜ Discussion
                    }
                }
                ReportTypes.REPORT_POST_EVENT.code -> groupId?.let { it ->
                    eventPresenter.deletedEventPost(it, id)
                }
                else -> R.string.report_member
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun onClose() {
        selectedSignalsIdList.clear()
        userPresenter.isUserReported = MutableLiveData()
        groupPresenter.isGroupReported = MutableLiveData()
        eventPresenter.isEventReported = MutableLiveData()
        actionPresenter.isActionReported = MutableLiveData()
        discussionsPresenter.isConversationReported = MutableLiveData()
    }

    private fun handleCloseButton() {
        binding.header.iconCross.setOnClickListener {
            onClose()
            dismiss()
        }
    }

    companion object {
        const val TAG = "ReportModalFragment"
        fun newInstance(id: Int, groupId: Int, reportType: ReportTypes , isFromMe:Boolean, isConv:Boolean, isOneToOne:Boolean, isMyLanguage:Boolean? = null, isNotTranslatable:Boolean? = null, contentCopied:String): ReportModalFragment {
            val fragment = ReportModalFragment()
            val args = Bundle()
            args.putInt(Const.REPORTED_ID, id)
            args.putInt(Const.GROUP_ID, groupId)
            args.putBoolean(Const.IS_FROM_ME, isFromMe)
            args.putString(Const.CONTENT_COPIED, contentCopied)
            if(isMyLanguage != null){
                args.putBoolean(Const.IS_MY_LANGUAGE, isMyLanguage)
            }
            if(isNotTranslatable != null){
                args.putBoolean(Const.IS_NOT_TRANSLATABLE, isNotTranslatable)
            }
            args.putInt(Const.REPORT_TYPE, reportType.code)
            args.putInt(Const.REPORT_TYPE, reportType.code)
            args.putBoolean(Const.IS_FROM_CONV, isConv)
            args.putBoolean(Const.IS_ONE_TO_ONE, isOneToOne)
            fragment.arguments = args
            return fragment
        }
    }
}


interface onDissmissFragment{
    fun reloadView()
    fun translateView(id:Int)
}

public object DataLanguageStock {
    var userLanguage: String = "default"
    var postLanguage: String = "default"
    var contentToCopy:String = ""

    fun updateUserLanguage(language: String) {
        userLanguage = language
    }

    fun updatePostLanguage(language: String) {
        postLanguage = language
    }
    fun updateContentToCopy(content:String){
        contentToCopy = content
    }
    fun deleteContentToCopy(){
        contentToCopy = ""
    }
}