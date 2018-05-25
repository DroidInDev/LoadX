package com.cn.loadx.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.cn.loadx.R;
import com.cn.loadx.customUserInterface.LoadingIndicatorView;
import com.cn.loadx.fragments.LoadUnLoadFragment;
import com.cn.loadx.network.api.APIClient;
import com.cn.loadx.network.api.APIInterface;
import com.cn.loadx.network.pojo.ProfileUploadResponse;
import com.cn.loadx.network.pojo.User;
import com.cn.loadx.network.util.AWSConstants;
import com.cn.loadx.network.util.AWSUtil;
import com.cn.loadx.util.ApplicationUtil;
import com.cn.loadx.util.FilePathUtil;
import com.cn.loadx.util.FileUtil;
import com.cn.loadx.util.SharedPrefsUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import id.zelory.compressor.Compressor;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.cn.loadx.network.util.AWSConstants.AWS_UPLOAD_STATE_COMPLETE;
import static com.cn.loadx.network.util.AWSConstants.AWS_UPLOAD_STATE_FAILED;
import static com.cn.loadx.util.AppConstants.APP_TEMP_PATH;
import static com.cn.loadx.util.AppConstants.DRIVER_ID;
import static com.cn.loadx.util.AppConstants.DRIVER_LICENSE_IMG;
import static com.cn.loadx.util.AppConstants.DRIVER_NAME;
import static com.cn.loadx.util.AppConstants.EMPTY_STRING;
import static com.cn.loadx.util.AppConstants.KEY_DRI_IMG_URL;
import static com.cn.loadx.util.AppConstants.KEY_IS_PROFILE_UPDATED;
import static com.cn.loadx.util.AppConstants.LICENSE_UPDATE_STATUS;
import static com.cn.loadx.util.AppConstants.LR;
import static com.cn.loadx.util.AppConstants.LWS;
import static com.cn.loadx.util.AppConstants.PAN_IMG;
import static com.cn.loadx.util.AppConstants.PAN_UPDATE_SATUS;
import static com.cn.loadx.util.AppConstants.PHOTO_UPDATE_STATUS;
import static com.cn.loadx.util.AppConstants.POD;
import static com.cn.loadx.util.AppConstants.START_UP_UPDATE;
import static com.cn.loadx.util.AppConstants.ULWS;
import static com.cn.loadx.util.AppConstants.USER_PHOTO_IMG;

/**
 * Created by Admin on 28-01-2018.
 */

public class UserProfileActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final String[] STORAGE_PERMISSION = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int RC_STORAGE_PERM = 1002;
    private static final int PICK_PAN_IMG_REQ = 3000;
    private static final int PICK_LICENSE_IMG_REQ = 3001;
    private static final int PICK_PHOTO_IMG_REQ = 3002;
    private static final String TAG = "UserProfileActivity";
    ImageView imgLicense;
    private ImageView imgPan;
    private ImageView imgPhoto;
    private TextView txtLicemnse;
    private TextView txtPan;
    private TextView txtPhoto;
    private TextView txtDriverName;
    boolean hasStoragePermission;
    HashMap<Integer, String> hashMap;
    HashMap<String,String> imgFIleNameMap;
    APIInterface apiInterface;
    LoadingIndicatorView loadingIndicatorView;
    private File actualImage;
    private File compressedImage;
    private RelativeLayout layoutPANCopy;
    private RelativeLayout layoutPhoto;
    String update =null;
    private RelativeLayout layoutLicense;
    private EditText txtLicenseNo;
    private RelativeLayout layoutLicenseNo;
    //AWS declaration
