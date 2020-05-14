package social.entourage.android.base;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

/**
 * Created by Mihai Ionescu on 11/04/2018.
 */
public class EntourageLinkMovementMethod extends LinkMovementMethod {

    private static EntourageLinkMovementMethod instance;

    public static MovementMethod getInstance() {
        if (instance == null) {
            instance = new EntourageLinkMovementMethod();
        }
        return instance;
    }

    @Override
    public boolean onTouchEvent(final TextView widget, final Spannable buffer, final MotionEvent event) {
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {

            final int x = (int) event.getX() - widget.getTotalPaddingLeft() + widget.getScrollX();
            final int y = (int) event.getY() - widget.getTotalPaddingTop() + widget.getScrollY();
            final Layout layout = widget.getLayout();
            final int line = layout.getLineForVertical(y);
            final int off = layout.getOffsetForHorizontal(line, x);
            final ClickableSpan[] links = buffer.getSpans(off, off, ClickableSpan.class);
            if (links.length != 0) {
                // to avoid double handling, we handle the link only on down
                if (action == MotionEvent.ACTION_DOWN) {
                    if (links[0] instanceof  URLSpan) {
                        String url = ((URLSpan) links[0]).getURL();
                        BusProvider.INSTANCE.getInstance().post(new Events.OnShowURLEvent(url));
                    }

                }
                return true;
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }

}
