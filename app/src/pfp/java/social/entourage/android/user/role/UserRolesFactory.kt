package social.entourage.android.user.role

import social.entourage.android.R

/**
 * PFP-related user roles
 * Created by Mihai Ionescu on 21/05/2018.
 */
object UserRolesFactory : BaseUserRolesFactory() {
    fun isVisited(role: String?): Boolean {
        val userRole = findByName(role)
        return userRole?.name?.equals("visited", ignoreCase = true) ?: false
    }

    init {
        register(UserRole("not_validated", R.string.role_not_validated, R.color.profile_role_pending, true))
        register(UserRole("visitor", R.string.role_visitor, 0, false))
        register(UserRole("visited", R.string.role_visited, 0, false))
        register(UserRole("coordinator", R.string.role_coordinator, R.color.profile_role_accepted, true))
    }
}