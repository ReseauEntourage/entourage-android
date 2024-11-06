package social.entourage.android.tools.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.view.View
import android.widget.FrameLayout

class HighlightOverlayView(context: Context, private val targetView: View) : FrameLayout(context) {

    private val backgroundPaint = Paint()
    private val eraserPaint = Paint()

    init {
        // Peinture pour le fond semi-transparent
        backgroundPaint.color = Color.parseColor("#80000000") // Noir semi-transparent

        // Peinture pour effacer (créer le trou)
        eraserPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        setLayerType(LAYER_TYPE_HARDWARE, null) // Nécessaire pour utiliser l'effacement
    }

    override fun dispatchDraw(canvas: Canvas) {
        // Dessiner le fond semi-transparent
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Obtenir la position de 'targetView' dans l'écran
        val location = IntArray(2)
        targetView.getLocationOnScreen(location)

        // Calculer la position de 'targetView' dans les coordonnées de l'overlay
        val rect = RectF(
            location[0].toFloat(),
            location[1].toFloat() - getStatusBarHeight(), // Ajuster pour la barre de statut
            (location[0] + targetView.width).toFloat(),
            (location[1] + targetView.height).toFloat() - getStatusBarHeight()
        )

        // Effacer le rectangle au-dessus de 'targetView'
        canvas.drawRect(rect, eraserPaint)

        // Dessiner le reste des enfants (par exemple, votre bubble layout)
        super.dispatchDraw(canvas)
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }
}
