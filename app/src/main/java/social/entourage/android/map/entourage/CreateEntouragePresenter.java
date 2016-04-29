package social.entourage.android.map.entourage;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.TourPoint;

/**
 * Created by mihaiionescu on 28/04/16.
 */
public class CreateEntouragePresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final CreateEntourageFragment fragment;

    @Inject
    EntourageRequest entourageRequest;

    // ----------------------------------
    // Constructor
    // ----------------------------------

    @Inject
    public CreateEntouragePresenter(CreateEntourageFragment fragment) {
        this.fragment = fragment;
    }

    // ----------------------------------
    // Methods
    // ----------------------------------

    protected void createEntourage(String type, String title, String description, TourPoint location) {
        Entourage entourage = new Entourage(type, title, description, location);
        Entourage.EntourageWrapper entourageWrapper = new Entourage.EntourageWrapper();
        entourageWrapper.setEntourage(entourage);

        Call<Entourage.EntourageWrapper> call = entourageRequest.entourage(entourageWrapper);
        call.enqueue(new Callback<Entourage.EntourageWrapper>() {
            @Override
            public void onResponse(final Call<Entourage.EntourageWrapper> call, final Response<Entourage.EntourageWrapper> response) {
                if (response.isSuccess()) {
                    if (fragment != null) {
                        fragment.onEntourageCreated(response.body().getEntourage());
                    }
                }
                else {
                    if (fragment != null) {
                        fragment.onEntourageCreated(null);
                    }
                }
            }

            @Override
            public void onFailure(final Call<Entourage.EntourageWrapper> call, final Throwable t) {
                if (fragment != null) {
                    fragment.onEntourageCreated(null);
                }
            }
        });
    }

}
