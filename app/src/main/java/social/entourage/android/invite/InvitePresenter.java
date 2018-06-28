package social.entourage.android.invite;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.model.MultipleInvitations;
import social.entourage.android.api.model.map.FeedItem;

/**
 * Created by mihaiionescu on 12/07/16.
 */
public class InvitePresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final InviteBaseFragment fragment;
    private final EntourageRequest entourageRequest;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Inject
    public InvitePresenter(InviteBaseFragment fragment, EntourageRequest entourageRequest) {
        this.fragment = fragment;
        this.entourageRequest = entourageRequest;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void inviteBySMS(String feedItemUUID, int feedItemType, MultipleInvitations invitations) {
        if (feedItemType == FeedItem.ENTOURAGE_CARD) {
            inviteBySMSEntourage(feedItemUUID, invitations);
        }
        else  if (feedItemType == FeedItem.TOUR_CARD) {
            // TODO Tour InviteBySMS
            fragment.onInviteSent(false);
        }
    }

    private void inviteBySMSEntourage(String entourageUUID, MultipleInvitations invitations) {

        MultipleInvitations.MultipleInvitationsWrapper wrapper = new MultipleInvitations.MultipleInvitationsWrapper(invitations);
        Call<MultipleInvitations.MultipleInvitationsResponse> call = entourageRequest.inviteBySMS(entourageUUID, wrapper);
        call.enqueue(new Callback<MultipleInvitations.MultipleInvitationsResponse>() {
            @Override
            public void onResponse(final Call<MultipleInvitations.MultipleInvitationsResponse> call, final Response<MultipleInvitations.MultipleInvitationsResponse> response) {
                if (response.isSuccessful()) {
                    if (fragment != null) {
                        fragment.onInviteSent(true);
                    }
                } else {
                    if (fragment != null) {
                        fragment.onInviteSent(false);
                    }
                }
            }

            @Override
            public void onFailure(final Call<MultipleInvitations.MultipleInvitationsResponse> call, final Throwable t) {
                if (fragment != null) {
                    fragment.onInviteSent(false);
                }
            }
        });
    }

}
