package social.entourage.android.tools

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.MotionEvent
import android.widget.TextView
import social.entourage.android.old_v7.MainActivity_v7

/**
 * Created by Mihai Ionescu on 11/04/2018.
 */
object EntLinkMovementMethod : LinkMovementMethod() {
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
            val x = event.x.toInt() - widget.totalPaddingLeft + widget.scrollX
            val y = event.y.toInt() - widget.totalPaddingTop + widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())
            val links = buffer.getSpans(off, off, ClickableSpan::class.java)
            if (links.isNotEmpty()) {
                // to avoid double handling, we handle the link only on down
                if (action == MotionEvent.ACTION_DOWN) {
                    if (links[0] is URLSpan) {
                        (links[0] as URLSpan).url?.let { url ->
                            (widget.context as? MainActivity_v7)?.showWebView(url)
                        }
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }
}