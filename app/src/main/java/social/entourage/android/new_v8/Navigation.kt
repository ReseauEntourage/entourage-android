package social.entourage.android.new_v8

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import social.entourage.android.R
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.new_v8.events.create.CreateEventActivity
import social.entourage.android.new_v8.groups.create.CreateGroupActivity
import social.entourage.android.new_v8.groups.details.feed.FeedActivity
import social.entourage.android.new_v8.home.pedago.PedagoDetailActivity
import social.entourage.android.new_v8.models.ActionSummary
import social.entourage.android.new_v8.models.Params
import social.entourage.android.new_v8.models.Type
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
            type: Type,
            action: ActionSummary,
            params: Params
        ) {
            when (type) {
                Type.CONVERSATION -> when (action) {
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
                    ActionSummary.NEW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                }
                Type.NEIGHBORHOOD -> when (action) {
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
                    ActionSummary.NEW -> context.startActivity(
                        Intent(context, CreateGroupActivity::class.java)
                    )
                }
                Type.PROFILE -> when (action) {
                    ActionSummary.SHOW -> context.startActivity(
                        Intent(context, ProfileActivity::class.java).putExtra(
                            Const.GO_TO_EDIT_PROFILE,
                            true
                        )
                    )
                    else -> {}
                }
                Type.POI -> when (action) {
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
                    ActionSummary.NEW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                }
                Type.USER -> when (action) {
                    ActionSummary.SHOW -> context.startActivity(
                        Intent(context, UserProfileActivity::class.java).putExtra(
                            Const.USER_ID,
                            params.id
                        )
                    )
                    else -> {}
                }
                Type.OUTING -> when (action) {
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
                    ActionSummary.NEW -> context.startActivity(
                        Intent(context, CreateEventActivity::class.java)
                    )
                }
                Type.WEBVIEW -> when (action) {
                    ActionSummary.SHOW -> {
                        WebViewFragment.newInstance(params.url, 0)
                            .show(fragmentManager, WebViewFragment.TAG)
                    }
                    else -> {}
                }
                Type.CONTRIBUTION -> when (action) {
                    //TODO a faire
                    ActionSummary.SHOW -> {
                        Utils.showToast(
                            context,
                            context.getString(R.string.not_implemented)
                        )
                    }
                    ActionSummary.INDEX -> {
                        val bottomNavigationView =
                            (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                        bottomNavigationView.selectedItemId = R.id.navigation_donations
                    }
                    ActionSummary.NEW -> {
                        Utils.showToast(
                            context,
                            context.getString(R.string.not_implemented)
                        )
                    }
                }

                Type.SOLICITATION -> when (action) {
                    //TODO a faire
                    ActionSummary.SHOW -> {
                        Utils.showToast(
                            context,
                            context.getString(R.string.not_implemented)
                        )
                    }
                    ActionSummary.INDEX -> {
                        val bottomNavigationView =
                            (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                        bottomNavigationView.selectedItemId = R.id.navigation_donations
                    }
                    ActionSummary.NEW -> {
                        Utils.showToast(
                            context,
                            context.getString(R.string.not_implemented)
                        )
                    }
                }
                Type.RESOURCE -> when (action) {
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
                    ActionSummary.NEW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                }
            }
        }
    }
}