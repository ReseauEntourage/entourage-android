package social.entourage.android.survey

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.Survey
import social.entourage.android.api.model.SurveyResponsesListWrapper
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityResponseSurveyLayoutBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.groups.details.members.OnItemShowListener
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const

class ResponseSurveyActivity:BaseActivity(), OnItemShowListener {

    private lateinit var binding:ActivityResponseSurveyLayoutBinding
    private var surveyPresenter = SurveyPresenter()
    private var surveyResponses:SurveyResponsesListWrapper? = null
    private val discussionPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResponseSurveyLayoutBinding.inflate(layoutInflater)
        surveyPresenter.surveyResponseList.observe(this, ::handleSurveyResponseList)
        discussionPresenter.newConversation.observe(this, ::handleGetConversation)
        initView()
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        if(isGroup) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Clic_Group_Poll_See_Votes)
            surveyPresenter.getSurveyResponsesForGroup(itemId, postId)
        }else{
            AnalyticsEvents.logEvent(AnalyticsEvents.Clic_Event_Poll_See_Votes)
            surveyPresenter.getSurveyResponsesForEvent(itemId, postId)
        }
    }

    private fun initView(){
        binding.question.text = question
        binding.iconBack.setOnClickListener {
            finish()
        }
        if(survey != null && surveyResponses != null){
            binding.recyclerViewResponse.adapter = SurveyResponseAdapter(survey!!, surveyResponses!!, this, this)
        }
        binding.recyclerViewResponse.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

    }

    private fun handleSurveyResponseList(surveyResponsesListWrapper: SurveyResponsesListWrapper) {
        surveyResponses = surveyResponsesListWrapper
        // Met à jour l'adapter avec les nouvelles réponses et rafraîchit le RecyclerView
        if(survey != null) {
            val adapter = binding.recyclerViewResponse.adapter as? SurveyResponseAdapter
            if (adapter == null) {
                binding.recyclerViewResponse.adapter = SurveyResponseAdapter(survey!!, surveyResponses!!, this, this)
            } else {
                adapter.updateResponses(surveyResponses!!)
            }
        }
    }

    private fun handleGetConversation(conversation: Conversation?) {
        conversation?.let {
            DetailConversationActivity.isSmallTalkMode = false
            startActivityForResult(
                Intent(this, DetailConversationActivity::class.java)
                    .putExtras(
                        bundleOf(
                            Const.ID to conversation.id,
                            Const.POST_AUTHOR_ID to conversation.user?.id,
                            Const.SHOULD_OPEN_KEYBOARD to false,
                            Const.NAME to conversation.title,
                            Const.IS_CONVERSATION_1TO1 to true,
                            Const.IS_MEMBER to true,
                            Const.IS_CONVERSATION to true,
                            Const.HAS_TO_SHOW_MESSAGE to conversation.hasToShowFirstMessage()
                        )
                    ), 0
            )
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        survey = null
        isGroup = false
        itemId = 0
        postId = 0
        question = ""
        myVote = mutableListOf()
    }





    companion object {
        var survey: Survey? = null
        var isGroup:Boolean = false
        var itemId:Int = 0
        var postId:Int = 0
        var question:String = ""
        var myVote: MutableList<Boolean> = mutableListOf()

    }

    override fun onShowConversation(userId: Int) {
        discussionPresenter.createOrGetConversation(userId.toString())
    }
}