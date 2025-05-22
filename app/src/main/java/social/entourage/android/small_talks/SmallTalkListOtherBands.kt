package social.entourage.android.small_talks

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.MainActivity
import social.entourage.android.api.model.UserSmallTalkRequest
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.SmallTalkOtherBandsBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

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
        updatePaddingTopForEdgeToEdge(binding.root)

        viewModel = ViewModelProvider(this)[SmallTalkViewModel::class.java]

        /* ──────────────── OBSERVERS ──────────────── */

        // 1️⃣ Résultat d’un force-match ── configuré AVANT d’appeler l’API
        viewModel.matchResult.observe(this) { result ->
            if (result?.match == true && result.smalltalkId != null) {
                val intent = Intent(this, DetailConversationActivity::class.java).apply {
                    DetailConversationActivity.isSmallTalkMode = true
                    DetailConversationActivity.smallTalkId = result.smalltalkId.toString()
                }
                startActivity(intent)
                finish()
            }
        }

        // 2️⃣ Liste des demandes « presque matchées »
        observeAlmostMatches()

        // 3️⃣ Lancement de la requête réseau
        viewModel.listAlmostMatches()

        /* ──────────────── BOUTON « Je patiente » ──────────────── */
        binding.buttonWait.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            startActivity(intent)
            finish()
        }

        binding.title.setOnClickListener {
            startActivity(Intent(this, SmallTalkNoBandFound::class.java))
            finish()
        }
    }

    /** Observe la liste des demandes avec un (presque) match. */
    private fun observeAlmostMatches() {
        viewModel.almostMatches.observe(this) { userRequests ->
            if (userRequests.isEmpty()) {
                startActivity(Intent(this, SmallTalkNoBandFound::class.java))
                finish()
                return@observe
            }

            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(this@SmallTalkListOtherBands)
                adapter = OtherBandsAdapter(userRequests) { selected ->
                    val smallTalk = selected.smallTalk
                    if (smallTalk?.id != null) {
                        // ✅ SmallTalk déjà créé : on entre dans la conversation
                        val intent = Intent(
                            this@SmallTalkListOtherBands,
                            DetailConversationActivity::class.java
                        ).apply {
                            DetailConversationActivity.isSmallTalkMode = true
                            DetailConversationActivity.smallTalkId = smallTalk.id.toString()
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        // 🚀 Pas encore de smallTalk → on force le match
                        viewModel.forceMatchRequest(selected.id.toString())
                    }
                }
            }
        }
    }

    /** Détermine la (première) raison pour laquelle la demande ne matche pas. */
    private fun determineMismatchType(request: UserSmallTalkRequest): OtherBandType = when {
        matchingLocality && request.matchLocality == false ->
            OtherBandType.DIFFERENT_LOCATION
        matchingGender && request.matchGender == false   ->
            OtherBandType.DIFFERENT_INTERESTS
        matchingGroup == "one" && request.matchFormat != "one" ->
            OtherBandType.DUO
        else -> OtherBandType.GROUP_OF_THREE_PLUS
    }
}
