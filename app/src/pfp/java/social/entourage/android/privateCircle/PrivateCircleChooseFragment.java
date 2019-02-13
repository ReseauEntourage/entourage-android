package social.entourage.android.privateCircle;


import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.NewsfeedRequest;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.base.EntouragePagination;
import social.entourage.android.map.entourage.my.filter.MyEntouragesFilter;
import social.entourage.android.map.entourage.my.filter.MyEntouragesFilterFactory;

/**
 * A {@link EntourageDialogFragment} subclass that shows a list of private circles with the capability of choosing one
 */
public class PrivateCircleChooseFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = PrivateCircleChooseFragment.class.getSimpleName();

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.privatecircle_choose_list)
    RecyclerView privateCircleRecyclerView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    PrivateCircleChooseAdapter adapter;

    EntouragePagination pagination = new EntouragePagination(Constants.ITEMS_PER_PAGE);
    private OnScrollListener scrollListener = new OnScrollListener();

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public PrivateCircleChooseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();

        privateCircleRecyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        privateCircleRecyclerView.removeOnScrollListener(scrollListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_privatecircle_choose, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureView();
    }

    private void configureView() {
        //adapter
        if (adapter == null) {
            privateCircleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new PrivateCircleChooseAdapter();
            privateCircleRecyclerView.setAdapter(adapter);
        }
        //get the list of neighborhoods
        getNeighborhoods();
    }

    // ----------------------------------
    // Buttons Handling
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseButtonClicked() {
        dismiss();
    }

    @OnClick(R.id.title_action_button)
    protected void onNextButtonClicked() {
        if (adapter.getSelectedPrivateCircle() == AdapterView.INVALID_POSITION) {
            Toast.makeText(getContext(), R.string.privatecircle_choose_error, Toast.LENGTH_SHORT).show();
            return;
        }
        Entourage entourage = adapter.getItemAt(adapter.getSelectedPrivateCircle());
        if (entourage != null) {
            PrivateCircleDateFragment privateCircleDateFragment = PrivateCircleDateFragment.newInstance(entourage.getId());
            privateCircleDateFragment.show(getFragmentManager(), PrivateCircleDateFragment.TAG);
        }
    }

    // ----------------------------------
    // API Calls
    // ----------------------------------

    private void getNeighborhoods() {
        NewsfeedRequest newsfeedRequest = EntourageApplication.get().getEntourageComponent().getNewsfeedRequest();
        MyEntouragesFilter filter = MyEntouragesFilterFactory.getMyEntouragesFilter(getContext());
        Call<Newsfeed.NewsfeedWrapper> call = newsfeedRequest.retrieveMyFeeds(
                pagination.page,
                pagination.itemsPerPage,
                filter.getEntourageTypes(),
                filter.getTourTypes(),
                filter.getStatus(),
                filter.isShowOwnEntouragesOnly(),
                filter.isShowPartnerEntourages(),
                filter.isShowJoinedEntourages()
        );
        progressBar.setVisibility(View.VISIBLE);
        call.enqueue(new Callback<Newsfeed.NewsfeedWrapper>() {
            @Override
            public void onResponse(final Call<Newsfeed.NewsfeedWrapper> call, final Response<Newsfeed.NewsfeedWrapper> response) {
                if (response.isSuccessful()) {
                    List<Entourage> entourageList = new ArrayList<>();
                    List <Newsfeed> newsfeedList = response.body().getNewsfeed();
                    if (newsfeedList != null) {
                        for (Newsfeed newsfeed: newsfeedList) {
                            Object feedData = newsfeed.getData();
                            if (feedData == null || !(feedData instanceof Entourage)) {
                                continue;
                            }
                            Entourage entourage = (Entourage) newsfeed.getData();
                            if (Entourage.TYPE_PRIVATE_CIRCLE.equalsIgnoreCase(entourage.getGroupType())) {
                                entourageList.add(entourage);
                            }
                        }
                    }
                    adapter.addPrivateCircleList(entourageList);
                    pagination.loadedItems(newsfeedList.size());
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(final Call<Newsfeed.NewsfeedWrapper> call, final Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // ----------------------------------
    // PRIVATE CLASSES
    // ----------------------------------

    private class OnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
            if (dy > 0) {
                // Scrolling down
                int visibleItemCount = recyclerView.getChildCount();
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                int totalItemCount = linearLayoutManager.getItemCount();
                if (totalItemCount - visibleItemCount <= firstVisibleItem + 2) {
                    getNeighborhoods();
                }
            } else {
                // Scrolling up
            }
        }

        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
        }
    }

}
