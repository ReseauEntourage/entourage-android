package social.entourage.android.authentication;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import social.entourage.android.Constants;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.tape.Events;
import social.entourage.android.authentication.login.LoginActivity;
import social.entourage.android.map.entourage.my.filter.MyEntouragesFilter;
import social.entourage.android.map.filter.MapFilter;
import social.entourage.android.map.filter.MapFilterFactory;
import social.entourage.android.tools.BusProvider;

/**
 * Controller that managed the authenticated user and persist it on the phone
 */
public class AuthenticationController {

    private static final String PREF_KEY_USER = "user";
    private static final String PREF_KEY_USER_TOURS_ONLY = "user_tours_only";
    private static final String PREF_KEY_MAP_FILTER = "map_filter";
    private static final String PREF_KEY_MAP_FILTER_HASHMAP = "map_filter_hashmap";
    private static final String PREF_KEY_USER_PREFERENCES = "user_preferences";

    private final ComplexPreferences appSharedPref;
    private User loggedUser;
    private UserPreferences userPreferences;

    private Map<Integer, UserPreferences> userPreferencesHashMap = new HashMap<>();

    public AuthenticationController(ComplexPreferences appSharedPref) {
        this.appSharedPref = appSharedPref;
        loggedUser = null;
        userPreferences = null;
    }

    public AuthenticationController init() {
        loggedUser = appSharedPref.getObject(PREF_KEY_USER, User.class);
        if (loggedUser != null && loggedUser.getToken() == null) {
            loggedUser = null;
        }
        loadUserPreferences();

        return this;
    }

    public void saveUser(User user) {
        boolean shouldLoadUserPreferences = true;
        if (loggedUser != null && loggedUser.getId() == user.getId()) {
            user.setPhone(loggedUser.getPhone());
            user.setSmsCode(loggedUser.getSmsCode());
            if (user != loggedUser) {
                user.setEntourageDisclaimerShown(loggedUser.isEntourageDisclaimerShown());
                user.setEncounterDisclaimerShown(loggedUser.isEncounterDisclaimerShown());
                user.setOnboardingUser(loggedUser.isOnboardingUser());
            }
            shouldLoadUserPreferences = false;
        }
        loggedUser = user;
        appSharedPref.putObject(PREF_KEY_USER, user);
        appSharedPref.commit();

        if (shouldLoadUserPreferences) loadUserPreferences();

        BusProvider.getInstance().post(new Events.OnUserInfoUpdatedEvent());
    }

    public void saveUserPhoneAndCode(String phone, String smsCode) {
        loggedUser.setPhone(phone);
        loggedUser.setSmsCode(smsCode);
        appSharedPref.putObject(PREF_KEY_USER, loggedUser);
        appSharedPref.commit();
    }

    public void incrementUserToursCount() {
        loggedUser.incrementTours();
        appSharedPref.putObject(PREF_KEY_USER, loggedUser);
        appSharedPref.commit();
    }

    public void incrementUserEncountersCount() {
        loggedUser.incrementEncouters();
        appSharedPref.putObject(PREF_KEY_USER, loggedUser);
        appSharedPref.commit();
    }

    public void logOutUser() {
        if(loggedUser != null) {
            appSharedPref.putObject(PREF_KEY_USER, null);
            appSharedPref.commit();
        }
        loggedUser = null;
        userPreferences = null;
    }

    public boolean isAuthenticated() {
        return loggedUser != null;
    }

    public boolean isTutorialDone(Context context) {
        if (loggedUser == null) return false;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        HashSet<String> loggedNumbers = (HashSet<String>) sharedPreferences.getStringSet(LoginActivity.KEY_TUTORIAL_DONE, new HashSet<String>());
        return loggedNumbers.contains(loggedUser.getPhone());
    }

    public User getUser() {
        return loggedUser;
    }

    public void saveUserToursOnly(boolean choice) {
        if (userPreferences != null) {
            userPreferences.setUserToursOnly(choice);
            saveUserPreferences();
        }
    }

    public boolean isUserToursOnly() {
        return userPreferences != null && userPreferences.isUserToursOnly();
    }

    public boolean isShowNoEntouragesPopup() {
        return userPreferences == null || userPreferences.isShowNoEntouragesPopup();
    }

    public void setShowNoEntouragesPopup(boolean showNoEntouragesPopup) {
        if (userPreferences != null) {
            userPreferences.setShowNoEntouragesPopup(showNoEntouragesPopup);
            saveUserPreferences();
        }
    }

    public boolean isShowNoPOIsPopup() {
        return userPreferences == null || userPreferences.isShowNoPOIsPopup();
    }

