package social.entourage.android.api.model

import social.entourage.android.R

class Interest(
    val id: String?,
    val title: String?,
    var isSelected: Boolean
) {
    val icon: Int
        get() {
            return when (id) {
                animals -> R.drawable.ic_onboarding_interest_name_animaux
                wellBeing -> R.drawable.ic_onboarding_interest_name_bien_etre
                cooking -> R.drawable.ic_onboarding_interest_name_cuisine
                culture -> R.drawable.ic_onboarding_interest_name_art
                games -> R.drawable.ic_onboarding_interest_name_jeux
                nature -> R.drawable.ic_onboarding_interest_name_nature
                sport -> R.drawable.ic_onboarding_interest_sport
                activities -> R.drawable.ic_onboarding_interest_name_activite_manuelle
                marauding -> R.drawable.ic_onboarding_interest_name_rencontre_nomade
                else -> R.drawable.ic_onboarding_interest_name_autre
            }
        }

    override fun toString(): String {
        return "Interest(id=$id, title=$title, isSelected=$isSelected, icon=$icon)"
    }

    companion object {
        const val animals = "animaux"
        const val wellBeing = "bien-etre"
        const val cooking = "cuisine"
        const val culture = "culture"
        const val games = "jeux"
        const val nature = "nature"
        const val sport = "sport"
        const val activities = "activites"
        const val marauding = "marauding"

        fun getIconFromId(id: String): Int = when (id) {
            animals -> R.drawable.ic_onboarding_interest_name_animaux
            wellBeing -> R.drawable.ic_onboarding_interest_name_bien_etre
            cooking -> R.drawable.ic_onboarding_interest_name_cuisine
            culture -> R.drawable.ic_onboarding_interest_name_art
            games -> R.drawable.ic_onboarding_interest_name_jeux
            nature -> R.drawable.ic_onboarding_interest_name_nature
            sport -> R.drawable.ic_onboarding_interest_sport
            activities -> R.drawable.ic_onboarding_interest_name_activite_manuelle
            marauding -> R.drawable.ic_onboarding_interest_name_rencontre_nomade
            else -> R.drawable.ic_onboarding_interest_name_autre
        }
    }
}
