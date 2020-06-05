package social.entourage.android.user.role

import social.entourage.android.R
import java.util.ArrayList

/**
 * Entourage-related user roles
 * Created by Mihai Ionescu on 21/05/2018.
 */
object UserRolesFactory  {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private val userRoles = ArrayList<UserRole>()

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun register(userRole: UserRole) {
        userRoles.add(userRole)
    }

    fun findByName(name: String): UserRole? {
        for (userRole in userRoles) {
            if (name.equals(userRole.name, ignoreCase = true)) {
                return userRole
            }
        }
        return null
    }

    init {
        register(UserRole("ambassador", R.string.role_ambassador, R.color.profile_role_ambassador, true))
    }
}