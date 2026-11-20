package social.entourage.android.suggestion

import androidx.annotation.StringRes
import social.entourage.android.api.model.guide.Poi

/**
 * Modèles d'affichage de « Votre prochain pas » (EN-9473). Ils sont alimentés par
 * [NextStepSuggestionProvider] (moteur) ou par [MockSuggestionProvider] (tests, prévisualisation).
 * Les libellés fixes (badge, CTA, titre du « Pourquoi ? ») sont portés par l'app (strings.xml) ;
 * les champs saisis par les utilisateurs (titre, texte d'entraide) et les phrases des raisons
 * arrivent du moteur et sont affichés tels quels.
 */
enum class SuggestionType(val key: String) {
    // Les clés sont les `kind` du moteur (design.md, décision 12).
    ENTRAIDE("action"),
    EVENT("outing"),
    GROUP("neighborhood"),
    PLACE("poi"),
    CONTENT_PATH("content_path"),
    SMALL_TALK("smalltalk"),
    POST_MESSAGE("post_message"),
    AMBASSADOR("become_ambassador"),

    // Types des maquettes que le moteur n'envoie jamais : conservés, inertes côté moteur.
    CONTENT("content"),
    CONNECTION("connection");

    companion object {
        /** Retourne null pour un type inconnu de cette version de l'app (ancienne version installée). */
        fun fromKey(key: String?): SuggestionType? = entries.firstOrNull { it.key == key }
    }
}

enum class SuggestionVariant { DEFAULT, IN_PERSON, REMOTE, LOCAL, NATIONAL }

enum class SuggestionCta { ANSWER, READ, PARTICIPATE, REGISTER, JOIN, SAY_HELLO, SEE_PLACE, TRY, WRITE, LEARN_MORE }

enum class ContentTag { UNDERSTAND, ACT }

/** Une raison du « Pourquoi cette suggestion ? » : le code du moteur et sa phrase, affichée telle quelle. */
data class SuggestionReason(val code: String, val text: String) {
    /** Icône de la raison, choisie par l'app selon le code (code inconnu : icône par défaut). */
    val icon: String
        get() = when (code) {
            "near_you" -> "📍"
            "shared_interests" -> "✨"
            "popular" -> "👥"
            "upcoming_event" -> "🗓️"
            "online_event", "papotage" -> "💻"
            "answers_your_need", "poi_for_need" -> "🔎"
            "local_group", "national_group" -> "👥"
            "neighbor_needs_help", "help_offered" -> "🤝"
            "content_path" -> "📘"
            "group_member_message", "smalltalk" -> "💬"
            "become_ambassador" -> "🌟"
            else -> "✅"
        }
}

/**
 * Ligne de méta optionnelle de la card (date, lieu, durée…). [textRes] remplace [text] pour un
 * libellé fixe de l'app.
 */
data class SuggestionMeta(
    val icon: String? = null,
    val text: String = "",
    @StringRes val textRes: Int? = null,
    /** Valeur injectée dans [textRes] (distance en m ou en km). */
    val textArg: Int? = null,
    /** Durée en minutes, ajoutée après [text] (contenu d'un mini-parcours). */
    val minutes: Int? = null,
)

/** Un contenu d'un mini-parcours. */
data class SuggestionItem(
    val id: Int?,
    val title: String,
    val detail: String? = null,
    val watched: Boolean = false,
    val minutes: Int? = null,
)

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
    /**
     * Entraide : photo de profil de l'auteur (`target.author.avatar_url`, miniature 300×300), nulle sans photo.
     * Jamais l'image jointe à une proposition d'aide (design.md, décision 13).
     */
    val avatarUrl: String? = null,
    /** Champ libre saisi par un utilisateur : affiché brut. Vide : l'app affiche le titre par défaut du type. */
    val title: String,
    /** Texte libre d'entraide ou phrase d'explication du moteur : affiché brut. */
    val body: String? = null,
    val metas: List<SuggestionMeta> = emptyList(),
    val cta: SuggestionCta,
    /** Identifiant de la ressource ouverte par la card (action, événement, groupe, contenu, lieu). */
    val targetId: Int? = null,
    val reasons: List<SuggestionReason> = emptyList(),
    /** Identifiant du prochain pas côté moteur (nul pour une autre piste ou une donnée factice). */
    val engineId: Int? = null,
    /** Formulaire « devenir Animateur ». */
    val link: String? = null,
    /** Contenus d'un mini-parcours. */
    val items: List<SuggestionItem> = emptyList(),
    /** Poster un message : groupe national (sans zone), comme le teste l'écran de création de message. */
    val nationalGroup: Boolean = false,
    /** Poster un message : groupe thématique (national) plutôt que groupe de voisins ; sert au badge. */
    val thematicGroup: Boolean = false,
    /** Entraide : une proposition d'aide (et non une demande), pour ouvrir le bon détail. */
    val isContribution: Boolean = false,
    /** Lieu solidaire : la cible du moteur, pour ouvrir sa fiche. */
    val poi: Poi? = null,
) {
    val type: SuggestionType? get() = SuggestionType.fromKey(typeKey)

    /** Contenu ouvert par un mini-parcours : le premier non lu, ou le premier si tous sont lus. */
    fun firstContentToOpen(): SuggestionItem? = items.firstOrNull { !it.watched } ?: items.firstOrNull()

    /** Les phrases du moteur, sans raison vide, bornées à [MAX_REASONS]. */
    fun displayableReasons(): List<SuggestionReason> =
        reasons.filter { it.text.isNotBlank() }.take(MAX_REASONS)

    companion object {
        const val MAX_REASONS = 3
    }
}

enum class ExplorerTab { ENTRAIDE_DEMANDS, EVENTS, GROUPS, CONTENT, PLACES }

/**
 * Une ligne de la zone « Pour vous aujourd'hui ». Avec une [suggestion] (autre piste du moteur),
 * le tap ouvre cette suggestion ; sans elle, il ouvre l'onglet [tab].
 */
data class ExplorerAccess(
    val id: String,
    val typeKey: String,
    /** Titre de la piste ou teaser. */
    val label: String,
    /** Contexte : sous-titre. */
    val context: String? = null,
    val tab: ExplorerTab,
    /** Filtre de rayon à appliquer à l'onglet cible, quand il le supporte. */
    val radiusKm: Int? = null,
    val suggestion: NextStepSuggestion? = null,
) {
    val type: SuggestionType? get() = SuggestionType.fromKey(typeKey)
}

data class SuggestionPayload(
    val hero: NextStepSuggestion? = null,
    val explorer: List<ExplorerAccess> = emptyList(),
) {
    /** Retire les suggestions de type inconnu et borne l'Explorer à 3 pistes. */
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
