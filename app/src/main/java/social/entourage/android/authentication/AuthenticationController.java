package social.entourage.android.authentication;

import android.content.SharedPreferences;

import social.entourage.android.api.model.User;

/**
 * Controller that managed the authenticated user and persist it on the phone
 */
public class AuthenticationController {

    private static final String PREF_KEY_ID = "id";
    private static final String PREF_KEY_FIRST_NAME = "firstName";
    private static final String PREF_KEY_LAST_NAME = "lastName";
    private static final String PREF_KEY_EMAIL = "email";
    private static final String PREF_KEY_TOKEN = "token";

    private final SharedPreferences userSharedPref;
    private User loggedUser;

    public AuthenticationController(SharedPreferences userSharedPref) {
        this.userSharedPref = userSharedPref;
        loggedUser = null;
    }

    public AuthenticationController init() {
        User.Builder builder = new User.Builder();
        builder.withId(userSharedPref.getInt(PREF_KEY_ID, -1));
        builder.withFirstName(userSharedPref.getString(PREF_KEY_FIRST_NAME, null));
        builder.withLastName(userSharedPref.getString(PREF_KEY_LAST_NAME, null));
        builder.withEmail(userSharedPref.getString(PREF_KEY_EMAIL, null));
        builder.withToken(userSharedPref.getString(PREF_KEY_TOKEN, null));
        loggedUser = builder.build();
        if(loggedUser!=null && loggedUser.getToken()==null) {
            loggedUser=null;
        }
        return this;
    }

    public void saveUser(User user) {
        loggedUser = user;
        final SharedPreferences.Editor edit = userSharedPref.edit();
        edit.putInt(PREF_KEY_ID, user.getId());
        edit.putString(PREF_KEY_FIRST_NAME, user.getFirstName());
        edit.putString(PREF_KEY_LAST_NAME, user.getLastName());
        edit.putString(PREF_KEY_EMAIL, user.getEmail());
        edit.putString(PREF_KEY_TOKEN, user.getToken());
        edit.apply();
    }

    public void logOutUser() {
        if(loggedUser!=null) {
            final SharedPreferences.Editor edit = userSharedPref.edit();
            //we force token to be null but we keep other information for input
            edit.putString(PREF_KEY_TOKEN, null);
            edit.apply();
        }
        loggedUser = null;
    }

    public boolean isAuthenticated() {
        return loggedUser != null;
    }

    public User getUser() {
        return loggedUser;
    }

}
