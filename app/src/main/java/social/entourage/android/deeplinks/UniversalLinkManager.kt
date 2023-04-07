package social.entourage.android.deeplinks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavController
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.groups.details.feed.FeedActivity
import social.entourage.android.tools.utils.Const

object UniversalLinkManager {
private const val prodURL = "app.entourage.social"
private const val stagingURL = "entourage-webapp-preprod.herokuapp.com"

fun handleUniversalLink(context: Context, uri: Uri) {
    val pathSegments = uri.pathSegments

    uri.queryParameterNames.forEach { name ->
        val value = uri.getQueryParameter(name)
        println("eho $name: $value")
    }

    if (uri.host == stagingURL || uri.host == prodURL) {
        when {
            pathSegments.contains("outings") && pathSegments.contains("chat_messages") -> {
                if (pathSegments.size > 3) {
                    val eventId = pathSegments[2]
                    val postId = pathSegments[3]
                    //HERE GO TO DETAIL MESSAGE EVENT
                }
            }
            pathSegments.contains("neighborhoods") && pathSegments.contains("chat_messages") -> {
                if (pathSegments.size > 3) {
                    val groupId = pathSegments[2]
                    val postId = pathSegments[3]
                    //HERE GO TO DETAIL MESSAGE GROUP
                }
            }
            pathSegments.contains("conversations") && pathSegments.contains("chat_messages") -> {
                val convId = pathSegments[2]
                //GO MESSAGE
            }
            pathSegments.contains("outings") -> {
                if (pathSegments.size > 2) {
                    val outingId = pathSegments[2]
                    (context as? Activity)?.startActivityForResult(
                        Intent(
                            context,
                            social.entourage.android.events.details.feed.FeedActivity::class.java
                        ).putExtra(
                            Const.EVENT_ID,
                            outingId
                        ), 0
                    )
                } else {
                    val navController: NavController =
                        androidx.navigation.Navigation.findNavController((context as MainActivity), social.entourage.android.R.id.nav_host_fragment_new_activity_main)
                    // Naviguer vers le EventsFragment avec l'argument isDiscover à true
                    val bundle = Bundle().apply {
                        putBoolean("isDiscover", true)
                    }
                    navController.navigate(R.id.events_fragment, bundle)
                }
            }
            pathSegments.contains("neighborhoods") || pathSegments.contains("groups") -> {
                if (pathSegments.size > 2) {
                    val neighborhoodId = pathSegments[2]
                    (context as? Activity)?.startActivityForResult(
                        Intent(context, FeedActivity::class.java).putExtra(
                            Const.GROUP_ID,
                            neighborhoodId
                            ), 0
                    )
                }
            }
            pathSegments.contains("conversations") || pathSegments.contains("messages") -> {
                val conversationId = uri.getQueryParameter("conversation_id") ?: "AAAAA"
                //HereCOnversationList
            }
            pathSegments.contains("solicitations") -> {
                if (pathSegments.contains("new")) {
                    //HERE CREATE SOLICIATION
                } else {
                    if (pathSegments.size > 2) {
                        val soliciationId = pathSegments[2]
                    }
                }
            }
            pathSegments.contains("contributions") -> {
                if (pathSegments.contains("new")) {
                    //CREATE CONTRIB
                } else {
                    if (pathSegments.size > 2) {
                        val soliciationId = pathSegments[2]
                       // GO CONTRIB
                    }
                }
            }
            pathSegments.contains("resources") -> {
                if (pathSegments.size > 2) {
                    val soliciationId = pathSegments[2]
                        //GO RESSOURCE
                    }
                }
            }
        }
    }
}
