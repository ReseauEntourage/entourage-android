package social.entourage.android.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.annotations.SerializedName

class CommunicationHandlerBadgeViewModel : ViewModel() {
    var badgeCount = MutableLiveData<UnreadMessages>()

    fun resetValues() {
        badgeCount.value?.unreadCount = 0
    }
}

class UnreadMessages(
    @SerializedName("unread_count")
    var unreadCount: Int? = null,
)
