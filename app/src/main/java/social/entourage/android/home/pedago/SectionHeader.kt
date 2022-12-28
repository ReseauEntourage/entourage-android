package social.entourage.android.home.pedago

import com.intrusoft.sectionedrecyclerview.Section
import social.entourage.android.api.model.Pedago

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