package social.entourage.android.tools.log

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.google.firebase.crashlytics.FirebaseCrashlytics
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.api.model.User
import social.entourage.android.base.location.LocationUtils.isLocationPermissionGranted
import timber.log.Timber
import java.util.*

/**
 * Wrapper for sending events to different aggregators
 * Created by Mihai Ionescu on 03/10/2017.
 */
object AnalyticsEvents {
    // Analytics events
    const val EVENT_OPEN_POI_FROM_MAP = "Open_POI_From_Map"

    //----------------------------//
    // PREONBOARDING EVENTS
    //----------------------------//
    const val EVENT_VIEW_START_CARROUSEL1 = "View__Start__Carrousel1"
    const val EVENT_VIEW_START_CARROUSEL2 = "View__Start__Carrousel2"
    const val EVENT_VIEW_START_CARROUSEL3 = "View__Start__Carrousel3"
    const val EVENT_VIEW_START_CARROUSEL4 = "View__Start__Carrousel4"
    const val EVENT_VIEW_START_SIGNUPLOGIN = "View__Start__SignUpOrLogin"
    const val EVENT_ACTION_START_LOGINSTART = "Action__Start__LoginStart"
    const val EVENT_ACTION_START_SIGNUPSTART = "Action__Start__SignUpStart"

    //----------------------------//
    // LOGIN EVENTS
    //----------------------------//
    const val EVENT_VIEW_LOGIN_LOGIN = "View__Login__Login"
    const val EVENT_ACTION_LOGIN_SMS = "Action__Login__SMSCodeRequest"
    const val EVENT_ACTION_LOGIN_SUBMIT = "Action__Login__LoginSubmit"
    const val EVENT_ACTION_LOGIN_SUCCESS = "Action__Login__LoginSuccess"
    const val EVENT_ERROR_LOGIN_ERROR = "Error__Login__LoginError"
    const val EVENT_ERROR_LOGIN_FAIL = "Error__Login__LoginFail"
    const val EVENT_ERROR_LOGIN_PHONE = "Error__Login__TelephoneSubmitError"

    const val EVENT_VIEW_LOGIN_ACTION_ZONE = "View__Login__ActionZone"
    const val EVENT_ACTION_LOGIN_SETACTION_ZONE_GEOLOC = "Action__Login__SetActionZoneGeoloc"
    const val EVENT_ACTION_LOGIN_SETACTION_ZONE_SEARCH = "Action__Login__SetActionZoneSearch"
    const val EVENT_ACTION_LOGIN_ACTION_ZONE_SUBMIT = "Action__Login__ActionZoneSubmit"
    const val EVENT_VIEW_LOGIN_INPUT_EMAIL = "View__Login__InputEmail"

    const val EVENT_ACTION_LOGIN_EMAIL_SUBMIT = "Action__Login__EmailSubmit"
    const val EVENT_ERROR_LOGIN_EMAIL_SUBMIT_ERROR = "Error__Login__EmailSubmitError"


    //----------------------------//
    // ONBOARDING - START
    //-------
    //------INPUT NAMES
    const val EVENT_VIEW_ONBOARDING_NAMES = "View__Onboarding__InputNames"
    const val EVENT_ACTION_ONBOARDING_NAMES = "Action__Onboarding__NameSubmit"

    //------PHONE
    const val EVENT_VIEW_ONBOARDING_PHONE = "View__Onboarding__InputPhone"
    const val EVENT_ACTION_ONBOARDING_PHONE_SUBMIT = "Action__Onboarding__PhoneSubmit"
    const val EVENT_ACTION_ONBOARDING_PHONE_SUBMIT_SUCCESS = "Action__Onboarding__PhoneSubmitSuccess"
    const val EVENT_ERROR_ONBOARDING_PHONE_SUBMIT_ERROR = "Error__Onboarding__PhoneSubmitError"
    const val EVENT_ERROR_ONBOARDING_PHONE_SUBMIT_EXIST = "Error__Onboarding__PhoneAlreadyExistErro"

    //------CODE
    const val EVENT_VIEW_ONBOARDING__PASSCODE = "View__Onboarding__InputPasscode"
    const val EVENT_ACTION_ONBOARDING_SMS = "Action__Onboarding__SMSCodeRequest"
    const val EVENT_ACTION_ONBOARDING_SIGNUP_SUBMIT = "Action__Onboarding__SignUpSubmit"
    const val EVENT_ACTION_ONBOARDING_SIGNUP_SUCCESS = "Action__Onboarding__SignUpSuccess"

    //------public static final String EVENT_ERROR_ONBOARDING_SIGNUP_ERROR = "Error__Onboarding__SignUpError";
    const val EVENT_ERROR_ONBOARDING_SINGUP_FAIL = "Error__Onboarding__SignUpFail"

    //------PROFILE
    const val EVENT_VIEW_ONBOARDING_CHOOSE_PROFILE = "View__Onboarding__ChooseProfile"
    const val EVENT_ACTION_ONBOARDING_CHOOSE_PROFILE_SIGNUP = "Action__Onboarding__ChooseProfile"
    const val EVENT_ACTION_ONBOARDING_CHOOSE_PROFILE_SKIP = "Action__Onboarding__ChooseProfileSkip"

    //------Riverain__Sdf
    const val EVENT_VIEW_ONBOARDING_ACTION_ZONE = "View__Onboarding__ActionZone"
    const val EVENT_ACTION_ONBOARDING_SETACTION_ZONE_GEOLOC = "Action__Onboarding__SetActionZoneGeoloc"
    const val EVENT_ACTION_ONBOARDING_SETACTION_ZONE_SEARCH = "Action__Onboarding__SetActionZoneSearch"
    const val EVENT_ACTION_ONBOARDING_ACTION_ZONE_SUBMIT = "Action__Onboarding__ActionZoneSubmit"
    const val EVENT_VIEW_ONBOARDING_ACTION_ZONE2 = "View__Onboarding__ActionZone2"
    const val EVENT_ACTION_ONBOARDING_SETACTION_ZONE2_GEOLOC = "Action__Onboarding__SetActionZone2Geoloc"
    const val EVENT_ACTION_ONBOARDING_SETACTION_ZONE2_SEARCH = "Action__Onboarding__SetActionZone2Search"
    const val EVENT_ACTION_ONBOARDING_ACTION_ZONE2_SUBMIT = "Action__Onboarding__ActionZone2Submit"
    const val EVENT_ACTION_ONBOARDING_ACTION_ZONE2_SKIP = "Action__Onboarding__ActionZone2Skip"

