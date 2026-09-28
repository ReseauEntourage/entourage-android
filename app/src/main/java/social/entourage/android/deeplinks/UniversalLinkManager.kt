package social.entourage.android.deeplinks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import kotlinx.coroutines.launch
import social.entourage.android.BuildConfig
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.actions.create.CreateActionActivity
import social.entourage.android.actions.detail.ActionDetailActivity
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Group
import social.entourage.android.badges.BadgeIntroBottomSheet
import social.entourage.android.badges.BadgesListActivity
import social.entourage.android.comment.CommentActivity
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.events.create.CreateEventActivity
import social.entourage.android.events.details.feed.EventFeedActivity
import social.entourage.android.events.details.feed.EventFeedFragment
import social.entourage.android.events.list.WelcomeEventsListActivity
import social.entourage.android.groups.details.feed.GroupFeedActivity
import social.entourage.android.groups.details.rules.GroupRulesActivity
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.home.NationalGroupsActivity
import social.entourage.android.home.pedago.PedagoDetailActivity
import social.entourage.android.home.pedago.PedagoListActivity
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.overrideTransitionCompat
import timber.log.Timber

/**
 * Gestionnaire des Deeplinks officiels de l'application Entourage (https://www.entourage.social/app/...)
 *
 * Deeplinks officiels pris en charge :
 * - Page d'accueil : `/app/` -> MainActivity
 * - Page groupes : `/app/groups` -> Onglet découvrir groupes
 * - Groupe national : `/app/groups/national` -> NationalGroupsActivity
 * - Page détail d'un groupe : `/app/neighborhoods/{id}` -> GroupFeedActivity
 * - Page événements : `/app/outings` -> Onglet découvrer événements
 * - Page détail d'un événement : `/app/outings/{id}` -> EventFeedActivity
 * - Ajout à l'agenda : `/app/outings/{id}/agenda` -> EventFeedActivity (ajout agenda)
 * - Funnel création événement : `/app/outings/new` -> CreateEventActivity
 * - Listes d'événements spécialisés :
 *   - `/app/outings/webinar` -> Événement de sensibilisation en ligne
 *   - `/app/outings/welcome` -> Événement de bienvenue en ligne
 *   - `/app/outings/first_steps` -> Rdv de bienvenue
 *   - `/app/outings/sensibilisation` -> Atelier de sensibilisation
 *   - `/app/outings/papotages` -> Papotages solidaires
 * - Contributions (Menu Entraide) :
 *   - `/app/contributions` -> Onglet contributions
 *   - `/app/contributions/new` -> Funnel création contribution
 * - Demandes (Menu Entraide) :
 *   - `/app/solicitations` -> Onglet demandes
 *   - `/app/solicitations/new` -> Funnel création demande
 * - Carte des lieux solidaires : `/app/map` -> GDSMainActivity
 * - Charte des événements : `/app/chart-event` -> GroupRulesActivity
 * - Conversations : `/app/conversation-message` -> Écran des conversations
 * - Badges :
 *   - `/app/badges/intro` -> Modal présentation des badges
 *   - `/app/badges` -> Liste des badges
 *   - `/app/badges/{badge_key}` -> Détail d'un badge spécifique (bienvenue, premier_contact, moteur_rencontres, fidele_papotages, voix_presente)
 * - Contenus pédagogiques (Ressources) :
 *   - `/app/resources` -> Liste des contenus
 *   - `/app/resources/{hash_id}` -> Détail d'un contenu pédagogique
 *
 *   Deeplinks internes
 *   https://<DEEP_LINKS_URL>/app/users/<id>
 *   https://<DEEP_LINKS_URL>/app/user/<id>
 *   https://<DEEP_LINKS_URL>/app/welcome-video
 *
 */
class UniversalLinkManager(val context: Context) : UniversalLinksPresenterCallback {

    val baseURL = BuildConfig.DEEP_LINKS_URL
    val presenter: UniversalLinkPresenter = UniversalLinkPresenter(this)

