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

class OnboardingViewModel : ViewModel() {
    var onboardingFirstStep = MutableLiveData<Boolean>()
    var onboardingSecondStep = MutableLiveData<Boolean>()
    var onboardingThirdStep = MutableLiveData<Boolean>()
    var onboardingFourthStep = MutableLiveData<Boolean>()
    var onboardingFifthStep = MutableLiveData<Boolean>()
    var onboardingShouldQuit = MutableLiveData<Boolean>()
    var hasRegistered = MutableLiveData<Boolean>()
    var interests = MutableLiveData<List<InterestForAdapter>>()
    var categories = MutableLiveData<List<InterestForAdapter>>()
    var actionsWishes = MutableLiveData<List<InterestForAdapter>>()
    var shouldDismissBtnBack = MutableLiveData<Boolean>()
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
        // Assure-toi que ces MutableLiveData ont déjà été initialisés et contiennent des données valides.
        val interestsList = interests.value?.filter { it.isSelected }?.map { it.id } ?: listOf()
        val categoriesList = categories.value?.filter { it.isSelected }?.map { it.id } ?: listOf()
        val actionsWishesList = actionsWishes.value?.filter { it.isSelected }?.map { it.id } ?: listOf()

        val user = ArrayMap<String, Any>().apply {
            put("interests", interestsList)
            put("concerns", categoriesList)
            put("involvements", actionsWishesList)
        }

        val request = ArrayMap<String, Any>()
        request["user"] = user
        val call = onboardingService.updateUser(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
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
        Log.wtf("wtf", "setOnboardingFirstStep")
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