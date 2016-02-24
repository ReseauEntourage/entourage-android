package social.entourage.android.map.tour;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.map.TourUser;

/**
 * Presenter controlling the TourInformationFragment
 * @see TourInformationFragment
 */
public class TourInformationPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final TourInformationFragment fragment;

    @Inject
    TourRequest tourRequest;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public TourInformationPresenter(final TourInformationFragment fragment) {
        this.fragment = fragment;
    }

    // --
    // Api calls
    // --

    public void getTourUsers() {
        if (fragment.tour == null) {
            fragment.onTourUsersReceived(null);
            return;
        }
        Call<TourUser.TourUsersWrapper> call = tourRequest.retrieveTourUsers(fragment.tour.getId());
        call.enqueue(new Callback<TourUser.TourUsersWrapper>() {
            @Override
            public void onResponse(final Call<TourUser.TourUsersWrapper> call, final Response<TourUser.TourUsersWrapper> response) {
                if (response.isSuccess()) {
                    fragment.onTourUsersReceived(response.body().getUsers());
                }
                else {
                    fragment.onTourUsersReceived(null);
                }
            }

            @Override
            public void onFailure(final Call<TourUser.TourUsersWrapper> call, final Throwable t) {
                fragment.onTourUsersReceived(null);
            }
        });
    }

}
