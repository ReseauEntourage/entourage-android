package social.entourage.android.authentication

import com.google.gson.reflect.TypeToken
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.User
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.tape.Events.OnUserInfoUpdatedEvent
import social.entourage.android.entourage.my.filter.MyEntouragesFilter
import social.entourage.android.map.filter.MapFilter
import social.entourage.android.tools.BusProvider
import java.util.*
import kotlin.collections.HashMap

/**
 * Controller that managed the authenticated user and persist it on the phone
 */
class AuthenticationController(private val appSharedPref: ComplexPreferences) {
    private var user: User? = null
    private var userPreferences: UserPreferences = UserPreferences()
    private var userPreferencesHashMap: MutableMap<Int, UserPreferences?> = HashMap()

    fun init(): AuthenticationController {
        user = appSharedPref.getObject(PREF_KEY_USER, User::class.java)
        if (user?.token == null) {
            user = null
        }

        loadUserPreferences()

        //To reset show pop up empty POI on GDS
        userPreferences.isShowNoPOIsPopup = true

        return this
    }

    val me: User?
        get() = user

    fun saveUser(updatedUser: User) {
        val shouldLoadUserPreferences = user?.let { user ->
            if (user.id == updatedUser.id) {
                //updatedUser doesnot contain all current user info
                updatedUser.phone = user.phone
                updatedUser.smsCode = user.smsCode
                false
            } else true
        } ?: true

        user = updatedUser
        saveCurrentUser()
        if (shouldLoadUserPreferences) loadUserPreferences()
        BusProvider.instance.post(OnUserInfoUpdatedEvent())
    }

    fun saveUserPhoneAndCode(phone: String?, smsCode: String?) {
        user?.let { user ->
            user.phone = phone
            user.smsCode = smsCode
            saveCurrentUser()
        }
    }

    fun incrementUserToursCount() {
        user?.let { user ->
            user.incrementTours()
            saveCurrentUser()
        }
    }

    fun incrementUserEncountersCount() {
        user?.let { user ->
            user.incrementEncouters()
            saveCurrentUser()
        }
    }

    fun logOutUser() {
        saveCurrentUser()
        user = null
        userPreferences = UserPreferences()
    }

    val isAuthenticated: Boolean
        get() = user != null

    fun isTutorialDone(): Boolean {
        user?.let { user ->
            val sharedPreferences = EntourageApplication.get().sharedPreferences
            val loggedNumbers = sharedPreferences.getStringSet(EntourageApplication.KEY_TUTORIAL_DONE, HashSet()) as HashSet<String>?
            return loggedNumbers?.contains(user.phone) ?: false
        }
        return false
    }

    private fun loadUserPreferences() {
        val type = object : TypeToken<Map<Int?, UserPreferences?>?>() {}.type
        userPreferencesHashMap = (appSharedPref.getObjectFromType<MutableMap<Int, UserPreferences?>>(PREF_KEY_USER_PREFERENCES, type) ?: HashMap()).toMutableMap()

        // since we save the user preferences for all the users
        // we just need to check for an existing user preferences to see if it's a new user or not
        userPreferences = user?.let { user ->
            userPreferencesHashMap[user.id]
        } ?: UserPreferences()
        // Check if we have an old version of saving the map filter with hashmap
        val typeMapFilterHashMap = object : TypeToken<Map<Int?, MapFilter?>?>() {}.type
        appSharedPref.getObjectFromType<Map<Int, MapFilter>>(PREF_KEY_MAP_FILTER_HASHMAP, typeMapFilterHashMap)?.let { mapFilterHashMap ->
            // save it to user preferences
            user?.let { user ->
                userPreferences.mapFilter = mapFilterHashMap[user.id]
                saveUserPreferences()
            }
            // delete it
            appSharedPref.putObject(PREF_KEY_MAP_FILTER_HASHMAP, null)
        }
        if (user != null) {
            // Check if we have the old version of saving map filter
            appSharedPref.getObject(PREF_KEY_MAP_FILTER, MapFilter::class.java)?.let { mapFilter ->
                // Found old version, save it to the new structure
                userPreferences.mapFilter = mapFilter
                saveUserPreferences()
                // Delete it
                appSharedPref.putObject(PREF_KEY_MAP_FILTER, null)
            }
        }
    }

