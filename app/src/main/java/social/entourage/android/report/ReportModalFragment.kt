package social.entourage.android.report

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
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
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.events.EventsPresenter
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.user.UserPresenter
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import timber.log.Timber

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
    private var reportedId: Int? = Const.DEFAULT_VALUE
    private var groupId: Int? = Const.DEFAULT_VALUE
    private var reportType: Int? = Const.DEFAULT_VALUE
    private var title: String = ""
    private var isEventComment = false


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
        groupPresenter.isPostDeleted.observe(requireActivity(), ::handleDeletedResponse)
        eventPresenter.isEventReported.observe(requireActivity(), ::handleDeletedResponse)
        eventPresenter.isEventDeleted.observe(requireActivity(), ::handleDeletedResponse)
        eventPresenter.isEventPostReported.observe(requireActivity(), ::handleReportResponse)
        actionPresenter.isActionReported.observe(requireActivity(), ::handleReportResponse)
        discussionsPresenter.isConversationReported.observe(requireActivity(), ::handleReportResponse)
        discussionsPresenter.isConversationDeleted.observe(requireActivity(), ::handleDeletedResponse)

        setupViewStep1()
        handleCloseButton()
        setStartView()

        //Use to force refresh layout
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(social.entourage.android.R.id.design_bottom_sheet) as FrameLayout?
            val coordinatorLayout = bottomSheet!!.parent as CoordinatorLayout
            val bottomSheetBehavior: BottomSheetBehavior<*> =
                BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.peekHeight = bottomSheet.height
            coordinatorLayout.parent.requestLayout()
        }
    }


    fun setAfterChoose(){
        val animSignal= ObjectAnimator.ofFloat(binding.layoutChooseSignal, "alpha", 1.0f,0.0F)
        val animSupress = ObjectAnimator.ofFloat(binding.layoutChooseSuppress, "alpha", 1.0f,0.0F)
        animSignal.duration = 100
        animSupress.duration = 100
        animSignal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                binding.layoutChooseSignal.visibility = View.GONE

            }
        })
        animSupress.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                binding.layoutChooseSuppress.visibility = View.GONE
                val view = binding.root
                val behavior = BottomSheetBehavior.from(view.parent as View)
                val peekheight = view.height
                behavior.peekHeight = peekheight
            }
        })
        animSignal.start()
        animSupress.start()
    }
    fun setStartView(){
        Timber.wtf("wtf " + reportType)
        binding.header.title = getString(R.string.title_param_post)
        binding.layoutChooseSuppress.setOnClickListener {
            setAfterChoose()
            onClose()
            dismiss()
            CustomAlertDialog.showWithCancelFirst(
                requireContext(),
                getString(R.string.discussion_supress_the_post),
                getString(R.string.discussion_ask_supress),
                getString(R.string.discussion_button_supress)
                ,{
                    //ON CANCEL DO NOTHING YET
                },{
                    deleteMessage()

                })

        }
        binding.layoutChooseSignal.setOnClickListener {
            setAfterChoose()
            setView()
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
        if (success) CustomAlertDialog.showOnlyOneButton(
            requireContext(),
            title,
            getString(R.string.report_sent),
            getString(R.string.button_OK)
        )
        else showToast(getString(R.string.user_report_error_send_failed))
        dismiss()
    }

    private fun handleDeletedResponse(success: Boolean) {
        if (success){
            showToast(getString(R.string.delete_success_send))
        }else{
            showToast(getString(R.string.delete_error_send_failed))
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
                ReportTypes.REPORT_POST.code, ReportTypes.REPORT_COMMENT.code -> groupId?.let { it ->
                    groupPresenter.deletedGroupPost(
                        it,
                        id
                    )
                }
                ReportTypes.REPORT_POST_EVENT.code-> groupId?.let { it ->
                    eventPresenter.deletedEventPost(
                        it,
                        id
                    )
                }

                ReportTypes.REPORT_CONVERSATION.code -> discussionsPresenter.sendReport(
                    id,
                    binding.message.text.toString(),
                    selectedSignalsIdList
                )
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
        fun newInstance(id: Int, groupId: Int, reportType: ReportTypes): ReportModalFragment {
            val fragment = ReportModalFragment()
            val args = Bundle()
            args.putInt(Const.REPORTED_ID, id)
            args.putInt(Const.GROUP_ID, groupId)
            args.putInt(Const.REPORT_TYPE, reportType.code)
            fragment.arguments = args
            return fragment
        }
    }
}