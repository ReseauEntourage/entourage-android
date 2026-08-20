package social.entourage.android.events.list

import social.entourage.android.api.model.Events

class SectionHeader(childList: List<Events>, sectionText: String) {
    var childList: List<Events>
    var sectionText: String

    init {
        this.childList = childList
        this.sectionText = sectionText
    }
}
