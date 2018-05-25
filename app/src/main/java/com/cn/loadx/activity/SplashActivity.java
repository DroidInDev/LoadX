package com.cn.loadx.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cn.loadx.R;
import com.cn.loadx.network.api.APIClient;
import com.cn.loadx.network.api.APIInterface;
import com.cn.loadx.network.pojo.LoginResponse;
import com.cn.loadx.util.ApplicationUtil;
import com.cn.loadx.util.ConnectivityReceiver;
import com.cn.loadx.util.SharedPrefsUtils;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.cn.loadx.notification.FCMConfig.KEY_FCM_TOKEN;
import static com.cn.loadx.notification.FCMConfig.KEY_FCM_TOKEN_REGISTERED;
import static com.cn.loadx.util.AppConstants.DRIVER_ID;
import static com.cn.loadx.util.AppConstants.DRIVER_NAME;
import static com.cn.loadx.util.AppConstants.DRIVER_PHONE_NO;
import static com.cn.loadx.util.AppConstants.EMPTY_STRING;
import static com.cn.loadx.util.AppConstants.ERROR_LOADING;
import static com.cn.loadx.util.AppConstants.KEY_IS_PHONE_NO_UPDATED;
import static com.cn.loadx.util.AppConstants.KEY_IS_PROFILE_UPDATED;
import static com.cn.loadx.util.AppConstants.KEY_OTP_VERIFIED;
import static com.cn.loadx.util.AppConstants.KEY_USER_NAME;
import static com.cn.loadx.util.AppConstants.KEY_VERSION_NAME;
import static com.cn.loadx.util.AppConstants.LICENSE_UPDATE_STATUS;
import static com.cn.loadx.util.AppConstants.NOT_UPLOADED;
import static com.cn.loadx.util.AppConstants.PAN_UPDATE_SATUS;
import static com.cn.loadx.util.AppConstants.PHOTO_UPDATE_STATUS;
import static com.cn.loadx.util.AppConstants.START_UP_UPDATE;

/**
 * Created by Admin on 27-01-2018.
 */

