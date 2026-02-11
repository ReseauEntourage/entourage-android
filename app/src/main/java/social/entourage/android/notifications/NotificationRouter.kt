package social.entourage.android.notifications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import social.entourage.android.MainActivity
import social.entourage.android.Navigation
import social.entourage.android.R
import social.entourage.android.actions.detail.ActionDetailActivity
import social.entourage.android.api.model.ActionSummary
import social.entourage.android.api.model.HomeActionParams
import social.entourage.android.api.model.HomeType
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.home.BirthdayActivity
import social.entourage.android.small_talks.SmallTalkListOtherBands
import social.entourage.android.tools.utils.Const
import social.entourage.android.user.partner.PartnerDetailActivity
import social.entourage.android.welcome.WelcomeFiveActivity
import social.entourage.android.welcome.WelcomeFourActivity
import social.entourage.android.welcome.WelcomeOneActivity
import social.entourage.android.welcome.WelcomeThreeActivity
import social.entourage.android.welcome.WelcomeTwoActivity
import timber.log.Timber
import social.entourage.android.tools.log.AnalyticsEvents

/**
 * Centralized Router for all Notification-related navigation.
 * Handles both Push Notifications (via MainActivity) and In-App Notifications.
 */
object NotificationRouter {

    const val TAG = "NotificationRouter"

    // Argument wrapper to unify Push and In-App data
    data class NotificationArguments(
        val instance: String? = null,
        val id: Int = 0,
        val postId: Int? = null,
        val stage: String? = null,
        val popup: String? = null,
        val notifContext: String? = null,
        val tracking: String? = null,
        val contentJson: String? = null // For passing raw content if needed
    )

    private val validTracking = listOf(
        "public_chat_message_on_create",
        "post_on_create_to_outing",
        "post_on_create",
        "comment_on_create_to_outing",
        "comment_on_create",
        "chat_message_on_mention",
        "reaction_on_create",
        "survey_response_on_create"
    )

