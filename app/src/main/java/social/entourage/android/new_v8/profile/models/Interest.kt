package social.entourage.android.new_v8.profile.models

import social.entourage.android.R

class Interest(
    val id: String?,
    val title: String?,
    var isSelected: Boolean
) {
    private val animals = "animaux"
    private val wellBeing = "bien-etre"
    private val cooking = "cuisine"
    private val culture = "culture"
    private val games = "jeux"
    private val nature = "nature"
    private val sport = "sport"
    private val activities = "activites"

    val icon: Int
        get() {
            return when (id) {
                animals -> R.drawable.new_animals
                wellBeing -> R.drawable.new_wellbeing
                cooking -> R.drawable.new_cooking
                culture -> R.drawable.new_art
                games -> R.drawable.new_games
                nature -> R.drawable.new_nature
                sport -> R.drawable.new_sport
                activities -> R.drawable.new_drawing
                else -> R.drawable.new_others
            }
        }

    override fun toString(): String {
        return "Interest(id=$id, title=$title, isSelected=$isSelected, icon=$icon)"
    }
}