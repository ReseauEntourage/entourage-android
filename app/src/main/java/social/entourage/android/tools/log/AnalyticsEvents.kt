package social.entourage.android.tools.log

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.analytics.FirebaseAnalytics
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
    // LOGIN EVENTS
    //----------------------------//
    const val EVENT_VIEW_LOGIN_LOGIN = "View__Login__Login"
    const val EVENT_ACTION_LOGIN_SMS = "Action__Login__SMSCodeRequest"
    const val EVENT_ACTION_LOGIN_SUBMIT = "Action__Login__LoginSubmit"
    const val EVENT_ACTION_LOGIN_SUCCESS = "Action__Login__LoginSuccess"
    const val EVENT_ERROR_LOGIN_ERROR = "Error__Login__LoginError"
    const val EVENT_ERROR_LOGIN_FAIL = "Error__Login__LoginFail"
    const val EVENT_ERROR_LOGIN_PHONE = "Error__Login__TelephoneSubmitError"

    //----------------------------//
    // ONBOARDING - START
    //-------

    //------PROFILE
    const val EVENT_VIEW_ONBOARDING_CHOOSE_PROFILE = "View__Onboarding__ChooseProfile"

    //------PHOTO
    const val EVENT_VIEW_ONBOARDING_CHOOSE_PHOTO = "View__Onboarding__ChoosePhoto"
    const val EVENT_ACTION_ONBOARDING_UPLOAD_PHOTO = "Action__Onboarding__UploadPhoto"
    const val EVENT_ACTION_ONBOARDING_TAKE_PHOTO = "Action__Onboarding__TakePhoto"

    //-------
    // ONBOARDING - END
    //----------------------------//
    //----------------------------//
    // PROFILE EDIT - START
    //SUPRESS POST
    const val POST_SUPPRESSED = "View__DeletePostPop"
    const val SUPPRESS_CLICK = "Clic__DeletePostPop__Delete"
    //Suppress post

    const val Click_delete_comm = "Clic__DeleteCommPop__Delete"
    const val Delete_comm = "View__DeleteCommPop"
    const val Click_delete_mess = "Clic_DeleteMessagePop_Delete"
    const val Delete_mess = "View__DeleteMessagePop"
    //Action_Group_Pop_Ipost
    //View_Group_Pop
    //Action_Group_Pop_Iclose
    //Group Present Pop
    const val I_present_view_pop = "View__Group__Pop"
    const val I_present_click_i_post = "Action__Group__Pop_Ipost"
    const val i_present_close_pop = "Action__Group__Pop_Iclose"

    //PedagoList
    const val Pedago_View = "View__PedagoList"
    const val Pedago_View_all_tag = "Action__PedagoAllTag"
    const val Pedago_View_understand_tag = "Action__PedagoUnderstandTag"
    const val Pedago_View_act_tag = "Action__PedagoActTag"
    const val Pedago_View_inspire_tag = "Action__PedagoInspireTag"
    const val Pedago_View_card = "Action__PedagoCard"


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
    const val EVENT_PHOTO_SUBMIT = "SubmitInstantPhoto"
    const val EVENT_GEOLOCATION_ACTIVATE_04_4A = "ActivateGeolocFromScreen04_4UserBlocked"

    // SCREEN Events

    const val EVENT_SCREEN_09_1_ME = "Screen09_1MyProfileViewAsPublicView"
    const val EVENT_SCREEN_09_1_OTHER = "Screen09_1OtherUserProfileView"
    const val EVENT_SCREEN_09_2 = "Screen09_2EditMyProfileView"
    const val EVENT_SCREEN_09_4 = "Screen09_4EditPasswordView"
    const val EVENT_SCREEN_09_4_SUBMIT = "Screen09_4ChangePasswordSubmit"
    const val EVENT_SCREEN_09_5 = "Screen09_5EditNameView"
    const val EVENT_SCREEN_09_9 = "Screen09_9MovePhotoView"
    const val EVENT_SCREEN_06_1 = "Screen06_1FeedView"
    const val EVENT_SCREEN_06_2 = "Screen06_2MapView"

    //MENU Events
    const val EVENT_PROFILE_FROM_MENU = "Open_Profile_From_Menu"
    const val EVENT_ABOUT_FAQ = "AppFAQClick"

    //FEED Events
    const val EVENT_FEED_USERPROFILE = "UserProfileClick"
    const val EVENT_FEED_ACTIVE_CLOSE_OVERLAY = "OpenActiveCloseOverlay"
    const val EVENT_FEED_MAPCLICK = "MapClick"
    const val EVENT_FEED_HEATZONECLICK = "HeatzoneMapClick"
    const val EVENT_FEED_OPEN_ENTOURAGE = "OpenEntouragePublicPage"
    const val EVENT_FEED_OPEN_CONTACT = "OpenEnterInContact"
    const val EVENT_FEED_RECENTERCLICK = "RecenterMapClick"
    const val EVENT_FEED_SCROLL_LIST = "ScrollListPage"
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
    const val EVENT_ENTOURAGE_VIEW_INVITE_CLOSE = "InviteFriendsClose"
    const val EVENT_ENTOURAGE_VIEW_SWITCH_PUBLIC = "EntouragePublicPageFromMessages"
    const val EVENT_ENTOURAGE_VIEW_ASK_JOIN = "AskJoinFromPublicPage"
    const val EVENT_ENTOURAGE_CREATE_CHANGE_LOCATION = "ChangeLocationClick"
    const val EVENT_ENTOURAGE_CLOSE_POPUP_CANCEL = "CancelClosePopup"
    const val EVENT_ENTOURAGE_SHARE_MEMBER = "ShareLinkAsMemberOrCreator"
    const val EVENT_ENTOURAGE_SHARE_NONMEMBER = "ShareLinkAsNonMember"

    //MY ENTOURAGES Events
    const val EVENT_MYENTOURAGES_BANNER_CLICK = "BannerMessageClick"
    const val EVENT_MYENTOURAGES_MESSAGE_OPEN = "MessageOpen"
    const val ACTION_PLUS_HELP = "Action__Plus__Help"
    const val ACTION_PLUS_GOOD_WAVES = "Action__Plus__GoodWaves"
    const val ACTION_PLUS_CREATE_ASKFORHELP = "Action__Plus__CreateAskForHelp"
    const val ACTION_PLUS_CREATE_CONTRIBUTE = "Action__Plus__CreateContribute"
    const val ACTION_PLUS_CREATE_OUTING = "Action__Plus__CreateOuting"
    const val ACTION_PLUS_BACK = "Action__Plus__BackPressed"

    //MY ENTOURAGES FILTER Events
    const val EVENT_MYENTOURAGES_FILTER_EXIT = "ExitMessagesFilter"
    const val EVENT_MYENTOURAGES_FILTER_SAVE = "SaveMessagesFilter"
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
    const val ACTION_PROFILE_BLOG = "Action__Profile__SCBonjour"
    const val ACTION_PROFILE_CHART = "Action__Profile__Ethic"
    const val ACTION_PROFILE_DONATION = "Action__Profile__Donate"
    const val ACTION_PROFILE_AMBASSADOR = "Action__Profile__Ambassador"
    const val ACTION_PROFILE_GOODWAVES = "Action__Profile__GoodWaves"
    const val ACTION_PROFILE_MODPROFIL = "Action__Profile__ModProfil"
    const val ACTION_PROFILE_SHOWPROFIL = "Action__Profile__ShowProfil"
    const val VIEW_PROFILE_MENU = "View__Profile__Menu"
    const val VIEW_PLUS_SCREEN = "View__Plus__Screen"

    const val VIEW_ADD_USERNAME_SUBMIT = "Action__Add__UsernameSubmit"
    const val ACTION_PROFILE_EDITPWD = "Action__Profile__ChangePWD"
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

    //SHARE EVENT
    const val CONTRIB_SHARED= "Action__Contrib__Share"
    const val SOLICITATION_SHARED = "Action__Demand__Share"
    const val EVENT_SHARED = "Action__EventFeed__Share"
    const val GROUP_SHARED = "Action__GroupFeed__Share"
    const val EVENT_OPTION_SHARED = "Action__EventOption__Share"


    //GROUPES

    const val VIEW_GROUP_SHOW = "View__Group__MyGroups"
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

    const val ACTION_GROUP_SHARE = "Action_GroupFeed_Share"
    const val ACTION_GROUPOPTION_SHARE = "Action__GroupOption__Share"
    const val ACTION_GROUP_REPORT = "Action__GroupOption__Report_Confirmation"

    /***
     * Home
     */

