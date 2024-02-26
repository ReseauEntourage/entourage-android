package social.entourage.android.survey

import android.os.Bundle
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityResponseSurveyLayoutBinding

class ResponseSurveyActivity:BaseActivity() {

    private lateinit var binding:ActivityResponseSurveyLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResponseSurveyLayoutBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}