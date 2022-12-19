package social.entourage.android.old_v7.user

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.layout_user_name.view.*
import social.entourage.android.R
import social.entourage.android.old_v7.user.role.UserRole
import social.entourage.android.old_v7.user.role.UserRoleView
import social.entourage.android.old_v7.user.role.UserRolesFactory.findByName
import java.util.*

/**
 * Custom View used to display a text with optional tags
 * Created by Mihai Ionescu on 17/05/2018.
 */
class UserNameView : LinearLayout {

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        View.inflate(context, R.layout.layout_user_name, this)

        // Load attributes
        val a = context.obtainStyledAttributes(attrs, R.styleable.UserNameView, defStyle, 0)
        if (a.hasValue(R.styleable.UserNameView_android_textColor)) {
            user_name_name?.setTextColor(a.getColor(R.styleable.UserNameView_android_textColor, ContextCompat.getColor(context, R.color.white)))
        }
        if (a.hasValue(R.styleable.UserNameView_android_textSize)) {
            user_name_name?.setTextSize(TypedValue.COMPLEX_UNIT_PX, a.getDimension(R.styleable.UserNameView_android_textSize, 14.0f))
        }
        a.recycle()
        if (!isInEditMode) {
            removeAllRoleViews()
        }
    }

    fun setText(text: String?) {
        user_name_name?.text = text
    }

    // ----------------------------------
    // USER TAGS HANDLING
    // ----------------------------------
    fun setRoles(roles: ArrayList<String>?) {
        removeAllRoleViews()
        roles?.forEach { role ->
            findByName(role)?.let {
                if (it.isVisible) {
                    addRoleView(it)
                }
            }
        }
    }

    private fun addRoleView(role: UserRole) {
        val userRoleView = UserRoleView(context)
        userRoleView.setRole(role)
        userRoleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.entourage_font_medium))
        user_name_tags_holder?.addView(userRoleView)
        user_name_tags_holder?.visibility = View.VISIBLE
    }

    private fun removeAllRoleViews() {
        user_name_tags_holder?.removeAllViews()
        user_name_tags_holder?.visibility = View.GONE
    }
}