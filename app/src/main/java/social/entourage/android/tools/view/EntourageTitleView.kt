package social.entourage.android.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import kotlinx.android.synthetic.main.layout_view_title.view.*
import social.entourage.android.R

/**
 * Custom title view with a close button and a title
 */
class EntourageTitleView : RelativeLayout {

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        View.inflate(context, R.layout.layout_view_title, this)
        // Load attributes
        val attributes = context.obtainStyledAttributes(
                attrs, R.styleable.EntourageTitleView, defStyle, 0)
        title_text?.text = attributes.getString(R.styleable.EntourageTitleView_entourageTitle)
        if (attributes.hasValue(R.styleable.EntourageTitleView_android_textColor)) {
            title_text?.setTextColor(attributes.getColor(R.styleable.EntourageTitleView_android_textColor, ContextCompat.getColor(context, R.color.greyish_brown)))
        }
        if (attributes.hasValue(R.styleable.EntourageTitleView_entourageTitleCloseDrawable)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                title_close_button?.setImageDrawable(attributes.getDrawable(
                    R.styleable.EntourageTitleView_entourageTitleCloseDrawable))
            } else {
                title_close_button?.setImageDrawable(ContextCompat.getDrawable(context,attributes.getResourceId(
                        R.styleable.EntourageTitleView_entourageTitleCloseDrawable, 0)))
            }

        }
        if (attributes.hasValue(R.styleable.EntourageTitleView_entourageTitleCloseDrawableTint)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                title_close_button?.imageTintList = attributes.getColorStateList(
                        R.styleable.EntourageTitleView_entourageTitleCloseDrawableTint)
            } else {
                ContextCompat.getDrawable(context,attributes.getResourceId(R.styleable.EntourageTitleView_entourageTitleCloseDrawable, 0))?.let {
                    DrawableCompat.setTint(it, attributes.getColor(R.styleable.EntourageTitleView_entourageTitleCloseDrawableTint, ContextCompat.getColor(context, R.color.accent)))
                }
            }
        }
        title_action_button?.text = attributes.getString(R.styleable.EntourageTitleView_entourageTitleAction)
        setBackgroundResource(attributes.getResourceId(R.styleable.EntourageTitleView_android_background, R.color.background))
        title_separator?.visibility = if (attributes.getBoolean(R.styleable.EntourageTitleView_entourageShowSeparator, true)) View.VISIBLE else View.GONE
        attributes.recycle()
    }

    fun setTitle(title: String) {
        title_text?.text = title
    }
}