package social.entourage.android.small_talks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import social.entourage.android.R
import social.entourage.android.enhanced_onboarding.InterestForAdapter

class SmallTalkViewModel(application: Application) : AndroidViewModel(application) {

    data class SmallTalkStep(
        val title: String,
        val subtitle: String,
        val items: List<InterestForAdapter>
    )

    private val context = getApplication<Application>().applicationContext

    private val steps = listOf(
        SmallTalkStep(
            title = context.getString(R.string.small_talk_step_title_1),
            subtitle = context.getString(R.string.small_talk_step_subtitle_1),
            items = listOf(
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_autre,
                    title = context.getString(R.string.small_talk_step1_item1_title),
                    subtitle = context.getString(R.string.small_talk_step1_item1_subtitle),
                    isSelected = false,
                    id = "1"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_autre,
                    title = context.getString(R.string.small_talk_step1_item2_title),
                    subtitle = context.getString(R.string.small_talk_step1_item2_subtitle),
                    isSelected = false,
                    id = "2"
                )
            )
        ),
        SmallTalkStep(
            title = context.getString(R.string.small_talk_step_title_2),
            subtitle = context.getString(R.string.small_talk_step_subtitle_2),
            items = listOf(
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_autre,
                    title = context.getString(R.string.small_talk_step2_item1_title),
                    subtitle = context.getString(R.string.small_talk_step2_item1_subtitle),
                    isSelected = false,
                    id = "3"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_autre,
                    title = context.getString(R.string.small_talk_step2_item2_title),
                    subtitle = context.getString(R.string.small_talk_step2_item2_subtitle),
                    isSelected = false,
                    id = "4"
                )
            )
        ),
        SmallTalkStep(
            title = context.getString(R.string.small_talk_step_title_3),
            subtitle = context.getString(R.string.small_talk_step_subtitle_3),
            items = listOf(
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_autre,
                    title = context.getString(R.string.small_talk_step3_item1_title),
                    subtitle = "",
                    isSelected = false,
                    id = "5"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_autre,
                    title = context.getString(R.string.small_talk_step3_item2_title),
                    subtitle = "",
                    isSelected = false,
                    id = "6"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_autre,
                    title = context.getString(R.string.small_talk_step3_item3_title),
                    subtitle = "",
                    isSelected = false,
                    id = "7"
                )
            )
        ),
        SmallTalkStep(
            title = context.getString(R.string.small_talk_step_title_4),
            subtitle = context.getString(R.string.small_talk_step_subtitle_4),
            items = listOf(
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_autre,
                    title = context.getString(R.string.small_talk_step4_item1_title),
                    subtitle = context.getString(R.string.small_talk_step4_item1_subtitle),
                    isSelected = false,
                    id = "8"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_autre,
                    title = context.getString(R.string.small_talk_step4_item2_title),
                    subtitle = context.getString(R.string.small_talk_step4_item2_subtitle),
                    isSelected = false,
                    id = "9"
                )
            )
        ),
        // 🆕 Étape finale : Centres d'intérêt
        SmallTalkStep(
            title = context.getString(R.string.onboarding_interest_title),
            subtitle = context.getString(R.string.onboarding_interest_content),
            items = listOf(
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_sport,
                    title = context.getString(R.string.interest_sport),
                    subtitle = "",
                    isSelected = false,
                    id = "sport"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_animaux,
                    title = context.getString(R.string.interest_animaux),
                    subtitle = "",
                    isSelected = false,
                    id = "animaux"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_rencontre_nomade,
                    title = context.getString(R.string.interest_marauding),
                    subtitle = "",
                    isSelected = false,
                    id = "marauding"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_bien_etre,
                    title = context.getString(R.string.interest_bien_etre),
                    subtitle = "",
                    isSelected = false,
                    id = "bien-etre"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_cuisine,
                    title = context.getString(R.string.interest_cuisine),
                    subtitle = "",
                    isSelected = false,
                    id = "cuisine"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_art,
                    title = context.getString(R.string.interest_culture),
                    subtitle = "",
                    isSelected = false,
                    id = "culture"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_nature,
                    title = context.getString(R.string.interest_nature),
                    subtitle = "",
                    isSelected = false,
                    id = "nature"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_jeux,
                    title = context.getString(R.string.interest_jeux),
                    subtitle = "",
                    isSelected = false,
                    id = "jeux"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_activite_manuelle,
                    title = context.getString(R.string.interest_activites_onboarding),
                    subtitle = "",
                    isSelected = false,
                    id = "activites"
                ),
                InterestForAdapter(
                    icon = R.drawable.ic_onboarding_interest_name_autre,
                    title = context.getString(R.string.interest_other),
                    subtitle = "",
                    isSelected = false,
                    id = "other"
                )
            )
        )
    )

    private val _currentStepIndex = MutableLiveData(0)
    val currentStepIndex: LiveData<Int> = _currentStepIndex

    private val _currentStep = MutableLiveData(steps[0])
    val currentStep: LiveData<SmallTalkStep> = _currentStep

    fun goToNextStep() {
        val nextIndex = (_currentStepIndex.value ?: 0) + 1
        if (nextIndex < steps.size) {
            _currentStepIndex.value = nextIndex
            _currentStep.value = steps[nextIndex]
        }
    }

    fun goToPreviousStep() {
        val previousIndex = (_currentStepIndex.value ?: 0) - 1
        if (previousIndex >= 0) {
            _currentStepIndex.value = previousIndex
            _currentStep.value = steps[previousIndex]
        }
    }

    fun getStepProgress(): Float {
        return ((_currentStepIndex.value ?: 0) + 1).toFloat() / steps.size.toFloat()
    }

    fun isLastStep(): Boolean {
        return (_currentStepIndex.value ?: 0) == steps.lastIndex
    }
}
