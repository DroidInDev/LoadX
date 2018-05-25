package com.cn.loadx.network.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Admin on 08-02-2018.
 */
public class TripDetails {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("trip_status")
    @Expose
    private String tripStatus;
    @SerializedName("truck_id")
    @Expose
    private Integer truckId;
    @SerializedName("unloading_time")
    @Expose
    private Object unloadingTime;
    @SerializedName("unloading_weight")
    @Expose
    private String unloadingWeight;
    @SerializedName("load_accept_id")
    @Expose
    private Integer loadAcceptId;
    @SerializedName("lo_rate")
    @Expose
    private String loRate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTripStatus() {
        return tripStatus;
    }

    public void setTripStatus(String tripStatus) {
        this.tripStatus = tripStatus;
    }

    public Integer getTruckId() {
        return truckId;
    }

    public void setTruckId(Integer truckId) {
        this.truckId = truckId;
    }

    public Object getUnloadingTime() {
        return unloadingTime;
    }

    public void setUnloadingTime(Object unloadingTime) {
        this.unloadingTime = unloadingTime;
    }

    public String getUnloadingWeight() {
        return unloadingWeight;
    }

    public void setUnloadingWeight(String unloadingWeight) {
        this.unloadingWeight = unloadingWeight;
    }

    public Integer getLoadAcceptId() {
        return loadAcceptId;
    }

    public void setLoadAcceptId(Integer loadAcceptId) {
        this.loadAcceptId = loadAcceptId;
    }
}