    //------Riverain__Sdf Mosaïc
    const val EVENT_VIEW_ONBOARDING_NEIGHBOR_MOSAIC = "View__Onboarding__NeighborMosaic"
    const val EVENT_VIEW_ONBOARDING_INNEED_MOSAIC = "View__Onboarding__InNeedMosaic"
    const val EVENT_ACTION_ONBOARDING_NEIGHBOR_MOSAIC = "Action__Onboarding__NeighborMosaic"
    const val EVENT_ACTION_ONBOARDING_INNEED_MOSAIC = "Action__Onboarding__InNeedMosaic"

    //------ASSO
    const val EVENT_VIEW_ONBOARDING_PRO_STORIES = "View__Onboarding__ProStoriesPage"
    const val EVENT_VIEW_ONBOARDING_PRO_FEATURES = "View__Onboarding__ProFeaturesPage"
    const val EVENT_VIEW_ONBOARDING_PRO_SIGNUP = "View__Onboarding__ProSignUp"
    const val EVENT_ACTION_ONBOARDING_PRO_SIGNUP_SUBMIT = "Action__Onboarding__ProSignUpSubmit"
    const val EVENT_ACTION_ONBOARDING_PRO_SIGNUP_SUCCESS = "Action__Onboarding__ProSignUpSuccess"
    const val EVENT_ERROR_ONBOARDING_PRO_SIGNUP_ERROR = "Error__Onboarding__ProSignUpError"

    //------Asso Mosaïc
    const val EVENT_VIEW_ONBOARDING_PRO_MOSAIC = "View__Onboarding__ProMosaic"
    const val EVENT_ACTION_ONBOARDING_PRO_MOSAIC = "Action__Onboarding__ProMosaic"

    //------EMAIL
    const val EVENT_VIEW_ONBOARDING_INPUT_EMAIL = "View__Onboarding__InputEmail"
    const val EVENT_ACTION_ONBOARDING_EMAIL_SUBMIT = "Action__Onboarding__EmailSubmit"
    const val EVENT_ERROR_ONBOARDING_EMAIL_SUBMIT_ERROR = "Error__Onboarding__EmailSubmitError"

    //------PHOTO
    const val EVENT_VIEW_ONBOARDING_CHOOSE_PHOTO = "View__Onboarding__ChoosePhoto"
    const val EVENT_ACTION_ONBOARDING_PHOTO_SUBMIT = "Action__Onboarding__PhotoSubmit"
    const val EVENT_ACTION_ONBOARDING_IGNORE_PHOTO = "Action__Onboarding__IgnorePhoto"
    const val EVENT_ACTION_ONBOARDING_UPLOAD_PHOTO = "Action__Onboarding__UploadPhoto"
    const val EVENT_ACTION_ONBOARDING_TAKE_PHOTO = "Action__Onboarding__TakePhoto"

    //------TOOLTIP
    const val EVENT_ACTION_TOOLTIP_FILTER_NEXT = "Action__Tooltip__FilterNext"
    const val EVENT_ACTION_TOOLTIP_FILTER_CLOSE = "Action__Tooltip__FilterClose"
    const val EVENT_ACTION_TOOLTIP_GUIDE_NEXT = "Action__Tooltip__GuideNext"
    const val EVENT_ACTION_TOOLTIP_GUIDE_CLOSE = "Action__Tooltip__GuideClose"
    const val EVENT_ACTION_TOOLTIP_PLUS_NEXT = "Action__Tooltip__PlusNext"
    const val EVENT_ACTION_TOOLTIP_PLUS_CLOSE = "Action__Tooltip__PlusClose"

    //-------
    // ONBOARDING - END
    //----------------------------//
    //----------------------------//
    // PROFILE EDIT - START
    //-------
    //------PHOTO
    const val EVENT_VIEW_PROFILE_CHOOSE_PHOTO = "View__Profile__ChoosePhoto"
    const val EVENT_ACTION_PROFILE_PHOTO_SUBMIT = "Action__Profile__PhotoSubmit"
    const val EVENT_ACTION_PROFILE_UPLOAD_PHOTO = "Action__Profile__UploadPhoto"
    const val EVENT_ACTION_PROFILE_TAKE_PHOTO = "Action__Profile__TakePhoto"

    //------ZONE
    const val EVENT_VIEW_PROFILE_ACTION_ZONE = "View__Profile__ActionZone"
    const val EVENT_ACTION_PROFILE_SETACTION_ZONE_GEOLOC = "Action__Profile__SetActionZoneGeoloc"
    const val EVENT_ACTION_PROFILE_SETACTION_ZONE_SEARCH = "Action__Profile__SetActionZoneSearch"
    const val EVENT_ACTION_PROFILE_ACTION_ZONE_SUBMIT = "Action__Profile__ActionZoneSubmit"
    const val EVENT_VIEW_PROFILE_ACTION_ZONE2 = "View__Profile__ActionZone2"
    const val EVENT_ACTION_PROFILE_SETACTION_ZONE2_GEOLOC = "Action__Profile__SetActionZone2Geoloc"
    const val EVENT_ACTION_PROFILE_SETACTION_ZONE2_SEARCH = "Action__Profile__SetActionZone2Search"
    const val EVENT_ACTION_PROFILE_ACTION_ZONE2_SUBMIT = "Action__Profile__ActionZone2Submit"

    //------TYPE
    const val EVENT_VIEW_PROFILE_CHOOSE_PROFILE = "View__Profile__ChooseProfile"
    const val EVENT_ACTION_PROFILE_CHOOSE_PROFILE_SIGNUP = "Action__Profile__ChooseProfile"

