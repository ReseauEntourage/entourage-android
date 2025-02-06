package social.entourage.android.notifications

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import social.entourage.android.MainActivity
import social.entourage.android.Navigation
import social.entourage.android.R
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.actions.detail.ActionDetailActivity
import social.entourage.android.user.partner.PartnerDetailActivity
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.api.model.ActionSummary
import social.entourage.android.api.model.HomeActionParams
import social.entourage.android.api.model.HomeType
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.welcome.WelcomeFiveActivity
import social.entourage.android.welcome.WelcomeFourActivity
import social.entourage.android.welcome.WelcomeOneActivity
import social.entourage.android.welcome.WelcomeThreeActivity
import social.entourage.android.welcome.WelcomeTwoActivity

/**
 * Created by Me on 26/09/2022.
 */
object NotificationActionManager {

    /**/
    fun presentAction(context:Context,supportFragmentManager: FragmentManager, instance:String, id:Int, postId:Int?, stage:String? = "", popup:String? = "" , notifContext:String? = "") {
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

        if(!stage.isNullOrEmpty()){

            if(stage.equals("h1")){
                val intent = Intent(context, WelcomeOneActivity::class.java)
                context.startActivity(intent)
            }
            if(stage.equals("j2")){
                val intent = Intent(context, WelcomeTwoActivity::class.java)
                context.startActivity(intent)
            }
            if(stage.equals("j5")){
                val intent = Intent(context, WelcomeThreeActivity::class.java)
                context.startActivity(intent)
            }
            if(stage.equals("j8")){
                val intent = Intent(context, WelcomeFourActivity::class.java)
                context.startActivity(intent)
            }
            if(stage.equals("j11")){
                val intent = Intent(context, WelcomeFiveActivity::class.java)
                context.startActivity(intent)
            }
        }
        Log.wtf("wtf", "instance: $instance")
        Log.wtf("wtf", "id: $id")
        when(getInstanceTypeFromName(instance)) {
            InstanceType.POIS -> showPoi(supportFragmentManager,id)
            InstanceType.USERS -> showUser(context,supportFragmentManager,id)
            InstanceType.NEIGHBORHOODS -> showNeighborhood(context,supportFragmentManager,id)
            InstanceType.RESOURCES -> showResource(context,supportFragmentManager,id)
            InstanceType.OUTINGS -> showOuting(context,supportFragmentManager,id)
            InstanceType.CONTRIBUTIONS -> showContribution(context,supportFragmentManager,id)
            InstanceType.SOLICITATIONS -> showSolicitation(context,supportFragmentManager,id)
            InstanceType.CONVERSATIONS -> showConversation(context,supportFragmentManager,id)
            InstanceType.PARTNERS -> showPartner(context,id)
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
            if(stage.equals("h1")){
                val intent = Intent(context, WelcomeOneActivity::class.java)
                context.startActivity(intent)
            }
            if(stage.equals("j2")){
                val intent = Intent(context, WelcomeTwoActivity::class.java)
                context.startActivity(intent)
            }
            if(stage.equals("j5")){
                val intent = Intent(context, WelcomeThreeActivity::class.java)
                context.startActivity(intent)
            }
            if(stage.equals("j8")){
                val intent = Intent(context, WelcomeFourActivity::class.java)
                context.startActivity(intent)
            }
            if(stage.equals("j11")){
                val intent = Intent(context, WelcomeFiveActivity::class.java)
                context.startActivity(intent)
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
            InstanceType.CONTRIBUTIONS -> return R.drawable.ic_new_placeholder_notif
            InstanceType.SOLICITATIONS -> return R.drawable.ic_new_placeholder_notif
            InstanceType.CONVERSATIONS -> return R.drawable.placeholder_user
            InstanceType.PARTNERS -> return R.drawable.ic_new_placeholder_notif
            InstanceType.NONE -> R.drawable.ic_new_placeholder_notif
            InstanceType.NEIGHBORHOODS_POSTS -> return R.drawable.placeholder_user
            InstanceType.OUTING_POSTS -> return R.drawable.placeholder_user
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

    private fun showUser(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        val params = HomeActionParams()
        ProfileFullActivity.userId = id.toString()
        ProfileFullActivity.isMe = false
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
        OUTING_POSTS,
        CONTRIBUTIONS,
        SOLICITATIONS,
        CONVERSATIONS,
        PARTNERS,
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
            "outing_post" -> InstanceType.OUTING_POSTS
            "contributions", "contribution" -> InstanceType.CONTRIBUTIONS
            "solicitations", "solicitation" -> InstanceType.SOLICITATIONS
            "conversations", "conversation" -> InstanceType.CONVERSATIONS
            "partners" -> InstanceType.PARTNERS
            else -> InstanceType.NONE
        }
    }
}