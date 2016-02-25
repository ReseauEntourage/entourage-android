package social.entourage.android.map.tour;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.ChatMessage;
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

    public void getTourMessages() {
        if (fragment.tour == null) {
            fragment.onTourMessagesReceived(null);
            return;
        }
        Call<ChatMessage.ChatMessagesWrapper> call = tourRequest.retrieveTourMessages(fragment.tour.getId());
        call.enqueue(new Callback<ChatMessage.ChatMessagesWrapper>() {
            @Override
            public void onResponse(final Call<ChatMessage.ChatMessagesWrapper> call, final Response<ChatMessage.ChatMessagesWrapper> response) {
                if (response.isSuccess()) {
                    fragment.onTourMessagesReceived(response.body().getChatMessages());
                }
                else {
                    fragment.onTourMessagesReceived(null);
                }
            }

            @Override
            public void onFailure(final Call<ChatMessage.ChatMessagesWrapper> call, final Throwable t) {
                fragment.onTourMessagesReceived(null);
            }
        });
    }

    public void sendTourMessage(String message) {
        if (fragment.tour == null || message == null || message.trim().length() == 0) {
            fragment.onTourMessageSent(null);
            return;
        }
        ChatMessage chatMessage = new ChatMessage(message);
        ChatMessage.ChatMessageWrapper chatMessageWrapper = new ChatMessage.ChatMessageWrapper();
        chatMessageWrapper.setChatMessage(chatMessage);
        Call<ChatMessage> call = tourRequest.chatMessage(fragment.tour.getId(), chatMessageWrapper);
        call.enqueue(new Callback<ChatMessage>() {
            @Override
            public void onResponse(final Call<ChatMessage> call, final Response<ChatMessage> response) {
                if (response.isSuccess()) {
                    fragment.onTourMessageSent(response.body());
                }
                else {
                    fragment.onTourMessageSent(null);
                }
            }

            @Override
            public void onFailure(final Call<ChatMessage> call, final Throwable t) {
                fragment.onTourMessageSent(null);
            }
        });
    }

}
