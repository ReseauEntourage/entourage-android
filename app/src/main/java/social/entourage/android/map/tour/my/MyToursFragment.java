package social.entourage.android.map.tour.my;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Tour;

public class MyToursFragment extends DialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.mytours";

    private static final String TAB_ONGOING_TOURS = "tab_ongoing_tours";
    private static final String TAB_RECORDED_TOURS = "tab_recorded_tours";
    private static final String TAB_FROZEN_TOURS = "tab_frozen_tours";

    private static final int API_TOURS_PER_PAGE = 10;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private OnFragmentInteractionListener mListener;

    @Inject
    MyToursPresenter presenter;

    @Bind(R.id.mytours_tabHost)
    TabHost tabHost;

    @Bind(R.id.mytours_ongoing)
    RecyclerView ongoingToursRecyclerView;

    ToursAdapter ongoingToursAdapter;

    @Bind(R.id.mytours_recorded)
    RecyclerView recordedToursRecyclerView;

    ToursAdapter recordedToursAdapter;

    @Bind(R.id.mytours_frozen)
    RecyclerView frozenToursRecyclerView;

    ToursAdapter frozenToursAdapter;

    @Bind(R.id.mytours_progress_bar)
    ProgressBar progressBar;

    private int apiRequestsCount = 0;

    private int page = 0;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public MyToursFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_tours, container, false);
        ButterKnife.bind(this, view);
        initializeView();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        retrieveMyTours();
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerMyToursComponent.builder()
                .entourageComponent(entourageComponent)
                .myToursModule(new MyToursModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private void initializeView() {
        initializeTabHost();
        initializeRecyclerViews();
    }

    private void initializeTabHost() {
        tabHost.setup();
        setupTab(R.id.mytours_ongoing_layout, getString(R.string.mytours_ongoing));
        setupTab(R.id.mytours_recorded_layout, getString(R.string.mytours_recorded));
        setupTab(R.id.mytours_frozen_layout, getString(R.string.mytours_frozen));

        TabWidget tabWidget = tabHost.getTabWidget();
        tabWidget.getChildTabViewAt(0).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_tabitem_left));
        tabWidget.getChildTabViewAt(tabWidget.getTabCount() - 1).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_tabitem_right));
    }

    private void setupTab(@IdRes int viewId, String tag) {
        View tabView = createTabView(tabHost.getContext(), tag);
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setIndicator(tabView);
        tabSpec.setContent(viewId);
        tabHost.addTab(tabSpec);
    }

    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_mytours_tab_item, null);
        TextView tv = (TextView) view.findViewById(R.id.tabitem_textview);
        tv.setText(text);
        return view;
    }

    private void initializeRecyclerViews() {
        ongoingToursRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ongoingToursAdapter = new ToursAdapter();
        ongoingToursRecyclerView.setAdapter(ongoingToursAdapter);

        recordedToursRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recordedToursAdapter = new ToursAdapter();
        recordedToursRecyclerView.setAdapter(recordedToursAdapter);

        frozenToursRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        frozenToursAdapter = new ToursAdapter();
        frozenToursRecyclerView.setAdapter(frozenToursAdapter);
    }

    protected void showProgressBar() {
        apiRequestsCount++;
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        apiRequestsCount--;
        if (apiRequestsCount <= 0) {
            progressBar.setVisibility(View.GONE);
            apiRequestsCount = 0;
        }
    }

    private void retrieveMyTours() {
        showProgressBar();
        presenter.getMyTours(page, API_TOURS_PER_PAGE);
    }

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------

    protected void onToursReceived(List<Tour> tourList) {
        hideProgressBar();
        if (tourList == null) return;
        if (tourList.size() > 0) {
            Iterator<Tour> iterator = tourList.iterator();
            while (iterator.hasNext()) {
                Tour tour = iterator.next();
                if (tour.getTourStatus().equals(Tour.TOUR_ON_GOING)) {
                    ongoingToursAdapter.add(tour);
                }
                else if (tour.getTourStatus().equals(Tour.TOUR_CLOSED)) {
                    recordedToursAdapter.add(tour);
                }
                else {
                    frozenToursAdapter.add(tour);
                }
            }
            page++;
            //TODO Implement pagination better
            //presenter.getMyTours(page, API_TOURS_PER_PAGE);
        }
    }

    // ----------------------------------
    // BUTTONS HANDLING
    // ----------------------------------

    @OnClick(R.id.mytours_close_button)
    protected void onCloseButton() {
        dismiss();
    }

    // ----------------------------------
    // ACTIVITY INTERFACE
    // ----------------------------------

    public interface OnFragmentInteractionListener {
        void onShowTourInfo(Tour tour);
    }
}
