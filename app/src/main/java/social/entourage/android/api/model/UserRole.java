package social.entourage.android.api.model;

import android.support.annotation.StringRes;

import social.entourage.android.R;

/**
 * Created by Mihai Ionescu on 18/05/2018.
 */
public enum UserRole {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    NOT_VALIDATED("not_validated", R.string.role_not_validated),
    VISITOR("visitor", R.string.role_visitor),
    VISITED("visited", R.string.role_visited),
    COORDINATOR("coordinator", R.string.role_coordinator);

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final String name;
    private final @StringRes int resourceId;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    UserRole(String name, @StringRes int resourceId) {
        this.name = name;
        this.resourceId = resourceId;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------


    public String getName() {
        return name;
    }

    public int getResourceId() {
        return resourceId;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public static UserRole findByName(String name) {
        if (name == null) return null;
        for (UserRole userRole : UserRole.values()) {
            if (name.equalsIgnoreCase(userRole.name)) {
                return userRole;
            }
        }
        return null;
    }

}
