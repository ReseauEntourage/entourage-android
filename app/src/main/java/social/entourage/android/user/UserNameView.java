package social.entourage.android.user;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import social.entourage.android.R;
import social.entourage.android.user.role.UserRole;
import social.entourage.android.user.role.RoleView;
import social.entourage.android.user.role.UserRolesFactory;

/**
 * Custom View used to display a text with optional tags
 * Created by Mihai Ionescu on 17/05/2018.
 */
public class UserNameView extends LinearLayout {

    private TextView nameTextView;
    private LinearLayout tagsHolder;

    public UserNameView(final Context context) {
        super(context);
        init(null, 0);
    }

    public UserNameView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public UserNameView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.layout_user_name, this);

        nameTextView = findViewById(R.id.user_name_name);
        tagsHolder = findViewById(R.id.user_name_tags_holder);

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.UserNameView, defStyle, 0);

        if (a.hasValue(R.styleable.UserNameView_android_textColor)) {
            nameTextView.setTextColor(a.getColor(R.styleable.UserNameView_android_textColor, ContextCompat.getColor(getContext(), R.color.white)));
        }
        if (a.hasValue(R.styleable.UserNameView_android_textSize)) {
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, a.getDimension(R.styleable.UserNameView_android_textSize, 14.0f));
        }

        a.recycle();

        if (!isInEditMode()) {
            removeAllRoleViews();
        }
    }

    public void setText(String text) {
        nameTextView.setText(text);
    }

    // ----------------------------------
    // USER TAGS HANDLING
    // ----------------------------------

    public void setRoles(ArrayList<String> roles) {
        removeAllRoleViews();
        if (roles == null) return;
        for (String role: roles) {
            UserRole userRole = UserRolesFactory.getInstance().findByName(role);
            if (userRole != null && userRole.isVisible()) {
                addRoleView(userRole);
            }
        }
    }

    public void addRoleView(UserRole role) {
        RoleView roleView = new RoleView(getContext());
        roleView.setRole(role);
        roleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.entourage_font_medium));
        tagsHolder.addView(roleView);
        tagsHolder.setVisibility(VISIBLE);
    }

    private void removeAllRoleViews() {
        tagsHolder.removeAllViews();
        tagsHolder.setVisibility(GONE);
    }

}
