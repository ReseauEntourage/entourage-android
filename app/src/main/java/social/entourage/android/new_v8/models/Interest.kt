package social.entourage.android.new_v8.models

import social.entourage.android.R

class Interest(
    val id: String?,
    val title: String?,
    var isSelected: Boolean
) {
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

    companion object {
        private const val animals = "animaux"
        private const val wellBeing = "bien-etre"
        private const val cooking = "cuisine"
        private const val culture = "culture"
        private const val games = "jeux"
        private const val nature = "nature"
        private const val sport = "sport"
        private const val activities = "activites"

        fun getIconFromId(id: String): Int = when (id) {
            animals -> R.drawable.new_interests_animal_item
            wellBeing -> R.drawable.new_interests_wellbeing_item
            cooking -> R.drawable.new_interests_cooking_item
            culture -> R.drawable.new_interests_art_item
            games -> R.drawable.new_interests_games_item
            nature -> R.drawable.new_interests_nature_item
            sport -> R.drawable.new_interests_sport_item
            activities -> R.drawable.new_interests_drawing_item
            else -> R.drawable.new_interests_other_item
        }
    }
}