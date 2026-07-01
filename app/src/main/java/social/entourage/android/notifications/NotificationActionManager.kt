package social.entourage.android.notifications

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import social.entourage.android.small_talks.SmallTalkListOtherBands
import social.entourage.android.tools.utils.Const
import social.entourage.android.user.partner.PartnerDetailActivity
import social.entourage.android.home.BirthdayActivity
import timber.log.Timber

/**
 * Created by Me on 26/09/2022.
 */
object NotificationActionManager {

    /**/
    fun presentAction(context:Context,supportFragmentManager: FragmentManager, instance:String, id:Int = 0, postId:Int?, stage:String? = "", popup:String? = "" , notifContext:String? = "", tracking:String? = ""){

        if(popup.equals("outing_on_day_before")){
            if(context is MainActivity){
                (context as MainActivity).ifEventLastDay(id)
                return
            }
            else{
                MainActivity.shouldLaunchEventPopUp = id
                (context as Activity).finish()
                return
            }
        }
        if(notifContext.equals("outing_on_day_before")){
            if(context is MainActivity){
                (context as MainActivity).ifEventLastDay(id)
                return

            }else{
                MainActivity.shouldLaunchEventPopUp = id
                (context as Activity).finish()
                return
            }
        }

        Log.wtf("wtf", "instance from NotificationActionManager: $instance")
        Log.wtf("wtf", "tracking: from NotificationActionManager$tracking")
        Log.wtf("wtf", "id: from NotificationActionManager$id")


        // Cas spécifiques : si c'est un outing ET que le tracking correspond à une conversation
        val validTracking = listOf(
            "public_chat_message_on_create",
            "post_on_create_to_outing",
            "post_on_create",
            "comment_on_create_to_outing",
            "comment_on_create",
            "chat_message_on_mention",
            "reaction_on_create",
            "survey_response_on_create"
        )

        if ((instance == "outings" || instance == "outing") && (notifContext in validTracking || tracking in validTracking)) {
            Log.wtf("wtf", "➡️ Redirection discussion/outing via notifContext = $notifContext")
            context.startActivity(
                Intent(context, DetailConversationActivity::class.java).apply {
                    putExtras(
                        bundleOf(
                            Const.ID to id,
                            Const.SHOULD_OPEN_KEYBOARD to false,
                            Const.IS_CONVERSATION_1TO1 to true,
                            Const.IS_MEMBER to true,
                            Const.IS_CONVERSATION to true,
                            Const.HAS_TO_SHOW_MESSAGE to true
                        )
                    )
                }
            )
            return
        }

        when(getInstanceTypeFromName(instance)) {
            InstanceType.POIS -> showPoi(supportFragmentManager,id)
            InstanceType.USERS -> showUser(context,supportFragmentManager,id)
            InstanceType.NEIGHBORHOODS -> showNeighborhood(context,supportFragmentManager,id)
            InstanceType.RESOURCES -> showResource(context,supportFragmentManager,id)
            InstanceType.OUTINGS -> showOuting(context,supportFragmentManager,id)
            InstanceType.OUTINGS_MESSAGE -> showConversation(context,supportFragmentManager,id)
            InstanceType.CONTRIBUTIONS -> showContribution(context,supportFragmentManager,id)
            InstanceType.SOLICITATIONS -> showSolicitation(context,supportFragmentManager,id)
            InstanceType.CONVERSATIONS -> showConversation(context,supportFragmentManager,id)
            InstanceType.SMALLTALK -> showSmallTalk(context,supportFragmentManager,id)
            InstanceType.AlMOSTMATCH -> showAlmostMatch(context,supportFragmentManager,id)
            InstanceType.PARTNERS -> showPartner(context,id)
            InstanceType.BADGES -> return
            InstanceType.NONE -> return

            else -> {
                if(getInstanceTypeFromName(instance) == InstanceType.OUTING_POSTS){
                    if (postId != null) {
                        showEventPost(context,supportFragmentManager,id,postId)
                    }
                } else if(getInstanceTypeFromName(instance) == InstanceType.NEIGHBORHOODS_POSTS){
                    if (postId != null) {
                        showGroupPost(context,supportFragmentManager,id,postId)
                    }
                }
            }
        }
    }

    fun presentWelcomeAction(context: Context, stage:String? = ""){
        if(!stage.isNullOrEmpty()){
            if (stage == "birthday") {
                if (context is MainActivity) {
                    (context as MainActivity).goHome()
                    context.startActivity(Intent(context, BirthdayActivity::class.java))
                } else {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("goBirthday", true)
                    context.startActivity(intent)
                }
                return
            }
        }

    }


