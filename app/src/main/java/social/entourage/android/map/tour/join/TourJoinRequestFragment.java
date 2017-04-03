package social.entourage.android.map.tour.join;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.flurry.android.FlurryAgent;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;

public class TourJoinRequestFragment extends DialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "tour_join_request_message";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.tour_join_request_message)
    EditText messageView;

    // ----------------------------------
    // PRIVATE MEMBERS
    // ----------------------------------

    private FeedItem feedItem;

    @Inject
    TourJoinRequestPresenter presenter;

    private boolean startedTyping = false;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public TourJoinRequestFragment() {
        // Required empty public constructor
    }


    public static TourJoinRequestFragment newInstance(FeedItem feedItem) {
        TourJoinRequestFragment fragment = new TourJoinRequestFragment();
        Bundle args = new Bundle();
        args.putSerializable(Tour.KEY_TOUR, feedItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        feedItem = (FeedItem) getArguments().getSerializable(Tour.KEY_TOUR);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tour_join_request_message, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());

        messageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() > 0 && !startedTyping) {
                    FlurryAgent.logEvent(Constants.EVENT_JOIN_REQUEST_START);
                    startedTyping = true;
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @OnClick(R.id.tour_join_request_message_send)
    protected void onMessageSend() {
        if (presenter != null && messageView != null) {
            if ( feedItem != null && (feedItem.getType() == FeedItem.TOUR_CARD || feedItem.getType() == FeedItem.ENTOURAGE_CARD) ) {
                FlurryAgent.logEvent(Constants.EVENT_JOIN_REQUEST_SUBMIT);
                presenter.sendMessage(messageView.getText().toString(), feedItem);
            }
            else {
                dismiss();
            }
        }
    }

    @OnClick(R.id.tour_join_request_close)
    protected void onClosePressed() {
        dismiss();
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerTourJoinRequestComponent.builder()
                .entourageComponent(entourageComponent)
                .tourJoinRequestModule(new TourJoinRequestModule(this))
                .build()
                .inject(this);
    }
}
