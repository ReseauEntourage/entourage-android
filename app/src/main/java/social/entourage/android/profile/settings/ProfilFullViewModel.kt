package social.entourage.android.profile.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfilFullViewModel: ViewModel() {

    var hasToUpdate = MutableLiveData<Boolean>()

    fun updateProfile() {
        hasToUpdate.postValue(true)
    }

}