package social.entourage.android.guide.filter;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.tools.BusProvider;

/**
 * Guide Filter Fragment
 */
public class GuideFilterFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = GuideFilterFragment.class.getSimpleName();

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @BindView(R.id.guide_filter_list)
    ListView filterListView;

    GuideFilterAdapter filterAdapter;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public GuideFilterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_guide_filter, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeFiltersList();
    }

    // ----------------------------------
    // Buttons handling
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.title_action_button)
    protected void onValidateClicked() {
        // Save the filter
        GuideFilter guideFilter = GuideFilter.getInstance();
        for (int i = 0; i < filterAdapter.getCount(); i++) {
            GuideFilterAdapter.GuideFilterItem filterItem = filterAdapter.getItem(i);
            guideFilter.setValueForCategoryId(filterItem.categoryType.getCategoryId(), filterItem.isChecked);
        }
        // Apply the filter
        BusProvider.getInstance().post(new Events.OnSolidarityGuideFilterChanged());
        // Dismiss the fragment
        dismiss();
    }

    // ----------------------------------
    // ListView
    // ----------------------------------

    private void initializeFiltersList() {
        filterAdapter = new GuideFilterAdapter();
        filterListView.setAdapter(filterAdapter);
    }

}
