package social.entourage.android.neighborhood;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
 * A {@link EntourageDialogFragment} subclass that shows a list of neighborhoods with the capability of choosing one
 */
public class NeighborhoodChooseFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = NeighborhoodChooseFragment.class.getSimpleName();

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.neighborhood_choose_list)
    RecyclerView neighborhoodRecyclerView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    NeighborhoodChooseAdapter adapter;

    EntouragePagination pagination = new EntouragePagination(Constants.ITEMS_PER_PAGE);

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public NeighborhoodChooseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_neighborhood_choose, container, false);
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
            neighborhoodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new NeighborhoodChooseAdapter();
            neighborhoodRecyclerView.setAdapter(adapter);
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
        if (adapter.getSelectedNeighborhood() == AdapterView.INVALID_POSITION) {
            Toast.makeText(getContext(), R.string.neighborhood_choose_error, Toast.LENGTH_SHORT).show();
            return;
        }
        Entourage entourage = adapter.getItemAt(adapter.getSelectedNeighborhood());
        if (entourage != null) {
            NeighborhoodDateFragment neighborhoodDateFragment = NeighborhoodDateFragment.newInstance(entourage.getId());
            neighborhoodDateFragment.show(getFragmentManager(), NeighborhoodDateFragment.TAG);
        }
    }

    // ----------------------------------
    // API Calls
    // ----------------------------------

    //TODO we need to have an adapter with pagination!!

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
                            if (feedData == null || !(feedData instanceof FeedItem)) {
                                continue;
                            }
                            FeedItem feedItem = (FeedItem) newsfeed.getData();
                            if (feedItem.getType() == Entourage.ENTOURAGE_CARD) {
                                entourageList.add((Entourage)feedItem);
                            }
                        }
                    }
                    adapter.setNeighborhoodList(entourageList);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(final Call<Newsfeed.NewsfeedWrapper> call, final Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

}
