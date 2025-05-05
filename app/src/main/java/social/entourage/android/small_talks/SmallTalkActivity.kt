package social.entourage.android.small_talks

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import social.entourage.android.user.UserPresenter
import social.entourage.android.api.request.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.profile.editProfile.EditPhotoActivity

class SmallTalkActivity : BaseActivity() {

    private lateinit var binding: SmallTalkActivityBinding
    private lateinit var viewModel: SmallTalkViewModel
    private lateinit var adapter: OnboardingInterestsAdapter
    private lateinit var userPresenter: UserPresenter

    private var shouldAskProfilePhoto = false
    private lateinit var editPhotoLauncher: ActivityResultLauncher<Intent>

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
        userPresenter = UserPresenter()

        // 🔥 Check if the user needs to upload a profile photo
        val currentUser = EntourageApplication.me(this)
        shouldAskProfilePhoto = currentUser?.avatarURL.isNullOrBlank()

        // 🔥 Register the photo edit launcher
        editPhotoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // Peu importe résultat => on va direct à la recherche
            SmallTalkingSearchingActivity.id = SMALL_TALK_REQUEST_ID
            startActivity(Intent(this, SmallTalkingSearchingActivity::class.java))
            finish()
        }

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

            // 🔥 Preselect already chosen interests
            if (isInterestStep) {
                preselectUserInterests()
            }

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
            val selectedItems = adapter.interests.filter { it.isSelected }
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
                2 -> { // 🎯 Step GENDER
                    val genderValue = when (selectedItem?.id) {
                        "5" -> "male"
                        "6" -> "female"
                        else -> "non_binary"
                    }
                    selectedRequest = selectedRequest.copy(userGender = genderValue)
                    update["user_gender"] = genderValue

                    val userUpdate = ArrayMap<String, Any>()
                    userUpdate["gender"] = genderValue
                    val wrapper = ArrayMap<String, Any>()
                    wrapper["user"] = userUpdate
                    EntourageApplication.get().apiModule.userRequest.updateUser(wrapper).enqueue(object : Callback<UserResponse> {
                        override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {}
                        override fun onFailure(call: Call<UserResponse>, t: Throwable) {}
                    })
                }
                3 -> { // 🎯 Step INTERESTS
                    selectedRequest = selectedRequest.copy(matchInterest = true)
                    update["match_interest"] = true

                    val selectedInterestIds = selectedItems.mapNotNull { it.id }
                    val userUpdate = ArrayMap<String, Any>()
                    userUpdate["interests"] = selectedInterestIds
                    val wrapper = ArrayMap<String, Any>()
                    wrapper["user"] = userUpdate
                    EntourageApplication.get().apiModule.userRequest.updateUser(wrapper).enqueue(object : Callback<UserResponse> {
                        override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {}
                        override fun onFailure(call: Call<UserResponse>, t: Throwable) {}
                    })
                }
            }

            if (update.isNotEmpty()) {
                viewModel.updateRequest(SMALL_TALK_REQUEST_ID, update)
            }

            // 🔥 Special case: Ask photo if needed
            if (viewModel.isLastStep() && shouldAskProfilePhoto) {
                shouldAskProfilePhoto = false // Avoid infinite loop
                editPhotoLauncher.launch(Intent(this, EditPhotoActivity::class.java))
                return@setOnClickListener
            }

            if (viewModel.isLastStep()) {
                SmallTalkingSearchingActivity.id = SMALL_TALK_REQUEST_ID
                startActivity(Intent(this, SmallTalkingSearchingActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
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

    /**
     * 🔥 Preselect already chosen interests
     */
    private fun preselectUserInterests() {
        val currentUser = EntourageApplication.get(this).authenticationController.me
        val userInterests = currentUser?.interests ?: return

        adapter.interests.forEach { interest ->
            interest.isSelected = userInterests.contains(interest.id)
        }
    }

    companion object {
        var SMALL_TALK_REQUEST_ID = "12345"
    }
}
