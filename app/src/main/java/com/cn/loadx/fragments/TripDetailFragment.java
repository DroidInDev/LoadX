package com.cn.loadx.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cn.loadx.R;
import com.cn.loadx.activity.HomeActivity;
import com.cn.loadx.interfaces.TripListener;
import com.cn.loadx.network.pojo.LoadDetails;
import com.cn.loadx.util.SharedPrefsUtils;

import static com.cn.loadx.util.AppConstants.KEY_TRIP_DETAIL;
import static com.cn.loadx.util.AppConstants.KEY_TRIP_ID;
import static com.cn.loadx.util.AppConstants.KEY_TRIP_STATUS;
import static com.cn.loadx.util.AppConstants.TR_SCHEDULED;

/**
 * Created by Admin on 09-02-2018.
 */

public class TripDetailFragment extends Fragment implements View.OnClickListener {
    private static final String SCHEDULED = "Scheduled";
    private static final String ONGOING = "On Going";
    String tripData;
    LoadDetails loadDetails;
    private TextView txtSourceTittle;
    private TextView txtSourceAddress;
    private TextView txtDesTittle;
    private TextView txtDesAddress;
    private ImageView imgCallSupervisor;
    private RelativeLayout layoutTripHeader;
    private TextView txtWeight;
    private TextView txtMaterial;
    RelativeLayout layoutExpansion;
    private FrameLayout frame_trip_container;
    TripListener tripListener;
    boolean isExpanded = true;
    private ImageView headerIndicator;

    Animator indicatorAnimator;
    private int headerRotationExpanded = 270;
    private int headerRotationCollapsed = 90;
    String tripId;
    private TextView txtTripLayoutTittle;
    private RelativeLayout layoutTripSource;
    private RelativeLayout layoutTripDestination;
    private TextView txtTripStatus;

