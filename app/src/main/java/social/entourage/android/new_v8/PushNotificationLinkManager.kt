package social.entourage.android.new_v8

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import social.entourage.android.R
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.new_v8.association.PartnerDetailActivity
import social.entourage.android.new_v8.groups.details.feed.FeedActivity
import social.entourage.android.new_v8.home.pedago.PedagoDetailActivity
import social.entourage.android.new_v8.models.Action
import social.entourage.android.new_v8.models.Params
import social.entourage.android.new_v8.models.Type
import social.entourage.android.new_v8.user.UserProfileActivity
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils

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
        //TODO à faire
    }
    private fun showSolicitation(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        //TODO à faire
    }
    private fun showConversation(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        //TODO à faire
    }

    private fun showUser(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        val params = Params()
        params.id = id
        Navigation.navigate(context,supportFragmentManager,
            Type.USER,
            Action.SHOW, params)
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
        val params = Params()
        params.id = id
        Navigation.navigate(context,supportFragmentManager,
            Type.OUTING,
            Action.SHOW, params)
    }

    private fun showNeighborhood(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        val params = Params()
        params.id = id
        Navigation.navigate(context,supportFragmentManager,
            Type.NEIGHBORHOOD,
            Action.SHOW, params)
    }

    private fun showResource(context:Context,supportFragmentManager: FragmentManager, id: Int) {
        val params = Params()
        params.id = id
        Navigation.navigate(context,supportFragmentManager,
            Type.RESOURCE,
            Action.SHOW, params)
    }
}

class InstanceTypeNotif(val instanceName:String) {
    fun getInstanceTypeFromName() : InstanceType {
        when(instanceName) {
            "pois" -> return  InstanceType.POIS
            "users" -> return  InstanceType.USERS
            "neighborhoods" -> return  InstanceType.NEIGHBORHOODS
            "resources" -> return  InstanceType.RESOURCES
            "outings" -> return  InstanceType.OUTINGS
            "contributions" -> return  InstanceType.CONTRIBUTIONS
            "solicitations" -> return  InstanceType.SOLICITATIONS
            "conversations" -> return  InstanceType.CONVERSATIONS
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