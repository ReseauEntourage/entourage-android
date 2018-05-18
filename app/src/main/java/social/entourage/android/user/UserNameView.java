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
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.UserRole;
import social.entourage.android.user.role.RoleView;

/**
 * Custom View used to display an user name with optional tags
 * Created by Mihai Ionescu on 17/05/2018.
 */
public class UserNameView extends LinearLayout {

    private LinearLayout contentHolder;
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

        contentHolder = findViewById(R.id.user_name_holder);
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
            int index = 0;
            for (int i = 0; i < roles.size(); i++) {
                String role = roles.get(i);
                UserRole userRole = UserRole.findByName(role);
                if (userRole != null) {
                    addRoleView(getContext().getString(userRole.getResourceId()), index);
                    index++;
                }
            }
        }
    }

    public void addRoleView(String role) {
        addRoleView(role, 0);
    }

    public void addRoleView(String role, int index) {
        RoleView roleView = new RoleView(getContext());
        roleView.setText(role);
        roleView.setTextColor(ContextCompat.getColor(getContext(), R.color.profile_role_text));
        roleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.entourage_font_medium));
        if (User.ROLE_PENDING.equalsIgnoreCase(role)) {
            roleView.changeBackgroundColor(R.color.profile_role_pending);
        }
        else {
            roleView.changeBackgroundColor(R.color.profile_role_accepted);
        }

        LinearLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = (int)getResources().getDimension(R.dimen.role_margin);
        contentHolder.addView(roleView, index, lp);
    }

    private void removeAllRoleViews() {
        contentHolder.removeViews(0, contentHolder.getChildCount()-1);
    }

}
