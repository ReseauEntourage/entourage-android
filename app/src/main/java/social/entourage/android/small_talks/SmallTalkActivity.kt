package social.entourage.android.small_talks

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.collection.ArrayMap
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.api.model.UserSmallTalkRequest
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.SmallTalkActivityBinding
import social.entourage.android.enhanced_onboarding.InterestForAdapter
import social.entourage.android.enhanced_onboarding.fragments.OnboardingInterestsAdapter

class SmallTalkActivity : BaseActivity() {

    private lateinit var binding: SmallTalkActivityBinding
    private lateinit var viewModel: SmallTalkViewModel
    private lateinit var adapter: OnboardingInterestsAdapter

    private var selectedRequest = UserSmallTalkRequest(
        id = SMALL_TALK_REQUEST_ID.toIntOrNull(),
        uuid = null,
        smalltalkId = null,
        matchFormat = "",
        matchLocality = false,
        matchGender = false,
        userGender = "",
        matchInterest = false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SmallTalkActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[SmallTalkViewModel::class.java]

        setupRecyclerView()
        setupButtons()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = OnboardingInterestsAdapter(
            context = this,
            isFromInterest = false,
            interests = emptyList(),
            onInterestClicked = { /* handled by adapter internally */ }
        )

        binding.rvSmallTalk.apply {
            layoutManager = LinearLayoutManager(this@SmallTalkActivity)
            adapter = this@SmallTalkActivity.adapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_anim_fade_in)
            itemAnimator = DefaultItemAnimator().apply {
                addDuration = 200
                removeDuration = 150
            }
        }
    }

    private fun observeViewModel() {
        viewModel.currentStep.observe(this) { step ->
            binding.title.text = step.title
            binding.subtitle.text = step.subtitle

            val isInterestStep = viewModel.isLastStep()
            adapter.forceSingleSelectionForSmallTalk = !isInterestStep

            val newLayoutManager = if (isInterestStep) {
                GridLayoutManager(this, 2)
            } else {
                LinearLayoutManager(this)
            }

            binding.rvSmallTalk.suppressLayout(true)
            if (binding.rvSmallTalk.layoutManager?.javaClass != newLayoutManager.javaClass) {
                binding.rvSmallTalk.layoutManager = newLayoutManager
            }
            binding.rvSmallTalk.suppressLayout(false)

            adapter.interests = step.items
            adapter.notifyDataSetChanged()
            binding.rvSmallTalk.scheduleLayoutAnimation()

            animateProgressTo(viewModel.getStepProgress())
        }

        viewModel.currentStepIndex.observe(this) { stepIndex ->
            binding.buttonStart.text = getString(
                if (viewModel.isLastStep()) R.string.onboarding_btn_finish
                else R.string.onboarding_btn_next
            )
        }
    }

    private fun setupButtons() {
        binding.buttonStart.setOnClickListener {
            val selectedItem = adapter.interests.find { it.isSelected }
            val stepIndex = viewModel.currentStepIndex.value ?: 0
            val update = ArrayMap<String, Any>()

            if (!viewModel.isLastStep() && selectedItem == null) {
                Toast.makeText(this, getString(R.string.error_not_yet_implemented), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (stepIndex) {
                0 -> {
                    val value = if (selectedItem?.id == "1") "one" else "many"
                    selectedRequest = selectedRequest.copy(matchFormat = value)
                    update["match_format"] = value
                }
                1 -> {
                    val value = selectedItem?.id == "3"
                    selectedRequest = selectedRequest.copy(matchLocality = value)
                    update["match_locality"] = value
                }
                2 -> {
                    val genderValue = when (selectedItem?.id) {
                        "5" -> "male"
                        "6" -> "female"
                        else -> "not_defined"
                    }
                    selectedRequest = selectedRequest.copy(userGender = genderValue)
                    update["user_gender"] = genderValue
                }
                3 -> {
                    selectedRequest = selectedRequest.copy(matchInterest = true)
                    update["match_interest"] = true
                }
            }

            if (update.isNotEmpty()) {
                viewModel.updateRequest(SMALL_TALK_REQUEST_ID, update)
            }

            if (viewModel.isLastStep()) {
                SmallTalkingSearchingActivity.id = SMALL_TALK_REQUEST_ID
                startActivity(Intent(this, SmallTalkingSearchingActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                viewModel.goToNextStep()
            }
        }

        binding.buttonConfigureLater.setOnClickListener {
            viewModel.goToPreviousStep()
        }
    }

    private fun animateProgressTo(progressRatio: Float) {
        ValueAnimator.ofInt(binding.progressBar.progress, (progressRatio * 100).toInt()).apply {
            duration = 300
            addUpdateListener { animation ->
                binding.progressBar.progress = animation.animatedValue as Int
            }
            start()
        }
    }

    companion object {
        var SMALL_TALK_REQUEST_ID = "12345" // À injecter depuis SmallTalkIntroActivity
    }
}
