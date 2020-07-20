package social.entourage.android.tools.view.countrycodepicker

class Country(var iso: String, var phoneCode: String, var name: String) {

    /**
     * If country have query word in name or name code or phone code, this will return true.
     */
    fun isEligibleForQuery(query: String): Boolean {
        val queryLowerCase = query.toLowerCase()
        return (name.toLowerCase().contains(queryLowerCase)
                || iso.toLowerCase().contains(queryLowerCase)
                || phoneCode.toLowerCase().contains(queryLowerCase))
    }

}