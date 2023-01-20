package social.entourage.android

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.onboarding.OnboardingMainActivity
import java.util.*

@LargeTest
@RunWith(AndroidJUnit4::class)
class SignUpTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(OnboardingMainActivity::class.java)

    private val login = "0651234145"
    private val password = "108674"

    /****************************** Views ******************************/

    private val firstNameEt = onView(
            allOf(withId(R.id.ui_onboard_names_et_firstname),
                    withParent(withId(R.id.onboard_names_mainlayout)),
                    isDisplayed()))

    private val lastNameEt = onView(
            allOf(withId(R.id.ui_onboard_names_et_lastname),
                    withParent(withId(R.id.onboard_names_mainlayout)),
                    isDisplayed()))

    private val askPhoneNumberTv = onView(
            allOf(withId(R.id.ui_onboard_phone_tv_description),
                    withParent(withId(R.id.onboard_phone_mainlayout)),
                    isDisplayed()))

    private val phoneNumberEt = onView(
            allOf(withId(R.id.ui_onboard_phone_et_phone),
                    withParent(withId(R.id.onboard_phone_mainlayout)),
                    isDisplayed()))

    private val placeTitleTv = onView(
            allOf(withId(R.id.ui_onboard_place_tv_title),
                    withParent(allOf(withId(R.id.onboard_phone_mainlayout))),
                    isDisplayed()))

    private val emailSubtitleTv = onView(
            allOf(withId(R.id.ui_onboard_email_tv_description),
                    isDisplayed()))

    private val emailEt = onView(
            allOf(withId(R.id.ui_onboard_email_pwd_et_mail),
                    withParent(withId(R.id.onboard_email_pwd_mainlayout)),
                    isDisplayed()))

    private val assoFillTitleTv = onView(
            allOf(withId(R.id.ui_onboard_email_tv_title),
                    isDisplayed()))

    private val activitiesSubtitleTv = onView(
            allOf(withId(R.id.ui_onboard_type_tv_info),
                    isDisplayed()))

    private val assoEmailSubtitleTv = onView(
            allOf(withId(R.id.ui_onboard_email_tv_description),
                    isDisplayed()))

    private val assoEmailEt = onView(
            allOf(withId(R.id.ui_onboard_email_pwd_et_mail),
                    isDisplayed()))

    private val feedButtonBottomBar = onView(
            allOf(withId(R.id.bottom_bar_newsfeed),
                withContentDescription(R.string.action_map),
                isDisplayed()))

    private val nextButton = onView(
            allOf(withId(R.id.ui_bt_next),
                    isDisplayed()))

    /****************************** OnboardingNamesFragment ******************************/

    @Test
    fun validFirstNameAndLastNameTest() {
        fillValidNames()

        //Check that OnboardingPhoneFragment is displayed
        askPhoneNumberTv.check(matches(withText(R.string.onboard_phone_sub)))

        //Check that OnboardingNamesFragment is not displayed
        firstNameEt.check(doesNotExist())
        lastNameEt.check(doesNotExist())
    }

    @Test
    fun emptyFirstNameAndLastNameTest() {
        firstNameEt.perform(typeText(""), closeSoftKeyboard())
        lastNameEt.perform(typeText(""), closeSoftKeyboard())
        clickNextButton()

        //Check that OnboardingNamesFragment is still displayed
        firstNameEt.check(matches(isDisplayed()))
        lastNameEt.check(matches(isDisplayed()))

        //Check that OnboardingPhoneFragment is not displayed
        askPhoneNumberTv.check(doesNotExist())
    }

    @Test
    fun emptyFirstNameTest() {
        firstNameEt.perform(typeText(""), closeSoftKeyboard())
        lastNameEt.perform(typeText("Dupont"), closeSoftKeyboard())
        clickNextButton()

        //Check that OnboardingNamesFragment is still displayed
        firstNameEt.check(matches(isDisplayed()))
        lastNameEt.check(matches(isDisplayed()))

        //Check that OnboardingPhoneFragment is not displayed
        askPhoneNumberTv.check(doesNotExist())
    }

    @Test
    fun emptyLastNameTest() {
        firstNameEt.perform(typeText("Jean"), closeSoftKeyboard())
        lastNameEt.perform(typeText(""), closeSoftKeyboard())
        clickNextButton()

        //Check that OnboardingNamesFragment is still displayed
        firstNameEt.check(matches(isDisplayed()))
        lastNameEt.check(matches(isDisplayed()))

        //Check that OnboardingPhoneFragment is not displayed
        askPhoneNumberTv.check(doesNotExist())
    }

    /****************************** OnboardingPhoneFragment ******************************/

    //TODO For now there is no simple way to send a phone number and remove it to be able running the test again
    // It would need for instance an API call that remove the sent number so that it is not already used for the next test call