    //------MOSAIC
    const val EVENT_VIEW_PROFILE_PRO_MOSAIC = "View__Profile__ProMosaic"
    const val EVENT_ACTION_PROFILE_PRO_MOSAIC = "Action__Profile__ProMosaic"
    const val EVENT_VIEW_PROFILE_NEIGHBOR_MOSAIC = "View__Profile__NeighborMosaic"
    const val EVENT_VIEW_PROFILE_INNEED_MOSAIC = "View__Profile__InNeedMosaic"
    const val EVENT_ACTION_PROFILE_NEIGHBOR_MOSAIC = "Action__Profile__NeighborMosaic"
    const val EVENT_ACTION_PROFILE_INNEED_MOSAIC = "Action__Profile__InNeedMosaic"

    //-------
    // PROFILE EDIT - END
    //----------------------------//
    //LOG IN Events OLD VALUES
    const val EVENT_LOGOUT = "Logout"
    const val EVENT_LOGIN_START = "Login_Start"
    const val EVENT_LOGIN_OK = "Login_Success"
    const val EVENT_LOGIN_FAILED = "Login_Failed"
    const val EVENT_LOGIN_ERROR = "Login_Error"
    const val EVENT_TUTORIAL_START = "Log_Tutorial_Start"
    const val EVENT_TUTORIAL_END = "Log_Tutorial_End"
    const val EVENT_LOGIN_SEND_NEW_CODE = "Login_Send_New_Code"
    const val EVENT_LOGIN_SLIDESHOW = "SwipeTutorial"
    const val EVENT_NEWSLETTER_INSCRIPTION_OK = "Newsletter_Inscription_OK"
    const val EVENT_NEWSLETTER_INSCRIPTION_FAILED = "Newsletter_Inscription_Failed"
    const val EVENT_SPLASH_SIGNUP = "SplashSignUp"
    const val EVENT_SPLASH_LOGIN = "SplashLogIn"
    const val EVENT_PHONE_SUBMIT = "TelephoneSubmit"
    const val EVENT_PHONE_SUBMIT_FAIL = "TelephoneSubmitFail"
    const val EVENT_PHONE_SUBMIT_ERROR = "TelephoneSubmitError"
    const val EVENT_SMS_CODE_REQUEST = "SMSCodeRequest"
    const val EVENT_EMAIL_SUBMIT = "EmailSubmit"
    const val EVENT_EMAIL_SUBMIT_ERROR = "EmailSubmitError"
    const val EVENT_EMAIL_IGNORE = "IgnoreEmail"
    const val EVENT_NAME_SUBMIT = "NameSubmit"
    const val EVENT_NAME_SUBMIT_ERROR = "NameSubmitError"
    const val EVENT_NAME_TYPE = "NameType"
    const val EVENT_PHOTO_UPLOAD_SUBMIT = "PhotoUploadSubmit"
    const val EVENT_PHOTO_TAKE_SUBMIT = "PhotoTakeSubmit"
    const val EVENT_PHOTO_IGNORE = "IgnorePhoto"
    const val EVENT_PHOTO_BACK = "BackFromPhoto1"
    const val EVENT_PHOTO_SUBMIT = "SubmitInstantPhoto"
    const val EVENT_NOTIFICATIONS_ACCEPT = "AcceptNotifications"
    const val EVENT_NOTIFICATIONS_POPUP_ACCEPT = "AcceptNotificationsFromPopup"
    const val EVENT_NOTIFICATIONS_POPUP_REFUSE = "RefuseNotificationsFromPopup"
    const val EVENT_GEOLOCATION_ACCEPT = "AcceptGeoloc"
    const val EVENT_GEOLOCATION_POPUP_ACCEPT = "AcceptGeolocFromPopup"
    const val EVENT_GEOLOCATION_POPUP_REFUSE = "RefuseGeolocFromPopup"
    const val EVENT_GEOLOCATION_ACTIVATE_04_4A = "ActivateGeolocFromScreen04_4UserBlocked"
    const val EVENT_GEOLOCATION_ACTION_ZONE_04_4A = "ActionZoneFromScreen04_4UserBlocked"
    const val EVENT_WELCOME_CONTINUE = "WelcomeScreenContinue"

    // SCREEN Events
    const val EVENT_SCREEN_01 = "Screen01SplashView"
    const val EVENT_SCREEN_02_1 = "Screen02OnboardingLoginView"
    const val EVENT_SCREEN_03_1 = "Screen03_1OnboardingCodeResendView"
    const val EVENT_SCREEN_03_2 = "Screen03_2OnboardingPhoneNotFoundView"
    const val EVENT_SCREEN_04 = "Screen04_GoEnableGeolocView" //Not implemented on Android
    const val EVENT_SCREEN_04_3 = "Screen04_3OnboardingNotificationsView"
    const val EVENT_SCREEN_30_1 = "Screen30_1WelcomeView"
    const val EVENT_SCREEN_30_2 = "Screen30_2InputPhoneView"
    const val EVENT_SCREEN_30_2_E = "Screen30_2PhoneAlreadyExistsError"
    const val EVENT_SCREEN_30_3 = "Screen30_3InputPasscodeView"
    const val EVENT_SCREEN_30_4 = "Screen30_4InputEmailView"
    const val EVENT_SCREEN_30_5 = "Screen30_5InputNameView"
    const val EVENT_SCREEN_09_1_ME = "Screen09_1MyProfileViewAsPublicView"
    const val EVENT_SCREEN_09_1_OTHER = "Screen09_1OtherUserProfileView"
    const val EVENT_SCREEN_09_2 = "Screen09_2EditMyProfileView"
    const val EVENT_SCREEN_09_4 = "Screen09_4EditPasswordView"
    const val EVENT_SCREEN_09_4_SUBMIT = "Screen09_4ChangePasswordSubmit"
    const val EVENT_SCREEN_09_5 = "Screen09_5EditNameView"
    const val EVENT_SCREEN_09_6 = "Screen09_6ChoosePhotoView"
    const val EVENT_SCREEN_09_7 = "Screen09_7TakePhotoView"
    const val EVENT_SCREEN_09_8 = "Screen09_8ConfirmPhotoView"
    const val EVENT_SCREEN_09_9 = "Screen09_9MovePhotoView"
    const val EVENT_SCREEN_06_1 = "Screen06_1FeedView"
    const val EVENT_SCREEN_06_2 = "Screen06_2MapView"
    const val EVENT_SCREEN_17_2 = "Screen17_2MyMessagesView"

