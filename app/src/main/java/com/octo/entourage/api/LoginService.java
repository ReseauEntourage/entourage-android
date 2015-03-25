package com.octo.entourage.api;

import com.octo.entourage.api.model.LoginResponse;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface LoginService {

    @FormUrlEncoded
    @POST("/login.json")
    void login(@Field("email") String email, Callback<LoginResponse> callback);
}
