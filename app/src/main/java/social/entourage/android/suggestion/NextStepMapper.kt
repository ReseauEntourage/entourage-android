package social.entourage.android.suggestion

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import social.entourage.android.R
import social.entourage.android.api.json.AppJson
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Pedago
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.request.NextStepDto
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Adaptateur du contrat du moteur vers les modèles d'affichage. Les cibles (`target`, `targets`)
 * sont décodées avec les modèles existants de l'app : elles sortent des sérialiseurs v1 du back.
 * Une suggestion illisible ou d'un type inconnu est écartée (null), jamais bloquante.
 */
object NextStepMapper {

    /** Les autres pistes du moteur sont au plus trois. */
    const val MAX_OTHERS = 3

    fun toSuggestion(dto: NextStepDto, locale: Locale = Locale.getDefault()): NextStepSuggestion? {
        val type = SuggestionType.fromKey(dto.kind) ?: return null
        val reasons = dto.reasons.mapNotNull { r ->
            val code = r.code
            val text = r.text
            if (code.isNullOrBlank() || text.isNullOrBlank()) null else SuggestionReason(code, text)
        }
        // La phrase d'explication du moteur sert de texte aux types sans contenu à consulter.
        val explanation = reasons.firstOrNull()?.text
        val base = runCatching {
            when (type) {
                SuggestionType.EVENT -> outing(dto, locale)
                SuggestionType.GROUP -> group(dto, type)
                SuggestionType.POST_MESSAGE -> group(dto, type)?.copy(
                    body = explanation, cta = SuggestionCta.WRITE, variant = SuggestionVariant.DEFAULT,
                    nationalGroup = dto.target?.decode<Group>()?.zone == null,
                    thematicGroup = dto.target?.decode<Group>()?.national == true,
                )
                SuggestionType.ENTRAIDE -> action(dto)
                SuggestionType.PLACE -> place(dto)
                SuggestionType.CONTENT_PATH -> contentPath(dto)
                SuggestionType.SMALL_TALK -> NextStepSuggestion(
                    id = idOf(dto), typeKey = type.key, title = "", body = explanation, cta = SuggestionCta.TRY,
                )
                // Sans adresse https exploitable, pas de card : un bouton sans effet vaut moins que rien.
                SuggestionType.AMBASSADOR -> httpsLink(dto.link)?.let { link ->
                    NextStepSuggestion(
                        id = idOf(dto), typeKey = type.key, title = "", body = explanation,
                        cta = SuggestionCta.LEARN_MORE, link = link,
                    )
                }
                // Jamais envoyés par le moteur.
                SuggestionType.CONTENT, SuggestionType.CONNECTION -> null
            }
        }.getOrNull() ?: return null
        return base.copy(id = idOf(dto), engineId = dto.id, reasons = reasons)
    }

    /** Les autres pistes deviennent les lignes de la zone « Pour vous aujourd'hui » : un tap ouvre la piste. */
    fun toExplorer(dtos: List<NextStepDto>, locale: Locale = Locale.getDefault()): List<ExplorerAccess> =
        dtos.take(MAX_OTHERS).mapNotNull { toSuggestion(it, locale) }.map { s ->
            ExplorerAccess(
                id = s.id,
                typeKey = s.typeKey,
                label = s.title,
                context = s.metas.firstOrNull { it.text.isNotBlank() }?.text ?: s.body,
                tab = s.type.toTab(),
                suggestion = s,
            )
        }

    private fun idOf(dto: NextStepDto): String =
        dto.id?.let { "next-step-$it" } ?: "other-${dto.kind}-${targetIdOf(dto) ?: dto.hashCode()}"

    private fun targetIdOf(dto: NextStepDto): Int? =
        (dto.target?.get("id") as? kotlinx.serialization.json.JsonPrimitive)?.content?.toIntOrNull()

    private inline fun <reified T> JsonObject.decode(): T = AppJson.json.decodeFromJsonElement(this)

