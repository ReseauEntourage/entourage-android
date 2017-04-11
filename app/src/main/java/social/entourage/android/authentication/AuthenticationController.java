package social.entourage.android.authentication;

import java.util.HashMap;

import social.entourage.android.api.model.User;
import social.entourage.android.api.tape.Events;
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

    private final ComplexPreferences userSharedPref;
    private User loggedUser;
    private boolean userToursOnly;
    private boolean showNoEntouragesPopup = true;
    private boolean showNoPOIsPopup = true;

    private HashMap<Integer, MapFilter> mapFilterHashMap;
    private MapFilter mapFilter = null;

    public AuthenticationController(ComplexPreferences userSharedPref) {
        this.userSharedPref = userSharedPref;
        loggedUser = null;
    }

    public AuthenticationController init() {
        loggedUser = userSharedPref.getObject(PREF_KEY_USER, User.class);
        if (loggedUser != null && loggedUser.getToken() == null) {
            loggedUser = null;
        }
        mapFilterHashMap = userSharedPref.getObject(PREF_KEY_MAP_FILTER_HASHMAP, HashMap.class);
        if (mapFilterHashMap == null) {
            mapFilterHashMap = new HashMap<>();
        }
        if (loggedUser != null) {
            // Check if we have the old version of saving map filter
            mapFilter = userSharedPref.getObject(PREF_KEY_MAP_FILTER, MapFilter.class);
            if (mapFilter != null) {
                // Found old version, save it to the new structure
                saveMapFilter();
                // Delete it
                userSharedPref.putObject(PREF_KEY_MAP_FILTER, null);
            }
        }
        return this;
    }

    public void saveUser(User user) {
        if (loggedUser != null && loggedUser.getId() == user.getId()) {
            user.setPhone(loggedUser.getPhone());
            user.setSmsCode(loggedUser.getSmsCode());
            if (user != loggedUser) {
                user.setEntourageDisclaimerShown(loggedUser.isEntourageDisclaimerShown());
                user.setEncounterDisclaimerShown(loggedUser.isEncounterDisclaimerShown());
                user.setOnboardingUser(loggedUser.isOnboardingUser());
            }
        }
        loggedUser = user;
        userSharedPref.putObject(PREF_KEY_USER, user);
        userSharedPref.commit();
        BusProvider.getInstance().post(new Events.OnUserInfoUpdatedEvent());
    }

    public void saveUserPhoneAndCode(String phone, String smsCode) {
        loggedUser.setPhone(phone);
        loggedUser.setSmsCode(smsCode);
        userSharedPref.putObject(PREF_KEY_USER, loggedUser);
        userSharedPref.commit();
    }

    public void incrementUserToursCount() {
        loggedUser.incrementTours();
        userSharedPref.putObject(PREF_KEY_USER, loggedUser);
        userSharedPref.commit();
    }

    public void incrementUserEncountersCount() {
        loggedUser.incrementEncouters();
        userSharedPref.putObject(PREF_KEY_USER, loggedUser);
        userSharedPref.commit();
    }

    public void logOutUser() {
        if(loggedUser != null) {
            userSharedPref.putObject(PREF_KEY_USER, null);
            userSharedPref.commit();
        }
        if (mapFilter != null) {
            userSharedPref.putObject(PREF_KEY_MAP_FILTER, null);
            userSharedPref.commit();
        }
        loggedUser = null;
        mapFilter = null;
    }

    public boolean isAuthenticated() {
        return loggedUser != null;
    }

    public User getUser() {
        return loggedUser;
    }

    public void saveUserToursOnly(boolean choice) {
        this.userToursOnly = choice;
        userSharedPref.putObject(PREF_KEY_USER_TOURS_ONLY, userToursOnly);
    }

    public boolean isUserToursOnly() {
        return userToursOnly;
    }

    public boolean isShowNoEntouragesPopup() {
        return showNoEntouragesPopup;
    }

    public void setShowNoEntouragesPopup(final boolean showNoEntouragesPopup) {
        this.showNoEntouragesPopup = showNoEntouragesPopup;
    }

    public boolean isShowNoPOIsPopup() {
        return showNoPOIsPopup;
    }

    public void setShowNoPOIsPopup(final boolean showNoPOIsPopup) {
        this.showNoPOIsPopup = showNoPOIsPopup;
    }

    public MapFilter getMapFilter() {
        if (mapFilter == null && loggedUser != null && mapFilterHashMap != null) {
            mapFilter = mapFilterHashMap.get(loggedUser.getId());
            if (mapFilter == null) {
                // create the default map filter
                mapFilter = MapFilterFactory.getMapFilter(loggedUser.isPro());
                // save it
                saveMapFilter();
            }
        }
        return mapFilter;
    }

    public void saveMapFilter() {
        if (loggedUser == null) {
            return;
        }
        mapFilterHashMap.put(loggedUser.getId(), mapFilter);
        userSharedPref.putObject(PREF_KEY_MAP_FILTER_HASHMAP, this.mapFilterHashMap);
        userSharedPref.commit();
    }
}
