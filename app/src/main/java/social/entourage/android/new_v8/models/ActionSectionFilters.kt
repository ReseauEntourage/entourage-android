package social.entourage.android.new_v8.models

import social.entourage.android.api.MetaDataRepository

/**
 * Created by Me on 24/10/2022.
 */
class ActionSectionFilters() : java.io.Serializable {

    private var sections = ArrayList<ActionSection>()

    init {
        sections.clear()
        MetaDataRepository.metaData.value?.sections?.let {
            for (tag in it) {
                sections.add(ActionSection(tag.id,tag.name,tag.subname,false))
            }
        }
    }

    fun resetToDefault() {
        sections.forEach { it.isSelected = false }
    }

    fun getSectionFromKey(key:String) : ActionSection? {
        return sections.first { it.id == key }
    }

    fun getSections() : ArrayList<ActionSection> {
        return sections
    }

    fun hasSectionSelected() : Boolean {
        return sections.find { it.isSelected } != null
    }

    fun setSectionSelected(key:String) {
        resetToDefault()

        val pos = sections.indexOfFirst { it.id == key }
        sections[pos].isSelected = true
    }

    fun getNumberOfSectionsSelected() : Int {
        var count = 0
        sections.forEach {  if (it.isSelected) count++ }
        return count
    }

    fun getSectionsForWS() : String? {
        var wsStr = ""

        for (tag in sections) {
            if (tag.isSelected) {
                val coma = if (wsStr.isNotEmpty()) "," else ""
                wsStr = wsStr + coma + tag.id
            }
        }

        return if (wsStr.isNotBlank()) wsStr else null
    }

    fun getActionSectionNameFromKey(key:String?) : String {
        val ret =  sections.firstOrNull { it.id == key }
        ret?.title?.let { return it } ?: kotlin.run {
            return  "-"
        }
    }

    fun validate() : Boolean {
        return hasSectionSelected()
    }
}