    fun handleUniversalLink(uri: Uri) {
        val pathSegments = uri.pathSegments
        if (uri.host == baseURL) {
            Timber.d("Universal link: $uri")
            when {
                pathSegments.contains("users") || pathSegments.contains("user") -> {
                    if (pathSegments.size > 2) {
                        val userId = pathSegments[2]
                        try {
                            val intent = Intent(context, ProfileFullActivity::class.java)
                            intent.putExtra(Const.USER_ID, userId.toInt())
                            context.startActivity(intent)
                            (context as Activity).overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
                        } catch (_: NumberFormatException) {
                            Timber.e("NumberFormatException")
                        }
                    }
                }
                // Page d'accueil : https://www.entourage.social/app/
                pathSegments.contains("app") && pathSegments.size == 1 -> {
                    (context as? MainActivity)?.goHome()
                }
                pathSegments.contains("welcome-video") || (pathSegments.contains("home") && pathSegments.contains("welcome-video")) -> {
                    (context as? MainActivity)?.goWelcomeVideo() ?: run {
                        val intent = Intent(context, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        intent.putExtra("goWelcomeVideo", true)
                        context.startActivity(intent)
                        (context as? Activity)?.overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }
                // Groupes nationaux : https://www.entourage.social/app/groups/national
                pathSegments.contains("national") -> {
                    val intent = Intent(context, NationalGroupsActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                // Groupes & Détail groupe : https://www.entourage.social/app/groups ou /app/neighborhoods/{id}
                //TODO HERE GO TO DETAIL MESSAGE GROUP
                pathSegments.contains("neighborhoods") && pathSegments.contains("chat_messages") && pathSegments.size > 3 -> {
                    val groupId = pathSegments[2]
                    val postId = pathSegments[3]
                }
                pathSegments.contains("neighborhoods") || pathSegments.contains("groups") -> {
                    if (pathSegments.size > 2) {
                        val neighborhoodId = pathSegments[2]
                        presenter.getGroup(neighborhoodId)
                    } else {
                        (context as? MainActivity)?.goGroup() ?: run {
                            val intent = Intent(context, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            intent.putExtra("goDiscoverGroup", true)
                            context.startActivity(intent)
                            (context as Activity).overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                    }
                }
                // Événements : https://www.entourage.social/app/outings...
                //TODO HERE GO TO DETAIL MESSAGE EVENT
//                pathSegments.contains("outings") && pathSegments.contains("chat_messages") && pathSegments.size > 3 -> {
//                    val eventId = pathSegments[2]
//                    val postId = pathSegments[3]
//                }
                pathSegments.contains("outings") -> {
                    handleOutings(pathSegments)
                }
                // Contributions : https://www.entourage.social/app
                // /contributions
                // /contributions/{id}
                // /contributions/new
                pathSegments.contains("contributions") -> {
                    if (pathSegments.contains("new")) {
                        val intent = Intent(context, CreateActionActivity::class.java)
                        intent.putExtra(Const.IS_ACTION_DEMAND, false)
                        context.startActivity(intent)
                    } else {
                        if (pathSegments.size > 2) {
                            val contribId = pathSegments[2]
                            presenter.getDetailAction(contribId,false)
                        } else {
                            (context as? MainActivity)?.goContrib() ?: run {
                                val intent = Intent(context, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                intent.putExtra("goContrib", true)
                                context.startActivity(intent)
                                (context as Activity).overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
                            }
                        }
                    }
                }
                // Demandes : https://www.entourage.social/app
                // /solicitations
                // /solicitations/{id]
                // /solicitations/new
                pathSegments.contains("solicitations") -> {
                    if (pathSegments.contains("new")) {
                        val intent = Intent(context, CreateActionActivity::class.java)
                        intent.putExtra(Const.IS_ACTION_DEMAND, true)
                        context.startActivity(intent)
                    } else {
                        if (pathSegments.size > 2) {
                            val soliciationId = pathSegments[2]
                            presenter.getDetailAction(soliciationId,true)
                        }else{
                            (context as? MainActivity)?.goDemand() ?: run {
                                val intent = Intent(context, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                intent.putExtra("goDemand", true)
                                context.startActivity(intent)
                                (context as Activity).overrideTransitionCompat(
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left
                                )
                            }
                        }
                    }
                }
                // Carte des lieux solidaires : https://www.entourage.social/app/map
                pathSegments.contains("map") -> {
                    val intent = Intent(context, GDSMainActivity::class.java)
                    context.startActivity(intent)
                    (context as Activity).overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                // Charte des événements : https://www.entourage.social/app/chart-event
                pathSegments.contains("chart-event") -> {
                    val intent = Intent(context, GroupRulesActivity::class.java).apply {
                        putExtra(Const.RULES_TYPE, Const.RULES_EVENT)
                    }
                    (context as Activity).startActivity(intent)
                }
                // Conversations : https://www.entourage.social/app/conversation-message
                pathSegments.contains("conversation-message") || pathSegments.contains("conversations") || pathSegments.contains("messages") -> {
                    if(pathSegments.size > 2){
                        val convId = pathSegments[2]
                        presenter.addUserToConversation(convId)
                    } else {
                        (context as? MainActivity)?.goConv()
                    }
                }

                // Badges : https://www.entourage.social/app/badges...
                pathSegments.contains("badges") && pathSegments.contains("intro") -> {
                    (context as? AppCompatActivity)?.let { activity ->
                        // Links can arrive via onNewIntent(), before onResume(): showing a
                        // DialogFragment while state is saved would throw IllegalStateException
                        activity.lifecycleScope.launch {
                            activity.withResumed {
                                BadgeIntroBottomSheet.newInstance()
                                    .show(activity.supportFragmentManager, "badge_intro")
                            }
                        }
                    }
                }
                pathSegments.contains("badges") && pathSegments.size > 2 -> {
                    val badgeId = pathSegments[2]
                    val intent = Intent(context, BadgesListActivity::class.java)
                    intent.putExtra(BadgesListActivity.EXTRA_OPEN_BADGE_KEY, badgeId)
                    context.startActivity(intent)
                    (context as? Activity)?.overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                pathSegments.contains("badges") -> {
                    val intent = Intent(context, BadgesListActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                // Contenus pédagogiques (Ressources) : https://www.entourage.social/app/resources...
                pathSegments.contains("resources") -> {
                    val intent = when {
                        pathSegments.size > 2 -> {
                            // Un ID de ressource est présent
                            val resourcesId = pathSegments[2]
                            PedagoDetailActivity.hashId = resourcesId
                            Intent(context, PedagoDetailActivity::class.java)
                        }
                        else -> {
                            // Aucun ID de ressource spécifié ; ouvrir la liste
                            Intent(context, PedagoListActivity::class.java)
                        }
                    }
                    context.startActivity(intent)
                    (context as? Activity)?.overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
        }
    }

    private fun handleOutings(pathSegments: List<String>) {
        if (pathSegments.contains("papotages")) {
            val intent = Intent(context, WelcomeEventsListActivity::class.java)
            intent.putExtra("TYPE", "papotages")
            context.startActivity(intent)
            (context as? Activity)?.overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
        } else if (pathSegments.contains("new")) {
            val intent = Intent(context, CreateEventActivity::class.java)
            context.startActivity(intent)
        } else if (pathSegments.contains("webinar")) {
            val intent = Intent(context, WelcomeEventsListActivity::class.java)
            intent.putExtra("TYPE", "webinar")
            context.startActivity(intent)
            (context as? Activity)?.overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
        } else if (pathSegments.contains("welcome")) {
            val intent = Intent(context, WelcomeEventsListActivity::class.java)
            intent.putExtra("TYPE", "welcome")
            context.startActivity(intent)
            (context as? Activity)?.overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
        } else if (pathSegments.contains("sensibilisation")) {
            val intent = Intent(context, WelcomeEventsListActivity::class.java)
            intent.putExtra("TYPE", "sensibilisation")
            context.startActivity(intent)
            (context as? Activity)?.overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
        } else if (pathSegments.contains("first_steps")) {
            val intent = Intent(context, WelcomeEventsListActivity::class.java)
            intent.putExtra("TYPE", "first_steps")
            context.startActivity(intent)
            (context as? Activity)?.overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
        } else if (pathSegments.size > 3) {
            val outingId = pathSegments[2]
            EventFeedFragment.shouldAddToAgenda = true
            presenter.getEvent(outingId)
        } else if (pathSegments.size > 2) {
            val outingId = pathSegments[2]
            presenter.getEvent(outingId)
        } else {
            (context as? MainActivity)?.goEvent() ?: run {
                val intent = Intent(context, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                intent.putExtra("goDiscoverEvent", true)
                context.startActivity(intent)
                (context as Activity).overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    override fun onRetrievedEvent(event: Events) {
        context.startActivity(
            Intent(
                context,
                EventFeedActivity::class.java
            ).apply {
                putExtra(Const.EVENT_ID, event.id)
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
        )
    }

    override fun onRetrievedGroup(group: Group?) {
        group?.id?.let { groupId ->
            context.startActivity(
                Intent(context, GroupFeedActivity::class.java).putExtra(
                    Const.GROUP_ID,
                    groupId
                )
            )
        } ?: run {
            Timber.e("Group or Group ID is null")
        }
    }

    override fun onRetrievedAction(action: Action, isContrib: Boolean) {
        val intent = Intent(context, ActionDetailActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(Const.ACTION_ID, action.id)
            .putExtra(Const.ACTION_TITLE, action.title)
            .putExtra(Const.IS_ACTION_DEMAND, !isContrib)
            .putExtra(Const.IS_ACTION_MINE, action.isMine())
        context.startActivity(intent)
        (context as Activity).overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onRetrievedDiscussion(discussion: Conversation) {
        val intent = Intent(context, DetailConversationActivity::class.java).apply {
            putExtras(Bundle().apply {
                discussion.id?.let { putInt(Const.ID, it) }
                discussion.user?.id?.let { putInt(Const.POST_AUTHOR_ID, it) }
                putBoolean(Const.SHOULD_OPEN_KEYBOARD, false)
                putString(Const.NAME, discussion.title)
                putBoolean(Const.IS_CONVERSATION_1TO1, true)
                putBoolean(Const.IS_MEMBER, true)
                putBoolean(Const.IS_CONVERSATION, true)
                putBoolean(Const.HAS_TO_SHOW_MESSAGE, discussion.hasToShowFirstMessage())
            })
        }


        when (context) {
            is MainActivity -> {
                // Si le context est MainActivity, on lance l'activité normalement
                context.startActivity(intent)
            }
            is DetailConversationActivity -> {
                // Si le context est DetailConversationActivity, on ajoute le flag et on lance une nouvelle activité
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                (context as Activity).overrideTransitionCompat(R.anim.slide_in_right, R.anim.slide_out_left)
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

    override fun onUserJoinedConversation(conversationId: String) {
        presenter.getDetailConversation(conversationId)

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
