package social.entourage.android


import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
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
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.onboarding.OnboardingMainActivity
import social.entourage.android.onboarding.asso.OnboardingAssoSearchActivity
import java.util.*

@LargeTest
@RunWith(AndroidJUnit4::class)
class SignUpTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(OnboardingMainActivity::class.java)


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

    private val homeNeoTitleTv = onView(
            allOf(withId(R.id.ui_home_neo_start_title),
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
            allOf(withId(R.id.bottom_bar_newsfeed), withContentDescription(R.string.action_map),
                    childAtPosition(
                            childAtPosition(
                                    withId(R.id.bottom_navigation),
                                    0),
                            0),
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
        nextButton.perform(click())

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
        nextButton.perform(click())

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
        nextButton.perform(click())

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
//        nextButton.perform(click())
//
//        //Check that toast shows given message
//        onView(withText(R.string.login_smscode_sent)).inRoot(ToastMatcher()).check(matches(isDisplayed()))
//    }

    @Test
    fun emptyPhoneNumberTest() {
        fillValidNames()

        phoneNumberEt.perform(typeText(""), closeSoftKeyboard())
        nextButton.perform(click())

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
        nextButton.perform(click())

        //Check that toast shows given message
        onView(withText(R.string.login_text_invalid_format)).inRoot(ToastMatcher()).check(matches(isDisplayed()))
    }

    @Test
    fun alreadyUsedPhoneNumberTest() {
        fillValidNames()

        phoneNumberEt.perform(typeText("123456789"), closeSoftKeyboard())
        nextButton.perform(click())

        //Check that dialog shows given message
        Thread.sleep(5000) // Wait for API response
        onView(withText(R.string.login_already_registered_go_back)).check(matches(isDisplayed()))
    }


    /****************************** OnboardingPasscodeFragment ******************************/

    @Test
    fun validPasscodeTest() {
        fillValidNames()

        //Skip phone number validation and go to passcode fragment
        goNextStep()

        //Login to an already existing account
        login("0651234145", "661192")

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

        nextButton.perform(click())

        //Check that OnboardingPlaceFragment is still displayed
        placeTitleTv.check(matches(withText(R.string.onboard_place_title)))
    }

    @Test
    fun aloneEmptyPlaceTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        nextButton.perform(click())

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

        nextButton.perform(click())

        Thread.sleep(1000)

        //Check that HomeNeoMainFragment is displayed
        homeNeoTitleTv.check(matches(withText(R.string.home_neo_title)))
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

        nextButton.perform(click())

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

        nextButton.perform(click())

        Thread.sleep(1000)

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

        nextButton.perform(click())

        //Check that LoginEmailFragment is still displayed
        emailSubtitleTv.check(matches(withText(R.string.login_email_description)))
    }


    /****************************** OnboardingAssoStartFragment ******************************/

    @Test
    fun assoTunnelTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_assos)

        nextButton.perform(click())

        //Check that OnboardingAssoStartFragment 2nd screen is displayed
        val assoTitleTv = onView(
                allOf(withId(R.id.ui_onboard_asso_info_tv_title),
                        withParent(withId(R.id.onboard_phone_mainlayout)),
                        isDisplayed()))
        assoTitleTv.check(matches(withText(R.string.onboard_asso_info_title)))

        nextButton.perform(click())

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

        nextButton.perform(click())

        //Check that OnboardingAssoActivitiesFragment is displayed
        activitiesSubtitleTv.check(matches(withText(R.string.onboard_asso_activity_description)))
    }

    @Test
    fun missingAssoNameTest() {
        goToAssoInfo()

        fillPostalCode()
        fillFunction()

        nextButton.perform(click())

        //Check that OnboardingAssoFillFragment is still displayed
        assoFillTitleTv.check(matches(withText(R.string.onboard_asso_fill_title)))
    }

    @Test
    fun missingPostalCodeTest() {
        goToAssoInfo()

        fillAssoName()
        fillFunction()

        nextButton.perform(click())

        //Check that error message is displayed
        onView(withText(R.string.onboard_asso_fill_error)).check(matches(isDisplayed()))
    }

    @Test
    fun missingFunctionTest() {
        goToAssoInfo()

        fillAssoName()
        fillPostalCode()

        nextButton.perform(click())

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

        nextButton.perform(click())

        //Check that OnboardingEmailPwdFragment is Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }

    @Test
    fun pick3AssoActivitiesTest() {
        goToAssoActivities()

        clickAssoActivity2()
        clickAssoActivity3()
        clickAssoActivity4()

        nextButton.perform(click())

        //Check that OnboardingEmailPwdFragment is Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }

    @Test
    fun pick2AssoActivitiesTest() {
        goToAssoActivities()

        clickAssoActivity1()
        clickAssoActivity3()

        nextButton.perform(click())

        //Check that OnboardingEmailPwdFragment is Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }

    @Test
    fun pick1AssoActivityTest() {
        goToAssoActivities()

        clickAssoActivity2()

        nextButton.perform(click())

        //Check that OnboardingEmailPwdFragment is Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }

    @Test
    fun pickNoAssoActivitiesTest() {
        goToAssoActivities()

        nextButton.perform(click())

        //Check that error message is displayed
        onView(withText(R.string.onboard_asso_activity_error)).check(matches(isDisplayed()))
    }


    /****************************** OnboardingEmailPwdFragment ******************************/

    @Test
    fun assoValidMailTest() {
        goToAssoEmail()

        assoEmailEt.perform(typeText("jean.dupont@jeandupont.fr"), closeSoftKeyboard())

        nextButton.perform(click())

        //Check that OnboardingMainActivity is displayed
        feedButtonBottomBar.check(matches(isDisplayed()))
    }

    @Test
    fun assoInvalidMailTest() {
        goToAssoEmail()

        assoEmailEt.perform(typeText("jean.dupont@jeandupont"), closeSoftKeyboard())

        nextButton.perform(click())

        //Check that OnboardingEmailPwdFragment is still Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }

    @Test
    fun assoEmptyMailTest() {
        goToAssoEmail()

        nextButton.perform(click())

        //Check that OnboardingEmailPwdFragment is still Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }


    /****************************** Utils ******************************/

    private fun fillValidNames() {
        firstNameEt.perform(typeText("Jean"), closeSoftKeyboard())
        lastNameEt.perform(typeText("Dupont"), closeSoftKeyboard())
        nextButton.perform(click())
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
        login("0651234145", "661192")

        //Skip passcode validation and go to type fragment
        goNextStep()

        val typeButton = onView(allOf(withId(buttonId), isDisplayed()))
        typeButton.perform(click())

        Thread.sleep(2000)
        nextButton.perform(click())
        Thread.sleep(1000)
    }

    private fun login(phoneNumber: String, codePwd: String) {
        val authenticationController = EntourageApplication.get().components.authenticationController
        OnboardingAPI.getInstance().login(phoneNumber, codePwd) { isOK, loginResponse, _ ->
            if (isOK) {
                loginResponse?.let {
                    authenticationController.saveUser(loginResponse.user)
                }
                authenticationController.saveUserPhoneAndCode(phoneNumber, codePwd)
                authenticationController.saveUserToursOnly(false)

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
                allOf(withId(R.id.places_autocomplete_search_bar),
                        childAtPosition(
                                allOf(withId(R.id.places_autocomplete_search_bar_container),
                                        childAtPosition(
                                                withClassName(Matchers.`is`("android.widget.LinearLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        locationSearchBar.perform(click())
        locationSearchBar.perform(typeText(locationInput), closeSoftKeyboard())

        val locationsResultRv = onView(
                allOf(withId(R.id.places_autocomplete_list),
                        childAtPosition(
                                withClassName(Matchers.`is`("android.widget.LinearLayout")),
                                2)))
        locationsResultRv.perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        nextButton.perform(click())
        Thread.sleep(1000)
    }

    private fun testInvalidEmail() {
        emailEt.perform(typeText("jean.dupont@jeandupont"), closeSoftKeyboard())

        //Lose email edit text focus to enable next button
        emailSubtitleTv.perform(click())

        nextButton.perform(click())

        //Check that LoginEmailFragment is still displayed
        emailSubtitleTv.check(matches(withText(R.string.login_email_description)))
    }

    private fun goToAssoInfo() {
        clickTypeButton(R.id.ui_onboard_type_layout_assos)

        nextButton.perform(click())

        nextButton.perform(click())
    }

    private fun fillAssoName() {
        Thread.sleep(1000)

        val assoNameLayout = onView(
                allOf(withId(R.id.ui_layout_asso_fill_location),
                        isDisplayed()))
        assoNameLayout.perform(click())

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

        Thread.sleep(1000)
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

        nextButton.perform(click())
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

        nextButton.perform(click())
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

    class ToastMatcher : TypeSafeMatcher<Root>() {
        override fun matchesSafely(item: Root?): Boolean {
            item?.windowLayoutParams?.get()?.type?.let { type ->
                if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                    val windowToken = item.decorView.windowToken
                    val appToken = item.decorView.applicationWindowToken
                    if (windowToken == appToken) {
                        //Means this window isn't contained by any other windows
                        return true
                    }
                }
            }
            return false
        }

        override fun describeTo(description: Description?) {}
    }
}
