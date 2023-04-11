package social.entourage.android.deeplinks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.actions.create.CreateActionActivity
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.groups.details.feed.FeedActivity
import social.entourage.android.home.pedago.PedagoListActivity
import social.entourage.android.tools.utils.Const
import timber.log.Timber

object UniversalLinkManager {
    /*private const val prodURL = "https://www.entourage.social"
    private const val stagingURL = "https://www.entourage.social"*/

    private const val prodURL = "app.entourage.social"
    private const val stagingURL = "entourage-webapp-preprod.herokuapp.com"

    fun handleUniversalLink(context: Context, uri: Uri) {
        val pathSegments = uri.pathSegments

        uri.queryParameterNames.forEach { name ->
            val value = uri.getQueryParameter(name)
            Timber.wtf("eho $name: $value")
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
                    val meId = EntourageApplication.get().me()?.id
                    (context as? Activity)?.startActivityForResult(
                        Intent(context, DetailConversationActivity::class.java)
                            .putExtras(
                                bundleOf(
                                    Const.ID to convId,
                                    Const.POST_AUTHOR_ID to meId,
                                    Const.SHOULD_OPEN_KEYBOARD to false,
                                    //CHECK NAME PARAM FOR SMART PLACEHOLDER
                                    Const.NAME to "",
                                    Const.IS_CONVERSATION_1TO1 to true,
                                    Const.IS_MEMBER to true,
                                    Const.IS_CONVERSATION to true,
                                    Const.HAS_TO_SHOW_MESSAGE to true
                                )
                            ),0
                    )
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
                        (context as? MainActivity)?.goEvent()

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
                    (context as? MainActivity)?.goConv()

                }
                pathSegments.contains("solicitations") -> {
                    if (pathSegments.contains("new")) {
                        val intent = Intent(context, CreateActionActivity::class.java)
                        intent.putExtra(Const.IS_ACTION_DEMAND, true)
                        (context as MainActivity).startActivityForResult(intent, 0)
                    } else {
                        if (pathSegments.size > 2) {
                            val soliciationId = pathSegments[2]
                            //HERE GO SOLICITTION
                        }
                    }
                }
                pathSegments.contains("contributions") -> {
                    if (pathSegments.contains("new")) {
                        val intent = Intent(context, CreateActionActivity::class.java)
                        intent.putExtra(Const.IS_ACTION_DEMAND, false)
                        (context as MainActivity).startActivityForResult(intent, 0)
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
                        (context as MainActivity).startActivityForResult(
                            Intent(
                                context,
                                PedagoListActivity::class.java
                            ), 0
                        )
                        }
                    }
                }
            }
        }
    }
