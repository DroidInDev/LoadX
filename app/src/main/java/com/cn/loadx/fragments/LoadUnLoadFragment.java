package com.cn.loadx.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.internal.Constants;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.cn.loadx.R;
import com.cn.loadx.customUserInterface.LoadingIndicatorView;
import com.cn.loadx.interfaces.ImageCompressListener;
import com.cn.loadx.interfaces.LoadListener;
import com.cn.loadx.network.api.APIClient;
import com.cn.loadx.network.api.APIInterface;
import com.cn.loadx.network.pojo.TripStatus;
import com.cn.loadx.network.util.AWSConstants;
import com.cn.loadx.network.util.AWSUtil;
import com.cn.loadx.util.AppConstants;
import com.cn.loadx.util.ApplicationUtil;
import com.cn.loadx.util.FilePathUtil;
import com.cn.loadx.util.ImageCompressTask;
import com.cn.loadx.util.SharedPrefsUtils;

import java.io.File;
import java.net.URISyntaxException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.cn.loadx.network.util.AWSConstants.AWS_UPLOAD_STATE_COMPLETE;
import static com.cn.loadx.network.util.AWSConstants.AWS_UPLOAD_STATE_FAILED;
import static com.cn.loadx.util.AppConstants.DRIVER_ID;
import static com.cn.loadx.util.AppConstants.EMPTY_STRING;
import static com.cn.loadx.util.AppConstants.KEY_LOAD_DETAIL_UPDATED;
import static com.cn.loadx.util.AppConstants.KEY_LOAD_LR;
import static com.cn.loadx.util.AppConstants.KEY_LOAD_LR_URL;
import static com.cn.loadx.util.AppConstants.KEY_LOAD_POD;
import static com.cn.loadx.util.AppConstants.KEY_LOAD_POD_URL;
import static com.cn.loadx.util.AppConstants.KEY_LOAD_WEIGHT;
import static com.cn.loadx.util.AppConstants.KEY_LOAD_WEIGHT_URL;
import static com.cn.loadx.util.AppConstants.KEY_TRIP_ID;
import static com.cn.loadx.util.AppConstants.KEY_TRIP_STATUS;
import static com.cn.loadx.util.AppConstants.KEY_UN_LOAD_DETAIL_UPDATED;
import static com.cn.loadx.util.AppConstants.KEY_UN_LOAD_WEIGHT;
import static com.cn.loadx.util.AppConstants.KEY_UN_LOAD_WEIGHT_URL;
import static com.cn.loadx.util.AppConstants.LR;
import static com.cn.loadx.util.AppConstants.LR_COPY;
import static com.cn.loadx.util.AppConstants.LWS;
import static com.cn.loadx.util.AppConstants.POD;
import static com.cn.loadx.util.AppConstants.POD_COPY;
import static com.cn.loadx.util.AppConstants.TR_COMPLETE;
import static com.cn.loadx.util.AppConstants.TR_ONGOING;
import static com.cn.loadx.util.AppConstants.TR_SCHEDULED;
import static com.cn.loadx.util.AppConstants.ULWS;

/**
 * Created by Admin on 14-02-2018.
 */

public class LoadUnLoadFragment extends Fragment implements View.OnClickListener {
    private static final String WS = "ws";
    private static final String LOADING = "load";
    boolean isExpanded = true;
    private ImageView headerIndicator;
    /* EditText txtloadWeight;
     EditText txtLoadingCharge;*/
    TextView txtPODCopy;
    Animator indicatorAnimator;
    private int headerRotationExpanded = 270;
    private int headerRotationCollapsed = 90;
    private RelativeLayout loadExpansionLayout;
    RelativeLayout layoutLoadUnLoadHeader;
    TextView txtLoadDetail;
    TextView txtBtnSubmit;
    String loadType;
    LoadListener loadListener;
    String podImgString;
    String wSImgString;
    File wsImgFileCompressed;
    File podImgFIleCompressed;
    APIInterface apiInterface;
    private ImageView imgBtnPODCopy;
    String tripId;
    private LoadingIndicatorView loadingIndicatorView;
    private TextView txtWS;
    private ImageView imgBtnWS;
    private File podImgFile;
    private File wsImgFile;
    //AWS declaration
// The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility transferUtility;
    // Reference to the utility class
    private AWSUtil awsUtil;
    private Uri imgUri;

