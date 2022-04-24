package social.entourage.android.new_v8.group

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CommunicationHandlerViewModel : ViewModel() {
    var clickNextStepOne = MutableLiveData<Boolean>()
    var isConditionStepOne = MutableLiveData<Boolean>()
    var isButtonClickableStepOne = MutableLiveData<Boolean>()

    var clickNextStepTwo = MutableLiveData<Boolean>()
    var isConditionStepTwo = MutableLiveData<Boolean>()
    var isButtonClickableStepTwo = MutableLiveData<Boolean>()


    fun resetStepOne() {
        isConditionStepOne.value = false
        isButtonClickableStepOne.value = false
        clickNextStepOne.value = false
    }

    fun resetStepTwo() {
        isConditionStepTwo.value = false
        isButtonClickableStepTwo.value = false
        clickNextStepTwo.value = false
    }
}