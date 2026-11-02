package social.entourage.android.suggestion

import kotlinx.coroutines.delay

/** Source des suggestions. L'implémentation réelle appellera l'API du moteur (EN-9473). */
interface SuggestionProvider {
    suspend fun fetch(): SuggestionPayload

    /** « Plus tard » confirmé : le moteur ne re-propose pas la suggestion avant 30 jours. */
    suspend fun dismiss(suggestionId: String)
}

/**
 * Données factices en attendant le moteur. Chaque appel à [fetch] renvoie le type de suggestion
 * suivant, pour pouvoir passer en revue les 6 types (et leurs variants) depuis la home.
 */
class MockSuggestionProvider(private val latencyMs: Long = 700) : SuggestionProvider {

    private var index = 0

    override suspend fun fetch(): SuggestionPayload {
        delay(latencyMs)
        val hero = heroes[index % heroes.size]
        return SuggestionPayload(hero = hero, explorer = explorer).sanitized()
    }

    override suspend fun dismiss(suggestionId: String) {
        index++
    }

    private val heroes = listOf(
        NextStepSuggestion(
            id = "mock-event-in-person",
            typeKey = SuggestionType.EVENT.key,
            variant = SuggestionVariant.IN_PERSON,
            title = "Café solidaire du 11e",
            metas = listOf(
                SuggestionMeta("🗓️", "Samedi 14 juin, 14h00"),
                SuggestionMeta("📍", "Ménilmontant, à 1,2 km"),
            ),
            cta = SuggestionCta.PARTICIPATE,
            targetId = 1,
            reasons = listOf(
                SuggestionReason(ReasonKind.PROXIMITY, "1,2 km"),
                SuggestionReason(ReasonKind.MOMENT, "ce samedi"),
                SuggestionReason(ReasonKind.INTERESTS, "les rencontres autour d'un café"),
            ),
        ),
        NextStepSuggestion(
            id = "mock-entraide",
            typeKey = SuggestionType.ENTRAIDE.key,
            categoryLabel = "Équipement",
            personName = "Nino D.",
            title = "Besoin d'un téléphone",
            body = "Bonjour à tous, je suis actuellement à la recherche d'un télé…",
            metas = listOf(SuggestionMeta("📍", "À moins de 100 m de vous")),
            cta = SuggestionCta.ANSWER,
            targetId = 2,
            reasons = listOf(
                SuggestionReason(ReasonKind.PROXIMITY, "100 m"),
                SuggestionReason(ReasonKind.HISTORY, "avril"),
            ),
        ),
        NextStepSuggestion(
            id = "mock-content",
            typeKey = SuggestionType.CONTENT.key,
            contentTag = ContentTag.UNDERSTAND,
            title = "Femmes isolées en précarité : fiche profil",
            metas = listOf(SuggestionMeta("⏱️", "2 min de lecture")),
            cta = SuggestionCta.READ,
            targetId = 3,
            reasons = listOf(
                SuggestionReason(ReasonKind.INTERESTS, "comprendre l'isolement"),
                SuggestionReason(ReasonKind.ACCESSIBILITY),
            ),
        ),
        NextStepSuggestion(
            id = "mock-event-remote",
            typeKey = SuggestionType.EVENT.key,
            variant = SuggestionVariant.REMOTE,
            title = "Papotage solidaire",
            metas = listOf(
                SuggestionMeta("🗓️", "Jeudi 19 juin, 20h00"),
                SuggestionMeta("💻", "En visio, depuis chez vous"),
            ),
            cta = SuggestionCta.REGISTER,
            targetId = 4,
            reasons = listOf(
                SuggestionReason(ReasonKind.MOMENT, "jeudi soir"),
                SuggestionReason(ReasonKind.ACCESSIBILITY),
                SuggestionReason(ReasonKind.SOCIAL_PROOF),
            ),
            isolatedAudience = true,
        ),
        NextStepSuggestion(
            id = "mock-group-local",
            typeKey = SuggestionType.GROUP.key,
            variant = SuggestionVariant.LOCAL,
            title = "Voisins Ménilmontant",
            metas = listOf(SuggestionMeta(null, "Un groupe de voisins près de chez vous")),
            cta = SuggestionCta.JOIN,
            targetId = 5,
            reasons = listOf(
                SuggestionReason(ReasonKind.PROXIMITY, "800 m"),
                SuggestionReason(ReasonKind.SOCIAL_PROOF),
            ),
        ),
        NextStepSuggestion(
            id = "mock-group-national",
            typeKey = SuggestionType.GROUP.key,
            variant = SuggestionVariant.NATIONAL,
            title = "Cuisine et partage",
            metas = listOf(SuggestionMeta(null, "Un groupe pour échanger autour de la cuisine, partout en France")),
            cta = SuggestionCta.JOIN,
            targetId = 6,
            reasons = listOf(
                SuggestionReason(ReasonKind.INTERESTS, "cuisiner"),
                SuggestionReason(ReasonKind.HISTORY, "mars"),
            ),
        ),
        NextStepSuggestion(
            id = "mock-connection",
            typeKey = SuggestionType.CONNECTION.key,
            personName = "Karim G.",
            title = "Souhaitez la bienvenue à Karim",
            metas = listOf(SuggestionMeta(null, "Nouveau voisin dans votre quartier")),
            cta = SuggestionCta.SAY_HELLO,
            targetId = 7,
            reasons = listOf(
                SuggestionReason(ReasonKind.PROXIMITY, "400 m"),
                SuggestionReason(ReasonKind.INTERESTS, "le jardinage"),
            ),
        ),
        NextStepSuggestion(
            id = "mock-place",
            typeKey = SuggestionType.PLACE.key,
            title = "Bagagerie solidaire du 20e",
            metas = listOf(
                SuggestionMeta(null, "Consigne gratuite, sans rendez-vous"),
                SuggestionMeta("📍", "À 600 m, ouvert jusqu'à 18h"),
            ),
            cta = SuggestionCta.SEE_PLACE,
            reasons = listOf(
                SuggestionReason(ReasonKind.NEED, "où déposer vos affaires"),
                SuggestionReason(ReasonKind.ACCESSIBILITY),
            ),
        ),
    )

    private val explorer = listOf(
        ExplorerAccess(
            id = "mock-x-entraide",
            typeKey = SuggestionType.ENTRAIDE.key,
            label = "2 voisins ont besoin d'un coup de pouce",
            context = "À moins de 2 km de vous",
            tab = ExplorerTab.ENTRAIDE_DEMANDS,
            radiusKm = 2,
        ),
        ExplorerAccess(
            id = "mock-x-content",
            typeKey = SuggestionType.CONTENT.key,
            label = "Un contenu pour bien démarrer",
            context = "2 min de lecture",
            tab = ExplorerTab.CONTENT,
        ),
        ExplorerAccess(
            id = "mock-x-group",
            typeKey = SuggestionType.GROUP.key,
            label = "Un groupe de voisins près de chez vous",
            context = "Voisins Ménilmontant",
            tab = ExplorerTab.GROUPS,
        ),
    )
}
