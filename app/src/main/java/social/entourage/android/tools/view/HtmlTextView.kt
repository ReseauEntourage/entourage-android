package social.entourage.android.view

import android.content.Context
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import social.entourage.android.R
import social.entourage.android.tools.Utils

class HtmlTextView : AppCompatTextView {
    private var htmlString: String? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        buildAttrs(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        buildAttrs(context, attrs)
    }

    private fun buildAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.HtmlTextView, 0, 0)
        try {
            typedArray.getString(R.styleable.HtmlTextView_htmlText)?.let {
                htmlString = it
                setText(Utils.fromHtml(it), BufferType.SPANNABLE)
                movementMethod = LinkMovementMethod.getInstance()
            } ?: run {
                htmlString = null
            }
        } finally {
            typedArray.recycle()
        }
    }

    fun setHtmlString(resourceID: Int) {
        setHtmlString(resources.getString(resourceID))
    }

    fun setHtmlString(htmlString: String?) {
        setHtmlString(htmlString, LinkMovementMethod.getInstance())
    }

    fun setHtmlString(newHtmlString: String?, movementMethod: MovementMethod?) {
        this.htmlString = newHtmlString ?: ""
        setText(Utils.fromHtml(htmlString ?: ""), BufferType.SPANNABLE)
        this.movementMethod = movementMethod
    }
}