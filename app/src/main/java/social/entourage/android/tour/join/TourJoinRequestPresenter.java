package social.entourage.android.tour.join;

import androidx.annotation.NonNull;
import android.widget.Toast;

import java.util.HashMap;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourJoinMessage;
import social.entourage.android.api.model.map.TourUser;
import timber.log.Timber;

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

    @Inject
    EntourageRequest entourageRequest;

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

    protected void sendMessage(String message, FeedItem feedItem) {
        if (fragment == null) {
            return;
        }
        if (message == null || message.trim().length() == 0 || feedItem == null) {
            fragment.dismiss();
            return;
        }
        User me = EntourageApplication.me(fragment.getContext());
        if (me == null) {
            return;
        }
        if (feedItem.getType() == FeedItem.TOUR_CARD) {
            sendMessage(message, (Tour)feedItem);
        }
        else if (feedItem.getType() == FeedItem.ENTOURAGE_CARD) {
            sendMessage(message, (Entourage)feedItem);
        }
    }

    protected void sendMessage(String message, Tour tour) {
        User me = EntourageApplication.me(fragment.getContext());

        TourJoinMessage joinMessage = new TourJoinMessage(message.trim());
        TourJoinMessage.TourJoinMessageWrapper joinMessageWrapper = new TourJoinMessage.TourJoinMessageWrapper();
        joinMessageWrapper.setJoinMessage(joinMessage);
        Call<TourUser.TourUserWrapper> call = tourRequest.updateJoinTourMessage(tour.getUUID(), me.getId(), joinMessageWrapper);
        call.enqueue(new Callback<TourUser.TourUserWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Response<TourUser.TourUserWrapper> response) {
                if (response.isSuccessful()) {
                    if (fragment.getActivity() != null) {
                        Toast.makeText(fragment.getActivity().getApplicationContext(), R.string.tour_join_request_message_sent, Toast.LENGTH_SHORT).show();
                    }
                    fragment.dismiss();
                }
                else {
                    if (fragment.getActivity() != null) {
                        Toast.makeText(fragment.getActivity().getApplicationContext(), R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Throwable t) {
                if (fragment.getActivity() != null) {
                    Toast.makeText(fragment.getActivity().getApplicationContext(), R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void sendMessage(String message, Entourage entourage) {
        User me = EntourageApplication.me(fragment.getContext());

        HashMap<String, Object> info = new HashMap<>();
        HashMap<String, String> messageHashMap = new HashMap<>();
        messageHashMap.put("message", message);
        info.put("request", messageHashMap);

        Call<ResponseBody> call = entourageRequest.updateUserEntourageStatus(entourage.getUUID(), me.getId(), info);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull final Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        if (fragment.getActivity() != null) {
                            Toast.makeText(fragment.getActivity().getApplicationContext(), R.string.tour_join_request_message_sent, Toast.LENGTH_SHORT).show();
                        }
                        fragment.dismiss();
                    }
                    else {
                        if (fragment.getActivity() != null) {
                            Toast.makeText(fragment.getActivity().getApplicationContext(), R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch(IllegalStateException e) {
                    Timber.w(e);
                }
            }

            @Override
            public void onFailure(@NonNull final Call<ResponseBody> call, @NonNull final Throwable t) {
                if (fragment.getActivity() != null) {
                    Toast.makeText(fragment.getActivity().getApplicationContext(), R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
