package social.entourage.android.user;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import social.entourage.android.R;
import social.entourage.android.api.model.User;

/**
 * Custom View used to display an user name with an optional tag
 * Created by Mihai Ionescu on 17/05/2018.
 */
public class UserNameView extends LinearLayout {

    private TextView tagTextView;
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

        tagTextView = findViewById(R.id.user_name_tag);
        nameTextView = findViewById(R.id.user_name_name);
    }

    public void setText(String text) {
        nameTextView.setText(text);
    }

    public void setTag(String tag) {
        tagTextView.setText(tag);
        if (tag == null) {
            tagTextView.setBackgroundResource(0);
        }
        else if (User.TAG_PENDING.equalsIgnoreCase(tag)) {
            tagTextView.setBackgroundResource(R.drawable.profile_tag_pending);
        }
        else if (User.TAG_ACCEPTED.equalsIgnoreCase(tag)) {
            tagTextView.setBackgroundResource(R.drawable.profile_tag_accepted);
        }
        else {
            tagTextView.setBackgroundResource(0);
        }
    }

}
