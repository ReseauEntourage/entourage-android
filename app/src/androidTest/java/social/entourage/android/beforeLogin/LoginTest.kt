package social.entourage.android.beforeLogin

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.BuildConfig
import social.entourage.android.R
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.onboarding.login.LoginActivity

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginTest : EntourageTestBeforeLogin() {

    private val screenshot = E2EScreenshot("login")
    private var scenario: ActivityScenario<LoginActivity>? = null

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(LoginActivity::class.java)
        scenario?.onActivity { activity ->
            super.setUp(activity)
            activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity.setFinishOnTouchOutside(false)
        }
        Thread.sleep(5000)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        scenario?.close()
    }

    private fun closeAutofill() {
        scenario?.onActivity { activity ->
            closeAutofill(activity)
        }
    }

    @Test
    fun loginOK() {
        loginRobot {
            typePhoneNumber(BuildConfig.TEST_ACCOUNT_LOGIN)
            typeCode(BuildConfig.TEST_ACCOUNT_PWD)
            clickSubmit()
        } verify {
            isLoginSuccessful()
        }
        screenshot.shoot("login_ok")
    }

    @Test
    fun loginOKWithoutCountryCode() {
        loginRobot {
            typePhoneNumber(BuildConfig.TEST_ACCOUNT_LOGIN.replaceFirst("\\+33".toRegex(), "0"))
            typeCode(BuildConfig.TEST_ACCOUNT_PWD)
            clickSubmit()
        } verify {
            isLoginSuccessful()
        }
        screenshot.shoot("login_ok_no_country_code")
    }

    @Test
    fun loginFailureWrongPassword() {
        loginRobot {
            typePhoneNumber(BuildConfig.TEST_ACCOUNT_LOGIN)
            typeCode("999999")
            clickSubmit()
        } verify {
            isLoginFailureDisplayed()
        }
        screenshot.shoot("login_failure_wrong_pwd")
    }

    @Test
    fun loginFailureShortPassword() {
        loginRobot {
            typePhoneNumber(BuildConfig.TEST_ACCOUNT_LOGIN)
            typeCode("9999")
            clickSubmit()
        } verify {
            isLoginFailureDisplayed(R.string.attention_pop_title, R.string.close)
        }
        screenshot.shoot("login_failure_short_pwd")
    }

    @Test
    fun loginFailureWrongPhoneNumberFormat() {
        loginRobot {
            typePhoneNumber("012345678")
            typeCode("000000")
            clickSubmit()
        } verify {
            isLoginFailureDisplayed()
        }
        screenshot.shoot("login_failure_wrong_phone")
    }

    @Test
    fun loginFailureNoInternetConnection() {
        enableWifiAndData(false)

        loginRobot {
            typePhoneNumber(BuildConfig.TEST_ACCOUNT_LOGIN)
            typeCode(BuildConfig.TEST_ACCOUNT_PWD)
            clickSubmit()
        } verify {
            isNetworkErrorDisplayed()
        }
        screenshot.shoot("login_failure_no_internet")
    }

    @Test
    fun resendCodeButtonWithoutClick() {
        loginRobot {
            typePhoneNumber(BuildConfig.TEST_ACCOUNT_LOGIN)
            clickResendCode()
        } verify {
            isResendCodeActionDisplayed()
        }
        screenshot.shoot("resend_code_action")
    }

    @Test
    fun resendCodeButtonWithEmptyPhoneNumber() {
        loginRobot {
            clearPhoneNumber()
            clickResendCode()
        } verify {
            isLoginFailureDisplayed(R.string.attention_pop_title, R.string.close)
        }
        screenshot.shoot("resend_code_empty_phone")
    }

    @Test
    fun displayChangePhoneNumberScreen() {
        loginRobot {
            clickChangePhone()
        } verify {
            isChangePhoneScreenDisplayed()
        }
        screenshot.shoot("change_phone_screen")
    }

    @Test
    fun clickGoBack() {
        loginRobot {
            clickBack()
        } verify {
            isPreOnboardingScreenDisplayed()
        }
        screenshot.shoot("pre_onboarding_screen")
    }

    //@Test
    fun resendCodeFailureNoInternetConnection() {
        enableWifiAndData(false)

        loginRobot {
            typePhoneNumber(BuildConfig.TEST_ACCOUNT_LOGIN)
            closeAutofill()
            clickResendCode()
            clickResendCodeAction()
        }
        // Original code had a TODO for Toast check
    }
}
