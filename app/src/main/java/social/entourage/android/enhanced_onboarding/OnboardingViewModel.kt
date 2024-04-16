package social.entourage.android.enhanced_onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import social.entourage.android.R

class OnboardingViewModel : ViewModel() {
    var onboardingFirstStep = MutableLiveData<Boolean>()
    var onboardingSecondStep = MutableLiveData<Boolean>()
    var onboardingThirdStep = MutableLiveData<Boolean>()
    var onboardingFourthStep = MutableLiveData<Boolean>()
    var onboardingFifthStep = MutableLiveData<Boolean>()
    var onboardingShouldQuit = MutableLiveData<Boolean>()
    var interests = MutableLiveData<List<InterestForAdapter>>()
    var categories = MutableLiveData<List<InterestForAdapter>>()
    var actionsWishes = MutableLiveData<List<InterestForAdapter>>()


    fun registerAndQuit() {
        register()
        onboardingShouldQuit.postValue(true)
    }

    fun register() {

    }

    fun setInterests(interestsList: List<InterestForAdapter>) {
        interests.postValue(interestsList)
    }
    fun updateInterest(interest: InterestForAdapter) {
        val updatedInterests = interests.value?.map { currentInterest ->
            if (currentInterest.title == interest.title) {
                currentInterest.copy(isSelected = !currentInterest.isSelected)
            } else {
                currentInterest
            }
        }
        interests.postValue(updatedInterests ?: listOf())
    }
    fun setcategories(categoryList: List<InterestForAdapter>) {
        categories.postValue(categoryList)
    }
    fun updateCategories(interest: InterestForAdapter) {
        val updatedCategory = categories.value?.map { currentCategory ->
            if (currentCategory.title == interest.title) {
                currentCategory.copy(isSelected = !currentCategory.isSelected)
            } else {
                currentCategory
            }
        }
        categories.postValue(updatedCategory ?: listOf())
    }

    fun setActionsWishes(actionsWishesList: List<InterestForAdapter>) {
        actionsWishes.postValue(actionsWishesList)
    }
    fun updateActionsWishes(interest: InterestForAdapter) {
        val updatedActionsWishes = actionsWishes.value?.map { currentActionsWishes ->
            if (currentActionsWishes.title == interest.title) {
                currentActionsWishes.copy(isSelected = !currentActionsWishes.isSelected)
            } else {
                currentActionsWishes
            }
        }
        actionsWishes.postValue(updatedActionsWishes ?: listOf())
    }


    fun setOnboardingFirstStep(value: Boolean) {
        onboardingFirstStep.postValue(true)
    }
    fun setOnboardingSecondStep(value: Boolean) {
        onboardingSecondStep.postValue(true)
    }
    fun setOnboardingThirdStep(value: Boolean) {
        onboardingThirdStep.postValue(true)
    }
    fun setOnboardingFourthStep(value: Boolean) {
        onboardingFourthStep.postValue(true)
    }
    fun setOnboardingFifthStep(value: Boolean) {
        onboardingFifthStep.postValue(true)
    }
}