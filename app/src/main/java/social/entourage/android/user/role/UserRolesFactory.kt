package social.entourage.android.user.role

import social.entourage.android.R

/**
 * Entourage-related user roles
 * Created by Mihai Ionescu on 21/05/2018.
 */
object UserRolesFactory : BaseUserRolesFactory() {
    init {
        register(UserRole("ambassador", R.string.role_ambassador, R.color.profile_role_ambassador, true))
    }
}