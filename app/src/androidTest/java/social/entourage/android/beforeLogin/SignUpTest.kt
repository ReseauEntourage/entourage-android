package social.entourage.android.beforeLogin

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.BuildConfig
import social.entourage.android.onboarding.onboard.OnboardingStartActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class SignUpTest : EntourageTestBeforeLogin() {

    @get:Rule
    var activityRule = ActivityScenarioRule(OnboardingStartActivity::class.java)

    @Before
    fun setup() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun emptyFirstNameAndLastNameTest() {
        signUpRobot {
            typeFirstName("")
            typeLastName("")
            clickNext()
        } verify {
            isNamesScreenDisplayed()
            isPhoneScreenNotDisplayed()
        }
    }

    @Test
    fun emptyFirstNameTest() {
        signUpRobot {
            typeFirstName("")
            typeLastName("Dupont")
            clickNext()
        } verify {
            isNamesScreenDisplayed()
            isPhoneScreenNotDisplayed()
        }
    }

    @Test
    fun emptyLastNameTest() {
        signUpRobot {
            typeFirstName("Jean")
            typeLastName("")
            clickNext()
        } verify {
            isNamesScreenDisplayed()
            isPhoneScreenNotDisplayed()
        }
    }

    @Test
    fun emptyPhoneNumberTest() {
        signUpRobot {
            fillNames()
            typePhoneNumber("")
            clickNext()
        } verify {
            isPhoneScreenDisplayed()
        }
    }

    @Test
    fun invalidPhoneNumberTest() {
        signUpRobot {
            fillNames()
            typePhoneNumber("000000000")
            clickNext()
        } verify {
            isInvalidPhoneErrorDisplayed()
        }
    }

    @Test
    fun alreadyUsedPhoneNumberTest() {
        signUpRobot {
            fillNames()
            typePhoneNumber(BuildConfig.TEST_ACCOUNT_LOGIN)
            clickNext()
        } verify {
            isAlreadyRegisteredErrorDisplayed()
        }
    }

    @Test
    fun phoneNumberFailureNoInternetConnectionTest() {
        enableWifiAndData(false)

        signUpRobot {
            fillNames()
            typePhoneNumber("123456789")
            clickNext()
        } verify {
            isNetworkErrorDisplayed()
        }
    }
}
