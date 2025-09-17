package social.entourage.android.small_talks

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.MainActivity
import social.entourage.android.api.model.UserSmallTalkRequest
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.SmallTalkOtherBandsBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.tools.log.AnalyticsEvents
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
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__SMALLTALK__SUGGESTIONS)

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
            AnalyticsEvents.logEvent(AnalyticsEvents.CLIC__SMALLTALK__SUGGESTIONS_WAIT)
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            startActivity(intent)
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
                adapter = OtherBandsAdapter(userRequests) { request ->
                    // 👉 Toujours un forceMatch AVANT d’entrer dans DetailConversationActivity
                    viewModel.forceMatchRequest(
                        smallTalkId = request.userSmallTalkId
                    )
                }
            }
        }

        // ⚡️ Résultat du forceMatch (qu’il vienne d’un userRequestId ou d’un smalltalkId)
        viewModel.matchResult.observe(this) { result ->
            if (result?.match == true && result.smalltalkId != null) {
                AnalyticsEvents.logEvent(AnalyticsEvents.CLIC__SMALLTALK__SUGGESTIONS_JOIN)
                val intent = Intent(this, DetailConversationActivity::class.java).apply {
                    DetailConversationActivity.isSmallTalkMode = true
                    DetailConversationActivity.smallTalkId = result.smalltalkId.toString()
                }
                startActivity(intent)
                finish()
            }else{
                //toast error
                Toast.makeText(this, "Erreur lors du match", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
