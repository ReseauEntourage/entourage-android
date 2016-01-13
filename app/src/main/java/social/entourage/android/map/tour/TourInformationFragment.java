package social.entourage.android.map.tour;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageApplication;
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

    @Bind(R.id.tour_info_icon)
    ImageView tourIcon;

    @Bind(R.id.tour_info_date)
    TextView tourDate;

    @Bind(R.id.tour_info_duration)
    TextView tourDuration;

    @Bind(R.id.tour_info_organization)
    TextView tourOrganization;

    @Bind(R.id.tour_info_transport)
    TextView tourTransport;

    @Bind(R.id.tour_info_type)
    TextView tourType;

    @Bind(R.id.tour_info_status_ongoing)
    TextView tourStatusOnGoing;

    @Bind(R.id.tour_info_status_closed)
    TextView tourStatusClosed;

    @Bind(R.id.tour_info_button_close)
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
        super.onCreateView(inflater, container, savedInstanceState);
        View toReturn = inflater.inflate(R.layout.fragment_tour_information, container, false);
        ButterKnife.bind(this, toReturn);
        initializeView();
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
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

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

        if (tour.getStartTime() != null && tour.getEndTime() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH'h'mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+1"));
            tourDuration.setText(res.getString(R.string.tour_info_text_duration, dateFormat.format(tour.getStartTime()), dateFormat.format(tour.getEndTime())));
        } else {
            tourDuration.setText(res.getString(R.string.tour_info_text_duration, "", ""));
        }

        tourOrganization.setText(tour.getOrganizationName());

        if (vehicule != null) {
            if (vehicule.equals(TourTransportMode.FEET.getName())) {
                tourTransport.setText(getString(R.string.tour_check_feet));
            } else if (vehicule.equals(TourTransportMode.CAR.getName())) {
                tourTransport.setText(getString(R.string.tour_check_car));
            }
        } else {
            tourTransport.setText(getString(R.string.tour_info_unknown));
        }

        if (type != null) {
            if (type.equals(TourType.MEDICAL.getName())) {
                tourType.setText(getString(R.string.tour_type_medical));
            } else if (type.equals(TourType.ALIMENTARY.getName())) {
                tourType.setText(getString(R.string.tour_type_alimentary));
            } else if (type.equals(TourType.BARE_HANDS.getName())) {
                tourType.setText(getString(R.string.tour_type_bare_hands));
            }
        } else {
            tourType.setText(getString(R.string.tour_info_unknown));
        }

        if (status != null) {
            if (status.equals(Tour.TOUR_ON_GOING)) {
                tourStatusClosed.setVisibility(View.GONE);
            } else if (status.equals(Tour.TOUR_CLOSED)) {
                tourStatusOnGoing.setVisibility(View.GONE);
            }
        } else {
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
