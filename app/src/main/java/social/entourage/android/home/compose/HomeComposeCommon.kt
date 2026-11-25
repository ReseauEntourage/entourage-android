package social.entourage.android.home.compose

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlin.math.roundToInt

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
 * Image chargée par Glide (même décodage, même `scaleType`, même placeholder que l'ancienne home) mais dessinée par
 * Compose : pas d'`ImageView` créée à chaque carte qui entre à l'écran, ce qui coûtait au défilement.
 * Glide décode et transforme le bitmap à la taille mesurée ; [circleCrop] et [options] reproduisent les
 * transformations des anciens adapters. Le placeholder reste affiché tant que le bitmap n'est pas là (ou en cas d'échec).
 */
@Composable
fun HomeGlideImage(
    url: String?,
    modifier: Modifier,
    @DrawableRes placeholder: Int,
    scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP,
    circleCrop: Boolean = false,
    contentDescription: String? = null,
    @DrawableRes backgroundRes: Int? = null,
    options: (RequestOptions) -> RequestOptions = { it },
) {
    val context = LocalContext.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    val optionsState = rememberUpdatedState(options)

    DisposableEffect(url, size, circleCrop) {
        if (url.isNullOrBlank() || size.width <= 0 || size.height <= 0) {
            onDispose { }
        } else {
            var request = optionsState.value(RequestOptions())
            if (circleCrop) request = request.circleCrop()
            val glide = Glide.with(context)
            val target = object : CustomTarget<Bitmap>(size.width, size.height) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    bitmap = resource.asImageBitmap()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    bitmap = null
                }
            }
            glide.asBitmap().load(url).apply(request).into(target)
            onDispose {
                bitmap = null
                glide.clear(target)
            }
        }
    }

    val placeholderPainter = rememberDrawablePainter(placeholder)
    val contentScale = when (scaleType) {
        ImageView.ScaleType.FIT_CENTER, ImageView.ScaleType.CENTER_INSIDE -> ContentScale.Fit
        ImageView.ScaleType.FIT_XY -> ContentScale.FillBounds
        else -> ContentScale.Crop
    }
    val loaded = bitmap
    Box(
        modifier
            .onSizeChanged { size = it }
            .then(if (backgroundRes != null) Modifier.drawableBackground(backgroundRes) else Modifier)
            .then(
                if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription }
                else Modifier.clearAndSetSemantics { }
            )
    ) {
        if (loaded != null) {
            Image(
                bitmap = loaded,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Image(
                painter = placeholderPainter,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun rememberDrawablePainter(@DrawableRes id: Int): Painter {
    val context = LocalContext.current
    return remember(id, context) { DrawablePainter(AppCompatResources.getDrawable(context, id)?.mutate()) }
}

/** Dessine un `Drawable` (bitmap, vector, shape, layer-list…) tel quel, comme `ImageView.setImageResource`. */
private class DrawablePainter(private val drawable: Drawable?) : Painter() {
    override val intrinsicSize: Size = drawable
        ?.takeIf { it.intrinsicWidth > 0 && it.intrinsicHeight > 0 }
        ?.let { Size(it.intrinsicWidth.toFloat(), it.intrinsicHeight.toFloat()) }
        ?: Size.Unspecified

    override fun DrawScope.onDraw() {
        val d = drawable ?: return
        d.setBounds(0, 0, size.width.roundToInt(), size.height.roundToInt())
        drawIntoCanvas { d.draw(it.nativeCanvas) }
    }
}
