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
        register()
        selectedCategory = category
        onboardingShouldQuit.postValue(true)
    }

    fun toggleBtnBack(value: Boolean) {
        shouldDismissBtnBack.postValue(value)
    }

    fun register() {
        updateUserInterests { isOK, userResponse ->
            hasRegistered.postValue(isOK)
        }
    }

    fun updateUserInterests(listener: (isOK: Boolean, userResponse: UserResponse?) -> Unit) {
        val interestsList = interests.value?.filter { it.isSelected }?.map { it.id } ?: listOf()
        val categoriesList = categories.value?.filter { it.isSelected }?.map { it.id } ?: listOf()
        val actionsWishesList = actionsWishes.value?.filter { it.isSelected }?.map { it.id } ?: listOf()
        val updatedInterests = interestsList.toSet()
        val updatedConcerns = categoriesList.toSet()
        val updatedInvolvements = actionsWishesList.toSet()


        // Mettre à jour l'utilisateur localement
        user?.interests = ArrayList(updatedInterests)
        user?.concerns = ArrayList(updatedConcerns)
        user?.involvements = ArrayList(updatedInvolvements)

        // Construire la structure availability en mappant les jours et horaires
        val dayMapping = mapOf(
            "Lundi" to "1",
            "Mardi" to "2",
            "Mercredi" to "3",
            "Jeudi" to "4",
            "Vendredi" to "5",
            "Samedi" to "6",
            "Dimanche" to "7"
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

        // Préparer la requête pour le serveur
        val userMap = ArrayMap<String, Any>().apply {
            put("interests", user?.interests)
            put("concerns", user?.concerns)
            put("involvements", user?.involvements)
            put("availability", availability) // Ajout de la structure availability
        }
        val request = ArrayMap<String, Any>()
        request["user"] = userMap

        val call = onboardingService.updateUser(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                listener(response.isSuccessful, response.body())
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Timber.wtf("wtf onFailure updateUserInterests " + t.message)
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
