package social.entourage.android

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import social.entourage.android.onboarding.onboard.OnboardingStartActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class OnboardingTest : EntourageTestAfterLogin() {

    @get:Rule
    var activityRule = ActivityScenarioRule(OnboardingStartActivity::class.java)

    @Before
    fun setup() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @After
    override fun tearDown() {
        //just keep it for the annotation
        super.tearDown()
    }

    /*private val placeTitleTv = onView(
        allOf(
            withId(R.id.ui_onboard_place_tv_title),
            isDisplayed()
        )
    )

    private val emailSubtitleTv = onView(
        allOf(
            withId(R.id.ui_onboard_email),
            isDisplayed()
        )
    )

    private val emailEt = onView(
            allOf(withId(R.id.ui_onboard_email_pwd_et_mail),
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
                isDisplayed()))*/

    private val nextButton = onView(
        allOf(
            withId(R.id.ui_onboarding_bt_next),
            isDisplayed()
        )
    )

    /****************************** Onboarding Tests ******************************/
    //TODO
    /*@Test
    fun validOnboardingTest() {
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
    }*/

    /****************************** OnboardingTypeFragment ******************************/
    //TODO
    /*@Test
    fun neighbourTypeTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_neighbour)

        //Check that OnboardingPlaceFragment is displayed
        placeTitleTv.check(matches(withText(R.string.onboard_place_title)))
    }*/
    //TODO
    /*@Test
    fun aloneTypeTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        //Check that OnboardingPlaceFragment is displayed
        placeTitleTv.check(matches(withText(R.string.onboard_place_title_sdf)))
    }*/
    //TODO
    /*@Test
    fun assosTypeTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_assos)

        //Check that OnboardingAssoStartFragment is displayed
        val assoTitleTv = onView(
                allOf(withId(R.id.ui_onboard_asso_start_tv_title),
                    withParent(withId(R.id.onboard_phone_mainlayout)),
                    isDisplayed()))
        assoTitleTv.check(matches(withText(R.string.onboard_asso_start_title)))
    }*/

    /****************************** OnboardingPlaceFragment ******************************/
    //TODO
    /*@Test
    fun neighbourPlaceTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_neighbour)

        pickLocation("paris")

        //Check that LoginEmailFragment is displayed
        emailSubtitleTv.check(matches(withText(R.string.login_email_description)))
    }*/
    //TODO
    /*@Test
    fun alonePlaceTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        pickLocation("toulouse")

        //Check that LoginEmailFragment is displayed
        emailSubtitleTv.check(matches(withText(R.string.login_email_description)))
    }*/
    //TODO
    /*@Test
    fun neighbourEmptyPlaceTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_neighbour)

        clickNextButton()

        //Check that OnboardingPlaceFragment is still displayed
        placeTitleTv.check(matches(withText(R.string.onboard_place_title)))
    }*/
    //TODO
    /*@Test
    fun aloneEmptyPlaceTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        clickNextButton()

        //Check that OnboardingPlaceFragment is still displayed
        placeTitleTv.check(matches(withText(R.string.onboard_place_title_sdf)))
    }*/

    /****************************** LoginEmailFragment ******************************/
    //TODO
    /*@Test
    fun neighbourValidMailTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_neighbour)

        pickLocation("paris")

        emailEt.perform(typeText("jean.dupont@jeandupont.fr"), closeSoftKeyboard())

        //Lose email edit text focus to enable next button
        emailSubtitleTv.perform(click())

        clickNextButton()

        //Check that OnboardingMainActivity is displayed
        feedButtonBottomBar.check(matches(isDisplayed()))
    }*/
    //TODO
    /*@Test
    fun neighbourInvalidMailTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_neighbour)

        pickLocation("paris")

        testInvalidEmail()
    }*/
    //TODO
    /*@Test
    fun neighbourEmptyMailTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_neighbour)

        pickLocation("paris")

        clickNextButton()

        //Check that LoginEmailFragment is still displayed
        emailSubtitleTv.check(matches(withText(R.string.login_email_description)))
    }*/
    //TODO
    /*@Test
    fun aloneValidMailTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        pickLocation("toulouse")

        emailEt.perform(typeText("jean.dupont@jeandupont.fr"), closeSoftKeyboard())

        //Lose email edit text focus to enable next button
        emailSubtitleTv.perform(click())

        clickNextButton()

        //Check that OnboardingMainActivity is displayed
        feedButtonBottomBar.check(matches(isDisplayed()))
    }*/
    //TODO
    /*@Test
    fun aloneInvalidMailTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        pickLocation("toulouse")

        testInvalidEmail()
    }*/
    //TODO
    /*@Test
    fun aloneEmptyMailTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        pickLocation("toulouse")

        clickNextButton()

        //Check that LoginEmailFragment is still displayed
        emailSubtitleTv.check(matches(withText(R.string.login_email_description)))
    }*/

    /****************************** OnboardingAssoStartFragment ******************************/
    //TODO
    /*@Test
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
    }*/

    /****************************** OnboardingAssoFillFragment ******************************/
    //TODO
    /*@Test
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
    }*/
    //TODO
    /*@Test
    fun fillAssoInfoTest() {
        goToAssoInfo()

        fillAssoName()
        fillPostalCode()
        fillFunction()

        clickNextButton()

        //Check that OnboardingAssoActivitiesFragment is displayed
        activitiesSubtitleTv.check(matches(withText(R.string.onboard_asso_activity_description)))
    }*/
    //TODO
    /*@Test
    fun missingAssoNameTest() {
        goToAssoInfo()

        fillPostalCode()
        fillFunction()

        clickNextButton()

        //Check that OnboardingAssoFillFragment is still displayed
        assoFillTitleTv.check(matches(withText(R.string.onboard_asso_fill_title)))
    }*/
    //TODO
    /*@Test
    fun missingPostalCodeTest() {
        goToAssoInfo()

        fillAssoName()
        fillFunction()

        clickNextButton()

        //Check that error message is displayed
        onView(withText(R.string.onboard_asso_fill_error)).check(matches(isDisplayed()))
    }*/
    //TODO
    /*@Test
    fun missingFunctionTest() {
        goToAssoInfo()

        fillAssoName()
        fillPostalCode()

        clickNextButton()

        //Check that error message is displayed
        onView(withText(R.string.onboard_asso_fill_error)).check(matches(isDisplayed()))
    }*/

    /****************************** OnboardingAssoActivitiesFragment ******************************/
    //TODO
    /*@Test
    fun pick4AssoActivitiesTest() {
        goToAssoActivities()

        clickAssoActivity1()
        clickAssoActivity2()
        clickAssoActivity3()
        clickAssoActivity4()

        clickNextButton()

        //Check that OnboardingEmailPwdFragment is Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }*/
    //TODO
    /*@Test
    fun pick3AssoActivitiesTest() {
        goToAssoActivities()

        clickAssoActivity2()
        clickAssoActivity3()
        clickAssoActivity4()

        clickNextButton()

        //Check that OnboardingEmailPwdFragment is Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }*/
    //TODO
    /*@Test
    fun pick2AssoActivitiesTest() {
        goToAssoActivities()

        clickAssoActivity1()
        clickAssoActivity3()

        clickNextButton()

        //Check that OnboardingEmailPwdFragment is Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }*/
    //TODO
    /*@Test
    fun pick1AssoActivityTest() {
        goToAssoActivities()

        clickAssoActivity2()

        clickNextButton()

        //Check that OnboardingEmailPwdFragment is Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }*/
    //TODO
    /*@Test
    fun pickNoAssoActivitiesTest() {
        goToAssoActivities()

        clickNextButton()

        //Check that error message is displayed
        onView(withText(R.string.onboard_asso_activity_error)).check(matches(isDisplayed()))
    }*/

    /****************************** OnboardingEmailPwdFragment ******************************/
    //TODO
    /*@Test
    fun assoValidMailTest() {
        goToAssoEmail()

        assoEmailEt.perform(typeText("jean.dupont@jeandupont.fr"), closeSoftKeyboard())

        clickNextButton()

        //Check that OnboardingMainActivity is displayed
        feedButtonBottomBar.check(matches(isDisplayed()))
    }*/

    //TODO
    /*@Test
    fun assoInvalidMailTest() {
        goToAssoEmail()

        assoEmailEt.perform(typeText("jean.dupont@jeandupont"), closeSoftKeyboard())

        clickNextButton()

        //Check that OnboardingEmailPwdFragment is still Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }*/

    //TODO
    /*@Test
    fun assoEmptyMailTest() {
        goToAssoEmail()

        clickNextButton()

        //Check that OnboardingEmailPwdFragment is still Displayed
        assoEmailSubtitleTv.check(matches(withText(R.string.onboard_email_pwd_description)))
    }*/

    /****************************** Utils ******************************/

    private fun clickNextButton() {
        nextButton.perform(click())
    }

    /*private fun goNextStep() {
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
    }*/

    /*private fun goToAssoInfo() {
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
    }*/

    /*private fun clickAssoActivity1() {
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
    }*/

    /*private fun goToAssoEmail() {
        goToAssoActivities()

        clickAssoActivity1()
        clickAssoActivity2()
        clickAssoActivity3()
        clickAssoActivity4()

        clickNextButton()
    }*/
}
