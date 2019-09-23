package social.entourage.android.tour.join;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
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
        args.putSerializable(FeedItem.KEY_FEEDITEM, feedItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            feedItem = (FeedItem)getArguments().getSerializable(FeedItem.KEY_FEEDITEM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        View view = inflater.inflate(R.layout.fragment_tour_join_request_ok, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------

    @OnClick(R.id.tour_join_request_ok_x_button)
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
