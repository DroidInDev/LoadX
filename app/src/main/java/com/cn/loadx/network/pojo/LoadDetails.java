package com.cn.loadx.network.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Admin on 08-02-2018.
 */

public class LoadDetails implements Parcelable {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("source_address")
    @Expose
    private String sourceAddress;
    @SerializedName("source_lat")
    @Expose
    private String sourceLat;
    @SerializedName("source_long")
    @Expose
    private String sourceLong;
    @SerializedName("destination_address")
    @Expose
    private String destinationAddress;
    @SerializedName("dest_lat")
    @Expose
    private String destLat;
    @SerializedName("dest_long")
    @Expose
    private String destLong;
    @SerializedName("load_status")
    @Expose
    private String loadStatus;
    @SerializedName("quantity")
    @Expose
    private String quantity;
    @SerializedName("meterial_name")
    @Expose
    private String meterialName;
    @SerializedName("supervisor_at_source_name")
    @Expose
    private String supervisorAtSourceName;
    @SerializedName("supervisor_at_source_contact")
    @Expose
    private String supervisorAtSourceContact;
    @SerializedName("lo_id")
    @Expose
    private Integer loId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getSourceLat() {
        return sourceLat;
    }

    public void setSourceLat(String sourceLat) {
        this.sourceLat = sourceLat;
    }

    public String getSourceLong() {
        return sourceLong;
    }

    public void setSourceLong(String sourceLong) {
        this.sourceLong = sourceLong;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getDestLat() {
        return destLat;
    }

    public void setDestLat(String destLat) {
        this.destLat = destLat;
    }

    public String getDestLong() {
        return destLong;
    }

    public void setDestLong(String destLong) {
        this.destLong = destLong;
    }

    public String getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(String loadStatus) {
        this.loadStatus = loadStatus;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getMeterialName() {
        return meterialName;
    }

    public void setMeterialName(String meterialName) {
        this.meterialName = meterialName;
    }

    public String getSupervisorAtSourceName() {
        return supervisorAtSourceName;
    }

    public void setSupervisorAtSourceName(String supervisorAtSourceName) {
        this.supervisorAtSourceName = supervisorAtSourceName;
    }

    public String getSupervisorAtSourceContact() {
        return supervisorAtSourceContact;
    }

    public void setSupervisorAtSourceContact(String supervisorAtSourceContact) {
        this.supervisorAtSourceContact = supervisorAtSourceContact;
    }

    public Integer getLoId() {
        return loId;
    }

    public void setLoId(Integer loId) {
        this.loId = loId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.sourceAddress);
        dest.writeString(this.sourceLat);
        dest.writeString(this.sourceLong);
        dest.writeString(this.destinationAddress);
        dest.writeString(this.destLat);
        dest.writeString(this.destLong);
        dest.writeString(this.loadStatus);
        dest.writeString(this.quantity);
        dest.writeString(this.meterialName);
        dest.writeString(this.supervisorAtSourceName);
        dest.writeString(this.supervisorAtSourceContact);
        dest.writeValue(this.loId);
    }

    public LoadDetails() {
    }

    protected LoadDetails(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.sourceAddress = in.readString();
        this.sourceLat = in.readString();
        this.sourceLong = in.readString();
        this.destinationAddress = in.readString();
        this.destLat = in.readString();
        this.destLong = in.readString();
        this.loadStatus = in.readString();
        this.quantity = in.readString();
        this.meterialName = in.readString();
        this.supervisorAtSourceName = in.readString();
        this.supervisorAtSourceContact = in.readString();
        this.loId = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Parcelable.Creator<LoadDetails> CREATOR = new Parcelable.Creator<LoadDetails>() {
        @Override
        public LoadDetails createFromParcel(Parcel source) {
            return new LoadDetails(source);
        }

        @Override
        public LoadDetails[] newArray(int size) {
            return new LoadDetails[size];
        }
    };
}