    public static TripDetailFragment newInstance(String tripId, LoadDetails loadDetails) {
        TripDetailFragment tripDetailFragment = new TripDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_TRIP_DETAIL, loadDetails);
        args.putString(KEY_TRIP_ID,tripId);
        tripDetailFragment.setArguments(args);
        return tripDetailFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View tripView = inflater.inflate(R.layout.fragment_layout_trip, container, false);
        if (getArguments() != null) {
            loadDetails = getArguments().getParcelable(KEY_TRIP_DETAIL);
            tripId = getArguments().getString(KEY_TRIP_ID);
        }
       /* JSONObject tripObject = null;
        try {
            tripObject = new JSONObject(tripData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        loadDetails = gson.fromJson(tripData,LoadDetails.class) ;*/
       tripListener = (TripListener)getActivity();
       Log.d("TRIP ",loadDetails.getSourceAddress());
        initUIComponents(tripView);
        isExpanded =false;
        return tripView;
    }

    private void initUIComponents(View view) {
       txtTripLayoutTittle =(TextView)view.findViewById(R.id.txtTripDetail);
        txtSourceTittle = (TextView)view.findViewById(R.id.txtSourceName);
        txtSourceAddress = (TextView)view.findViewById(R.id.txtSourceAddress);
        txtDesTittle = (TextView)view.findViewById(R.id.txtDesName);
        txtDesAddress = (TextView)view.findViewById(R.id.txtDesAddress);
        txtWeight = (TextView)view.findViewById(R.id.txtWeight);
        txtMaterial = (TextView)view.findViewById(R.id.txtMaterial);
        imgCallSupervisor = (ImageView)view.findViewById(R.id.imgCallSupervisor);
        imgCallSupervisor.setOnClickListener(this);
        layoutTripHeader = (RelativeLayout)view.findViewById(R.id.layoutTripHeader);
        frame_trip_container =(FrameLayout)view.findViewById(R.id.frame_trip_Container);
        layoutExpansion = (RelativeLayout)view.findViewById(R.id.expansionLayout);
        layoutTripHeader.setOnClickListener(this);
        layoutExpansion.setVisibility(View.GONE);
        layoutTripSource = (RelativeLayout)view.findViewById(R.id.layoutTripSource);
        layoutTripDestination = (RelativeLayout)view.findViewById(R.id.layoutTripDestination);
        layoutTripSource.setOnClickListener(this);
        layoutTripDestination.setOnClickListener(this);
        headerIndicator = (ImageView)view.findViewById(R.id.headerIndicator);
        String tripStatus = SharedPrefsUtils.getStringPreference(getActivity(), KEY_TRIP_STATUS);
        txtTripStatus = view.findViewById(R.id.txtTripStatus);
        setTripStatus(tripStatus);
        if(loadDetails!=null){
            txtTripLayoutTittle.setText("Trip : #"+tripId);
            txtSourceTittle.setText(loadDetails.getSourceAddress());
            txtSourceAddress.setText(loadDetails.getSourceAddress());
            txtDesAddress.setText(loadDetails.getDestinationAddress());
            txtDesTittle.setText(loadDetails.getDestinationAddress());
            txtWeight.setText(loadDetails.getQuantity());
            txtMaterial.setText(loadDetails.getMeterialName().toLowerCase());
        }
        isExpanded = false;
        initialiseView(isExpanded);

        //tripListener.onTripClicked(true);
    }

    public void setTripStatus(String tripStatus) {
        if(tripStatus.equals(TR_SCHEDULED))
        {
            txtTripStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_scheduled, 0, 0, 0);
            txtTripStatus.setText(SCHEDULED);
        }else{
            txtTripStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_started, 0, 0, 0);
            txtTripStatus.setText(ONGOING);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.imgCallSupervisor){
            //ApplicationUtil.getmInstance().callSupervisorDialog(getActivity(),loadDetails.getSupervisorAtSourceName(),loadDetails.getSupervisorAtSourceContact());
            tripListener.callSupervisorDialog();
        }
        else if(view.getId()==R.id.layoutTripHeader)
        {
           tripListener.onTripClicked(isExpanded);
          //  getActivity().getFragmentManager().beginTransaction().remove(this).commit();
            onExpansionModifyView(isExpanded);
            if(isExpanded) {
                scaleView(layoutExpansion, 1f, 0f);
                isExpanded =false;
            }else{
                layoutExpansion.setVisibility(View.VISIBLE);
                scaleView(layoutExpansion, 0f, 1f);
                isExpanded =true;
            }

        }
        else if(view.getId()==R.id.layoutTripSource){
            tripListener.onSourceLocationClicked();
            onExpansionModifyView(isExpanded);
            if(isExpanded) {
                scaleView(layoutExpansion, 1f, 0f);
                isExpanded =false;
            }else{
                layoutExpansion.setVisibility(View.VISIBLE);
                scaleView(layoutExpansion, 0f, 1f);
                isExpanded =true;
            }
        }else if(view.getId()==R.id.layoutTripDestination){
            tripListener.onDestinationLocationClicked();
            onExpansionModifyView(isExpanded);
            if(isExpanded) {
                scaleView(layoutExpansion, 1f, 0f);
                isExpanded =false;
            }else{
                layoutExpansion.setVisibility(View.VISIBLE);
                scaleView(layoutExpansion, 0f, 1f);
                isExpanded =true;
            }
        }
    }
    //can be overriden
    protected void initialiseView(boolean isExpanded) {
        if (headerIndicator != null) {
            headerIndicator.setRotation(isExpanded ? headerRotationCollapsed:headerRotationCollapsed);
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
        if(isExpanded)
        v.setVisibility(View.GONE);
    }

    public void hideDetail() {
        if(isExpanded) {
            onExpansionModifyView(isExpanded);
            isExpanded =false;
            scaleView(layoutExpansion, 1f, 0f);
            layoutExpansion.setVisibility(View.GONE);
        }
    }

    public void showLayout() {
        layoutExpansion.setVisibility(View.VISIBLE);
        scaleView(layoutExpansion, 0f, 1f);
        onExpansionModifyView(isExpanded);
        isExpanded =true;
    }
}
