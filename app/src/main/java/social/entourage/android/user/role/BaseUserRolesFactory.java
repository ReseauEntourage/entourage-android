package social.entourage.android.user.role;

import java.util.ArrayList;

/**
 * Factory for user roles
 * Created by Mihai Ionescu on 21/05/2018.
 */
public class BaseUserRolesFactory {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private ArrayList<UserRole> userRoles = new ArrayList<>();

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void register(UserRole userRole) {
        userRoles.add(userRole);
    }

    public UserRole findByName(String name) {
        if (name == null) return null;
        for (UserRole userRole : userRoles) {
            if (name.equalsIgnoreCase(userRole.getName())) {
                return userRole;
            }
        }
        return null;
    }

}
