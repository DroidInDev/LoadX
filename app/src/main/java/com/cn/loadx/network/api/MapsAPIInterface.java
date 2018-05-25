package com.cn.loadx.network.api;

import com.cn.loadx.network.mapsPojo.Example;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Admin on 09-02-2018.
 */

public interface MapsAPIInterface {
    @GET("api/directions/json?key=AIzaSyCQY79bv3TA8kD1BfUWI0_TGUhKNFw09Wg")
    Call<Example> getDistanceDuration(@Query("units") String units, @Query("origin") String origin, @Query("destination") String destination, @Query("mode") String mode);
}
