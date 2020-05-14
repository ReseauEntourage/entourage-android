package social.entourage.android.user.role

import java.util.*

/**
 * Factory for user roles
 * Created by Mihai Ionescu on 21/05/2018.
 */
open class BaseUserRolesFactory {
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
}