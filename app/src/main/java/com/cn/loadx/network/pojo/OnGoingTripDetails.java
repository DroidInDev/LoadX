package com.cn.loadx.network.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Admin on 08-02-2018.
 */

public class OnGoingTripDetails {
    @SerializedName("error")
    @Expose
    private Boolean error;
    @SerializedName("response")
    @Expose
    private TripResponse response;
    @SerializedName("message")
    @Expose
    private String message;
    public Boolean getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public void setError(Boolean error) {
        this.error = error;
    }

    public TripResponse getTripResponse() {
        return response;
    }

    public void setTripResponse(TripResponse response) {
        this.response = response;
    }
    public class TripResponse{

        @SerializedName("loadDetails")
        @Expose
        private LoadDetails loadDetails;

        @SerializedName("tripDetails")
        @Expose
        private TripDetails tripDetails;

        @SerializedName("truckDetails")
        @Expose
        private TruckDetails truckDetails;

        public LoadDetails getLoadDetails() {
            return loadDetails;
        }

        public void setLoadDetails(LoadDetails loadDetails) {
            this.loadDetails = loadDetails;
        }

        public TripDetails getTripDetails() {
            return tripDetails;
        }

        public void setTripDetails(TripDetails tripDetails) {
            this.tripDetails = tripDetails;
        }

        public TruckDetails getTruckDetails() {
            return truckDetails;
        }

        public void setTruckDetails(TruckDetails truckDetails) {
            this.truckDetails = truckDetails;
        }
    }
}
