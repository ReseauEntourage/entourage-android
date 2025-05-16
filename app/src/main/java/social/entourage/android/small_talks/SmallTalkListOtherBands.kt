package social.entourage.android.small_talks

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.api.model.SmallTalk
import social.entourage.android.api.model.UserSmallTalkRequest
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.SmallTalkOtherBandsBinding
import social.entourage.android.discussions.DetailConversationActivity

enum class OtherBandType {
    DIFFERENT_LOCATION,
    DIFFERENT_INTERESTS,
    DUO,
    GROUP_OF_THREE_PLUS
}


class SmallTalkListOtherBands : BaseActivity() {

    private lateinit var binding: SmallTalkOtherBandsBinding
    private lateinit var viewModel: SmallTalkViewModel

    companion object {
        var matchingLocality: Boolean = false
        var matchingInterest: Boolean = true
        var matchingGender: Boolean = false
        var matchingGroup: String = "" // "one" ou "many"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SmallTalkOtherBandsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SmallTalkViewModel::class.java]

        viewModel.userRequests.observe(this) { requests ->
            // Ne rien faire avec userRequests ici pour l’instant
        }

        // 🔁 Observer la liste des "presque matchs"
        observeAlmostMatches()

        // ⏩ Lance la requête
        viewModel.listAlmostMatches()

        // 🔘 Bouton "je préfère attendre"
        binding.buttonWait.setOnClickListener {
            finish()
        }

        // 🔘 Debug (accès direct)
        binding.title.setOnClickListener {
            startActivity(Intent(this, SmallTalkGroupFoundActivity::class.java))
        }
    }

    private fun observeAlmostMatches() {
        viewModel.almostMatches.observe(this) { userRequests ->
            if (userRequests.isEmpty()) {
                startActivity(Intent(this, SmallTalkNoBandFound::class.java))
                finish()
                return@observe
            }

            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = OtherBandsAdapter(userRequests) { selected ->
                val smallTalk = selected.smallTalk
                if (smallTalk?.id != null) {
                    // 👇 Redirige vers la conversation directement
                    val intent = Intent(this, DetailConversationActivity::class.java)
                    DetailConversationActivity.isSmallTalkMode = true
                    DetailConversationActivity.smallTalkId = smallTalk.id.toString()
                    startActivity(intent)
                    finish()
                } else {
                    // Sinon : appelle match + redirection
                    selected.smallTalk?.id?.toString()?.let { smallTalkId ->
                        viewModel.matchRequest(smallTalkId)
                        startActivity(Intent(this, SmallTalkGroupFoundActivity::class.java))
                    }
                }
            }
        }
    }

    private fun determineMismatchType(request: UserSmallTalkRequest): OtherBandType {
        return when {
            matchingLocality && request.matchLocality == false -> OtherBandType.DIFFERENT_LOCATION
            matchingInterest && request.matchInterest == false -> OtherBandType.DIFFERENT_INTERESTS
            matchingGender && request.matchGender == false -> OtherBandType.DIFFERENT_INTERESTS
            matchingGroup == "one" && request.matchFormat != "one" -> OtherBandType.DUO
            else -> OtherBandType.GROUP_OF_THREE_PLUS
        }
    }
}
