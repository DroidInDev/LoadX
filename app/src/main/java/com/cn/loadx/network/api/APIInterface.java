package com.cn.loadx.network.api;

import com.cn.loadx.network.pojo.LoginResponse;
import com.cn.loadx.network.pojo.OnGoingTripDetails;
import com.cn.loadx.network.pojo.ProfileUploadResponse;
import com.cn.loadx.network.pojo.TripStatus;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public interface APIInterface {


    @FormUrlEncoded
    @POST("login")
    Call<LoginResponse> getLoginResponseField(@Field("mobile_number") String mobileNo, @Field("device_token") String token);

    @FormUrlEncoded
    @POST("otpresend")
    Call<LoginResponse> getResendOTP(@Field("mobile_number") String mobileNo);

    @FormUrlEncoded
    @POST("updateprofile")
    Call<ProfileUploadResponse> uploadFile(@Field("driver_id") String id, @Field("driver_name") String driverName, @Field("driver_license") String driverLicense, @Field("driver_pancard") String driverPancard, @Field("driver_photo") String driverPhoto);

    @FormUrlEncoded
    @POST("tripdetails")
    Call<OnGoingTripDetails> getTripDetails(@Field("driver_id") int id);


    @FormUrlEncoded
    @POST("starttrip")
    Call<TripStatus> getTripStatusStart(@Field("trip_id") String tripId);

    @FormUrlEncoded
    @POST("tripcomplete")
    Call<TripStatus> getTripStatusComplete(@Field("trip_id") String tripId);

    @FormUrlEncoded
    @POST("updateloadingdetails")
    Call<TripStatus> getLoadingSTatus(@Field("trip_id") String tripId, @Field("loading_weight_slip") String loadingWeight,@Field("lr_copy") String lrCOpy);

    @FormUrlEncoded
    @POST("updateunloadingdetails")
    Call<TripStatus> getUnLloadingSTatus(@Field("trip_id") String tripId, @Field("unloading_weight_slip") String loadingWeight,@Field("pod_copy") String lrCOpy);

    @FormUrlEncoded
    @POST("roteupdate")
    Call<TripStatus> setRouteUpdateStatus(@Field("trip_id") String tripId, @Field("latitude") String latitude, @Field("longitude") String longitude);


    //https://stackoverflow.com/questions/21026409/fragment-transaction-animation-slide-in-and-slide-out

}


    /*@Headers("Content-Type: application/json")
    @POST("login")
    Call<LoginResponse>getLoginResponse(@Body User user);
    @POST("login")
    Call<LoginResponse> getLoginResponse(@Query(value = "mobile_number",encoded = true) String phoneNumber, @Query(value = "device_token",encoded = true) String token);

*/