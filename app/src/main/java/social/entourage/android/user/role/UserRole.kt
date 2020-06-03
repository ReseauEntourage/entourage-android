package social.entourage.android.user.role

import androidx.annotation.ColorRes
import androidx.annotation.StringRes

/**
 * UI information of an user role
 * Created by Mihai Ionescu on 18/05/2018.
 */
class UserRole // ----------------------------------
// CONSTRUCTOR
// ----------------------------------
internal constructor(// ----------------------------------
        // GETTERS & SETTERS
        // ----------------------------------
        // ----------------------------------
        // ATTRIBUTES
        // ----------------------------------
        val name: String, @field:StringRes @param:StringRes val nameResourceId: Int, @field:ColorRes @param:ColorRes val colorResourceId: Int, val isVisible: Boolean)