// The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility transferUtility;
    // Reference to the utility class
    private AWSUtil awsUtil;
    boolean enableSkip;
    private TextView skipBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        enableSkip =false;
        apiInterface = APIClient.getClient().create(APIInterface.class);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        toolbar.setTitle("Profile"); // set Title for Toolbar
        toolbar.setTitleTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.white));
        toolbar.setLogo(R.drawable.x_logo_white); // set logo for Toolbar
        setSupportActionBar(toolbar); // Setting/replace toolbar as the ActionBar
        loadingIndicatorView = (LoadingIndicatorView) findViewById(R.id.animatedLoader);
        loadingIndicatorView.smoothToHide();
        imgLicense = (ImageView) findViewById(R.id.imgGetLicence);
        imgPan = (ImageView) findViewById(R.id.imgPanCopy);
        imgPhoto = (ImageView) findViewById(R.id.imgPhoto);
        layoutPANCopy  =(RelativeLayout)findViewById(R.id.layoutPanCopy);
        layoutPhoto =(RelativeLayout)findViewById(R.id.layoutPhoto);
        layoutLicense  = (RelativeLayout)findViewById(R.id.layoutLicense);
       /* imgLicense.setEnabled(false);
        imgPan.setEnabled(false);
        imgPhoto.setEnabled(false);*/
        txtLicemnse = (TextView) findViewById(R.id.txtLicense);
        txtPan = (TextView) findViewById(R.id.txtPanCopy);
        txtPhoto = (TextView) findViewById(R.id.txtPhoto);
        txtDriverName = (TextView) findViewById(R.id.txtDriverName);
        //txtLicenseNo =(EditText)findViewById(R.id.txtDriverLicenseNo);
        //layoutLicenseNo = (RelativeLayout)findViewById(R.id.layoutLicenseNo);
        String driverName = SharedPrefsUtils.getStringPreference(UserProfileActivity.this,DRIVER_NAME);
        txtDriverName.setText(driverName);
        skipBtn = findViewById(R.id.btnSkip);
        skipBtn.setVisibility(View.INVISIBLE);

        initAWS();
        Intent userIntent = getIntent();
        Bundle extras = userIntent.getExtras();
