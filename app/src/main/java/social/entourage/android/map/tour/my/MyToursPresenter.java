package social.entourage.android.map.tour.my;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.NewsfeedRequest;
import social.entourage.android.api.model.Newsfeed;

/**
 * Created by mihaiionescu on 10/03/16.
 */
public class MyToursPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final MyToursFragment fragment;

    @Inject
    NewsfeedRequest newsfeedRequest;

    // ----------------------------------
    // Constructor
    // ----------------------------------

    @Inject
    public MyToursPresenter(final MyToursFragment fragment) {
        this.fragment = fragment;
    }

    // ----------------------------------
    // Methods
    // ----------------------------------

    protected void getMyFeeds(int page, int per, final String status) {
        Call<Newsfeed.NewsfeedWrapper> call = newsfeedRequest.retrieveMyFeeds(page, per, "", "", status, true, false, true);
        call.enqueue(new Callback<Newsfeed.NewsfeedWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Newsfeed.NewsfeedWrapper> call, @NonNull final Response<Newsfeed.NewsfeedWrapper> response) {
                if (response.isSuccessful()) {
                    fragment.onNewsfeedReceived(response.body().getNewsfeed(), status);
                }
                else {
                    fragment.onNewsfeedReceived(null, status);
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Newsfeed.NewsfeedWrapper> call, @NonNull final Throwable t) {
                fragment.onNewsfeedReceived(null, status);
            }
        });
    }

}