    //MENU Events
    const val EVENT_PROFILE_FROM_MENU = "Open_Profile_From_Menu"
    const val EVENT_ABOUT_FAQ = "AppFAQClick"


    //FEED Events
    const val EVENT_FEED_MESSAGES = "GoToMessages" // No longer used
    const val EVENT_FEED_USERPROFILE = "UserProfileClick"
    const val EVENT_FEED_ACTIVE_CLOSE_OVERLAY = "OpenActiveCloseOverlay"
    const val EVENT_FEED_MAPCLICK = "MapClick"
    const val EVENT_FEED_HEATZONECLICK = "HeatzoneMapClick"
    const val EVENT_FEED_OPEN_ENTOURAGE = "OpenEntouragePublicPage"
    const val EVENT_FEED_OPEN_CONTACT = "OpenEnterInContact"
    const val EVENT_FEED_RECENTERCLICK = "RecenterMapClick"
    const val EVENT_FEED_FILTERSCLICK = "FeedFiltersPress"
    const val EVENT_FEED_REFRESH_LIST = "RefreshListPage"
    const val EVENT_FEED_SCROLL_LIST = "ScrollListPage"
    const val EVENT_FEED_ACTION_CREATE_CLICK = "CreateActionClick"
    const val EVENT_FEED_PENDING_OVERLAY = "PendingRequestOverlay"
    const val EVENT_FEED_CANCEL_JOIN_REQUEST = "CancelJoinRequest"
    const val EVENT_FEED_OPEN_ACTIVE_OVERLAY = "OpenActiveOverlay"
    const val EVENT_FEED_QUIT_ENTOURAGE = "QuitFromFeed"
    const val EVENT_FEED_ACTIVATE_GEOLOC_RECENTER = "ActivateGeolocFromRecenterPopup"
    const val EVENT_GUIDE_ACTIVATE_GEOLOC_RECENTER = "ActivateGeolocFromGuideRecenterPopup"
    const val EVENT_FEED_ACTIVATE_GEOLOC_FROM_BANNER = "ActivateGeolocFromBanner"
    const val EVENT_GUIDE_ACTIVATE_GEOLOC_FROM_BANNER = "ActivateGeolocFromGuideBanner"

    //MAP Events
    const val EVENT_MAP_LONGPRESS = "HiddenButtonsOverlayPress"
    const val EVENT_MAP_ZOOM_IN = "ZoomIn"
    const val EVENT_MAP_ZOOM_OUT = "ZoomOut"
    const val EVENT_MAP_SHIFT_CENTER = "MapShiftCenter" //Not able to detect if it's an automatic or manual shift

    //GUIDE Events
    const val EVENT_GUIDE_LONGPRESS = "LongPressFromGuideClick"

    //SEND JOIN REQUEST Events
    const val EVENT_JOIN_REQUEST_START = "StartJoinMessage"
    const val EVENT_JOIN_REQUEST_SUBMIT = "SubmitJoinMessage"
    const val EVENT_JOIN_REQUEST_ACCEPT = "AcceptJoinRequest"
    const val EVENT_JOIN_REQUEST_REJECT = "RejectJoinRequest"

    //ENTOURAGE VIEW Events
    const val EVENT_ENTOURAGE_DISCUSSION_VIEW = "Screen14_1DiscussionView"
    const val EVENT_ENTOURAGE_PUBLIC_VIEW_MEMBER = "Screen14_2PublicPageViewAsMember"
    const val EVENT_ENTOURAGE_PUBLIC_VIEW_NONMEMBER = "Screen14_2PublicPageViewAsNonMember"
    const val EVENT_ENTOURAGE_VIEW_WRITE_MESSAGE = "WriteMessage"
    const val EVENT_ENTOURAGE_VIEW_SPEECH = "SpeechRecognitionMessage"
    const val EVENT_ENTOURAGE_VIEW_ADD_MESSAGE = "AddContentToMessage"
    const val EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE = "CloseEntourageConfirm"
    const val EVENT_ENTOURAGE_VIEW_OPTIONS_QUIT = "ExitEntourageConfirm"
    const val EVENT_ENTOURAGE_VIEW_INVITE_FRIENDS = "InviteFriendsClick"
    const val EVENT_ENTOURAGE_VIEW_INVITE_CONTACTS = "InviteContacts"
    const val EVENT_ENTOURAGE_VIEW_INVITE_PHONE = "InviteByPhoneNumber"
    const val EVENT_ENTOURAGE_VIEW_INVITE_CLOSE = "InviteFriendsClose"
    const val EVENT_ENTOURAGE_VIEW_SWITCH_PUBLIC = "EntouragePublicPageFromMessages"
    const val EVENT_ENTOURAGE_VIEW_ASK_JOIN = "AskJoinFromPublicPage"
    const val EVENT_ENTOURAGE_CREATE_CHANGE_LOCATION = "ChangeLocationClick"
    const val EVENT_ENTOURAGE_CLOSE_POPUP_HELP = "HelpRequestOnClosePopup"
    const val EVENT_ENTOURAGE_CLOSE_POPUP_CANCEL = "CancelClosePopup"
    const val EVENT_ENTOURAGE_SHARE_MEMBER = "ShareLinkAsMemberOrCreator"
    const val EVENT_ENTOURAGE_SHARE_NONMEMBER = "ShareLinkAsNonMember"

