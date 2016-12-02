package social.entourage.android.map.tour.join.received;

import java.util.HashMap;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourUser;

public class TourJoinRequestReceivedPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private TourJoinRequestReceivedActivity activity;

    @Inject
    protected TourRequest tourRequest;
    @Inject
    protected EntourageRequest entourageRequest;

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

    protected void acceptTourJoinRequest(long tourId, int userId) {
        HashMap<String, String> status = new HashMap<>();
        status.put("status", Tour.JOIN_STATUS_ACCEPTED);
        HashMap<String, Object> user = new HashMap<>();
        user.put("user", status);
        Call<ResponseBody> call = tourRequest.updateUserTourStatus(tourId, userId, user);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(final Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (activity != null) {
                    if (response.isSuccessful()) {
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

    protected void rejectJoinTourRequest(long tourId, int userId) {
        Call<TourUser.TourUserWrapper> call = tourRequest.removeUserFromTour(tourId, userId);
        call.enqueue(new Callback<TourUser.TourUserWrapper>() {
            @Override
            public void onResponse(final Call<TourUser.TourUserWrapper> call, final Response<TourUser.TourUserWrapper> response) {
                if (activity != null) {
                    if (response.isSuccessful()) {
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

    protected void acceptEntourageJoinRequest(long entourageId, int userId) {
        HashMap<String, String> status = new HashMap<>();
        status.put("status", Tour.JOIN_STATUS_ACCEPTED);
        HashMap<String, Object> user = new HashMap<>();
        user.put("user", status);
        Call<ResponseBody> call = entourageRequest.updateUserEntourageStatus(entourageId, userId, user);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(final Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (activity != null) {
                    if (response.isSuccessful()) {
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

    protected void rejectJoinEntourageRequest(long entourageId, int userId) {
        Call<TourUser.TourUserWrapper> call = entourageRequest.removeUserFromEntourage(entourageId, userId);
        call.enqueue(new Callback<TourUser.TourUserWrapper>() {
            @Override
            public void onResponse(final Call<TourUser.TourUserWrapper> call, final Response<TourUser.TourUserWrapper> response) {
                if (activity != null) {
                    if (response.isSuccessful()) {
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
