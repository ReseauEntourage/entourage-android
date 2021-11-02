package social.entourage.android.tools.view.countrycodepicker

import java.util.*

class Country(var iso: String, var phoneCode: String, var name: String) {

    /**
     * If country have query word in name or name code or phone code, this will return true.
     */
    fun isEligibleForQuery(query: String): Boolean {
        val queryLowerCase = query.lowercase(Locale.getDefault())
        return (name.lowercase(Locale.getDefault()).contains(queryLowerCase)
                || iso.lowercase(Locale.getDefault()).contains(queryLowerCase)
                || phoneCode.lowercase(Locale.getDefault()).contains(queryLowerCase))
    }

}