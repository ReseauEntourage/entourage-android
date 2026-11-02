package social.entourage.android.suggestion

/**
 * Modèles du moteur de suggestion (EN-9473), côté app.
 *
 * Le moteur n'est pas encore livré : ces modèles décrivent le payload { hero, explorer[] } attendu
 * et sont aujourd'hui alimentés par [MockSuggestionProvider]. Les libellés fixes (badge, CTA, raisons)
 * sont portés par l'app (strings.xml) à partir de clés ; seuls les champs saisis par les utilisateurs
 * (titre, texte d'entraide) et les métas contextuelles (date, lieu, distance) arrivent en texte.
 */
enum class SuggestionType(val key: String) {
    ENTRAIDE("entraide"),
    CONTENT("content"),
    EVENT("event"),
    GROUP("group"),
    CONNECTION("connection"),
    PLACE("place");

    companion object {
        /** Retourne null pour un type inconnu de cette version de l'app (ancienne version installée). */
        fun fromKey(key: String?): SuggestionType? = entries.firstOrNull { it.key == key }
    }
}

enum class SuggestionVariant { DEFAULT, IN_PERSON, REMOTE, LOCAL, NATIONAL }

enum class SuggestionCta { ANSWER, READ, PARTICIPATE, REGISTER, JOIN, SAY_HELLO, SEE_PLACE }

enum class ContentTag { UNDERSTAND, ACT }

enum class ReasonKind { PROXIMITY, INTERESTS, MOMENT, HISTORY, NEED, ACCESSIBILITY, SOCIAL_PROOF }

/** Une raison du « Pourquoi cette suggestion ? » : un signal réellement utilisé par le moteur. */
data class SuggestionReason(val kind: ReasonKind, val param: String? = null)

/** Ligne de méta optionnelle de la card (date, lieu, distance, durée…). */
data class SuggestionMeta(val icon: String? = null, val text: String)

data class NextStepSuggestion(
    val id: String,
    /** Clé brute du type : conservée pour pouvoir ignorer un type inconnu sans planter. */
    val typeKey: String,
    val variant: SuggestionVariant = SuggestionVariant.DEFAULT,
    /** Entraide : catégorie (ex. « Équipement »). */
    val categoryLabel: String? = null,
    /** Contenu pédagogique : Comprendre / Agir. */
    val contentTag: ContentTag? = null,
    /** Entraide, connexion : auteur ou personne suggérée. */
    val personName: String? = null,
    /** Champ libre saisi par un utilisateur : affiché brut. */
    val title: String,
    /** Texte libre d'entraide : affiché brut. */
    val body: String? = null,
    val metas: List<SuggestionMeta> = emptyList(),
    val cta: SuggestionCta,
    /** Identifiant de la ressource ouverte par la card (action, événement, groupe, contenu, utilisateur). */
    val targetId: Int? = null,
    val reasons: List<SuggestionReason> = emptyList(),
    /** Personne isolée : la raison de type preuve sociale ne doit pas être affichée. */
    val isolatedAudience: Boolean = false,
) {
    val type: SuggestionType? get() = SuggestionType.fromKey(typeKey)

    /** 2 à 3 raisons maximum, sans preuve sociale pour une personne isolée. */
    fun displayableReasons(): List<SuggestionReason> =
        reasons
            .filterNot { isolatedAudience && it.kind == ReasonKind.SOCIAL_PROOF }
            .take(MAX_REASONS)

    companion object {
        const val MAX_REASONS = 3
    }
}

enum class ExplorerTab { ENTRAIDE_DEMANDS, EVENTS, GROUPS, CONTENT, PLACES }

/** Un accès de la zone « Pour vous aujourd'hui ». */
data class ExplorerAccess(
    val id: String,
    val typeKey: String,
    /** Teaser du moteur. */
    val label: String,
    /** Contexte : compteur ou sous-titre. */
    val context: String? = null,
    val tab: ExplorerTab,
    /** Filtre de rayon à appliquer à l'onglet cible, quand il le supporte. */
    val radiusKm: Int? = null,
) {
    val type: SuggestionType? get() = SuggestionType.fromKey(typeKey)
}

data class SuggestionPayload(
    val hero: NextStepSuggestion? = null,
    val explorer: List<ExplorerAccess> = emptyList(),
) {
    /** Retire les suggestions de type inconnu et borne l'Explorer à 4 accès. */
    fun sanitized(): SuggestionPayload = SuggestionPayload(
        hero = hero?.takeIf { it.type != null },
        explorer = explorer.filter { it.type != null }.take(MAX_EXPLORER),
    )

    companion object {
        const val MAX_EXPLORER = 4
    }
}

data class SuggestionUiState(
    val loading: Boolean = true,
    /** Dernier payload connu : conservé pendant un rafraîchissement ou une erreur réseau. */
    val payload: SuggestionPayload? = null,
)
