package social.entourage.android.tour.choice;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
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

    @BindView(R.id.choice_recycler_view)
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        View toReturn = inflater.inflate(R.layout.fragment_choice, container, false);
        ButterKnife.bind(this, toReturn);

        Tour.Tours receivedTours = (Tour.Tours) getArguments().getSerializable(Tour.KEY_TOURS);
        if (receivedTours != null) {
            tours = receivedTours.getTours();
        }
        else {
            tours = new ArrayList<>();
        }
        Collections.sort(tours, new Tour.TourComparatorNewToOld());
        initializeView();

        return toReturn;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
        if (getDialog() != null && getDialog().getWindow() != null && getDialog().getWindow().getAttributes() != null) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentFade;
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeView() {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new ChoiceAdapter(this, tours));
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
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
        if (tour != null && getOnChoiceFragmentFinish() != null) {
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
