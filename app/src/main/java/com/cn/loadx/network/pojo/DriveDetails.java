
package com.cn.loadx.network.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DriveDetails {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("driver_name")
    @Expose
    private String driverName;
    @SerializedName("driver_license")
    @Expose
    private String driverLicense;
    @SerializedName("driver_license_status")
    @Expose
    private String driverLicenseStatus;
    @SerializedName("driver_pancard")
    @Expose
    private String driverPancard;
    @SerializedName("driver_pancard_status")
    @Expose
    private String driverPancardStatus;
    @SerializedName("driver_photo")
    @Expose
    private String driverPhoto;
    @SerializedName("driver_photo_status")
    @Expose
    private String driverPhotoStatus;
    @SerializedName("driver_mobile_number")
    @Expose
    private String driverMobileNumber;
    @SerializedName("device_token")
    @Expose
    private String deviceToken;
    @SerializedName("last_update")
    @Expose
    private String lastUpdate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverLicense() {
        return driverLicense;
    }

    public void setDriverLicense(String driverLicense) {
        this.driverLicense = driverLicense;
    }

    public String getDriverLicenseStatus() {
        return driverLicenseStatus;
    }

    public void setDriverLicenseStatus(String driverLicenseStatus) {
        this.driverLicenseStatus = driverLicenseStatus;
    }

    public String getDriverPancard() {
        return driverPancard;
    }

    public void setDriverPancard(String driverPancard) {
        this.driverPancard = driverPancard;
    }

    public String getDriverPancardStatus() {
        return driverPancardStatus;
    }

    public void setDriverPancardStatus(String driverPancardStatus) {
        this.driverPancardStatus = driverPancardStatus;
    }

    public String getDriverPhoto() {
        return driverPhoto;
    }

    public void setDriverPhoto(String driverPhoto) {
        this.driverPhoto = driverPhoto;
    }

    public String getDriverPhotoStatus() {
        return driverPhotoStatus;
    }

    public void setDriverPhotoStatus(String driverPhotoStatus) {
        this.driverPhotoStatus = driverPhotoStatus;
    }

    public String getDriverMobileNumber() {
        return driverMobileNumber;
    }

    public void setDriverMobileNumber(String driverMobileNumber) {
        this.driverMobileNumber = driverMobileNumber;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

}
