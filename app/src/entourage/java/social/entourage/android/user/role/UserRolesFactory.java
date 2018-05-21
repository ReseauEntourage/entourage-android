package social.entourage.android.user.role;

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
    }

}
