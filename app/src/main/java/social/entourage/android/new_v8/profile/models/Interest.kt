package social.entourage.android.new_v8.profile.models

import social.entourage.android.R

class Interest(
    val id: String?,
    val title: String?,
    var isSelected: Boolean
) {

    val icon: Int
        get() {
            return when (id) {
                "animaux" -> R.drawable.new_animals
                "bien-etre" -> R.drawable.new_wellbeing
                "cuisine" -> R.drawable.new_cooking
                "culture" -> R.drawable.new_art
                "jeux" -> R.drawable.new_games
                "nature" -> R.drawable.new_nature
                "sport" -> R.drawable.new_sport
                "activites" -> R.drawable.new_drawing
                else -> R.drawable.new_others
            }
        }

    override fun toString(): String {
        return "Interest(id=$id, title=$title, isSelected=$isSelected, icon=$icon)"
    }
}