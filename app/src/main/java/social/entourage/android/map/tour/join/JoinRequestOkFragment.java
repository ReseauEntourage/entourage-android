package social.entourage.android.map.tour.join;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.R;

public class JoinRequestOkFragment extends DialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "tour_join_request_ok";

    private static final String KEY_FEED_ITEM = "social.entourage.android.KEY_FEEDITEM";

    // ----------------------------------
    // PRIVATE MEMBERS
    // ----------------------------------

    private FeedItem feedItem;

    private OnFragmentInteractionListener mListener;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public JoinRequestOkFragment() {
        // Required empty public constructor
    }

    public static JoinRequestOkFragment newInstance(FeedItem feedItem) {
        JoinRequestOkFragment fragment = new JoinRequestOkFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_FEED_ITEM, feedItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            feedItem = (FeedItem)getArguments().getSerializable(KEY_FEED_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.fragment_tour_join_request_ok, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------

    @OnClick({R.id.tour_join_request_ok_close_button, R.id.tour_join_request_ok_x_button})
    protected void onCloseClicked() {
        dismiss();
    }

    @OnClick(R.id.tour_join_request_ok_message_button)
    protected void onMessageClicked() {
        TourJoinRequestFragment joinRequestFragment = TourJoinRequestFragment.newInstance(feedItem);
        joinRequestFragment.show(getFragmentManager(), TourJoinRequestFragment.TAG);

        dismiss();
    }

    public interface OnFragmentInteractionListener {
        void onShowJoinRequestMessageDialog(FeedItem feedItem);
    }
}
