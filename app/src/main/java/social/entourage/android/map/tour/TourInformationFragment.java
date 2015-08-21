package social.entourage.android.map.tour;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.TourTransportMode;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Tour;

public class TourInformationFragment extends DialogFragment {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    TourInformationPresenter presenter;

    @InjectView(R.id.tour_info_icon)
    ImageView tourIcon;

    @InjectView(R.id.tour_info_date)
    TextView tourDate;

    @InjectView(R.id.tour_info_transport)
    TextView tourTransport;

    @InjectView(R.id.tour_info_type)
    TextView tourType;

    @InjectView(R.id.tour_info_status_ongoing)
    TextView tourStatusOnGoing;

    @InjectView(R.id.tour_info_status_closed)
    TextView tourStatusClosed;

    @InjectView(R.id.tour_info_button_close)
    Button closeButton;

    Tour tour;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public static TourInformationFragment newInstance(Tour tour) {
        TourInformationFragment fragment = new TourInformationFragment();
        Bundle args = new Bundle();
        args.putSerializable(Tour.KEY_TOUR, tour);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View toReturn = inflater.inflate(R.layout.fragment_tour_information, container, false);
        ButterKnife.inject(this, toReturn);
        initializeView();
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerTourInformationComponent.builder()
                .entourageComponent(entourageComponent)
                .tourInformationModule(new TourInformationModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof  OnTourInformationFragmentFinish)) {
            throw new ClassCastException(activity.toString() + " must implement OnTourInformationFragmentFinish");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentFade;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @OnClick(R.id.tour_info_button_close)
    void closeFragment() {
        getOnTourInformationFragmentFinish().closeTourInformationFragment(this);
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeView() {
        Resources res = getResources();
        tour = (Tour) getArguments().getSerializable(Tour.KEY_TOUR);
        String vehicule = tour.getTourVehicleType();
        String type = tour.getTourType();
        String status = tour.getTourStatus();

        if (!tour.getTourPoints().isEmpty()) {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String date = dateFormat.format(tour.getTourPoints().get(0).getPassingTime());
            tourDate.setText(date);
        }

        if (vehicule.equals(TourTransportMode.FEET.getName())) {
            tourIcon.setImageResource(R.drawable.ic_feet);
            tourTransport.setText(res.getString(R.string.tour_info_text_transport, getString(R.string.tour_check_feet)));
        }
        else if (vehicule.equals(TourTransportMode.CAR.getName())) {
            tourIcon.setImageResource(R.drawable.ic_car);
            tourTransport.setText(res.getString(R.string.tour_info_text_transport, getString(R.string.tour_check_car)));
        }

        if (type.equals(TourType.SOCIAL.getName())) {
            tourType.setText(res.getString(R.string.tour_info_text_type, getString(R.string.tour_type_bare_hands)));
        }
        else if (type.equals(TourType.FOOD.getName())) {
            tourType.setText(res.getString(R.string.tour_info_text_type, getString(R.string.tour_type_alimentary)));
        }
        else if (type.equals(TourType.OTHER.getName())) {
            tourType.setText(res.getString(R.string.tour_info_text_type, getString(R.string.tour_type_alimentary)));
        }

        if (status.equals(Tour.TOUR_ON_GOING)) {
            tourStatusClosed.setVisibility(View.GONE);
        }
        else if (status.equals(Tour.TOUR_CLOSED)) {
            tourStatusOnGoing.setVisibility(View.GONE);
        }
    }

    private OnTourInformationFragmentFinish getOnTourInformationFragmentFinish() {
        final Activity activity = getActivity();
        return activity != null ? (OnTourInformationFragmentFinish) activity : null;
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public interface OnTourInformationFragmentFinish {
        void closeTourInformationFragment(TourInformationFragment fragment);
    }
}
