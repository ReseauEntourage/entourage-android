package social.entourage.android.home.pedago

import social.entourage.android.api.model.Pedago

class SectionHeader(childList: List<Pedago>, sectionText: String) {
    var childList: List<Pedago>
    var sectionText: String

    init {
        this.childList = childList
        this.sectionText = sectionText
    }
}
