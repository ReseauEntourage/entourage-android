package social.entourage.android.tools.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuffXfermode
import android.graphics.PorterDuff
import android.view.View
import android.widget.FrameLayout

class HighlightOverlayView(context: Context, private val targetView: View) : FrameLayout(context) {

    private val backgroundPaint = Paint()
    private val eraserPaint = Paint()

    init {
        // Peinture pour le fond semi-transparent
        backgroundPaint.color = Color.parseColor("#80000000") // Noir semi-transparent

        // Peinture pour effacer (créer le trou)
        eraserPaint.isAntiAlias = true
        eraserPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        setLayerType(LAYER_TYPE_HARDWARE, null) // Nécessaire pour utiliser l'effacement
    }

    override fun dispatchDraw(canvas: Canvas) {
        // Dessiner le fond semi-transparent
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Obtenir la position de 'targetView' dans l'écran
        val location = IntArray(2)
        targetView.getLocationOnScreen(location)

        // Calculer les coordonnées du centre du 'targetView' dans les coordonnées de l'overlay
        val centerX = location[0] + targetView.width / 2f
        val centerY = location[1] + targetView.height / 2f - getStatusBarHeight()

        // Calculer le rayon du cercle (ajustez le facteur selon vos besoins)
        val radius = (Math.max(targetView.width, targetView.height) / 2f) * 1.2f // Augmentez 1.2f pour un cercle plus grand

        // Effacer le cercle au-dessus de 'targetView'
        canvas.drawCircle(centerX, centerY, radius, eraserPaint)

        // Dessiner le reste des enfants (par exemple, votre bubble layout)
        super.dispatchDraw(canvas)
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }
}
