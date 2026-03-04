package social.entourage.android.home.pedago

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.widget.TextView
import androidx.core.content.ContextCompat
import social.entourage.android.R
import social.entourage.android.api.model.Category

fun TextView.setPedagoCategory(category: Category?) {
    val context = this.context
    val backgroundDrawable = this.background as? LayerDrawable
    val shapeDrawable = (backgroundDrawable?.findDrawableByLayerId(R.id.background_shape) as? GradientDrawable)?.mutate()

    when (category) {
        Category.ALL -> {
            this.text = context.getString(R.string.home_v2_pedago_item_tag_all)
            this.setTextColor(ContextCompat.getColor(context, R.color.light_orange))
            (shapeDrawable as? GradientDrawable)?.setColor(ContextCompat.getColor(context, R.color.beige_clair))
        }
        Category.ACT -> {
            this.text = context.getString(R.string.home_v2_pedago_item_tag_act)
            this.setTextColor(ContextCompat.getColor(context, R.color.pedago_text_act))
            (shapeDrawable as? GradientDrawable)?.setColor(ContextCompat.getColor(context, R.color.pedago_bg_act))
        }
        Category.INSPIRE -> {
            this.text = context.getString(R.string.home_v2_pedago_item_tag_inspire)
            this.setTextColor(ContextCompat.getColor(context, R.color.pedago_text_inspire))
            (shapeDrawable as? GradientDrawable)?.setColor(ContextCompat.getColor(context, R.color.pedago_bg_inspire))
        }
        Category.UNDERSTAND -> {
            this.text = context.getString(R.string.home_v2_pedago_item_tag_understand)
            this.setTextColor(ContextCompat.getColor(context, R.color.pedago_text_understand))
            (shapeDrawable as? GradientDrawable)?.setColor(ContextCompat.getColor(context, R.color.pedago_bg_understand))
        }
        null -> {
            this.text = ""
            this.setTextColor(ContextCompat.getColor(context, R.color.light_orange))
            (shapeDrawable as? GradientDrawable)?.setColor(ContextCompat.getColor(context, R.color.beige_clair))
        }
    }
}
