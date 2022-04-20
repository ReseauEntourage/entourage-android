package social.entourage.android.new_v8.group

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ErrorHandlerViewModel : ViewModel() {
    val clickNext = MutableLiveData<Boolean>()
    val isCondition = MutableLiveData<Boolean>()
    val isButtonClickable = MutableLiveData<Boolean>()
}