    public static LoadUnLoadFragment newInstance(String loadType) {
        LoadUnLoadFragment loadUnLoadFragment = new LoadUnLoadFragment();
        Bundle args = new Bundle();
        args.putString("LOAD_DETAIL", loadType);
        loadUnLoadFragment.setArguments(args);
        return loadUnLoadFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View loadUnloadView = inflater.inflate(R.layout.fragment_load_unload_detail, container, false);
        if (getArguments() != null)
            loadType = getArguments().getString("LOAD_DETAIL");
        loadListener = (LoadListener) getActivity();
        initUiComponents(loadUnloadView);
        initAWS();
        apiInterface = APIClient.getClient().create(APIInterface.class);
        return loadUnloadView;
    }

    private void initAWS() {
        try {
            awsUtil = new AWSUtil();
            transferUtility = awsUtil.getTransferUtility(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Use TransferUtility to get all upload transfers.
    }

    private void initUiComponents(View loadUnloadView) {
        layoutLoadUnLoadHeader = loadUnloadView.findViewById(R.id.layoutLoadUnloadHeader);
        txtLoadDetail = loadUnloadView.findViewById(R.id.txtLoadDetail);

        headerIndicator = loadUnloadView.findViewById(R.id.loadheaderIndicator);
       /* txtLoadingCharge  = (EditText)loadUnloadView.findViewById(R.id.txtLoadingCharge);
        txtloadWeight = (EditText)loadUnloadView.findViewById(R.id.txtLoadingWeight);*/
        txtPODCopy = loadUnloadView.findViewById(R.id.txtPODCopy);
        txtWS = loadUnloadView.findViewById(R.id.txtWS);
        loadExpansionLayout = loadUnloadView.findViewById(R.id.loadExpansionLayout);
        txtBtnSubmit = loadUnloadView.findViewById(R.id.btnLoadSubmit);
        layoutLoadUnLoadHeader.setOnClickListener(this);
        txtBtnSubmit.setOnClickListener(this);
        loadExpansionLayout.setVisibility(View.GONE);
        imgBtnPODCopy = loadUnloadView.findViewById(R.id.imgAddPodCopy);
        imgBtnPODCopy.setOnClickListener(this);
        imgBtnWS = loadUnloadView.findViewById(R.id.imgAddWS);
        imgBtnWS.setOnClickListener(this);
        loadingIndicatorView = loadUnloadView.findViewById(R.id.animatedLoader);
        loadingIndicatorView.smoothToHide();

        if (loadType.equals(TR_SCHEDULED)) {
            txtLoadDetail.setText(getString(R.string.load_details));
            txtPODCopy.setHint(LR_COPY);
            boolean isLoadUpdated = SharedPrefsUtils.getBooleanPreference(getActivity(), KEY_LOAD_DETAIL_UPDATED, false);
            if (isLoadUpdated) {
                txtBtnSubmit.setVisibility(View.INVISIBLE);
                imgBtnPODCopy.setEnabled(false);
                /*txtLoadingCharge.setFocusable(false);
                txtloadWeight.setFocusable(false);
                txtloadWeight.setText(SharedPrefsUtils.getStringPreference(getActivity(),KEY_LOAD_WEIGHT));
                txtLoadingCharge.setText(SharedPrefsUtils.getStringPreference(getActivity(),KEY_LOAD_CHARGE));*/
                txtPODCopy.setText(SharedPrefsUtils.getStringPreference(getActivity(), KEY_LOAD_LR_URL));
            } else {
                txtBtnSubmit.setVisibility(View.VISIBLE);
                imgBtnPODCopy.setEnabled(true);
               /* txtLoadingCharge.setFocusable(true);
                txtloadWeight.setFocusable(true);*/
            }
        } else if (loadType.equals(TR_ONGOING) || loadType.equals(TR_COMPLETE)) {
            showUnloadDetail();
        } else if (loadType.equals(LWS)) {
            Log.d("LDHOME in", LWS);
            txtLoadDetail.setText(getString(R.string.load_details));
            txtPODCopy.setHint(LR_COPY);
            imgBtnWS.setVisibility(View.VISIBLE);
            imgBtnPODCopy.setVisibility(View.INVISIBLE);
            txtPODCopy.setText(SharedPrefsUtils.getStringPreference(getActivity(), AppConstants.KEY_LOAD_LR_URL));
            SharedPrefsUtils.setBooleanPreference(getActivity(), KEY_LOAD_DETAIL_UPDATED, false);
        } else if (loadType.equals(ULWS)) {
            Log.d("LDHOME in", ULWS);
            txtLoadDetail.setText(getString(R.string.un_load_details));
            txtPODCopy.setHint(POD_COPY);
            imgBtnPODCopy.setVisibility(View.INVISIBLE);
            imgBtnWS.setVisibility(View.VISIBLE);
            txtPODCopy.setText(SharedPrefsUtils.getStringPreference(getActivity(), KEY_LOAD_POD_URL));
            SharedPrefsUtils.setBooleanPreference(getActivity(), KEY_UN_LOAD_DETAIL_UPDATED, false);
        } else if (loadType.equals(LR)) {
            Log.d("LDHOME in", LR);
            txtLoadDetail.setText(getString(R.string.load_details));
            txtPODCopy.setHint(LR_COPY);
            imgBtnPODCopy.setVisibility(View.VISIBLE);
            imgBtnWS.setVisibility(View.INVISIBLE);
            txtPODCopy.setText(SharedPrefsUtils.getStringPreference(getActivity(), KEY_LOAD_LR_URL));
            SharedPrefsUtils.setBooleanPreference(getActivity(), KEY_LOAD_DETAIL_UPDATED, false);

        } else if (loadType.equals(POD)) {
            Log.d("LDHOME in", POD);
            txtLoadDetail.setText(R.string.un_load_details);
            txtPODCopy.setHint(POD_COPY);
            imgBtnWS.setVisibility(View.INVISIBLE);
            imgBtnPODCopy.setVisibility(View.VISIBLE);
            txtWS.setText(SharedPrefsUtils.getStringPreference(getActivity(), KEY_UN_LOAD_WEIGHT_URL));
            SharedPrefsUtils.setBooleanPreference(getActivity(), KEY_UN_LOAD_DETAIL_UPDATED, false);
        }
        isExpanded = false;
        initialiseView(isExpanded);


    }

    private void showUnloadDetail() {
        txtLoadDetail.setText(R.string.un_load_details);
        txtPODCopy.setHint(POD_COPY);
        boolean isLoadUpdated = SharedPrefsUtils.getBooleanPreference(getActivity(), KEY_UN_LOAD_DETAIL_UPDATED, false);
        if (isLoadUpdated) {
            imgBtnPODCopy.setVisibility(View.INVISIBLE);
            txtBtnSubmit.setVisibility(View.INVISIBLE);
            imgBtnPODCopy.setEnabled(false);
            imgBtnWS.setVisibility(View.INVISIBLE);
            txtPODCopy.setText("");
            txtWS.setText("");
            txtPODCopy.setText(SharedPrefsUtils.getStringPreference(getActivity(), KEY_LOAD_POD_URL));
            txtWS.setText(SharedPrefsUtils.getStringPreference(getActivity(), KEY_UN_LOAD_WEIGHT_URL));
            /*txtLoadingCharge.setFocusable(false);
            txtloadWeight.setFocusable(false);
            txtloadWeight.setText(SharedPrefsUtils.getStringPreference(getActivity(),KEY_LOAD_WEIGHT));
            txtLoadingCharge.setText(SharedPrefsUtils.getStringPreference(getActivity(),KEY_LOAD_CHARGE));
            */
        } else {
            txtBtnSubmit.setVisibility(View.VISIBLE);
            txtBtnSubmit.setEnabled(true);
            imgBtnWS.setVisibility(View.VISIBLE);
            imgBtnPODCopy.setVisibility(View.VISIBLE);
            imgBtnPODCopy.setEnabled(true);
            imgBtnWS.setEnabled(true);
            txtPODCopy.setText("");
            txtWS.setText("");
            wSImgString = "";
            podImgString = "";
            podImgFile = null;
            wsImgFile = null;
            wsImgFileCompressed =null;
            podImgFIleCompressed = null;
            /*txtLoadingCharge.setFocusable(true);
            txtloadWeight.setFocusable(true);*/
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.layoutLoadUnloadHeader) {
            loadListener.onLoadCLicked(isExpanded);
            onExpansionModifyView(isExpanded);
            if (isExpanded) {
                scaleView(loadExpansionLayout, 1f, 0f);
                isExpanded = false;
            } else {
                loadExpansionLayout.setVisibility(View.VISIBLE);
                scaleView(loadExpansionLayout, 0f, 1f);
                isExpanded = true;
            }
        } else if (view.getId() == R.id.btnLoadSubmit) {
            boolean isLoadUpdated = SharedPrefsUtils.getBooleanPreference(getActivity(), KEY_LOAD_DETAIL_UPDATED, false);
            if (txtWS.getText().toString().length() == 0) {
                Toast.makeText(getActivity(), "Weight Slip Cannot be Empty ", Toast.LENGTH_SHORT).show();
            }/*else if(txtloadWeight.getText().toString().length()==0){
                Toast.makeText(getActivity(),"Loading Weight Cannot be Empty ",Toast.LENGTH_SHORT).show();
            }*/ else if (txtPODCopy.getText().toString().length() == 0) {
                if (!isLoadUpdated)
                    Toast.makeText(getActivity(), "LR copy cannot be Empty ", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "POD copy cannot be Empty ", Toast.LENGTH_SHORT).show();
            } else {
                loadingIndicatorView.smoothToShow();
                //if(loadType.equals(TR_SCHEDULED)||loadType.equals(TR_ONGOING))
                if (!isLoadUpdated) {
                   compressImage(wsImgFile, LWS);
                } else {
                   // uploadLoadToAWS(wsImgFile,ULWS);
                   compressImage(wsImgFile, ULWS);
                }
            }
        } else if (view.getId() == R.id.imgAddPodCopy) {
            loadListener.onPODSelect();
        } else if (view.getId() == R.id.imgAddWS) {
            loadListener.onWSSelect();
        }
    }

    private void compressImage(File imgFile, String category) {
        ImageCompressTask imageCompressToStringTask = new ImageCompressTask(category, new ImageCompressListener() {

            @Override
            public void onImageCompressed(String imageCategory, File compressedImg) {
                if (compressedImg != null) {
                    if (imageCategory.equals(LWS)) {
                        wsImgFileCompressed = compressedImg;
                        compressImage(podImgFile, LR);
                    }
                    if (imageCategory.equals(LR)) {
                        podImgFIleCompressed = compressedImg;
                        uploadLoadToAWS(wsImgFileCompressed, LWS);
                        //callUpdateLoadDetail();
                    }
                    if (imageCategory.equals(ULWS)) {
                        wsImgFileCompressed = compressedImg;
                        compressImage(podImgFile, POD);
                    }
                    if (imageCategory.equals(POD)) {
                        podImgFIleCompressed = compressedImg;
                        uploadLoadToAWS(wsImgFileCompressed, ULWS);
                        //callUpdateUnLoadDetail();
                    }
                } else {
                    loadingIndicatorView.smoothToHide();
                    Toast.makeText(getActivity(), "Upload Failed! Try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
        imageCompressToStringTask.execute(imgFile);
    }


    private void callUpdateLoadDetail() {
        if (wSImgString == null || podImgString == null) {
            loadingIndicatorView.smoothToHide();
            Toast.makeText(getActivity(), "Image processing Error! Try Again", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(ApplicationUtil.APPTAG,"ws "+wSImgString+"LR "+podImgString);
        Call<TripStatus> call = apiInterface.getLoadingSTatus(tripId, wSImgString, podImgString);
        call.enqueue(new Callback<TripStatus>() {
            @Override
            public void onResponse(Call<TripStatus> call, Response<TripStatus> response) {
                TripStatus tripStatus = response.body();
                if (tripStatus != null) {
                    if (tripStatus.getError()) {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), "Error Uploading Load detail.", Toast.LENGTH_SHORT).show();
                        loadingIndicatorView.smoothToHide();
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), "Upload Success", Toast.LENGTH_SHORT).show();
                        txtBtnSubmit.setVisibility(View.INVISIBLE);
                        imgBtnPODCopy.setVisibility(View.INVISIBLE);
                        imgBtnPODCopy.setEnabled(false);
                        imgBtnWS.setVisibility(View.INVISIBLE);
                        imgBtnPODCopy.setEnabled(false);
                        SharedPrefsUtils.setStringPreference(getActivity(), KEY_LOAD_WEIGHT_URL, txtWS.getText().toString());
                        SharedPrefsUtils.setStringPreference(getActivity(), KEY_LOAD_LR_URL, txtPODCopy.getText().toString());
                        SharedPrefsUtils.setStringPreference(getActivity(), KEY_LOAD_LR, podImgString);
                        SharedPrefsUtils.setStringPreference(getActivity(), KEY_LOAD_WEIGHT, wSImgString);
                      /*  txtLoadingCharge.setFocusable(false);
                        txtloadWeight.setFocusable(false);
                        SharedPrefsUtils.setStringPreference(getActivity(),KEY_LOAD_CHARGE,loadCharge);
                        SharedPrefsUtils.setStringPreference(getActivity(),KEY_LOAD_WEIGHT,loadWeight);
                        SharedPrefsUtils.setStringPreference(getActivity(),KEY_LOAD_POD,txtPODCopy.getText().toString());*/
                        callTripStart();
                    }
                }
            }

            @Override
            public void onFailure(Call<TripStatus> call, Throwable t) {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), "Error Uploading Load detail", Toast.LENGTH_SHORT).show();
                loadingIndicatorView.smoothToHide();
            }
        });
    }


    private void callTripStart() {
        if (tripId != null) {
            Call<TripStatus> call = apiInterface.getTripStatusStart(tripId);
            call.enqueue(new Callback<TripStatus>() {

                @Override
                public void onResponse(Call<TripStatus> call, Response<TripStatus> response) {
                    loadingIndicatorView.smoothToHide();
                    TripStatus tripStatus = response.body();
                    if (tripStatus != null) {
                        if (tripStatus.getError()) {
                            SharedPrefsUtils.setBooleanPreference(getActivity(), KEY_LOAD_DETAIL_UPDATED, false);
                            txtBtnSubmit.setVisibility(View.VISIBLE);
                            imgBtnPODCopy.setVisibility(View.VISIBLE);
                            imgBtnPODCopy.setEnabled(true);
                            imgBtnWS.setVisibility(View.INVISIBLE);
                           /* txtLoadingCharge.setFocusable(true);
                            txtloadWeight.setFocusable(true);*/
                            if (getActivity() != null)
                                Toast.makeText(getActivity(), "Error Uploading Load detail Try Again.", Toast.LENGTH_SHORT).show();
                        } else {
                            SharedPrefsUtils.setStringPreference(getActivity(), KEY_TRIP_STATUS, TR_ONGOING);
                            SharedPrefsUtils.setBooleanPreference(getActivity(), KEY_LOAD_DETAIL_UPDATED, true);
                            showUnloadDetail();
                            loadListener.onLoadDetailUpdated();
                        }
                    }
                }

                @Override
                public void onFailure(Call<TripStatus> call, Throwable t) {
                    loadingIndicatorView.smoothToHide();
                }
            });
        }
    }

    private void callUpdateUnLoadDetail() {
        if (wSImgString == null || podImgString == null) {
            loadingIndicatorView.smoothToHide();
            Toast.makeText(getActivity(), "Image processing Error! Try Again", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(ApplicationUtil.APPTAG,"ws "+wSImgString+"LR "+podImgString);
        Call<TripStatus> call = apiInterface.getUnLloadingSTatus(tripId, wSImgString, podImgString);
        call.enqueue(new Callback<TripStatus>() {
            @Override
            public void onResponse(Call<TripStatus> call, Response<TripStatus> response) {
                // loadingIndicatorView.smoothToHide();
                TripStatus tripStatus = response.body();
                if (tripStatus != null) {
                    if (tripStatus.getError()) {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), "Error Uploading UnLoad detail onRes", Toast.LENGTH_SHORT).show();
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), "Upload Success", Toast.LENGTH_SHORT).show();
                        txtBtnSubmit.setVisibility(View.INVISIBLE);
                        imgBtnPODCopy.setEnabled(false);
                       /* txtLoadingCharge.setFocusable(false);
                        txtloadWeight.setFocusable(false);
                        SharedPrefsUtils.setStringPreference(getActivity(),KEY_LOAD_CHARGE,loadCharge);
                        */
                        SharedPrefsUtils.setStringPreference(getActivity(), KEY_UN_LOAD_WEIGHT_URL, txtWS.getText().toString());
                        SharedPrefsUtils.setStringPreference(getActivity(), KEY_LOAD_POD_URL, txtPODCopy.getText().toString());
                        SharedPrefsUtils.setStringPreference(getActivity(), KEY_LOAD_POD, podImgString);
                        SharedPrefsUtils.setStringPreference(getActivity(), KEY_UN_LOAD_WEIGHT, wSImgString);
                        callTripComplete();
                    }
                }
            }

            @Override
            public void onFailure(Call<TripStatus> call, Throwable t) {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), "Error Uploading Load detail", Toast.LENGTH_SHORT).show();
                loadingIndicatorView.smoothToHide();
            }
        });
    }

    private void callTripComplete() {
        if (tripId != null) {
            Call<TripStatus> call = apiInterface.getTripStatusComplete(tripId);
            call.enqueue(new Callback<TripStatus>() {

                @Override
                public void onResponse(Call<TripStatus> call, Response<TripStatus> response) {
                    loadingIndicatorView.smoothToHide();
                    TripStatus tripStatus = response.body();
                    if (tripStatus != null) {
                        if (tripStatus.getError()) {
                            Log.d("LDHOME ", "callTripComplete " + "error");
                            SharedPrefsUtils.setBooleanPreference(getActivity(), KEY_UN_LOAD_DETAIL_UPDATED, false);
                            txtBtnSubmit.setVisibility(View.VISIBLE);
                            imgBtnPODCopy.setVisibility(View.VISIBLE);
                            imgBtnPODCopy.setEnabled(true);
                            imgBtnWS.setVisibility(View.VISIBLE);
                           /* txtLoadingCharge.setFocusable(true);
                            txtloadWeight.setFocusable(true);*/
                            if (getActivity() != null)
                                Toast.makeText(getActivity(), "Error Uploading Load detail Try Again.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("LDHOME ", "callTripComplete " + "success");
                            clearTripRelatedPreference();
                            loadListener.onTripComplete();
                            /*SharedPrefsUtils.setStringPreference(getActivity(), KEY_TRIP_STATUS,TR_COMPLETE);
                            SharedPrefsUtils.setBooleanPreference(getActivity(),KEY_UN_LOAD_DETAIL_UPDATED,true);
                            txtBtnSubmit.setVisibility(View.INVISIBLE);
                            imgBtnPODCopy.setVisibility(View.INVISIBLE);
                            imgBtnWS.setVisibility(View.INVISIBLE);
                            imgBtnPODCopy.setEnabled(false);*/
                          /*  txtLoadingCharge.setFocusable(false);
                            txtloadWeight.setFocusable(false);*/
                            // showUnloadDetail();
                        }
                    }
                }

                @Override
                public void onFailure(Call<TripStatus> call, Throwable t) {
                    loadingIndicatorView.smoothToHide();
                }
            });
        }
    }

    private void clearTripRelatedPreference() {
        SharedPrefsUtils.setBooleanPreference(getActivity(), KEY_LOAD_DETAIL_UPDATED, false);
        SharedPrefsUtils.setBooleanPreference(getActivity(), KEY_UN_LOAD_DETAIL_UPDATED, false);

        SharedPrefsUtils.setStringPreference(getActivity(), KEY_TRIP_STATUS, TR_COMPLETE);
        SharedPrefsUtils.setStringPreference(getActivity(), KEY_TRIP_ID, EMPTY_STRING);

        SharedPrefsUtils.setStringPreference(getActivity(), KEY_LOAD_WEIGHT_URL, EMPTY_STRING);
        SharedPrefsUtils.setStringPreference(getActivity(), KEY_LOAD_POD_URL, EMPTY_STRING);
        SharedPrefsUtils.setStringPreference(getActivity(), KEY_LOAD_POD, EMPTY_STRING);
        SharedPrefsUtils.setStringPreference(getActivity(), KEY_LOAD_WEIGHT, EMPTY_STRING);

        SharedPrefsUtils.setStringPreference(getActivity(), KEY_UN_LOAD_WEIGHT_URL, EMPTY_STRING);
        SharedPrefsUtils.setStringPreference(getActivity(), KEY_LOAD_LR_URL, EMPTY_STRING);
        SharedPrefsUtils.setStringPreference(getActivity(), KEY_LOAD_LR, EMPTY_STRING);
        SharedPrefsUtils.setStringPreference(getActivity(), KEY_UN_LOAD_WEIGHT, EMPTY_STRING);
    }

    //can be overriden
    protected void initialiseView(boolean isExpanded) {
        if (headerIndicator != null) {
            headerIndicator.setRotation(isExpanded ? headerRotationExpanded : headerRotationCollapsed);
        }
    }

    //can be overriden
    protected void onExpansionModifyView(boolean willExpand) {
        if (headerIndicator != null) {
            if (indicatorAnimator != null) {
                indicatorAnimator.cancel();
            }
            if (!willExpand) {
                indicatorAnimator = ObjectAnimator.ofFloat(headerIndicator, View.ROTATION, headerRotationExpanded);
            } else {
                indicatorAnimator = ObjectAnimator.ofFloat(headerIndicator, View.ROTATION, headerRotationCollapsed);
            }

            indicatorAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation, boolean isReverse) {
                    indicatorAnimator = null;
                }
            });

            if (indicatorAnimator != null) {
                indicatorAnimator.start();
            }
        }
    }

    public void scaleView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(
                1f, 1f, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(500);
        v.startAnimation(anim);
        if (isExpanded)
            v.setVisibility(View.GONE);
    }

    public void hideLayout() {
        if (isExpanded) {
            onExpansionModifyView(isExpanded);
            isExpanded = false;
            scaleView(loadExpansionLayout, 1f, 0f);
            loadExpansionLayout.setVisibility(View.GONE);
        }
    }

    public void setPODCopy(File imgFIle, String url, int strTripId) {
        String filename = url.substring(url.lastIndexOf("/") + 1);
        txtPODCopy.setText(filename);
        podImgFile = imgFIle;
        // podImgString = encodedString;
        tripId = String.valueOf(strTripId);
    }

    public void setWSCopy(File imgFIle, String url, int id) {
        String filename = url.substring(url.lastIndexOf("/") + 1);
        txtWS.setText(filename);
        wsImgFile = imgFIle;
        //wSImgString = encodedString;
        tripId = String.valueOf(id);
    }

    public void updateUI(String update) {
        Log.d("LDHOME in updateUI", update);
        if (update.equals(LWS)) {
            Log.d("LDHOME in", LWS);
            txtLoadDetail.setText(getString(R.string.load_details));
            txtPODCopy.setHint(LR_COPY);
            imgBtnWS.setVisibility(View.VISIBLE);
            imgBtnPODCopy.setVisibility(View.INVISIBLE);
            txtPODCopy.setText(SharedPrefsUtils.getStringPreference(getActivity(), KEY_LOAD_POD_URL));
            SharedPrefsUtils.setBooleanPreference(getActivity(), KEY_LOAD_DETAIL_UPDATED, false);
        } else if (update.equals(ULWS)) {
            Log.d("LDHOME in", ULWS);
            txtLoadDetail.setText(R.string.un_load_details);
            txtPODCopy.setHint(POD_COPY);
            imgBtnPODCopy.setVisibility(View.INVISIBLE);
            imgBtnWS.setVisibility(View.VISIBLE);
            txtPODCopy.setText(SharedPrefsUtils.getStringPreference(getActivity(), KEY_LOAD_LR_URL));
            SharedPrefsUtils.setBooleanPreference(getActivity(), KEY_UN_LOAD_DETAIL_UPDATED, false);
        } else if (update.equals(LR)) {
            Log.d("LDHOME in", LR);
            txtLoadDetail.setText(R.string.un_load_details);
            txtPODCopy.setHint(POD_COPY);
            imgBtnPODCopy.setVisibility(View.VISIBLE);
            imgBtnWS.setVisibility(View.INVISIBLE);
            txtPODCopy.setText(SharedPrefsUtils.getStringPreference(getActivity(), KEY_LOAD_LR_URL));
            SharedPrefsUtils.setBooleanPreference(getActivity(), KEY_UN_LOAD_DETAIL_UPDATED, false);

        } else if (update.equals(POD)) {
            Log.d("LDHOME in", POD);
            txtLoadDetail.setText(getString(R.string.load_details));
            txtPODCopy.setHint(LR_COPY);
            imgBtnWS.setVisibility(View.INVISIBLE);
            imgBtnPODCopy.setVisibility(View.VISIBLE);
            txtWS.setText(SharedPrefsUtils.getStringPreference(getActivity(), KEY_LOAD_WEIGHT_URL));
            SharedPrefsUtils.setBooleanPreference(getActivity(), KEY_LOAD_DETAIL_UPDATED, false);
        }
        //initialiseView(false);
    }

    private Uri getUri(File imgFile) {
        MediaScannerConnection.scanFile(
                getActivity(),
                new String[]{imgFile.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener()
                {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                        imgUri = uri;
                    }
                });
        return imgUri;
    }
 /*   public void uploadLoadToAWS(String imgFile) {
        Log.d(ApplicationUtil.APPTAG," Path ");
        File file = new File(imgFile);

        try {
            TransferObserver observer = transferUtility.upload(AWSConstants.BUCKET_NAME, file.getName(),
                    file, CannedAccessControlList.PublicRead);
            observer.setTransferListener(new UploadListener());
            Log.d(ApplicationUtil.APPTAG, " upload begin ws "+file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(ApplicationUtil.APPTAG, " upload " + e.getMessage());
        }
    }*/
    public void uploadLoadToAWS(File imgFile, String type) {
        Log.d(ApplicationUtil.APPTAG," Path "+imgFile.getPath());

        File file = new File(imgFile.getPath());
        String ext = ApplicationUtil.getExtension(imgFile.getPath());
        String fileName = "";
        try {
            if(type.equals(LWS)){
                fileName = LWS+"_"+ApplicationUtil.getTimeStamp(tripId)+ext;
                wSImgString = fileName;
            }else if(type.equals(LR)){
                fileName = LR+"_"+ApplicationUtil.getTimeStamp(tripId)+ext;
                podImgString = fileName;
            }else if(type.equals(ULWS)){
                fileName = ULWS+"_"+ApplicationUtil.getTimeStamp(tripId)+ext;
                wSImgString = fileName;
            }else if(type.equals(POD)){
                fileName = POD+"_"+ApplicationUtil.getTimeStamp(tripId)+ext;
                podImgString = fileName;
            }
            TransferObserver observer = transferUtility.upload(AWSConstants.BUCKET_NAME, fileName,
                    file, CannedAccessControlList.PublicRead);
            observer.setTransferListener(new UploadListener(type));
            Log.d(ApplicationUtil.APPTAG, " upload begin" +file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(ApplicationUtil.APPTAG, " upload " + e.getMessage());
        }
    }

    /*
    * A TransferListener class that can listen to a upload task and be notified
    * when the status changes.
    */
    private class UploadListener implements TransferListener {
        String type;
        public UploadListener(String type) {
            this.type = type;
        }

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.d(ApplicationUtil.APPTAG, " upload error ");
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
                if(type.equals(LWS)){
                    uploadLoadToAWS(podImgFIleCompressed,LR);
                }else if(type.equals(LR))
                {
                    callUpdateLoadDetail();
                }else if(type.equals(ULWS)){
                    uploadLoadToAWS(podImgFIleCompressed,POD);
                }
                else if(type.equals(POD)){
                    callUpdateUnLoadDetail();
                }
                Log.d(ApplicationUtil.APPTAG, " upload Suceess");
              //  Toast.makeText(getActivity(), "File uploaded", Toast.LENGTH_SHORT).show();
            }else if(newState.toString().equals(AWS_UPLOAD_STATE_FAILED)){
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Error Uploading Load detail", Toast.LENGTH_SHORT).show();
                    loadingIndicatorView.smoothToHide();
                }
            }
        }
    }
   /* //AWS update status Listeners
    @Override
    public void onStateChanged(int id, TransferState state) {
        Log.d(ApplicationUtil.APPTAG, " upload state"+ state);
        if (state.toString().equals(AWS_UPLOAD_STATE_COMPLETE)) {
            loadingIndicatorView.smoothToHide();
            Log.d(ApplicationUtil.APPTAG, " upload Suceess");
            Toast.makeText(getActivity(), "File uploaded", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        Log.d(ApplicationUtil.APPTAG, " upload progress");
    }

    @Override
    public void onError(int id, Exception ex) {
        Log.d(ApplicationUtil.APPTAG, " upload error " + ex.getMessage());
    }*/
}
