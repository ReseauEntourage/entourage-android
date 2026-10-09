package social.entourage.android.beforeLogin

import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotExist
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import com.adevinta.android.barista.interaction.BaristaEditTextInteractions.writeTo
import com.adevinta.android.barista.interaction.BaristaKeyboardInteractions.closeKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import social.entourage.android.R

fun signUpRobot(func: SignUpRobot.() -> Unit) = SignUpRobot().apply { func() }

class SignUpRobot {

    fun typeFirstName(firstName: String) {
        writeTo(R.id.ui_onboard_names_et_firstname, firstName)
        closeKeyboard()
    }

    fun typeLastName(lastName: String) {
        writeTo(R.id.ui_onboard_names_et_lastname, lastName)
        closeKeyboard()
    }

    fun typePhoneNumber(phoneNumber: String) {
        writeTo(R.id.ui_onboard_phone_et_phone, phoneNumber)
        closeKeyboard()
    }

    fun typeEmail(email: String) {
        writeTo(R.id.ui_onboard_email, email)
        closeKeyboard()
    }

    fun clickConsent() {
        clickOn(R.id.ui_onboard_consent_check)
    }

    fun clickNext() {
        clickOn(R.id.ui_onboarding_bt_next)
    }

    fun fillNames(firstName: String = "Jean", lastName: String = "Dupont") {
        typeFirstName(firstName)
        typeLastName(lastName)
    }

    fun fillValidNames(firstName: String = "Jean", lastName: String = "Dupont") {
        fillNames(firstName, lastName)
        clickNext()
    }

    infix fun verify(func: SignUpVerificationRobot.() -> Unit) =
        SignUpVerificationRobot().apply { func() }
}

class SignUpVerificationRobot {

    fun isNamesScreenDisplayed() {
        assertDisplayed(R.id.ui_onboard_names_et_firstname)
        assertDisplayed(R.id.ui_onboard_names_et_lastname)
    }

    fun isPhoneScreenDisplayed() {
        assertDisplayed(R.id.ui_onboard_phone_et_phone)
    }

    fun isPhoneScreenDescriptionDisplayed() {
        assertDisplayed(R.id.ui_onboard_code_tv_description)
    }

    fun isNamesScreenNotDisplayed() {
        assertNotExist(R.id.ui_onboard_names_et_firstname)
        assertNotExist(R.id.ui_onboard_names_et_lastname)
    }

    fun isPhoneScreenNotDisplayed() {
        assertNotExist(R.id.ui_onboard_code_tv_description)
    }

    fun isInvalidPhoneErrorDisplayed() {
        onView(withText(R.string.login_error_invalid_phone_format))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    fun isAlreadyRegisteredErrorDisplayed() {
        onView(withText(R.string.login_already_registered_go_back))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    fun isEmailErrorDisplayed() {
        assertDisplayed(R.id.error_message_email)
    }

    fun isNetworkErrorDisplayed() {
        onView(withText(R.string.login_error_network))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }
}
