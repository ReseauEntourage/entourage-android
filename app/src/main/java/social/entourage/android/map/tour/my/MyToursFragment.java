package social.entourage.android.map.tour.my;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

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

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private OnFragmentInteractionListener mListener;

    @Inject
    MyToursPresenter presenter;

    @Bind(R.id.mytours_tabHost)
    TabHost tabHost;

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

    private void initializeView() {
        tabHost.setup();
        tabHost.getTabWidget().setDividerDrawable(R.color.accent);
        tabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        setupTab(R.id.mytours_ongoing_layout, getString(R.string.mytours_ongoing));
        setupTab(R.id.mytours_recorded_layout, getString(R.string.mytours_recorded));
        setupTab(R.id.mytours_frozen_layout, getString(R.string.mytours_frozen));
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
