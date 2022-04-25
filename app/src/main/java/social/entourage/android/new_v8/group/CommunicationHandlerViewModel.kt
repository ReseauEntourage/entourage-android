package social.entourage.android.new_v8.group

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import social.entourage.android.new_v8.models.Group

class CommunicationHandlerViewModel : ViewModel() {
    var clickNext = MutableLiveData<Boolean>()
    var isCondition = MutableLiveData<Boolean>()
    var isButtonClickable = MutableLiveData<Boolean>()

    var group: Group = Group()

    fun resetStepOne() {
        isCondition.value = false
        isButtonClickable.value = false
        clickNext.value = false
    }
}