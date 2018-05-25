package com.cn.loadx.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.cn.loadx.R;
import com.cn.loadx.customUserInterface.LoadingIndicatorView;
import com.cn.loadx.network.api.APIClient;
import com.cn.loadx.network.api.APIInterface;
import com.cn.loadx.network.pojo.LoginResponse;
import com.cn.loadx.util.ApplicationUtil;
import com.cn.loadx.util.SharedPrefsUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.cn.loadx.notification.FCMConfig.KEY_FCM_TOKEN;
import static com.cn.loadx.notification.FCMConfig.KEY_FCM_TOKEN_REGISTERED;
import static com.cn.loadx.util.AppConstants.DRIVER_ID;
import static com.cn.loadx.util.AppConstants.DRIVER_LICENSE_STATUS;
import static com.cn.loadx.util.AppConstants.DRIVER_NAME;
import static com.cn.loadx.util.AppConstants.DRIVER_PHONE_NO;
import static com.cn.loadx.util.AppConstants.EMPTY_STRING;
import static com.cn.loadx.util.AppConstants.ERROR_LOADING;
import static com.cn.loadx.util.AppConstants.KEY_DRI_IMG_URL;
import static com.cn.loadx.util.AppConstants.KEY_IS_PHONE_NO_UPDATED;
import static com.cn.loadx.util.AppConstants.KEY_IS_PROFILE_UPDATED;
import static com.cn.loadx.util.AppConstants.KEY_OTP_VERIFIED;
import static com.cn.loadx.util.AppConstants.KEY_USER_NAME;
import static com.cn.loadx.util.AppConstants.LICENSE_UPDATE_STATUS;
import static com.cn.loadx.util.AppConstants.NOT_UPLOADED;
import static com.cn.loadx.util.AppConstants.PAN_UPDATE_SATUS;
import static com.cn.loadx.util.AppConstants.PHOTO_UPDATE_STATUS;
import static com.cn.loadx.util.AppConstants.START_UP_UPDATE;
import static com.cn.loadx.util.AppConstants.TRUCK_VECHILE_NO;

/**
 * Created by Admin on 28-01-2018.
 */

public class OTPActivity extends AppCompatActivity {
    private static final String TAG = "OTPActivity";
    private GoogleApiClient mCredentialsApiClient;
    private static final int RC_HINT = 1000;
    PinView pinView;
    private FocusControl phoneFocus;
    String phoneNo;
    APIInterface apiInterface;
    LoadingIndicatorView loadingIndicatorView;
    String otpString;
    LoginResponse loginResponse;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        loadingIndicatorView = (LoadingIndicatorView) findViewById(R.id.animatedLoader);
        pinView = (PinView) findViewById(R.id.pinTxt);
        pinView.setEnabled(true);
        pinView.requestFocus();
        phoneFocus = new FocusControl(pinView);
        Intent userIntent = getIntent();
        Bundle extras = userIntent.getExtras();
        if (userIntent.hasExtra("PHONE_NO")) {
            phoneNo = extras.getString("PHONE_NO");
        }
        TextView phNoTxtView = (TextView) findViewById(R.id.mobile_no);
        phNoTxtView.setText(phoneNo+")");
        TextView resendOtpTxt = (TextView) findViewById(R.id.resendOtp);
        resendOtpTxt.setPaintFlags(resendOtpTxt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        getLoginResponse(phoneNo);
    }

    public void getOTPFromUser(View view) {
        phoneFocus.showKeyboard();
        pinView.setText("");
        if (pinView.getText().toString().equals(otpString)) {

        }

    }