    private fun date(d: Date?, locale: Locale): String? =
        d?.let { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale).format(it) }

    private fun outing(dto: NextStepDto, locale: Locale): NextStepSuggestion? {
        val e = dto.target?.decode<Events>() ?: return null
        val id = e.id ?: return null
        val remote = e.online == true
        val metas = buildList {
            date(e.metadata?.startsAt, locale)?.let { add(SuggestionMeta("🗓️", it)) }
            if (remote) {
                add(SuggestionMeta("💻", textRes = R.string.home_suggestion_meta_remote))
            } else {
                val address = e.location?.displayAddress?.takeIf { it.isNotBlank() }
                    ?: e.metadata?.displayAddress?.takeIf { it.isNotBlank() }
                address?.let { add(SuggestionMeta("📍", it)) }
                distanceMeta(e.distance)?.let { add(it) }
            }
        }
        return NextStepSuggestion(
            id = "", typeKey = SuggestionType.EVENT.key,
            variant = if (remote) SuggestionVariant.REMOTE else SuggestionVariant.IN_PERSON,
            title = e.title.orEmpty(), metas = metas,
            cta = if (remote) SuggestionCta.REGISTER else SuggestionCta.PARTICIPATE,
            targetId = id,
        )
    }

    private fun group(dto: NextStepDto, type: SuggestionType): NextStepSuggestion? {
        val g = dto.target?.decode<Group>() ?: return null
        val id = g.id ?: return null
        val metas = if (g.national) emptyList() else {
            listOfNotNull(g.address?.displayAddress?.takeIf { it.isNotBlank() }?.let { SuggestionMeta("📍", it) })
        }
        return NextStepSuggestion(
            id = "", typeKey = type.key,
            variant = if (g.national) SuggestionVariant.NATIONAL else SuggestionVariant.LOCAL,
            title = g.name.orEmpty(), metas = metas, cta = SuggestionCta.JOIN, targetId = id,
        )
    }

    private fun action(dto: NextStepDto): NextStepSuggestion? {
        val a = dto.target?.decode<Action>() ?: return null
        val id = a.id ?: return null
        // La distance du moteur d'abord (comme sur iOS) ; l'adresse seulement quand elle manque.
        val metas = listOfNotNull(
            distanceMeta(a.distance, "📍")
                ?: a.location?.displayAddress?.takeIf { it.isNotBlank() }?.let { SuggestionMeta("📍", it) },
        )
        return NextStepSuggestion(
            id = "", typeKey = SuggestionType.ENTRAIDE.key,
            personName = a.author?.userName, title = a.title.orEmpty(), body = a.description,
            metas = metas, cta = SuggestionCta.ANSWER, targetId = id,
            isContribution = !a.isDemand(),
        )
    }

    private fun place(dto: NextStepDto): NextStepSuggestion? {
        val p = dto.target?.decode<Poi>() ?: return null
        val metas = listOfNotNull(
            p.address?.takeIf { it.isNotBlank() }?.let { SuggestionMeta("📍", it) },
            p.openTimeTxt?.takeIf { it.isNotBlank() }?.let { SuggestionMeta("🕒", it) },
        )
        return NextStepSuggestion(
            id = "", typeKey = SuggestionType.PLACE.key,
            title = p.name.orEmpty(), metas = metas, cta = SuggestionCta.SEE_PLACE,
            targetId = p.id.toInt().takeIf { it != 0 },
            poi = p,
        )
    }

    private fun contentPath(dto: NextStepDto): NextStepSuggestion? {
        val items = dto.targets.orEmpty().mapNotNull { o ->
            val p = runCatching { o.decode<Pedago>() }.getOrNull() ?: return@mapNotNull null
            val title = p.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            SuggestionItem(p.id, title, watched = p.watched == true, minutes = p.duration?.toInt()?.takeIf { it > 0 })
        }
        if (items.isEmpty()) return null
        return NextStepSuggestion(
            id = "", typeKey = SuggestionType.CONTENT_PATH.key, title = "",
            metas = items.map { SuggestionMeta("📘", it.title, minutes = it.minutes) }, cta = SuggestionCta.READ,
            targetId = (items.firstOrNull { !it.watched } ?: items.first()).id, items = items,
        )
    }

    /** Seule une adresse https ouvre le formulaire « devenir Animateur » ; sinon la suggestion est écartée. */
    private fun httpsLink(link: String?): String? =
        link?.trim()?.takeIf { it.startsWith("https://", ignoreCase = true) }

    /**
     * Distance du moteur (en km), arrondie comme sur iOS : « 800 m » sous 1 km (par 100 m, 100 m au
     * minimum), « 3 km » au-delà.
     */
    private fun distanceMeta(km: Double?, icon: String? = null): SuggestionMeta? {
        if (km == null || km < 0) return null
        return if (km < 1) {
            SuggestionMeta(icon, textRes = R.string.home_suggestion_meta_distance_m, textArg = maxOf((km * 10).roundToInt() * 100, 100))
        } else {
            SuggestionMeta(icon, textRes = R.string.home_suggestion_meta_distance_km, textArg = km.roundToInt())
        }
    }

    private fun SuggestionType?.toTab(): ExplorerTab = when (this) {
        SuggestionType.ENTRAIDE -> ExplorerTab.ENTRAIDE_DEMANDS
        SuggestionType.EVENT -> ExplorerTab.EVENTS
        SuggestionType.GROUP, SuggestionType.POST_MESSAGE -> ExplorerTab.GROUPS
        SuggestionType.PLACE -> ExplorerTab.PLACES
        else -> ExplorerTab.CONTENT
    }
}
