package social.entourage.android.view.countrycodepicker

import android.content.Context
import social.entourage.android.R
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

/**
 * Util related to Country
 */
internal object CountryList {
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
        newCountries.add(Country(context.getString(R.string.country_afghanistan_code),
                context.getString(R.string.country_afghanistan_number),
                context.getString(R.string.country_afghanistan_name)))
        newCountries.add(Country(context.getString(R.string.country_albania_code),
                context.getString(R.string.country_albania_number),
                context.getString(R.string.country_albania_name)))
        newCountries.add(Country(context.getString(R.string.country_algeria_code),
                context.getString(R.string.country_algeria_number),
                context.getString(R.string.country_algeria_name)))
        newCountries.add(Country(context.getString(R.string.country_andorra_code),
                context.getString(R.string.country_andorra_number),
                context.getString(R.string.country_andorra_name)))
        newCountries.add(Country(context.getString(R.string.country_angola_code),
                context.getString(R.string.country_angola_number),
                context.getString(R.string.country_angola_name)))
        newCountries.add(Country(context.getString(R.string.country_argentina_code),
                context.getString(R.string.country_argentina_number),
                context.getString(R.string.country_argentina_name)))
        newCountries.add(Country(context.getString(R.string.country_armenia_code),
                context.getString(R.string.country_armenia_number),
                context.getString(R.string.country_armenia_name)))
        newCountries.add(Country(context.getString(R.string.country_austria_code),
                context.getString(R.string.country_austria_number),
                context.getString(R.string.country_austria_name)))
        newCountries.add(Country(context.getString(R.string.country_australia_code),
                context.getString(R.string.country_australia_number),
                context.getString(R.string.country_australia_name)))
        newCountries.add(Country(context.getString(R.string.country_azerbaijan_code),
                context.getString(R.string.country_azerbaijan_number),
                context.getString(R.string.country_azerbaijan_name)))
        newCountries.add(Country(context.getString(R.string.country_bahrain_code),
                context.getString(R.string.country_bahrain_number),
                context.getString(R.string.country_bahrain_name)))
        newCountries.add(Country(context.getString(R.string.country_bangladesh_code),
                context.getString(R.string.country_bangladesh_number),
                context.getString(R.string.country_bangladesh_name)))
        newCountries.add(Country(context.getString(R.string.country_belarus_code),
                context.getString(R.string.country_belarus_number),
                context.getString(R.string.country_belarus_name)))
        newCountries.add(Country(context.getString(R.string.country_belgium_code),
                context.getString(R.string.country_belgium_number),
                context.getString(R.string.country_belgium_name)))
        newCountries.add(Country(context.getString(R.string.country_belize_code),
                context.getString(R.string.country_belize_number),
                context.getString(R.string.country_belize_name)))
        newCountries.add(Country(context.getString(R.string.country_benin_code),
                context.getString(R.string.country_benin_number),
                context.getString(R.string.country_benin_name)))
        newCountries.add(Country(context.getString(R.string.country_bhutan_code),
                context.getString(R.string.country_bhutan_number),
                context.getString(R.string.country_bhutan_name)))
        newCountries.add(Country(context.getString(R.string.country_bolivia_code),
                context.getString(R.string.country_bolivia_number),
                context.getString(R.string.country_bolivia_name)))
        newCountries.add(Country(context.getString(R.string.country_bosnia_and_herzegovina_code),
                context.getString(R.string.country_bosnia_and_herzegovina_number),
                context.getString(R.string.country_bosnia_and_herzegovina_name)))
        newCountries.add(Country(context.getString(R.string.country_botswana_code),
                context.getString(R.string.country_botswana_number),
                context.getString(R.string.country_botswana_name)))
        newCountries.add(Country(context.getString(R.string.country_brazil_code),
                context.getString(R.string.country_brazil_number),
                context.getString(R.string.country_brazil_name)))
        newCountries.add(Country(context.getString(R.string.country_brunei_darussalam_code),
                context.getString(R.string.country_brunei_darussalam_number),
                context.getString(R.string.country_brunei_darussalam_name)))
        newCountries.add(Country(context.getString(R.string.country_bulgaria_code),
                context.getString(R.string.country_bulgaria_number),
                context.getString(R.string.country_bulgaria_name)))
        newCountries.add(Country(context.getString(R.string.country_burkina_faso_code),
                context.getString(R.string.country_burkina_faso_number),
                context.getString(R.string.country_burkina_faso_name)))
        newCountries.add(Country(context.getString(R.string.country_burundi_code),
                context.getString(R.string.country_burundi_number),
                context.getString(R.string.country_burundi_name)))
        newCountries.add(Country(context.getString(R.string.country_cambodia_code),
                context.getString(R.string.country_cambodia_number),
                context.getString(R.string.country_cambodia_name)))
        newCountries.add(Country(context.getString(R.string.country_cameroon_code),
                context.getString(R.string.country_cameroon_number),
                context.getString(R.string.country_cameroon_name)))
        newCountries.add(Country(context.getString(R.string.country_canada_code),
                context.getString(R.string.country_canada_number),
                context.getString(R.string.country_canada_name)))
        newCountries.add(Country(context.getString(R.string.country_cape_verde_code),
                context.getString(R.string.country_cape_verde_number),
                context.getString(R.string.country_cape_verde_name)))
        newCountries.add(Country(context.getString(R.string.country_central_african_republic_code),
                context.getString(R.string.country_central_african_republic_number),
                context.getString(R.string.country_central_african_republic_name)))
        newCountries.add(Country(context.getString(R.string.country_chad_code),
                context.getString(R.string.country_chad_number),
                context.getString(R.string.country_chad_name)))
        newCountries.add(Country(context.getString(R.string.country_chile_code),
                context.getString(R.string.country_chile_number),
                context.getString(R.string.country_chile_name)))
        newCountries.add(Country(context.getString(R.string.country_china_code),
                context.getString(R.string.country_china_number),
                context.getString(R.string.country_china_name)))
        newCountries.add(Country(context.getString(R.string.country_colombia_code),
                context.getString(R.string.country_colombia_number),
                context.getString(R.string.country_colombia_name)))
        newCountries.add(Country(context.getString(R.string.country_congo_code),
                context.getString(R.string.country_congo_number),
                context.getString(R.string.country_congo_name)))
        newCountries.add(Country(context.getString(R.string.country_the_democratic_republic_of_congo_code),
                context.getString(R.string.country_the_democratic_republic_of_congo_number),
                context.getString(R.string.country_the_democratic_republic_of_congo_name)))
        newCountries.add(Country(context.getString(R.string.country_costa_rica_code),
                context.getString(R.string.country_costa_rica_number),
                context.getString(R.string.country_costa_rica_name)))
        newCountries.add(Country(context.getString(R.string.country_croatia_code),
                context.getString(R.string.country_croatia_number),
                context.getString(R.string.country_croatia_name)))
        newCountries.add(Country(context.getString(R.string.country_cuba_code),
                context.getString(R.string.country_cuba_number),
                context.getString(R.string.country_cuba_name)))
        newCountries.add(Country(context.getString(R.string.country_cyprus_code),
                context.getString(R.string.country_cyprus_number),
                context.getString(R.string.country_cyprus_name)))
        newCountries.add(Country(context.getString(R.string.country_czech_republic_code),
                context.getString(R.string.country_czech_republic_number),
                context.getString(R.string.country_czech_republic_name)))
        newCountries.add(Country(context.getString(R.string.country_denmark_code),
                context.getString(R.string.country_denmark_number),
                context.getString(R.string.country_denmark_name)))
        newCountries.add(Country(context.getString(R.string.country_djibouti_code),
                context.getString(R.string.country_djibouti_number),
                context.getString(R.string.country_djibouti_name)))
        newCountries.add(Country(context.getString(R.string.country_dominica_code),
                context.getString(R.string.country_dominica_number),
                context.getString(R.string.country_dominica_name)))
        newCountries.add(Country(context.getString(R.string.country_dominican_republic_code),
                context.getString(R.string.country_dominican_republic_number),
                context.getString(R.string.country_dominican_republic_name)))
        newCountries.add(Country(context.getString(R.string.country_timor_leste_code),
                context.getString(R.string.country_timor_leste_number),
                context.getString(R.string.country_timor_leste_name)))
        newCountries.add(Country(context.getString(R.string.country_ecuador_code),
                context.getString(R.string.country_ecuador_number),
                context.getString(R.string.country_ecuador_name)))
        newCountries.add(Country(context.getString(R.string.country_egypt_code),
                context.getString(R.string.country_egypt_number),
                context.getString(R.string.country_egypt_name)))
        newCountries.add(Country(context.getString(R.string.country_el_salvador_code),
                context.getString(R.string.country_el_salvador_number),
                context.getString(R.string.country_el_salvador_name)))
        newCountries.add(Country(context.getString(R.string.country_equatorial_guinea_code),
                context.getString(R.string.country_equatorial_guinea_number),
                context.getString(R.string.country_equatorial_guinea_name)))
        newCountries.add(Country(context.getString(R.string.country_eritrea_code),
                context.getString(R.string.country_eritrea_number),
                context.getString(R.string.country_eritrea_name)))
        newCountries.add(Country(context.getString(R.string.country_estonia_code),
                context.getString(R.string.country_estonia_number),
                context.getString(R.string.country_estonia_name)))
        newCountries.add(Country(context.getString(R.string.country_ethiopia_code),
                context.getString(R.string.country_ethiopia_number),
                context.getString(R.string.country_ethiopia_name)))
        newCountries.add(Country(context.getString(R.string.country_fiji_code),
                context.getString(R.string.country_fiji_number),
                context.getString(R.string.country_fiji_name)))
        newCountries.add(Country(context.getString(R.string.country_finland_code),
                context.getString(R.string.country_finland_number),
                context.getString(R.string.country_finland_name)))
        newCountries.add(Country(context.getString(R.string.country_france_code),
                context.getString(R.string.country_france_number),
                context.getString(R.string.country_france_name)))
        newCountries.add(Country(context.getString(R.string.country_french_guyana_code),
                context.getString(R.string.country_french_guyana_number),
                context.getString(R.string.country_french_guyana_name)))
        newCountries.add(Country(context.getString(R.string.country_french_polynesia_code),
                context.getString(R.string.country_french_polynesia_number),
                context.getString(R.string.country_french_polynesia_name)))
        newCountries.add(Country(context.getString(R.string.country_gabon_code),
                context.getString(R.string.country_gabon_number),
                context.getString(R.string.country_gabon_name)))
        newCountries.add(Country(context.getString(R.string.country_gambia_code),
                context.getString(R.string.country_gambia_number),
                context.getString(R.string.country_gambia_name)))
        newCountries.add(Country(context.getString(R.string.country_georgia_code),
                context.getString(R.string.country_georgia_number),
                context.getString(R.string.country_georgia_name)))
        newCountries.add(Country(context.getString(R.string.country_germany_code),
                context.getString(R.string.country_germany_number),
                context.getString(R.string.country_germany_name)))
        newCountries.add(Country(context.getString(R.string.country_ghana_code),
                context.getString(R.string.country_ghana_number),
                context.getString(R.string.country_ghana_name)))
        newCountries.add(Country(context.getString(R.string.country_gibraltar_code),
                context.getString(R.string.country_gibraltar_number),
                context.getString(R.string.country_gibraltar_name)))
        newCountries.add(Country(context.getString(R.string.country_greece_code),
                context.getString(R.string.country_greece_number),
                context.getString(R.string.country_greece_name)))
        newCountries.add(Country(context.getString(R.string.country_guadeloupe_code),
                context.getString(R.string.country_guadeloupe_number),
                context.getString(R.string.country_guadeloupe_name)))
        newCountries.add(Country(context.getString(R.string.country_guatemala_code),
                context.getString(R.string.country_guatemala_number),
                context.getString(R.string.country_guatemala_name)))
        newCountries.add(Country(context.getString(R.string.country_guinea_code),
                context.getString(R.string.country_guinea_number),
                context.getString(R.string.country_guinea_name)))
        newCountries.add(Country(context.getString(R.string.country_guinea_bissau_code),
                context.getString(R.string.country_guinea_bissau_number),
                context.getString(R.string.country_guinea_bissau_name)))
        newCountries.add(Country(context.getString(R.string.country_guyana_code),
                context.getString(R.string.country_guyana_number),
                context.getString(R.string.country_guyana_name)))
        newCountries.add(Country(context.getString(R.string.country_haiti_code),
                context.getString(R.string.country_haiti_number),
                context.getString(R.string.country_haiti_name)))
        newCountries.add(Country(context.getString(R.string.country_honduras_code),
                context.getString(R.string.country_honduras_number),
                context.getString(R.string.country_honduras_name)))
        newCountries.add(Country(context.getString(R.string.country_hong_kong_code),
                context.getString(R.string.country_hong_kong_number),
                context.getString(R.string.country_hong_kong_name)))
        newCountries.add(Country(context.getString(R.string.country_hungary_code),
                context.getString(R.string.country_hungary_number),
                context.getString(R.string.country_hungary_name)))
        newCountries.add(Country(context.getString(R.string.country_india_code),
                context.getString(R.string.country_india_number),
                context.getString(R.string.country_india_name)))
        newCountries.add(Country(context.getString(R.string.country_indonesia_code),
                context.getString(R.string.country_indonesia_number),
                context.getString(R.string.country_indonesia_name)))
        newCountries.add(Country(context.getString(R.string.country_iran_code),
                context.getString(R.string.country_iran_number),
                context.getString(R.string.country_iran_name)))
        newCountries.add(Country(context.getString(R.string.country_iraq_code),
                context.getString(R.string.country_iraq_number),
                context.getString(R.string.country_iraq_name)))
        newCountries.add(Country(context.getString(R.string.country_ireland_code),
                context.getString(R.string.country_ireland_number),
                context.getString(R.string.country_ireland_name)))
        newCountries.add(Country(context.getString(R.string.country_iceland_code),
                context.getString(R.string.country_iceland_number),
                context.getString(R.string.country_iceland_name)))
        newCountries.add(Country(context.getString(R.string.country_israel_code),
                context.getString(R.string.country_israel_number),
                context.getString(R.string.country_israel_name)))
        newCountries.add(Country(context.getString(R.string.country_italy_code),
                context.getString(R.string.country_italy_number),
                context.getString(R.string.country_italy_name)))
        newCountries.add(Country(context.getString(R.string.country_cote_d_ivoire_code),
                context.getString(R.string.country_cote_d_ivoire_number),
                context.getString(R.string.country_cote_d_ivoire_name)))
        newCountries.add(Country(context.getString(R.string.country_jamaica_code),
                context.getString(R.string.country_jamaica_number),
                context.getString(R.string.country_jamaica_name)))
        newCountries.add(Country(context.getString(R.string.country_japan_code),
                context.getString(R.string.country_japan_number),
                context.getString(R.string.country_japan_name)))
        newCountries.add(Country(context.getString(R.string.country_jordan_code),
                context.getString(R.string.country_jordan_number),
                context.getString(R.string.country_jordan_name)))
        newCountries.add(Country(context.getString(R.string.country_kazakhstan_code),
                context.getString(R.string.country_kazakhstan_number),
                context.getString(R.string.country_kazakhstan_name)))
        newCountries.add(Country(context.getString(R.string.country_kenya_code),
                context.getString(R.string.country_kenya_number),
                context.getString(R.string.country_kenya_name)))
        newCountries.add(Country(context.getString(R.string.country_kuwait_code),
                context.getString(R.string.country_kuwait_number),
                context.getString(R.string.country_kuwait_name)))
        newCountries.add(Country(context.getString(R.string.country_kyrgyzstan_code),
                context.getString(R.string.country_kyrgyzstan_number),
                context.getString(R.string.country_kyrgyzstan_name)))
        newCountries.add(Country(context.getString(R.string.country_kosovo_code),
                context.getString(R.string.country_kosovo_number),
                context.getString(R.string.country_kosovo_name)))
        newCountries.add(Country(context.getString(R.string.country_lao_peoples_democratic_republic_code),
                context.getString(R.string.country_lao_peoples_democratic_republic_number),
                context.getString(R.string.country_lao_peoples_democratic_republic_name)))
        newCountries.add(Country(context.getString(R.string.country_latvia_code),
                context.getString(R.string.country_latvia_number),
                context.getString(R.string.country_latvia_name)))
        newCountries.add(Country(context.getString(R.string.country_lebanon_code),
                context.getString(R.string.country_lebanon_number),
                context.getString(R.string.country_lebanon_name)))
        newCountries.add(Country(context.getString(R.string.country_lesotho_code),
                context.getString(R.string.country_lesotho_number),
                context.getString(R.string.country_lesotho_name)))
        newCountries.add(Country(context.getString(R.string.country_liberia_code),
                context.getString(R.string.country_liberia_number),
                context.getString(R.string.country_liberia_name)))
        newCountries.add(Country(context.getString(R.string.country_libya_code),
                context.getString(R.string.country_libya_number),
                context.getString(R.string.country_libya_name)))
        newCountries.add(Country(context.getString(R.string.country_liechtenstein_code),
                context.getString(R.string.country_liechtenstein_number),
                context.getString(R.string.country_liechtenstein_name)))
        newCountries.add(Country(context.getString(R.string.country_lithuania_code),
                context.getString(R.string.country_lithuania_number),
                context.getString(R.string.country_lithuania_name)))
        newCountries.add(Country(context.getString(R.string.country_luxembourg_code),
                context.getString(R.string.country_luxembourg_number),
                context.getString(R.string.country_luxembourg_name)))
        newCountries.add(Country(context.getString(R.string.country_macedonia_code),
                context.getString(R.string.country_macedonia_number),
                context.getString(R.string.country_macedonia_name)))
        newCountries.add(Country(context.getString(R.string.country_madagascar_code),
                context.getString(R.string.country_madagascar_number),
                context.getString(R.string.country_madagascar_name)))
        newCountries.add(Country(context.getString(R.string.country_malawi_code),
                context.getString(R.string.country_malawi_number),
                context.getString(R.string.country_malawi_name)))
        newCountries.add(Country(context.getString(R.string.country_malaysia_code),
                context.getString(R.string.country_malaysia_number),
                context.getString(R.string.country_malaysia_name)))
        newCountries.add(Country(context.getString(R.string.country_maldives_code),
                context.getString(R.string.country_maldives_number),
                context.getString(R.string.country_maldives_name)))
        newCountries.add(Country(context.getString(R.string.country_mali_code),
                context.getString(R.string.country_mali_number),
                context.getString(R.string.country_mali_name)))
        newCountries.add(Country(context.getString(R.string.country_malta_code),
                context.getString(R.string.country_malta_number),
                context.getString(R.string.country_malta_name)))
        newCountries.add(Country(context.getString(R.string.country_martinique_code),
                context.getString(R.string.country_martinique_number),
                context.getString(R.string.country_martinique_name)))
        newCountries.add(Country(context.getString(R.string.country_mauritania_code),
                context.getString(R.string.country_mauritania_number),
                context.getString(R.string.country_mauritania_name)))
        newCountries.add(Country(context.getString(R.string.country_mauritius_code),
                context.getString(R.string.country_mauritius_number),
                context.getString(R.string.country_mauritius_name)))
        newCountries.add(Country(context.getString(R.string.country_mayotte_code),
                context.getString(R.string.country_mayotte_number),
                context.getString(R.string.country_mayotte_name)))
        newCountries.add(Country(context.getString(R.string.country_mexico_code),
                context.getString(R.string.country_mexico_number),
                context.getString(R.string.country_mexico_name)))
        newCountries.add(Country(context.getString(R.string.country_moldova_code),
                context.getString(R.string.country_moldova_number),
                context.getString(R.string.country_moldova_name)))
        newCountries.add(Country(context.getString(R.string.country_monaco_code),
                context.getString(R.string.country_monaco_number),
                context.getString(R.string.country_monaco_name)))
        newCountries.add(Country(context.getString(R.string.country_mongolia_code),
                context.getString(R.string.country_mongolia_number),
                context.getString(R.string.country_mongolia_name)))
        newCountries.add(Country(context.getString(R.string.country_montenegro_code),
                context.getString(R.string.country_montenegro_number),
                context.getString(R.string.country_montenegro_name)))
        newCountries.add(Country(context.getString(R.string.country_morocco_code),
                context.getString(R.string.country_morocco_number),
                context.getString(R.string.country_morocco_name)))
        newCountries.add(Country(context.getString(R.string.country_mozambique_code),
                context.getString(R.string.country_mozambique_number),
                context.getString(R.string.country_mozambique_name)))
        newCountries.add(Country(context.getString(R.string.country_namibia_code),
                context.getString(R.string.country_namibia_number),
                context.getString(R.string.country_namibia_name)))
        newCountries.add(Country(context.getString(R.string.country_nepal_code),
                context.getString(R.string.country_nepal_number),
                context.getString(R.string.country_nepal_name)))
        newCountries.add(Country(context.getString(R.string.country_netherlands_code),
                context.getString(R.string.country_netherlands_number),
                context.getString(R.string.country_netherlands_name)))
        newCountries.add(Country(context.getString(R.string.country_new_caledonia_code),
                context.getString(R.string.country_new_caledonia_number),
                context.getString(R.string.country_new_caledonia_name)))
        newCountries.add(Country(context.getString(R.string.country_new_zealand_code),
                context.getString(R.string.country_new_zealand_number),
                context.getString(R.string.country_new_zealand_name)))
        newCountries.add(Country(context.getString(R.string.country_nicaragua_code),
                context.getString(R.string.country_nicaragua_number),
                context.getString(R.string.country_nicaragua_name)))
        newCountries.add(Country(context.getString(R.string.country_niger_code),
                context.getString(R.string.country_niger_number),
                context.getString(R.string.country_niger_name)))
        newCountries.add(Country(context.getString(R.string.country_nigeria_code),
                context.getString(R.string.country_nigeria_number),
                context.getString(R.string.country_nigeria_name)))
        newCountries.add(Country(context.getString(R.string.country_norway_code),
                context.getString(R.string.country_norway_number),
                context.getString(R.string.country_norway_name)))
        newCountries.add(Country(context.getString(R.string.country_oman_code),
                context.getString(R.string.country_oman_number),
                context.getString(R.string.country_oman_name)))
        newCountries.add(Country(context.getString(R.string.country_pakistan_code),
                context.getString(R.string.country_pakistan_number),
                context.getString(R.string.country_pakistan_name)))
        newCountries.add(Country(context.getString(R.string.country_panama_code),
                context.getString(R.string.country_panama_number),
                context.getString(R.string.country_panama_name)))
        newCountries.add(Country(context.getString(R.string.country_papua_new_guinea_code),
                context.getString(R.string.country_papua_new_guinea_number),
                context.getString(R.string.country_papua_new_guinea_name)))
        newCountries.add(Country(context.getString(R.string.country_paraguay_code),
                context.getString(R.string.country_paraguay_number),
                context.getString(R.string.country_paraguay_name)))
        newCountries.add(Country(context.getString(R.string.country_peru_code),
                context.getString(R.string.country_peru_number),
                context.getString(R.string.country_peru_name)))
        newCountries.add(Country(context.getString(R.string.country_philippines_code),
                context.getString(R.string.country_philippines_number),
                context.getString(R.string.country_philippines_name)))
        newCountries.add(Country(context.getString(R.string.country_poland_code),
                context.getString(R.string.country_poland_number),
                context.getString(R.string.country_poland_name)))
        newCountries.add(Country(context.getString(R.string.country_portugal_code),
                context.getString(R.string.country_portugal_number),
                context.getString(R.string.country_portugal_name)))
        newCountries.add(Country(context.getString(R.string.country_puerto_rico_code),
                context.getString(R.string.country_puerto_rico_number),
                context.getString(R.string.country_puerto_rico_name)))
        newCountries.add(Country(context.getString(R.string.country_qatar_code),
                context.getString(R.string.country_qatar_number),
                context.getString(R.string.country_qatar_name)))
        newCountries.add(Country(context.getString(R.string.country_reunion_code),
                context.getString(R.string.country_reunion_number),
                context.getString(R.string.country_reunion_name)))
        newCountries.add(Country(context.getString(R.string.country_romania_code),
                context.getString(R.string.country_romania_number),
                context.getString(R.string.country_romania_name)))
        newCountries.add(Country(context.getString(R.string.country_russian_federation_code),
                context.getString(R.string.country_russian_federation_number),
                context.getString(R.string.country_russian_federation_name)))
        newCountries.add(Country(context.getString(R.string.country_rwanda_code),
                context.getString(R.string.country_rwanda_number),
                context.getString(R.string.country_rwanda_name)))
        newCountries.add(Country(context.getString(R.string.country_saudi_arabia_code),
                context.getString(R.string.country_saudi_arabia_number),
                context.getString(R.string.country_saudi_arabia_name)))
        newCountries.add(Country(context.getString(R.string.country_senegal_code),
                context.getString(R.string.country_senegal_number),
                context.getString(R.string.country_senegal_name)))
        newCountries.add(Country(context.getString(R.string.country_serbia_code),
                context.getString(R.string.country_serbia_number),
                context.getString(R.string.country_serbia_name)))
        newCountries.add(Country(context.getString(R.string.country_seychelles_code),
                context.getString(R.string.country_seychelles_number),
                context.getString(R.string.country_seychelles_name)))
        newCountries.add(Country(context.getString(R.string.country_sierra_leone_code),
                context.getString(R.string.country_sierra_leone_number),
                context.getString(R.string.country_sierra_leone_name)))
        newCountries.add(Country(context.getString(R.string.country_singapore_code),
                context.getString(R.string.country_singapore_number),
                context.getString(R.string.country_singapore_name)))
        newCountries.add(Country(context.getString(R.string.country_slovakia_code),
                context.getString(R.string.country_slovakia_number),
                context.getString(R.string.country_slovakia_name)))
        newCountries.add(Country(context.getString(R.string.country_slovenia_code),
                context.getString(R.string.country_slovenia_number),
                context.getString(R.string.country_slovenia_name)))
        newCountries.add(Country(context.getString(R.string.country_somalia_code),
                context.getString(R.string.country_somalia_number),
                context.getString(R.string.country_somalia_name)))
        newCountries.add(Country(context.getString(R.string.country_south_africa_code),
                context.getString(R.string.country_south_africa_number),
                context.getString(R.string.country_south_africa_name)))
        newCountries.add(Country(context.getString(R.string.country_south_korea_code),
                context.getString(R.string.country_south_korea_number),
                context.getString(R.string.country_south_korea_name)))
        newCountries.add(Country(context.getString(R.string.country_spain_code),
                context.getString(R.string.country_spain_number),
                context.getString(R.string.country_spain_name)))
        newCountries.add(Country(context.getString(R.string.country_sri_lanka_code),
                context.getString(R.string.country_sri_lanka_number),
                context.getString(R.string.country_sri_lanka_name)))
        newCountries.add(Country(context.getString(R.string.country_sudan_code),
                context.getString(R.string.country_sudan_number),
                context.getString(R.string.country_sudan_name)))
        newCountries.add(Country(context.getString(R.string.country_suriname_code),
                context.getString(R.string.country_suriname_number),
                context.getString(R.string.country_suriname_name)))
        newCountries.add(Country(context.getString(R.string.country_swaziland_code),
                context.getString(R.string.country_swaziland_number),
                context.getString(R.string.country_swaziland_name)))
        newCountries.add(Country(context.getString(R.string.country_sweden_code),
                context.getString(R.string.country_sweden_number),
                context.getString(R.string.country_sweden_name)))
        newCountries.add(Country(context.getString(R.string.country_switzerland_code),
                context.getString(R.string.country_switzerland_number),
                context.getString(R.string.country_switzerland_name)))
        newCountries.add(Country(context.getString(R.string.country_syrian_arab_republic_code),
                context.getString(R.string.country_syrian_arab_republic_number),
                context.getString(R.string.country_syrian_arab_republic_name)))
        newCountries.add(Country(context.getString(R.string.country_taiwan_code),
                context.getString(R.string.country_taiwan_number),
                context.getString(R.string.country_taiwan_name)))
        newCountries.add(Country(context.getString(R.string.country_tajikistan_code),
                context.getString(R.string.country_tajikistan_number),
                context.getString(R.string.country_tajikistan_name)))
        newCountries.add(Country(context.getString(R.string.country_tanzania_code),
                context.getString(R.string.country_tanzania_number),
                context.getString(R.string.country_tanzania_name)))
        newCountries.add(Country(context.getString(R.string.country_thailand_code),
                context.getString(R.string.country_thailand_number),
                context.getString(R.string.country_thailand_name)))
        newCountries.add(Country(context.getString(R.string.country_togo_code),
                context.getString(R.string.country_togo_number),
                context.getString(R.string.country_togo_name)))
        newCountries.add(Country(context.getString(R.string.country_tunisia_code),
                context.getString(R.string.country_tunisia_number),
                context.getString(R.string.country_tunisia_name)))
        newCountries.add(Country(context.getString(R.string.country_turkey_code),
                context.getString(R.string.country_turkey_number),
                context.getString(R.string.country_turkey_name)))
        newCountries.add(Country(context.getString(R.string.country_turkmenistan_code),
                context.getString(R.string.country_turkmenistan_number),
                context.getString(R.string.country_turkmenistan_name)))
        newCountries.add(Country(context.getString(R.string.country_united_arab_emirates_code),
                context.getString(R.string.country_united_arab_emirates_number),
                context.getString(R.string.country_united_arab_emirates_name)))
        newCountries.add(Country(context.getString(R.string.country_uganda_code),
                context.getString(R.string.country_uganda_number),
                context.getString(R.string.country_uganda_name)))
        newCountries.add(Country(context.getString(R.string.country_united_kingdom_code),
                context.getString(R.string.country_united_kingdom_number),
                context.getString(R.string.country_united_kingdom_name)))
        newCountries.add(Country(context.getString(R.string.country_ukraine_code),
                context.getString(R.string.country_ukraine_number),
                context.getString(R.string.country_ukraine_name)))
        newCountries.add(Country(context.getString(R.string.country_uruguay_code),
                context.getString(R.string.country_uruguay_number),
                context.getString(R.string.country_uruguay_name)))
        newCountries.add(Country(context.getString(R.string.country_united_states_code),
                context.getString(R.string.country_united_states_number),
                context.getString(R.string.country_united_states_name)))
        newCountries.add(Country(context.getString(R.string.country_uzbekistan_code),
                context.getString(R.string.country_uzbekistan_number),
                context.getString(R.string.country_uzbekistan_name)))
        newCountries.add(Country(context.getString(R.string.country_venezuela_code),
                context.getString(R.string.country_venezuela_number),
                context.getString(R.string.country_venezuela_name)))
        newCountries.add(Country(context.getString(R.string.country_viet_nam_code),
                context.getString(R.string.country_viet_nam_number),
                context.getString(R.string.country_viet_nam_name)))
        newCountries.add(Country(context.getString(R.string.country_yemen_code),
                context.getString(R.string.country_yemen_number),
                context.getString(R.string.country_yemen_name)))
        newCountries.add(Country(context.getString(R.string.country_zambia_code),
                context.getString(R.string.country_zambia_number),
                context.getString(R.string.country_zambia_name)))
        newCountries.add(Country(context.getString(R.string.country_zimbabwe_code),
                context.getString(R.string.country_zimbabwe_number),
                context.getString(R.string.country_zimbabwe_name)))
        newCountries.sortWith(Comparator { o1: Country, o2: Country -> o1.name.compareTo(o2.name) })
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
        if(timeZoneAndCountryISOs.isNullOrEmpty()) {
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
        }
        return timeZoneAndCountryISOs!!
    }
}