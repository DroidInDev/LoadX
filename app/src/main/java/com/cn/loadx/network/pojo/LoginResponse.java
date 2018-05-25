package com.cn.loadx.network.pojo;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Admin on 04-02-2018.
 */

public class LoginResponse {
    @SerializedName("error")
    @Expose
    private Boolean error;
    @SerializedName("response")
    @Expose
    private UserData userData;
    @SerializedName("message")
    @Expose
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public UserData getuserData() {
        return userData;
    }
    public String getLicenseStatus(){
        if(getuserData().getDriveDetails()!=null) {
            if (getuserData().getDriveDetails().getDriverLicenseStatus() != null)
                return getuserData().getDriveDetails().getDriverLicenseStatus();
        }
        return null;
    }
    public String getPanStatus(){
        if(getuserData().getDriveDetails()!=null) {
            if (getuserData().getDriveDetails().getDriverPancardStatus() != null)
                return getuserData().getDriveDetails().getDriverPancardStatus();

        }
        return null;
    }
    public String getPhotoStatus(){
        if(getuserData().getDriveDetails()!=null) {
            if (getuserData().getDriveDetails().getDriverPhotoStatus() != null)
                return getuserData().getDriveDetails().getDriverPhotoStatus();
        }
        return null;
    }
    public void setUserData(UserData userData) {
        this.userData = userData;
    }
    public class UserData {

        @SerializedName("userCount")
        @Expose
        private Integer userCount;
        @SerializedName("otp")
        @Expose
        private String otp;
        @SerializedName("driveDetails")
        @Expose
        private DriveDetails driveDetails;
        @SerializedName("truckDetails")
        @Expose
        private TruckDetails truckDetails;

        public TruckDetails getTruckDetails() {
            return truckDetails;
        }

        public void setTruckDetails(TruckDetails truckDetails) {
            this.truckDetails = truckDetails;
        }

        public Integer getUserCount() {
            return userCount;
        }

        public void setUserCount(Integer userCount) {
            this.userCount = userCount;
        }

        public String getOtp() {
            return otp;
        }

        public void setOtp(String otp) {
            this.otp = otp;
        }

        public DriveDetails getDriveDetails() {
            return driveDetails;
        }

        public void setDriveDetails(DriveDetails driveDetails) {
            this.driveDetails = driveDetails;
        }

    }
}
