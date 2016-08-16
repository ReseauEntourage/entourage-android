package social.entourage.android.map.entourage.my;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.InvitationRequest;
import social.entourage.android.api.NewsfeedRequest;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.map.entourage.my.filter.MyEntouragesFilter;

/**
 * Created by mihaiionescu on 03/08/16.
 */
public class MyEntouragesPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    MyEntouragesFragment fragment;

    @Inject
    NewsfeedRequest newsfeedRequest;

    @Inject
    InvitationRequest invitationRequest;

    // ----------------------------------
    // Constructor
    // ----------------------------------

    @Inject
    public MyEntouragesPresenter(MyEntouragesFragment fragment) {
        this.fragment = fragment;
    }

    // ----------------------------------
    // Methods
    // ----------------------------------

    protected void getMyFeeds(int page, int per) {
        MyEntouragesFilter filter = MyEntouragesFilter.getInstance();
        Call<Newsfeed.NewsfeedWrapper> call = newsfeedRequest.retrieveMyFeeds(
                page,
                per,
                filter.getEntourageTypes(),
                filter.getStatus(),
                filter.showOwnEntourages,
                filter.showJoinedEntourages
        );
        call.enqueue(new Callback<Newsfeed.NewsfeedWrapper>() {
            @Override
            public void onResponse(final Call<Newsfeed.NewsfeedWrapper> call, final Response<Newsfeed.NewsfeedWrapper> response) {
                if (response.isSuccess()) {
                    fragment.onNewsfeedReceived(response.body().getNewsfeed());
                }
                else {
                    fragment.onNewsfeedReceived(null);
                }
            }

            @Override
            public void onFailure(final Call<Newsfeed.NewsfeedWrapper> call, final Throwable t) {
                fragment.onNewsfeedReceived(null);
            }
        });
    }

    protected void getMyInvitations() {
        Call<Invitation.InvitationsWrapper> call = invitationRequest.retrieveUserInvitations();
        call.enqueue(new Callback<Invitation.InvitationsWrapper>() {
            @Override
            public void onResponse(final Call<Invitation.InvitationsWrapper> call, final Response<Invitation.InvitationsWrapper> response) {
                if (response.isSuccess()) {
                    fragment.onInvitationsReceived(response.body().getInvitations());
                }
                else {
                    fragment.onInvitationsReceived(null);
                }
            }

            @Override
            public void onFailure(final Call<Invitation.InvitationsWrapper> call, final Throwable t) {
                fragment.onInvitationsReceived(null);
            }
        });
    }
}
