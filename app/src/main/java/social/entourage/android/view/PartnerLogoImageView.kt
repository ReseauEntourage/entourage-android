package social.entourage.android.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import social.entourage.android.R

/**
 * Custom class for partner logo with a transparent background if no drawable is set
 * Created by mihaiionescu on 30/01/2017.
 */
class PartnerLogoImageView : AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (drawable == null) {
            setBackgroundResource(R.color.partner_logo_transparent)
        } else {
            background = AppCompatResources.getDrawable(context, R.drawable.bg_partner_logo)
        }
    }
}