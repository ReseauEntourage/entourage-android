package social.entourage.android.user;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
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

    private LinearLayout tagsLeftHolder;
    private LinearLayout tagsRightHolder;
    private TextView nameTextView;

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

        tagsLeftHolder = findViewById(R.id.user_name_tags_left_holder);
        tagsRightHolder = findViewById(R.id.user_name_tags_right_holder);
        nameTextView = findViewById(R.id.user_name_name);

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
        if (roles != null) {
            for (int i = 0; i < roles.size(); i++) {
                String role = roles.get(i);
                UserRole userRole = UserRolesFactory.getInstance().findByName(role);
                if (userRole != null && userRole.isVisible()) {
                    addRoleView(userRole);
                }
            }
        }
    }

    public void addRoleView(UserRole role) {
        RoleView roleView = new RoleView(getContext());
        roleView.setText(role.getNameResourceId());
        roleView.setTextColor(ContextCompat.getColor(getContext(), R.color.profile_role_text));
        roleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.entourage_font_medium));
        roleView.changeBackgroundColor(role.getColorResourceId());

        LinearLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (role.getPosition() == UserRole.Position.LEFT) {
            lp.rightMargin = (int)getResources().getDimension(R.dimen.role_margin);
            tagsLeftHolder.addView(roleView, lp);
        } else {
            lp.leftMargin = (int)getResources().getDimension(R.dimen.role_margin);
            tagsRightHolder.addView(roleView, lp);
        }
    }

    private void removeAllRoleViews() {
        tagsLeftHolder.removeAllViews();
        tagsRightHolder.removeAllViews();
    }

}
