package social.entourage.android.map.tour.join;

import android.widget.Toast;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.R;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.map.tour.information.TourInformationFragment;

/**
 * Created by mihaiionescu on 07/03/16.
 */
public class TourJoinRequestPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final TourJoinRequestFragment fragment;

    @Inject
    TourRequest tourRequest;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public TourJoinRequestPresenter(final TourJoinRequestFragment fragment) {
        this.fragment = fragment;
    }

    // ----------------------------------
    // API CALLS
    // ----------------------------------

    protected void sendMessage(String message, Tour tour) {
        if (message == null || message.trim().length() == 0) {
            fragment.dismiss();
            return;
        }
        ChatMessage chatMessage = new ChatMessage(message);
        ChatMessage.ChatMessageWrapper chatMessageWrapper = new ChatMessage.ChatMessageWrapper();
        chatMessageWrapper.setChatMessage(chatMessage);
        Call<ChatMessage.ChatMessageWrapper> call = tourRequest.chatMessage(tour.getId(), chatMessageWrapper);
        call.enqueue(new Callback<ChatMessage.ChatMessageWrapper>() {
            @Override
            public void onResponse(final Call<ChatMessage.ChatMessageWrapper> call, final Response<ChatMessage.ChatMessageWrapper> response) {
                if (response.isSuccess()) {
                    fragment.dismiss();
                    Toast.makeText(fragment.getActivity().getApplicationContext(), R.string.tour_join_request_message_sent, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(fragment.getActivity().getApplicationContext(), R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(final Call<ChatMessage.ChatMessageWrapper> call, final Throwable t) {
                Toast.makeText(fragment.getActivity().getApplicationContext(), R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
