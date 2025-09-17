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
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.SmallTalkIntroActivityBinding
import social.entourage.android.tools.utils.CustomTypefaceSpan

class SmallTalkIntroActivity : BaseActivity() {

    private lateinit var binding: SmallTalkIntroActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SmallTalkIntroActivityBinding.inflate(layoutInflater)
        applyFormattedIntroText()
        binding.startButton.setOnClickListener {
            val intent = Intent(this, SmallTalkActivity::class.java)
            startActivity(intent)
            this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        binding.endButton.setOnClickListener {
            finish()
        }
        setContentView(binding.root)

    }

    private fun applyFormattedIntroText() {
        val rawHtml = getString(R.string.onboarding_intro_html)
        val spanned = HtmlCompat.fromHtml(rawHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val spannable = SpannableStringBuilder(spanned)

        val quicksandBold = ResourcesCompat.getFont(this, R.font.quicksand_bold)
        val nunitoRegular = ResourcesCompat.getFont(this, R.font.nunitosans_regular)

        // Supprimer tous les TypefaceSpan "par défaut"
        spannable.getSpans(0, spannable.length, TypefaceSpan::class.java).forEach { span ->
            spannable.removeSpan(span)
        }

        // Remplacer tous les StyleSpan(BOLD) par ton CustomTypefaceSpan(quicksand_bold)
        spannable.getSpans(0, spannable.length, android.text.style.StyleSpan::class.java).forEach { span ->
            if (span.style == Typeface.BOLD) {
                val start = spannable.getSpanStart(span)
                val end = spannable.getSpanEnd(span)
                spannable.removeSpan(span)
                quicksandBold?.let {
                    spannable.setSpan(CustomTypefaceSpan(it), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        // Appliquer nunito_sans_regular à tout le texte
        nunitoRegular?.let {
            spannable.setSpan(CustomTypefaceSpan(it), 0, spannable.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }

        binding.descriptionText.text = spannable
    }




}
