package social.entourage.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import social.entourage.android.R;
import social.entourage.android.tools.Utils;

public class HtmlTextView extends androidx.appcompat.widget.AppCompatTextView {

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
                setText(Utils.fromHtml(htmlString), TextView.BufferType.SPANNABLE);
                setMovementMethod(LinkMovementMethod.getInstance());
            }
        } finally {
            typedArray.recycle();
        }
    }

    public void setHtmlString(int resourceID) {
        setHtmlString(getResources().getString(resourceID));
    }

    public void setHtmlString(String htmlString) {
        setHtmlString(htmlString, LinkMovementMethod.getInstance());
    }

    public void setHtmlString(String htmlString, MovementMethod movementMethod) {
        if (htmlString == null) htmlString = "";
        setText(Utils.fromHtml(htmlString), BufferType.SPANNABLE);
        this.htmlString = htmlString;
        setMovementMethod(movementMethod);
    }
}
