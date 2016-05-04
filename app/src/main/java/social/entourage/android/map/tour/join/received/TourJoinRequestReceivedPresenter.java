package social.entourage.android.map.tour.join.received;

import android.util.ArrayMap;

import com.squareup.okhttp.ResponseBody;

import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourUser;

/**
 * Created by mihaiionescu on 18/03/16.
 */
public class TourJoinRequestReceivedPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private TourJoinRequestReceivedActivity activity;

    @Inject
    protected TourRequest tourRequest;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public TourJoinRequestReceivedPresenter(final TourJoinRequestReceivedActivity activity) {
        this.activity = activity;
    }

    // ----------------------------------
    // API CALLS
    // ----------------------------------

    protected void acceptJoinRequest(long tourId, int userId) {
        HashMap<String, String> status = new HashMap<>();
        status.put("status", Tour.JOIN_STATUS_ACCEPTED);
        HashMap<String, Object> user = new HashMap<>();
        user.put("user", status);
        Call<ResponseBody> call = tourRequest.updateUserTourStatus(tourId, userId, user);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(final Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (activity != null) {
                    if (response.isSuccess()) {
                        activity.onUserTourStatusChanged(true);
                    }
                    else {
                        activity.onUserTourStatusChanged(false);
                    }
                }
            }

            @Override
            public void onFailure(final Call<ResponseBody> call, final Throwable t) {
                if (activity != null) {
                    activity.onUserTourStatusChanged(false);
                }
            }
        });
    }

    protected void rejectJoinRequest(long tourId, int userId) {
        Call<TourUser.TourUserWrapper> call = tourRequest.removeUserFromTour(tourId, userId);
        call.enqueue(new Callback<TourUser.TourUserWrapper>() {
            @Override
            public void onResponse(final Call<TourUser.TourUserWrapper> call, final Response<TourUser.TourUserWrapper> response) {
                if (activity != null) {
                    if (response.isSuccess()) {
                        activity.onUserTourStatusChanged(true);
                    } else {
                        activity.onUserTourStatusChanged(false);
                    }
                }
            }

            @Override
            public void onFailure(final Call<TourUser.TourUserWrapper> call, final Throwable t) {
                if (activity != null) {
                    activity.onUserTourStatusChanged(false);
                }
            }
        });
    }

}
