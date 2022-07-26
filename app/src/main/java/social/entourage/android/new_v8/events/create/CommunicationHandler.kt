package social.entourage.android.new_v8.events.create

import androidx.lifecycle.MutableLiveData
import social.entourage.android.new_v8.models.Events

object CommunicationHandler {
    var clickNext = MutableLiveData<Boolean>()
    var isCondition = MutableLiveData<Boolean>()
    var isButtonClickable = MutableLiveData<Boolean>()
    var canExitEventCreation: Boolean = true

    var event: Events = Events()


    fun resetValues() {
        isCondition.value = false
        isButtonClickable.value = false
        clickNext.value = false
    }
}