//Tab bar
    const val Action_Tabbar_home = "Action__Tab__Feeds"
    const val Action_Tabbar_help = "Action__Tab__Aid"
    const val Action_Tabbar_messages = "Action__Tab__Messages"
    const val Action_Tabbar_groups = "Action__Tab__Group"
    const val Action_Tabbar_events = "Action__Tab__Event"

//Home:
    const val Home_view_home = "View__Home"
    const val Home_action_notif = "Action__Home__Notif"
    const val Home_action_profile = "Action__Tab__Profil"
    const val Home_action_pedago = "Action__Home__Pedago"
    const val Home_action_meetcount = "Action__Home__MeetCounter"
    const val Home_action_groupcount = "Action__Home__GroupCounter"
    const val Home_action_eventcount = "Action__Home__EventCounter"
    const val Home_action_moderator = "Action__Home__Moderator"
    const val Home_action_map = "Action__Home__Map"
    const val Home_view_notif = "View__Notif"

//Profile
    const val Profile_view_profile = "View__Profile"
    const val Profile_action_modify = "Clic__Profile__Modify"
    const val Profile_view_param = "View__Profile__Param"

//Help
    const val Help_view_contrib = "View__Aid"
    const val Help_view_demand = "View__Aid__DemandList"
    const val Help_view_myactions = "View__Aid__MyAds"
    const val Help_action_filters = "Action__Aid__CategoryFilter"
    const val Help_action_location = "Action__Aid__LocationFilter"
    const val Help_action_create = "Action__Aid__New"

