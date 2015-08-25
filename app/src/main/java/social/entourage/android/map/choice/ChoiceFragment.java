package social.entourage.android.map.choice;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Tour;

public class ChoiceFragment extends DialogFragment implements ChoiceAdapter.RecyclerViewClickListener {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    ChoicePresenter presenter;

    @InjectView(R.id.choice_recycler_view)
    RecyclerView recyclerView;

    private List<Tour> tours;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public static ChoiceFragment newInstance(Tour.Tours tours) {
        ChoiceFragment fragment = new ChoiceFragment();
        Bundle args = new Bundle();
        args.putSerializable(Tour.KEY_TOURS, tours);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View toReturn = inflater.inflate(R.layout.fragment_choice, container, false);
        ButterKnife.inject(this, toReturn);

        tours = ((Tour.Tours) getArguments().getSerializable(Tour.KEY_TOURS)).getTours();
        Collections.sort(tours, new Tour.TourComparator());
        initializeView();

        return toReturn;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerChoiceComponent.builder()
                .entourageComponent(entourageComponent)
                .choiceModule(new ChoiceModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentFade;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OnChoiceFragmentFinish)) {
            throw new ClassCastException(activity.toString() + " must implement OnChoiceFragmentFinish");
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeView() {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new ChoiceAdapter(this, tours));
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                if (recyclerView.getChildCount() > 0) {
                    final View lastCell = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
                    if (lastCell.getBottom() < recyclerView.getHeight()) {
                        final ViewGroup.LayoutParams layoutParams = recyclerView.getLayoutParams();
                        layoutParams.height = lastCell.getBottom();
                        recyclerView.setLayoutParams(layoutParams);
                    }
                }

            }
        });
    }

    private OnChoiceFragmentFinish getOnChoiceFragmentFinish() {
        final Activity activity = getActivity();
        return activity != null ? (OnChoiceFragmentFinish) activity : null;
    }

    @Override
    public void recyclerViewListClicked(Tour tour) {
        if (tour != null) {
            getOnChoiceFragmentFinish().closeChoiceFragment(this, tour);
        }
    }

    // ----------------------------------
    // INNER CLASSE
    // ----------------------------------

    public interface OnChoiceFragmentFinish {
        void closeChoiceFragment(ChoiceFragment fragment, Tour tour);
    }
}
