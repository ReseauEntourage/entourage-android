package social.entourage.android.user.role

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import social.entourage.android.R

/**
 * Custom AppCompatTextView with a rounded rectangle background
 * Used to display a role
 * Created by Mihai Ionescu on 18/05/2018.
 */
class UserRoleView : AppCompatTextView {
    private var backgroundCornerRadius = 5.0f

    @ColorRes
    private var backgroundColor = 0

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    constructor(context: Context?) : super(context!!) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        backgroundCornerRadius = resources.getDimension(R.dimen.role_corner_radius)
        backgroundColor = R.color.white
        setCustomBackground()
        setTextColor(ContextCompat.getColor(context, R.color.profile_role_text))
        setSingleLine()
    }

    // ----------------------------------
    // HELPER
    // ----------------------------------
    fun setRole(role: UserRole) {
        setText(role.nameResourceId)
        changeBackgroundColor(role.colorResourceId)
    }

    // ----------------------------------
    // BACKGROUND
    // ----------------------------------
    fun changeBackgroundColor(@ColorRes backgroundColor: Int) {
        this.backgroundColor = backgroundColor
        setCustomBackground()
    }

    private fun setCustomBackground() {
        val background = GradientDrawable()
        background.setColor(ContextCompat.getColor(context, backgroundColor))
        background.cornerRadius = backgroundCornerRadius
        setPadding(
                (textSize * 2 / 3.0).toInt(),
                (textSize * 1 / 4.0).toInt(),
                (textSize * 2 / 3.0).toInt(),
                (textSize * 1 / 4.0).toInt()
        )
        setBackground(background)
    }
}