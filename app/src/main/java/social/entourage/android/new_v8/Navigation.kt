package social.entourage.android.new_v8

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import social.entourage.android.new_v8.groups.create.CreateGroupActivity
import social.entourage.android.new_v8.groups.details.feed.FeedActivity
import social.entourage.android.new_v8.models.Action
import social.entourage.android.new_v8.models.Params
import social.entourage.android.new_v8.models.Type
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.tools.view.WebViewFragment

class Navigation {
    companion object {
        public fun navigate(context: Context, type: Type, action: Action, params: Params) {
            when (type) {
                Type.CONVERSATION -> when (action) {
                    Action.SHOW -> TODO()
                    Action.INDEX -> TODO()
                    Action.NEW -> TODO()
                }
                Type.NEIGHBORHOOD -> when (action) {
                    Action.SHOW -> context.startActivity(
                        Intent(context, FeedActivity::class.java).putExtra(
                            Const.GROUP_ID,
                            params.id
                        )
                    )
                    Action.INDEX -> TODO()
                    Action.NEW -> context.startActivity(
                        Intent(context, CreateGroupActivity::class.java)
                    )
                }
                Type.PROFILE -> when (action) {
                    Action.SHOW -> TODO()
                    Action.INDEX -> TODO()
                    Action.NEW -> TODO()
                }
                Type.POI -> when (action) {
                    Action.SHOW -> TODO()
                    Action.INDEX -> TODO()
                    Action.NEW -> TODO()
                }
                Type.USER -> when (action) {
                    Action.SHOW -> TODO()
                    Action.INDEX -> TODO()
                    Action.NEW -> TODO()
                }
                Type.OUTING -> when (action) {
                    Action.SHOW -> TODO()
                    Action.INDEX -> TODO()
                    Action.NEW -> TODO()
                }
                Type.WEBVIEW -> when (action) {
                    Action.SHOW -> TODO()
                    Action.INDEX -> TODO()
                    Action.NEW -> {
                        WebViewFragment.newInstance(params.url, 0)
                            .show(fragmentManager, WebViewFragment.TAG)
                    }
                }
                Type.CONTRIBUTION -> when (action) {
                    Action.SHOW -> TODO()
                    Action.INDEX -> TODO()
                    Action.NEW -> TODO()
                }
                Type.ASK_FOR_HELP -> when (action) {
                    Action.SHOW -> TODO()
                    Action.INDEX -> TODO()
                    Action.NEW -> TODO()
                }
            }
        }
    }
}