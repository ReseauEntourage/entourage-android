package social.entourage.android.new_v8

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import social.entourage.android.R
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.new_v8.actions.create.CreateActionActivity
import social.entourage.android.new_v8.actions.detail.ActionDetailActivity
import social.entourage.android.new_v8.events.create.CreateEventActivity
import social.entourage.android.new_v8.groups.create.CreateGroupActivity
import social.entourage.android.new_v8.groups.details.feed.FeedActivity
import social.entourage.android.new_v8.home.pedago.PedagoDetailActivity
import social.entourage.android.new_v8.models.ActionSummary
import social.entourage.android.new_v8.models.HomeActionParams
import social.entourage.android.new_v8.models.HomeType
import social.entourage.android.new_v8.profile.ProfileActivity
import social.entourage.android.new_v8.user.UserProfileActivity
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.tools.view.WebViewFragment

class Navigation {
    companion object {
        fun navigate(
            context: Context,
            fragmentManager: FragmentManager,
            homeType: HomeType,
            action: ActionSummary,
            params: HomeActionParams
        ) {
            when (homeType) {
                HomeType.CONVERSATION -> when (action) {
                    //TODO a faire
                    ActionSummary.SHOW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
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
                    ActionSummary.SHOW -> context.startActivity(
                        Intent(context, FeedActivity::class.java).putExtra(
                            Const.GROUP_ID,
                            params.id
                        )
                    )
                    ActionSummary.INDEX -> {
                        ViewPagerDefaultPageController.shouldSelectDiscoverGroups = true
                        val bottomNavigationView =
                            (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                        bottomNavigationView.selectedItemId = R.id.navigation_groups
                    }
                    ActionSummary.CREATE -> context.startActivity(
                        Intent(context, CreateGroupActivity::class.java)
                    )
                }
                HomeType.PROFILE -> when (action) {
                    ActionSummary.SHOW -> context.startActivity(
                        Intent(context, ProfileActivity::class.java).putExtra(
                            Const.GO_TO_EDIT_PROFILE,
                            true
                        )
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
                    ActionSummary.INDEX -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                    ActionSummary.CREATE -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                }
                HomeType.USER -> when (action) {
                    ActionSummary.SHOW -> context.startActivity(
                        Intent(context, UserProfileActivity::class.java).putExtra(
                            Const.USER_ID,
                            params.id
                        )
                    )
                    else -> {}
                }
                HomeType.OUTING -> when (action) {
                    ActionSummary.SHOW -> context.startActivity(
                        Intent(context, social.entourage.android.new_v8.events.details.feed.FeedActivity::class.java).putExtra(
                            Const.EVENT_ID,
                            params.id
                        )
                    )
                    ActionSummary.INDEX -> {
                        val bottomNavigationView =
                            (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                        bottomNavigationView.selectedItemId = R.id.navigation_events
                    }
                    ActionSummary.CREATE -> context.startActivity(
                        Intent(context, CreateEventActivity::class.java)
                    )
                }
                HomeType.WEBVIEW -> when (action) {
                    ActionSummary.SHOW -> {

                        WebViewFragment.newInstance(params.url, 0, true)
                            .show(fragmentManager, WebViewFragment.TAG)
                    }
                    else -> {}
                }
                HomeType.CONTRIBUTION -> when (action) {
                    ActionSummary.SHOW -> {
                        context.startActivity(
                            Intent(context, ActionDetailActivity::class.java)
                                .putExtra(Const.ACTION_ID, params.id)
                                .putExtra(Const.IS_ACTION_DEMAND,false)
                                .putExtra(Const.IS_ACTION_MINE, false)
                        )
                    }
                    ActionSummary.INDEX -> {
                        val bottomNavigationView =
                            (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                        bottomNavigationView.selectedItemId = R.id.navigation_donations
                    }
                    ActionSummary.CREATE -> {
                        context.startActivity(
                            Intent(context, CreateActionActivity::class.java)
                                .putExtra(Const.IS_ACTION_DEMAND, false)
                        )
                    }
                }

                HomeType.SOLICITATION -> when (action) {
                    ActionSummary.SHOW -> {
                        context.startActivity(
                            Intent(context, ActionDetailActivity::class.java)
                                .putExtra(Const.ACTION_ID, params.id)
                                .putExtra(Const.IS_ACTION_DEMAND,true)
                                .putExtra(Const.IS_ACTION_MINE, false)
                        )
                    }
                    ActionSummary.INDEX -> {
                        val bottomNavigationView =
                            (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                        bottomNavigationView.selectedItemId = R.id.navigation_donations
                    }
                    ActionSummary.CREATE -> {
                        context.startActivity(
                            Intent(context, CreateActionActivity::class.java)
                                .putExtra(Const.IS_ACTION_DEMAND, true)
                        )
                    }
                }
                HomeType.RESOURCE -> when (action) {
                    ActionSummary.SHOW -> {
                        context.startActivity(
                            Intent(context, PedagoDetailActivity::class.java)
                                .putExtra(Const.ID, params.id)
                                .putExtra(Const.IS_FROM_NOTIF,true)
                                .putExtra(Const.HTML_CONTENT,"")
                        )
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
            }
        }
    }
}