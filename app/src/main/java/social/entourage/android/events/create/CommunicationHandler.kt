package social.entourage.android.events.create

import androidx.lifecycle.MutableLiveData
import social.entourage.android.api.model.Events

object CommunicationHandler {
    var clickNext = MutableLiveData<Boolean>()
    var isCondition = MutableLiveData<Boolean>()
    var isButtonClickable = MutableLiveData<Boolean>()
    var canExitEventCreation: Boolean = true

    var event: CreateEvent = CreateEvent()
    var eventEdited: Events? = null

    fun resetValues() {
        isCondition.value = false
        isButtonClickable.value = false
        clickNext.value = false
    }
}