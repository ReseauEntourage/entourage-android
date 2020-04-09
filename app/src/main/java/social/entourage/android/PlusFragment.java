package social.entourage.android;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.AuthenticationController;

public class PlusFragment extends Fragment implements BackPressable{
    public static final String TAG = "social.entourage.android.fragment_plus";
    public static final String KEY_START_TOUR = "social.entourage.android.KEY_START_TOUR";
    public static final String KEY_ADD_ENCOUNTER = "social.entourage.android.KEY_ADD_ENCOUNTER";
    public static final String KEY_CREATE_DEMAND = "social.entourage.android.KEY_CREATE_DEMAND";
    public static final String KEY_CREATE_CONTRIBUTION = "social.entourage.android.KEY_CREATE_CONTRIBUTION";
    public static final String KEY_CREATE_OUTING = "social.entourage.android.KEY_CREATE_OUTING";

    @BindView(R.id.fragment_plus_overlay)
    protected RelativeLayout mapActionView;

    public PlusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();

        View addTourEncounterButton = getView().findViewById(R.id.layout_line_add_tour_encounter);
        View startTourButton = getView().findViewById(R.id.layout_line_start_tour_launcher);
        AuthenticationController authenticationController = EntourageApplication.get().getEntourageComponent().getAuthenticationController();
        if (authenticationController != null && authenticationController.getSavedTour()!=null) {
            if (addTourEncounterButton != null) addTourEncounterButton.setVisibility(View.VISIBLE);
            if (startTourButton != null) startTourButton.setVisibility(View.GONE);
        } else {
            User me = EntourageApplication.me(getActivity());
            boolean isPro = (me != null && me.isPro());

            if (addTourEncounterButton != null) addTourEncounterButton.setVisibility(View.GONE);
            if (startTourButton != null) startTourButton.setVisibility(isPro ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_plus, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @OnClick(R.id.plus_help_button)
    protected void onHelpButton() {
        if (getActivity() instanceof MainActivity) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_PLUS_NOT_READY);
            ((MainActivity) getActivity()).showWebViewForLinkId(Constants.SCB_LINK_ID);
        }
    }

    @OnClick(R.id.layout_line_create_entourage_ask_help)
    protected void onCreateEntourageHelpAction() {
        if(getActivity() instanceof MainActivity) {
            Intent newIntent = new Intent(getContext(), MainActivity.class);
            newIntent.setAction(KEY_CREATE_DEMAND);
            startActivity(newIntent);
            ((MainActivity)getActivity()).showFeed();
        }
    }


    @OnClick(R.id.layout_line_create_entourage_contribute)
    protected void onCreateEntourageContributionAction() {
        if(getActivity() instanceof MainActivity) {
            Intent newIntent = new Intent(getContext(), MainActivity.class);
            newIntent.setAction(KEY_CREATE_CONTRIBUTION);
            startActivity(newIntent);
            ((MainActivity)getActivity()).showFeed();
        }
    }

    @Optional
    @OnClick(R.id.layout_line_create_outing)
    protected void onCreateOuting() {
        if(getActivity() instanceof MainActivity) {Intent newIntent = new Intent(getContext(), MainActivity.class);
            newIntent.setAction(KEY_CREATE_OUTING);
            startActivity(newIntent);
            ((MainActivity)getActivity()).showFeed();
        }
    }

    @Optional
    @OnClick(R.id.layout_line_start_tour_launcher)
    public void onStartTourLauncher() {
        if(getActivity() instanceof MainActivity) {
            Intent newIntent = new Intent(getContext(), MainActivity.class);
            newIntent.setAction(KEY_START_TOUR);
            startActivity(newIntent);
            ((MainActivity)getActivity()).showFeed();
        }
    }

    @Optional
    @OnClick({R.id.layout_line_add_tour_encounter, R.id.map_longclick_button_create_encounter})
    public void onAddEncounter() {
        if(getActivity() instanceof MainActivity) {
            Intent newIntent = new Intent(getContext(), MainActivity.class);
            newIntent.setAction(KEY_ADD_ENCOUNTER);
            startActivity(newIntent);
            ((MainActivity)getActivity()).showFeed();
        }
    }

    @OnClick(R.id.fragment_plus_overlay)
    void onClickBackground() {
        onBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        if(getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showFeed();
        }
        return true;
    }
}