    fun setPlaceHolder(instance:String?):Int {
        if (instance == null ) return R.drawable.ic_new_placeholder_notif

        when(getInstanceTypeFromName(instance)) {
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
            InstanceType.BADGES -> return R.drawable.ic_new_placeholder_notif
            InstanceType.NONE -> R.drawable.ic_new_placeholder_notif
            InstanceType.NEIGHBORHOODS_POSTS -> return R.drawable.placeholder_user
            InstanceType.OUTING_POSTS -> return R.drawable.placeholder_user
            InstanceType.SMALLTALK -> return R.drawable.placeholder_user
            InstanceType.AlMOSTMATCH -> return R.drawable.placeholder_user
        }
        return R.drawable.ic_new_placeholder_notif
    }
            /*InstanceType.NEIGHBORHOODS_POST -> showEventPost(context,supportFragmentManager, postId)
            InstanceType.OUTINGS_POST -> showGroupPost(context,supportFragmentManager, postId)*/

    private fun showContribution(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        context.startActivity(
            Intent(context, ActionDetailActivity::class.java)
                .putExtra(Const.ACTION_ID, id)
                .putExtra(Const.IS_ACTION_DEMAND,false)
        )
    }
    private fun showSolicitation(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        context.startActivity(
            Intent(context, ActionDetailActivity::class.java)
                .putExtra(Const.ACTION_ID, id)
                .putExtra(Const.IS_ACTION_DEMAND,true)
        )
    }
    private fun showConversation(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        DetailConversationActivity.isSmallTalkMode = false
        context.startActivity(
            Intent(context, DetailConversationActivity::class.java)
                .putExtras(
                    bundleOf(
                        Const.ID to id,
                        Const.SHOULD_OPEN_KEYBOARD to false,
                        Const.IS_CONVERSATION_1TO1 to true,
                        Const.IS_MEMBER to true,
                        Const.IS_CONVERSATION to true
                    )
                )
        )
    }
    private fun showSmallTalk(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        DetailConversationActivity.isSmallTalkMode = true
        DetailConversationActivity.smallTalkId = id.toString()
        context.startActivity(
            Intent(context, DetailConversationActivity::class.java)
                .putExtras(
                    bundleOf(
                        Const.ID to id,
                        Const.SHOULD_OPEN_KEYBOARD to false,
                        Const.IS_CONVERSATION_1TO1 to true,
                        Const.IS_MEMBER to true,
                        Const.IS_CONVERSATION to true
                    )
                )
        )
    }
    private fun showAlmostMatch(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        context.startActivity(
            Intent(context, SmallTalkListOtherBands::class.java)
        )
    }

    private fun showUser(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        val params = HomeActionParams()
        params.id = id
        Navigation.navigate(context,supportFragmentManager,
            HomeType.USER,
            ActionSummary.SHOW, params)
    }

    private fun showPartner(context:Context, id: Int) {
        context.startActivity(
            Intent(context, PartnerDetailActivity::class.java)
                .putExtra(Const.PARTNER_ID, id)
                .putExtra(Const.IS_FROM_NOTIF,true)
        )
    }

    private fun showPoi(fragmentManager: FragmentManager, id: Int) {
        val poi = Poi()
        poi.uuid = "$id"
        ReadPoiFragment.newInstance(poi, "")
            .show(fragmentManager, ReadPoiFragment.TAG)
    }

    private fun showOuting(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        val params = HomeActionParams()
        params.id = id
        Navigation.navigate(context,supportFragmentManager,
            HomeType.OUTING,
            ActionSummary.SHOW, params)
    }

    private fun showNeighborhood(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        val params = HomeActionParams()
        params.id = id
        Navigation.navigate(context,supportFragmentManager,
            HomeType.NEIGHBORHOOD,
            ActionSummary.SHOW, params)
    }

    private fun showResource(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        val params = HomeActionParams()
        params.id = id
        Navigation.navigate(context,supportFragmentManager,
            HomeType.RESOURCE,
            ActionSummary.SHOW, params)
    }

    private fun showEventPost(context:Context,supportFragmentManager: FragmentManager, instanceId: Int , postID:Int) {
        val params = HomeActionParams()
        params.id = instanceId
        params.postId = postID
        Navigation.navigate(context,supportFragmentManager,
            HomeType.OUTING_POST,
            ActionSummary.SHOW, params)
    }

    private fun showGroupPost(context:Context,supportFragmentManager: FragmentManager, instanceId: Int , postID:Int) {

        val params = HomeActionParams()
        params.id = instanceId
        params.postId = postID
        Navigation.navigate(context,supportFragmentManager,
            HomeType.NEIGHBORHOOD_POST,
            ActionSummary.SHOW, params)
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
        BADGES,
        NONE
    }

    fun getInstanceTypeFromName(instanceName:String) : InstanceType {
        return when (instanceName) {
            "pois","poi" -> InstanceType.POIS
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
            "badges", "badge", "user_badge" -> InstanceType.BADGES
            else -> InstanceType.NONE
        }
    }
}