//Help detail
    const val Help_view_contrib_detail = "View__Contrib__Detail"
    const val Help_view_demand_detail = "View__Demand__Detail"
    const val Help_action_contrib_contact = "Action__Contrib__Contact"
    const val Help_action_demand_contact = "Action__Demand__Contact"

//Help create
    const val Help_create_demand_chart = "View__NewDemand__Chart"
    const val Help_create_demand_1 = "View__NewDemand__Step1"
    const val Help_create_demand_2 = "View__NewDemand__Step2"
    const val Help_create_demand_3 = "View__NewDemand__Step3"
    const val Help_create_demand_end = "View__NewDemand__Confirmation"

    const val Help_create_contrib_chart = "View__NewContrib__Chart"
    const val Help_create_contrib_1 = "View__NewContrib__Step1"
    const val Help_create_contrib_2 = "View__NewContrib__Step2"
    const val Help_create_contrib_3 = "View__NewContrib__Step3"
    const val Help_create_contrib_end = "View__NewContrib__Confirmation"

//Messages
    const val Message_view = "View__Discussion__List"
    const val Message_view_detail = "View__Discussion__Detail"
    const val Message_action_param = "Action__Discussion__Param"

//Events
    const val Event_view_discover = "View__Event__List"
    const val Event_view_my = "View__Event__MyEvents"
    const val Event_action_filter = "Action__Event__LocationFilter"
    const val Event_action_create = "Action__Event__New"

//Event Create
    const val Event_create_1 = "View__NewEvent__Step1"
    const val Event_create_2 = "View__NewEvent__Step2"
    const val Event_create_3 = "View__NewEvent__Step3"
    const val Event_create_4 = "View__NewEvent__Step4"
    const val Event_create_5 = "View__NewEvent__Step5"
    const val Event_create_end = "View__NewEvent__Confirmation"

