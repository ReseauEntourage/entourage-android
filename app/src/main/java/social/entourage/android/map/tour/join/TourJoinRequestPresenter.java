package social.entourage.android.map.tour.join;

import android.widget.Toast;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.R;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourJoinMessage;
import social.entourage.android.api.model.map.TourUser;

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
        if (message == null || message.trim().length() == 0 || tour == null) {
            fragment.dismiss();
            return;
        }
        TourJoinMessage joinMessage = new TourJoinMessage(message.trim());
        TourJoinMessage.TourJoinMessageWrapper joinMessageWrapper = new TourJoinMessage.TourJoinMessageWrapper();
        joinMessageWrapper.setJoinMessage(joinMessage);
        Call<TourUser.TourUserWrapper> call = tourRequest.updateJoinTourMessage(tour.getId(), joinMessageWrapper);
        call.enqueue(new Callback<TourUser.TourUserWrapper>() {
            @Override
            public void onResponse(final Call<TourUser.TourUserWrapper> call, final Response<TourUser.TourUserWrapper> response) {
                if (response.isSuccess()) {
                    fragment.dismiss();
                    Toast.makeText(fragment.getActivity().getApplicationContext(), R.string.tour_join_request_message_sent, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(fragment.getActivity().getApplicationContext(), R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(final Call<TourUser.TourUserWrapper> call, final Throwable t) {
                Toast.makeText(fragment.getActivity().getApplicationContext(), R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
