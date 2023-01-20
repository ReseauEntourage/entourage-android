package social.entourage.android.api.model

import social.entourage.android.R

class ActionSection(
    val id: String?,
    val title: String?,
    val subtitle:String?,
    var isSelected: Boolean
) : java.io.Serializable {
    val icon: Int
        get() {
            return when (id) {
                social -> R.drawable.ic_action_section_social
                clothes -> R.drawable.ic_action_section_clothes
                equipment -> R.drawable.ic_action_section_equipment
                hygiene -> R.drawable.ic_action_section_hygiene
                services -> R.drawable.ic_action_section_services
                else -> R.drawable.new_others
            }
        }

    override fun toString(): String {
        return "Signal(id=$id, title=$title, isSelected=$isSelected, icon=$icon, subtitle=$subtitle)"
    }

    companion object {
        const val social = "social"
        const val clothes = "clothes"
        const val equipment = "equipment"
        const val hygiene = "hygiene"
        const val services = "services"

        fun getIconFromId(id: String?): Int = when (id) {
            social -> R.drawable.ic_action_section_social
            clothes -> R.drawable.ic_action_section_clothes
            equipment -> R.drawable.ic_action_section_equipment
            hygiene -> R.drawable.ic_action_section_hygiene
            services -> R.drawable.ic_action_section_services
            else -> R.drawable.new_others
        }

    }
}