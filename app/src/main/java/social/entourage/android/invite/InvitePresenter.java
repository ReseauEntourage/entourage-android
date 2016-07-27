package social.entourage.android.invite;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.invite.contacts.InviteContactsFragment;

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

    public void inviteBySMS(long feedItemId, int feedItemType, String phoneNumber) {
        if (feedItemType == FeedItem.ENTOURAGE_CARD) {
            inviteBySMSEntourage(feedItemId, phoneNumber);
        }
        else  if (feedItemType == FeedItem.TOUR_CARD) {
            // TODO Tour InviteBySMS
            fragment.onInviteSent(false);
        }
    }

    private void inviteBySMSEntourage(long entourageId, String phoneNumber) {
        Invitation invitation = new Invitation(Invitation.INVITE_BY_SMS, phoneNumber);
        Invitation.InvitationWrapper wrapper = new Invitation.InvitationWrapper(invitation);
        Call<Invitation.InvitationWrapper> call = entourageRequest.inviteBySMS(entourageId, wrapper);
        call.enqueue(new Callback<Invitation.InvitationWrapper>() {
            @Override
            public void onResponse(final Call<Invitation.InvitationWrapper> call, final Response<Invitation.InvitationWrapper> response) {
                if (response.isSuccess()) {
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
            public void onFailure(final Call<Invitation.InvitationWrapper> call, final Throwable t) {
                if (fragment != null) {
                    fragment.onInviteSent(false);
                }
            }
        });
    }

}
