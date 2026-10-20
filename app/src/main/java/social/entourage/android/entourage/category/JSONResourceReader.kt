package social.entourage.android.entourage.category

import android.content.res.Resources
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringWriter
import java.io.Writer
import java.nio.charset.StandardCharsets
import social.entourage.android.api.json.AppJson

/**
 * Created by mihaiionescu on 20/09/2017.
 */
class JSONResourceReader(resources: Resources, id: Int) {
    // Our JSON, in string form.
    @PublishedApi
    internal val jsonString: String

    /** Décode la ressource JSON en liste d'objets de type [T]. */
    inline fun <reified T> decodeList(): List<T> = AppJson.decode(jsonString)

    /**
     * Read from a resources file and create a [JSONResourceReader] object that will allow the creation of other
     * objects from this resource.
     *
     * @param id The id for the resource to load, typically held in the raw/ folder.
     */
    init {
        val resourceReader = resources.openRawResource(id)
        val writer: Writer = StringWriter()
        try {
            val reader = BufferedReader(InputStreamReader(resourceReader, StandardCharsets.UTF_8))
            var line = reader.readLine()
            while (line != null) {
                writer.write(line)
                line = reader.readLine()
            }
        } catch (e: Exception) {
            Timber.e(e, "Unhandled exception while using JSONResourceReader")
        } finally {
            try {
                resourceReader.close()
            } catch (e: Exception) {
                Timber.e(e, "Unhandled exception while using JSONResourceReader")
            }
        }
        jsonString = writer.toString()
    }
}