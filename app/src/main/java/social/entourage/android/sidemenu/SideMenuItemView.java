package social.entourage.android.sidemenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
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
                ImageView divider = (ImageView) findViewById(R.id.side_menu_item_divider);
                divider.setVisibility(View.GONE);
            }

            //Icon
            ImageView iconView = (ImageView) findViewById(R.id.side_menu_item_icon);
            int iconResourceID = styledAttributes.getResourceId(R.styleable.SideMenuItemView_android_icon, 0);
            if (iconResourceID == 0) {
                iconView.setVisibility(View.GONE);
            } else {
                iconView.setImageResource(iconResourceID);
            }

            //Title
            TextView titleView = (TextView) findViewById(R.id.side_menu_item_title);
            String title = styledAttributes.getString(R.styleable.SideMenuItemView_android_title);
            titleView.setText(title);

            int defaultTextColor = titleView.getCurrentTextColor();
            int textColor = styledAttributes.getResourceId(R.styleable.SideMenuItemView_android_textColor, defaultTextColor);
            if (textColor != defaultTextColor) {
                titleView.setTextColor(ContextCompat.getColor(getContext(), textColor));
            }

            boolean centerText = styledAttributes.getBoolean(R.styleable.SideMenuItemView_centerText, false);
            if (centerText) {
                titleView.setGravity(Gravity.CENTER);
            }
        }
        finally {
            styledAttributes.recycle();
        }
    }

}
