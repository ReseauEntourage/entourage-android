package social.entourage.android.sidemenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import social.entourage.android.R;

/**
 * Class that controls a sidemenu item
 */
public class SideMenuItemView extends RelativeLayout {

    private TextView titleView = null;

    public SideMenuItemView(Context context) {
        super(context);
        init(null, 0);
    }

    public SideMenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SideMenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.layout_side_menu_item, this);
        // Load attributes
        final TypedArray styledAttributes = getContext().obtainStyledAttributes(
                attrs, R.styleable.SideMenuItemView, defStyle, 0);

        try {
            //Handle divider
            boolean bShowDivider = styledAttributes.getBoolean(R.styleable.SideMenuItemView_showDivider, true);
            if (!bShowDivider) {
                ImageView divider = findViewById(R.id.side_menu_item_divider);
                divider.setVisibility(View.GONE);
            }

            //Icon
            ImageView iconView = findViewById(R.id.side_menu_item_icon);
            int iconResourceID = styledAttributes.getResourceId(R.styleable.SideMenuItemView_android_icon, 0);
            if (iconResourceID == 0) {
                iconView.setVisibility(View.GONE);
            } else {
                iconView.setImageResource(iconResourceID);
            }

            int iconTint = styledAttributes.getColor(R.styleable.SideMenuItemView_iconTint, 0);
            if (iconTint != 0) {
                ImageViewCompat.setImageTintList(iconView, ColorStateList.valueOf(iconTint));
            }

            //Title
            titleView = findViewById(R.id.side_menu_item_title);
            String title = styledAttributes.getString(R.styleable.SideMenuItemView_android_title);
            titleView.setText(title);

            int defaultTextColor = titleView.getCurrentTextColor();
            int textColor = styledAttributes.getResourceId(R.styleable.SideMenuItemView_android_textColor, defaultTextColor);
            if (textColor != defaultTextColor) {
                titleView.setTextColor(ContextCompat.getColor(getContext(), textColor));
            }

            float textSize = styledAttributes.getDimensionPixelSize(R.styleable.SideMenuItemView_android_textSize, 0);
            if (textSize > 0) {
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }

            int backgroundResourceID = styledAttributes.getResourceId(R.styleable.SideMenuItemView_textBackground, 0);
            if (backgroundResourceID != 0) {
                titleView.setBackgroundResource(backgroundResourceID);
            }

            boolean centerText = styledAttributes.getBoolean(R.styleable.SideMenuItemView_centerText, false);
            if (centerText) {
                titleView.setGravity(Gravity.CENTER);
            }

            //Right arrow
            boolean bShowRightArrow = styledAttributes.getBoolean(R.styleable.SideMenuItemView_showRightArrow, true);
            if (!bShowRightArrow) {
                ImageView rightArrow = findViewById(R.id.side_menu_item_arrow);
                if (rightArrow != null) rightArrow.setVisibility(GONE);
            }
        }
        finally {
            styledAttributes.recycle();
        }
    }

    public void setTitle(@StringRes int resid) {
        if (titleView != null) {
            titleView.setText(resid);
        }
    }

}
