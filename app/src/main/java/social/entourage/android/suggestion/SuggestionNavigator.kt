package social.entourage.android.suggestion

import android.content.Context
import android.content.Intent
import social.entourage.android.MainActivity
import social.entourage.android.actions.detail.ActionDetailActivity
import social.entourage.android.events.details.feed.EventFeedActivity
import social.entourage.android.groups.details.feed.GroupFeedActivity
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.home.pedago.PedagoDetailActivity
import social.entourage.android.home.pedago.PedagoListActivity
import social.entourage.android.tools.utils.Const

/**
 * Ouvre l'écran ou le flux existant correspondant à une suggestion (corps de la card ou CTA).
 * Le CTA et le tap sur le corps mènent au même détail : l'action « agir » n'est loguée côté moteur
 * qu'à la complétion réelle, et la card est rafraîchie au retour sur la home.
 */
class SuggestionNavigator(
    private val context: Context,
    /** « Dire bonjour » : crée ou ouvre la conversation avec cet utilisateur. */
    private val onSayHello: (userId: Int) -> Unit,
) {
    fun open(s: NextStepSuggestion) {
        val id = s.targetId
        when (s.type) {
            SuggestionType.ENTRAIDE -> id?.let {
                context.startActivity(
                    Intent(context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, it)
                        .putExtra(Const.ACTION_TITLE, s.title)
                        .putExtra(Const.IS_ACTION_DEMAND, true)
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
            SuggestionType.GROUP -> id?.let {
                context.startActivity(
                    Intent(context, GroupFeedActivity::class.java).putExtra(Const.GROUP_ID, it)
                )
            }
            SuggestionType.CONNECTION -> id?.let(onSayHello)
            SuggestionType.PLACE -> context.startActivity(
                Intent(context, GDSMainActivity::class.java)
                    .putExtra(GDSMainActivity.EXTRA_AIR_CONDITIONED, false)
            )
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
