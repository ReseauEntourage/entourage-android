package social.entourage.android.deeplinks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.os.bundleOf
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.actions.create.CreateActionActivity
import social.entourage.android.actions.detail.ActionDetailActivity
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Group
import social.entourage.android.comment.CommentActivity
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.events.details.feed.EventFeedFragment
import social.entourage.android.groups.details.feed.GroupFeedActivity
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.home.pedago.PedagoDetailActivity
import social.entourage.android.home.pedago.PedagoListActivity
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.tools.utils.Const
import timber.log.Timber

class UniversalLinkManager(val context:Context):UniversalLinksPresenterCallback {
    /*private const val prodURL = "https://www.entourage.social"
    private const val stagingURL = "https://www.entourage.social"*/

    val prodURL = "www.entourage.social"
    val stagingURL = "preprod.entourage.social"
    private var conversationId: String = ""
    val presenter:UniversalLinkPresenter = UniversalLinkPresenter(this)


    fun handleUniversalLink(uri: Uri) {
        val pathSegments = uri.pathSegments
        uri.queryParameterNames.forEach { name ->
            val value = uri.getQueryParameter(name)
        }
        for (segment in pathSegments) {
            Timber.d("Segment: $segment")
        }
        if (uri.host == stagingURL || uri.host == prodURL) {
            Timber.d("Universal link: $uri")
            when {
                pathSegments.contains("users") -> {
                    if (pathSegments.size > 2) {
                        val userId = pathSegments[2]
                        ProfileFullActivity.isMe = false
                        ProfileFullActivity.userId = userId
                        val intent = Intent(context, ProfileFullActivity::class.java)
                        context.startActivity(intent)
                        (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                    }
                }
                pathSegments.contains("user") -> {
                    if (pathSegments.size > 2) {
                        val userId = pathSegments[2]
                        ProfileFullActivity.isMe = false
                        ProfileFullActivity.userId = userId
                        val intent = Intent(context, ProfileFullActivity::class.java)
                        context.startActivity(intent)
                        (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                    }
                }

                pathSegments.contains("charte-ethique-entourage") ->{
                    val chartIntent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.disclaimer_link_public)))
                    context.startActivity(chartIntent)
                }
                pathSegments.contains("app") && pathSegments.size == 1 -> {
                    (context as? MainActivity)?.goHome()
                }
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
                pathSegments.contains("conversations") || pathSegments.contains("messages") -> {
                    if(pathSegments.size > 2){
                        val convId = pathSegments[2]
                        this.conversationId = convId
                        presenter.addUserToConversation(convId)
                    }else{
                        (context as? MainActivity)?.goConv()
                    }
                }
/*                pathSegments.contains("conversations") && pathSegments.contains("chat_messages") -> {
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
                }*/
                pathSegments.contains("outings") -> {
                    if (pathSegments.size > 3) {
                        val outingId = pathSegments[2]
                        EventFeedFragment.shouldAddToAgenda = true
                        presenter.getEvent(outingId)
                    }
                    else if (pathSegments.size > 2) {
                        val outingId = pathSegments[2]
                        presenter.getEvent(outingId)

                    } else {
                        val intent = Intent(context, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        //intent.putExtra("fromWelcomeActivityThreeEvent", true)
                        intent.putExtra("goDiscoverEvent", true)
                        context.startActivity(intent)
                        (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                    }
                }
                pathSegments.contains("neighborhoods") || pathSegments.contains("groups") -> {
                    if (pathSegments.size > 2) {
                        val neighborhoodId = pathSegments[2]
                        presenter.getGroup(neighborhoodId)
                    }else{
                        val intent = Intent(context, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        intent.putExtra("fromWelcomeActivity", true)
                        intent.putExtra("goDiscoverGroup", true)
                        context.startActivity(intent)
                        (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }

                pathSegments.contains("solicitations") -> {
                    if (pathSegments.contains("new")) {
                        val intent = Intent(context, CreateActionActivity::class.java)
                        intent.putExtra(Const.IS_ACTION_DEMAND, true)
                        (context as? MainActivity)?.startActivityForResult(intent, 0)
                    } else {
                        if (pathSegments.size > 2) {
                            val soliciationId = pathSegments[2]
                            presenter.getDetailAction(soliciationId,true)
                        }else{
                            val intent = Intent(context, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            intent.putExtra("goDemand", true)
                            context.startActivity(intent)
                            (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                    }
                }
                pathSegments.contains("contributions") -> {
                    if (pathSegments.contains("new")) {
                        val intent = Intent(context, CreateActionActivity::class.java)
                        intent.putExtra(Const.IS_ACTION_DEMAND, false)
                        (context as? MainActivity)?.startActivityForResult(intent, 0)
                    } else {
                        if (pathSegments.size > 2) {
                            val contribId = pathSegments[2]
                            presenter.getDetailAction(contribId,false)
                        }else{
                            val intent = Intent(context, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            intent.putExtra("goContrib", true)
                            context.startActivity(intent)
                            (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                    }
                }
                pathSegments.contains("map") -> {
                    val intent = Intent(context, GDSMainActivity::class.java)
                    context.startActivity(intent)
                    (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                pathSegments.contains("resources") -> {
                    val intent = when {
                        pathSegments.size > 2 -> {
                            // Un ID de ressource est présent
                            val resourcesId = pathSegments[2]
                            PedagoDetailActivity.hashId = resourcesId
                            Intent(context, PedagoDetailActivity::class.java).apply {
                                // Assumons que PedagoDetailActivity attend un extra avec l'ID de la ressource
                            }
                        }
                        else -> {
                            // Aucun ID de ressource spécifié; ouvrir la liste
                            Intent(context, PedagoListActivity::class.java)
                        }
                    }


                    when (context) {
                        is MainActivity -> {
                            context.startActivity(intent)
                            (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                        is DetailConversationActivity -> {
                            context.startActivity(intent)
                            (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                        is CommentActivity -> {
                            context.startActivity(intent)
                            (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                        else -> {
                            // Logique par défaut ou gestion d'autres contextes
                            context.startActivity(intent)
                            (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

                        }
                    }
                }
            }
            }
        }

    override fun onRetrievedEvent(event: Events) {
        (context as? Activity)?.startActivityForResult(
            Intent(
                context,
                social.entourage.android.events.details.feed.EventFeedActivity::class.java
            ).apply {
                putExtra(Const.EVENT_ID, event.id)
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }, 0
        )
    }

    override fun onRetrievedGroup(group: Group) {
        (context as? Activity)?.startActivityForResult(
            Intent(context, GroupFeedActivity::class.java).putExtra(
                Const.GROUP_ID,
                group.id
            ), 0
        )
    }

    override fun onRetrievedAction(action: Action,isContrib:Boolean) {
        if(isContrib){
            val intent = Intent(context, ActionDetailActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Const.ACTION_ID, action.id)
                .putExtra(Const.ACTION_TITLE,action.title)
                .putExtra(Const.IS_ACTION_DEMAND,false)
                .putExtra(Const.IS_ACTION_MINE, action.isMine())
            context.startActivity(intent)
            (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }else{
            val intent = Intent(context, ActionDetailActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Const.ACTION_ID, action.id)
                .putExtra(Const.ACTION_TITLE,action.title)
                .putExtra(Const.IS_ACTION_DEMAND,true)
                .putExtra(Const.IS_ACTION_MINE, action.isMine())
            context.startActivity(intent)
            (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onRetrievedDiscussion(discussion: Conversation) {
        val intent = Intent(context, DetailConversationActivity::class.java).apply {
            putExtras(bundleOf(
                Const.ID to discussion.id,
                Const.POST_AUTHOR_ID to discussion.user?.id,
                Const.SHOULD_OPEN_KEYBOARD to false,
                Const.NAME to discussion.title,
                Const.IS_CONVERSATION_1TO1 to true,
                Const.IS_MEMBER to true,
                Const.IS_CONVERSATION to true,
                Const.HAS_TO_SHOW_MESSAGE to discussion.hasToShowFirstMessage()
            ))
        }


        when (context) {
            is MainActivity -> {
                // Si le context est MainActivity, on lance l'activité normalement
                context.startActivityForResult(intent, 0)
            }
            is DetailConversationActivity -> {
                // Si le context est DetailConversationActivity, on ajoute le flag et on lance une nouvelle activité
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                (context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                context.finish() // Fermer l'activité actuelle pour éviter l'empilement des activités
            }
            is CommentActivity -> {
                // Si le context est DetailConversationActivity, on ajoute le flag et on lance une nouvelle activité
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                context.finish() // Fermer l'activité actuelle pour éviter l'empilement des activités
            }
            else -> {
                // Gérer d'autres types de contextes si nécessaire
            }
        }

    }

    override fun onUserJoinedConversation() {
        presenter.getDetailConversation(this.conversationId.toString())

    }

    override fun onErrorRetrievedDiscussion() {
        (context as? Activity)?.finish()
    }

    override fun onErrorRetrievedGroup() {
        (context as? MainActivity)?.DisplayErrorFromAppLinks(1)
    }

    override fun onErrorRetrievedEvent() {
        (context as? MainActivity)?.DisplayErrorFromAppLinks(0)
    }

    override fun onErrorRetrievedAction() {
        (context as? MainActivity)?.DisplayErrorFromAppLinks(2)
    }

    override fun onUserErrorJoinedConversation() {
        Toast.makeText(context, "Erreur : Vous ne pouvez pas rejoindre cette conversation pour le moment", Toast.LENGTH_SHORT).show()
    }
}
