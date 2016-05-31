package social.entourage.android.badge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import social.entourage.android.R;

/**
 * ImageView with badge count
 */
public class BadgeView extends RelativeLayout {

    private TextView badgeCountView;

    public BadgeView(Context context) {
        super(context);
        init(null, 0);
    }

    public BadgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public BadgeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.layout_badge, this);
        //init fields
        badgeCountView = (TextView)this.findViewById(R.id.badge_count);
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.BadgeView, defStyle, 0);

        if (a.hasValue(R.styleable.BadgeView_android_icon)) {
            Drawable iconSource = a.getDrawable(
                    R.styleable.BadgeView_android_icon);
            ImageView badgeIcon = (ImageView) this.findViewById(R.id.badge_icon);
            badgeIcon.setImageDrawable(iconSource);
        }

        a.recycle();

    }

    public void setBadgeCount(int badgeCount) {
        if (badgeCount <= 0) {
            badgeCountView.setVisibility(GONE);
        }
        else {
            badgeCountView.setText("" + badgeCount);
            if (badgeCountView.getVisibility() == GONE) {
                badgeCountView.setVisibility(VISIBLE);
            }
        }
    }

}
