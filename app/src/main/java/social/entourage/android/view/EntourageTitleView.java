package social.entourage.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import social.entourage.android.R;

/**
 * Custom title view with a close button and a title
 */
public class EntourageTitleView extends RelativeLayout {

    private TextView titleTextView;
    private ImageButton closeButton;
    private TextView actionButton;

    private String mTitle;
    private Drawable mCloseButtonDrawable;

    public EntourageTitleView(Context context) {
        super(context);
        init(null, 0);
    }

    public EntourageTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EntourageTitleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.layout_view_title, this);

        titleTextView = findViewById(R.id.title_text);
        closeButton = findViewById(R.id.title_close_button);
        actionButton = findViewById(R.id.title_action_button);
        View separator = findViewById(R.id.title_separator);

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.EntourageTitleView, defStyle, 0);

        mTitle = a.getString(
                R.styleable.EntourageTitleView_entourageTitle);
        titleTextView.setText(mTitle);

        if (a.hasValue(R.styleable.EntourageTitleView_android_textColor)) {
            titleTextView.setTextColor(a.getColor(R.styleable.EntourageTitleView_android_textColor, getResources().getColor(R.color.greyish_brown)));
        }

        if (a.hasValue(R.styleable.EntourageTitleView_entourageTitleCloseDrawable)) {
            mCloseButtonDrawable = a.getDrawable(
                    R.styleable.EntourageTitleView_entourageTitleCloseDrawable);
            closeButton.setImageDrawable(mCloseButtonDrawable);
        }

        actionButton.setText(a.getString(R.styleable.EntourageTitleView_entourageTitleAction));

        setBackgroundResource(a.getResourceId(R.styleable.EntourageTitleView_android_background, R.color.background));

        boolean showSeparator = a.getBoolean(R.styleable.EntourageTitleView_entourageShowSeparator, true);
        separator.setVisibility(showSeparator ? VISIBLE : GONE);

        a.recycle();
    }

    public void setTitle(final String mTitle) {
        this.mTitle = mTitle;
        titleTextView.setText(mTitle);
    }
}
