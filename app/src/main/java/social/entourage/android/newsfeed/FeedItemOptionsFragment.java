package social.entourage.android.newsfeed;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.map.entourage.CreateEntourageFragment;


public class FeedItemOptionsFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.FeedItemOptions";

    private static final String KEY_FEEDITEM = "social.entourage.android.KEY_FEEDITEM";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private FeedItem feedItem;

    @Bind(R.id.feeditem_options)
    View optionsView;

    @Bind(R.id.feeditem_option_stop)
    Button stopButton;

    @Bind(R.id.feeditem_option_quit)
    Button quitButton;

    @Bind(R.id.feeditem_option_edit)
    Button editButton;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public FeedItemOptionsFragment() {
        // Required empty public constructor
    }

    public static FeedItemOptionsFragment newInstance(FeedItem feedItem) {
        FeedItemOptionsFragment fragment = new FeedItemOptionsFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_FEEDITEM, feedItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            feedItem = (FeedItem)getArguments().getSerializable(KEY_FEEDITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_feeditem_options, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeView();
    }

    private void initializeView() {
        if (feedItem == null) return;
        User me = EntourageApplication.me(getActivity());
        if (me == null || feedItem.getAuthor() == null) return;

        int myId = me.getId();
        if (feedItem.getAuthor().getUserID() != myId) {
            quitButton.setVisibility(View.VISIBLE);
        }
        else {
            stopButton.setVisibility(feedItem.isFreezed() ? View.GONE : View.VISIBLE);
            if (feedItem.isClosed() && feedItem.getType() == FeedItem.TOUR_CARD) {
                stopButton.setText(R.string.tour_info_options_freeze_tour);
            } else {
                stopButton.setText(R.string.tour_info_options_stop_tour);
            }
            if (feedItem.getType() == FeedItem.ENTOURAGE_CARD && FeedItem.STATUS_OPEN.equals(feedItem.getStatus())) {
                editButton.setVisibility(View.VISIBLE);
            }
        }
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------

    @OnClick({R.id.feeditem_option_cancel, R.id.feeditem_options})
    protected void onCancelClicked() {
        dismiss();
    }

    @OnClick(R.id.feeditem_option_stop)
    protected void onStopClicked() {

    }

    @OnClick(R.id.feeditem_option_quit)
    protected void onQuitClicked() {

    }

    @OnClick(R.id.feeditem_option_edit)
    protected void onEditClicked() {
        CreateEntourageFragment fragment = CreateEntourageFragment.newInstance((Entourage)feedItem);
        fragment.show(getFragmentManager(), CreateEntourageFragment.TAG);

        dismiss();
    }

}
