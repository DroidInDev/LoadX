package com.cn.loadx.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cn.loadx.R;
import com.cn.loadx.customUserInterface.LoadingIndicatorView;
import com.cn.loadx.network.api.APIClient;
import com.cn.loadx.network.api.APIInterface;
import com.cn.loadx.network.pojo.LoginResponse;
import com.cn.loadx.util.ApplicationUtil;
import com.cn.loadx.util.SharedPrefsUtils;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.cn.loadx.notification.FCMConfig.KEY_FCM_TOKEN;
import static com.cn.loadx.notification.FCMConfig.KEY_FCM_TOKEN_REGISTERED;
import static com.cn.loadx.util.AppConstants.DRIVER_PHONE_NO;
import static com.cn.loadx.util.AppConstants.EMPTY_STRING;
import static com.cn.loadx.util.AppConstants.ERROR_LOADING;

/**
 * Created by Admin on 28-01-2018.
 */

public class PhoneNumberActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "PhoneNumberActivity";
    private static final int PHONE_NO_LENGTH = 10;
    private static final int GOOGLE_PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mCredentialsApiClient;
    private static final int RC_HINT = 1000;
    private static final int RC_LOGIN = 3000;
    APIInterface apiInterface;
    protected PhoneNumberUi ui;
    LoadingIndicatorView loadingIndicatorView;
    boolean isPlayServiceAvailable;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        ui = new PhoneNumberUi(findViewById(R.id.phone_number));
        loadingIndicatorView= (LoadingIndicatorView)findViewById(R.id.animatedLoader);
        loadingIndicatorView.smoothToHide();

        //show error dialog if Google Play Services not available
        if (!isGooglePlayServicesAvailable()) {
            Log.d(ApplicationUtil.APPTAG, "Google Play Services not available. Ending Test case.");
         //   Toast.makeText(PhoneNumberActivity.this,"Update play services",Toast.LENGTH_SHORT).show();
            //finish();
        }
        isPlayServiceAvailable = isGooglePlayServicesAvailable();
        try {
            mCredentialsApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.CREDENTIALS_API)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.log(e.getMessage());
        }
    }
    private void showHint() {
       ui.clearKeyboard();
            try {
                HintRequest hintRequest = new HintRequest.Builder()
                        .setHintPickerConfig(new CredentialPickerConfig.Builder()
                                .setShowCancelButton(true)
                                .build())
                        .setPhoneNumberIdentifierSupported(true)
                        .build();

                PendingIntent intent =
                        Auth.CredentialsApi.getHintPickerIntent(mCredentialsApiClient, hintRequest);
                startIntentSenderForResult(intent.getIntentSender(), RC_HINT, null, 0, 0, 0);
            } catch (Exception e) {
                Crashlytics.logException(e);
                Crashlytics.log(e.getMessage());
                Log.e(TAG, "Could not start hint picker Intent", e);
            }
    }
    // Checking if Google Play Services Available or not
    private boolean isGooglePlayServicesAvailable() {
           /* GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
            if(resultCode != ConnectionResult.SUCCESS) {
                if(googleAPI.isUserResolvableError(resultCode)) {
                    googleAPI.showErrorDialogFragment(PhoneNumberActivity.this, resultCode, GOOGLE_PLAY_SERVICES_RESOLUTION_REQUEST);
                }
                //Toast.makeText(PhoneNumberActivity.this,"Update play services",Toast.LENGTH_SHORT).show();
                return false;
            }*/
            return true;
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_HINT) {
            if (resultCode == RESULT_OK) {
                try {
                    Credential cred = data.getParcelableExtra(Credential.EXTRA_KEY);
                    if(!TextUtils.isEmpty(cred.getId()))
                         ui.setPhoneNumber(cred.getId());
                    else
                        ui.focusPhoneNumber();
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    Crashlytics.log("Hint request result+ cred.getId");
                }
            } else {
                ui.focusPhoneNumber();
            }
        }
    }
    class PhoneNumberUi implements View.OnClickListener {
        private final FocusControl phoneFocus;
        private EditText phoneField;
        private TextView submit;

        PhoneNumberUi(View root) {
            phoneField = (EditText) root.findViewById(R.id.eTxtMobileNumber);
            submit = (TextView) root.findViewById(R.id.btnContinue);
            phoneFocus = new FocusControl(phoneField);

            submit.setOnClickListener(this);
            phoneField.setOnClickListener(this);
            setSubmitEnabled(true);
        }

        @Override
        public void onClick(View view) {
            if (view.equals(submit)) {
                //phoneField.setFocusable(false);
                phoneField.setCursorVisible(false);
                ValidatePhoneNumber(getPhoneNumber());
            }
            if (view.equals(phoneField)) {
                phoneField.setEnabled(true);
                phoneField.requestFocus();

                if (TextUtils.isEmpty(getPhoneNumber())) {
                   if(isPlayServiceAvailable)
                       showHint();
                   else
                       focusPhoneNumber();
                }else{
                    focusPhoneNumber();
                }
            }
        }

        void setPhoneNumber(String phoneNumber) {
            phoneField.setText(phoneNumber);
        }

        String getPhoneNumber() {
            return phoneField.getText().toString();
        }

        void focusPhoneNumber() {
          //  phoneField.setCursorVisible(true);
          //  phoneField.setFocusable(true);
            phoneFocus.showKeyboard();
        }

        void clearKeyboard() {
            phoneFocus.hideKeyboard();
        }

        void setSubmitEnabled(boolean enabled) {
            submit.setEnabled(enabled);
        }
    }

    private void ValidatePhoneNumber(String phoneNumber) {
        if(phoneNumber.length()>=PHONE_NO_LENGTH)
        {

            getLoginResponse(phoneNumber);
        }else{
            Toast.makeText(PhoneNumberActivity.this,"Enter valid mobile no",Toast.LENGTH_SHORT).show();
        }
    }


    class FocusControl {
        static final int POST_DELAY = 250;
        private Handler handler;
        private InputMethodManager manager;
        private View focus;

        /**
         * Keyboard focus controller
         *
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
            focus.setEnabled(true);
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

            try {
                if (currentView != null && currentView.equals(focus)) {
                    manager.hideSoftInputFromWindow(currentView.getWindowToken(), 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void getLoginResponse(String phoneNumber) {
        loadingIndicatorView.smoothToShow();
        final String fcmToken = SharedPrefsUtils.getStringPreference(PhoneNumberActivity.this,KEY_FCM_TOKEN);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG,"refreshedToken "+refreshedToken);
        Call<LoginResponse> call = apiInterface.getLoginResponseField(phoneNumber,refreshedToken);
        //Call<LoginResponse> call = apiInterface.getLoginResponse(user);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loadingIndicatorView.smoothToHide();
                LoginResponse loginResponse = response.body();


                if(loginResponse!=null) {
                    Log.d("API Res ",loginResponse.getError()+"");
                    if (loginResponse.getError()) {
                        Log.d("API Res ", loginResponse.getMessage() + "");
                      //  ui.clearKeyboard();

                       // ui.phoneField.requestFocus();
                       // ui.focusPhoneNumber();
                     //   getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                        if (loginResponse.getMessage() != null) {
                            Toast.makeText(PhoneNumberActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PhoneNumberActivity.this, ERROR_LOADING, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (!fcmToken.equals(EMPTY_STRING)) {
                            SharedPrefsUtils.setBooleanPreference(PhoneNumberActivity.this, KEY_FCM_TOKEN_REGISTERED, true);
                        }
                        Log.d(TAG, "refreshedToken res " + loginResponse.getuserData().getDriveDetails().getDeviceToken());
                        if (loginResponse.getuserData().getDriveDetails().getDriverMobileNumber() != null) {
                            SharedPrefsUtils.setStringPreference(PhoneNumberActivity.this, DRIVER_PHONE_NO, loginResponse.getuserData().getDriveDetails().getDriverMobileNumber());
                            Intent intent = new Intent(PhoneNumberActivity.this, OTPActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("PHONE_NO", loginResponse.getuserData().getDriveDetails().getDriverMobileNumber());
                            startActivity(intent);
                            try {
                                PhoneNumberActivity.this.finish();
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("PHONE_NO", e.getMessage());
                            }

                        } else {

                            Toast.makeText(PhoneNumberActivity.this, "Mobile no not updated try again", Toast.LENGTH_SHORT).show();
                        }

                    }
                }else{
                    Toast.makeText(PhoneNumberActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loadingIndicatorView.smoothToHide();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if(loadingIndicatorView!=null)
                loadingIndicatorView.dismissDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*if(mCredentialsApiClient!=null)
            mCredentialsApiClient.disconnect();*/
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
