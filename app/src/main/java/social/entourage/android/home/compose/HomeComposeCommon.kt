package social.entourage.android.home.compose

import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * Interrupteur de dev de la home Compose (branche `homeCompose`). `true` : [HomeComposeFragment]
 * (home entièrement en Compose) ; `false` : l'ancienne [social.entourage.android.home.HomeFragment]
 * (XML + RecyclerView), conservée le temps de la comparaison pixel à pixel.
 * Les deux fragments sont résolus par [social.entourage.android.home.HomeFragmentFactory].
 */
object HomeComposeFeature {
    val ENABLED: Boolean get() = true
}

/** Taille de texte d'une dimension `sp` du projet, au pixel près comme `android:textSize="@dimen/..."` (suit l'échelle de police). */
@Composable
fun spRes(@DimenRes id: Int): TextUnit {
    val resources = LocalContext.current.resources
    val density = LocalDensity.current
    return with(density) { resources.getDimension(id).toSp() }
}

/**
 * Image chargée par Glide dans un vrai `ImageView` : même décodage, même `scaleType`, même placeholder
 * que l'ancienne home (pas de recadrage différent d'une librairie d'images Compose).
 * [circleCrop] et [roundedCornersPx] reproduisent les transformations Glide des anciens adapters.
 */
@Composable
fun HomeGlideImage(
    url: String?,
    modifier: Modifier,
    @DrawableRes placeholder: Int,
    scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP,
    circleCrop: Boolean = false,
    contentDescription: String? = null,
    options: (RequestOptions) -> RequestOptions = { it },
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                this.scaleType = scaleType
                if (contentDescription == null) importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
        },
        update = { view ->
            view.contentDescription = contentDescription
            var request = options(RequestOptions().placeholder(placeholder))
            if (circleCrop) request = request.circleCrop()
            if (url.isNullOrBlank()) {
                Glide.with(view).clear(view)
                view.setImageResource(placeholder)
            } else {
                Glide.with(view).load(url).apply(request).into(view)
            }
        },
    )
}
