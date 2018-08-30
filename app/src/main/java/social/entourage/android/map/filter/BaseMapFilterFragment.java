package social.entourage.android.map.filter;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.map.entourage.category.EntourageCategory;
import social.entourage.android.map.entourage.category.EntourageCategoryManager;
import social.entourage.android.tools.BusProvider;

public abstract class BaseMapFilterFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage_android.MapFilterFragment";

    private static final String KEY_PRO_USER = "social.entourage.android.KEY_PRO_USER";

    // ----------------------------------
    // Attributes
    // ----------------------------------

    protected boolean isProUser = false;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public BaseMapFilterFragment() {
        // Required empty public constructor
    }

    public static MapFilterFragment newInstance(boolean isProUser) {
        MapFilterFragment fragment = new MapFilterFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_PRO_USER, isProUser);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map_filter, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            isProUser = args.getBoolean(KEY_PRO_USER, false);
        }
        initializeView();
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseClicked() {
        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_CLOSE);
        dismiss();
    }

    @OnClick(R.id.title_action_button)
    protected void onValidateClicked() {
        // save the values to the filter
        saveFilter();

        // inform the map screen to refresh the newsfeed
        BusProvider.getInstance().post(new Events.OnMapFilterChanged());

        EntourageEvents.logEvent(Constants.EVENT_MAP_FILTER_SUBMIT);

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