    fun navigate(context: Context, fragmentManager: FragmentManager?, args: NotificationArguments) {
        Timber.tag(TAG).d("Navigate with args: $args")
        logNotificationClicked(context, args)

        // 1. Handle Popup / Special Contexts
        if (args.popup == "outing_on_day_before" || args.notifContext == "outing_on_day_before") {
            if (context is MainActivity) {
                context.ifEventLastDay(args.id)
            } else {
                MainActivity.shouldLaunchEventPopUp = args.id
                if (context is Activity) {
                    context.finish()
                }
            }
            return
        }

        // 2. Handle Stages (Welcome / Birthday)
        if (!args.stage.isNullOrEmpty()) {
            when (args.stage) {
                "h1" -> context.startActivity(Intent(context, WelcomeOneActivity::class.java))
                "j2" -> context.startActivity(Intent(context, WelcomeTwoActivity::class.java))
                "j5" -> context.startActivity(Intent(context, WelcomeThreeActivity::class.java))
                "j8" -> context.startActivity(Intent(context, WelcomeFourActivity::class.java))
                "j11" -> context.startActivity(Intent(context, WelcomeFiveActivity::class.java))
                "birthday" -> {
                    val intent = Intent(context, BirthdayActivity::class.java)
                    intent.putExtra("notification_content", args.contentJson)

                    if (context !is MainActivity) {
                        // If not in MainActivity, route through it to ensure back stack
                        val mainIntent = Intent(context, MainActivity::class.java)
                        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        mainIntent.putExtra("goBirthday", true)
                        // Pass content along if needed by MainActivity logic, though goBirthday usually handles it
                        args.contentJson?.let { mainIntent.putExtra("notification_content", it) }
                        context.startActivity(mainIntent)
                    } else {
                        (context as MainActivity).goHome()
                        context.startActivity(intent)
                    }
                    return
                }
            }
        }

        // 3. Handle Discussion / Outing redirection (The "Complex Logic")
        val isDiscussionTracking = (args.notifContext in validTracking || args.tracking in validTracking)
        if ((args.instance == "outings" || args.instance == "outing") && isDiscussionTracking) {
            DetailConversationActivity.isSmallTalkMode = false
            val intent = Intent(context, DetailConversationActivity::class.java).apply {
                putExtras(
                    bundleOf(
                        Const.ID to args.id,
                        Const.SHOULD_OPEN_KEYBOARD to false,
                        Const.IS_CONVERSATION_1TO1 to true,
                        Const.IS_MEMBER to true,
                        Const.IS_CONVERSATION to true,
                        Const.HAS_TO_SHOW_MESSAGE to true
                    )
                )
            }
            context.startActivity(intent)
            return
        }

        // 4. Handle Standard Instances
        val instanceType = getInstanceTypeFromName(args.instance)
        when (instanceType) {
            InstanceType.POIS -> {
                val poi = Poi()
                poi.uuid = "${args.id}"
                ReadPoiFragment.newInstance(poi, "")
                    .show(fragmentManager!!, ReadPoiFragment.TAG)
            }
            InstanceType.USERS -> {
                val params = HomeActionParams()
                params.id = args.id
                Navigation.navigate(context, fragmentManager!!, HomeType.USER, ActionSummary.SHOW, params)
            }
            InstanceType.NEIGHBORHOODS -> {
                val params = HomeActionParams()
                params.id = args.id
                Navigation.navigate(context, fragmentManager!!, HomeType.NEIGHBORHOOD, ActionSummary.SHOW, params)
            }
            InstanceType.RESOURCES -> {
                val params = HomeActionParams()
                params.id = args.id
                Navigation.navigate(context, fragmentManager!!, HomeType.RESOURCE, ActionSummary.SHOW, params)
            }
            InstanceType.OUTINGS -> {
                val params = HomeActionParams()
                params.id = args.id
                Navigation.navigate(context, fragmentManager!!, HomeType.OUTING, ActionSummary.SHOW, params)
            }
            InstanceType.OUTINGS_MESSAGE, InstanceType.CONVERSATIONS -> {
                DetailConversationActivity.isSmallTalkMode = false
                context.startActivity(
                    Intent(context, DetailConversationActivity::class.java).putExtras(
                        bundleOf(
                            Const.ID to args.id,
                            Const.SHOULD_OPEN_KEYBOARD to false,
                            Const.IS_CONVERSATION_1TO1 to true,
                            Const.IS_MEMBER to true,
                            Const.IS_CONVERSATION to true
                        )
                    )
                )
            }
            InstanceType.CONTRIBUTIONS -> {
                context.startActivity(
                    Intent(context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, args.id)
                        .putExtra(Const.IS_ACTION_DEMAND, false)
                )
            }
            InstanceType.SOLICITATIONS -> {
                context.startActivity(
                    Intent(context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, args.id)
                        .putExtra(Const.IS_ACTION_DEMAND, true)
                )
            }
            InstanceType.SMALLTALK -> {
                DetailConversationActivity.isSmallTalkMode = true
                DetailConversationActivity.smallTalkId = args.id.toString()
                context.startActivity(
                    Intent(context, DetailConversationActivity::class.java).putExtras(
                        bundleOf(
                            Const.ID to args.id,
                            Const.SHOULD_OPEN_KEYBOARD to false,
                            Const.IS_CONVERSATION_1TO1 to true,
                            Const.IS_MEMBER to true,
                            Const.IS_CONVERSATION to true
                        )
                    )
                )
            }
            InstanceType.AlMOSTMATCH -> {
                context.startActivity(Intent(context, SmallTalkListOtherBands::class.java))
            }
            InstanceType.PARTNERS -> {
                context.startActivity(
                    Intent(context, PartnerDetailActivity::class.java)
                        .putExtra(Const.PARTNER_ID, args.id)
                        .putExtra(Const.IS_FROM_NOTIF, true)
                )
            }
            InstanceType.NONE -> {
                // Fallback or explicit NONE handling
                return
            }
            else -> {
                // Handle Post types which are not directly in the enum or need special handling
                if (instanceType == InstanceType.OUTING_POSTS) {
                     args.postId?.let { postId ->
                        val params = HomeActionParams()
                        params.id = args.id
                        params.postId = postId
                        Navigation.navigate(context, fragmentManager!!, HomeType.OUTING_POST, ActionSummary.SHOW, params)
                    }
                } else if (instanceType == InstanceType.NEIGHBORHOODS_POSTS) {
                    args.postId?.let { postId ->
                        val params = HomeActionParams()
                        params.id = args.id
                        params.postId = postId
                        Navigation.navigate(context, fragmentManager!!, HomeType.NEIGHBORHOOD_POST, ActionSummary.SHOW, params)
                    }
                }
            }
        }
    }

