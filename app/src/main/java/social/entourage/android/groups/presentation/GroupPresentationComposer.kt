package social.entourage.android.groups.presentation

/**
 * Génère le message de présentation à partir du profil et des intentions choisies.
 * Pur (aucune dépendance Android) : les libellés sont fournis par [Strings].
 */
object GroupPresentationComposer {

    enum class Intent { TALK, EVENTS, HELP }

    class Strings(
        val greeting: String,
        val introWithName: (String) -> String,
        val interestsLine: (String) -> String,
        val aboutLine: (String) -> String,
        val talk: String,
        val events: String,
        val help: String,
        val intentsIntro: String,
        val closing: String,
    )

    fun compose(
        strings: Strings,
        firstName: String?,
        interests: List<String>,
        about: String?,
        intents: Set<Intent>,
    ): String {
        val paragraphs = mutableListOf<String>()
        paragraphs += strings.greeting
        firstName?.trim()?.takeIf { it.isNotEmpty() }?.let { paragraphs += strings.introWithName(it) }
        if (interests.isNotEmpty()) paragraphs += strings.interestsLine(interests.joinToString(", ").lowercase())
        about?.trim()?.takeIf { it.isNotEmpty() }?.let { paragraphs += strings.aboutLine(it) }
        if (intents.isNotEmpty()) {
            val lines = intents.sorted().map {
                when (it) {
                    Intent.TALK -> strings.talk
                    Intent.EVENTS -> strings.events
                    Intent.HELP -> strings.help
                }
            }
            paragraphs += strings.intentsIntro + "\n" + lines.joinToString("\n") { "• $it" }
        }
        paragraphs += strings.closing
        return paragraphs.joinToString("\n\n")
    }
}
