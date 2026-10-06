package social.entourage.android.tools.utils

/**
 * Détection côté client d'un numéro de téléphone français dans le texte d'un message
 * (avertissement non bloquant affiché sous la bulle des conversations privées, EN-8022).
 *
 * Formats reconnus : 06 12 34 56 78, 0612345678, 06.12.34.56.78, 06-12-34-56-78,
 * +33 6 12 34 56 78, +33612345678, 0033 6 12 34 56 78 (un éventuel "(0)" après l'indicatif
 * est toléré). Les 9 chiffres nationaux commencent par 1 à 9.
 */
object PhoneNumberDetector {

    private val FRENCH_PHONE_REGEX = Regex(
        """(?<![\d+])(?:(?:\+|00)33[\s.\-]?(?:\(0\)[\s.\-]?)?|0)[1-9](?:[\s.\-]?\d{2}){4}(?!\d)"""
    )

    private val HTML_TAG_REGEX = Regex("<[^>]*>")

    /** Le message (texte brut ou HTML) contient-il un numéro de téléphone français ? */
    fun containsFrenchPhoneNumber(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        val plain = text.replace(HTML_TAG_REGEX, " ")
        return FRENCH_PHONE_REGEX.containsMatchIn(plain)
    }

    /** Plages (début..fin inclus) des numéros trouvés dans un texte brut, pour les mettre en évidence. */
    fun findFrenchPhoneNumbers(plainText: CharSequence): List<IntRange> =
        FRENCH_PHONE_REGEX.findAll(plainText).map { it.range }.toList()
}
