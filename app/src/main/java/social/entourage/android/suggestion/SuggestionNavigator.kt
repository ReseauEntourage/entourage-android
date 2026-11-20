package social.entourage.android.suggestion

import android.content.Context
import android.content.Intent
import android.net.Uri
import social.entourage.android.MainActivity
import social.entourage.android.actions.detail.ActionDetailActivity
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.events.details.feed.EventFeedActivity
import social.entourage.android.groups.details.feed.CreatePostGroupActivity
import social.entourage.android.groups.details.feed.GroupFeedActivity
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.home.pedago.PedagoDetailActivity
import social.entourage.android.home.pedago.PedagoListActivity
import social.entourage.android.small_talks.SmallTalkIntroActivity
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.view.WebViewFragment

/**
 * Ouvre l'écran ou le flux existant correspondant à une suggestion (corps de la card ou CTA).
 * Le CTA et le tap sur le corps mènent au même détail : l'action « agir » n'est loguée côté moteur
 * qu'à la complétion réelle, et la card est rafraîchie au retour sur la home.
 */
class SuggestionNavigator(
    private val context: Context,
    /** « Dire bonjour » : crée ou ouvre la conversation avec cet utilisateur. */
    private val onSayHello: (userId: Int) -> Unit,
    /** Fiche d'un lieu solidaire (feuille du guide) ; sans elle, on ouvre le guide. */
    private val onOpenPoi: ((Poi) -> Unit)? = null,
) {
    fun open(s: NextStepSuggestion) {
        val id = s.targetId
        when (s.type) {
            SuggestionType.ENTRAIDE -> id?.let {
                context.startActivity(
                    Intent(context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, it)
                        .putExtra(Const.ACTION_TITLE, s.title)
                        .putExtra(Const.IS_ACTION_DEMAND, !s.isContribution)
                        .putExtra(Const.IS_ACTION_MINE, false)
                )
            }
            SuggestionType.CONTENT -> id?.let {
                PedagoDetailActivity.setPedagoId(it)
                context.startActivity(
                    Intent(context, PedagoDetailActivity::class.java).putExtra(Const.ID, it)
                )
            }
            SuggestionType.EVENT -> id?.let {
                context.startActivity(
                    Intent(context, EventFeedActivity::class.java).putExtra(Const.EVENT_ID, it)
                )
            }
            // Poster un message : l'écran de saisie du groupe, comme le bouton « créer un message » du fil.
            SuggestionType.POST_MESSAGE -> id?.let {
                context.startActivity(
                    Intent(context, CreatePostGroupActivity::class.java)
                        .putExtra(Const.ID, it)
                        .putExtra(Const.IS_NATIONAL_GROUP, s.nationalGroup)
                )
            }
            SuggestionType.GROUP -> id?.let {
                context.startActivity(
                    Intent(context, GroupFeedActivity::class.java).putExtra(Const.GROUP_ID, it)
                )
            }
            // Mini-parcours : le premier contenu non lu (le premier si tous sont lus).
            SuggestionType.CONTENT_PATH -> s.firstContentToOpen()?.id?.let {
                PedagoDetailActivity.setPedagoId(it)
                context.startActivity(
                    Intent(context, PedagoDetailActivity::class.java).putExtra(Const.ID, it)
                )
            }
            // Bonnes ondes : le flux existant (introduction puis demande).
            SuggestionType.SMALL_TALK -> context.startActivity(Intent(context, SmallTalkIntroActivity::class.java))
            // « Devenir Animateur » : formulaire ouvert dans l'app (onglet personnalisé, comme les autres liens),
            // seulement une adresse https ; le navigateur du téléphone en repli.
            SuggestionType.AMBASSADOR -> s.link?.let { link ->
                val uri = Uri.parse(link)
                if (uri.scheme == "https" && !WebViewFragment.launchURL(context, link)) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            }
            SuggestionType.CONNECTION -> id?.let(onSayHello)
            // Lieu solidaire : sa fiche (comme sur iOS) ; le guide seulement si la fiche n'est pas disponible.
            SuggestionType.PLACE -> {
                val poi = s.poi
                val openPoi = onOpenPoi
                if (poi != null && openPoi != null) {
                    openPoi(poi)
                } else {
                    context.startActivity(
                        Intent(context, GDSMainActivity::class.java)
                            .putExtra(GDSMainActivity.EXTRA_AIR_CONDITIONED, false)
                    )
                }
            }
            null -> Unit
        }
    }

    fun open(access: ExplorerAccess) {
        val main = context as? MainActivity
        when (access.tab) {
            ExplorerTab.ENTRAIDE_DEMANDS -> main?.goDemand()
            ExplorerTab.EVENTS -> main?.goEvent()
            ExplorerTab.GROUPS -> main?.goGroup()
            ExplorerTab.CONTENT -> context.startActivity(Intent(context, PedagoListActivity::class.java))
            ExplorerTab.PLACES -> context.startActivity(
                Intent(context, GDSMainActivity::class.java)
                    .putExtra(GDSMainActivity.EXTRA_AIR_CONDITIONED, false)
            )
        }
    }
}
