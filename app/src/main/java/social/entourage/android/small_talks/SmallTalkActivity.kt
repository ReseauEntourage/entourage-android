package social.entourage.android.small_talks

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

class SmallTalkActivity : BaseActivity() {

    private lateinit var binding: SmallTalkActivityBinding
    private lateinit var viewModel: SmallTalkViewModel
    private lateinit var adapter: OnboardingInterestsAdapter
    private lateinit var userPresenter: UserPresenter
    private val userSelectionsByStep: MutableMap<Int, String> = mutableMapOf()
    private var isFinished = false
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
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SmallTalkActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updatePaddingTopForEdgeToEdge(binding.root)
        viewModel = ViewModelProvider(this)[SmallTalkViewModel::class.java]
        userPresenter = UserPresenter()
        val currentUser = EntourageApplication.me(this)
        shouldAskProfilePhoto = currentUser?.avatarURL.isNullOrBlank()
        editPhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                SmallTalkingSearchingActivity.id = SMALL_TALK_REQUEST_ID
                startActivity(Intent(this, SmallTalkingSearchingActivity::class.java))
                isFinished = true
                finish()
            } else {
                // L'utilisateur a appuyé sur "Précédent" dans EditPhotoActivity : on ne fait rien.
                // Tu peux même revenir à l'étape précédente si besoin ici.
            }
        }

        setupRecyclerView()
        setupButtons()
        observeViewModel()

    }

    private fun setupRecyclerView() {
        adapter = OnboardingInterestsAdapter(
            isFromInterest = false,
            onInterestClicked = { /* handled internally */ }
        )

        binding.rvSmallTalk.apply {
            layoutManager = GridLayoutManager(this@SmallTalkActivity, 1)
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
            adapter.isFromInterestLocal = isInterestStep

            (binding.rvSmallTalk.layoutManager as? GridLayoutManager)?.spanCount = if (isInterestStep) 2 else 1

            val updatedItems = when {
                isInterestStep -> preselectUserInterests(step.items)
                else -> {
                    val restoredId = userSelectionsByStep[viewModel.currentStepIndex.value ?: 0]
                    step.items.map { it.copy(isSelected = it.id == restoredId) }
                }
            }
            // 1️⃣ Soumission d'une liste vide
            adapter.submitList(emptyList())

            // 2️⃣ Post une mise à jour un peu plus tard (après le "clear")
            binding.rvSmallTalk.post {
                adapter.submitList(updatedItems)
                binding.rvSmallTalk.scheduleLayoutAnimation()
            }
            animateProgressTo(viewModel.getStepProgress())
        }
        viewModel.shouldLeave.observe(this) { shouldLeave -> if (shouldLeave) finish() }

        viewModel.currentStepIndex.observe(this) { stepIndex ->
            binding.buttonStart.text = getString(
                if (viewModel.isLastStep()) R.string.onboarding_btn_next
                else R.string.onboarding_btn_next
            )
        }
    }

    private fun setupButtons() {
        binding.buttonStart.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.CLIC__SMALLTALK__FORMAT_NEXT)
            val selectedItem = adapter.currentList.find { it.isSelected }
            val selectedItems = adapter.currentList.filter { it.isSelected }
            val stepIndex = viewModel.currentStepIndex.value ?: 0
            val update = ArrayMap<String, Any>()

            if (!viewModel.isLastStep() && selectedItem == null) {
                Toast.makeText(this, getString(R.string.error_not_yet_implemented), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!viewModel.isLastStep() && selectedItem != null) {
                userSelectionsByStep[stepIndex] = selectedItem.id ?: ""
            }

            when (stepIndex) {
                0 -> { // Step MATCH FORMAT
                    AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__SMALLTALK__FORMAT)
                    val value = if (selectedItem?.id == "1") "one" else "many"
                    selectedRequest = selectedRequest.copy(matchFormat = value)
                    SmallTalkListOtherBands.matchingGroup = value
                    update["match_format"] = value
                }
                1 -> { // Step LOCALITY
                    AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__SMALLTALK__LOCALITY)
                    val value = selectedItem?.id == "3"
                    selectedRequest = selectedRequest.copy(matchLocality = value)
                    SmallTalkListOtherBands.matchingLocality = value
                    update["match_locality"] = value
                }
                2 -> { // Step GENDER
                    AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__SMALLTALK__GENDER)
                    val genderValue = when (selectedItem?.id) {
                        "5" -> "male"
                        "6" -> "female"
                        else -> "not_binary"
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
                3 -> { // Step MATCH GENDER
                    AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__SMALLTALK__MIXITE)
                    val matchGenderValue = selectedItem?.id == "9"
                    selectedRequest = selectedRequest.copy(matchGender = matchGenderValue)
                    SmallTalkListOtherBands.matchingGender = matchGenderValue
                    update["match_gender"] = matchGenderValue
                }
                4 -> { // Step INTERESTS
                    //update["match_interest"] = true
                    AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__SMALLTALK__INTERESTS)
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

            if (viewModel.isLastStep() && shouldAskProfilePhoto) {
                shouldAskProfilePhoto = false
                EditPhotoActivity.isFromSmallTalk = true
                editPhotoLauncher.launch(Intent(this, EditPhotoActivity::class.java))
                return@setOnClickListener
            }

            if (viewModel.isLastStep()) {
                SmallTalkingSearchingActivity.id = SMALL_TALK_REQUEST_ID
                startActivity(Intent(this, SmallTalkingSearchingActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                isFinished = true
                finish()
            } else {
                viewModel.goToNextStep()
            }
        }

        binding.buttonConfigureLater.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.CLIC__SMALLTALK__FORMAT_PREVIOUS)
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

    override fun onDestroy() {
        if(!isFinished){
            viewModel.deleteRequest()
            Log.wtf("wtf" , "request deleted" )
        }
        super.onDestroy()

    }

    private fun preselectUserInterests(interests: List<InterestForAdapter>): List<InterestForAdapter> {
        val currentUser = EntourageApplication.get(this).authenticationController.me
        val userInterests = currentUser?.interests ?: return interests

        return interests.map { interest ->
            interest.copy(isSelected = userInterests.contains(interest.id))
        }
    }

    companion object {
        var SMALL_TALK_REQUEST_ID = "12345"
    }
}
