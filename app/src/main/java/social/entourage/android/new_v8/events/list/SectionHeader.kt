package social.entourage.android.new_v8.events.list

import com.intrusoft.sectionedrecyclerview.Section
import social.entourage.android.new_v8.models.Events

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