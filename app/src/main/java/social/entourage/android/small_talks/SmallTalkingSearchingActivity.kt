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
    private var isMatchFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmallTalkSearchingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        smallTalkViewModel.matchResult.observe(this, Observer { result ->
            isMatchFinished = true
            maybeGoToNextScreen(result)
        })
        //smallTalkViewModel.matchRequest(id)
        binding.titleText.setOnClickListener {
            startActivity(Intent(this, SmallTalkListOtherBands::class.java))
        }
        startProgressiveMessages()
    }

    private fun startProgressiveMessages() {
        val delay = 2000L
        animatedTexts.forEachIndexed { index, message ->
            handler.postDelayed({
                binding.progressiveMessage.alpha = 0f
                binding.progressiveMessage.text = message
                binding.progressiveMessage.animate().alpha(1f).setDuration(500).start()

                // Si c’est le dernier message, on marque l’animation comme finie
                if (index == animatedTexts.lastIndex) {
                    handler.postDelayed({
                        isAnimationFinished = true
                        maybeGoToNextScreen(smallTalkViewModel.matchResult.value)
                    }, 500) // attendre que le fade-in soit terminé
                }
            }, index * delay)
        }
    }

    private fun maybeGoToNextScreen(result: SmallTalkMatchResponse?) {
        if (isAnimationFinished && isMatchFinished && result != null) {
            startActivity(Intent(this, SmallTalkListOtherBands::class.java))
        }
    }

    companion object {
        var id: String = "id"
    }
}
