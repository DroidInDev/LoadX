package com.cn.loadx.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cn.loadx.R;
import com.cn.loadx.customUserInterface.CircleImageView;
import com.cn.loadx.customUserInterface.LoadingIndicatorView;
import com.cn.loadx.fragments.LoadUnLoadFragment;
import com.cn.loadx.fragments.TripDetailFragment;
import com.cn.loadx.geofence.GeoConstants;
import com.cn.loadx.geofence.GeofenceBroadcastReceiver;
import com.cn.loadx.geofence.GeofenceErrorMessages;
import com.cn.loadx.interfaces.LoadListener;
import com.cn.loadx.interfaces.TripListener;
import com.cn.loadx.network.api.APIClient;
import com.cn.loadx.network.api.APIInterface;
import com.cn.loadx.network.api.MapsAPIInterface;
import com.cn.loadx.network.mapsPojo.Example;
import com.cn.loadx.network.pojo.OnGoingTripDetails;
import com.cn.loadx.notification.FCMConfig;
import com.cn.loadx.service.LoadXLocationUpdatesService;
import com.cn.loadx.util.ApplicationUtil;
import com.cn.loadx.util.ConnectivityReceiver;
import com.cn.loadx.util.FilePathUtil;
import com.cn.loadx.util.FileUtil;
import com.cn.loadx.util.SharedPrefsUtils;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.cn.loadx.geofence.GeoConstants.GEOFENCE_RADIUS_IN_METERS;
import static com.cn.loadx.util.AppConstants.CUSTOMER_CARE_NO;
import static com.cn.loadx.util.AppConstants.DRIVER_ID;
import static com.cn.loadx.util.AppConstants.DRIVER_NAME;
import static com.cn.loadx.util.AppConstants.IMAGE_DIRECTORY;
import static com.cn.loadx.util.AppConstants.KEY_DEST_ADDRESS;
import static com.cn.loadx.util.AppConstants.KEY_DRI_IMG_URL;
import static com.cn.loadx.util.AppConstants.KEY_TRIP_ID;
import static com.cn.loadx.util.AppConstants.KEY_TRIP_STATUS;
import static com.cn.loadx.util.AppConstants.KEY_VERSION_NAME;
import static com.cn.loadx.util.AppConstants.LWS;
import static com.cn.loadx.util.AppConstants.TRUCK_VECHILE_NO;
import static com.cn.loadx.util.AppConstants.TR_COMPLETE;
import static com.cn.loadx.util.AppConstants.TR_ONGOING;
import static com.cn.loadx.util.AppConstants.TR_SCHEDULED;
import static com.cn.loadx.util.AppConstants.ULWS;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, ConnectivityReceiver.ConnectivityReceiverListener, EasyPermissions.PermissionCallbacks, TripListener, LoadListener
        , NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, OnCompleteListener<Void> {
    private static final String TAG = "HomeActivity";
    private static final float DEFAULT_ZOOM = 16;
    private static final String[] PHONE_CALL_PERMISSION = {Manifest.permission.CALL_PHONE};
    private static final String[] PHONE_CALL_AND_SMS_PERMISSION = {Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS};

    private static final int CAMERA = 1;
    private static final int GALLERY = 2;
    Toolbar toolbar;
    private View navHeader;
    private TextView txtName;
    TextView txtAppVName;
    RelativeLayout loadTripLayout;
    String userName;
    private GoogleMap mMap;
    int padding = 250;
    private static final String[] LOCATION_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int RC_LOCATION_PERM = 1001;
    private static final int RC_PHONE_CALL_PERM = 1002;
    private static final int RC_PHONE_CALL_AND_SMS_PERMS = 1003;

    private static final String[] STORAGE_PERMISSION = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int RC_STORAGE_PERM_IMAGE_PICKUP = 1005;
    private static final int RC_STORAGE_PERM_WS = 1006;
    private static final int RC_STORAGE_PERM_POD_BACK = 1007;
    private static final int PICK_POD_IMG_REQ = 3005;
    private static final int PICK_WS_IMG_REQ = 3006;
    private static final int PICK_POD_IMG_BACK_REQ = 3007;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private Boolean mRequestingLocationUpdates;
    SupportMapFragment mapFragment;
    private Location mLastKnownLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    CircleImageView driverImageView;
    LoadingIndicatorView loadingIndicatorView;
    LatLng sourceLatLng;
    LatLng destinationLatLng;
    APIInterface apiInterface;
    OnGoingTripDetails onGoingTripDetails;
    Polyline line;
    private TripDetailFragment tripDetailFragment;
    private LoadUnLoadFragment loadUnloadFragment;
    FrameLayout frameLayoutTrip;
    private FrameLayout frameLayoutLoad;
    private FrameLayout frameLayoutNoTrip;
    private TableRow trowTrip;
    private TableRow trowLoad;
    boolean hasStoragePermission;
    private String update;
    boolean showLoadUnloadDetail;
    private TextView txtLicenseNo;
    private Location sourceLocation;
    private Location destinationLocation;
    private ImageView currentLocImg;
    private ImageView reouteViewImg;
    private ImageView directionViewImg;
    private LatLngBounds bounds;
    boolean isTripLoaded;
    boolean isCurrentLocationSelected;
    private Marker mPositionMarker;
    int height;
    int width;
    private FrameLayout frameLayoutNoInternet;
    boolean isLoactionUpdateConnected;
    private int TYPE_OF_IMG_REQ;
    private int IMG_SELECTED_VIA;
    public void onCurrentLocationClick(View view) {
        if (isTripLoaded) {
            if (isCurrentLocationSelected) {
                // currentLocImg.setBackgroundColor(ContextCompat.getColor(HomeActivity.this,R.color.white));
                currentLocImg.setBackgroundResource(R.drawable.go_route);
                isCurrentLocationSelected = false;
                if (mCurrentLocation != null) {

                    LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    if (mPositionMarker == null && mMap != null) {
                        mPositionMarker = mMap.addMarker(new MarkerOptions()
                                .position(currentLatLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck_in_map)));

                    } else {
                        mPositionMarker.setPosition(currentLatLng);
                    }
                    CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                            currentLatLng, 15);
                    mMap.animateCamera(location);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                }
            } else {
                //currentLocImg.setBackgroundColor(ContextCompat.getColor(HomeActivity.this,R.color.white));
                currentLocImg.setBackgroundResource(R.drawable.go_current);
                isCurrentLocationSelected = true;
                if (mMap != null) {
                    try {
                        LatLng centerLatLng = getCenter(sourceLatLng,destinationLatLng);
                        int zoom = getZoomLevel(centerLatLng,destinationLatLng);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, zoom));
                       // offset from edges of the map in pixels
                      /*  CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,width,height,padding);
                        mMap.animateCamera(cu);*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void onRouteViewClick(View view) {
        if (mMap != null) {
            try {
                 // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onTryAgainCLicked(View view) {
        Animation slideBottom = AnimationUtils.loadAnimation(this, R.anim.slide_bottom);
        frameLayoutNoTrip.setVisibility(View.INVISIBLE);
        frameLayoutNoTrip.startAnimation(slideBottom);
        checkNetworkConnection();
    }
    // To animate view slide out from top to bottom
    public void slideToBottom(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }
    // To animate view slide out from bottom to top
    public void slideToTop(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,-view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
    }

    public void onRefreshClicked(View view) {
        Animation slideBottom = AnimationUtils.loadAnimation(this, R.anim.slide_bottom);
        frameLayoutNoInternet.setVisibility(View.INVISIBLE);
        frameLayoutNoInternet.startAnimation(slideBottom);
        checkNetworkConnection();
    }
//Geofence

    /**
     * Tracks whether the user requested to add or remove geofences, or to do neither.
     */
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    /**
     * Provides access to the Geofencing API.
     */
    private GeofencingClient mGeofencingClient;

    /**
     * The list of geofences used in this sample.
     */
    private ArrayList<Geofence> mGeofenceList;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;
    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;
    // A reference to the service used to get location updates.
    private LoadXLocationUpdatesService mService = null;
    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LoadXLocationUpdatesService.LocalBinder binder = (LoadXLocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };
    ReloadBroadCastReceiver reloadBroadCastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        isLoactionUpdateConnected = false;
        apiInterface = APIClient.getClient().create(APIInterface.class);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        txtAppVName = (TextView) findViewById(R.id.txtVersionName);
        txtAppVName.setText(SharedPrefsUtils.getStringPreference(HomeActivity.this, KEY_VERSION_NAME));
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.txtUserName);
        txtLicenseNo = (TextView) navHeader.findViewById(R.id.txtUserLicenseNo);
        driverImageView = (CircleImageView) navHeader.findViewById(R.id.profileImageView);
        loadingIndicatorView = (LoadingIndicatorView) findViewById(R.id.animatedLoader);
        loadTripLayout = (RelativeLayout) findViewById(R.id.tripBtnLayout);
        frameLayoutTrip = (FrameLayout) findViewById(R.id.frame_trip_Container);
        frameLayoutLoad = (FrameLayout) findViewById(R.id.frame_loading_Container);
        frameLayoutNoTrip = (FrameLayout) findViewById(R.id.frame_layout_NoTriip);
        frameLayoutNoInternet = (FrameLayout) findViewById(R.id.frame_layout_request_failed);
        frameLayoutNoInternet.setVisibility(View.INVISIBLE);
        trowTrip = (TableRow) findViewById(R.id.trowTrip);
        trowLoad = (TableRow) findViewById(R.id.trowLoad);
        loadTripLayout.setVisibility(View.INVISIBLE);
        loadTripLayout.setOnClickListener(this);
        loadingIndicatorView.smoothToHide();
        userName = SharedPrefsUtils.getStringPreference(HomeActivity.this, DRIVER_NAME);
        txtName.setText(userName);
        width = getResources().getDisplayMetrics().widthPixels;
        height = getResources().getDisplayMetrics().heightPixels;
        padding = (int) (height * 0.25);
        String vechileNo = SharedPrefsUtils.getStringPreference(HomeActivity.this, TRUCK_VECHILE_NO).toUpperCase();
        if (!TextUtils.isEmpty(vechileNo))
            txtLicenseNo.setText(vechileNo);
        String profilePicUrl = SharedPrefsUtils.getStringPreference(HomeActivity.this, KEY_DRI_IMG_URL);
        if (!TextUtils.isEmpty(profilePicUrl)) {
            Glide.with(this).load(APIClient.IMG_BASE_URL + profilePicUrl).apply(new RequestOptions()
                    .placeholder(R.drawable.taxi_driver)
                    .centerCrop()
                    .dontAnimate()).into(driverImageView);
        }
        Log.d(ApplicationUtil.APPTAG, "onCreate HOME");
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        currentLocImg = (ImageView) findViewById(R.id.imgCurrentLoction);
       /* reouteViewImg =(ImageView)findViewById(R.id.imgRouteView);
        reouteViewImg.setVisibility(View.INVISIBLE);*/
        directionViewImg = (ImageView) findViewById(R.id.imgRouteDirection);
        directionViewImg.setVisibility(View.INVISIBLE);
        mFusedLocationClient = getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mRequestingLocationUpdates = false;
        isTripLoaded = false;
        isCurrentLocationSelected = true;
        reloadBroadCastReceiver = new ReloadBroadCastReceiver();

     /*   LocalBroadcastManager.getInstance(this).registerReceiver(
                ReloadBroadCastReceiver,
                new IntentFilter(FCMConfig.PUSH_NOTIFICATION));*/
        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        reFormatContainerView();
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();
        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        checkNetworkConnection();
        // Register the local broadcast

        Log.d("LDHOME ", "HOME oncreate");

    }

    private void checkNetworkConnection() {
        boolean isNetworkConnected = ConnectivityReceiver.isConnected();
        if (!isNetworkConnected) {
            ApplicationUtil.getmInstance().checkUrInterntConnection(this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Toast.makeText(SplashActivity.this,"Hi",Toast.LENGTH_SHORT).show();
                    checkNetworkConnection();
                }
            }).show();
        } else {
            checkLocationPermission();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkNetworkConnection();
        Log.d("LDHOME ", "onNewIntent Home");
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        try {
            mLocationRequest = new LocationRequest();

            // Sets the desired interval for active location updates. This interval is
            // inexact. You may not receive updates at all if no location sources are available, or
            // you may receive them slower than requested. You may also receive updates faster than
            // requested if other applications are requesting location at a faster interval.
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates faster than this value.
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(ApplicationUtil.APPTAG,e.getMessage());
        }
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        try {
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    mCurrentLocation = locationResult.getLastLocation();
                    float bearing = locationResult.getLastLocation().getBearing();
                    if (mCurrentLocation != null) {
                        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                        if (mPositionMarker == null && mMap != null) {
                            mPositionMarker = mMap.addMarker(new MarkerOptions()
                                    .position(currentLatLng)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.truck_in_map))
                                    .anchor(0.5f, 0.5f)
                                    .rotation(bearing)
                                    .flat(true));
                            //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                        } else
                            mPositionMarker.setPosition(currentLatLng);
                    }

                    //   mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                    if (mCurrentLocation != null && mRequestingLocationUpdates) {
                        Log.d(ApplicationUtil.APPTAG,"updateLocationUI called");
                        updateLocationUI();
                        mRequestingLocationUpdates = false;
                    }
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(ApplicationUtil.APPTAG,e.getMessage());
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            // Set the map's camera position to the current location of the device.
            if (mMap != null) {

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mCurrentLocation.getLatitude(),
                                mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
                // sourceLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                mRequestingLocationUpdates = false;
                getTripDetails();


            }
        }
    }

    private void getTripDetails() {
        callLocationUpdateService();
        if (!isFinishing() && loadingIndicatorView != null)
            loadingIndicatorView.smoothToShow();
        int userID = SharedPrefsUtils.getIntegerPreference(HomeActivity.this, DRIVER_ID, 0);
        Call<OnGoingTripDetails> getOnGoingTripDetailsCall = apiInterface.getTripDetails(userID);
        getOnGoingTripDetailsCall.enqueue(new Callback<OnGoingTripDetails>() {
            @Override
            public void onResponse(Call<OnGoingTripDetails> call, Response<OnGoingTripDetails> response) {
                onGoingTripDetails = response.body();
                loadingIndicatorView.smoothToHide();
                if (onGoingTripDetails != null) {
                    if (onGoingTripDetails.getError()) {
                        Toast.makeText(HomeActivity.this, onGoingTripDetails.getMessage(), Toast.LENGTH_SHORT).show();
                       // ApplicationUtil.getmInstance().noTripDialog(HomeActivity.this);
                        Animation slideUp = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.slide_up);
                        frameLayoutNoTrip.setVisibility(View.VISIBLE);
                        frameLayoutNoTrip.startAnimation(slideUp);
                        // loadingIndicatorView.smoothToHide();
                    } else {
                        //String tripStatus = SharedPrefsUtils.getStringPreference(HomeActivity.this, KEY_TRIP_STATUS);
                        if (onGoingTripDetails.getTripResponse().getTripDetails().getId() != null)
                            SharedPrefsUtils.setStringPreference(HomeActivity.this, KEY_TRIP_ID, onGoingTripDetails.getTripResponse().getTripDetails().getId().toString());
                        if (onGoingTripDetails.getTripResponse().getTripDetails().getTripStatus() != null) {
                            String tripStatus = onGoingTripDetails.getTripResponse().getTripDetails().getTripStatus();
                            SharedPrefsUtils.setStringPreference(HomeActivity.this, KEY_TRIP_STATUS, onGoingTripDetails.getTripResponse().getTripDetails().getTripStatus());
                            Log.d(ApplicationUtil.APPTAG, "TS from res" + onGoingTripDetails.getTripResponse().getTripDetails().getTripStatus());
                            if (tripStatus.equals(TR_ONGOING) || tripStatus.equals(TR_SCHEDULED)) {
                                frameLayoutNoTrip.setVisibility(View.INVISIBLE);
                                addMarkerToMap();
                            } else {
                                Animation slideUp = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.slide_up);
                                frameLayoutNoTrip.setVisibility(View.VISIBLE);
                                frameLayoutNoTrip.startAnimation(slideUp);
                              //  ApplicationUtil.getmInstance().noTripDialog(HomeActivity.this);
                            }
                        }

                    }
                } else {
                    Toast.makeText(HomeActivity.this, "No Data Found", Toast.LENGTH_SHORT).show();
                    Log.d("LDHOME ", "ERROR response" + response.toString());
                }
            }

            @Override
            public void onFailure(Call<OnGoingTripDetails> call, Throwable t) {
                // Toast.makeText(HomeActivity.this, "Network Error! Try Again.", Toast.LENGTH_SHORT).show();
                loadingIndicatorView.smoothToHide();
                Animation slideUp = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.slide_up);
                frameLayoutNoInternet.setVisibility(View.VISIBLE);
                frameLayoutNoInternet.startAnimation(slideUp);
              //  checkNetworkConnection();
                //reloadDataRequest();
            }
        });
    }

    private void callLocationUpdateService() {
        try {
            bindService(new Intent(this, LoadXLocationUpdatesService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
            mService.requestLocationUpdates();
        } catch (Exception e) {
            Log.d("LDHOME ", "On START");
            e.printStackTrace();
        }
    }

    private void reloadDataRequest() {
        Dialog removeDialog = ApplicationUtil.showDialog(HomeActivity.this, "Try Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // deleteNotification(notification);
                mRequestingLocationUpdates = true;
                checkLocationPermission();

            }
        }, "Network Error!", "Sorry We can't connect. Try Again.");
        removeDialog.show();
        removeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //  notificationAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(ApplicationUtil.APPTAG, "Home onpause");
        try {
            if (loadingIndicatorView != null)
                loadingIndicatorView.dismissDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMarkerToMap() {
        if (onGoingTripDetails != null) {
            double destinationLat;
            double destinationLong;
            double sourceLat;
            double sourceLong;
            if (onGoingTripDetails.getTripResponse().getLoadDetails().getSourceLat() != null) {
                destinationLat = Double.parseDouble(onGoingTripDetails.getTripResponse().getLoadDetails().getDestLat());
            } else {
                destinationLat = 0;
            }
            // loadTripLayout.setVisibility(View.VISIBLE);
            if (onGoingTripDetails.getTripResponse().getLoadDetails().getSourceLong() != null) {
                destinationLong = Double.parseDouble(onGoingTripDetails.getTripResponse().getLoadDetails().getDestLong());
            } else {
                destinationLong = 0;
            }
            sourceLocation = new Location("S");
            if (onGoingTripDetails.getTripResponse().getLoadDetails().getSourceLat() != null) {
                sourceLocation.setLatitude(Double.parseDouble(onGoingTripDetails.getTripResponse().getLoadDetails().getSourceLat()));
            } else {
                sourceLocation.setLatitude(0);
            }
            if (onGoingTripDetails.getTripResponse().getLoadDetails().getSourceLong() != null) {
                sourceLocation.setLongitude(Double.parseDouble(onGoingTripDetails.getTripResponse().getLoadDetails().getSourceLong()));
            } else {
                sourceLocation.setLongitude(0);
            }
            // sourceLat = Double.parseDouble(onGoingTripDetails.getTripResponse().getLoadDetails().getSourceLat());
            // sourceLong = Double.parseDouble(onGoingTripDetails.getTripResponse().getLoadDetails().getSourceLong());
            destinationLocation = new Location("D1");
            destinationLocation.setLatitude(destinationLat);
            destinationLocation.setLongitude(destinationLong);

            //TODO:testing
           /* destinationLocation.setLatitude(17.491595);
            destinationLocation.setLongitude(78.29204070000003);

            sourceLocation.setLatitude(12.9482166);
            sourceLocation.setLongitude(80.13977419999992);*/
            Log.d("LATLNG S", sourceLocation.getLatitude() + " " + sourceLocation.getLongitude());
            Log.d("LATLNG S", destinationLat + " " + destinationLong);
            destinationLatLng = new LatLng(destinationLocation.getLatitude(), destinationLocation.getLongitude());
            sourceLatLng = new LatLng(sourceLocation.getLatitude(), sourceLocation.getLongitude());
            plotMapWithSourceAndDestination();


            frameLayoutTrip.setVisibility(View.VISIBLE);
            frameLayoutLoad.setVisibility(View.VISIBLE);

        }

    }

    private void plotMapWithSourceAndDestination() {
        if (mMap != null) {
            //  Bitmap sourceBitmap = ApplicationUtil.getMapBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.source_marker), "S");

            //  Bitmap destinationBitmap = ApplicationUtil.getMapBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.destination_marker), "D1");
            String sourceAddress = onGoingTripDetails.getTripResponse().getLoadDetails().getSourceAddress();
            String desinationAddress = onGoingTripDetails.getTripResponse().getLoadDetails().getDestinationAddress();
            SharedPrefsUtils.setStringPreference(HomeActivity.this, KEY_DEST_ADDRESS, desinationAddress);
            Marker sourceMarker = mMap.addMarker(new MarkerOptions().position(sourceLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.source_marker)).title(sourceAddress));
            Marker destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker)).title(desinationAddress));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(sourceMarker.getPosition());
            builder.include(destinationMarker.getPosition());
            bounds = builder.build();
            // offset from edges of the map in pixels
           // CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,width,height,padding);
          //  mMap.animateCamera(cu);

            LatLng centerLatLng = getCenter(sourceLatLng,destinationLatLng);
            int zoom = getZoomLevel(centerLatLng,destinationLatLng);
             mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerLatLng, zoom));
            //Add route directions to map
            getDirectionPoints("driving", sourceLatLng, destinationLatLng);
            HashMap<String, LatLng> geofenceLoactions = new HashMap<>();
            geofenceLoactions.put("S", sourceLatLng);
            geofenceLoactions.put("D1", destinationLatLng);
            boolean isGeofenceAdded = SharedPrefsUtils.getBooleanPreference(HomeActivity.this, GeoConstants.GEOFENCES_ADDED_KEY, false);
            if (!isGeofenceAdded) {
                populateGeofenceList(geofenceLoactions);
                //adding geofences
                addGeofence();
            }
        }
    }
    public Pair<LatLng, Integer> getCenterWithZoomLevel(LatLng... l) {
        float max = 0;

        if (l == null || l.length == 0) {
            return null;
        }
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (int count = 0; count < l.length; count++) {
            if (l[count] == null) {
                continue;
            }
            b.include(l[count]);
        }

        LatLng center = b.build().getCenter();

        float distance = 0;
        for (int count = 0; count < l.length; count++) {
            if (l[count] == null) {
                continue;
            }
            distance = distance(center.latitude,center.longitude, l[count].latitude,l[count].longitude);
            if (distance > max) {
                max = distance;
            }
        }

        double scale = max / 1000;
        int zoom = ((int) (16 - Math.log(scale) / Math.log(2)));
        return new Pair<LatLng, Integer>(center, zoom);
    }
    private LatLng getCenter(LatLng...l){

        float max = 0;

        if (l == null || l.length == 0) {
            return null;
        }
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (int count = 0; count < l.length; count++) {
            if (l[count] == null) {
                continue;
            }
            b.include(l[count]);
        }

        LatLng center = b.build().getCenter();
        return center;
    }
    private  int getZoomLevel(LatLng center,LatLng dest){
        float distance = 0;

            distance = distance(center.latitude,center.longitude,dest.latitude,dest.longitude);
        double scale = distance / 1000;
        int zoom = ((int) (14- Math.log(scale) / Math.log(2)));
        Log.d("LDHOME zoom ",zoom+" "+distance);
        return zoom;
    }
    public float distance (double lat_a, double lng_a, double lat_b, double lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }
    private void populateGeofenceList(HashMap<String, LatLng> geofenceLoactions) {
        for (Map.Entry<String, LatLng> entry : geofenceLoactions.entrySet()) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(GeoConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay(GeoConstants.GEOFENCE_LOITERING_DELAY)
                    // Create the geofence.
                    .build());
        }
    }

    /**
     * Uses a {@link LocationSettingsRequest.Builder} to build
     * a {@link LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_customer) {
            checkPhoneCallPermission(RC_PHONE_CALL_PERM);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkPhoneCallPermission(int permissionCode) {
        boolean hasLocationPermission = EasyPermissions.hasPermissions(this, PHONE_CALL_PERMISSION);
        if (!hasLocationPermission) {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_call_phone),
                    permissionCode,
                    PHONE_CALL_PERMISSION);
        } else {
            if (permissionCode == RC_PHONE_CALL_PERM) {
                ApplicationUtil.getmInstance().callCustomerCareDialog(HomeActivity.this, CUSTOMER_CARE_NO);
            }
        }
    }

    private void checkPhoneCallAndSMSPermission(int permissionCode) {
        boolean hasLocationPermission = EasyPermissions.hasPermissions(this, PHONE_CALL_AND_SMS_PERMISSION);
        if (!hasLocationPermission) {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_call_phone),
                    permissionCode,
                    PHONE_CALL_AND_SMS_PERMISSION);
        } else {
            if (permissionCode == RC_PHONE_CALL_AND_SMS_PERMS) {
                ApplicationUtil.getmInstance().callSupervisorDialog(HomeActivity.this, onGoingTripDetails.getTripResponse().getLoadDetails().getSupervisorAtSourceName(), onGoingTripDetails.getTripResponse().getLoadDetails().getSupervisorAtSourceContact());
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
         /* SharedPrefsUtils.setBooleanPreference(HomeActivity.this, KEY_IS_PROFILE_UPDATED, false);
          SharedPrefsUtils.setStringPreference(HomeActivity.this, KEY_USER_NAME, null);
          SharedPrefsUtils.setBooleanPreference(HomeActivity.this, KEY_IS_PHONE_NO_UPDATED, false);*/
            //PreferenceManager.getDefaultSharedPreferences(HomeActivity.this).edit().clear().apply();
           // PreferenceManager.getDefaultSharedPreferences(HomeActivity.this).edit().clear().commit();
            if(mService!=null)
            mService.removeLocationUpdates();
            removeGeofences();
            SharedPrefsUtils.clearPreferences(HomeActivity.this);
            Intent intent = new Intent(HomeActivity.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            // Handle the camera action
        }
         /* else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getDeviceLocation() {
    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        try {
            boolean hasLocationPermission = EasyPermissions.hasPermissions(this, LOCATION_PERMISSION);
            if (hasLocationPermission) {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                FusedLocationProviderClient mFusedLocationProviderClient = getFusedLocationProviderClient(this);
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            if (mLastKnownLocation != null) {
                                // Set the map's camera position to the current location of the device.
                                mLastKnownLocation = (Location) task.getResult();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                           /* Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastKnownLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);*/
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("LDHOME: %s", e.getMessage());
        }
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mRequestingLocationUpdates = true;
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(HomeActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(HomeActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }
                    }
                });
    }

    private void checkLocationPermission() {
        boolean hasLocationPermission = EasyPermissions.hasPermissions(this, LOCATION_PERMISSION);
        if (!hasLocationPermission) {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location),
                    RC_LOCATION_PERM,
                    LOCATION_PERMISSION);
        } else {
            handleNotificaton();
            startLocationUpdates();
        }
    }

    private void handleNotificaton() {
        Intent userIntent = getIntent();
        Bundle extras = userIntent.getExtras();
        if (userIntent.hasExtra("UPDATE")) {
            update = extras.getString("UPDATE");
        }

        if (update != null) {
            Log.d("LDHOME ", update);
            bindService(new Intent(this, LoadXLocationUpdatesService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
            if (update.equals("S")) {

//                showLoadUnloadDetail = true;
//                SharedPrefsUtils.setStringPreference(HomeActivity.this,KEY_TRIP_STATUS,"ONGOING");
            } else if (update.equals("D1")) {
              /*  SharedPrefsUtils.setStringPreference(HomeActivity.this,KEY_TRIP_STATUS,"ONCOMPLETE");
                showLoadUnloadDetail = true;*/
            } else if (update.equals(LWS)) {

            } else if (update.equals(ULWS)) {

            }
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
        if (requestCode == RC_LOCATION_PERM) {
            startLocationUpdates();
        } else if (requestCode == RC_PHONE_CALL_PERM) {
            ApplicationUtil.getmInstance().callCustomerCareDialog(HomeActivity.this, CUSTOMER_CARE_NO);
        } else if (requestCode == RC_PHONE_CALL_AND_SMS_PERMS) {
            checkPhoneCallAndSMSPermission(RC_PHONE_CALL_AND_SMS_PERMS);
            // ApplicationUtil.getmInstance().callSupervisorDialog(HomeActivity.this, onGoingTripDetails.getTripResponse().getLoadDetails().getSupervisorAtSourceName(), onGoingTripDetails.getTripResponse().getLoadDetails().getSupervisorAtSourceContact());
        } else if (requestCode == RC_STORAGE_PERM_IMAGE_PICKUP) {
          showImagePickDialog();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(ApplicationUtil.APPTAG,"onMAp");
        mMap = googleMap;
        mMap.clear();
        mMap.setMaxZoomPreference(15.0f);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        getDeviceLocation();

      /*  ApplicationUtil.getmInstance().noTripDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Toast.makeText(SplashActivity.this,"Hi",Toast.LENGTH_SHORT).show();
            }
        }).show();*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(reloadBroadCastReceiver!=null)
        registerReceiver(reloadBroadCastReceiver,new IntentFilter(FCMConfig.PUSH_NOTIFICATION));
        //reFormatContainerView();


        /*frameLayoutLoad.setVisibility(View.INVISIBLE);
        frameLayoutTrip.setVisibility(View.INVISIBLE);*/

    }

    private void reFormatContainerView() {
        trowTrip.removeAllViews();
        trowLoad.removeAllViews();
        trowTrip.addView(frameLayoutTrip);
        trowLoad.addView(frameLayoutLoad);
    }

    private void getDirectionPoints(String type, LatLng orgin, final LatLng destination) {

        String url = "https://maps.googleapis.com/maps/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MapsAPIInterface service = retrofit.create(MapsAPIInterface.class);
        Call<Example> call = service.getDistanceDuration("metric", orgin.latitude + "," + orgin.longitude, destination.latitude + "," + destination.longitude, type);

        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                if (loadingIndicatorView != null)
                    loadingIndicatorView.smoothToHide();
                try {
                    //Remove previous line from map
                    if (line != null) {
                        line.remove();
                    }
                    // Bitmap sourceBitmap = ApplicationUtil.getMapBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.source_marker), "S");

                    // Bitmap destinationBitmap = ApplicationUtil.getMapBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.destination_marker), "D1");


                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getRoutes().size(); i++) {
                        String distance = response.body().getRoutes().get(i).getLegs().get(i).getDistance().getText();
                        String time = response.body().getRoutes().get(i).getLegs().get(i).getDuration().getText();
                        // ShowDistanceDuration.setText("Distance:" + distance + ", Duration:" + time);
                        String encodedString = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();
                        List<LatLng> list = decodePoly(encodedString);
                        line = mMap.addPolyline(new PolylineOptions()
                                .addAll(list)
                                .width(15)
                                .color(Color.BLACK)
                                .geodesic(false)
                                .startCap(new RoundCap())
                                .endCap(new RoundCap())

                        );
                    }
                    String tripId = SharedPrefsUtils.getStringPreference(HomeActivity.this, KEY_TRIP_ID);
                    if (!TextUtils.isEmpty(tripId))
                        toolbar.setTitle("Trip #" + tripId);
                    String tripStatus = SharedPrefsUtils.getStringPreference(HomeActivity.this, KEY_TRIP_STATUS);
                    loadTripDetail(tripId);

                    Log.d("LDHOME", "tripStatus " + tripStatus + " showLoadUnloadDetail " + showLoadUnloadDetail);
                    /*if(tripStatus.equals("SCHEDULED")) {
                        float distanceTo = mCurrentLocation.distanceTo(sourceLocation);
                        if (distanceTo <= GEOFENCE_RADIUS_IN_METERS)
                        {
                            tripStatus = "ONGOING";
                            SharedPrefsUtils.setStringPreference(HomeActivity.this,KEY_TRIP_STATUS,"ONGOING");
                        }
                    }else if(tripStatus.equals("ONGOING")){
                        float distanceTo = mCurrentLocation.distanceTo(destinationLocation);
                        if (distanceTo <= GEOFENCE_RADIUS_IN_METERS)
                        {
                            tripStatus = "ONCOMPLETE";
                            SharedPrefsUtils.setStringPreference(HomeActivity.this,KEY_TRIP_STATUS,"ONCOMPLETE");
                        }
                    }*/
                    Log.d(ApplicationUtil.APPTAG, "TS before init" + tripStatus);
                    if (update == null) {
                        if (showLoadUnloadDetail || tripStatus.equals(TR_ONGOING) || tripStatus.equals(TR_SCHEDULED) || tripStatus.equals(TR_COMPLETE))
                            initLoadUnloadFragment(tripStatus);
                    } else {
                        initLoadUnloadFragment(update);
                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                loadingIndicatorView.smoothToHide();
            }
        });

    }

    private void loadTripDetail(String tripId) {
        isTripLoaded = true;
        directionViewImg.setVisibility(View.VISIBLE);
        // reouteViewImg.setVisibility(View.VISIBLE);
        frameLayoutTrip.setVisibility(View.VISIBLE);
        Log.d("LATLNG ", "VISIBLE");
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();//.setCustomAnimations(R.anim.slide_in, R.anim.abc_slide_out_top);
        tripDetailFragment = TripDetailFragment.newInstance(tripId, onGoingTripDetails.getTripResponse().getLoadDetails());
        fragmentTransaction.replace(R.id.frame_trip_Container, tripDetailFragment);
        fragmentTransaction.commit();
    }

    private void initLoadUnloadFragment(String tripStatus) {
        Log.d("LDHOME ", "update " + update);
        frameLayoutLoad.setVisibility(View.VISIBLE);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();//.setCustomAnimations(R.anim.slide_in, R.anim.abc_slide_out_top);
        if (tripStatus != null) {
            loadUnloadFragment = LoadUnLoadFragment.newInstance(tripStatus);
        /*if(tripStatus.equals("ONGOING")||tripStatus.equals("SCHEDULED"))
        loadUnloadFragment = LoadUnLoadFragment.newInstance("Load");
        else
            loadUnloadFragment = LoadUnLoadFragment.newInstance("UnLoad");*/
            fragmentTransaction.replace(R.id.frame_loading_Container, loadUnloadFragment);
            fragmentTransaction.commit();
        }

    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
            case PICK_POD_IMG_REQ: {
                Uri uri = null;
                String url = null;
                String path = null;
                if(IMG_SELECTED_VIA==GALLERY) {
                    if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                        try {
                            uri = data.getData();
                            url = FileUtil.getFileName(HomeActivity.this, uri);
                            File actualImage = FileUtil.from(this, data.getData());
                            if (loadUnloadFragment != null)
                                loadUnloadFragment.setPODCopy(actualImage, url, onGoingTripDetails.getTripResponse().getTripDetails().getId());

                        } catch (IOException e) {
                            Toast.makeText(HomeActivity.this, "Image not Readable. Try Agin!", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }
                if(IMG_SELECTED_VIA==CAMERA){
                    Bitmap thumbnail  = (Bitmap) data.getExtras().get("data");
                    path = saveImage(thumbnail);
                    url = path;
                    File actualImage = new File(path);
                    if (loadUnloadFragment != null)
                        loadUnloadFragment.setPODCopy(actualImage, url, onGoingTripDetails.getTripResponse().getTripDetails().getId());

                }

                    /*try {
                        Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG, 70, stream);
                        byte[] byteArray = stream.toByteArray();
                        String encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        url = FileUtil.getFileName(HomeActivity.this, uri);
                        if (loadUnloadFragment != null)
                            loadUnloadFragment.setPODCopy(encodedString, url, onGoingTripDetails.getTripResponse().getTripDetails().getId());
                        bmp.recycle();
                        bmp = null;
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    break;

            }
            case PICK_POD_IMG_BACK_REQ: {
                Uri uri = null;
                String url = null;
                String path = null;
                if(IMG_SELECTED_VIA==GALLERY) {
                    if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                        try {
                            uri = data.getData();
                            url = FileUtil.getFileName(HomeActivity.this, uri);
                            File actualImage = FileUtil.from(this, data.getData());
                            if (loadUnloadFragment != null)
                                loadUnloadFragment.setPODBackCopy(actualImage, url, onGoingTripDetails.getTripResponse().getTripDetails().getId());

                        } catch (IOException e) {
                            Toast.makeText(HomeActivity.this, "Image not Readable. Try Agin!", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }
                if(IMG_SELECTED_VIA==CAMERA){
                    Bitmap thumbnail  = (Bitmap) data.getExtras().get("data");
                    path = saveImage(thumbnail);
                    url = path;
                    File actualImage = new File(path);
                    if (loadUnloadFragment != null)
                        loadUnloadFragment.setPODBackCopy(actualImage, url, onGoingTripDetails.getTripResponse().getTripDetails().getId());

                }

                    /*try {
                        Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG, 70, stream);
                        byte[] byteArray = stream.toByteArray();
                        String encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        url = FileUtil.getFileName(HomeActivity.this, uri);
                        if (loadUnloadFragment != null)
                            loadUnloadFragment.setPODCopy(encodedString, url, onGoingTripDetails.getTripResponse().getTripDetails().getId());
                        bmp.recycle();
                        bmp = null;
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    break;

            }
            case PICK_WS_IMG_REQ: {
                Uri uri = null;
                String url = null;
                String path = null;
                if(IMG_SELECTED_VIA==GALLERY) {
                    if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                        try {
                            uri = data.getData();
                            url = FileUtil.getFileName(HomeActivity.this, uri);
                            File actualImage = FileUtil.from(this, data.getData());
                            if (loadUnloadFragment != null)
                                loadUnloadFragment.setWSCopy(actualImage, url, onGoingTripDetails.getTripResponse().getTripDetails().getId());

                        } catch (IOException e) {
                            Toast.makeText(HomeActivity.this, "Image not Readable. Try Agin!", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }
                    if(IMG_SELECTED_VIA==CAMERA){
                       Bitmap thumbnail  = (Bitmap) data.getExtras().get("data");
                        path = saveImage(thumbnail);
                        url = path;
                        File actualImage = new File(path);
                        if (loadUnloadFragment != null)
                            loadUnloadFragment.setWSCopy(actualImage, url, onGoingTripDetails.getTripResponse().getTripDetails().getId());

                    }
                  /*  try {
                        Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG, 70, stream);
                        byte[] byteArray = stream.toByteArray();
                        String encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        url = FileUtil.getFileName(HomeActivity.this, uri);
                        if (loadUnloadFragment != null)
                            loadUnloadFragment.setWSCopy(encodedString, url, onGoingTripDetails.getTripResponse().getTripDetails().getId());
                        bmp.recycle();
                        bmp = null;
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    break;
            }
        }
    }
    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File dir = new File(getExternalFilesDir(null), IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            File f = new File(dir, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tripBtnLayout) {
            // loadTripLayout.setVisibility(View.INVISIBLE);
            //  frameLayoutTrip.setVisibility(View.VISIBLE);
            //  loadTripDetail();
        }
    }

    //Change row position if not expanded
    @Override
    public void onTripClicked(boolean isExpand) {
        if (!isExpand) {
            if (loadUnloadFragment != null) {
                trowTrip.removeAllViews();
                trowLoad.removeAllViews();
                trowTrip.addView(frameLayoutTrip);
                trowLoad.addView(frameLayoutLoad);
                loadUnloadFragment.hideLayout();
            }
        } else {

        }
        //frameLayoutTrip.setVisibility(View.INVISIBLE);
        //loadTripLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadCLicked(boolean isExpand) {
        if (!isExpand) {
            trowTrip.removeAllViews();
            trowLoad.removeAllViews();
            trowTrip.addView(frameLayoutLoad);
            trowLoad.addView(frameLayoutTrip);
            tripDetailFragment.hideDetail();
        }
    }

    @Override
    public void onPODSelect() {
        TYPE_OF_IMG_REQ  = PICK_POD_IMG_REQ;
        checkStoragePermissionForUpload(RC_STORAGE_PERM_IMAGE_PICKUP);
    }

    @Override
    public void onWSSelect() {
        TYPE_OF_IMG_REQ  = PICK_WS_IMG_REQ;
        checkStoragePermissionForUpload(RC_STORAGE_PERM_IMAGE_PICKUP);
    }
    @Override
    public void onPODBackSelect() {
        TYPE_OF_IMG_REQ  = PICK_POD_IMG_BACK_REQ;
        checkStoragePermissionForUpload(RC_STORAGE_PERM_IMAGE_PICKUP);
    }
    @Override
    public void onTripComplete() {
        directionViewImg.setVisibility(View.INVISIBLE);
        frameLayoutTrip.setVisibility(View.INVISIBLE);
        frameLayoutLoad.setVisibility(View.INVISIBLE);
        if(mMap!=null)
            mMap.clear();
        reFormatContainerView();
        checkNetworkConnection();
    }

    @Override
    public void onLoadDetailUpdated() {
        if (tripDetailFragment != null) {
            trowTrip.removeAllViews();
            trowLoad.removeAllViews();
            trowTrip.addView(frameLayoutTrip);
            trowLoad.addView(frameLayoutLoad);
            tripDetailFragment.setTripStatus(TR_ONGOING);
            tripDetailFragment.showLayout();
            loadUnloadFragment.hideLayout();
        }
    }




    private void checkStoragePermissionForUpload(int rcStoragePerm) {
        hasStoragePermission = EasyPermissions.hasPermissions(this, STORAGE_PERMISSION);
        if (!hasStoragePermission) {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_storage),
                    rcStoragePerm,
                    STORAGE_PERMISSION);
        } else if (rcStoragePerm == RC_STORAGE_PERM_IMAGE_PICKUP) {
            showImagePickDialog();
           // pickPODFromGallery();
        }
    }

    private void pickPODBackFromGallery() {
        Intent intent = new Intent();
// Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
// Always show the chooser (if there are multiple options available)
        startActivityForResult(intent,PICK_POD_IMG_BACK_REQ);
    }

    private void pickPODFromGallery() {
        Intent intent = new Intent();
// Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
// Always show the chooser (if there are multiple options available)
        startActivityForResult(intent, PICK_POD_IMG_REQ);
    }

    private void pickWSFromGallery() {
        Intent intent = new Intent();
// Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
// Always show the chooser (if there are multiple options available)
        startActivityForResult(intent, PICK_WS_IMG_REQ);
    }

    @Override
    public void callSupervisorDialog() {
        checkPhoneCallAndSMSPermission(RC_PHONE_CALL_AND_SMS_PERMS);
    }

    @Override
    public void onSourceLocationClicked() {
        if (mMap != null && sourceLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(sourceLocation.getLatitude(), sourceLocation.getLongitude()), 13.0f));
        }
    }

    @Override
    public void onDestinationLocationClicked() {
        if (mMap != null && destinationLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(destinationLocation.getLatitude(), destinationLocation.getLongitude()), 13.0f));
        }
    }

    @Override
    public void tryLoadingTripAgain() {
        checkNetworkConnection();
    }

    public void scaleView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(
                1f, 1f, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(1000);
        v.startAnimation(anim);
    }


    public void onDirectionImgClick(View view) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + sourceLatLng.latitude + "," + sourceLatLng.longitude + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @SuppressLint("MissingPermission")
    private void addGeofence() {
        if (!checkPermissions())
            return;
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        if (!checkPermissions()) {
            return;
        }
        if(mGeofencingClient!=null)
        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*LocalBroadcastManager.getInstance(this).unregisterReceiver(
                ReloadBroadCastReceiver);*/
        if(reloadBroadCastReceiver!=null)
        unregisterReceiver(reloadBroadCastReceiver);
        Log.d("LDHOME ", "HOME on stop " + mBound);
        //   mRequestingLocationUpdates = true;
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        String tripStatus = SharedPrefsUtils.getStringPreference(HomeActivity.this, KEY_TRIP_STATUS);
        if (tripStatus.equals("ONGOING") || tripStatus.equals("ONCOMPLETE")) {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            bindService(new Intent(this, LoadXLocationUpdatesService.class), mServiceConnection,
                    Context.BIND_AUTO_CREATE);
            mService.requestLocationUpdates();
        } catch (Exception e) {
            Log.d("LDHOME ", "On START");
            e.printStackTrace();
        }

    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            SharedPrefsUtils.setBooleanPreference(HomeActivity.this, GeoConstants.GEOFENCES_ADDED_KEY, true);
            //Toast.makeText(this, "Location Monitor Started", Toast.LENGTH_SHORT).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);
        }
    }
    public class ReloadBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    reFormatContainerView();
                    startLocationUpdates();
                }
            });

        }
    };

    private void showImagePickDialog(){
        final Dialog imagePickDialog = new Dialog(HomeActivity.this);
        imagePickDialog.setContentView(R.layout.alert_dialog_image_selection);
        imagePickDialog.setTitle("Choose image from");
        LinearLayout cameraLayout = (LinearLayout)imagePickDialog.findViewById(R.id.captureCameraLayout);
        LinearLayout galleryLayout = (LinearLayout)imagePickDialog.findViewById(R.id.galleryLayout);
        cameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePickDialog.dismiss();
                IMG_SELECTED_VIA = CAMERA;
                takePhotoFromCamera();
            }
        });
        galleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePickDialog.dismiss();
                // choosePhotoFromGallary();
                IMG_SELECTED_VIA = GALLERY;
                pickImageFromGallery();
            }
        });
        imagePickDialog.show();

    }
    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TYPE_OF_IMG_REQ);
    }
    private void pickImageFromGallery() {
        Intent intent = new Intent();
// Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), TYPE_OF_IMG_REQ);

    }
}