//Event detail
    const val Event_detail_main = "View__EventFeed__Show"
    const val Event_detail_full = "View__EventFeed__About"
    const val Event_detail_action_participate = "Action__EventFeed__Participate"
    const val Event_detail_action_param = "Action__EventFeed__Param"
    const val Event_detail_action_post = "Action__EventFeed__NewPost"

//PreOnboarding
    const val PreOnboard_car1 = "View__Start__Carrousel1"
    const val PreOnboard_car2 = "View__Start__Carrousel2"
    const val PreOnboard_car3 = "View__Start__Carrousel3"

    const val PreOnboard_view_choice = "View__Start__SignUpOrLoginPage"
    const val PreOnboard_action_signup = "Action__Onboarding__SignUpStart"
    const val PreOnboard_action_signin = "Action__Login__LoginStart"

//Onboarding
    const val Onboard_name = "View__Onboarding__InputNames"
    const val Onboard_code = "View__Onboarding__InputCode"
    const val Onboard_profile = "View__Onboarding__InputProfile"
    const val Onboard_end = "View__Onboarding__Confirmation"
    const val View_WelcomeOfferHelp_Day2 = "View_WelcomeOfferHelp_Day2"
    const val Action_WelcomeOfferHelp_Day2 = "Action_WelcomeOfferHelp_Day2"

    const val View_WelcomeOfferHelp_Day8 = "View_WelcomeOfferHelp_Day8"
    const val Action_WelcomeOfferHelp_Day8 = "Action_WelcomeOfferHelp_Day8"

    const val View_WelcomeOfferHelp_Day11 = "View_WelcomeOfferHelp_Day11"
    const val Action_WelcomeOfferHelp_Day11 = "Action_WelcomeOfferHelp_Day11"

    const val NotificationReceived__Demand = "NotificationReceived__Demand"

    const val NotificationClicked__Demand = "NotificationClicked__Demand"

    const val NotificationReceived__Contribution = "NotificationReceived__Contribution"

    const val NotificationClicked__Contribution = "NotificationClicked__Contribution"

    const val NotificationReceived__PostGroup = "NotificationReceived__PostGroup"

    const val NotificationClicked__PostGroup = "NotificationClicked__PostGroup"

    const val NotificationReceived__CommentGroup = "NotificationReceived__CommentGroup"

    const val NotificationClicked__CommentGroup = "NotificationClicked__CommentGroup"

    const val NotificationReceived__MemberGroup = "NotificationReceived__MemberGroup"

    const val NotificationClicked__MemberGroup = "NotificationClicked__MemberGroup"

    const val NotificationReceived__PostEvent = "NotificationReceived__PostEvent"

    const val NotificationClicked__PostEvent = "NotificationClicked__PostEvent"

    const val NotificationReceived__CommentEvent = "NotificationReceived__CommentEvent"

    const val NotificationClicked__CommentEvent = "NotificationClicked__CommentEvent"

    const val NotificationReceived__MemberEvent = "NotificationReceived__MemberEvent"

    const val NotificationClicked__MemberEvent = "NotificationClicked__MemberEvent"

    const val NotificationReceived__EventInGroup = "NotificationReceived__EventInGroup"

    const val NotificationClicked__EventInGroup = "NotificationClicked__EventInGroup"

    const val NotificationReceived__PrivateMessage = "NotificationReceived__PrivateMessage"

    const val NotificationClicked__PrivateMessage = "NotificationClicked__PrivateMessage"

    const val NotificationReceived__ModifiedEvent = "NotificationReceived__ModifiedEvent"

    const val NotificationClicked__ModifiedEvent = "NotificationClicked__ModifiedEvent"

    const val NotificationReceived__CanceledEvent = "NotificationReceived__CanceledEvent"

    const val NotificationClicked__CanceledEvent = "NotificationClicked__CanceledEvent"

    const val NotificationReceived__OfferHelp__WDay1 = "NotificationReceived__OfferHelp__WDay1"

    const val NotificationClicked__OfferHelp__WDay1 = "NotificationClicked__OfferHelp__WDay1"

    const val NotificationReceived__OfferHelp__WDay2 = "NotificationReceived__OfferHelp__WDay2"

    const val NotificationClicked__OfferHelp__WDay2 = "NotificationClicked__OfferHelp__WDay2"

    const val NotificationReceived__OfferHelp__WDay5 = "NotificationReceived__OfferHelp__WDay5"

    const val NotificationClicked__OfferHelp__WDay5 = "NotificationClicked__OfferHelp__WDay5"

    const val NotificationReceived__OfferHelp__WDay8 = "NotificationReceived__OfferHelp__WDay8"

    const val NotificationClicked__OfferHelp__WDay8 = "NotificationClicked__OfferHelp__WDay8"

    const val NotificationReceived__OfferHelp__WDay11 = "NotificationReceived__OfferHelp__WDay11"

    const val NotificationClicked__OfferHelp__WDay11 = "NotificationClicked__OfferHelp__WDay11"


    const val View__StateDemandPop__Day10 = "View__StateDemandPop__Day10"
    const val Clic__StateDemandPop__No__Day10 = "Clic__StateDemandPop__No__Day10"
    const val Clic__StateDemandPop__Yes__Day10 = "Clic__StateDemandPop__Yes__Day10"
    const val View__StateDemandPop__No__Day10 = "View__StateDemandPop__No__Day10"
    const val View__DeleteDemandPop__Day10 = "View__DeleteDemandPop__Day10"
    const val   Clic__SeeDemand__Day10 = "Clic__SeeDemand__Day10"

    const val  View__StateContribPop__Day10 = "View__StateContribPop__Day10"
    const val  Clic__StateContribPop__No__Day10 = "Clic__StateContribPop__No__Day10"
    const val  Clic__StateContribPop__Yes__Day10 = "Clic__StateContribPop__Yes__Day10"
    const val  View__StateContribPop__No__Day10 = "View__StateContribPop__No__Day10"
    const val  Clic__SeeContrib__Day10 = "Clic__SeeContrib__Day10"
    const val   View__DeleteContribPop__Day10 = "View__DeleteContribPop__Day10"

    //NEW EVENT
    const val   View__Home = "View__Home"
    const val   Action__Home__Notif = "Action__Home__Notif"
    const val   Action__Tab__Profil = "Action__Tab__Profil"
    const val   Action_Home_Demand_Detail = "Action_Home_Demand_Detail"
    const val   Action_Home_Demand_All = "Action_Home_Demand_All"
    const val   Action_Home_Event_Detail = "Action_Home_Event_Detail"
    const val   Action_Home_Event_All = "Action_Home_Event_All"
    const val   Action_Home_Group_Detail = "Action_Home_Group_Detail"
    const val   Action_Home_Group_All = "Action_Home_Group_All"
    const val   Action__Home__Map = "Action__Home__Map"
    const val   Action_Home_Article = "Action_Home_Article"
    const val   Action__Home__Pedago = "Action__Home__Pedago"
    const val   Action_Home_CreateEvent = "Action_Home_CreateEvent"
    const val   Action_Home_CreateGroup = "Action_Home_CreateGroup"
    const val   Action__Home__Moderator = "Action__Home__Moderator"
    const val   Action_Home_Contrib_Detail = "Action_Home_Contrib_Detail"
    const val   Action_Home_Contrib_All = "Action_Home_Contrib_All"
    const val   Action_Home_Buffet = "Action_Home_Buffet"

