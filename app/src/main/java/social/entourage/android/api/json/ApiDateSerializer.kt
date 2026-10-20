package social.entourage.android.api.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

/**
 * Sérialiseur des `Date` de l'API : écrit `yyyy-MM-dd'T'HH:mm:ss.SSSZ` (comme l'ancien
 * `setDateFormat` de Gson) et relit, dans l'ordre :
 * 1. ce même format ;
 * 2. l'ISO 8601 avec offset ou `Z` (Gson retombait sur `ISO8601Utils`) ;
 * 3. le format par défaut de Gson ("Jan 15, 2024 9:30:00 AM"), pour relire les objets déjà persistés
 *    en local par `ComplexPreferences` (qui utilisait un `Gson()` sans format de date).
 */
object ApiDateSerializer : KSerializer<Date> {
    private const val PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("social.entourage.android.api.json.ApiDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        // SimpleDateFormat n'est pas thread-safe : une instance par appel.
        encoder.encodeString(SimpleDateFormat(PATTERN, Locale.US).format(value))
    }

    override fun deserialize(decoder: Decoder): Date = parse(decoder.decodeString())

    internal fun parse(text: String): Date {
        try {
            return SimpleDateFormat(PATTERN, Locale.US).parse(text)!!
        } catch (_: ParseException) {
        }
        try {
            return Date.from(OffsetDateTime.parse(text).toInstant())
        } catch (_: DateTimeParseException) {
        }
        for (style in intArrayOf(DateFormat.DEFAULT, DateFormat.MEDIUM, DateFormat.LONG, DateFormat.SHORT)) {
            try {
                return DateFormat.getDateTimeInstance(style, style, Locale.US).parse(text)!!
            } catch (_: ParseException) {
            }
        }
        throw SerializationException("Date illisible : $text")
    }
}
