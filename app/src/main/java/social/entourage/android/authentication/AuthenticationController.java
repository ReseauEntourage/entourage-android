package social.entourage.android.authentication;

import social.entourage.android.api.model.User;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

/**
 * Controller that managed the authenticated user and persist it on the phone
 */
public class AuthenticationController {

    private static final String PREF_KEY_USER = "user";
    private static final String PREF_KEY_USER_TOURS_ONLY = "user_tours_only";

    private final ComplexPreferences userSharedPref;
    private User loggedUser;
    private boolean userToursOnly;

    public AuthenticationController(ComplexPreferences userSharedPref) {
        this.userSharedPref = userSharedPref;
        loggedUser = null;
    }

    public AuthenticationController init() {
        loggedUser = userSharedPref.getObject(PREF_KEY_USER, User.class);
        if(loggedUser != null && loggedUser.getToken() == null) {
            loggedUser = null;
        }
        return this;
    }

    public void saveUser(User user) {
        if (loggedUser != null && loggedUser.getId() == user.getId()) {
            user.setPhone(loggedUser.getPhone());
            user.setSmsCode(loggedUser.getSmsCode());
            if (user != loggedUser) {
                user.setEntourageDisclaimerShown(loggedUser.isEntourageDisclaimerShown());
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
        loggedUser = null;
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
}
