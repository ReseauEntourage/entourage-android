package social.entourage.android.events.list

import com.intrusoft.sectionedrecyclerview.Section
import social.entourage.android.api.model.Events

class SectionHeader(childList: List<Events>, sectionText: String) :
    Section<Events> {
    var childList: List<Events>
    var sectionText: String

    override fun getChildItems(): List<Events> {
        return childList
    }

    init {
        this.childList = childList
        this.sectionText = sectionText
    }
}