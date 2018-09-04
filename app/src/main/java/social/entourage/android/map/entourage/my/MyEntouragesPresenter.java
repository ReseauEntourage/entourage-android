package social.entourage.android.map.entourage.my;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.InvitationRequest;
import social.entourage.android.api.NewsfeedRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.map.entourage.my.filter.MyEntouragesFilter;
import social.entourage.android.map.entourage.my.filter.MyEntouragesFilterFactory;

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
    TourRequest tourRequest;

    @Inject
    EntourageRequest entourageRequest;

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
        MyEntouragesFilter filter = MyEntouragesFilterFactory.getMyEntouragesFilter(fragment.getContext());
        Call<Newsfeed.NewsfeedWrapper> call = newsfeedRequest.retrieveMyFeeds(
                page,
                per,
                filter.getEntourageTypes(),
                filter.getTourTypes(),
                filter.getStatus(),
                filter.isShowOwnEntouragesOnly(),
                filter.isShowPartnerEntourages(),
                filter.isShowJoinedEntourages()
        );
        call.enqueue(new Callback<Newsfeed.NewsfeedWrapper>() {
            @Override
            public void onResponse(final Call<Newsfeed.NewsfeedWrapper> call, final Response<Newsfeed.NewsfeedWrapper> response) {
                if (response.isSuccessful()) {
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

    protected void getMyPendingInvitations() {
        Call<Invitation.InvitationsWrapper> call = invitationRequest.retrieveUserInvitationsWithStatus(Invitation.STATUS_PENDING);
        call.enqueue(new Callback<Invitation.InvitationsWrapper>() {
            @Override
            public void onResponse(final Call<Invitation.InvitationsWrapper> call, final Response<Invitation.InvitationsWrapper> response) {
                if (response.isSuccessful()) {
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

    public void getFeedItem(String feedItemUUID, int feedItemType) {
        fragment.showProgressBar();
        if (feedItemType == TimestampedObject.TOUR_CARD) {
            Call<Tour.TourWrapper> call = tourRequest.retrieveTourById(feedItemUUID);
            call.enqueue(new Callback<Tour.TourWrapper>() {
                @Override
                public void onResponse(final Call<Tour.TourWrapper> call, final Response<Tour.TourWrapper> response) {
                    if (response.isSuccessful()) {
                        if (fragment != null) fragment.onFeedItemReceived(response.body().getTour());
                    } else {
                        if (fragment != null) fragment.onFeedItemReceived(null);
                    }
                }

                @Override
                public void onFailure(final Call<Tour.TourWrapper> call, final Throwable t) {
                    if (fragment != null) fragment.onFeedItemReceived(null);
                }
            });
        }
        else if (feedItemType == TimestampedObject.ENTOURAGE_CARD) {
            Call<Entourage.EntourageWrapper> call = entourageRequest.retrieveEntourageById(feedItemUUID, -1, 0);
            call.enqueue(new Callback<Entourage.EntourageWrapper>() {
                @Override
                public void onResponse(final Call<Entourage.EntourageWrapper> call, final Response<Entourage.EntourageWrapper> response) {
                    if (response.isSuccessful()) {
                        if (fragment != null) fragment.onFeedItemReceived(response.body().getEntourage());
                    } else {
                        if (fragment != null) fragment.onFeedItemReceived(null);
                    }
                }

                @Override
                public void onFailure(final Call<Entourage.EntourageWrapper> call, final Throwable t) {
                    if (fragment != null) fragment.onFeedItemReceived(null);
                }
            });
        }
        else {
            if (fragment != null) fragment.onFeedItemReceived(null);
        }
    }

}
