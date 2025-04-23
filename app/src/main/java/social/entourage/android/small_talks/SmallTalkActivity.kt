package social.entourage.android.small_talks

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.SmallTalkActivityBinding
import social.entourage.android.enhanced_onboarding.InterestForAdapter
import social.entourage.android.enhanced_onboarding.fragments.OnboardingInterestsAdapter

class SmallTalkActivity : BaseActivity() {

    private lateinit var binding: SmallTalkActivityBinding
    private lateinit var viewModel: SmallTalkViewModel
    private lateinit var adapter: OnboardingInterestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SmallTalkViewModel::class.java]
        binding = SmallTalkActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupButtons()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = OnboardingInterestsAdapter(
            context = this,
            isFromInterest = false,
            interests = emptyList()
        ) { selected ->
            selected.isSelected = !selected.isSelected
            val index = adapter.interests.indexOf(selected)
            if (index != -1) adapter.notifyItemChanged(index)
        }

        // LayoutManager initial selon la première étape
        binding.rvSmallTalk.layoutManager = LinearLayoutManager(this)

        binding.rvSmallTalk.adapter = adapter
        binding.rvSmallTalk.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_anim_fade_in)
        binding.rvSmallTalk.itemAnimator = DefaultItemAnimator().apply {
            addDuration = 200
            removeDuration = 150
        }
    }

    private fun observeViewModel() {
        viewModel.currentStep.observe(this) { step ->
            binding.title.text = step.title
            binding.subtitle.text = step.subtitle

            // Sécurise le changement de LayoutManager
            binding.rvSmallTalk.suppressLayout(true)

            val isInterestStep = viewModel.isLastStep()
            val newLayoutManager = if (isInterestStep) {
                GridLayoutManager(this, 2)
            } else {
                LinearLayoutManager(this)
            }

            if (binding.rvSmallTalk.layoutManager?.javaClass != newLayoutManager.javaClass) {
                binding.rvSmallTalk.layoutManager = newLayoutManager
            }

            binding.rvSmallTalk.suppressLayout(false)

            // Applique ton animation
            binding.rvSmallTalk.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_anim_fade_in)
            binding.rvSmallTalk.scheduleLayoutAnimation()

            // Rafraîchit les données proprement
            adapter.interests = step.items
            adapter.notifyDataSetChanged()

            // Barre de progression animée
            animateProgressTo(viewModel.getStepProgress())
        }

        viewModel.currentStepIndex.observe(this) { index ->
            val isLast = viewModel.isLastStep()
            binding.buttonStart.text = getString(
                if (isLast) R.string.onboarding_btn_finish else R.string.onboarding_btn_next
            )
        }
    }


    private fun setupButtons() {
        binding.buttonStart.setOnClickListener {
            if (viewModel.isLastStep()) {
                //If finished
                val intent = Intent(this, SmallTalkingSearchingActivity::class.java)
                startActivity(intent)
                this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                viewModel.goToNextStep()
            }
        }

        binding.buttonConfigureLater.setOnClickListener {
            viewModel.goToPreviousStep()
        }
    }

    private fun animateProgressTo(progressRatio: Float) {
        val animator = ValueAnimator.ofInt(binding.progressBar.progress, (progressRatio * 100).toInt())
        animator.duration = 300
        animator.addUpdateListener { anim ->
            binding.progressBar.progress = anim.animatedValue as Int
        }
        animator.start()
    }
}
