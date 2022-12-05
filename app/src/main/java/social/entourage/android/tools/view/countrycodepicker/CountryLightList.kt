package social.entourage.android.tools.view.countrycodepicker

import android.content.Context
import social.entourage.android.R
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

/**
 * Util related to Country
 */
internal object CountryLightList {
    private var countries: MutableList<Country>? = null
    private var timeZoneAndCountryISOs: MutableMap<String, List<String>>? = null

    /**
     * Get all countries
     *
     * @param context caller context
     * @return List of Country
     */
    fun getAllCountries(context: Context): List<Country>? {
        if (countries != null) {
            return countries
        }
        val newCountries = ArrayList<Country>()
        newCountries.add(Country(context.getString(R.string.country_france_code),
                context.getString(R.string.country_france_number),
                context.getString(R.string.country_france_name),context.getString(R.string.country_france_flag)))
        newCountries.add(Country(context.getString(R.string.country_belgium_code),
                context.getString(R.string.country_belgium_number),
                context.getString(R.string.country_belgium_name),context.getString(R.string.country_belgium_flag)))
        newCountries.sortWith { o1: Country, o2: Country -> o1.name.compareTo(o2.name) }
        countries = newCountries
        return newCountries
    }

    /**
     * Search a country which matches @param code.
     *
     * @param preferredCountries list of country with priority,
     * @param code               phone code. i.e 91 or 1
     * @return Country that has phone code as @param code.
     * or returns null if no country matches given code.
     */
    fun getByCode(context: Context, preferredCountries: List<Country>?, code: Int): Country? {
        return getByCodeString(context, preferredCountries, code.toString())
    }

    /**
     * Search a country which matches @param code.
     *
     * @param preferredCountries is list of preference countries.
     * @param code               phone code. i.e "91" or "1"
     * @return Country that has phone code as @param code.
     * or returns null if no country matches given code.
     * if same code (e.g. +1) available for more than one country ( US, canada) , this function will
     * return preferred country.
     */
    private fun getByCodeString(context: Context, preferredCountries: List<Country>?, code: String): Country? {
        //check in preferred countries first
        preferredCountries?.forEach {country->
            if (country.phoneCode == code) {
                return country
            }
        }
        getAllCountries(context)?.forEach {country ->
            if (country.phoneCode == code) {
                return country
            }
        }
        return null
    }

    /**
     * Search a country which matches @param nameCode.
     *
     * @param nameCode country name code. i.e US or us or Au. See countries.xml for all code names.
     * @return Country that has phone code as @param code.
     * or returns null if no country matches given code.
     */
    fun getByNameCodeFromCustomCountries(context: Context,
                                         customCountries: List<Country>?,
                                         nameCode: String?): Country? {
        customCountries?.forEach {country ->
            if (country.iso.equals(nameCode, ignoreCase = true)) {
                return country
            }
        } 
        return getByNameCodeFromAllCountries(context, nameCode)
    }

    /**
     * Search a country which matches @param nameCode.
     *
     * @param nameCode country name code. i.e US or us or Au. See countries.xml for all code names.
     * @return Country that has phone code as @param code.
     * or returns null if no country matches given code.
     */
    fun getByNameCodeFromAllCountries(context: Context, nameCode: String?): Country? {
        getAllCountries(context)?.forEach {country ->
            if (country.iso.equals(nameCode, ignoreCase = true)) {
                return country
            }
        }
        return null
    }

    fun getCountryIsoByTimeZone(context: Context, timeZoneId: String): List<String>? {
        val timeZoneAndCountryIsos = getTimeZoneAndCountryISOs(context)
        return timeZoneAndCountryIsos[timeZoneId]
    }

    /**
     * Return list of Map for timezone and iso country.
     *
     * @param context current context
     * @return List of timezone and country.
     */
    private fun getTimeZoneAndCountryISOs(context: Context): Map<String, List<String>> {
        timeZoneAndCountryISOs?.let {
            if (it.isNotEmpty()) return it
        }

        val newTimeZoneAndCountryISOs = HashMap<String, List<String>>()

        // Read from raw
        val inputStream = context.resources.openRawResource(R.raw.zone1970)
        val buf = BufferedReader(InputStreamReader(inputStream))
        var lineJustFetched: String?
        var wordsArray: Array<String>
        try {
            while (true) {
                lineJustFetched = buf.readLine()
                if (lineJustFetched == null) {
                    break
                } else {
                    wordsArray = lineJustFetched.split("\t".toRegex()).toTypedArray()
                    // Ignore line which have # as the first character.
                    if (!lineJustFetched.substring(0, 1).contains("#")) {
                        if (wordsArray.size >= 3) {
                            // First word is country code or list of country code separate by comma
                            val isos: MutableList<String> = ArrayList()
                            Collections.addAll(isos, *wordsArray[0].split(",".toRegex()).toTypedArray())
                            // Third word in wordsArray is timezone.
                            newTimeZoneAndCountryISOs[wordsArray[2]] = isos
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Timber.e(e)
        }
        timeZoneAndCountryISOs = newTimeZoneAndCountryISOs
        return newTimeZoneAndCountryISOs
    }
}