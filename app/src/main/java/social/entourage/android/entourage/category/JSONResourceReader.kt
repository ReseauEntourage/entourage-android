package social.entourage.android.entourage.category

import android.content.res.Resources
import com.google.gson.GsonBuilder
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringWriter
import java.io.Writer
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

/**
 * Created by mihaiionescu on 20/09/2017.
 */
class JSONResourceReader(resources: Resources, id: Int) {
    // Our JSON, in string form.
    private val jsonString: String

    /**
     * Build an object from the specified JSON resource using Gson.
     *
     * @param type The type of the object to build.
     *
     * @return An object of type T, with member fields populated using Gson.
     */
    fun <T> constructUsingGson(type: Class<T>?): T {
        return GsonBuilder().create().fromJson(jsonString, type)
    }

    /**
     * Build an object from the specified JSON resource using Gson.
     *
     * @param type The type of the object to build.
     *
     * @return An object of type T, with member fields populated using Gson.
     */
    fun <T> constructUsingGson(type: Type?): T {
        return GsonBuilder().create().fromJson(jsonString, type)
    }

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