    //MY ENTOURAGES Events
    const val EVENT_MYENTOURAGES_BANNER_CLICK = "BannerMessageClick"
    const val EVENT_MYENTOURAGES_MESSAGE_OPEN = "MessageOpen"
    const val EVENT_MYENTOURAGES_FILTER_CLICK = "MessagesFilterClick"
    const val EVENT_MYENTOURAGES_BANNER_MOVE = "MoveBannerClick" //A lot of code needs to be written to detect this
    const val EVENT_MYENTOURAGES_BACK_CLICK = "BackToFeedClick"
    const val ACTION_PLUS_HELP = "Action__Plus__Help"
    const val ACTION_PLUS_GOOD_WAVES = "Action__Plus__GoodWaves"
    const val ACTION_PLUS_CREATE_ASKFORHELP = "Action__Plus__CreateAskForHelp"
    const val ACTION_PLUS_CREATE_CONTRIBUTE = "Action__Plus__CreateContribute"
    const val ACTION_PLUS_CREATE_OUTING = "Action__Plus__CreateOuting"
    const val ACTION_PLUS_BACK = "Action__Plus__BackPressed"

    //MY ENTOURAGES FILTER Events
    const val EVENT_MYENTOURAGES_FILTER_EXIT = "ExitMessagesFilter"
    const val EVENT_MYENTOURAGES_FILTER_SAVE = "SaveMessagesFilter"
    const val EVENT_MYENTOURAGES_FILTER_ACTIVE = "ActiveMessagesFilter"
    const val EVENT_MYENTOURAGES_FILTER_INVITATIONS = "InvitationsFilter"
    const val EVENT_MYENTOURAGES_FILTER_ORGANIZER = "OrganizerFilter"
    const val EVENT_MYENTOURAGES_FILTER_UNREAD = "UnreadMessagesFilter"
    const val EVENT_MYENTOURAGES_FILTER_PAST = "ExcludeClosedEntouragesFilter"
    const val EVENT_MYENTOURAGES_FILTER_ASK = "AskMessagesFilter"
    const val EVENT_MYENTOURAGES_FILTER_OFFER = "OfferMessagesFilter"

    //ENTOURAGE DISCLAIMER Events
    const val EVENT_ENTOURAGE_DISCLAIMER_CLOSE = "CloseEthicsPopupClick"
    const val EVENT_ENTOURAGE_DISCLAIMER_ACCEPT = "AcceptEthicsChartClick"
    const val EVENT_ENTOURAGE_DISCLAIMER_LINK = "LinkToEthicsChartClick"

    //MAP Filter Events
    const val EVENT_MAP_FILTER_FILTER1 = "ClickFilter1Value"
    const val EVENT_MAP_FILTER_FILTER2 = "ClickFilter2Value"
    const val EVENT_MAP_FILTER_FILTER3 = "ClickFilter3Value"
    const val EVENT_MAP_FILTER_ONLY_MINE = "ShowOnlyMineFilter"
    const val EVENT_MAP_FILTER_ONLY_OFFERS = "ShowOnlyOffersClick"
    const val EVENT_MAP_FILTER_ONLY_ASK = "ShowOnlyAsksClick"
    const val EVENT_MAP_FILTER_ACTION_CATEGORY = "FilterActionSubtypeClick"
    const val EVENT_MAP_FILTER_SUBMIT = "SubmitFilterPreferences"
    const val EVENT_MAP_FILTER_CLOSE = "CloseFilter"

    // USER Events
    const val EVENT_USER_EDIT_PROFILE = "EditMyProfile"
    const val EVENT_USER_EDIT_PHOTO = "EditPhoto"
    const val EVENT_USER_ROTATE_PHOTO = "RotatePhoto"
    const val EVENT_USER_SAVE = "SaveProfileEdits"
    const val EVENT_USER_SAVE_FAILED = "SaveProfileEditFailed"
    const val EVENT_USER_TOBADGE = "ToBadgePageFromProfile"
    const val EVENT_USER_TONOTIFICATIONS = "ToNotifications"

    // SHORTCUT Events
    const val EVENT_SHORTCUT_SHAREAPP = "ShareShortcut"
    const val EVENT_SHORTCUT_RATEAPP = "RateShortcut"

    // ABOUT Events
    const val EVENT_ABOUT_RATING = "RatingClick"
    const val EVENT_ABOUT_FACEBOOK = "FacebookPageClick"
    const val EVENT_ABOUT_WEBSITE = "WebsiteVisitClick"
    const val EVENT_ABOUT_CGU = "CGUClick"
    const val EVENT_ABOUT_TUTORIAL = "OpenTutorialFromMenu"
    const val EVENT_ABOUT_PRIVACY = "OpenPrivacyFromAbout"
    const val EVENT_ABOUT_SUGGESTION = "OpenSuggestionFromAbout"
    const val EVENT_ABOUT_FEEDBACK = "OpenFeedbackFromAbout"
    const val EVENT_ABOUT_OSS = "OpenOpenSourceLibrariesFromAbout"
    const val EVENT_ABOUT_EMAIL = "OpenEmailFromAbout"

    //Notifications
    const val EVENT_NOTIFICATION_RECEIVED = "NotificationReceived"
    const val EVENT_NOTIFICATION_FCM_RECEIVED = "NotificationReceivedFromFCM"
    const val EVENT_NOTIFICATION_ENTOURAGE_RECEIVED = "NotificationReceivedFromEntourage"

    //PLUS Screen
    const val EVENT_PLUS_NOT_READY = "NotReadyToHelpClick"

    //Feed item info
    const val ACTION_FEEDITEMINFO_FAQ = "Action__FeedItemInfo__FAQ"

    //Action plus button agir / structure
    const val ACTION_PLUS_STRUCTURE = "Action__Plus__Structure"

    //Tabbar actions clic
    const val ACTION_TAB_FEEDS = "Action__Tab__Feeds"
    const val ACTION_TAB_GDS = "Action__Tab__GDS"
    const val ACTION_TAB_PLUS = "Action__Tab__Plus"
    const val ACTION_TAB_MESSAGES = "Action__Tab__Messages"
    const val ACTION_TAB_PROFIL = "Action__Tab__Profil"
    //Feeds
    const val ACTION_FEED_SHOWALL = "Action__Feed__ShowAll"
    const val ACTION_FEED_SHOWEVENTS = "Action__Feed__ShowEvents"
    const val ACTION_FEED_SHOWMAP = "Action__Feed__ShowMap"
    const val ACTION_FEED_SHOWLIST = "Action__Feed__ShowList"
    const val ACTION_FEED_SHOWFILTERS = "Action__Feed__ShowFilters"

