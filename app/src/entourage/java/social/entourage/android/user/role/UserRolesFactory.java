package social.entourage.android.user.role;

import social.entourage.android.R;

/**
 * Entourage-related user roles
 * Created by Mihai Ionescu on 21/05/2018.
 */
public class UserRolesFactory extends BaseUserRolesFactory {

    // ----------------------------------
    // SINGLETON
    // ----------------------------------

    private static final UserRolesFactory ourInstance = new UserRolesFactory();

    public static UserRolesFactory getInstance() {
        return ourInstance;
    }

    private UserRolesFactory() {
        register(new UserRole("ambassador", R.string.role_ambassador, R.color.profile_role_ambassador, true));
    }

}
