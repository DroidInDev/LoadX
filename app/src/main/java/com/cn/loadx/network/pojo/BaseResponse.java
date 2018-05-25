package com.cn.loadx.network.pojo;

/**
 * Created by Admin on 04-02-2018.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class BaseResponse{

    @SerializedName("error")
    @Expose
    private Boolean error;
    @SerializedName("response")
    @Expose
    private Class response;

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public Class getResponse() {
        return response;
    }

    public void setResponse(Class response) {
        this.response = response;
    }

}