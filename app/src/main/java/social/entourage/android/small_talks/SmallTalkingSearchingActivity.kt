package social.entourage.android.small_talks

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import social.entourage.android.databinding.ActivitySmallTalkSearchingBinding
import social.entourage.android.R

class SmallTalkingSearchingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmallTalkSearchingBinding
    private val handler = Handler(Looper.getMainLooper())

    private val animatedTexts by lazy {
        listOf(
            getString(R.string.small_talk_searching_step_1),
            getString(R.string.small_talk_searching_step_2),
            getString(R.string.small_talk_searching_step_3),
            getString(R.string.small_talk_searching_step_4)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmallTalkSearchingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.titleText.setOnClickListener {
            //send SmallTalkListOtherBands activity
            startActivity(Intent(this, SmallTalkListOtherBands::class.java))
        }
        startProgressiveMessages()
    }

    private fun startProgressiveMessages() {
        val delay = 2000L // 2 secondes entre chaque message
        animatedTexts.forEachIndexed { index, message ->
            handler.postDelayed({
                binding.progressiveMessage.alpha = 0f
                binding.progressiveMessage.text = message
                binding.progressiveMessage.animate().alpha(1f).setDuration(500).start()
            }, index * delay)
        }
    }
}
