package social.entourage.android.map.entourage;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

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

    protected void createEntourage(String type, String category, String title, String description, TourPoint location) {
        Entourage entourage = new Entourage(type, category, title, description, location);
        Entourage.EntourageWrapper entourageWrapper = new Entourage.EntourageWrapper();
        entourageWrapper.setEntourage(entourage);

        Call<Entourage.EntourageWrapper> call = entourageRequest.createEntourage(entourageWrapper);
        call.enqueue(new Callback<Entourage.EntourageWrapper>() {
            @Override
            public void onResponse(final Call<Entourage.EntourageWrapper> call, final Response<Entourage.EntourageWrapper> response) {
                if (response.isSuccessful()) {
                    Entourage receivedEntourage = response.body().getEntourage();
                    receivedEntourage.setNewlyCreated(true);
                    if (fragment != null) {
                        fragment.onEntourageCreated(receivedEntourage);
                    }
                    BusProvider.getInstance().post(new Events.OnEntourageCreated(receivedEntourage));
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

    protected void editEntourage(Entourage entourage) {
        Entourage.EntourageWrapper entourageWrapper = new Entourage.EntourageWrapper();
        entourageWrapper.setEntourage(entourage);

        Call<Entourage.EntourageWrapper> call = entourageRequest.editEntourage(entourage.getId(), entourageWrapper);
        call.enqueue(new Callback<Entourage.EntourageWrapper>() {
            @Override
            public void onResponse(final Call<Entourage.EntourageWrapper> call, final Response<Entourage.EntourageWrapper> response) {
                if (response.isSuccessful()) {
                    Entourage receivedEntourage = response.body().getEntourage();
                    if (fragment != null) {
                        fragment.onEntourageEdited(receivedEntourage);
                    }
                    BusProvider.getInstance().post(new Events.OnEntourageUpdated(receivedEntourage));
                } else {
                    if (fragment != null) {
                        fragment.onEntourageEdited(null);
                    }
                }
            }

            @Override
            public void onFailure(final Call<Entourage.EntourageWrapper> call, final Throwable t) {
                if (fragment != null) {
                    fragment.onEntourageEdited(null);
                }
            }
        });
    }

}
