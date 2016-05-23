package social.entourage.android.map.tour.information;

import java.util.Date;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
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

    @Inject
    EntourageRequest entourageRequest;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public TourInformationPresenter(final TourInformationFragment fragment) {
        this.fragment = fragment;
    }

    // ----------------------------------
    // Api calls
    // ----------------------------------

    public void getFeedItem(long feedItemId, int feedItemType) {
        fragment.showProgressBar();
        if (feedItemType == TimestampedObject.TOUR_CARD) {
            Call<Tour.TourWrapper> call = tourRequest.retrieveTourById(feedItemId);
            call.enqueue(new Callback<Tour.TourWrapper>() {
                @Override
                public void onResponse(final Call<Tour.TourWrapper> call, final Response<Tour.TourWrapper> response) {
                    if (response.isSuccess()) {
                        fragment.onFeedItemReceived(response.body().getTour());
                    } else {
                        fragment.onFeedItemReceived(null);
                    }
                }

                @Override
                public void onFailure(final Call<Tour.TourWrapper> call, final Throwable t) {
                    fragment.onFeedItemReceived(null);
                }
            });
        }
        else if (feedItemType == TimestampedObject.ENTOURAGE_CARD) {
            //TODO get entourage
            fragment.onFeedItemReceived(null);
        }
        else {
            fragment.onFeedItemReceived(null);
        }
    }

    public void getFeedItemUsers() {
        fragment.showProgressBar();
        if (fragment.feedItem == null) {
            fragment.onFeedItemUsersReceived(null);
            return;
        }
        int feedItemType = fragment.feedItem.getType();
        if (feedItemType == TimestampedObject.TOUR_CARD) {
            Call<TourUser.TourUsersWrapper> call = tourRequest.retrieveTourUsers(fragment.feedItem.getId());
            call.enqueue(new Callback<TourUser.TourUsersWrapper>() {
                @Override
                public void onResponse(final Call<TourUser.TourUsersWrapper> call, final Response<TourUser.TourUsersWrapper> response) {
                    if (response.isSuccess()) {
                        fragment.onFeedItemUsersReceived(response.body().getUsers());
                    } else {
                        fragment.onFeedItemUsersReceived(null);
                    }
                }

                @Override
                public void onFailure(final Call<TourUser.TourUsersWrapper> call, final Throwable t) {
                    fragment.onFeedItemUsersReceived(null);
                }
            });
        }
        else if (feedItemType == TimestampedObject.ENTOURAGE_CARD) {
            Call<TourUser.TourUsersWrapper> call = entourageRequest.retrieveEntourageUsers(fragment.feedItem.getId());
            call.enqueue(new Callback<TourUser.TourUsersWrapper>() {
                @Override
                public void onResponse(final Call<TourUser.TourUsersWrapper> call, final Response<TourUser.TourUsersWrapper> response) {
                    if (response.isSuccess()) {
                        fragment.onFeedItemUsersReceived(response.body().getUsers());
                    } else {
                        fragment.onFeedItemUsersReceived(null);
                    }
                }

                @Override
                public void onFailure(final Call<TourUser.TourUsersWrapper> call, final Throwable t) {
                    fragment.onFeedItemUsersReceived(null);
                }
            });
        }
        else {
            fragment.onFeedItemUsersReceived(null);
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
            Call<ChatMessage.ChatMessagesWrapper> call = tourRequest.retrieveTourMessages(fragment.feedItem.getId(), lastMessageDate);
            call.enqueue(new Callback<ChatMessage.ChatMessagesWrapper>() {
                @Override
                public void onResponse(final Call<ChatMessage.ChatMessagesWrapper> call, final Response<ChatMessage.ChatMessagesWrapper> response) {
                    if (response.isSuccess()) {
                        fragment.onFeedItemMessagesReceived(response.body().getChatMessages());
                    } else {
                        fragment.onFeedItemMessagesReceived(null);
                    }
                }

                @Override
                public void onFailure(final Call<ChatMessage.ChatMessagesWrapper> call, final Throwable t) {
                    fragment.onFeedItemMessagesReceived(null);
                }
            });
        }
        else if (feedItemType == TimestampedObject.ENTOURAGE_CARD) {
            Call<ChatMessage.ChatMessagesWrapper> call = entourageRequest.retrieveTourMessages(fragment.feedItem.getId(), lastMessageDate);
            call.enqueue(new Callback<ChatMessage.ChatMessagesWrapper>() {
                @Override
                public void onResponse(final Call<ChatMessage.ChatMessagesWrapper> call, final Response<ChatMessage.ChatMessagesWrapper> response) {
                    if (response.isSuccess()) {
                        fragment.onFeedItemMessagesReceived(response.body().getChatMessages());
                    } else {
                        fragment.onFeedItemMessagesReceived(null);
                    }
                }

                @Override
                public void onFailure(final Call<ChatMessage.ChatMessagesWrapper> call, final Throwable t) {
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
        ChatMessage chatMessage = new ChatMessage(message);
        ChatMessage.ChatMessageWrapper chatMessageWrapper = new ChatMessage.ChatMessageWrapper();
        chatMessageWrapper.setChatMessage(chatMessage);

        int feedItemType = fragment.feedItem.getType();
        if (feedItemType == TimestampedObject.TOUR_CARD) {
            Call<ChatMessage.ChatMessageWrapper> call = tourRequest.chatMessage(fragment.feedItem.getId(), chatMessageWrapper);
            call.enqueue(new Callback<ChatMessage.ChatMessageWrapper>() {
                @Override
                public void onResponse(final Call<ChatMessage.ChatMessageWrapper> call, final Response<ChatMessage.ChatMessageWrapper> response) {
                    if (response.isSuccess()) {
                        fragment.onFeedItemMessageSent(response.body().getChatMessage());
                    } else {
                        fragment.onFeedItemMessageSent(null);
                    }
                }

                @Override
                public void onFailure(final Call<ChatMessage.ChatMessageWrapper> call, final Throwable t) {
                    fragment.onFeedItemMessageSent(null);
                }
            });
        }
        else if (feedItemType == TimestampedObject.ENTOURAGE_CARD) {
            Call<ChatMessage.ChatMessageWrapper> call = entourageRequest.chatMessage(fragment.feedItem.getId(), chatMessageWrapper);
            call.enqueue(new Callback<ChatMessage.ChatMessageWrapper>() {
                @Override
                public void onResponse(final Call<ChatMessage.ChatMessageWrapper> call, final Response<ChatMessage.ChatMessageWrapper> response) {
                    if (response.isSuccess()) {
                        fragment.onFeedItemMessageSent(response.body().getChatMessage());
                    } else {
                        fragment.onFeedItemMessageSent(null);
                    }
                }

                @Override
                public void onFailure(final Call<ChatMessage.ChatMessageWrapper> call, final Throwable t) {
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
            Call<Encounter.EncountersWrapper> call = tourRequest.retrieveTourEncounters(fragment.feedItem.getId());
            call.enqueue(new Callback<Encounter.EncountersWrapper>() {
                @Override
                public void onResponse(final Call<Encounter.EncountersWrapper> call, final Response<Encounter.EncountersWrapper> response) {
                    if (response.isSuccess()) {
                        fragment.onFeedItemEncountersReceived(response.body().getEncounters());
                    } else {
                        fragment.onFeedItemEncountersReceived(null);
                    }
                }

                @Override
                public void onFailure(final Call<Encounter.EncountersWrapper> call, final Throwable t) {
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

}
