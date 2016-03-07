package social.entourage.android.map.tour.join;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Tour;

public class TourJoinRequestFragment extends DialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "tour_join_request_message";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Bind(R.id.tour_join_request_message)
    EditText messageView;

    // ----------------------------------
    // PRIVATE MEMBERS
    // ----------------------------------

    private Tour tour;

    @Inject
    TourJoinRequestPresenter presenter;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public TourJoinRequestFragment() {
        // Required empty public constructor
    }


    public static TourJoinRequestFragment newInstance(Tour tour) {
        TourJoinRequestFragment fragment = new TourJoinRequestFragment();
        Bundle args = new Bundle();
        args.putSerializable(Tour.KEY_TOUR, tour);
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

        tour = (Tour) getArguments().getSerializable(Tour.KEY_TOUR);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tour_join_request, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
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
        presenter.sendMessage(messageView.getText().toString(), tour);
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerTourJoinRequestComponent.builder()
                .entourageComponent(entourageComponent)
                .tourJoinRequestModule(new TourJoinRequestModule(this))
                .build()
                .inject(this);
    }
}
