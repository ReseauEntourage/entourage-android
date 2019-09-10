package social.entourage.android.entourage.information;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.HashMap;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.EntourageError;
import social.entourage.android.EntourageEvents;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.InvitationRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourUser;

/**
 * Presenter controlling the EntourageInformationFragment
 * @see EntourageInformationFragment
 */
public class EntourageInformationPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final EntourageInformationFragment fragment;

    @Inject
    TourRequest tourRequest;

    @Inject
    EntourageRequest entourageRequest;

    @Inject
    InvitationRequest invitationRequest;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public EntourageInformationPresenter(final EntourageInformationFragment fragment) {
        this.fragment = fragment;
    }

    // ----------------------------------
    // Api calls
    // ----------------------------------

    public void getFeedItem(String feedItemUUID, int feedItemType, int feedRank, int distance) {
        fragment.showProgressBar();
        if (feedItemType == TimestampedObject.TOUR_CARD) {
            Call<Tour.TourWrapper> call = tourRequest.retrieveTourById(feedItemUUID);
            call.enqueue(new Callback<Tour.TourWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Response<Tour.TourWrapper> response) {
                    if (response.isSuccessful()) {
                        fragment.onFeedItemReceived(response.body().getTour());
                    } else {
                        fragment.onFeedItemReceived(null);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Throwable t) {
                    fragment.onFeedItemReceived(null);
                }
            });
        }
        else if (feedItemType == TimestampedObject.ENTOURAGE_CARD) {
            Call<Entourage.EntourageWrapper> call = entourageRequest.retrieveEntourageById(feedItemUUID, distance, feedRank);
            call.enqueue(new Callback<Entourage.EntourageWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<Entourage.EntourageWrapper> call, @NonNull final Response<Entourage.EntourageWrapper> response) {
                    if (response.isSuccessful()) {
                        fragment.onFeedItemReceived(response.body().getEntourage());
                    } else {
                        fragment.onFeedItemReceived(null);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<Entourage.EntourageWrapper> call, @NonNull final Throwable t) {
                    fragment.onFeedItemReceived(null);
                }
            });
        }
        else {
            fragment.onFeedItemReceived(null);
        }
    }

    public void getFeedItem(String feedItemShareURL, int feedItemType) {
        fragment.showProgressBar();
        if (feedItemType == TimestampedObject.TOUR_CARD) {
            fragment.onFeedItemReceived(null);
        }
        else if (feedItemType == TimestampedObject.ENTOURAGE_CARD) {
            Call<Entourage.EntourageWrapper> call = entourageRequest.retrieveEntourageByShareURL(feedItemShareURL);
            call.enqueue(new Callback<Entourage.EntourageWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<Entourage.EntourageWrapper> call, @NonNull final Response<Entourage.EntourageWrapper> response) {
                    if (response.isSuccessful()) {
                        fragment.onFeedItemReceived(response.body().getEntourage());
                    } else {
                        fragment.onFeedItemReceived(null);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<Entourage.EntourageWrapper> call, @NonNull final Throwable t) {
                    fragment.onFeedItemReceived(null);
                }
            });
        }
        else {
            fragment.onFeedItemReceived(null);
        }
    }

    public void getFeedItemMembers() {
        getFeedItemUsers(null);
    }

    public void getFeedItemJoinRequests() {
        getFeedItemUsers("group_feed");
    }

    private void getFeedItemUsers(final String context) {
        fragment.showProgressBar();
        if (fragment.feedItem == null) {
            fragment.onFeedItemUsersReceived(null, context);
            return;
        }
        int feedItemType = fragment.feedItem.getType();
        if (feedItemType == TimestampedObject.TOUR_CARD) {
            Call<TourUser.TourUsersWrapper> call = tourRequest.retrieveTourUsers(fragment.feedItem.getUUID());
            call.enqueue(new Callback<TourUser.TourUsersWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<TourUser.TourUsersWrapper> call, @NonNull final Response<TourUser.TourUsersWrapper> response) {
                    if (response.isSuccessful()) {
                        fragment.onFeedItemUsersReceived(response.body().getUsers(), context);
                    } else {
                        fragment.onFeedItemUsersReceived(null, context);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<TourUser.TourUsersWrapper> call, @NonNull final Throwable t) {
                    fragment.onFeedItemUsersReceived(null, context);
                }
            });
        }
        else if (feedItemType == TimestampedObject.ENTOURAGE_CARD) {
            Call<TourUser.TourUsersWrapper> call = entourageRequest.retrieveEntourageUsers(fragment.feedItem.getUUID(), context);
            call.enqueue(new Callback<TourUser.TourUsersWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<TourUser.TourUsersWrapper> call, @NonNull final Response<TourUser.TourUsersWrapper> response) {
                    if (response.isSuccessful()) {
                        fragment.onFeedItemUsersReceived(response.body().getUsers(), context);
                    } else {
                        fragment.onFeedItemUsersReceived(null, context);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<TourUser.TourUsersWrapper> call, @NonNull final Throwable t) {
                    fragment.onFeedItemUsersReceived(null, context);
                }
            });
        }
        else {
            fragment.onFeedItemUsersReceived(null, context);
        }
    }

    public void getFeedItemMessages() {
        getFeedItemMessages(null);
    }

    public void getFeedItemMessages(Date lastMessageDate) {
        fragment.showProgressBar();
        if (fragment.feedItem == null) {
            fragment.onFeedItemMessagesReceived(null);
            return;
        }
        int feedItemType = fragment.feedItem.getType();
        if (feedItemType == TimestampedObject.TOUR_CARD) {
            Call<ChatMessage.ChatMessagesWrapper> call = tourRequest.retrieveTourMessages(fragment.feedItem.getUUID(), lastMessageDate);
            call.enqueue(new Callback<ChatMessage.ChatMessagesWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<ChatMessage.ChatMessagesWrapper> call, @NonNull final Response<ChatMessage.ChatMessagesWrapper> response) {
                    if (response.isSuccessful()) {
                        fragment.onFeedItemMessagesReceived(response.body().getChatMessages());
                    } else {
                        fragment.onFeedItemMessagesReceived(null);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<ChatMessage.ChatMessagesWrapper> call, @NonNull final Throwable t) {
                    fragment.onFeedItemMessagesReceived(null);
                }
            });
        }
        else if (feedItemType == TimestampedObject.ENTOURAGE_CARD) {
            Call<ChatMessage.ChatMessagesWrapper> call = entourageRequest.retrieveEntourageMessages(fragment.feedItem.getUUID(), lastMessageDate);
            call.enqueue(new Callback<ChatMessage.ChatMessagesWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<ChatMessage.ChatMessagesWrapper> call, @NonNull final Response<ChatMessage.ChatMessagesWrapper> response) {
                    if (response.isSuccessful()) {
                        fragment.onFeedItemMessagesReceived(response.body().getChatMessages());
                    } else {
                        fragment.onFeedItemMessagesReceived(null);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<ChatMessage.ChatMessagesWrapper> call, @NonNull final Throwable t) {
                    fragment.onFeedItemMessagesReceived(null);
                }
            });
        }
        else {
            fragment.onFeedItemMessagesReceived(null);
        }
    }

    public void sendFeedItemMessage(String message) {
        fragment.showProgressBar();
        if (fragment.feedItem == null || message == null || message.trim().length() == 0) {
            fragment.onFeedItemMessageSent(null);
            return;
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_ADD_MESSAGE);
        ChatMessage chatMessage = new ChatMessage(message);
        ChatMessage.ChatMessageWrapper chatMessageWrapper = new ChatMessage.ChatMessageWrapper();
        chatMessageWrapper.setChatMessage(chatMessage);

        int feedItemType = fragment.feedItem.getType();
        if (feedItemType == TimestampedObject.TOUR_CARD) {
            Call<ChatMessage.ChatMessageWrapper> call = tourRequest.chatMessage(fragment.feedItem.getUUID(), chatMessageWrapper);
            call.enqueue(new Callback<ChatMessage.ChatMessageWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<ChatMessage.ChatMessageWrapper> call, @NonNull final Response<ChatMessage.ChatMessageWrapper> response) {
                    if (response.isSuccessful()) {
                        fragment.onFeedItemMessageSent(response.body().getChatMessage());
                    } else {
                        fragment.onFeedItemMessageSent(null);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<ChatMessage.ChatMessageWrapper> call, @NonNull final Throwable t) {
                    fragment.onFeedItemMessageSent(null);
                }
            });
        }
        else if (feedItemType == TimestampedObject.ENTOURAGE_CARD) {
            Call<ChatMessage.ChatMessageWrapper> call = entourageRequest.chatMessage(fragment.feedItem.getUUID(), chatMessageWrapper);
            call.enqueue(new Callback<ChatMessage.ChatMessageWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<ChatMessage.ChatMessageWrapper> call, @NonNull final Response<ChatMessage.ChatMessageWrapper> response) {
                    if (response.isSuccessful()) {
                        fragment.onFeedItemMessageSent(response.body().getChatMessage());
                    } else {
                        fragment.onFeedItemMessageSent(null);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<ChatMessage.ChatMessageWrapper> call, @NonNull final Throwable t) {
                    fragment.onFeedItemMessageSent(null);
                }
            });
        }
        else {
            fragment.onFeedItemMessageSent(null);
        }
    }

    public void getFeedItemEncounters() {
        fragment.showProgressBar();
        if (fragment.feedItem == null) {
            fragment.onFeedItemEncountersReceived(null);
            return;
        }
        int feedItemType = fragment.feedItem.getType();
        if (feedItemType == TimestampedObject.TOUR_CARD) {
            Call<Encounter.EncountersWrapper> call = tourRequest.retrieveTourEncounters(fragment.feedItem.getUUID());
            call.enqueue(new Callback<Encounter.EncountersWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<Encounter.EncountersWrapper> call, @NonNull final Response<Encounter.EncountersWrapper> response) {
                    if (response.isSuccessful()) {
                        fragment.onFeedItemEncountersReceived(response.body().getEncounters());
                    } else {
                        fragment.onFeedItemEncountersReceived(null);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<Encounter.EncountersWrapper> call, @NonNull final Throwable t) {
                    fragment.onFeedItemEncountersReceived(null);
                }
            });
        }
        else if (feedItemType == TimestampedObject.ENTOURAGE_CARD) {
            //Entourage doesn't have encounters
            fragment.onFeedItemEncountersReceived(null);
        }
        else {
            fragment.onFeedItemEncountersReceived(null);
        }
    }

    // ----------------------------------
    // Update user join requests
    // ----------------------------------

    public void updateUserJoinRequest(int userId, String status, FeedItem feedItem) {
        fragment.showProgressBar();
        int feedItemType = feedItem.getType();
        if (feedItemType == TimestampedObject.TOUR_CARD) {
            // Tour user update status
            if (FeedItem.JOIN_STATUS_ACCEPTED.equals(status)) {
                acceptTourJoinRequest(feedItem.getUUID(), userId);
            }
            else if (FeedItem.JOIN_STATUS_REJECTED.equals(status)) {
                rejectJoinTourRequest(feedItem.getUUID(), userId);
            }
            else {
                fragment.onUserJoinRequestUpdated(userId, status, EntourageError.ERROR_UNKNOWN);
            }
        }
        else if (feedItemType == TimestampedObject.ENTOURAGE_CARD) {
            // Entourage user update status
            if (FeedItem.JOIN_STATUS_ACCEPTED.equals(status)) {
                acceptEntourageJoinRequest(feedItem.getUUID(), userId);
            }
            else if (FeedItem.JOIN_STATUS_REJECTED.equals(status)) {
                rejectJoinEntourageRequest(feedItem.getUUID(), userId);
            }
            else {
                fragment.onUserJoinRequestUpdated(userId, status, EntourageError.ERROR_UNKNOWN);
            }
        }
        else {
            // Unknown type
            fragment.onUserJoinRequestUpdated(userId, status, EntourageError.ERROR_UNKNOWN);
        }
    }

    protected void acceptTourJoinRequest(String tourUUID, final int userId) {
        HashMap<String, String> status = new HashMap<>();
        status.put("status", FeedItem.JOIN_STATUS_ACCEPTED);
        HashMap<String, Object> user = new HashMap<>();
        user.put("user", status);
        Call<ResponseBody> call = tourRequest.updateUserTourStatus(tourUUID, userId, user);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull final Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                if (fragment != null) {
                    if (response.isSuccessful()) {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntourageError.ERROR_NONE);
                    }
                    else {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<ResponseBody> call, @NonNull final Throwable t) {
                if (fragment != null) {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntourageError.ERROR_NETWORK);
                }
            }
        });
    }

    protected void rejectJoinTourRequest(String tourUUID, final int userId) {
        Call<TourUser.TourUserWrapper> call = tourRequest.removeUserFromTour(tourUUID, userId);
        call.enqueue(new Callback<TourUser.TourUserWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Response<TourUser.TourUserWrapper> response) {
                if (fragment != null) {
                    if (response.isSuccessful()) {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntourageError.ERROR_NONE);
                    } else {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Throwable t) {
                if (fragment != null) {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntourageError.ERROR_NETWORK);
                }
            }
        });
    }

    protected void acceptEntourageJoinRequest(String entourageUUID, final int userId) {
        HashMap<String, String> status = new HashMap<>();
        status.put("status", FeedItem.JOIN_STATUS_ACCEPTED);
        HashMap<String, Object> user = new HashMap<>();
        user.put("user", status);
        Call<ResponseBody> call = entourageRequest.updateUserEntourageStatus(entourageUUID, userId, user);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull final Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                if (fragment != null) {
                    if (response.isSuccessful()) {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntourageError.ERROR_NONE);
                    }
                    else {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<ResponseBody> call, @NonNull final Throwable t) {
                if (fragment != null) {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntourageError.ERROR_NETWORK);
                }
            }
        });
    }

    protected void rejectJoinEntourageRequest(String entourageUUID, final int userId) {
        Call<TourUser.TourUserWrapper> call = entourageRequest.removeUserFromEntourage(entourageUUID, userId);
        call.enqueue(new Callback<TourUser.TourUserWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Response<TourUser.TourUserWrapper> response) {
                if (fragment != null) {
                    if (response.isSuccessful()) {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntourageError.ERROR_NONE);
                    } else {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Throwable t) {
                if (fragment != null) {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntourageError.ERROR_NETWORK);
                }
            }
        });
    }

    // ----------------------------------
    // Update received invitation
    // ----------------------------------

    public void acceptInvitation(long invitationId) {
        Call<Invitation.InvitationWrapper> call = invitationRequest.acceptInvitation(invitationId);
        call.enqueue(new Callback<Invitation.InvitationWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Invitation.InvitationWrapper> call, @NonNull final Response<Invitation.InvitationWrapper> response) {
                if (fragment != null) {
                    if (response.isSuccessful()) {
                        fragment.onInvitationStatusUpdated(true, Invitation.STATUS_ACCEPTED);
                    } else {
                        fragment.onInvitationStatusUpdated(false, Invitation.STATUS_ACCEPTED);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Invitation.InvitationWrapper> call, @NonNull final Throwable t) {
                fragment.onInvitationStatusUpdated(false, Invitation.STATUS_ACCEPTED);
            }
        });
    }

    public void rejectInvitation(long invitationId) {
        Call<Invitation.InvitationWrapper> call = invitationRequest.refuseInvitation(invitationId);
        call.enqueue(new Callback<Invitation.InvitationWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Invitation.InvitationWrapper> call, @NonNull final Response<Invitation.InvitationWrapper> response) {
                if (fragment != null) {
                    if (response.isSuccessful()) {
                        fragment.onInvitationStatusUpdated(true, Invitation.STATUS_REJECTED);
                    } else {
                        fragment.onInvitationStatusUpdated(false, Invitation.STATUS_REJECTED);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Invitation.InvitationWrapper> call, @NonNull final Throwable t) {
                fragment.onInvitationStatusUpdated(false, Invitation.STATUS_REJECTED);
            }
        });
    }

}