// NEW EVENT
    const val   View__Event__List = "View__Event__List"
    const val   Action__Event__LocationFilter = "Action__Event__LocationFilter"
    const val   Action__Event__New = "Action__Event__New"

    const val   Clic_CopyPaste_LongClic = "Clic_CopyPaste_LongClic"
    const val   Clic_CopyPaste_Settings = "Clic_CopyPaste_Settings"

    const val   Clic_Post_Like = "Clic_Post_Like"
    const val   Clic_Post_List_Reactions = "Clic_Post_List_Reactions"
    const val   Clic_ListReactions_Contact = "Clic_ListReactions_Contact"

    const val   view_miss_location_popup = "view_miss_location_popup"
    const val   clic_miss_location_add = "clic_miss_location_add"

    const val   Clic_Group_Create_Poll = "Clic_Group_Create_Poll"
    const val   Clic_Event_Create_Poll = "Clic_Event_Create_Poll"
    const val   Clic_Group_Validate_Poll = "Clic_Group_Validate_Poll"
    const val   Clic_Event_Validate_Poll = "Clic_Event_Validate_Poll"
    const val   Clic_Group_Poll_See_Votes = "Clic_Group_Poll_See_Votes"
    const val   Clic_Event_Poll_See_Votes = "Clic_Event_Poll_See_Votes"


    const val   view_update_version = "view_update_version"
    const val   clic_update_version_validate = "clic_update_version_validate"
    const val   clic_update_version_cancel = "clic_update_version_cancel"


    // Écran de bienvenue
    const val onboarding_welcome_config_later_clic = "onboarding_welcome_config_later_clic"
    const val onboarding_welcome_next_clic = "onboarding_welcome_next_clic"

    // Écran envies d'agir
    const val onboarding_actions_config_later_clic = "onboarding_actions_config_later_clic"
    const val onboarding_actions_next_clic = "onboarding_actions_next_clic"

    // Écran centres d'intérêts
    const val onboarding_interests_config_later_clic = "onboarding_interests_config_later_clic"
    const val onboarding_interests_next_clic = "onboarding_interests_next_clic"

    // Écran catégories d'entraide
    const val onboarding_donations_categories_config_later_clic = "onboarding_donations_categories_config_later_clic"
    const val onboarding_donations_categories_next_clic = "onboarding_donations_categories_next_clic"

    // Écran de fin
    const val onboarding_end_browse_events_clic = "onboarding_end_browse_events_clic"



    const val onboarding_welcome_view = "onboarding_welcome_view"
    const val onboarding_actions_view = "onboarding_actions_view"
    const val onboarding_interests_view = "onboarding_interests_view"
    const val onboarding_donations_categories_view = "onboarding_donations_categories_view"
    const val onboarding_end_view = "onboarding_end_view"

    const val has_user_activated_notif = "has_user_activated_notif"
    const val has_user_disabled_notif = "has_user_disabled_notif"

    const val notification_settings_page_view = "notification_settings_page_view"
    const val notification_activate_button_clic = "notification_activate_button_clic"
    const val notification_deactivate_button_clic = "notification_deactivate_button_clic"


    const val popup_event_last_day_view = "popup_event_last_day_view"
    const val popup_event_last_day_accept = "popup_event_last_day_accept"
    const val popup_event_last_day_decline = "popup_event_last_day_decline"

    const val actions_filter_apply_clic = "actions_filter_apply_clic"
    const val groups_filter_apply_clic = "groups_filter_apply_clic"
    const val events_filter_apply_clic = "events_filter_apply_clic"

    const val actions_searchbar_clic = "actions_searchbar_clic"
    const val groups_searchbar_clic = "groups_searchbar_clic"
    const val events_searchbar_clic = "events_searchbar_clic"

    const val filter_tag_item_ = "filter_tag_item_"




    // EVENT NOTIF PUSH



    val TAG: String? = AnalyticsEvents::class.java.simpleName

    fun logEvent(event: String) {
        //Timber.d("***** FireB Log event : ${event} -- fb:${get().firebase}")
        get().firebase.logEvent(event, null)
    }
    fun logEventWithContext(context: Context, event: String) {
        FirebaseAnalytics.getInstance(context).logEvent(event, null)
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

        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        mFirebaseAnalytics.setUserProperty("EntourageNotifEnable", if (notificationsEnabled && areNotificationsEnabled) "YES" else "NO")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mFirebaseAnalytics.setUserProperty("BackgroundRestriction", if ((Objects.requireNonNull(context.getSystemService(Context.ACTIVITY_SERVICE)) as ActivityManager).isBackgroundRestricted) "YES" else "NO")
        }

        mFirebaseAnalytics.setUserProperty("engaged_user", if (user.isEngaged) "YES" else "NO")
    }
}