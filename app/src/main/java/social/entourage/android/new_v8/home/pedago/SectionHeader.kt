package social.entourage.android.new_v8.home.pedago

import com.intrusoft.sectionedrecyclerview.Section
import social.entourage.android.new_v8.models.Pedago

class SectionHeader(childList: List<Pedago>, sectionText: String) :
    Section<Pedago> {
    var childList: List<Pedago>
    var sectionText: String

    override fun getChildItems(): List<Pedago> {
        return childList
    }

    init {
        this.childList = childList
        this.sectionText = sectionText
    }
}