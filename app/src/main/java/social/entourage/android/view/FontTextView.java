package social.entourage.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import social.entourage.android.R;

public class FontTextView extends androidx.appcompat.widget.AppCompatTextView {
    public FontTextView(Context context) {
        super(context);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        buildAttrs(context, attrs);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        buildAttrs(context, attrs);
    }

    private void buildAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FontTextView, 0, 0);
        try {
            String fontName = typedArray.getString(R.styleable.FontTextView_fontName);
            setTypeface(Typeface.createFromAsset(context.getAssets(), fontName));
        } finally {
            typedArray.recycle();
        }
    }
}
