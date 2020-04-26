package social.entourage.android.map.filter;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.tools.BusProvider;

public abstract class BaseMapFilterFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage_android.MapFilterFragment";

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public BaseMapFilterFragment() {
        // Required empty public constructor
    }

    public static MapFilterFragment newInstance() {
        MapFilterFragment fragment = new MapFilterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map_filter, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeView();
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_CLOSE);
        dismiss();
    }

    @OnClick(R.id.title_action_button)
    protected void onValidateClicked() {
        // save the values to the filter
        saveFilter();

        // inform the map screen to refresh the newsfeed
        BusProvider.getInstance().post(new Events.OnMapFilterChanged());

        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_FILTER_SUBMIT);

        // dismiss the dialog
        dismiss();
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private void initializeView() {
        loadFilter();
    }

    protected abstract void loadFilter();
    protected abstract void saveFilter();

}
