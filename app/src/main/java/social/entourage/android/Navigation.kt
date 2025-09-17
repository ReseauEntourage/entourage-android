package social.entourage.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavArgument
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import social.entourage.android.actions.create.CreateActionActivity
import social.entourage.android.actions.detail.ActionDetailActivity
import social.entourage.android.api.model.ActionSummary
import social.entourage.android.api.model.HomeActionParams
import social.entourage.android.api.model.HomeType
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.events.create.CreateEventActivity
import social.entourage.android.events.details.feed.EventCommentActivity
import social.entourage.android.groups.create.CreateGroupActivity
import social.entourage.android.groups.details.feed.GroupCommentActivity
import social.entourage.android.groups.details.feed.GroupFeedActivity
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.home.pedago.PedagoDetailActivity
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.view.WebViewFragment

object Navigation {
    fun navigate(
        context: Context,
        fragmentManager: FragmentManager,
        homeType: HomeType,
        action: ActionSummary,
        params: HomeActionParams
    ) {
        getNavigateIntent(context, fragmentManager, homeType, action, params)?.let { intent ->
            context.startActivity(intent)

        }
    }

    fun getNavigateIntent(
        context: Context,
        fragmentManager: FragmentManager,
        homeType: HomeType,
        action: ActionSummary,
        params: HomeActionParams
    ): Intent? {
        when (homeType) {
            HomeType.CONVERSATION -> when (action) {
                ActionSummary.SHOW -> {
                    return Intent(context, DetailConversationActivity::class.java)
                        .putExtras(
                            bundleOf(
                                Const.ID to params.id,
                                Const.SHOULD_OPEN_KEYBOARD to false,
                                Const.IS_CONVERSATION_1TO1 to true,
                                Const.IS_MEMBER to true,
                                Const.IS_CONVERSATION to true
                            )
                        )
                }
                ActionSummary.INDEX -> {
                    val bottomNavigationView =
                        (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                    bottomNavigationView.selectedItemId = R.id.navigation_messages
                }
                ActionSummary.CREATE -> Utils.showToast(
                    context,
                    context.getString(R.string.not_implemented)
                )
            }
            HomeType.NEIGHBORHOOD -> when (action) {
                ActionSummary.SHOW ->
                    return Intent(context, GroupFeedActivity::class.java).putExtra(
                        Const.GROUP_ID,
                        params.id
                    )
                ActionSummary.INDEX -> {
                    ViewPagerDefaultPageController.shouldSelectDiscoverGroups = true
                    val bottomNavigationView =
                        (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                    bottomNavigationView.selectedItemId = R.id.navigation_groups
                }
                ActionSummary.CREATE ->
                    return Intent(context, CreateGroupActivity::class.java)
            }
            HomeType.PROFILE -> when (action) {
                ActionSummary.SHOW ->
                    return Intent(context, ProfileFullActivity::class.java).putExtra(
                        Const.GO_TO_EDIT_PROFILE,
                        true
                    )
                else -> {}
            }
            HomeType.POI -> when (action) {
                ActionSummary.SHOW -> {
                    val poi = Poi()
                    poi.uuid = "${params.uuid}"
                    ReadPoiFragment.newInstance(poi, "")
                        .show(fragmentManager, ReadPoiFragment.TAG)
                }
                ActionSummary.INDEX ->
                    return Intent(context, GDSMainActivity::class.java)
                ActionSummary.CREATE -> Utils.showToast(
                    context,
                    context.getString(R.string.not_implemented)
                )
            }
            HomeType.USER -> when (action) {
                        ActionSummary.SHOW ->
                    return Intent(context, ProfileFullActivity::class.java).putExtra(
                        Const.USER_ID,
                        params.id
                    )
                else -> {}
            }
            HomeType.OUTING -> when (action) {
                ActionSummary.SHOW ->
                    return Intent(
                        context,
                        social.entourage.android.events.details.feed.EventFeedActivity::class.java
                    ).putExtra(
                        Const.EVENT_ID,
                        params.id
                    )
                ActionSummary.INDEX -> {
                    //Use to pass data to the fragment with navigation controller !
                    fragmentManager.primaryNavigationFragment?.findNavController()?.let {
                        it.graph.findNode(R.id.navigation_events)?.addArgument(
                            Const.IS_OUTING_DISCOVER,
                            NavArgument.Builder().setDefaultValue(true).build()
                        )
                    }

                    val bottomNavigationView =
                        (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                    bottomNavigationView.selectedItemId = R.id.navigation_events
                }
                ActionSummary.CREATE ->
                    return Intent(context, CreateEventActivity::class.java)
            }
            HomeType.WEBVIEW -> when (action) {
                ActionSummary.SHOW -> WebViewFragment.newInstance(params.url, 0, true)
                    .show(fragmentManager, WebViewFragment.TAG)
                else -> {}
            }
            HomeType.CONTRIBUTION -> when (action) {
                ActionSummary.SHOW -> {
                    return Intent(context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, params.id)
                        .putExtra(Const.IS_ACTION_DEMAND, false)
                        .putExtra(Const.IS_ACTION_MINE, false)
                }
                ActionSummary.INDEX -> {
                    val bottomNavigationView =
                        (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                    bottomNavigationView.selectedItemId = R.id.navigation_donations
                }
                ActionSummary.CREATE -> {
                    return Intent(context, CreateActionActivity::class.java)
                        .putExtra(Const.IS_ACTION_DEMAND, false)
                }
            }
            HomeType.SOLICITATION -> when (action) {
                ActionSummary.SHOW -> {
                    return Intent(context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, params.id)
                        .putExtra(Const.IS_ACTION_DEMAND, true)
                        .putExtra(Const.IS_ACTION_MINE, false)
                }
                ActionSummary.INDEX -> {
                    //Use to pass data to the fragment with navigation controller !
                    fragmentManager.primaryNavigationFragment?.findNavController()?.let {
                        it.graph.findNode(R.id.navigation_donations)?.addArgument(
                            Const.IS_ACTION_DEMAND,
                            NavArgument.Builder().setDefaultValue(true).build()
                        )
                    }

                    val bottomNavigationView =
                        (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                    bottomNavigationView.selectedItemId = R.id.navigation_donations
                }
                ActionSummary.CREATE -> {
                    return Intent(context, CreateActionActivity::class.java)
                        .putExtra(Const.IS_ACTION_DEMAND, true)
                }
            }
            HomeType.RESOURCE -> when (action) {
                ActionSummary.SHOW -> {
                    return Intent(context, PedagoDetailActivity::class.java)
                        .putExtra(Const.ID, params.id)
                        .putExtra(Const.IS_FROM_NOTIF, true)
                        //.putExtra(Const.HTML_CONTENT, "")
                }
                ActionSummary.INDEX -> Utils.showToast(
                    context,
                    context.getString(R.string.not_implemented)
                )
                ActionSummary.CREATE -> Utils.showToast(
                    context,
                    context.getString(R.string.not_implemented)
                )
            }
            HomeType.NEIGHBORHOOD_POST -> {
                val intent = Intent(context, GroupCommentActivity::class.java)
                intent.putExtra(Const.ID, params.id)
                intent.putExtra(Const.POST_ID, params.postId)
                intent.putExtra(Const.IS_FROM_NOTIF, true)
                return intent
            }

            HomeType.OUTING_POST -> {
                val intent = Intent(context, EventCommentActivity::class.java)
                intent.putExtra(Const.ID, params.id)
                intent.putExtra(Const.POST_ID, params.postId)
                intent.putExtra(Const.IS_FROM_NOTIF, true)
                return intent
            }
        }
        return null
    }
}