    public void setShowNoPOIsPopup(boolean showNoPOIsPopup) {
        if (userPreferences != null) {
            userPreferences.setShowNoPOIsPopup(showNoPOIsPopup);
            saveUserPreferences();
        }
    }

    public boolean isShowInfoPOIsPopup() {
        return userPreferences == null || userPreferences.isShowInfoPOIsPopup();
    }

    public void setShowInfoPOIsPopup(final boolean showInfoPOIsPopup) {
        if (userPreferences != null) {
            userPreferences.setShowInfoPOIsPopup(showInfoPOIsPopup);
            saveUserPreferences();
        }
    }

    public boolean isShowEncounterDisclaimer() {
        return userPreferences == null || userPreferences.isShowEncounterDisclaimer();
    }

    public void setShowEncounterDisclaimer(final boolean showEncounterDisclaimer) {
        if (userPreferences != null) {
            userPreferences.setShowEncounterDisclaimer(showEncounterDisclaimer);
            saveUserPreferences();
        }
    }

    public MapFilter getMapFilter() {
        MapFilter mapFilter = null;
        if (loggedUser != null && userPreferences != null) {
            mapFilter = userPreferences.getMapFilter();
            if (mapFilter == null) {
                mapFilter = MapFilterFactory.getMapFilter(loggedUser.isPro());
                userPreferences.setMapFilter(mapFilter);
                saveUserPreferences();
            }
        }
        return mapFilter;
    }

    public void saveMapFilter() {
        saveUserPreferences();
    }

    public MyEntouragesFilter getMyEntouragesFilter() {
        MyEntouragesFilter myEntouragesFilter = null;
        if (loggedUser != null && userPreferences != null) {
            myEntouragesFilter = userPreferences.getMyEntouragesFilter();
            if (myEntouragesFilter == null) {
                myEntouragesFilter = new MyEntouragesFilter();
                userPreferences.setMyEntouragesFilter(myEntouragesFilter);
                saveUserPreferences();
            }
        }

        return myEntouragesFilter;
    }

    public void saveMyEntouragesFilter() {
        saveUserPreferences();
    }

    public Tour getSavedTour() {
        return (userPreferences != null ? userPreferences.getOngoingTour() : null);
    }

    public void saveTour(Tour tour) {
        if (loggedUser != null && userPreferences != null) {
            userPreferences.setOngoingTour(tour);
            saveUserPreferences();
        }
    }

    private void loadUserPreferences() {
        Type type = new TypeToken<Map<Integer, UserPreferences>>() {}.getType();
        userPreferencesHashMap = appSharedPref.getObject(PREF_KEY_USER_PREFERENCES, type);

        if (userPreferencesHashMap == null) {
            userPreferencesHashMap = new HashMap<>();
        }

        if (loggedUser != null) {
            userPreferences = userPreferencesHashMap.get(loggedUser.getId());
        }
        if (userPreferences == null) {
            userPreferences = new UserPreferences();
        }
        // Check if we have an old version of saving the map filter with hashmap
        Type typeMapFilterHashMap = new TypeToken<Map<Integer, MapFilter>>(){}.getType();
        Map<Integer, MapFilter> mapFilterHashMap = appSharedPref.getObject(PREF_KEY_MAP_FILTER_HASHMAP, typeMapFilterHashMap);
        if (mapFilterHashMap != null) {
            // save it to user preferences
            if (loggedUser != null) {
                MapFilter mapFilter = mapFilterHashMap.get(loggedUser.getId());
                userPreferences.setMapFilter(mapFilter);
                saveUserPreferences();
            }
            // delete it
            appSharedPref.putObject(PREF_KEY_MAP_FILTER_HASHMAP, null);
        }
        if (loggedUser != null) {
            // Check if we have the old version of saving map filter
            MapFilter mapFilter = appSharedPref.getObject(PREF_KEY_MAP_FILTER, MapFilter.class);
            if (mapFilter != null) {
                // Found old version, save it to the new structure
                userPreferences.setMapFilter(mapFilter);
                saveUserPreferences();
                // Delete it
                appSharedPref.putObject(PREF_KEY_MAP_FILTER, null);
            }
        }
        // MapFilter validation
        MapFilter mapFilter = getMapFilter();
        if (mapFilter != null) {
            mapFilter.validateCategories();
        }
    }

    public void saveUserPreferences() {
        if (loggedUser == null) {
            return;
        }
        userPreferencesHashMap.put(loggedUser.getId(), userPreferences);
        appSharedPref.putObject(PREF_KEY_USER_PREFERENCES, userPreferencesHashMap);
        appSharedPref.commit();
    }
}