    public void validateOtp(View view) {
        if (pinView.length() >= 4) {
            if (pinView.getText().toString().equals(otpString)) {
                SharedPrefsUtils.setBooleanPreference(OTPActivity.this, KEY_IS_PHONE_NO_UPDATED, true);
                String licenseStatus = SharedPrefsUtils.getStringPreference(OTPActivity.this, DRIVER_LICENSE_STATUS);
                SharedPrefsUtils.setBooleanPreference(OTPActivity.this,KEY_OTP_VERIFIED,true);
                if (loginResponse!=null){
                    if(loginResponse.getuserData()!=null) {
                        if(loginResponse.getuserData().getTruckDetails()!=null) {
                            if (loginResponse.getuserData().getTruckDetails().getVehicleNo() != null) {
                                SharedPrefsUtils.setStringPreference(OTPActivity.this, TRUCK_VECHILE_NO, loginResponse.getuserData().getTruckDetails().getVehicleNo());
                            }
                        }
                    }
                }
                handleOtpResponse();
                /* if (licenseStatus.equals(NOT_UPLOADED)) {
                    Intent intent = new Intent(OTPActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    OTPActivity.this.finish();
                } else {
                    if(loginResponse.getuserData()!=null) {
                        if(loginResponse.getuserData().getDriveDetails()!=null) {
                            SharedPrefsUtils.setStringPreference(OTPActivity.this, KEY_DRI_IMG_URL, loginResponse.getuserData().getDriveDetails().getDriverPhoto());
                            SharedPrefsUtils.setBooleanPreference(OTPActivity.this, KEY_IS_PROFILE_UPDATED, true);

                            Intent intent = new Intent(OTPActivity.this, HomeActivity.class);
                            startActivity(intent);
                            OTPActivity.this.finish();
                        }
                    }else{
                        Intent intent = new Intent(OTPActivity.this, UserProfileActivity.class);
                        startActivity(intent);
                        OTPActivity.this.finish();
                    }
                }*/
            }else{
                Toast.makeText(OTPActivity.this, "Enter Valid OTP number", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(OTPActivity.this, "Enter Valid OTP number", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleOtpResponse() {
        if (loginResponse.getuserData().getDriveDetails().getDriverMobileNumber() != null) {
            SharedPrefsUtils.setStringPreference(OTPActivity.this, DRIVER_PHONE_NO, loginResponse.getuserData().getDriveDetails().getDriverMobileNumber());
            SharedPrefsUtils.setIntegerPreference(OTPActivity.this, DRIVER_ID, loginResponse.getuserData().getDriveDetails().getId());
            SharedPrefsUtils.setStringPreference(OTPActivity.this, DRIVER_NAME, loginResponse.getuserData().getDriveDetails().getDriverName());
            if (loginResponse.getLicenseStatus().equals(NOT_UPLOADED) || loginResponse.getPanStatus().equals(NOT_UPLOADED) || loginResponse.getPhotoStatus().equals(NOT_UPLOADED)) {
                Intent userProfileIntent = new Intent(OTPActivity.this, UserProfileActivity.class);
                boolean licenseStatus = loginResponse.getLicenseStatus().equals(NOT_UPLOADED);
                boolean panStatus = loginResponse.getPanStatus().equals(NOT_UPLOADED);
                boolean photoStatus = loginResponse.getPhotoStatus().equals(NOT_UPLOADED);
                userProfileIntent.putExtra(LICENSE_UPDATE_STATUS, licenseStatus);
                userProfileIntent.putExtra(PAN_UPDATE_SATUS, panStatus);
                userProfileIntent.putExtra(PHOTO_UPDATE_STATUS, photoStatus);
                userProfileIntent.putExtra(START_UP_UPDATE, true);
                startActivity(userProfileIntent);
            } else {
                String driverNameStr = SharedPrefsUtils.getStringPreference(OTPActivity.this, KEY_USER_NAME);
                SharedPrefsUtils.setStringPreference(OTPActivity.this, KEY_DRI_IMG_URL, loginResponse.getuserData().getDriveDetails().getDriverPhoto());
                SharedPrefsUtils.setBooleanPreference(OTPActivity.this, KEY_IS_PROFILE_UPDATED, true);

                //show home screen
                Intent intent = new Intent(OTPActivity.this, HomeActivity.class);
                intent.putExtra(KEY_USER_NAME, driverNameStr);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
          }

    public void onResendOTPClicked(View view) {
            pinView.setText("");
        getResendOTPResponse();
    }

    class FocusControl {
        static final int POST_DELAY = 250;
        private Handler handler;
        private InputMethodManager manager;
        private View focus;

        /**
         * Keyboard focus controller
         * <p>
         * Shows and hides the keyboard. Uses a runnable to do the showing as there are race
         * conditions with hiding the keyboard that this solves.
         *
         * @param focus The view element to focus and hide the keyboard from
         */
        public FocusControl(View focus) {
            handler = new Handler();
            manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            this.focus = focus;
        }

        /**
         * Focus the view and show the keyboard.
         */
        public void showKeyboard() {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    focus.requestFocus();
                    manager.showSoftInput(focus, InputMethodManager.SHOW_IMPLICIT);
                }
            }, POST_DELAY);
        }

        /**
         * Hide the keyboard.
         */
        public void hideKeyboard() {
            View currentView = getCurrentFocus();
            if (currentView.equals(focus)) {
                manager.hideSoftInputFromWindow(currentView.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(loadingIndicatorView!=null)
            loadingIndicatorView.dismissDialog();
    }

    private void getLoginResponse(String phoneNumber) {
        loadingIndicatorView.smoothToShow();
        Toast.makeText(OTPActivity.this, "Waiting for OTP", Toast.LENGTH_SHORT).show();

        final String fcmToken = SharedPrefsUtils.getStringPreference(OTPActivity.this, KEY_FCM_TOKEN);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "refreshedToken " + refreshedToken);
        Call<LoginResponse> call = apiInterface.getLoginResponseField(phoneNumber, refreshedToken);
        //Call<LoginResponse> call = apiInterface.getLoginResponse(user);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        loadingIndicatorView.smoothToHide();
                Log.d(ApplicationUtil.APPTAG,"on LOgin res");
                loginResponse = response.body();
                if(loginResponse!=null) {
                    Log.d(ApplicationUtil.APPTAG, loginResponse.getError() + "");
                    if (loginResponse.getError()) {
                        Log.d(ApplicationUtil.APPTAG, loginResponse.getMessage() + "");
                        if (loginResponse.getMessage() != null) {
                            Toast.makeText(OTPActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OTPActivity.this, ERROR_LOADING, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (!fcmToken.equals(EMPTY_STRING)) {
                            SharedPrefsUtils.setBooleanPreference(OTPActivity.this, KEY_FCM_TOKEN_REGISTERED, true);
                        }
                        Log.d(TAG, "refreshedToken res " + loginResponse.getuserData().getDriveDetails().getDeviceToken());
                        otpString = loginResponse.getuserData().getOtp();
                        if (otpString != null) {
                            pinView.setText(otpString);
                        }
                        SharedPrefsUtils.setIntegerPreference(OTPActivity.this, DRIVER_ID, loginResponse.getuserData().getDriveDetails().getId());
                        SharedPrefsUtils.setStringPreference(OTPActivity.this, DRIVER_NAME, loginResponse.getuserData().getDriveDetails().getDriverName());
                        SharedPrefsUtils.setStringPreference(OTPActivity.this, DRIVER_LICENSE_STATUS, loginResponse.getuserData().getDriveDetails().getDriverLicenseStatus());
                        SharedPrefsUtils.setStringPreference(OTPActivity.this, DRIVER_PHONE_NO, loginResponse.getuserData().getDriveDetails().getDriverMobileNumber());

                        Log.d("API Res ", loginResponse.getuserData().getOtp() + "");
                    }
                }else{

                    Toast.makeText(OTPActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loadingIndicatorView.smoothToHide();
            }
        });
    }

    private void getResendOTPResponse() {
        loadingIndicatorView.smoothToShow();
        String phoneNumber = SharedPrefsUtils.getStringPreference(OTPActivity.this,DRIVER_PHONE_NO);
        if(!phoneNumber.equals(EMPTY_STRING)) {
            Call<LoginResponse> call = apiInterface.getResendOTP(phoneNumber);
            //Call<LoginResponse> call = apiInterface.getLoginResponse(user);

            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    loginResponse = response.body();
                    if(loadingIndicatorView!=null)
                        loadingIndicatorView.smoothToHide();
                    if(loginResponse!=null) {
                        Log.d("API Res ", loginResponse.getError() + "");
                        if (loginResponse.getError()) {
                            Log.d("API Res ", loginResponse.getMessage() + "");
                            if (loginResponse.getMessage() != null) {
                                Toast.makeText(OTPActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(OTPActivity.this, ERROR_LOADING, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            otpString = loginResponse.getuserData().getOtp();
                            if (otpString != null) {
                                pinView.setText(otpString);
                            }
                            if(loginResponse.getuserData()!=null) {
                                if(loginResponse.getuserData().getDriveDetails()!=null) {
                                    SharedPrefsUtils.setIntegerPreference(OTPActivity.this, DRIVER_ID, loginResponse.getuserData().getDriveDetails().getId());
                                    SharedPrefsUtils.setStringPreference(OTPActivity.this, DRIVER_NAME, loginResponse.getuserData().getDriveDetails().getDriverName());
                                    SharedPrefsUtils.setStringPreference(OTPActivity.this, DRIVER_LICENSE_STATUS, loginResponse.getuserData().getDriveDetails().getDriverLicenseStatus());
                                    SharedPrefsUtils.setStringPreference(OTPActivity.this, DRIVER_PHONE_NO, loginResponse.getuserData().getDriveDetails().getDriverMobileNumber());
                                }
                            }
                            Log.d("API Res ", loginResponse.getuserData().getOtp() + "");
                        }
                    }else{
                        Toast.makeText(OTPActivity.this,ERROR_LOADING, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    loadingIndicatorView.smoothToHide();
                }
            });
        }
    }
}
