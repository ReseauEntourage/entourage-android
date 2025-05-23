package social.entourage.android.small_talks

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import social.entourage.android.databinding.ActivitySmallTalkSearchingBinding
import social.entourage.android.R
import social.entourage.android.api.request.SmallTalkMatchResponse

class SmallTalkingSearchingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmallTalkSearchingBinding
    private val handler = Handler(Looper.getMainLooper())
    private val smallTalkViewModel: SmallTalkViewModel by viewModels()

    private val animatedTexts by lazy {
        listOf(
            getString(R.string.small_talk_searching_step_1),
            getString(R.string.small_talk_searching_step_2),
            getString(R.string.small_talk_searching_step_3),
            getString(R.string.small_talk_searching_step_4)
        )
    }

    private var isAnimationFinished = false
    private var matchResult: SmallTalkMatchResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmallTalkSearchingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        smallTalkViewModel.matchResult.observe(this, Observer { result ->
            matchResult = result
            maybeGoToNextScreen()
        })

        smallTalkViewModel.matchRequest(id)
        startProgressiveMessages()

    }

    private fun startProgressiveMessages() {
        val delay = 2000L
        animatedTexts.forEachIndexed { index, message ->
            handler.postDelayed({
                binding.progressiveMessage.alpha = 0f
                binding.progressiveMessage.text = message
                binding.progressiveMessage.animate().alpha(1f).setDuration(500).start()

                if (index == animatedTexts.lastIndex) {
                    handler.postDelayed({
                        isAnimationFinished = true
                        maybeGoToNextScreen()
                    }, 500)
                }
            }, index * delay)
        }
    }

    private fun maybeGoToNextScreen() {
        if (isAnimationFinished && matchResult != null) {
            if (matchResult?.match == true) {
                val intent = Intent(this, SmallTalkGroupFoundActivity::class.java)
                intent.putExtra(SmallTalkGroupFoundActivity.EXTRA_SMALL_TALK_ID, matchResult?.smalltalkId ?: -1)
                startActivity(intent)
            } else {
                startActivity(Intent(this, SmallTalkListOtherBands::class.java))
            }
            finish()
        }
    }

    companion object {
        var id: String = "id"
    }
}
