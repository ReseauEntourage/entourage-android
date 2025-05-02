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

class OnboardingViewModel() : ViewModel() {
    var onboardingFirstStep = MutableLiveData<Boolean>()
    var onboardingSecondStep = MutableLiveData<Boolean>()
    var onboardingThirdStep = MutableLiveData<Boolean>()
    var onboardingFourthStep = MutableLiveData<Boolean>()
    var onboardingFifthStep = MutableLiveData<Boolean>()
    var onboardingShouldQuit = MutableLiveData<Boolean>()
    var hasRegistered = MutableLiveData<Boolean>()
    var step : Int = 1
    var interests = MutableLiveData<List<InterestForAdapter>>()
    var categories = MutableLiveData<List<InterestForAdapter>>()
    var actionsWishes = MutableLiveData<List<InterestForAdapter>>()
    var shouldDismissBtnBack = MutableLiveData<Boolean>()
    var user:User? = null

    private val onboardingService : UserRequest
        get() =  EntourageApplication.get().apiModule.userRequest //service ?: retrofit!!.create(UserRequest::class.java)

    fun registerAndQuit() {
        register()
        onboardingShouldQuit.postValue(true)
    }

    fun toggleBtnBack(value: Boolean) {
       shouldDismissBtnBack.postValue(value)
    }

    fun register() {

        updateUserInterests { isOK, userResponse ->
            if (isOK) {
                hasRegistered.postValue(true)
            }
            else {
                hasRegistered.postValue(false)
            }
        }
    }

    fun updateUserInterests(listener: (isOK: Boolean, userResponse: UserResponse?) -> Unit) {
        // Préparez les listes à partir des choix de l'utilisateur
        val interestsList = interests.value?.filter { it.isSelected }?.map { it.id } ?: listOf()
        val categoriesList = categories.value?.filter { it.isSelected }?.map { it.id } ?: listOf()
        val actionsWishesList = actionsWishes.value?.filter { it.isSelected }?.map { it.id } ?: listOf()

        val updatedInterests = user?.interests?.toSet()?.plus(interestsList) ?: interestsList.toSet()
        val updatedConcerns = user?.concerns?.toSet()?.plus(categoriesList) ?: categoriesList.toSet()
        val updatedInvolvements = user?.involvements?.toSet()?.plus(actionsWishesList) ?: actionsWishesList.toSet()

        // Mettre à jour l'utilisateur localement
        user?.interests = ArrayList(updatedInterests)
        user?.concerns = ArrayList(updatedConcerns)
        user?.involvements = ArrayList(updatedInvolvements)

        // Préparer la requête pour le serveur
        val userMap = ArrayMap<String, Any>().apply {
            put("interests", user?.interests)
            put("concerns", user?.concerns)
            put("involvements", user?.involvements)
        }
        val request = ArrayMap<String, Any>()
        request["user"] = userMap

        val call = onboardingService.updateUser(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    // Confirmer la mise à jour sur le modèle local si nécessaire
                    listener(true, response.body())
                } else {
                    listener(false, null)
                }
            }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                listener(false, null)
            }
        })
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
        step = 1
    }
    fun setOnboardingSecondStep(value: Boolean) {
        step = 2
        onboardingSecondStep.postValue(true)
    }
    fun setOnboardingThirdStep(value: Boolean) {
        step = 3
        onboardingThirdStep.postValue(true)
    }
    fun setOnboardingFourthStep(value: Boolean) {
        step = 4
        onboardingFourthStep.postValue(true)
    }
    fun setOnboardingFifthStep(value: Boolean) {
        step = 5
        onboardingFifthStep.postValue(true)
    }

}