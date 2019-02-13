package social.entourage.android.map.entourage;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;

import social.entourage.android.R;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

/**
 * Created by Mihai Ionescu on 03/08/2018.
 */
public class EntourageCloseFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = EntourageCloseFragment.class.getSimpleName();

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private FeedItem feedItem;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public EntourageCloseFragment() {
        // Required empty public constructor
    }

    public static EntourageCloseFragment newInstance(FeedItem feedItem) {
        EntourageCloseFragment fragment = new EntourageCloseFragment();
        fragment.feedItem = feedItem;
        return fragment;
    }

    public void show(FragmentManager fragmentManager, String tag, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(R.string.entourage_close_alert_description)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        BusProvider.getInstance().post(new Events.OnFeedItemCloseRequestEvent(feedItem, false, true));
                    }
                })
                .setNegativeButton(R.string.no, null);

        builder.create().show();
    }

}
