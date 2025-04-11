package social.entourage.android.onboarding.onboard

import social.entourage.android.api.model.User
import social.entourage.android.tools.view.countrycodepicker.Country
import java.io.Serializable

/**
 * Created on 05/12/2022.
 */

/**********
 * Enums
 */

enum class PositionsType(val pos: Int) {
    NamesPhone(1),
    Passcode(2),
    Type(3)
}

enum class UserTypeSelection(val pos: Int) {
    NEIGHBOUR(1),
    ALONE(2),
    ASSOS(3),
    BOTH(4),
    NONE(0);

    fun getGoalString(): String {
        return when (this) {
            NEIGHBOUR -> User.USER_GOAL_NEIGHBOUR
            ALONE -> User.USER_GOAL_ALONE
            ASSOS -> User.USER_GOAL_ASSO
            NONE -> User.USER_GOAL_NONE
            BOTH ->  User.USER_GOAL_BOTH
        }
    }
}

/**********
 * Interface
 */
interface OnboardingStartCallback {
    fun validateNames(firstname: String?, lastname: String?, country: Country?, phoneNumber: String?, email: String?, hasConsent:Boolean)
    fun validatePasscode(password: String?)
    fun updateUsertypeAndAddress(isEntour:Boolean, isBeEntour:Boolean, both:Boolean, isAsso:Boolean,address:User.Address?)
    fun updateButtonNext(isValid: Boolean)
    fun goPreviousManually()
    fun requestNewCode()
}

interface OnboardingChoosePlaceCallback {
    fun updatePlace(address: User.Address?)
}

/********************************
 * Class AssoActivities
 ********************************/

class AssoActivities : Serializable {
    var choice1Selected = false
    var choice2Selected = false
    var choice3Selected = false
    var choice4Selected = false

    fun hasOneSelectionMin() : Boolean {
        if (choice1Selected || choice2Selected
            || choice3Selected || choice4Selected) return true

        return false
    }

    fun reset() {
        choice1Selected = false
        choice2Selected = false
        choice3Selected = false
        choice4Selected = false
    }

    fun getArrayForWs() : ArrayList<String> {
        val _array = ArrayList<String>()
        if (choice1Selected) _array.add("aide_pers_asso")
        if (choice2Selected) _array.add("cult_sport_asso")
        if (choice3Selected) _array.add("serv_pub_asso")
        if (choice4Selected) _array.add("autre_asso")

        return _array
    }
}

/********************************
 * Class SdfNeighbourActivities
 ********************************/

class SdfNeighbourActivities : Serializable {
    var choice1Selected = false
    var choice2Selected = false
    var choice3Selected = false
    var choice4Selected = false
    var choice5Selected = false
    var choice6Selected = false

    var isSdf = true

    fun hasOneSelectionMin() : Boolean {
        if (isSdf) {
            if (choice1Selected || choice2Selected
                || choice3Selected || choice4Selected
                || choice5Selected || choice6Selected) return true
        }
        else {
            if (choice1Selected || choice2Selected
                || choice3Selected || choice4Selected || choice5Selected) return true
        }

        return false
    }

    fun reset() {
        choice1Selected = false
        choice2Selected = false
        choice3Selected = false
        choice4Selected = false
        choice5Selected = false
        choice6Selected = false
    }

    fun getArrayForWs() : ArrayList<String> {
        val _array = ArrayList<String>()
        if (choice1Selected) {
            val _choice = if (isSdf) "rencontrer_sdf" else "m_informer_riverain"
            _array.add(_choice)
        }
        if (choice2Selected) {
            val _choice = if (isSdf) "event_sdf" else "event_riverain"
            _array.add(_choice)
        }
        if (choice3Selected) {
            val _choice = if (isSdf) "questions_sdf" else "entourer_riverain"
            _array.add(_choice)
        }
        if (choice4Selected) {
            val _choice = if (isSdf) "aide_sdf" else "dons_riverain"
            _array.add(_choice)
        }
        if (choice5Selected) {
            val _choice = if (isSdf) "m_orienter_sdf" else "benevolat_riverain"
            _array.add(_choice)
        }
        if (choice6Selected) _array.add("trouver_asso_sdf")

        return _array
    }

    fun setupForSdf(isSdf:Boolean) {
        this.isSdf = isSdf

        choice1Selected = true
        choice2Selected = true
        choice3Selected = true
        choice4Selected = true
        choice5Selected = true

        choice6Selected = isSdf
    }
}