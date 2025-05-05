package social.entourage.android.small_talks

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.SmallTalkOtherBandsBinding
import social.entourage.android.small_talks.SmallTalkGroupFoundActivity

enum class OtherBandType {
    DIFFERENT_LOCATION,
    DIFFERENT_INTERESTS,
    DUO,
    GROUP_OF_THREE_PLUS
}

data class OtherBand(
    val members: List<String>,
    val memberAvatarUrls: List<String>,
    val type: OtherBandType
)



class SmallTalkListOtherBands : BaseActivity() {

    private lateinit var binding: SmallTalkOtherBandsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SmallTalkOtherBandsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //TODO : changed to call request
        val bands = listOf(
            OtherBand(
                members = listOf("Théo", "Louis"),
                memberAvatarUrls = listOf("", ""), // ou des URL valides si tu en as
                type = OtherBandType.DIFFERENT_LOCATION
            ),
            OtherBand(
                members = listOf("Théo", "Louis"),
                memberAvatarUrls = listOf("", ""),
                type = OtherBandType.DIFFERENT_INTERESTS
            ),
            OtherBand(
                members = listOf("Paul"),
                memberAvatarUrls = listOf(""),
                type = OtherBandType.DUO
            ),
            OtherBand(
                members = listOf("Théo", "Louis", "Lilia"),
                memberAvatarUrls = listOf("", "", ""),
                type = OtherBandType.GROUP_OF_THREE_PLUS
            )
        )
        binding.title.setOnClickListener {
            startActivity(Intent(this, SmallTalkGroupFoundActivity::class.java))
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = OtherBandsAdapter(bands) { band ->
            // handle join logic
        }

        binding.buttonWait.setOnClickListener {
            finish()
        }
    }
}