package social.entourage.android.small_talks

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivitySmallTalkGuidelinesBinding
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

class SmallTalkGuidelinesActivity : BaseActivity() {

    private lateinit var binding: ActivitySmallTalkGuidelinesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmallTalkGuidelinesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__SMALLTALK__CHARTER)
        updatePaddingTopForEdgeToEdge(binding.root)
        setupUI()
    }

    private fun setupUI() {
        binding.btnSmallTalkClose.setOnClickListener {
            finish()
        }

        // Section "Ce que c’est"
        binding.cardSmallTalkWhatIs.tvSmallTalkInfoTitle.text = getString(R.string.small_talk_what_it_is_title)
        binding.cardSmallTalkWhatIs.tvSmallTalkInfo1.text = getString(R.string.small_talk_what_it_is_item_1)
        binding.cardSmallTalkWhatIs.tvSmallTalkInfo2.text = getString(R.string.small_talk_what_it_is_item_2)
        binding.cardSmallTalkWhatIs.tvSmallTalkInfo3.text = getString(R.string.small_talk_what_it_is_item_3)

        // Section "Ce que ce n’est pas"
        binding.cardSmallTalkWhatIsNot.tvSmallTalkInfoTitle.text = getString(R.string.small_talk_what_it_is_not_title)
        binding.cardSmallTalkWhatIsNot.tvSmallTalkInfo1.text = getString(R.string.small_talk_what_it_is_not_item_1)
        binding.cardSmallTalkWhatIsNot.tvSmallTalkInfo2.text = getString(R.string.small_talk_what_it_is_not_item_2)
        binding.cardSmallTalkWhatIsNot.tvSmallTalkInfo3.text = getString(R.string.small_talk_what_it_is_not_item_3)

        // Cartes de la charte
        binding.cardSmallTalkEthics1.tvSmallTalkEthicTitle.text = getString(R.string.small_talk_ethics_1_title)
        binding.cardSmallTalkEthics1.tvSmallTalkEthicDesc.text = getString(R.string.small_talk_ethics_1_desc)
        binding.cardSmallTalkEthics2.tvSmallTalkEthicTitle.text = getString(R.string.small_talk_ethics_2_title)
        binding.cardSmallTalkEthics2.tvSmallTalkEthicDesc.text = getString(R.string.small_talk_ethics_2_desc)
        binding.cardSmallTalkEthics3.tvSmallTalkEthicTitle.text = getString(R.string.small_talk_ethics_3_title)
        binding.cardSmallTalkEthics3.tvSmallTalkEthicDesc.text = getString(R.string.small_talk_ethics_3_desc)
        binding.cardSmallTalkEthics4.tvSmallTalkEthicTitle.text = getString(R.string.small_talk_ethics_4_title)
        binding.cardSmallTalkEthics4.tvSmallTalkEthicDesc.text = getString(R.string.small_talk_ethics_4_desc)
    }
}
