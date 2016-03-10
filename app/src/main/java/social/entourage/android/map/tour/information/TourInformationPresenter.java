package social.entourage.android.map.tour.information;

import java.util.Date;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.map.Encounter;
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

    // ----------------------------------
    // Api calls
    // ----------------------------------

    public void getTourUsers() {
        fragment.showProgressBar();
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
                } else {
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
        getTourMessages(null);
    }

    public void getTourMessages(Date lastMessageDate) {
        fragment.showProgressBar();
        if (fragment.tour == null) {
            fragment.onTourMessagesReceived(null);
            return;
        }
        Call<ChatMessage.ChatMessagesWrapper> call = tourRequest.retrieveTourMessages(fragment.tour.getId(), lastMessageDate);
        call.enqueue(new Callback<ChatMessage.ChatMessagesWrapper>() {
            @Override
            public void onResponse(final Call<ChatMessage.ChatMessagesWrapper> call, final Response<ChatMessage.ChatMessagesWrapper> response) {
                if (response.isSuccess()) {
                    fragment.onTourMessagesReceived(response.body().getChatMessages());
                } else {
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
        fragment.showProgressBar();
        if (fragment.tour == null || message == null || message.trim().length() == 0) {
            fragment.onTourMessageSent(null);
            return;
        }
        ChatMessage chatMessage = new ChatMessage(message);
        ChatMessage.ChatMessageWrapper chatMessageWrapper = new ChatMessage.ChatMessageWrapper();
        chatMessageWrapper.setChatMessage(chatMessage);
        Call<ChatMessage.ChatMessageWrapper> call = tourRequest.chatMessage(fragment.tour.getId(), chatMessageWrapper);
        call.enqueue(new Callback<ChatMessage.ChatMessageWrapper>() {
            @Override
            public void onResponse(final Call<ChatMessage.ChatMessageWrapper> call, final Response<ChatMessage.ChatMessageWrapper> response) {
                if (response.isSuccess()) {
                    fragment.onTourMessageSent(response.body().getChatMessage());
                }
                else {
                    fragment.onTourMessageSent(null);
                }
            }

            @Override
            public void onFailure(final Call<ChatMessage.ChatMessageWrapper> call, final Throwable t) {
                fragment.onTourMessageSent(null);
            }
        });
    }

    public void getTourEncounters() {
        fragment.showProgressBar();
        if (fragment.tour == null) {
            fragment.onTourEncountersReceived(null);
            return;
        }
        Call<Encounter.EncountersWrapper> call = tourRequest.retrieveTourEncounters(fragment.tour.getId());
        call.enqueue(new Callback<Encounter.EncountersWrapper>() {
            @Override
            public void onResponse(final Call<Encounter.EncountersWrapper> call, final Response<Encounter.EncountersWrapper> response) {
                if (response.isSuccess()) {
                    fragment.onTourEncountersReceived(response.body().getEncounters());
                } else {
                    fragment.onTourEncountersReceived(null);
                }
            }

            @Override
            public void onFailure(final Call<Encounter.EncountersWrapper> call, final Throwable t) {
                fragment.onTourEncountersReceived(null);
            }
        });
    }

}
