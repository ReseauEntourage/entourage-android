package social.entourage.android.groups.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import social.entourage.android.api.model.Group

class CommunicationHandlerViewModel : ViewModel() {
    var clickNext = MutableLiveData<Boolean>()
    var isCondition = MutableLiveData<Boolean>()
    var isButtonClickable = MutableLiveData<Boolean>()
    var canExitGroupCreation: Boolean = true

    var group: Group = Group()

    fun resetValues() {
        isCondition.value = false
        isButtonClickable.value = false
        clickNext.value = false
    }
}