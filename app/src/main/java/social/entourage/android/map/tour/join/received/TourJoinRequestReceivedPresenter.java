package social.entourage.android.map.tour.join.received;

import android.support.annotation.NonNull;

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

    protected void acceptTourJoinRequest(String tourUUID, int userId) {
        HashMap<String, String> status = new HashMap<>();
        status.put("status", Tour.JOIN_STATUS_ACCEPTED);
        HashMap<String, Object> user = new HashMap<>();
        user.put("user", status);
        Call<ResponseBody> call = tourRequest.updateUserTourStatus(tourUUID, userId, user);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull final Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                if (activity != null) {
                    if (response.isSuccessful()) {
                        activity.onUserTourStatusChanged(Tour.JOIN_STATUS_ACCEPTED, true);
                    }
                    else {
                        activity.onUserTourStatusChanged(Tour.JOIN_STATUS_ACCEPTED, false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<ResponseBody> call, @NonNull final Throwable t) {
                if (activity != null) {
                    activity.onUserTourStatusChanged(Tour.JOIN_STATUS_ACCEPTED, false);
                }
            }
        });
    }

    protected void rejectJoinTourRequest(String tourUUID, int userId) {
        Call<TourUser.TourUserWrapper> call = tourRequest.removeUserFromTour(tourUUID, userId);
        call.enqueue(new Callback<TourUser.TourUserWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Response<TourUser.TourUserWrapper> response) {
                if (activity != null) {
                    if (response.isSuccessful()) {
                        activity.onUserTourStatusChanged(Tour.JOIN_STATUS_REJECTED, true);
                    } else {
                        activity.onUserTourStatusChanged(Tour.JOIN_STATUS_REJECTED, false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Throwable t) {
                if (activity != null) {
                    activity.onUserTourStatusChanged(Tour.JOIN_STATUS_REJECTED, false);
                }
            }
        });
    }

    protected void acceptEntourageJoinRequest(String entourageUUID, int userId) {
        HashMap<String, String> status = new HashMap<>();
        status.put("status", Tour.JOIN_STATUS_ACCEPTED);
        HashMap<String, Object> user = new HashMap<>();
        user.put("user", status);
        Call<ResponseBody> call = entourageRequest.updateUserEntourageStatus(entourageUUID, userId, user);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull final Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                if (activity != null) {
                    if (response.isSuccessful()) {
                        activity.onUserTourStatusChanged(Tour.JOIN_STATUS_ACCEPTED, true);
                    }
                    else {
                        activity.onUserTourStatusChanged(Tour.JOIN_STATUS_ACCEPTED, false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<ResponseBody> call, @NonNull final Throwable t) {
                if (activity != null) {
                    activity.onUserTourStatusChanged(Tour.JOIN_STATUS_ACCEPTED, false);
                }
            }
        });
    }

    protected void rejectJoinEntourageRequest(String entourageUUID, int userId) {
        Call<TourUser.TourUserWrapper> call = entourageRequest.removeUserFromEntourage(entourageUUID, userId);
        call.enqueue(new Callback<TourUser.TourUserWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Response<TourUser.TourUserWrapper> response) {
                if (activity != null) {
                    if (response.isSuccessful()) {
                        activity.onUserTourStatusChanged(Tour.JOIN_STATUS_REJECTED, true);
                    } else {
                        activity.onUserTourStatusChanged(Tour.JOIN_STATUS_REJECTED, false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Throwable t) {
                if (activity != null) {
                    activity.onUserTourStatusChanged(Tour.JOIN_STATUS_REJECTED, false);
                }
            }
        });
    }

}