    //Guide
    const val ACTION_GUIDE_SHOWMAP = "Action__GuideMap__ShowMap"
    const val ACTION_GUIDE_SHOWLIST = "Action__GuideMap__ShowList"
    const val ACTION_GUIDE_SHOWFILTERS = "Action__GuideMap__ShowFilters"

    const val ACTION_GUIDE_SHOWGDS = "Action__HubGuide__ShowGDS"
    const val ACTION_GUIDE_WEBORIENTATION = "Action__HubGuide__WebOrientation"
    const val ACTION_GUIDE_WEBGUIDE = "Action__HubGuide__WebGuide"
    const val ACTION_GUIDE_WEBATELIER = "Action__HubGuide__WebAtelier"
    const val ACTION_GUIDE_WEBFAQ = "Action__HubGuide__WebFaq"

    const val ACTION_GUIDE_POI = "Action__GuideMap__POI"
    const val ACTION_GUIDE_SUBMITFILTERS = "Action__GuideMap__SubmitFilters"
    const val ACTION_GUIDE_SHAREPOI = "Action__GuideMap__SharePOI"
    const val ACTION_GUIDE_CALLPOI = "Action__GuideMap__CallPOI"

    const val SOLIGUIDE_SHOW_POI = "Soligde__Show__%s_%s__Search_%s"
    const val SOLIGUIDE_CLICK = "Soligde__Click__%s_%s__Search_%s"

    const val ACTION_GUIDE_STARTSEARCH = "Action__GuideMap__SearchStart"
    const val ACTION_GUIDE_SEARCHRESULTS = "Action__GuideMap__SearchResults"

    const val ACTION_GUIDE_SEARCHFILTER_ORGANIZ = "Action__GuideMap__OrganizationsFilter"
    const val ACTION_GUIDE_SEARCHFILTER_VOLUNT = "Action__GuideMap__Volunteers"
    const val ACTION_GUIDE_SEARCHFILTER_DONAT = "Action__GuideMap__Donations"

    //Start info
    const val SHOW_START_HOME = "View__Start__Home"

    //Profile
    const val ACTION_PROFILE_LOGOUT = "Action__Profile__LogOut"
    const val ACTION_PROFILE_ABOUT = "Action__Profile__About"
    const val ACTION_PROFILE_GOAL = "Action__Profile__Actions"
    const val ACTION_PROFILE_FOLLOW = "Action__Profile__Follow"
    const val ACTION_PROFILE_BLOG = "Action__Profile__SCBonjour"
    const val ACTION_PROFILE_CHART = "Action__Profile__Ethic"
    const val ACTION_PROFILE_DONATION = "Action__Profile__Donate"
    const val ACTION_PROFILE_AMBASSADOR = "Action__Profile__Ambassador"
    const val ACTION_PROFILE_GOODWAVES = "Action__Profile__GoodWaves"
    const val ACTION_PROFILE_SHOWEVENTS = "Action__Menu__EventsCount"
    const val ACTION_PROFILE_SHOWACTIONS = "Action__Menu__ActionsCount"
    const val ACTION_PROFILE_MODPROFIL = "Action__Profile__ModProfil"
    const val ACTION_PROFILE_SHOWPROFIL = "Action__Profile__ShowProfil"
    const val VIEW_PROFILE_MENU = "View__Profile__Menu"
    const val VIEW_PLUS_SCREEN = "View__Plus__Screen"

    const val VIEW_ADD_USERNAME_SUBMIT = "Action__Add__UsernameSubmit"

    /******
     * New home feed Expert
     */
    const val VIEW_START_EXPERTFEED = "View__Start__ExpertFeed"
    //Feed Expert Headline
    const val ACTION_EXPERTFEED_News_Announce = "Action__ExpertFeed__News_Announce"
    const val ACTION_EXPERTFEED_News_Event = "Action__ExpertFeed__News_Event"
    const val ACTION_EXPERTFEED_News_Action = "Action__ExpertFeed__News_Action"

    //Feed Expert Events
    const val ACTION_EXPERTFEED_Event = "Action__ExpertFeed__Event"
    const val ACTION_EXPERTFEED_MoreEvent = "Action__ExpertFeed__MoreEvents"
    const val ACTION_EXPERTFEED_MoreEventArrow = "Action__ExpertFeed__MoreEventsArrow"
    const val Event_EXPERTFEED_ModifyActionZone = "Action__ExpertFeed__ModifyActionZone"

    //Feed Expert Actions
    const val ACTION_EXPERTFEED_Action = "Action__ExpertFeed__Action"
    const val ACTION_EXPERTFEED_MoreAction = "Action__ExpertFeed__MoreActions"
    const val ACTION_EXPERTFEED_MoreActionArrow = "Action__ExpertFeed__MoreActionsArrow"
    const val ACTION_EXPERTFEED_HelpDifferent = "Action__ExpertFeed__HelpDifferently"

    const val Action_expertFeed_HelpReporter = "Action__ExpertFeed__Help_Reporter"
    const val Action_expertFeed_HelpGift = "Action__ExpertFeed__Help_Gift"
    const val Action_expertFeed_HelpShare = "Action__ExpertFeed__Help_Share"
    const val Action_expertFeed_HelpAmbassador = "Action__ExpertFeed__Help_Ambassador"
    const val Action_expertFeed_HelpLinkedout = "Action__ExpertFeed__Help_Linkedout"

    //Entourages search
    const val ACTION_FEEDSEARCH_START_ASK = "Action__FeedSearch__Start_Ask"
    const val ACTION_FEEDSEARCH_START_CONTRIB = "Action__FeedSearch__Start_Contrib"
    const val ACTION_FEEDSEARCH_START_EVENT = "Action__FeedSearch__Start_Event"
    const val ACTION_FEEDSEARCH_SHOW_DETAIL = "Action__FeedSearch__Show_Detail"
    const val ACTION_FEEDSEARCH_SHOW_PROFILE = "Action__FeedSearch__Show_Profile"
    const val VIEW_FEEDSEARCH_SEARCHRESULTS = "View__FeedSearch__SearchResults"

