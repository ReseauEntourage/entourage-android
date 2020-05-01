package social.entourage.android.entourage.create;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.model.map.BaseEntourage;
import social.entourage.android.api.model.map.LocationPoint;
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

    protected void createEntourage(
            @Nullable String actionGroupType, @Nullable String category,
            @NotNull String title, @NotNull String description, @NotNull LocationPoint location, boolean recipientConsentObtained,
            @Nullable String groupType, @Nullable BaseEntourage.Metadata metadata, boolean joinRequestTypePublic) {
        BaseEntourage entourage = BaseEntourage.Companion.create(groupType, actionGroupType, category, title, description, location);
        entourage.metadata = metadata;
        entourage.setRecipientConsentObtained(recipientConsentObtained);
        entourage.setJoinRequestPublic(joinRequestTypePublic);
        BaseEntourage.EntourageWrapper entourageWrapper = new BaseEntourage.EntourageWrapper();
        entourageWrapper.entourage = entourage;

        Call<BaseEntourage.EntourageWrapper> call = entourageRequest.createEntourage(entourageWrapper);
        call.enqueue(new Callback<BaseEntourage.EntourageWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<BaseEntourage.EntourageWrapper> call, @NonNull final Response<BaseEntourage.EntourageWrapper> response) {
                if (response.isSuccessful() && response.body()!=null) {
                    BaseEntourage receivedEntourage = response.body().entourage;
                    receivedEntourage.setNewlyCreated(true);
                    if (fragment != null) {
                        fragment.onEntourageCreated(receivedEntourage);
                    }
                    BusProvider.INSTANCE.getInstance().post(new Events.OnEntourageCreated(receivedEntourage));
                }
                else {
                    if (fragment != null) {
                        fragment.onEntourageCreated(null);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<BaseEntourage.EntourageWrapper> call, @NonNull final Throwable t) {
                if (fragment != null) {
                    fragment.onEntourageCreated(null);
                }
            }
        });
    }

    protected void editEntourage(BaseEntourage entourage) {
        BaseEntourage.EntourageWrapper entourageWrapper = new BaseEntourage.EntourageWrapper();
        entourageWrapper.entourage = entourage;

        Call<BaseEntourage.EntourageWrapper> call = entourageRequest.editEntourage(entourage.getUUID(), entourageWrapper);
        call.enqueue(new Callback<BaseEntourage.EntourageWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<BaseEntourage.EntourageWrapper> call, @NonNull final Response<BaseEntourage.EntourageWrapper> response) {
                if (response.isSuccessful() && response.body()!=null) {
                    BaseEntourage receivedEntourage = response.body().entourage;
                    if (fragment != null) {
                        fragment.onEntourageEdited(receivedEntourage);
                    }
                    BusProvider.INSTANCE.getInstance().post(new Events.OnEntourageUpdated(receivedEntourage));
                } else {
                    if (fragment != null) {
                        fragment.onEntourageEdited(null);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<BaseEntourage.EntourageWrapper> call, @NonNull final Throwable t) {
                if (fragment != null) {
                    fragment.onEntourageEdited(null);
                }
            }
        });
    }

}
