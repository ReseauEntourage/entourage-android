package social.entourage.android.user.role;

import social.entourage.android.R;

/**
 * PFP-related user roles
 * Created by Mihai Ionescu on 21/05/2018.
 */
public class UserRolesFactory extends BaseUserRolesFactory {

    // ----------------------------------
    // SINGLETON
    // ----------------------------------

    private static final UserRolesFactory ourInstance = new UserRolesFactory();

    public static BaseUserRolesFactory getInstance() {
        return ourInstance;
    }

    private UserRolesFactory() {
        register(new UserRole("not_validated", R.string.role_not_validated, R.color.profile_role_pending, UserRole.Position.RIGHT, true));
        register(new UserRole("visitor", R.string.role_visitor, 0, UserRole.Position.RIGHT, false));
        register(new UserRole("visited", R.string.role_visited, 0, UserRole.Position.RIGHT, false));
        register(new UserRole("coordinator", R.string.role_coordinator, R.color.profile_role_accepted, UserRole.Position.RIGHT, true));
    }

}