    enum class InstanceType {
        POIS, USERS, NEIGHBORHOODS, NEIGHBORHOODS_POSTS, RESOURCES, OUTINGS,
        OUTINGS_MESSAGE, OUTING_POSTS, CONTRIBUTIONS, SOLICITATIONS,
        CONVERSATIONS, SMALLTALK, AlMOSTMATCH, PARTNERS, NONE
    }

    private fun logNotificationClicked(context: Context, args: NotificationArguments) {
        val stage = args.stage
        if (!stage.isNullOrEmpty()) {
            when (stage) {
                "h1" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay1)
                "j2" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay2)
                "j5" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay5)
                "j!" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay8)
                "j11" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay11)
            }
        }
        val tracking = args.tracking
        if (!tracking.isNullOrEmpty()) {
             when (tracking) {
                "join_request_on_create" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__MemberEvent)
                "outing_on_update" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__ModifiedEvent)
                "outing_on_create" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__PostEvent)
                "post_on_create_to_neighborhood" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__PostGroup)
                "comment_on_create_to_neighborhood" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__CommentGroup)
                "comment_on_create_to_outing" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__CommentEvent)
                "outing_on_add_to_neighborhood" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__EventInGroup)
                "contribution_on_create" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__Contribution)
                "solicitation_on_create" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__Demand)
                "private_chat_message_on_create" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__PrivateMessage)
                "join_request_on_create_to_neighborhood" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__MemberGroup)
                "join_request_on_create_to_outing" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__MemberEvent)
                "outing_on_cancel" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__CanceledEvent)
                "post_on_create_to_outing" -> AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__PostEvent)
                "public_chat_message_on_create" -> AnalyticsEvents.logEvent("UNDEFINED_PUSH_TRACKING")
             }
        }
    }

    private fun getInstanceTypeFromName(instanceName: String?): InstanceType {
        return when (instanceName) {
            "pois", "poi" -> InstanceType.POIS
            "users", "user" -> InstanceType.USERS
            "neighborhoods", "neighborhood" -> InstanceType.NEIGHBORHOODS
            "neighborhood_post" -> InstanceType.NEIGHBORHOODS_POSTS
            "resources", "resource" -> InstanceType.RESOURCES
            "outings", "outing" -> InstanceType.OUTINGS
            "outings_message" -> InstanceType.OUTINGS_MESSAGE
            "outing_post" -> InstanceType.OUTING_POSTS
            "contributions", "contribution" -> InstanceType.CONTRIBUTIONS
            "solicitations", "solicitation" -> InstanceType.SOLICITATIONS
            "conversations", "conversation" -> InstanceType.CONVERSATIONS
            "smalltalk", "user_smalltalk " -> InstanceType.SMALLTALK
            "almost_matches" -> InstanceType.AlMOSTMATCH
            "partners" -> InstanceType.PARTNERS
            else -> InstanceType.NONE
        }
    }
}
