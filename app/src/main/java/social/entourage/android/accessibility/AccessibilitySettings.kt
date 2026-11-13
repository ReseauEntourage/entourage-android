package social.entourage.android.accessibility

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.accessibility.AccessibilityManager
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import social.entourage.android.EntourageApplication
import social.entourage.android.R

/**
 * Préférence d'accessibilité "contraste amélioré" (Profil > Paramètres > Accessibilité).
 *
 * Les descriptions TalkBack, les tailles de cibles tactiles, les titres et les annonces d'erreur
 * s'appliquent à tout le monde. Seules les couleurs changent selon ce mode : les couleurs de marque
 * d'origine par défaut, des teintes plus foncées (contraste >= 4,5:1) quand il est activé.
 *
 * Règles de la définition produit (EN-9858) appliquées ici :
 * - le choix est propre au compte connecté (clé par identifiant d'utilisateur) ;
 * - sans choix enregistré, le mode est désactivé, sauf si un lecteur d'écran (TalkBack) est actif sur
 *   le téléphone : il est alors activé automatiquement et enregistré ;
 * - le choix de l'utilisateur prime : un mode désactivé par lui n'est jamais réactivé automatiquement,
 *   un mode activé n'est jamais désactivé automatiquement ;
 * - les écrans d'avant connexion (aucun compte connecté) restent dans leur état d'origine.
 *
 * Le mode agit via un thème superposé ([R.style.ThemeOverlay_Entourage_HighContrast]) appliqué à
 * chaque activité par [EntourageApplication], qui redéfinit les attributs `?attr/a11y...` utilisés
 * dans les layouts, styles et drawables.
 *
 * Limite : le choix est conservé sur l'appareil. Le conserver sur le compte (retrouvé après un changement
 * de téléphone, partagé avec iOS) demande un champ côté API qui n'existe pas encore.
 */
object AccessibilitySettings {
    private const val PREFS_NAME = "accessibility_prefs"
    private const val KEY_HIGH_CONTRAST_PREFIX = "high_contrast_user_"

    /** Lu par Compose et par le code qui n'a pas de [Context] sous la main. Tenu à jour par [refresh]. */
    @Volatile
    var highContrast: Boolean = false
        private set

    private fun currentUserId(): Int? =
        runCatching { EntourageApplication.get().me()?.id }.getOrNull()?.takeIf { it != 0 }

    private fun key(userId: Int) = KEY_HIGH_CONTRAST_PREFIX + userId

    /** Choix enregistré pour le compte connecté : true / false, ou null s'il n'a jamais réglé le mode. */
    fun storedChoice(context: Context): Boolean? {
        val userId = currentUserId() ?: return null
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(key(userId))) prefs.getBoolean(key(userId), false) else null
    }

    /** Vrai si un lecteur d'écran (TalkBack) est actif sur le téléphone. */
    fun isScreenReaderActive(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        return manager?.isEnabled == true && manager.isTouchExplorationEnabled
    }

    /**
     * Recalcule l'état effectif. Sans compte connecté, le mode est désactivé. Pour un compte sans choix
     * enregistré, un lecteur d'écran actif active le mode et l'enregistre.
     */
    fun refresh(context: Context) {
        val userId = currentUserId()
        if (userId == null) {
            highContrast = false
            return
        }
        val stored = storedChoice(context)
        highContrast = when {
            stored != null -> stored
            isScreenReaderActive(context) -> {
                save(context, userId, true)
                true
            }
            else -> false
        }
    }

    /** Enregistre le choix explicite de l'utilisateur pour le compte connecté. */
    fun setHighContrast(context: Context, enabled: Boolean) {
        val userId = currentUserId() ?: return
        save(context, userId, enabled)
        highContrast = enabled
    }

    private fun save(context: Context, userId: Int, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(key(userId), enabled)
        }
    }

    /** Applique le thème de contraste amélioré à une activité qui vient d'être créée (avant son setContentView). */
    fun applyTo(activity: Activity) {
        refresh(activity)
        if (highContrast) {
            activity.theme.applyStyle(R.style.ThemeOverlay_Entourage_HighContrast, true)
        }
    }
}

/**
 * Résout une couleur du thème de l'activité (voir attrs.xml, `a11y...`). Si le contexte n'a pas le
 * thème de l'app (contexte d'application, par exemple), retombe sur la couleur d'origine [default].
 */
fun Context.a11yColor(@AttrRes attr: Int, @ColorRes default: Int): Int {
    val value = TypedValue()
    return if (theme.resolveAttribute(attr, value, true)) {
        if (value.resourceId != 0) ContextCompat.getColor(this, value.resourceId) else value.data
    } else {
        ContextCompat.getColor(this, default)
    }
}
