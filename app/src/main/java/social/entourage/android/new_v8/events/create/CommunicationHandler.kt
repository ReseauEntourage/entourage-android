package social.entourage.android.new_v8.events.create

import androidx.lifecycle.MutableLiveData

object CommunicationHandler {
    var clickNext = MutableLiveData<Boolean>()
    var isCondition = MutableLiveData<Boolean>()
    var isButtonClickable = MutableLiveData<Boolean>()
    var canExitEventCreation: Boolean = true


    fun resetValues() {
        isCondition.value = false
        isButtonClickable.value = false
        clickNext.value = false
    }
}