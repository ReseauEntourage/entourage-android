package social.entourage.android.enhanced_onboarding

import android.util.Log
import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.api.request.UserRequest
import social.entourage.android.api.request.UserResponse
import timber.log.Timber

class OnboardingViewModel() : ViewModel() {
    var onboardingFirstStep = MutableLiveData<Boolean>()
    var onboardingSecondStep = MutableLiveData<Boolean>()
    var onboardingThirdStep = MutableLiveData<Boolean>()
    var onboardingFourthStep = MutableLiveData<Boolean>()
    var onboardingFifthStep = MutableLiveData<Boolean>()
    var onboardingDisponibilityStep = MutableLiveData<Boolean>()

    var onboardingShouldQuit = MutableLiveData<Boolean>()
    var hasRegistered = MutableLiveData<Boolean>()
    var step : Int = 1
    var interests = MutableLiveData<List<InterestForAdapter>>()
    var categories = MutableLiveData<List<InterestForAdapter>>()
    var actionsWishes = MutableLiveData<List<InterestForAdapter>>()
    var shouldDismissBtnBack = MutableLiveData<Boolean>()
    var user: User? = null
    var selectedCategory: String? = null

    // Ajout des propriétés pour les jours et tranches horaires sélectionnés
    var selectedDays = MutableLiveData<List<String>>()
    var selectedTimeSlots = MutableLiveData<List<String>>()

    private val onboardingService: UserRequest
        get() = EntourageApplication.get().apiModule.userRequest

    fun registerAndQuit(category: String? = null) {
        register { isOK ->
            if (isOK) {
                selectedCategory = category
                onboardingShouldQuit.postValue(true)
            }
            else {
                // Gérer le cas d'erreur si besoin
            }
        }
    }
    fun register(onRegisterComplete: (Boolean) -> Unit = {}) {
        updateUserInterests { isOK, userResponse ->
            hasRegistered.postValue(isOK)
            onRegisterComplete(isOK)
        }
    }
    fun toggleBtnBack(value: Boolean) {
        shouldDismissBtnBack.postValue(value)
    }
    fun updateUserInterests(listener: (isOK: Boolean, userResponse: UserResponse?) -> Unit) {
        // Mapping des jours et horaires
        val dayMapping = mapOf(
            "Lundi" to "1", "Mardi" to "2", "Mercredi" to "3",
            "Jeudi" to "4", "Vendredi" to "5", "Samedi" to "6", "Dimanche" to "7"
        )
        val timeSlotMapping = mapOf(
            "Matin" to "09:00-12:00",
            "Après-midi" to "14:00-18:00",
            "Soir" to "18:00-21:00"
        )

        val availability = selectedDays.value?.associate { day ->
            val dayNumber = dayMapping[day]
            val timeRanges = selectedTimeSlots.value?.mapNotNull { timeSlotMapping[it] } ?: listOf()
            dayNumber!! to timeRanges
        } ?: emptyMap()

        // Fallback sur les données existantes si non modifiées
        val finalInterests = interests.value?.filter { it.isSelected }?.map { it.id }
            ?: user?.interests ?: emptyList()

        val finalConcerns = categories.value?.filter { it.isSelected }?.map { it.id }
            ?: user?.concerns ?: emptyList()

        val finalInvolvements = actionsWishes.value?.filter { it.isSelected }?.map { it.id }
            ?: user?.involvements ?: emptyList()

        // Mise à jour locale de l'objet User
        user?.apply {
            interests = ArrayList(finalInterests.toSet())
            concerns = ArrayList(finalConcerns.toSet())
            involvements = ArrayList(finalInvolvements.toSet())
        }

        // Construction de la requête JSON
        val userMap = ArrayMap<String, Any>().apply {
            if (EnhancedOnboarding.isFromSettingsinterest || !EnhancedOnboarding.isFromSettingsDisponibility) {
                put("interests", finalInterests)
            }
            if (EnhancedOnboarding.isFromSettingsActionCategorie || !EnhancedOnboarding.isFromSettingsDisponibility) {
                put("concerns", finalConcerns)
            }
            if (EnhancedOnboarding.isFromSettingsWishes || !EnhancedOnboarding.isFromSettingsDisponibility) {
                put("involvements", finalInvolvements)
            }
            if (availability.isNotEmpty()) {
                put("availability", availability)
            }
        }

        val request = ArrayMap<String, Any>().apply {
            put("user", userMap)
        }

        onboardingService.updateUser(request).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                listener(response.isSuccessful, response.body())
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Timber.wtf("updateUserInterests failed: ${t.message}")
                listener(false, null)
            }
        })
    }




    fun setcategories(categoryList: List<InterestForAdapter>) {
        categories.postValue(categoryList)
    }


    fun setInterests(interestsList: List<InterestForAdapter>) {
        interests.postValue(interestsList)
    }
    fun completeDisponibilityStep() {
        onboardingDisponibilityStep.postValue(true)
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

    // Méthodes pour mettre à jour les jours et tranches horaires sélectionnés
    fun updateSelectedDays(days: List<String>) {
        selectedDays.postValue(days)
    }

    fun updateSelectedTimeSlots(timeSlots: List<String>) {
        selectedTimeSlots.postValue(timeSlots)
    }

    fun setOnboardingFirstStep(value: Boolean) {
        onboardingFirstStep.postValue(value)
        step = 1
    }

    fun setOnboardingSecondStep(value: Boolean) {
        onboardingSecondStep.postValue(value)
        step = 2
    }

    fun setOnboardingThirdStep(value: Boolean) {
        onboardingThirdStep.postValue(value)
        step = 3
    }

    fun setOnboardingFourthStep(value: Boolean) {
        onboardingFourthStep.postValue(value)
        step = 4
    }

    fun setOnboardingFifthStep(value: Boolean) {
        onboardingFifthStep.postValue(value)
        step = 5
    }
}