//Handling intents from notification
        if (userIntent.hasExtra("UPDATE")) {
            txtDriverName.setFocusable(false);
            update = extras.getString("UPDATE").toLowerCase();
        }
        if(update!=null) {
            if (update.equals("license")) {
                layoutPANCopy.setVisibility(View.GONE);
                layoutPhoto.setVisibility(View.GONE);
            }else if(update.equals("photo")){
               // layoutLicenseNo.setVisibility(View.GONE);
                layoutPANCopy.setVisibility(View.GONE);
                layoutLicense.setVisibility(View.GONE);

            }else if(update.equals("pan")){
              //  layoutLicenseNo.setVisibility(View.GONE);
                layoutPhoto.setVisibility(View.GONE);
                layoutLicense.setVisibility(View.GONE);
            }
        }
        //Handling intents on Startup (ie second time when app opens) true- not updated ,false - updated
        if(userIntent.hasExtra(START_UP_UPDATE)){
            update=START_UP_UPDATE;
            if(!userIntent.getBooleanExtra(LICENSE_UPDATE_STATUS,false))
            {
                layoutLicense.setVisibility(View.GONE);
                skipBtn.setVisibility(View.VISIBLE);
                enableSkip = true;
            }
            if(!userIntent.getBooleanExtra(PAN_UPDATE_SATUS,false)){
                layoutPANCopy.setVisibility(View.GONE);
            }
            if(!userIntent.getBooleanExtra(PHOTO_UPDATE_STATUS,false)){
                layoutPhoto.setVisibility(View.GONE);
            }
        }
        hasStoragePermission = false;
        hashMap = new HashMap();
        imgFIleNameMap = new HashMap<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //checkStoragePermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        layoutPANCopy.setVisibility(View.GONE);
        layoutPhoto.setVisibility(View.GONE);
        super.onNewIntent(intent);
    }

    private void checkStoragePermission(int permissionCode, String permissionReqTxt) {
        hasStoragePermission = EasyPermissions.hasPermissions(this, STORAGE_PERMISSION);
        if (!hasStoragePermission) {
            EasyPermissions.requestPermissions(
                    this,permissionReqTxt,
                    permissionCode,
                    STORAGE_PERMISSION);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
       // enableImageFetchButtons();
        switch (requestCode){
            case PICK_LICENSE_IMG_REQ:
                pickImageFromGallery(PICK_LICENSE_IMG_REQ);
                break;
            case PICK_PAN_IMG_REQ:
                pickImageFromGallery(PICK_PAN_IMG_REQ);
                break;
            case PICK_PHOTO_IMG_REQ:
                pickImageFromGallery(PICK_PHOTO_IMG_REQ);
                break;
                default:
                    break;
        }

    }

    private void enableImageFetchButtons() {
        imgLicense.setEnabled(true);
        imgPan.setEnabled(true);
        imgPhoto.setEnabled(true);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    public void getPanCopyImage(View view) {
        if (hasStoragePermission) {
            pickImageFromGallery(PICK_PAN_IMG_REQ);
        } else {
            checkStoragePermission(PICK_PAN_IMG_REQ,getString(R.string.rationale_storage_pan));
        }
    }

    public void getLicenseCopyImage(View view) {
        if (hasStoragePermission) {
            pickImageFromGallery(PICK_LICENSE_IMG_REQ);
        } else {
            checkStoragePermission(PICK_LICENSE_IMG_REQ, getString(R.string.rationale_storage_license));
        }
    }


    public void getUserPhoto(View view) {
        if (hasStoragePermission) {
            pickImageFromGallery(PICK_PHOTO_IMG_REQ);
        } else {
            checkStoragePermission(PICK_PHOTO_IMG_REQ, getString(R.string.rationale_storage_photo));
        }
    }

    public void submitProfileData(View view) {
        String driverNameStr = txtDriverName.getText().toString();
        String licenseCopyStr = txtLicemnse.getText().toString();
        String panCopyStr = txtPan.getText().toString();
        String photoStr = txtPhoto.getText().toString();
     //   String driverLicense = txtLicenseNo.getText().toString();
        if(update==null) {
            if (driverNameStr.length() >= 5 && licenseCopyStr.length() > 0) {
                if(loadingIndicatorView!=null)
                    loadingIndicatorView.smoothToShow();
                uploadImageToAWS(hashMap.get(PICK_LICENSE_IMG_REQ),DRIVER_LICENSE_IMG);
            } else if (driverNameStr.length() < 5) {
                Toast.makeText(UserProfileActivity.this, "Name should be more than 5 characters", Toast.LENGTH_SHORT).show();
            }
            else if(licenseCopyStr.length() == 0){
                Toast.makeText(UserProfileActivity.this, "Choose your license copy to submit", Toast.LENGTH_SHORT).show();
            }
        }else{
            if(update.equals("license")&&licenseCopyStr.length()==0){
                if(licenseCopyStr.length()==0)
                    Toast.makeText(UserProfileActivity.this, "Choose your license copy to submit", Toast.LENGTH_SHORT).show();

            } if(update.equals("photo")&&photoStr.length()==0){
                    Toast.makeText(UserProfileActivity.this, "Choose your photo  to submit", Toast.LENGTH_SHORT).show();
            } if(update.equals("pan")&&panCopyStr.length()==0){
                    Toast.makeText(UserProfileActivity.this, "Choose your pan copy to submit", Toast.LENGTH_SHORT).show();
            }else if(update.equals(START_UP_UPDATE)) {
                if(layoutLicense.getVisibility()==View.VISIBLE){
                    if(licenseCopyStr.length() == 0){
                        Toast.makeText(UserProfileActivity.this, "Choose your license copy to submit", Toast.LENGTH_SHORT).show();
                    }else{
                        if(loadingIndicatorView!=null)
                            loadingIndicatorView.smoothToShow();
                        uploadImageToAWS(hashMap.get(PICK_LICENSE_IMG_REQ),DRIVER_LICENSE_IMG);
                    }
                }else if(layoutPANCopy.getVisibility()==View.VISIBLE){
                    if(panCopyStr.length()==0) {
                        Toast.makeText(UserProfileActivity.this, "Choose your pan copy to submit", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if(loadingIndicatorView!=null)
                            loadingIndicatorView.smoothToShow();
                        uploadImageToAWS(hashMap.get(PICK_PAN_IMG_REQ), PAN_IMG);
                    }
                }else if(layoutPhoto.getVisibility()==View.VISIBLE)
                {
                    if(photoStr.length()==0) {
                        Toast.makeText(UserProfileActivity.this, "Choose your photo  to submit", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if(loadingIndicatorView!=null)
                            loadingIndicatorView.smoothToShow();
                        uploadImageToAWS(hashMap.get(PICK_PHOTO_IMG_REQ), USER_PHOTO_IMG);
                    }
                }
            }else{
                    if(loadingIndicatorView!=null)
                        loadingIndicatorView.smoothToShow();
                    if(update.equals("license")){
                        uploadImageToAWS(hashMap.get(PICK_LICENSE_IMG_REQ),DRIVER_LICENSE_IMG);
                    }else if(update.equals("photo")) {
                        uploadImageToAWS(hashMap.get(PICK_PHOTO_IMG_REQ), USER_PHOTO_IMG);
                    }
                    else{
                        uploadImageToAWS(hashMap.get(PICK_PAN_IMG_REQ), PAN_IMG);
                    }

            }
        }

       /* if(driverNameStr.length()>=5&&licenseCopy.length()>0)
        {
            SharedPrefsUtils.setBooleanPreference(UserProfileActivity.this, KEY_IS_PROFILE_UPDATED, true);
            SharedPrefsUtils.setStringPreference(UserProfileActivity.this, KEY_USER_NAME, driverNameStr);
            Intent intent = new Intent(UserProfileActivity.this,HomeActivity.class);
            intent.putExtra(KEY_USER_NAME,driverNameStr);
            startActivity(intent);
            finish();
        }else if(driverNameStr.length()<5){
            Toast.makeText(UserProfileActivity.this,"Name should be more than 5 characters",Toast.LENGTH_SHORT).show();
        }else if(licenseCopy.length()==0)
        {
            Toast.makeText(UserProfileActivity.this,"Choose your license copy to submit",Toast.LENGTH_SHORT).show();
        }*/
//https://stackoverflow.com/questions/19834842/android-gallery-on-android-4-4-kitkat-returns-different-uri-for-intent-action
    }
    private void initAWS() {
        try {
            awsUtil = new AWSUtil();
            transferUtility = awsUtil.getTransferUtility(UserProfileActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Use TransferUtility to get all upload transfers.
    }
    private void uploadImageToAWS(String imgFilePath,String type) {
        if(imgFilePath==null) {
            Toast.makeText(this, "Could not find the filepath of the selected file",
                    Toast.LENGTH_LONG).show();
            if(loadingIndicatorView!=null)
                loadingIndicatorView.smoothToHide();
            return;
        }
        File file = new File(imgFilePath);
        String ext = ApplicationUtil.getExtension(imgFilePath);
        String fileName = "";
        if(type.equals(DRIVER_LICENSE_IMG))
        {
            fileName = DRIVER_LICENSE_IMG+ApplicationUtil.getTimeStamp("")+ext;
            imgFIleNameMap.put(DRIVER_LICENSE_IMG,fileName);
        }else if(type.equals(PAN_IMG))
        {
            fileName = PAN_IMG+ApplicationUtil.getTimeStamp("")+ext;
            imgFIleNameMap.put(PAN_IMG,fileName);
        }else if(type.equals(USER_PHOTO_IMG))
        {
            fileName = USER_PHOTO_IMG+ApplicationUtil.getTimeStamp("")+ext;
            imgFIleNameMap.put(USER_PHOTO_IMG,fileName);
        }
        try {
            TransferObserver observer = transferUtility.upload(AWSConstants.BUCKET_NAME, fileName,
                    file, CannedAccessControlList.PublicRead);
            observer.setTransferListener(new UploadListener(type));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void skipToHome(View view) {
        Intent intent = new Intent(UserProfileActivity.this,HomeActivity.class);
        startActivity(intent);
    }

    /*
      * A TransferListener class that can listen to a upload task and be notified
      * when the status changes.
      */
    private class UploadListener implements TransferListener {
        String type;
        private UploadListener(String type) {
            this.type = type;
        }

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.d(ApplicationUtil.APPTAG, " upload error " + e);

        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(ApplicationUtil.APPTAG, " upload progress");
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d(ApplicationUtil.APPTAG, " upload state"+ newState);
            if (newState.toString().equals(AWS_UPLOAD_STATE_COMPLETE)) {
                if(type.equals(DRIVER_LICENSE_IMG)){
                    if(hashMap.get(PICK_PAN_IMG_REQ)!=null) {
                        uploadImageToAWS(hashMap.get(PICK_PAN_IMG_REQ), PAN_IMG);
                    }else if(hashMap.get(PICK_PHOTO_IMG_REQ)!=null) {
                        uploadImageToAWS(hashMap.get(PICK_PHOTO_IMG_REQ), USER_PHOTO_IMG);
                    }
                    else {
                        uploadFilesToServer();
                    }
                }else if(type.equals(PAN_IMG))
                {
                    if(hashMap.get(PICK_PHOTO_IMG_REQ)!=null) {
                        uploadImageToAWS(hashMap.get(PICK_PHOTO_IMG_REQ), USER_PHOTO_IMG);
                    }else{
                        uploadFilesToServer();
                    }
                }else if(type.equals(USER_PHOTO_IMG))
                {
                    uploadFilesToServer();
                }
                Log.d(ApplicationUtil.APPTAG, " upload Suceess");
               // Toast.makeText(UserProfileActivity.this, "File uploaded", Toast.LENGTH_SHORT).show();
            }else if(newState.toString().equals(AWS_UPLOAD_STATE_FAILED)){
                    Toast.makeText(UserProfileActivity.this, "Error Uploading profile details", Toast.LENGTH_SHORT).show();
                    loadingIndicatorView.smoothToHide();

            }
        }
    }
    private void uploadFilesToServer() {
        try {
            loadingIndicatorView.smoothToShow();
           // Log.d(ApplicationUtil.APPTAG,"lic "+imgFIleNameMap.get(DRIVER_LICENSE_IMG)+" pan "+imgFIleNameMap.get(PAN_IMG)+ " photo "+imgFIleNameMap.get(USER_PHOTO_IMG));
            String driverLicense = imgFIleNameMap.get(DRIVER_LICENSE_IMG)!=null?imgFIleNameMap.get(DRIVER_LICENSE_IMG):EMPTY_STRING;
            String panCopy = imgFIleNameMap.get(PAN_IMG)!=null?imgFIleNameMap.get(PAN_IMG):EMPTY_STRING;
            String driverPhoto = imgFIleNameMap.get(USER_PHOTO_IMG)!=null?imgFIleNameMap.get(USER_PHOTO_IMG):EMPTY_STRING;
            int userId = SharedPrefsUtils.getIntegerPreference(UserProfileActivity.this,DRIVER_ID,0);
            String driverName = SharedPrefsUtils.getStringPreference(UserProfileActivity.this,DRIVER_NAME);
           // SharedPrefsUtils.setStringPreference(UserProfileActivity.this,KEY_LICENSE_NO,txtLicenseNo.getText().toString());
            Log.d(ApplicationUtil.APPTAG,"lic "+driverLicense+" pan "+panCopy+ " photo "+driverPhoto);
            Call<ProfileUploadResponse> call = apiInterface.uploadFile(String.valueOf(userId), driverName, driverLicense,panCopy,driverPhoto);
            call.enqueue(new Callback<ProfileUploadResponse>() {
                @Override
                public void onResponse(Call<ProfileUploadResponse> call, Response<ProfileUploadResponse> response) {
                    loadingIndicatorView.smoothToHide();
                    ProfileUploadResponse serverResponse = response.body();
                    if (serverResponse != null) {
                        Log.d("API Res ", serverResponse.getError() + "");
                        if (serverResponse.getError()) {
                            Toast.makeText(UserProfileActivity.this,serverResponse.getMessage(),Toast.LENGTH_SHORT).show();
                            Log.d("API Res ", serverResponse.getMessage() + "");
                        } else {
                                if(serverResponse.getMessage()!=null)
                                    Toast.makeText(UserProfileActivity.this,serverResponse.getMessage(),Toast.LENGTH_SHORT).show();
                            SharedPrefsUtils.setStringPreference(UserProfileActivity.this,KEY_DRI_IMG_URL,serverResponse.getUploadResponse().getDriveDetails().getDriverPhoto());
                            SharedPrefsUtils.setBooleanPreference(UserProfileActivity.this,KEY_IS_PROFILE_UPDATED,true);

                            Intent intent = new Intent(UserProfileActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            Log.d("API Res ", serverResponse.getUploadResponse().getDriveDetails().getDriverLicenseStatus());
                        }
                    } else {
                        try {
                            assert serverResponse != null;
                            Log.v("Response", serverResponse.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //  progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<ProfileUploadResponse> call, Throwable t) {
                    loadingIndicatorView.smoothToHide();
                    Toast.makeText(UserProfileActivity.this,"Network Error!Try Again.",Toast.LENGTH_SHORT).show();
                    Log.d("API Res ", call.toString() + " " + t.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void pickImageFromGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
// Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("LDHOME "," onActivityResult "+requestCode +" "+resultCode);
        if(requestCode==PICK_LICENSE_IMG_REQ||requestCode==PICK_PHOTO_IMG_REQ||requestCode==PICK_PAN_IMG_REQ) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                String url = uri.toString();
                String path = null;
                try {
                   path = new FilePathUtil().getPath(uri,UserProfileActivity.this);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
               /* try {
                    actualImage = FileUtil.from(this, data.getData());
                    compressedImage = new Compressor(this)
                            .setMaxWidth(640)
                            .setMaxHeight(480)
                            .setQuality(75)
                            .setCompressFormat(Bitmap.CompressFormat.WEBP)
                            .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES).getAbsolutePath())
                            .compressToFile(actualImage);
                    Log.d(TAG, getReadableFileSize(compressedImage.length()) + "  size");
                } catch (IOException e) {
                    Toast.makeText(UserProfileActivity.this, "Failed tp read Image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }*/

                try {
                    /*Bitmap bmp = BitmapFactory.decodeFile(compressedImage.getAbsolutePath());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    String encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);*/
               /* File dir = new File(getExternalFilesDir(null), APP_TEMP_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }*/
                    url = FileUtil.getFileName(UserProfileActivity.this, uri);

                    switch (requestCode) {
                        case PICK_PAN_IMG_REQ:
                      /*  File fileUserPan;
                        fileUserPan=writeImageByteToFile(UserProfileActivity.this,byteArray,PAN_IMG);*/

                            hashMap.put(PICK_PAN_IMG_REQ, path);
                            setImageUrl(PICK_PAN_IMG_REQ, url);
                            break;
                        case PICK_LICENSE_IMG_REQ:
                       /* File fileUserLicense;
                        fileUserLicense=writeImageByteToFile(UserProfileActivity.this,byteArray,DRIVER_LICENSE_IMG);*/
                            hashMap.put(PICK_LICENSE_IMG_REQ, path);
                            setImageUrl(PICK_LICENSE_IMG_REQ, url);
                            break;
                        case PICK_PHOTO_IMG_REQ:
                        /*File fileUserPhoto;
                        fileUserPhoto=writeImageByteToFile(UserProfileActivity.this,byteArray,USER_PHOTO_IMG);*/
                            hashMap.put(PICK_PHOTO_IMG_REQ, path);
                            setImageUrl(PICK_PHOTO_IMG_REQ, url);
                            break;
                        default:
                            break;

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // String url =getImageUrl(uri);
            /*if(url == null){
                Toast.makeText(UserProfileActivity.this,"Image not found. Try Again",Toast.LENGTH_SHORT).show();
                return;
            }*/

            }
        }
    }

    public File writeImageByteToFile(Context context, byte[] imgByte, String fileName) {
        String fullPath = APP_TEMP_PATH;
        File dir = new File(context.getExternalFilesDir(null), fullPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File fileUserPhoto = new File(dir.getAbsolutePath(), fileName);

        /*try {
            FileOutputStream fos=new FileOutputStream(fileUserPhoto.getPath());
            fos.write(imgByte);
            fos.close();
        }
        catch (java.io.IOException e) {
            Log.e(TAG, "Exception in writeImageByteToFile:"+e.getLocalizedMessage());
            return null;
        }*/

        try {
            fileUserPhoto.createNewFile();
            FileOutputStream fos = new FileOutputStream(fileUserPhoto);
            Bitmap bmp = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            Bitmap croppedBitmap = Bitmap.createScaledBitmap(bmp, 250, 360, true);

            boolean isSaved = croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.d(TAG, "saveGalleryImage:" + isSaved);
            fos.close();
        } catch (IOException ex) {
            Log.e(TAG, "IOException:" + ex.getLocalizedMessage());
        }
        return fileUserPhoto;
    }

    private String getImageUrl(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};

        try {
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            String picturePath = cursor.getString(columnIndex); // returns null
            cursor.close();
            return picturePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private Bitmap decodeFile(File f) {
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            //The new size we want to scale to
            final int REQUIRED_SIZE = 95;

            //Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }
    private void setImageUrl(int requestCode, String url) {
        String filename = url.substring(url.lastIndexOf("/") + 1);
        switch (requestCode) {
            case PICK_PAN_IMG_REQ:
                txtPan.setText(filename);
                break;
            case PICK_LICENSE_IMG_REQ:
                txtLicemnse.setText(filename);
                break;
            case PICK_PHOTO_IMG_REQ:
                txtPhoto.setText(filename);
                break;
            default:
                break;

        }
    }
    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