public class SplashActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener,EasyPermissions.PermissionCallbacks {
    private static final String[] LOCATION_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int RC_LOCATION_PERM = 1001;
    APIInterface apiInterface;
    ProgressBar progressBar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progressBar = (ProgressBar)findViewById(R.id.progressBarHorizontal);
        progressBar.setVisibility(View.INVISIBLE);
        apiInterface = APIClient.getClient().create(APIInterface.class);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetworkConnection();
    }

    private void checkNetworkConnection() {
        boolean isNetworkConnected = ConnectivityReceiver.isConnected();
        if(!isNetworkConnected){
            ApplicationUtil.getmInstance().checkUrInterntConnection(this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Toast.makeText(SplashActivity.this,"Hi",Toast.LENGTH_SHORT).show();
                    checkNetworkConnection();
                }
            }).show();
        }else{
            checkLocationPermission();
        }
    }

    private void checkLocationPermission() {
        boolean hasLocationPermission = EasyPermissions.hasPermissions(this,LOCATION_PERMISSION);
        if(!hasLocationPermission){
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location),
                    RC_LOCATION_PERM,
                    LOCATION_PERMISSION);
        }else{
            handleNotification();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if(requestCode==RC_LOCATION_PERM){
            handleNotification();
        }
    }
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }else{
            finish();
        }
    }

    private void checkPhoneNumberVerified() {
                    try
                    {
                        String app_ver = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                        SharedPrefsUtils.setStringPreference(SplashActivity.this,KEY_VERSION_NAME,app_ver);
                    }
                    catch (PackageManager.NameNotFoundException e)
                    {
                        Log.e("LoadX", e.getMessage());
                    }

                    boolean isPhoneNumberVerified = SharedPrefsUtils.getBooleanPreference(SplashActivity.this, KEY_IS_PHONE_NO_UPDATED, false);
                    boolean isProfileUpdated = SharedPrefsUtils.getBooleanPreference(SplashActivity.this, KEY_IS_PROFILE_UPDATED, false);
                    String driverPhoneNo = SharedPrefsUtils.getStringPreference(SplashActivity.this,DRIVER_PHONE_NO);
                    boolean isOtpVerified = SharedPrefsUtils.getBooleanPreference(SplashActivity.this,KEY_OTP_VERIFIED,false);
                    if(!isPhoneNumberVerified&& TextUtils.isEmpty(driverPhoneNo)){
                        Intent userProfileIntent = new Intent(SplashActivity.this,PhoneNumberActivity.class);
                        userProfileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(userProfileIntent);
                        finish();
                    }else if(!TextUtils.isEmpty(driverPhoneNo)&&!isOtpVerified){
                        Intent otpIntent = new Intent(SplashActivity.this,OTPActivity.class);
                        otpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        otpIntent.putExtra("PHONE_NO",driverPhoneNo);
                        startActivity(otpIntent);
                        finish();
                    }
                    else {
                        checkProfileStatus();
                    }/*if(!isProfileUpdated){
                        Intent userProfileIntent = new Intent(SplashActivity.this,UserProfileActivity.class);
                        startActivity(userProfileIntent);
                        finish();
                    }else if(isPhoneNumberVerified&&isProfileUpdated) {
                        String driverNameStr = SharedPrefsUtils.getStringPreference(SplashActivity.this, KEY_USER_NAME);
                        //show home screen
                        Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
                        intent.putExtra(KEY_USER_NAME,driverNameStr);
                        startActivity(intent);
                        finish();
                    }
                    }*/

                    // close this activity
    }

    private void checkProfileStatus() {
        String phoneNo =SharedPrefsUtils.getStringPreference(SplashActivity.this, DRIVER_PHONE_NO);
        getLoginResponse(phoneNo);
    }

    private void handleNotification() {
        Intent nIntent = getIntent();
        Bundle bundle = nIntent.getExtras();

        JSONObject notifObject;
        String page = null;
        String type = null;
        if (bundle != null && nIntent.hasExtra("extra")) {
            String extra = nIntent.getStringExtra("extra");
            try {
                notifObject = new JSONObject(extra);
                if(ApplicationUtil.contains(notifObject,"page"))
                page = notifObject.getString("page");
                if(ApplicationUtil.contains(notifObject,"type"))
                type = notifObject.getString("type");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(page!=null&&type!=null){
                if(page.equals("profile")){
                    boolean isPhoneNumberVerified = SharedPrefsUtils.getBooleanPreference(SplashActivity.this, KEY_IS_PHONE_NO_UPDATED, false);
                    if(isPhoneNumberVerified){
                        Intent intent = new Intent(SplashActivity.this,UserProfileActivity.class);
                        //intent.putExtra("type",type);
                        intent.putExtra("UPDATE",type);
                        startActivity(intent);
                    }
                }
                else if(page.equals("home")){
                    handleHomeIntent(page,type);
                }
            }else{
                checkPhoneNumberVerified();
            }
        }else if(bundle!=null&&nIntent.hasExtra("page")){
            if(nIntent.hasExtra("type"))
                type =nIntent.getStringExtra("type");
            page = nIntent.getStringExtra("page");
            boolean isPhoneNumberVerified = SharedPrefsUtils.getBooleanPreference(SplashActivity.this, KEY_IS_PHONE_NO_UPDATED, false);
            if(page.equals("profile")){
                if(isPhoneNumberVerified) {
                    Intent intent = new Intent(SplashActivity.this, UserProfileActivity.class);
                    //intent.putExtra("type",type);
                    intent.putExtra("UPDATE", type);
                    startActivity(intent);
                }
            }else {
                handleHomeIntent(page, type);
            }
        }
        else{
            Log.d("LDHOME","bundle null ");
            checkPhoneNumberVerified();
        }
    }

    private void handleHomeIntent(String page,String type) {
        Log.d("LDHOME","type "+type);
        Log.d("LDHOME","page "+page);
        boolean isPhoneNumberVerified = SharedPrefsUtils.getBooleanPreference(SplashActivity.this, KEY_IS_PHONE_NO_UPDATED, false);
        boolean isProfileUpdated = SharedPrefsUtils.getBooleanPreference(SplashActivity.this,KEY_IS_PROFILE_UPDATED,false);
        if(isPhoneNumberVerified)
        {
            if(isProfileUpdated)
            {
                Intent homeIntent = new Intent(SplashActivity.this,HomeActivity.class);
                homeIntent.putExtra("UPDATE",type);
                startActivity(homeIntent);
            }
        }
    }
    private void getLoginResponse(String phoneNumber) {
        progressBar.setVisibility(View.VISIBLE);
        final String fcmToken = SharedPrefsUtils.getStringPreference(SplashActivity.this,KEY_FCM_TOKEN);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Call<LoginResponse> call = apiInterface.getLoginResponseField(phoneNumber,refreshedToken);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse loginResponse = response.body();
                if(loginResponse!=null) {
                    Log.d("API Res ",loginResponse.getError()+"");
                    if (loginResponse.getError()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d("API Res ", loginResponse.getMessage() + "");
                        if (loginResponse.getMessage() != null) {
                            Toast.makeText(SplashActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SplashActivity.this, ERROR_LOADING, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        if (!fcmToken.equals(EMPTY_STRING)) {
                            SharedPrefsUtils.setBooleanPreference(SplashActivity.this, KEY_FCM_TOKEN_REGISTERED, true);
                        }
                        Log.d(ApplicationUtil.APPTAG, "refreshedToken res " + loginResponse.getuserData().getDriveDetails().getDeviceToken());
                        if (loginResponse.getuserData().getDriveDetails().getDriverMobileNumber() != null) {
                            SharedPrefsUtils.setStringPreference(SplashActivity.this, DRIVER_PHONE_NO, loginResponse.getuserData().getDriveDetails().getDriverMobileNumber());
                            SharedPrefsUtils.setIntegerPreference(SplashActivity.this, DRIVER_ID, loginResponse.getuserData().getDriveDetails().getId());
                            SharedPrefsUtils.setStringPreference(SplashActivity.this, DRIVER_NAME, loginResponse.getuserData().getDriveDetails().getDriverName());
                            if(loginResponse.getLicenseStatus().equals(NOT_UPLOADED)||loginResponse.getPanStatus().equals(NOT_UPLOADED)||loginResponse.getPhotoStatus().equals(NOT_UPLOADED))
                          {
                              Intent userProfileIntent = new Intent(SplashActivity.this,UserProfileActivity.class);
                              boolean licenseStatus = loginResponse.getLicenseStatus().equals(NOT_UPLOADED);
                              boolean panStatus = loginResponse.getPanStatus().equals(NOT_UPLOADED);
                              boolean photoStatus = loginResponse.getPhotoStatus().equals(NOT_UPLOADED);
                              userProfileIntent.putExtra(LICENSE_UPDATE_STATUS,licenseStatus);
                              userProfileIntent.putExtra(PAN_UPDATE_SATUS,panStatus);
                              userProfileIntent.putExtra(PHOTO_UPDATE_STATUS,photoStatus);
                              userProfileIntent.putExtra(START_UP_UPDATE,true);
                              startActivity(userProfileIntent);
                          }else{
                              String driverNameStr = SharedPrefsUtils.getStringPreference(SplashActivity.this, KEY_USER_NAME);
                              //show home screen
                              Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
                              intent.putExtra(KEY_USER_NAME,driverNameStr);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                              startActivity(intent);
                              finish();
                          }
                            try {
                                SplashActivity.this.finish();
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("PHONE_NO", e.getMessage());
                            }
                        } else {
                            Toast.makeText(SplashActivity.this, "Mobile no not updated try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SplashActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
