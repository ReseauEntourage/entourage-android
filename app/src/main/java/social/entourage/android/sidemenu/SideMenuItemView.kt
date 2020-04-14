package social.entourage.android.sidemenu

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import kotlinx.android.synthetic.entourage.layout_side_menu_item.view.*
import social.entourage.android.R

/**
 * Class that controls a sidemenu item
 */
class SideMenuItemView : RelativeLayout {
    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        View.inflate(context, R.layout.layout_side_menu_item, this)
        // Load attributes
        val styledAttributes = context.obtainStyledAttributes(
                attrs, R.styleable.SideMenuItemView, defStyle, 0)
        try {
            //Handle divider
            val bShowDivider = styledAttributes.getBoolean(R.styleable.SideMenuItemView_showDivider, true)
            if (!bShowDivider) {
                side_menu_item_divider.visibility = View.GONE
            }

            //Icon
            val iconResourceID = styledAttributes.getResourceId(R.styleable.SideMenuItemView_android_icon, 0)
            if (iconResourceID == 0) {
                side_menu_item_icon.visibility = View.GONE
            } else {
                side_menu_item_icon.setImageResource(iconResourceID)
            }
            val iconTint = styledAttributes.getColor(R.styleable.SideMenuItemView_iconTint, 0)
            if (iconTint != 0) {
                ImageViewCompat.setImageTintList(side_menu_item_icon, ColorStateList.valueOf(iconTint))
            }

            //Title
            val title = styledAttributes.getString(R.styleable.SideMenuItemView_android_title)
            side_menu_item_title.setText(title)
            val defaultTextColor = side_menu_item_title.getCurrentTextColor()
            val textColor = styledAttributes.getResourceId(R.styleable.SideMenuItemView_android_textColor, defaultTextColor)
            if (textColor != defaultTextColor) {
                side_menu_item_title.setTextColor(ContextCompat.getColor(context, textColor))
            }
            val textSize = styledAttributes.getDimensionPixelSize(R.styleable.SideMenuItemView_android_textSize, 0).toFloat()
            if (textSize > 0) {
                side_menu_item_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            }
            val backgroundResourceID = styledAttributes.getResourceId(R.styleable.SideMenuItemView_textBackground, 0)
            if (backgroundResourceID != 0) {
                side_menu_item_title.setBackgroundResource(backgroundResourceID)
            }
            val centerText = styledAttributes.getBoolean(R.styleable.SideMenuItemView_centerText, false)
            if (centerText) {
                side_menu_item_title.setGravity(Gravity.CENTER)
            }

            //Right arrow
            val bShowRightArrow = styledAttributes.getBoolean(R.styleable.SideMenuItemView_showRightArrow, true)
            if (!bShowRightArrow) {
                val rightArrow = findViewById<ImageView>(R.id.side_menu_item_arrow)
                if (rightArrow != null) rightArrow.visibility = View.GONE
            }
        } finally {
            styledAttributes.recycle()
        }
    }

    fun setTitle(@StringRes resid: Int) {
        side_menu_item_title?.setText(resid)
    }
}