package social.entourage.android.groups.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.flexbox.FlexboxLayout
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityGroupPresentationBinding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.tools.utils.px

/**
 * Parcours « Me présenter au groupe », en trois étapes :
 * 1. ce que l'on aime (centres d'intérêt du profil, ou renvoi vers l'onboarding enrichi si vide),
 * 2. pourquoi on rejoint le groupe (parler, trouver des événements, entraide),
 * 3. aperçu du message généré, modifiable, puis publication dans le groupe.
 */
class GroupPresentationActivity : BaseActivity() {

    private lateinit var binding: ActivityGroupPresentationBinding
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

    private var groupId = -1
    private var groupName: String = ""
    private var step = STEP_PROFILE
    private val selectedIntents = linkedSetOf<GroupPresentationComposer.Intent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGroupPresentationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getIntExtra(EXTRA_GROUP_ID, -1)
        groupName = intent.getStringExtra(EXTRA_GROUP_NAME).orEmpty()

        ViewCompat.setOnApplyWindowInsetsListener(binding.presentationRoot) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.updatePadding(top = bars.top, bottom = maxOf(bars.bottom, ime.bottom))
            insets
        }

        binding.presentationClose.setOnClickListener { finish() }
        binding.completeProfile.setOnClickListener {
            startActivity(Intent(this, EnhancedOnboarding::class.java))
        }
        binding.presentationNext.setOnClickListener { onNext() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (step > STEP_PROFILE) showStep(step - 1) else finish()
            }
        })

        groupPresenter.hasPost.observe(this) { ok ->
            if (ok == true) {
                setResult(RESULT_OK)
                finish()
            } else {
                binding.presentationNext.isEnabled = true
            }
        }

        buildIntentCards()
    }

    override fun onResume() {
        super.onResume()
        // Au retour de l'onboarding enrichi, le profil a pu changer.
        if (step == STEP_PROFILE) renderProfileStep()
    }

    // ---------------------------------------------------------------- navigation

    private fun showStep(newStep: Int) {
        step = newStep
        binding.stepProfile.visibility = if (step == STEP_PROFILE) View.VISIBLE else View.GONE
        binding.stepIntents.visibility = if (step == STEP_INTENTS) View.VISIBLE else View.GONE
        binding.stepPreview.visibility = if (step == STEP_PREVIEW) View.VISIBLE else View.GONE
        binding.presentationProgress.text = getString(R.string.group_presentation_progress, step + 1, STEP_COUNT)
        when (step) {
            STEP_PROFILE -> renderProfileStep()
            STEP_INTENTS -> binding.presentationNext.text = getString(R.string.group_presentation_next)
            STEP_PREVIEW -> binding.presentationNext.text = getString(R.string.group_presentation_publish)
        }
    }

    private fun onNext() {
        when (step) {
            STEP_PROFILE -> showStep(STEP_INTENTS)
            STEP_INTENTS -> {
                binding.previewMessage.setText(composeMessage())
                showStep(STEP_PREVIEW)
            }
            STEP_PREVIEW -> publish()
        }
    }

    // ---------------------------------------------------------------- étape 1

    private fun userInterests(): List<String> {
        val me = EntourageApplication.get().me() ?: return emptyList()
        val empty = getString(R.string.empty_description)
        return me.getFormattedInterests(this)
            .split(", ")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != empty }
    }

    private fun renderProfileStep() {
        val interests = userInterests()
        val firstName = EntourageApplication.get().me()?.firstName.orEmpty()
        binding.presentationProgress.text = getString(R.string.group_presentation_progress, STEP_PROFILE + 1, STEP_COUNT)
        binding.stepProfileTitle.text =
            if (firstName.isBlank()) getString(R.string.group_presentation_profile_title_no_name)
            else getString(R.string.group_presentation_profile_title, firstName)
        binding.interestsContainer.removeAllViews()
        if (interests.isEmpty()) {
            binding.stepProfileSubtitle.setText(R.string.group_presentation_profile_empty)
            binding.completeProfile.visibility = View.VISIBLE
            binding.presentationNext.setText(R.string.group_presentation_skip)
        } else {
            binding.stepProfileSubtitle.text = getString(R.string.group_presentation_profile_subtitle, groupName)
            binding.completeProfile.visibility = View.GONE
            interests.forEach { binding.interestsContainer.addView(chip(it, selected = true)) }
            binding.presentationNext.setText(R.string.group_presentation_next)
        }
    }

    private fun chip(label: String, selected: Boolean): TextView = TextView(this).apply {
        text = label
        textSize = 14f
        typeface = ResourcesCompat.getFont(this@GroupPresentationActivity, R.font.nunitosans_semibold)
        setTextColor(ContextCompat.getColor(this@GroupPresentationActivity, R.color.black))
        setBackgroundResource(
            if (selected) R.drawable.bg_chip_conversation_suggestion_selected
            else R.drawable.bg_chip_conversation_suggestion
        )
        setPadding(16.px, 9.px, 16.px, 9.px)
        layoutParams = FlexboxLayout.LayoutParams(
            FlexboxLayout.LayoutParams.WRAP_CONTENT,
            FlexboxLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            marginEnd = 8.px
            bottomMargin = 8.px
        }
    }

    // ---------------------------------------------------------------- étape 2

    private fun buildIntentCards() {
        val options = listOf(
            GroupPresentationComposer.Intent.TALK to R.string.group_presentation_intent_talk,
            GroupPresentationComposer.Intent.EVENTS to R.string.group_presentation_intent_events,
            GroupPresentationComposer.Intent.HELP to R.string.group_presentation_intent_help,
        )
        options.forEach { (intentType, labelRes) ->
            val card = chip(getString(labelRes), selected = false).apply {
                textSize = 16f
                setPadding(20.px, 16.px, 20.px, 16.px)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 10.px }
                setOnClickListener {
                    val selected = if (selectedIntents.contains(intentType)) {
                        selectedIntents.remove(intentType)
                        false
                    } else {
                        selectedIntents.add(intentType)
                        true
                    }
                    setBackgroundResource(
                        if (selected) R.drawable.bg_chip_conversation_suggestion_selected
                        else R.drawable.bg_chip_conversation_suggestion
                    )
                }
            }
            binding.intentsContainer.addView(card)
        }
    }

    // ---------------------------------------------------------------- étape 3

    private fun composeMessage(): String = GroupPresentationComposer.compose(
        GroupPresentationComposer.Strings(
            greeting = getString(R.string.group_presentation_msg_greeting, groupName),
            introWithName = { name -> getString(R.string.group_presentation_msg_intro_name, name) },
            interestsLine = { list -> getString(R.string.group_presentation_msg_interests, list) },
            aboutLine = { text -> getString(R.string.group_presentation_msg_about, text) },
            talk = getString(R.string.group_presentation_msg_talk),
            events = getString(R.string.group_presentation_msg_events),
            help = getString(R.string.group_presentation_msg_help),
            intentsIntro = getString(R.string.group_presentation_msg_intents_intro),
            closing = getString(R.string.group_presentation_msg_closing),
        ),
        firstName = EntourageApplication.get().me()?.firstName,
        interests = userInterests(),
        about = binding.aboutInput.text?.toString(),
        intents = selectedIntents,
    )

    private fun publish() {
        val text = binding.previewMessage.text
        if (text.isNullOrBlank()) {
            binding.previewMessage.error = getString(R.string.group_presentation_empty_error)
            return
        }
        binding.presentationNext.isEnabled = false
        val html = Html.toHtml(SpannableString(text), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
        val chat = ArrayMap<String, Any>().apply { put("content", html) }
        val request = ArrayMap<String, Any>().apply { put("chat_message", chat) }
        groupPresenter.addPost(groupId, request)
    }

    companion object {
        const val EXTRA_GROUP_ID = "presentation_group_id"
        const val EXTRA_GROUP_NAME = "presentation_group_name"
        private const val STEP_PROFILE = 0
        private const val STEP_INTENTS = 1
        private const val STEP_PREVIEW = 2
        private const val STEP_COUNT = 3

        fun intent(context: Context, groupId: Int, groupName: String?) =
            Intent(context, GroupPresentationActivity::class.java)
                .putExtra(EXTRA_GROUP_ID, groupId)
                .putExtra(EXTRA_GROUP_NAME, groupName.orEmpty())
    }
}
