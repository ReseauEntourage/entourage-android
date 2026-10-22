package social.entourage.android.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.StructureKind
import org.junit.Assert.fail
import org.junit.Test
import social.entourage.android.api.json.AppJson
import social.entourage.android.api.model.User
import java.io.File

/**
 * Filet de sécurité : chaque modèle `@Serializable` doit se décoder depuis `{}`.
 * Une propriété sans valeur par défaut fait lever `MissingFieldException` dès qu'une réponse
 * d'API est partielle (ex. `POST /users` ne renvoie que le téléphone) ; l'app avale l'erreur
 * dans `onFailure` et affiche « vérifiez votre connexion ».
 *
 * Les wrappers de racine (`class FooWrapper(val foo: Foo)`) sont volontairement obligatoires
 * et listés dans [INTENTIONALLY_REQUIRED], avec quelques modèles de métadonnées dont l'API envoie
 * toujours les champs (liste figée : toute nouvelle classe fautive fait échouer le test).
 */
class EmptyObjectDecodingTest {

    @Test
    fun everySerializableModel_decodesFromEmptyObject() {
        val offenders = mutableListOf<String>()
        var checked = 0

        modelClassNames().forEach { name ->
            val serializer = serializerOf(name) ?: return@forEach
            if (serializer.descriptor.kind != StructureKind.CLASS) return@forEach // enums, etc.
            checked++
            if (name in INTENTIONALLY_REQUIRED) return@forEach
            try {
                AppJson.json.decodeFromString(serializer, "{}")
            } catch (e: SerializationException) {
                offenders += "$name : ${e.message?.lineSequence()?.first()}"
            } catch (_: Throwable) {
                // Classe dépendant d'Android à l'initialisation : hors périmètre d'un test JVM.
            }
        }

        if (checked < 50) fail("Scan des modèles cassé : seulement $checked classes @Serializable trouvées")
        if (offenders.isNotEmpty()) {
            fail(
                "Modèles @Serializable avec propriété(s) obligatoire(s) (ajouter une valeur par défaut) :\n" +
                    offenders.sorted().joinToString("\n")
            )
        }
    }

    /** Noms des classes de premier niveau du code applicatif, qu'il soit livré en dossier ou en jar. */
    private fun modelClassNames(): List<String> {
        val location = File(User::class.java.protectionDomain.codeSource.location.toURI())
        val names = if (location.isDirectory) {
            location.walkTopDown().filter { it.isFile }
                .map { it.relativeTo(location).path.replace(File.separatorChar, '/') }.toList()
        } else {
            java.util.zip.ZipFile(location).use { zip -> zip.entries().toList().map { it.name } }
        }
        return names
            .filter { it.endsWith(".class") && !it.substringAfterLast('/').contains('$') }
            .map { it.removeSuffix(".class").replace('/', '.') }
            .filter { it.startsWith("social.entourage.android") }
    }

    private fun serializerOf(className: String): KSerializer<Any>? = try {
        val clazz = Class.forName(className, false, javaClass.classLoader)
        val companion = clazz.getField("Companion").get(null)
        val method = companion.javaClass.methods.firstOrNull { it.name == "serializer" && it.parameterCount == 0 }
        @Suppress("UNCHECKED_CAST")
        method?.invoke(companion) as? KSerializer<Any>
    } catch (_: Throwable) {
        null
    }

    private companion object {
        val INTENTIONALLY_REQUIRED = setOf(
            "social.entourage.android.api.model.ApplicationInfo",
            "social.entourage.android.api.model.ConcernTag",
            "social.entourage.android.api.model.InterestTag",
            "social.entourage.android.api.model.InvolvementTag",
            "social.entourage.android.api.model.LocationPoint",
            "social.entourage.android.api.model.MemberWrapper",
            "social.entourage.android.api.model.Metadata",
            "social.entourage.android.api.model.PartnerCreateWrapper",
            "social.entourage.android.api.model.PartnerResponse",
            "social.entourage.android.api.model.PartnerUpdateWrapper",
            "social.entourage.android.api.model.PoiCategory",
            "social.entourage.android.api.model.SectionTag",
            "social.entourage.android.api.model.ShareMessage",
            "social.entourage.android.api.model.SignalTag",
            "social.entourage.android.api.model.TagsMetadata",
            "social.entourage.android.api.model.UserBlockedUser",
            "social.entourage.android.api.model.UserMetadata",
            "social.entourage.android.api.model.UserReaction",
            "social.entourage.android.api.model.UserReportWrapper",
            "social.entourage.android.api.request.ApplicationWrapper",
            "social.entourage.android.api.request.ChatMessageWrapper",
            "social.entourage.android.api.request.ContribCancelWrapper",
            "social.entourage.android.api.request.ContribWrapper",
            "social.entourage.android.api.request.CreateEventWrapper",
            "social.entourage.android.api.request.DemandCancelWrapper",
            "social.entourage.android.api.request.DemandWrapper",
            "social.entourage.android.api.request.DiscussionDetailWrapper",
            "social.entourage.android.api.request.EventWrapper",
            "social.entourage.android.api.request.GroupWrapper",
            "social.entourage.android.api.request.JoinRoleBody",
            "social.entourage.android.api.request.LoginResponse",
            "social.entourage.android.api.request.MetaDataResponse",
            "social.entourage.android.api.request.NotificationInAppResponse",
            "social.entourage.android.api.request.NotificationPermissionsResponse",
            "social.entourage.android.api.request.PartnerWrapper",
            "social.entourage.android.api.request.PedagogicSingleResponse",
            "social.entourage.android.api.request.PostWrapper",
            "social.entourage.android.api.request.ReportWrapper",
            "social.entourage.android.api.request.ShareMessageWrapper",
            "social.entourage.android.api.request.SmallTalkWrapper",
            "social.entourage.android.api.request.SummaryResponse",
            "social.entourage.android.api.request.UnreadCountWrapper",
            "social.entourage.android.api.request.UserBlockedWrapper",
            "social.entourage.android.api.request.UserResponse",
            "social.entourage.android.api.request.UserSmallTalkRequestWrapper"
        )
    }
}