    //Feed Detail
    const val VIEW_FEEDVIEW_EVENTS = "View__FeedView__Events"
    const val VIEW_FEEDVIEW_CONTRIBS = "View__FeedView__Contribs"
    const val VIEW_FEEDVIEW_ASKS = "View__FeedView__Asks"

    const val VIEW_FEEDDETAIL_ACTION = "View__FeedDetail__Action"
    const val VIEW_FEEDDETAIL_EVENT = "View__FeedDetail__Event"

    const val VIEW_LISTACTIONS_SHOW = "View__ListActions__Show"
    const val ACTION_LISTACTIONS_SWITCH_ASK = "Action__ListActions__Switch_Ask"
    const val ACTION_LISTACTIONS_SWITCH_CONTRIB = "Action__ListActions__Switch_Contrib"
    const val ACTION_LISTACTIONS_SHOW_DETAIL = "Action__ListActions__Show_Detail"

    //POP Share / modify / close entourage
    const val SHOW_MENU_OPTIONS = "View__Menu_Options__Show"
    const val SHOW_MODIFY_ENTOURAGE = "View__Modify_Entourage__Show"

    const val SHOW_POP_SHARE = "View__Pop_Share__Show"
    const val ACTION_POP_SHARE_LINK = "Action__Pop_Share__Link"
    const val ACTION_POP_SHARE_ENTOURAGE = "Action__Pop_Share__On_Entourage"

    const val SHOW_POP_CLOSE = "View__Pop_Close_Entourage__Show"
    const val ACTION_POP_CLOSE_SUCCESS = "Action__Pop_Close_Entourage__Success"
    const val ACTION_POP_CLOSE_FAILED = "Action__Pop_Close_Entourage__Fail"

    //NEW_V8

    //GROUPES

    const val VIEW_GROUP_SHOW = "View__Group__Show"
    const val VIEW_GROUP_SHOW_DISCOVER = "View__Group__ShowDiscover"
    const val ACTION_GROUP_MY_GROUP = "Action__Group__MyGroup"
    const val ACTION_GROUP_DISCOVER = "Action__Group__Discover"
    const val ACTION_GROUP_MY_GROUP_CARD = "Action__Group__MyGroup_Card"
    const val ACTION_GROUP_DISCOVER_CARD = "Action__Group__Discover_Card"
    const val ACTION_GROUP_SEARCH_START = "Action__Group__Search_Start"
    const val ACTION_GROUP_SEARCH_VALIDATE = "Action__Group__Search_Validate"
    const val ACTION_GROUP_SEARCH_DELETE = "Action__Group__Search_Delete"
    const val ACTION_GROUP_SEARCH_SEE_RESULT = "Action__Group__Search_SeeResult"

    //GROUPE - NOUVEAU GROUPE

    const val ACTION_GROUP_PLUS = "Action__Group__Plus"

    const val ACTION_NEW_GROUP_BACK_ARROW = "Action__NewGroup__BackArrow"
    const val ACTION_NEW_GROUP_PREVIOUS = "Action__NewGroup__Previous"
    const val ACTION_NEW_GROUP_NEXT = "Action__NewGroup__Next"

    const val VIEW_NEW_GROUP_STEP1 = "View__NewGroup__Step1"
    const val ACTION_NEW_GROUP_ADD_LOCATION = "Action__NewGroup__AddLocation"
    const val VIEW_NEW_GROUP_ADD_LOCATION_SCREEN = "View__NewGroup__AddLocation_Screen"
    const val ACTION_NEW_GROUP_ADD_LOCATION_CITY = "Action__NewGroup__AddLocation_City"
    const val ACTION_NEW_GROUP_ADD_LOCATION_GEOLOC = "Action__NewGroup__AddLocation_Geoloc"
    const val ACTION_NEW_GROUP_ADD_LOCATION_VALIDATE = "Action__NewGroup__AddLocation_Validate"
    const val ACTION_NEW_GROUP_ADD_LOCATION_CLOSE = "Action__NewGroup__AddLocation_Close"

    const val VIEW_NEW_GROUP_STEP2 = "View__NewGroup__Step2"

    const val VIEW_NEW_GROUP_STEP3 = "View__NewGroup__Step3"
    const val ACTION_NEW_GROUP_STEP3_ADD_PICTURE = "Action__NewGroup__Step3_AddPicture"
    const val VIEW_NEW_GROUP_STEP3_PIC_GALLERY = "View__NewGroup__Step3_PicGallery"
    const val ACTION_NEW_GROUP_STEP3_PIC_GALLERY_VALIDATE = "Action__NewGroup__Step3_PicGallery_Validate"
    const val ACTION_NEW_GROUP_STEP3_PIC_GALLERY_CLOSE = "Action__NewGroup__Step3_PicGallery_Close"

    const val VIEW_NEW_GROUP_CONFIRMATION = "View__NewGroup__Confirmation"
    const val ACTION_NEW_GROUP_CONFIRMATION_NEW_POST = "Action__NewGroup__Confirmation_NewPost"
    const val ACTION_NEW_GROUP_CONFIRMATION_SKIP = "Action__NewGroup__Confirmation_Skip"

    const val VIEW_NEW_GROUP_CANCEL_POP = "View__NewGroup__CancelPop"
    const val ACTION_NEW_GROUP_CANCEL_POP_CANCEL = "Action__NewGroup__CancelPop_Cancel"
    const val ACTION_NEW_GROUP_CANCEL_POP_LEAVE = "Action__NewGroup__CancelPop_Leave"

    //GROUPE - FEED

