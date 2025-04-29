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
                    matchInterest = true
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
        val rawHtml = getString(R.string.onboarding_intro_html)
        val spanned = HtmlCompat.fromHtml(rawHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val spannable = SpannableStringBuilder(spanned)

        val quicksandBold = ResourcesCompat.getFont(this, R.font.quicksand_bold)
        val nunitoRegular = ResourcesCompat.getFont(this, R.font.nunitosans_regular)

        spannable.getSpans(0, spannable.length, TypefaceSpan::class.java).forEach { span ->
            spannable.removeSpan(span)
        }

        spannable.getSpans(0, spannable.length, StyleSpan::class.java).forEach { span ->
            if (span.style == Typeface.BOLD) {
                val start = spannable.getSpanStart(span)
                val end = spannable.getSpanEnd(span)
                spannable.removeSpan(span)
                quicksandBold?.let {
                    spannable.setSpan(CustomTypefaceSpan(it), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        nunitoRegular?.let {
            spannable.setSpan(CustomTypefaceSpan(it), 0, spannable.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }

        binding.descriptionText.text = spannable
    }
}
