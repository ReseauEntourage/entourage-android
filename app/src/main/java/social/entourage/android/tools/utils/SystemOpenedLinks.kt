package social.entourage.android.tools.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URI

/**
 * Liens de visioconférence qui doivent être confiés au système (app dédiée ou navigateur),
 * jamais ouverts dans la WebView embarquée de l'app : elle n'a pas de session Google, ne peut
 * pas lancer l'app Meet par lien universel et ne gère pas les permissions caméra/micro.
 *
 * Pour ajouter un service (zoom.us, teams.microsoft.com…), compléter [SYSTEM_OPENED_HOSTS].
 */
object SystemOpenedLinks {

    /** Hôtes exacts (en minuscules). `meet.google.com.evil.tld` ou `notmeet.google.com` n'en font pas partie. */
    private val SYSTEM_OPENED_HOSTS = setOf("meet.google.com")

    /**
     * Vrai si l'hôte de l'URL est exactement l'un de [SYSTEM_OPENED_HOSTS].
     * Accepte une URL sans schéma, en majuscules, avec un chemin (`/meet/…`) ou une query (`?authuser=0`).
     */
    fun shouldOpenWithSystem(url: String?): Boolean {
        val normalized = normalize(url) ?: return false
        return try {
            val uri = URI(normalized)
            val scheme = uri.scheme?.lowercase()
            val host = uri.host?.lowercase()
            (scheme == "http" || scheme == "https") && host in SYSTEM_OPENED_HOSTS
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Ouvre l'URL via le système si [shouldOpenWithSystem].
     * Renvoie vrai si le lien a été confié au système ; faux si le lien n'est pas concerné
     * ou si aucune app ne sait l'ouvrir (l'appelant garde alors son comportement actuel).
     */
    fun openIfSystemLink(context: Context, url: String?): Boolean {
        if (!shouldOpenWithSystem(url)) return false
        val normalized = normalize(url) ?: return false
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized))
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    /** Ajoute `https://` quand le schéma manque (l'URL elle-même n'est jamais réécrite autrement). */
    private fun normalize(url: String?): String? {
        val trimmed = url?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        return if (trimmed.contains("://")) trimmed else Const.HTTPS + trimmed
    }
}
