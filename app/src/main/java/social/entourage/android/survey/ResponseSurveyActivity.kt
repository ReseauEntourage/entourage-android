package social.entourage.android.survey

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.api.model.Survey
import social.entourage.android.api.model.SurveyResponsesListWrapper
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityResponseSurveyLayoutBinding

class ResponseSurveyActivity:BaseActivity() {

    private lateinit var binding:ActivityResponseSurveyLayoutBinding
    private var surveyPresenter = SurveyPresenter()
    private var surveyResponses:SurveyResponsesListWrapper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResponseSurveyLayoutBinding.inflate(layoutInflater)
        surveyPresenter.surveyResponseList.observe(this, ::handleSurveyResponseList)
        initView()
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        if(isGroup) {
            surveyPresenter.getSurveyResponsesForGroup(itemId, postId)
        }
    }

    private fun initView(){
        binding.question.text = question
        binding.iconBack.setOnClickListener {
            finish()
        }
        if(survey != null && surveyResponses != null){
            binding.recyclerViewResponse.adapter = SurveyResponseAdapter(survey!!, surveyResponses!!)
        }
        binding.recyclerViewResponse.layoutManager = LinearLayoutManager(this)

    }

    private fun handleSurveyResponseList(surveyResponsesListWrapper: SurveyResponsesListWrapper) {
        surveyResponses = surveyResponsesListWrapper
    }

    override fun onDestroy() {
        super.onDestroy()
        survey = null
        isGroup = false
        itemId = 0
        postId = 0
        question = ""
    }





    companion object {
        var survey: Survey? = null
        var isGroup:Boolean = false
        var itemId:Int = 0
        var postId:Int = 0
        var question:String = ""
    }
}