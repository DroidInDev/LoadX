package com.cn.loadx.network.pojo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Admin on 04-02-2018.
 */

public class User {
    @SerializedName("mobile_number")
    String mobileNumber;
    @SerializedName("device_token")
    String token;

    public User() {

    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public User (String phoneNo,String token){
        this.mobileNumber = phoneNo;
        this.token = token;
    }
}
