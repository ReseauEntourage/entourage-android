package social.entourage.android.user.role;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import social.entourage.android.R;

/**
 * Custom AppCompatTextView with a rounded rectangle background
 * Used to display a role
 * Created by Mihai Ionescu on 18/05/2018.
 */
public class RoleView extends AppCompatTextView {

    private float backgroundCornerRadius = 5.0f;
    private @ColorRes int backgroundColor;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public RoleView(final Context context) {
        super(context);
        init(null, 0);
    }

    public RoleView(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public RoleView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        backgroundCornerRadius = getResources().getDimension(R.dimen.role_corner_radius);
        backgroundColor = R.color.white;
        setCustomBackground();
    }

    // ----------------------------------
    // BACKGROUND
    // ----------------------------------

    public void changeBackgroundColor(@ColorRes int backgroundColor) {
        this.backgroundColor = backgroundColor;
        setCustomBackground();
    }

    private void setCustomBackground() {
        GradientDrawable background = new GradientDrawable();

        background.setColor(ContextCompat.getColor(getContext(), backgroundColor));
        background.setCornerRadius(backgroundCornerRadius);
        setPadding((int)backgroundCornerRadius, (int)getResources().getDimension(R.dimen.role_padding_top), (int)backgroundCornerRadius, (int)getResources().getDimension(R.dimen.role_padding_bottom));

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(background);
        } else {
            setBackground(background);
        }
    }

}