    const val VIEW_GROUP_FEED_SHOW = "View__GroupFeed__Show"
    const val ACTION_GROUP_FEED_MORE_MEMBERS = "Action__GroupFeed__MoreMembers"
    const val ACTION_GROUP_FEED_JOIN = "Action__GroupFeed__Join"
    const val ACTION_GROUP_FEED_MORE_EVENTS = "Action__GroupFeed__MoreEvents"
    const val ACTION_GROUP_FEED_MORE_DESCRIPTION = "Action__GroupFeed__MoreDescription"
    const val ACTION_GROUP_FEED_ONE_EVENT = "Action__GroupFeed__OneEvent"
    const val ACTION_GROUP_FEED_DESCRIPTION_PAGE = "Action__GroupFeed__DescriptionPage"
    const val ACTION_GROUP_FEED_OPTION = "Action__GroupFeed__Option"
    const val ACTION_GROUP_FEED_BACK_ARROW = "Action__GroupFeed__BackArrow"
    const val VIEW_GROUP_FEED_FULL_DESCRIPTION = "View__GroupFeed__FullDescription"

    //GROUPE - LISTE DES MEMBRES

    const val VIEW_GROUP_MEMBER_SHOW_LIST = "View__GroupMember__ShowList"
    const val ACTION_GROUP_MEMBER_SEE_1_MEMBER = "Action__GroupMember__See1Member"
    const val ACTION_GROUP_MEMBER_WRITE_TO_1_MEMBER = "Action__GroupMember__WriteTo1Member"
    const val ACTION_GROUP_MEMBER_SEARCH_START = "Action__GroupMember__Search_Start"
    const val ACTION_GROUP_MEMBER_SEARCH_VALIDATE = "Action__GroupMember__Search_Validate"
    const val ACTION_GROUP_MEMBER_SEARCH_DELETE = "Action__GroupMember__Search_Delete"
    const val ACTION_GROUP_MEMBER_SEARCH_SEE_RESULT = "Action__GroupMember__Search_SeeResult"

    //GROUPE - PLUS

    const val ACTION_GROUP_FEED_PLUS = "Action__GroupFeed__Plus"
    const val ACTION_GROUP_FEED_NEW_EVENT = "Action__GroupFeed__NewEvent"
    const val ACTION_GROUP_FEED_NEW_POST = "Action__GroupFeed__NewPost"
    const val ACTION_GROUP_FEED_PLUS_CLOSE = "Action__GroupFeed__Plus_Close"
    const val VIEW_GROUP_FEED_NEW_POST_SCREEN = "View__GroupFeed__NewPost_Screen"
    const val ACTION_GROUP_FEED_NEW_POST_ADD_PIC = "Action__GroupFeed__NewPost_AddPic"
    const val ACTION_GROUP_FEED_NEW_POST_VALIDATE_PIC = "Action__GroupFeed__NewPost_ValidatePic"
    const val ACTION_GROUP_FEED_NEW_POST_VALIDATE = "Action__GroupFeed__NewPost_Validate"

    //GROUPES - OPTIONS

    const val VIEW_GROUP_OPTION_SHOW = "View__GroupOption__Show"
    const val ACTION_GROUP_OPTION_EDIT_GROUP = "Action__GroupOption__EditGroup"
    const val VIEW_GROUP_OPTION_EDITION = "View__GroupOption__Edition"
    const val ACTION_GROUP_OPTION_RULES = "Action__GroupOption__Rules"
    const val VIEW_GROUP_OPTION_RULES = "View__GroupOption__Rules"
    const val ACTION_GROUP_OPTION_QUIT = "Action__GroupOption__Quitt"
    const val ACTION_GROUP_OPTION_REPORT = "Action__GroupOption__Report"

    const val ACTION_GROUP_OPTION_NOTIF_ALL = "Action__GroupOption__Notif_All"
    const val ACTION_GROUP_OPTION_NOTIF_EVENT = "Action__GroupOption__Notif_Event"
    const val ACTION_GROUP_OPTION_NOTIF_MESSAGE = "Action__GroupOption__Notif_Message"
    const val ACTION_GROUP_OPTION_NOTIF_MEMBER = "Action__GroupOption__Notif_Member"


    val TAG: String? = AnalyticsEvents::class.java.simpleName

    fun logEvent(event: String) {
        get().firebase.logEvent(event, null)
    }

    fun onLocationPermissionGranted(isPermissionGranted: Boolean) {
        get().firebase.setUserProperty("EntourageGeolocEnable", if (isPermissionGranted) "YES" else "NO")
    }

    fun updateUserInfo(user: User, context: Context, areNotificationsEnabled: Boolean) {
        /* TODO: catch this event elsewhere
        if (areNotificationsEnabled) {
            logEvent(EntourageEvents.EVENT_GEOLOCATION_POPUP_REFUSE);
        } else {
            logEvent(EntourageEvents.EVENT_GEOLOCATION_POPUP_ACCEPT);
        }
         */
        FirebaseCrashlytics.getInstance().setUserId(user.id.toString())

        val mFirebaseAnalytics = get().firebase
        mFirebaseAnalytics.setUserId(user.id.toString())
        mFirebaseAnalytics.setUserProperty("EntourageUserType", "Public")
        mFirebaseAnalytics.setUserProperty("Language", Locale.getDefault().language)
        user.partner?.let {
            mFirebaseAnalytics.setUserProperty("EntouragePartner", it.name)
        }
        user.firebaseProperties?.forEach { value ->
            mFirebaseAnalytics.setUserProperty(value.key,value.value)
            if(value.value.length>36) {
                Timber.e("Firebase Property too long: key=${value.key}; value=${value.value}")
            }
        }
        val geolocStatus = if (isLocationPermissionGranted()) "YES" else "NO"
        mFirebaseAnalytics.setUserProperty("EntourageGeolocEnable", geolocStatus)

        val notificationsEnabled = get().sharedPreferences.getBoolean(EntourageApplication.KEY_NOTIFICATIONS_ENABLED, true)
        mFirebaseAnalytics.setUserProperty("EntourageNotifEnable", if (notificationsEnabled && areNotificationsEnabled) "YES" else "NO")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mFirebaseAnalytics.setUserProperty("BackgroundRestriction", if ((Objects.requireNonNull(context.getSystemService(Context.ACTIVITY_SERVICE)) as ActivityManager).isBackgroundRestricted) "YES" else "NO")
        }

        mFirebaseAnalytics.setUserProperty("engaged_user", if (user.isEngaged) "Yes" else "No")
    }
}