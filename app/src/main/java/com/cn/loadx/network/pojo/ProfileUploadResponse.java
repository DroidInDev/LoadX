package com.cn.loadx.network.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Admin on 07-02-2018.
 */

public class ProfileUploadResponse {
    @SerializedName("error")
    @Expose
    private Boolean error;
    @SerializedName("message")
    @Expose
    private String message;

    public UploadResponse getUploadResponse() {
        return uploadResponse;
    }

    public void setUploadResponse(UploadResponse uploadResponse) {
        this.uploadResponse = uploadResponse;
    }

    @SerializedName("response")
    @Expose
    private UploadResponse uploadResponse;
    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public class UploadResponse{
        public DriveDetails getDriveDetails() {
            return driveDetails;
        }

        public void setDriveDetails(DriveDetails driveDetails) {
            this.driveDetails = driveDetails;
        }

        @SerializedName("driveDetails")
        @Expose
        private DriveDetails driveDetails;
    }
}
