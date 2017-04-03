package social.entourage.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

import social.entourage.android.R;

public class HtmlTextView extends android.support.v7.widget.AppCompatTextView {

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

    public void setHtmlString(int resourceID) {
        setHtmlString(getResources().getString(resourceID));
    }

    public void setHtmlString(String htmlString) {
        if (htmlString == null) htmlString = "";
        setText(Html.fromHtml(htmlString), BufferType.SPANNABLE);
        this.htmlString = htmlString;
    }
}
