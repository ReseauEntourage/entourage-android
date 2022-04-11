package social.entourage.android.new_v8.group

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ErrorHandlerViewModel : ViewModel() {
    val onClickNext = MutableLiveData<Boolean>()
    val isTextOk = MutableLiveData<Boolean>()
}