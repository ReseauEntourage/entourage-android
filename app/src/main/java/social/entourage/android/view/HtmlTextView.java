package social.entourage.android.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

import social.entourage.android.R;

public class HtmlTextView extends TextView {

    private String htmlString;

    public HtmlTextView(Context context) {
        super(context);
    }

    public HtmlTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        buildAttrs(context, attrs);
    }

    public HtmlTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        buildAttrs(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HtmlTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        buildAttrs(context, attrs);
    }

    private void buildAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HtmlTextView, 0, 0);
        try {
            htmlString = typedArray.getString(R.styleable.HtmlTextView_htmlText);
            if (htmlString != null) {
                setText(Html.fromHtml(htmlString), TextView.BufferType.SPANNABLE);
            }
        } finally {
            typedArray.recycle();
        }
    }
}
