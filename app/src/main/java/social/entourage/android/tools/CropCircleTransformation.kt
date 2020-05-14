package social.entourage.android.tools

import android.graphics.*
import com.squareup.picasso.Transformation
import kotlin.math.min

/**
 * Created by mihaiionescu on 10/02/2017.
 */
class CropCircleTransformation : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        val size = min(source.width, source.height)
        val width = (source.width - size) / 2
        val height = (source.height - size) / 2
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                ?: throw OutOfMemoryError()
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        if (width != 0 || height != 0) {
            // source isn't square, move viewport to center
            val matrix = Matrix()
            matrix.setTranslate(-width.toFloat(), -height.toFloat())
            shader.setLocalMatrix(matrix)
        }
        paint.shader = shader
        paint.isAntiAlias = true
        val r = size / 2f
        canvas.drawCircle(r, r, r, paint)
        source.recycle()
        return bitmap
    }

    override fun key(): String {
        return "CropCircleTransformation()"
    }
}