    ///////////////////////////////
    // userPreferences
    fun saveUserToursOnly(choice: Boolean) {
        userPreferences.isUserToursOnly = choice
        saveUserPreferences()
    }

    var entourageDisclaimerShown: Boolean
        get() = userPreferences.isEntourageDisclaimerShown
        set(isEntourageDisclaimerShown) {
            userPreferences.isEntourageDisclaimerShown = isEntourageDisclaimerShown
            saveUserPreferences()
        }

    var encounterDisclaimerShown: Boolean
        get() = userPreferences.isEncounterDisclaimerShown
        set(isEncounterDisclaimerShown) {
            userPreferences.isEncounterDisclaimerShown = isEncounterDisclaimerShown
            saveUserPreferences()
        }

    var isOnboardingUser: Boolean
        get() = userPreferences.isOnboardingUser
        set(isOnboardingUser) {
            userPreferences.isOnboardingUser = isOnboardingUser
            saveUserPreferences()
        }

    var editActionZoneShown: Boolean
        get() = userPreferences.isEditActionZoneShown
        set(isEditActionZoneShown) {
            userPreferences.isEditActionZoneShown = isEditActionZoneShown
            saveUserPreferences()
        }

    val isUserToursOnly: Boolean
        get() = userPreferences.isUserToursOnly

    var isShowNoEntouragesPopup: Boolean
        get() = userPreferences.isShowNoEntouragesPopup
        set(showNoEntouragesPopup) {
            userPreferences.isShowNoEntouragesPopup = showNoEntouragesPopup
            saveUserPreferences()
        }

    var isShowNoPOIsPopup: Boolean
        get() = userPreferences.isShowNoPOIsPopup
        set(showNoPOIsPopup) {
            userPreferences.isShowNoPOIsPopup = showNoPOIsPopup
            saveUserPreferences()
        }

    var isShowInfoPOIsPopup: Boolean
        get() = userPreferences.isShowInfoPOIsPopup
        set(showInfoPOIsPopup) {
            userPreferences.isShowInfoPOIsPopup = showInfoPOIsPopup
            saveUserPreferences()
        }

    var isShowEncounterDisclaimer: Boolean
        get() = userPreferences.isShowEncounterDisclaimer
        set(showEncounterDisclaimer) {
            userPreferences.isShowEncounterDisclaimer = showEncounterDisclaimer
            saveUserPreferences()
        }

    val mapFilter: MapFilter
        get() {
            return userPreferences.mapFilter ?: MapFilter().apply {
                this.setDefaultValues()
            }.also {
                userPreferences.mapFilter = it
                saveUserPreferences()
            }
        }

    fun saveMapFilter() {
        saveUserPreferences()
    }

    val myEntouragesFilter: MyEntouragesFilter?
        get() {
            if (user != null) {
                return userPreferences.myEntouragesFilter
                        ?: MyEntouragesFilter().also {
                            userPreferences.myEntouragesFilter = it
                            saveUserPreferences()
                        }
            }
            return null
        }

    fun saveMyEntouragesFilter() {
        saveUserPreferences()
    }

    val savedTour: Tour?
        get() = userPreferences.ongoingTour

    fun saveTour(tour: Tour?) {
        if (user != null) {
            userPreferences.ongoingTour = tour
            saveUserPreferences()
        }
    }

    private fun saveCurrentUser() {
        user?.let {
            appSharedPref.putObject(PREF_KEY_USER, it)
            appSharedPref.commit()
        }
    }

    var isIgnoringActionZone
        get() = userPreferences.isIgnoringActionZone
        set(newParam) {
            userPreferences.isIgnoringActionZone = newParam
            saveUserPreferences()
        }

    ///////////////////////////////
    // userPreferencesHashMap
    fun saveUserPreferences() {
        user?.let { user ->
            saveCurrentUser()
            userPreferencesHashMap[user.id] = userPreferences
            appSharedPref.putObject(PREF_KEY_USER_PREFERENCES, userPreferencesHashMap)
            appSharedPref.commit()
        }
    }

    companion object {
        private const val PREF_KEY_USER = "user"
        private const val PREF_KEY_MAP_FILTER = "map_filter"
        private const val PREF_KEY_MAP_FILTER_HASHMAP = "map_filter_hashmap"
        private const val PREF_KEY_USER_PREFERENCES = "user_preferences"
    }

}