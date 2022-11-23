package social.entourage.android.new_v8

import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.new_contrib_item.view.*
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.new_v8.actions.detail.ActionDetailActivity
import social.entourage.android.new_v8.association.PartnerDetailActivity
import social.entourage.android.new_v8.discussions.DetailConversationActivity
import social.entourage.android.new_v8.models.ActionSummary
import social.entourage.android.new_v8.models.HomeActionParams
import social.entourage.android.new_v8.models.HomeType
import social.entourage.android.new_v8.utils.Const

/**
 * Created by Me on 26/09/2022.
 */
class PushNotificationLinkManager {

    fun presentAction(context:Context,supportFragmentManager: FragmentManager, instance:String?,id:Int?) {
        if (instance == null || id == null) return

        when(InstanceTypeNotif(instance).getInstanceTypeFromName()) {
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
        }
    }

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
}

class InstanceTypeNotif(val instanceName:String) {
    fun getInstanceTypeFromName() : InstanceType {
        when(instanceName) {
            "pois" -> return  InstanceType.POIS
            "users","user" -> return  InstanceType.USERS
            "neighborhoods","neighborhood" -> return  InstanceType.NEIGHBORHOODS
            "resources" -> return  InstanceType.RESOURCES
            "outings","outing" -> return  InstanceType.OUTINGS
            "contributions","contribution" -> return  InstanceType.CONTRIBUTIONS
            "solicitations","solicitation" -> return  InstanceType.SOLICITATIONS
            "conversations","conversation" -> return  InstanceType.CONVERSATIONS
            "partners" -> return  InstanceType.PARTNERS
            else -> return  InstanceType.NONE
        }
    }
}

enum class InstanceType {
    POIS,
    USERS,
    NEIGHBORHOODS,
    RESOURCES,
    OUTINGS,
    CONTRIBUTIONS,
    SOLICITATIONS,
    CONVERSATIONS,
    PARTNERS,
    NONE
}