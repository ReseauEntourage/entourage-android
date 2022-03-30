package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Interests : Serializable {
    @SerializedName("activites")
    var activities: String? = null

    @SerializedName("animaux")
    var animals: String? = null

    @SerializedName("bien-etre")
    var wellBeing: String? = null

    @SerializedName("cuisine")
    var cooking: String? = null

    @SerializedName("culture")
    var culture: String? = null

    @SerializedName("jeux")
    var games: String? = null

    @SerializedName("nature")
    var nature: String? = null

    @SerializedName("sport")
    var sport: String? = null

    /* val allInterests: HashMap<String, String?>
         get() {
             return hashMapOf(
                 "activites" to activities,
                 "animaux" to animals,
                 "bien-etre" to wellBeing,
                 "cuisine" to cooking,
                 "culture" to culture,
                 "jeux" to games,
                 "nature" to nature,
                 "sport" to sport
             )
         }
     */

    val allInterests: ArrayList<InterestKeyValue>
        get() {
            val values: ArrayList<InterestKeyValue> = ArrayList()
            values.add(InterestKeyValue("activites", activities))
            values.add(InterestKeyValue("animaux", animals))
            values.add(InterestKeyValue("bien-etre", wellBeing))
            values.add(InterestKeyValue("cuisine", cooking))
            values.add(InterestKeyValue("culture", culture))
            values.add(InterestKeyValue("jeux", games))
            values.add(InterestKeyValue("nature", nature))
            values.add(InterestKeyValue("sport", sport))
            return values
        }

    override fun toString(): String {
        return "Interests(activities=$activities, animals=$animals, wellBeing=$wellBeing, cooking=$cooking, culture=$culture, games=$games, nature=$nature, sport=$sport)"
    }
}

class InterestKeyValue(var key: String, var value: String?) {
    override fun toString(): String {
        return "InterestKeyValue(key='$key', value=$value)"
    }
}