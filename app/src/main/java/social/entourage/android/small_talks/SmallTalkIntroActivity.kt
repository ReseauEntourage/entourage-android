package social.entourage.android.small_talks

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import social.entourage.android.R
import social.entourage.android.api.model.UserSmallTalkRequest
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.SmallTalkIntroActivityBinding
import social.entourage.android.tools.utils.CustomTypefaceSpan
import social.entourage.android.tools.utils.Utils.showToast
import timber.log.Timber

class SmallTalkIntroActivity : BaseActivity() {

    private lateinit var binding: SmallTalkIntroActivityBinding
    private lateinit var viewModel: SmallTalkViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SmallTalkIntroActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SmallTalkViewModel::class.java]

        applyFormattedIntroText()

        // Observe la liste des requêtes existantes
        viewModel.userRequests.observe(this) { requests ->

            val nbMatches = requests.count { it.smalltalkId != null }
            val hasPendingRequest = requests.any { it.smalltalkId == null }

            binding.startButton.setOnClickListener {
                if (nbMatches >= 3 || hasPendingRequest) {
                    showToast(this, getString(R.string.smalltalk_intro_limit))
                    return@setOnClickListener
                }

                // Crée une nouvelle requête avec des valeurs par défaut
                val newRequest = UserSmallTalkRequest(
                    matchFormat = "one",
                    matchLocality = false,
                    matchGender = false,
                    userGender = "not_defined",

                )
                viewModel.createRequest(newRequest)
            }
        }

        // Observe la requête créée (réponse de createRequest)
        viewModel.userRequest.observe(this) { request ->
            if (request?.id != null) {
                SmallTalkActivity.SMALL_TALK_REQUEST_ID = request.id.toString()
                val intent = Intent(this, SmallTalkActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                this.finish()
            } else {
                showToast(this, getString(R.string.error_not_yet_implemented))
            }
        }

        // Bouton retour
        binding.endButton.setOnClickListener {
            finish()
        }

        // Déclenche initialement le chargement
        viewModel.listUserRequests()
    }

    private fun applyFormattedIntroText() {
        val rawText = getString(R.string.onboarding_intro_raw)
        val spannable = SpannableStringBuilder(rawText)

        val quicksandBold = ResourcesCompat.getFont(this, R.font.quicksand_bold)
        val nunitoRegular = ResourcesCompat.getFont(this, R.font.nunitosans_regular)

        // Liste des parties à mettre en gras
        val toBold = listOf(
            "Bonnes ondes",
            "1. Vous répondez à quelques questions",
            "2. On vous met en relation avec des personnes",
            "3. Une conversation de groupe se crée automatiquement dans l'application",
            "4. Si vous le souhaitez, vous choisissez un moment pour vous rencontrer"
        )

        // Applique le gras aux parties définies
        toBold.forEach { phrase ->
            val start = rawText.indexOf(phrase)
            if (start >= 0) {
                val end = start + phrase.length
                quicksandBold?.let {
                    spannable.setSpan(CustomTypefaceSpan(it), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                } ?: run {
                    spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        // Applique la font par défaut au reste du texte
        nunitoRegular?.let {
            spannable.setSpan(CustomTypefaceSpan(it), 0, spannable.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }

        binding.descriptionText.text = spannable
    }


}
