package social.entourage.android.accessibility

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import social.entourage.android.R

/**
 * Préférence d'accessibilité "contraste amélioré" (Profil > Paramètres > Accessibilité).
 *
 * Les descriptions TalkBack, les tailles de cibles tactiles, les titres et les annonces d'erreur
 * s'appliquent à tout le monde. Seules les couleurs changent selon ce mode : les couleurs de marque
 * d'origine par défaut, des teintes plus foncées (contraste >= 4,5:1) quand il est activé.
 *
 * Le mode agit via un thème superposé ([R.style.ThemeOverlay_Entourage_HighContrast]) appliqué à
 * chaque activité par [social.entourage.android.EntourageApplication], qui redéfinit les attributs
 * `?attr/a11y...` utilisés dans les layouts, styles et drawables.
 */
object AccessibilitySettings {
    private const val PREFS_NAME = "accessibility_prefs"
    private const val KEY_HIGH_CONTRAST = "high_contrast"

    /** Lu par Compose et par le code qui n'a pas de [Context] sous la main. Chargé au démarrage de l'app. */
    @Volatile
    var highContrast: Boolean = false
        private set

    fun init(context: Context) {
        highContrast = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HIGH_CONTRAST, false)
    }

    fun setHighContrast(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_HIGH_CONTRAST, enabled)
        }
        highContrast = enabled
    }

    /** Applique le thème de contraste amélioré à une activité qui vient d'être créée (avant son setContentView). */
    fun applyTo(activity: Activity) {
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
