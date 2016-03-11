package social.entourage.android.map.tour.my;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.EntourageApplication;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Tour;

/**
 * Created by mihaiionescu on 10/03/16.
 */
public class MyToursPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final MyToursFragment fragment;

    @Inject
    TourRequest tourRequest;

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

    protected void getMyTours(int page, int per) {
        User me = EntourageApplication.me(fragment.getContext());
        if (me == null) {
            fragment.onToursReceived(null);
            return;
        }
        Call<Tour.ToursWrapper> call = tourRequest.retrieveToursByUserId(me.getId(), page, per);
        call.enqueue(new Callback<Tour.ToursWrapper>() {
            @Override
            public void onResponse(final Call<Tour.ToursWrapper> call, final Response<Tour.ToursWrapper> response) {
                if (response.isSuccess()) {
                    fragment.onToursReceived(response.body().getTours());
                }
                else {
                    fragment.onToursReceived(null);
                }
            }

            @Override
            public void onFailure(final Call<Tour.ToursWrapper> call, final Throwable t) {
                fragment.onToursReceived(null);
            }
        });
    }

}
