package social.entourage.android.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

class CommunicationHandlerBadgeViewModel : ViewModel() {
    var badgeCount = MutableLiveData<UnreadMessages>()

    fun resetValues() {
        badgeCount.value?.unreadCount = 0
    }
}

@KSerializable
class UnreadMessages(
    @SerialName("unread_count")
    var unreadCount: Int? = null,
    @SerialName("unread_conversations_count")
    var unreadConversationsCount: Int? = null,
    @SerialName("unread_neighborhoods_count")
    var unreadNeighborhoodsCount: Int? = null)
