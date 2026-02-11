package social.entourage.android.notifications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
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
import social.entourage.android.small_talks.SmallTalkListOtherBands
import social.entourage.android.tools.utils.Const
import social.entourage.android.user.partner.PartnerDetailActivity
import social.entourage.android.welcome.WelcomeFiveActivity
import social.entourage.android.welcome.WelcomeFourActivity
import social.entourage.android.welcome.WelcomeOneActivity
import social.entourage.android.welcome.WelcomeThreeActivity
import social.entourage.android.welcome.WelcomeTwoActivity
import social.entourage.android.home.BirthdayActivity

/**
 * Delegating wrapper for NotificationRouter.
 * Maintained for backward compatibility with In-App Notifications.
 */
object NotificationActionManager {

    fun presentAction(context: Context, supportFragmentManager: FragmentManager, instance: String, id: Int = 0, postId: Int? = null, stage: String? = "", popup: String? = "", notifContext: String? = "", tracking: String? = "") {
        val args = NotificationRouter.NotificationArguments(
            instance = instance,
            id = id,
            postId = postId,
            stage = stage,
            popup = popup,
            notifContext = notifContext,
            tracking = tracking
        )
        NotificationRouter.navigate(context, supportFragmentManager, args)
    }

    fun presentWelcomeAction(context: Context, stage: String? = "") {
        val args = NotificationRouter.NotificationArguments(
            stage = stage
        )
        // Pass null fragmentManager, assuming no fragment-based navigation for welcome actions
        NotificationRouter.navigate(context, null, args)
    }

    fun setPlaceHolder(instance: String?): Int {
        if (instance == null) return R.drawable.ic_new_placeholder_notif

        when (getInstanceTypeFromName(instance)) {
            InstanceType.POIS -> return R.drawable.ic_new_placeholder_notif
            InstanceType.USERS -> return R.drawable.placeholder_user
            InstanceType.NEIGHBORHOODS -> return R.drawable.placeholder_user
            InstanceType.RESOURCES -> return R.drawable.ic_new_placeholder_notif
            InstanceType.OUTINGS -> return R.drawable.placeholder_user
            InstanceType.OUTINGS_MESSAGE -> return R.drawable.placeholder_user
            InstanceType.CONTRIBUTIONS -> return R.drawable.ic_new_placeholder_notif
            InstanceType.SOLICITATIONS -> return R.drawable.ic_new_placeholder_notif
            InstanceType.CONVERSATIONS -> return R.drawable.placeholder_user
            InstanceType.PARTNERS -> return R.drawable.ic_new_placeholder_notif
            InstanceType.NONE -> return R.drawable.ic_new_placeholder_notif
            InstanceType.NEIGHBORHOODS_POSTS -> return R.drawable.placeholder_user
            InstanceType.OUTING_POSTS -> return R.drawable.placeholder_user
            InstanceType.SMALLTALK -> return R.drawable.placeholder_user
            InstanceType.AlMOSTMATCH -> return R.drawable.placeholder_user
        }
        return R.drawable.ic_new_placeholder_notif
    }

    enum class InstanceType {
        POIS,
        USERS,
        NEIGHBORHOODS,
        NEIGHBORHOODS_POSTS,
        RESOURCES,
        OUTINGS,
        OUTINGS_MESSAGE,
        OUTING_POSTS,
        CONTRIBUTIONS,
        SOLICITATIONS,
        CONVERSATIONS,
        SMALLTALK,
        AlMOSTMATCH,
        PARTNERS,
        NONE
    }

    fun getInstanceTypeFromName(instanceName: String): InstanceType {
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