//    @Test
//    fun validPhoneNumberTest() {
//        fillValidNames()
//
//        phoneNumberEt.perform(typeText(""), closeSoftKeyboard())
//        clickNextButton()
//
//        //Check that toast shows given message
//        onView(withText(R.string.login_smscode_sent)).inRoot(ToastMatcher()).check(matches(isDisplayed()))
//    }

    @Test
    fun emptyPhoneNumberTest() {
        fillValidNames()

        phoneNumberEt.perform(typeText(""), closeSoftKeyboard())
        clickNextButton()

        //Check that OnboardingPhoneFragment is still displayed
        phoneNumberEt.check(matches(isDisplayed()))

        //Check that OnboardingPasscodeFragment is not displayed
        val inputCodeTitleTv = onView(allOf(withId(R.id.ui_onboard_code_tv_title), isDisplayed()))
        inputCodeTitleTv.check(doesNotExist())
    }

    @Test
    fun invalidPhoneNumberTest() {
        fillValidNames()

        phoneNumberEt.perform(typeText("000000000"), closeSoftKeyboard())
        clickNextButton()

        //Check that error shows given message
        onView(withText(R.string.login_text_invalid_format)).check(matches(isDisplayed()))
    }

    @Test
    fun alreadyUsedPhoneNumberTest() {
        fillValidNames()

        phoneNumberEt.perform(typeText("123456789"), closeSoftKeyboard())
        clickNextButton()

        //Check that dialog shows given message
        Thread.sleep(5000) //Wait for API response
        onView(withText(R.string.login_already_registered_go_back)).check(matches(isDisplayed()))
    }

    @Test
    fun phoneNumberFailureNoInternetConnection() {
        //Disable wifi and data
        enableWifiAndData(false)

        //Try to submit phone number
        fillValidNames()
        phoneNumberEt.perform(typeText("123456789"), closeSoftKeyboard())
        clickNextButton()

        //Check that error is displayed
        onView(allOf(withId(R.id.error_message_tv), isDisplayed())).check(matches(withText(R.string.login_error_network)))

        //Enable wifi and data
        enableWifiAndData(true)
    }

    /****************************** OnboardingPasscodeFragment ******************************/

    @Test
    fun validPasscodeTest() {
        fillValidNames()

        //Skip phone number validation and go to passcode fragment
        goNextStep()

        //Login to an already existing account
        login(login, password)

        //Skip passcode validation and go to type fragment
        goNextStep()

        //Check that OnboardingTypeFragment is displayed
        val typeSubTitleTv = onView(
                allOf(withId(R.id.ui_onboard_type_tv_description),
                        withParent(withId(R.id.onboard_phone_mainlayout)),
                        isDisplayed()))
        typeSubTitleTv.check(matches(withText(R.string.onboard_type_sub)))
    }

    /****************************** OnboardingTypeFragment ******************************/

    @Test
    fun neighbourTypeTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_neighbour)

        //Check that OnboardingPlaceFragment is displayed
        placeTitleTv.check(matches(withText(R.string.onboard_place_title)))
    }

    @Test
    fun aloneTypeTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        //Check that OnboardingPlaceFragment is displayed
        placeTitleTv.check(matches(withText(R.string.onboard_place_title_sdf)))
    }

    @Test
    fun assosTypeTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_assos)

        //Check that OnboardingAssoStartFragment is displayed
        val assoTitleTv = onView(
                allOf(withId(R.id.ui_onboard_asso_start_tv_title),
                    withParent(withId(R.id.onboard_phone_mainlayout)),
                    isDisplayed()))
        assoTitleTv.check(matches(withText(R.string.onboard_asso_start_title)))
    }

    /****************************** OnboardingPlaceFragment ******************************/

    @Test
    fun neighbourPlaceTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_neighbour)

        pickLocation("paris")

        //Check that LoginEmailFragment is displayed
        emailSubtitleTv.check(matches(withText(R.string.login_email_description)))
    }

    @Test
    fun alonePlaceTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        pickLocation("toulouse")

        //Check that LoginEmailFragment is displayed
        emailSubtitleTv.check(matches(withText(R.string.login_email_description)))
    }

    @Test
    fun neighbourEmptyPlaceTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_neighbour)

        clickNextButton()

        //Check that OnboardingPlaceFragment is still displayed
        placeTitleTv.check(matches(withText(R.string.onboard_place_title)))
    }

    @Test
    fun aloneEmptyPlaceTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        clickNextButton()

        //Check that OnboardingPlaceFragment is still displayed
        placeTitleTv.check(matches(withText(R.string.onboard_place_title_sdf)))
    }

    /****************************** LoginEmailFragment ******************************/

    @Test
    fun neighbourValidMailTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_neighbour)

        pickLocation("paris")

        emailEt.perform(typeText("jean.dupont@jeandupont.fr"), closeSoftKeyboard())

        //Lose email edit text focus to enable next button
        emailSubtitleTv.perform(click())

        clickNextButton()

        //Check that OnboardingMainActivity is displayed
        feedButtonBottomBar.check(matches(isDisplayed()))
    }

    @Test
    fun neighbourInvalidMailTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_neighbour)

        pickLocation("paris")

        testInvalidEmail()
    }

    @Test
    fun neighbourEmptyMailTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_neighbour)

        pickLocation("paris")

        clickNextButton()

        //Check that LoginEmailFragment is still displayed
        emailSubtitleTv.check(matches(withText(R.string.login_email_description)))
    }

    @Test
    fun aloneValidMailTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        pickLocation("toulouse")

        emailEt.perform(typeText("jean.dupont@jeandupont.fr"), closeSoftKeyboard())

        //Lose email edit text focus to enable next button
        emailSubtitleTv.perform(click())

        clickNextButton()

        //Check that OnboardingMainActivity is displayed
        feedButtonBottomBar.check(matches(isDisplayed()))
    }

    @Test
    fun aloneInvalidMailTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        pickLocation("toulouse")

        testInvalidEmail()
    }

    @Test
    fun aloneEmptyMailTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        pickLocation("toulouse")

        clickNextButton()

        //Check that LoginEmailFragment is still displayed
        emailSubtitleTv.check(matches(withText(R.string.login_email_description)))
    }

    /****************************** OnboardingAssoStartFragment ******************************/

    @Test
    fun assoTunnelTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_assos)

        clickNextButton()

        //Check that OnboardingAssoStartFragment 2nd screen is displayed
        val assoTitleTv = onView(
                allOf(withId(R.id.ui_onboard_asso_info_tv_title),
                        withParent(withId(R.id.onboard_phone_mainlayout)),
                        isDisplayed()))
        assoTitleTv.check(matches(withText(R.string.onboard_asso_info_title)))

        clickNextButton()

        //Check that OnboardingAssoFillFragment is displayed
        assoFillTitleTv.check(matches(withText(R.string.onboard_asso_fill_title)))
    }

    /****************************** OnboardingAssoFillFragment ******************************/

    @Test
    fun searchAssoNameTest() {
        goToAssoInfo()

        Intents.init()
        val assoNameLayout = onView(
                allOf(withId(R.id.ui_layout_asso_fill_location),
                        isDisplayed()))
        assoNameLayout.perform(click())

        //Check that OnboardingAssoSearchActivity is displayed
        Intents.intended(IntentMatchers.hasComponent(OnboardingAssoSearchActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun fillAssoInfoTest() {
        goToAssoInfo()

        fillAssoName()
        fillPostalCode()
        fillFunction()

        clickNextButton()

        //Check that OnboardingAssoActivitiesFragment is displayed
        activitiesSubtitleTv.check(matches(withText(R.string.onboard_asso_activity_description)))
    }

    @Test
    fun missingAssoNameTest() {
        goToAssoInfo()

        fillPostalCode()
        fillFunction()

        clickNextButton()

        //Check that OnboardingAssoFillFragment is still displayed
        assoFillTitleTv.check(matches(withText(R.string.onboard_asso_fill_title)))
    }

    @Test
    fun missingPostalCodeTest() {
        goToAssoInfo()

        fillAssoName()
        fillFunction()

        clickNextButton()

        //Check that error message is displayed
        onView(withText(R.string.onboard_asso_fill_error)).check(matches(isDisplayed()))
    }

    @Test
    fun missingFunctionTest() {
        goToAssoInfo()

        fillAssoName()
        fillPostalCode()

        clickNextButton()

        //Check that error message is displayed
        onView(withText(R.string.onboard_asso_fill_error)).check(matches(isDisplayed()))
    }

    /****************************** OnboardingAssoActivitiesFragment ******************************/

    @Test
    fun pick4AssoActivitiesTest() {
        goToAssoActivities()

        clickAssoActivity1()
        clickAssoActivity2()
        clickAssoActivity3()
        clickAssoActivity4()

        clickNextButton()

        //Check that OnboardingEmailPwdFragment is Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }

    @Test
    fun pick3AssoActivitiesTest() {
        goToAssoActivities()

        clickAssoActivity2()
        clickAssoActivity3()
        clickAssoActivity4()

        clickNextButton()

        //Check that OnboardingEmailPwdFragment is Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }

    @Test
    fun pick2AssoActivitiesTest() {
        goToAssoActivities()

        clickAssoActivity1()
        clickAssoActivity3()

        clickNextButton()

        //Check that OnboardingEmailPwdFragment is Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }

    @Test
    fun pick1AssoActivityTest() {
        goToAssoActivities()

        clickAssoActivity2()

        clickNextButton()

        //Check that OnboardingEmailPwdFragment is Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }

    @Test
    fun pickNoAssoActivitiesTest() {
        goToAssoActivities()

        clickNextButton()

        //Check that error message is displayed
        onView(withText(R.string.onboard_asso_activity_error)).check(matches(isDisplayed()))
    }

    /****************************** OnboardingEmailPwdFragment ******************************/

    @Test
    fun assoValidMailTest() {
        goToAssoEmail()

        assoEmailEt.perform(typeText("jean.dupont@jeandupont.fr"), closeSoftKeyboard())

        clickNextButton()

        //Check that OnboardingMainActivity is displayed
        feedButtonBottomBar.check(matches(isDisplayed()))
    }

    @Test
    fun assoInvalidMailTest() {
        goToAssoEmail()

        assoEmailEt.perform(typeText("jean.dupont@jeandupont"), closeSoftKeyboard())

        clickNextButton()

        //Check that OnboardingEmailPwdFragment is still Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }

    @Test
    fun assoEmptyMailTest() {
        goToAssoEmail()

        clickNextButton()

        //Check that OnboardingEmailPwdFragment is still Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }

    /****************************** Utils ******************************/

    private fun clickNextButton() {
        nextButton.perform(click())
        Thread.sleep(1000)
    }

    private fun fillValidNames() {
        firstNameEt.perform(typeText("Jean"), closeSoftKeyboard())
        lastNameEt.perform(typeText("Dupont"), closeSoftKeyboard())
        clickNextButton()
    }

    private fun goNextStep() {
        activityRule.scenario.onActivity { activity ->
            activity.goNextStep()
        }
    }

    private fun clickTypeButton(buttonId: Int) {
        fillValidNames()

        //Skip phone number validation and go to passcode fragment
        goNextStep()

        //Login to an already existing account
        login(login, password)

        //Skip passcode validation and go to type fragment
        goNextStep()

        val typeButton = onView(allOf(withId(buttonId), isDisplayed()))
        typeButton.perform(click())
        Thread.sleep(1000)

        clickNextButton()
    }

    private fun login(phoneNumber: String, codePwd: String) {
        val authenticationController = EntourageApplication.get().authenticationController
        OnboardingAPI.getInstance().login(phoneNumber, codePwd) { isOK, loginResponse, _ ->
            if (isOK) {
                loginResponse?.let {
                    authenticationController.saveUser(loginResponse.user)
                }
                authenticationController.saveUserPhoneAndCode(phoneNumber, codePwd)

                //set the tutorial as done
                val sharedPreferences = EntourageApplication.get().sharedPreferences
                (sharedPreferences.getStringSet(EntourageApplication.KEY_TUTORIAL_DONE, HashSet()) as HashSet<String>?)?.let {loggedNumbers ->
                    loggedNumbers.add(phoneNumber)
                    sharedPreferences.edit().putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, loggedNumbers).apply()
                }
            }
        }
    }

    private fun pickLocation(locationInput: String) {
        val locationTv = onView(allOf(withId(R.id.ui_onboard_place_tv_location), isDisplayed()))
        locationTv.perform(click())

        val locationSearchBar = onView(
                allOf(withId(R.id.places_autocomplete_search_bar), isDisplayed()))
        locationSearchBar.perform(click())
        locationSearchBar.perform(typeText(locationInput), closeSoftKeyboard())

        Thread.sleep(1000) //Wait for search results

        val locationsResultRv = onView(withId(R.id.places_autocomplete_list))
        locationsResultRv.perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        clickNextButton()
    }

    private fun testInvalidEmail() {
        emailEt.perform(typeText("jean.dupont@jeandupont"), closeSoftKeyboard())

        //Lose email edit text focus to enable next button
        emailSubtitleTv.perform(click())

        clickNextButton()

        //Check that LoginEmailFragment is still displayed
        emailSubtitleTv.check(matches(withText(R.string.login_email_description)))
    }

    private fun goToAssoInfo() {
        clickTypeButton(R.id.ui_onboard_type_layout_assos)

        clickNextButton()

        clickNextButton()
    }

    private fun fillAssoName() {
        val assoNameLayout = onView(
                allOf(withId(R.id.ui_layout_asso_fill_location),
                        isDisplayed()))
        assoNameLayout.perform(click())

        Thread.sleep(1000) //Wait for search view to appear

        val searchAssoNameEt = onView(
                allOf(withId(R.id.ui_asso_search_et_search),
                        isDisplayed()))
        searchAssoNameEt.perform(typeText("test"))

        val assoNameResultsRv = onView(
                allOf(withId(R.id.ui_asso_search_rv),
                        isDisplayed()))
        assoNameResultsRv.perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        val validateButton = onView(
                allOf(withId(R.id.ui_bt_asso_search_validate),
                        isDisplayed()))
        validateButton.perform(click())

        Thread.sleep(1000) //Wait for search view to disappear
    }

    private fun fillPostalCode() {
        val postalCodeEt = onView(
                allOf(withId(R.id.ui_onboard_asso_fill_postal_code),
                        isDisplayed()))
        postalCodeEt.perform(typeText("75008"), closeSoftKeyboard())
    }

    private fun fillFunction() {
        val functionEt = onView(
                allOf(withId(R.id.ui_onboard_asso_fill_function),
                        isDisplayed()))
        functionEt.perform(typeText("directeur"), closeSoftKeyboard())
    }

    private fun goToAssoActivities() {
        goToAssoInfo()

        fillAssoName()
        fillPostalCode()
        fillFunction()

        clickNextButton()
    }

    private fun clickAssoActivity1() {
        val assoActivity1Layout = onView(
                allOf(withId(R.id.ui_onboard_asso_activities_layout_choice1),
                        isDisplayed()))
        assoActivity1Layout.perform(click())
    }

    private fun clickAssoActivity2() {
        val assoActivity2Layout = onView(
                allOf(withId(R.id.ui_onboard_asso_activities_layout_choice2),
                        isDisplayed()))
        assoActivity2Layout.perform(click())
    }

    private fun clickAssoActivity3() {
        val assoActivity3Layout = onView(
                allOf(withId(R.id.ui_onboard_asso_activities_layout_choice3),
                        isDisplayed()))
        assoActivity3Layout.perform(click())
    }

    private fun clickAssoActivity4() {
        val assoActivity4Layout = onView(
                allOf(withId(R.id.ui_onboard_asso_activities_layout_choice4),
                        isDisplayed()))
        assoActivity4Layout.perform(click())
    }

    private fun goToAssoEmail() {
        goToAssoActivities()

        clickAssoActivity1()
        clickAssoActivity2()
        clickAssoActivity3()
        clickAssoActivity4()

        clickNextButton()
    }

    private fun enableWifiAndData(enable: Boolean) {
        val parameter = if (enable) "enable" else "disable"
        InstrumentationRegistry.getInstrumentation().uiAutomation.apply {
            executeShellCommand("svc wifi $parameter")
            executeShellCommand("svc data $parameter")
        }
    }
}
