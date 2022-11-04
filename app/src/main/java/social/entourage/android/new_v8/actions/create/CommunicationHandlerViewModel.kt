package social.entourage.android.new_v8.actions.create

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.new_v8.models.Action
import social.entourage.android.new_v8.models.ActionSection
import social.entourage.android.new_v8.models.Address
import social.entourage.android.new_v8.models.MetadataActionLocation

class CommunicationActionHandlerViewModel : ViewModel() {
    var clickNext = MutableLiveData<Boolean>()
    var isCondition = MutableLiveData<Boolean>()
    var isButtonClickable = MutableLiveData<Boolean>()
    var canExitActionCreation: Boolean = true

    var imageURI: Uri? = null
    var action: Action = Action()

    var sectionsList = MutableLiveData<MutableList<ActionSection>>()

    var isDemand = false

    var metadata = MutableLiveData<MetadataActionLocation>()

    var keyImageUpload:String? = null

    fun prepareCreateAction() {
        sectionsList.value?.first { it.isSelected }?.let {
            action.sectionName = it.id
        }

       if ( metadata.value?.latitude != null &&  metadata.value?.longitude != null) {
           action.location = Address()
           action.location?.latitude = metadata.value?.latitude!!
           action.location?.longitude = metadata.value?.longitude!!
       }
        action.hasConsent = true

        if (keyImageUpload != null) {
            action.imageUrl = keyImageUpload!!
        }
    }

    fun resetValues() {
        isCondition.value = false
        isButtonClickable.value = false
        clickNext.value = false
    }

    fun initSectionList() {
        sectionsList.value = ArrayList()
        MetaDataRepository.metaData.value?.sections?.let {
            for (tag in it) {
                sectionsList.value?.add(ActionSection(tag.id,tag.name,tag.subname,false))
            }
        }
    }
}

