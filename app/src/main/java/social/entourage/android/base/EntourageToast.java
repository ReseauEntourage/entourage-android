package social.entourage.android.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * Created by Mihai Ionescu on 13/10/2017.
 */

public class EntourageToast {

    /**
     * Keeps track of certain Boast notifications that may need to be cancelled. This functionality
     * is only offered by some of the methods in this class.
     * <p>
     * Uses a {@link WeakReference} to avoid leaking the activity context used to show the original {@link Toast}.
     */
    @Nullable
    private volatile static WeakReference<EntourageToast> weakEntourageToast = null;

    @Nullable
    public static EntourageToast getGlobalEntourageToast() {
        if (weakEntourageToast == null) {
            return null;
        }

        return weakEntourageToast.get();
    }

    private static void setGlobalEntourageToast(@Nullable EntourageToast globalEntourageToast) {
        EntourageToast.weakEntourageToast = new WeakReference<>(globalEntourageToast);
    }


    // ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Internal reference to the {@link Toast} object that will be displayed.
     */
    private Toast internalToast;

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Private constructor creates a new {@link EntourageToast} from a given {@link Toast}.
     *
     * @throws NullPointerException if the parameter is <code>null</code>.
     */
    private EntourageToast(Toast toast) {
        // null check
        if (toast == null) {
            throw new NullPointerException("Boast.Boast(Toast) requires a non-null parameter.");
        }

        internalToast = toast;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Make a standard {@link EntourageToast} that just contains a text view.
     *
     * @param context  The context to use. Usually your {@link android.app.Application} or
     *                 {@link android.app.Activity} object.
     * @param text     The text to show. Can be formatted text.
     * @param duration How long to display the message. Either {@link Toast#LENGTH_SHORT} or
     *                 {@link Toast#LENGTH_LONG}
     */
    @SuppressLint("ShowToast")
    public static EntourageToast makeText(Context context, CharSequence text, int duration) {
        return new EntourageToast(Toast.makeText(context, text, duration));
    }

    /**
     * Make a standard {@link EntourageToast} that just contains a text view with the text from a resource.
     *
     * @param context  The context to use. Usually your {@link android.app.Application} or
     *                 {@link android.app.Activity} object.
     * @param resId    The resource id of the string resource to use. Can be formatted text.
     * @param duration How long to display the message. Either {@link Toast#LENGTH_SHORT} or
     *                 {@link Toast#LENGTH_LONG}
     * @throws Resources.NotFoundException if the resource can't be found.
     */
    @SuppressLint("ShowToast")
    public static EntourageToast makeText(Context context, int resId, int duration)
            throws Resources.NotFoundException {
        return new EntourageToast(Toast.makeText(context, resId, duration));
    }

    /**
     * Make a standard {@link EntourageToast} that just contains a text view. Duration defaults to
     * {@link Toast#LENGTH_SHORT}.
     *
     * @param context The context to use. Usually your {@link android.app.Application} or
     *                {@link android.app.Activity} object.
     * @param text    The text to show. Can be formatted text.
     */
    @SuppressLint("ShowToast")
    public static EntourageToast makeText(Context context, CharSequence text) {
        return new EntourageToast(Toast.makeText(context, text, Toast.LENGTH_SHORT));
    }

    /**
     * Make a standard {@link EntourageToast} that just contains a text view with the text from a resource.
     * Duration defaults to {@link Toast#LENGTH_SHORT}.
     *
     * @param context The context to use. Usually your {@link android.app.Application} or
     *                {@link android.app.Activity} object.
     * @param resId   The resource id of the string resource to use. Can be formatted text.
     * @throws Resources.NotFoundException if the resource can't be found.
     */
    @SuppressLint("ShowToast")
    public static EntourageToast makeText(Context context, int resId) throws Resources.NotFoundException {
        return new EntourageToast(Toast.makeText(context, resId, Toast.LENGTH_SHORT));
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Show a standard {@link EntourageToast} that just contains a text view.
     *
     * @param context  The context to use. Usually your {@link android.app.Application} or
     *                 {@link android.app.Activity} object.
     * @param text     The text to show. Can be formatted text.
     * @param duration How long to display the message. Either {@link Toast#LENGTH_SHORT} or
     *                 {@link Toast#LENGTH_LONG}
     */
    public static void showText(Context context, CharSequence text, int duration) {
        EntourageToast.makeText(context, text, duration).show();
    }

    /**
     * Show a standard {@link EntourageToast} that just contains a text view with the text from a resource.
     *
     * @param context  The context to use. Usually your {@link android.app.Application} or
     *                 {@link android.app.Activity} object.
     * @param resId    The resource id of the string resource to use. Can be formatted text.
     * @param duration How long to display the message. Either {@link Toast#LENGTH_SHORT} or
     *                 {@link Toast#LENGTH_LONG}
     * @throws Resources.NotFoundException if the resource can't be found.
     */
    public static void showText(Context context, int resId, int duration)
            throws Resources.NotFoundException {
        EntourageToast.makeText(context, resId, duration).show();
    }

    /**
     * Show a standard {@link EntourageToast} that just contains a text view. Duration defaults to
     * {@link Toast#LENGTH_SHORT}.
     *
     * @param context The context to use. Usually your {@link android.app.Application} or
     *                {@link android.app.Activity} object.
     * @param text    The text to show. Can be formatted text.
     */
    public static void showText(Context context, CharSequence text) {
        EntourageToast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show a standard {@link EntourageToast} that just contains a text view with the text from a resource.
     * Duration defaults to {@link Toast#LENGTH_SHORT}.
     *
     * @param context The context to use. Usually your {@link android.app.Application} or
     *                {@link android.app.Activity} object.
     * @param resId   The resource id of the string resource to use. Can be formatted text.
     * @throws Resources.NotFoundException if the resource can't be found.
     */
    public static void showText(Context context, int resId) throws Resources.NotFoundException {
        EntourageToast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Close the view if it's showing, or don't show it if it isn't showing yet. You do not normally
     * have to call this. Normally view will disappear on its own after the appropriate duration.
     */
    public void cancel() {
        internalToast.cancel();
    }

    /**
     * Show the view for the specified duration. By default, this method cancels any current
     * notification to immediately display the new one. For conventional {@link Toast#show()}
     * queueing behaviour, use method {@link #show(boolean)}.
     *
     * @see #show(boolean)
     */
    public void show() {
        show(true);
    }

    /**
     * Show the view for the specified duration. This method can be used to cancel the current
     * notification, or to queue up notifications.
     *
     * @param cancelCurrent <code>true</code> to cancel any current notification and replace it with this new
     *                      one
     * @see #show()
     */
    public void show(boolean cancelCurrent) {
        // cancel current
        if (cancelCurrent) {
            final EntourageToast cachedGlobalToast = getGlobalEntourageToast();
            if ((cachedGlobalToast != null)) {
                cachedGlobalToast.cancel();
            }
        }

        // save an instance of this current notification
        setGlobalEntourageToast(this);

        internalToast.show();
    }

}
