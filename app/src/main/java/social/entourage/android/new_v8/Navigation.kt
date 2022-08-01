package social.entourage.android.new_v8

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import social.entourage.android.R
import social.entourage.android.new_v8.groups.create.CreateGroupActivity
import social.entourage.android.new_v8.groups.details.feed.FeedActivity
import social.entourage.android.new_v8.groups.details.feed.FeedFragmentDirections
import social.entourage.android.new_v8.home.pedago.PedagoListActivity
import social.entourage.android.new_v8.models.Action
import social.entourage.android.new_v8.models.Params
import social.entourage.android.new_v8.models.Type
import social.entourage.android.new_v8.profile.ProfileActivity
import social.entourage.android.new_v8.user.UserProfileActivity
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.tools.view.WebViewFragment
import timber.log.Timber

class Navigation {
    companion object {
        fun navigate(
            context: Context,
            fragmentManager: FragmentManager,
            type: Type,
            action: Action,
            params: Params
        ) {
            when (type) {
                Type.CONVERSATION -> when (action) {
                    Action.SHOW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                    Action.INDEX -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                    Action.NEW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                }
                Type.NEIGHBORHOOD -> when (action) {
                    Action.SHOW -> context.startActivity(
                        Intent(context, FeedActivity::class.java).putExtra(
                            Const.GROUP_ID,
                            params.id
                        )
                    )
                    Action.INDEX -> {
                        ViewPagerDefaultPageController.shouldSelectDiscoverGroups = true
                        val bottomNavigationView =
                            (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                        bottomNavigationView.selectedItemId = R.id.navigation_groups
                    }
                    Action.NEW -> context.startActivity(
                        Intent(context, CreateGroupActivity::class.java)
                    )
                }
                Type.PROFILE -> when (action) {
                    Action.SHOW -> context.startActivity(
                        Intent(context, ProfileActivity::class.java).putExtra(
                            Const.GO_TO_EDIT_PROFILE,
                            true
                        )
                    )
                    Action.INDEX -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                    Action.NEW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                }
                Type.POI -> when (action) {
                    Action.SHOW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                    Action.INDEX -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                    Action.NEW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                }
                Type.USER -> when (action) {
                    Action.SHOW -> context.startActivity(
                        Intent(context, UserProfileActivity::class.java).putExtra(
                            Const.USER_ID,
                            params.id
                        )
                    )

                    Action.INDEX -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                    Action.NEW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                }
                Type.OUTING -> when (action) {
                    Action.SHOW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                    Action.INDEX -> {
                        val bottomNavigationView =
                            (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                        bottomNavigationView.selectedItemId = R.id.navigation_events
                    }
                    Action.NEW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                }
                Type.WEBVIEW -> when (action) {
                    Action.SHOW -> {
                        WebViewFragment.newInstance(params.url, 0)
                            .show(fragmentManager, WebViewFragment.TAG)
                    }
                    Action.INDEX -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                    Action.NEW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                }
                Type.CONTRIBUTION -> when (action) {
                    Action.SHOW -> {
                        val bottomNavigationView =
                            (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                        bottomNavigationView.selectedItemId = R.id.navigation_donations
                    }
                    Action.INDEX -> {
                        val bottomNavigationView =
                            (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                        bottomNavigationView.selectedItemId = R.id.navigation_donations
                    }
                    Action.NEW -> {
                        val bottomNavigationView =
                            (context as Activity).findViewById<BottomNavigationView>(R.id.nav_view)
                        bottomNavigationView.selectedItemId = R.id.navigation_donations
                    }
                }
                Type.ASK_FOR_HELP -> when (action) {
                    Action.SHOW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                    Action.INDEX -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                    Action.NEW -> Utils.showToast(
                        context,
                        context.getString(R.string.not_implemented)
                    )
                }
            }
        }
    }
}