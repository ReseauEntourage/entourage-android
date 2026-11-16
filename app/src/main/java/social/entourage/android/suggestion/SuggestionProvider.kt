package social.entourage.android.suggestion

import kotlinx.coroutines.delay

/** Source des suggestions : le moteur ([NextStepSuggestionProvider]) ou des données factices ([MockSuggestionProvider]). */
interface SuggestionProvider {
    suspend fun fetch(): SuggestionPayload

    /**
     * « Plus tard » confirmé : le moteur ne re-propose pas la suggestion avant 30 jours.
     * Renvoie la suggestion qui la remplace, ou null si elle est inconnue (l'appelant rafraîchit alors).
     */
    suspend fun dismiss(suggestion: NextStepSuggestion): NextStepSuggestion?

    /** Geste de l'utilisateur sur une suggestion, transmis au moteur pour la mesure. Silencieux en cas d'erreur. */
    suspend fun report(interaction: SuggestionInteraction) = Unit
}

/**
 * Données factices (tests, prévisualisation). Chaque appel à [fetch] renvoie le type de suggestion
 * suivant, pour pouvoir passer en revue les types (et leurs variants) depuis la home.
 */
class MockSuggestionProvider(private val latencyMs: Long = 700) : SuggestionProvider {

    private var index = 0

    override suspend fun fetch(): SuggestionPayload {
        delay(latencyMs)
        val hero = heroes[index % heroes.size]
        return SuggestionPayload(hero = hero, explorer = explorer).sanitized()
    }

    override suspend fun dismiss(suggestion: NextStepSuggestion): NextStepSuggestion? {
        index++
        return null
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
                SuggestionReason("near_you", "C'est à 1,2 km de chez vous."),
                SuggestionReason("upcoming_event", "C'est ce samedi, une occasion de rencontrer du monde."),
                SuggestionReason("shared_interests", "On y parle de les rencontres autour d'un café, des sujets qui vous plaisent."),
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
                SuggestionReason("near_you", "C'est à 100 m de chez vous."),
                SuggestionReason("history", "Vous avez participé à un moment similaire en avril."),
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
                SuggestionReason("shared_interests", "On y parle de comprendre l'isolement, des sujets qui vous plaisent."),
                SuggestionReason("accessibility", "C'est gratuit et sans rendez-vous."),
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
                SuggestionReason("upcoming_event", "C'est jeudi soir, une occasion de rencontrer du monde."),
                SuggestionReason("accessibility", "C'est gratuit et sans rendez-vous."),
                SuggestionReason("popular", "Des voisins y participent déjà."),
            ),
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
                SuggestionReason("near_you", "C'est à 800 m de chez vous."),
                SuggestionReason("popular", "Des voisins y participent déjà."),
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
                SuggestionReason("shared_interests", "On y parle de cuisiner, des sujets qui vous plaisent."),
                SuggestionReason("history", "Vous avez participé à un moment similaire en mars."),
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
                SuggestionReason("near_you", "C'est à 400 m de chez vous."),
                SuggestionReason("shared_interests", "On y parle de le jardinage, des sujets qui vous plaisent."),
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
                SuggestionReason("answers_your_need", "Vous cherchiez où déposer vos affaires."),
                SuggestionReason("accessibility", "C'est gratuit et sans rendez-vous."),
            ),
        ),
        NextStepSuggestion(
            id = "mock-smalltalk",
            typeKey = SuggestionType.SMALL_TALK.key,
            title = "",
            body = "Les Bonnes ondes vous mettent en relation avec quelques personnes pour discuter.",
            cta = SuggestionCta.TRY,
            reasons = listOf(SuggestionReason("smalltalk", "Les Bonnes ondes vous mettent en relation avec quelques personnes pour discuter.")),
        ),
        NextStepSuggestion(
            id = "mock-content-path",
            typeKey = SuggestionType.CONTENT_PATH.key,
            title = "",
            metas = listOf(
                SuggestionMeta("📘", "Comprendre l'isolement"),
                SuggestionMeta("📘", "Agir près de chez soi"),
                SuggestionMeta("📘", "Aller vers l'autre"),
            ),
            cta = SuggestionCta.READ,
            targetId = 3,
            items = listOf(SuggestionItem(3, "Comprendre l'isolement")),
            reasons = listOf(SuggestionReason("content_path", "Trois lectures courtes pour découvrir Entourage.")),
        ),
        NextStepSuggestion(
            id = "mock-post-message",
            typeKey = SuggestionType.POST_MESSAGE.key,
            title = "Voisins Ménilmontant",
            body = "Vous faites partie de ce groupe : un petit message suffit pour faire connaissance.",
            cta = SuggestionCta.WRITE,
            targetId = 5,
            reasons = listOf(SuggestionReason("group_member_message", "Vous faites partie de ce groupe : un petit message suffit pour faire connaissance.")),
        ),
        NextStepSuggestion(
            id = "mock-ambassador",
            typeKey = SuggestionType.AMBASSADOR.key,
            title = "",
            body = "Envie d'animer des rencontres près de chez vous ? Le rôle d'Animateur Entourage est fait pour ça.",
            cta = SuggestionCta.LEARN_MORE,
            link = "https://forms.example.com/devenir-animateur-entourage",
            reasons = listOf(SuggestionReason("become_ambassador", "Envie d'animer des rencontres près de chez vous ? Le rôle d'Animateur Entourage est fait pour ça.")),
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
