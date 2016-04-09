package com.freeman.flyshare;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.LocationListener;

import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import dji.sdk.MissionManager.DJIFollowMeMission;
import dji.sdk.MissionManager.DJIMission;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIError;


public final class FollowMeMissionFragment extends MissionFragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private OnFollowMeMissionFragmentInteractionListener mListener;
    private RadioGroup headingRG;
    private RadioButton headingObjectRB, controlledByRC_RB;
    private DJIFollowMeMission.DJIFollowMeHeading heading = DJIFollowMeMission.DJIFollowMeHeading.ControlledByRemoteController;
    UpdateFollowingObjectLocationThread updateFollowingObjectLocationThread = null;


    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    GoogleApiClient googleApiClient;
    LocationRequest mLocationRequest;
    Location mCurrentLocation;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void missionProgressStatus(DJIMission.DJIMissionProgressStatus djiMissionProgressStatus) {

    }

    @Override
    protected void startUpdateFollowedObjectLocation() {
        super.startUpdateFollowedObjectLocation();
        updateFollowingObjectLocationThread = new UpdateFollowingObjectLocationThread(mMission, homeLat, homeLng);
        if (updateFollowingObjectLocationThread != null)
            updateFollowingObjectLocationThread.start();
    }

    @Override
    protected void stopUpdateFollowedObjectLocation() {
        super.stopUpdateFollowedObjectLocation();
        if (updateFollowingObjectLocationThread != null) {
            updateFollowingObjectLocationThread.stopRunning();
            updateFollowingObjectLocationThread = null;
        }
    }

    @Override
    protected void setPauseUpdateFollowedObjectLocation(boolean isPaused) {
        super.setPauseUpdateFollowedObjectLocation(isPaused);
        if (updateFollowingObjectLocationThread != null)
            updateFollowingObjectLocationThread.setIsPause(isPaused);
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("FollowMe mission", "Connection failed: " + connectionResult.toString());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(fragmentActivity);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(fragmentActivity, result, 0).show();
            }
            return false;
        }

        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        UpdateFollowingObjectLocationThread.tarPosLat = location.getLatitude();
        UpdateFollowingObjectLocationThread.tarPosLon = location.getLongitude();
        Log.e("FollowMe Mission", "Location: (" + Double.toString(location.getLatitude()) + ", " + Double.toString(location.getLongitude()) + ")");
    }

    public static class UpdateFollowingObjectLocationThread extends Thread {
        private DJIMission mDJIMission = null;
        private double mHomeLatitude = 181;
        private double mHomeLongitude = 181;

        private boolean mIsRunning = false;
        private boolean mIsPausing = false;

        float radius = 6378137;
        public static double tarPosLat;
        public static double tarPosLon;


        public UpdateFollowingObjectLocationThread(DJIMission mission, double homeLat, double homeLng) {
            super();
            mDJIMission = mission;
            Log.e("new update Object", "________________object created");
            this.mHomeLatitude = homeLat;
            this.mHomeLongitude = homeLng;


        }

        @Override
        public void run() {
            if (mDJIMission == null ||
                    mDJIMission.getMissionType() != DJIMission.DJIMissionType.Followme ||
                    !Utils.checkGpsCoordinate(mHomeLatitude, mHomeLongitude))
                return;
            mIsRunning = true;
//            Log.e("update Object", "________________start running");


            while (mIsRunning) {
                if (!mIsPausing) {
                    Log.e("onLocationChanged", "Lat: " + Double.toString(tarPosLat) + ", Lng: " + Double.toString(tarPosLon));
                    DJIFollowMeMission.updateFollowMeCoordinate(
                            Utils.Degree(tarPosLat), Utils.Degree(tarPosLon), new DJIBaseComponent.DJICompletionCallback() {
                                @Override
                                public void onResult(DJIError error) {
                                }
                            });
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopRunning() {
            mIsRunning = false;

        }

        public void setIsPause(boolean isPaused) {
            mIsPausing = isPaused;
        }
    }

    public FollowMeMissionFragment() {
        // Required empty public constructor
    }

    @Override
    protected DJIMission initMission() {
        updateHomeLocation();
        if (!Utils.checkGpsCoordinate(homeLat, homeLng)) {
            Utils.setResultToToast(getContext(), "No home point!");
            return null;
        }
        DJIFollowMeMission djiFollowMeMission = new DJIFollowMeMission(homeLat, homeLng);
        djiFollowMeMission.heading = this.heading;

        return djiFollowMeMission;
    }

    @Override
    protected int getFragmentViewResource() {
        return R.layout.fragment_follow_me_mission;
    }

    @Override
    protected void initMissionOngoingUIComponents() {

    }

    @Override
    protected void initConfigMissionUIComponents() {
        this.headingRG = (RadioGroup) mView.findViewById(R.id.follow_me_heading);
        this.headingObjectRB = (RadioButton) mView.findViewById(R.id.headingObject);
        this.controlledByRC_RB = (RadioButton) mView.findViewById(R.id.headingFree);
        this.controlledByRC_RB.setChecked(true);
        headingRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.headingFree) {
                    heading = DJIFollowMeMission.DJIFollowMeHeading.ControlledByRemoteController;
                } else if (checkedId == R.id.headingObject) {
                    heading = DJIFollowMeMission.DJIFollowMeHeading.TowardFollowPosition;
                }
            }
        });
    }

    @Override
    protected void initMissionVariables() {
        if (isGooglePlayServicesAvailable()) {
            googleApiClient = new GoogleApiClient.Builder(fragmentActivity)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            createLocationRequest();
        } else {
            Log.e("FollowMe", "______________________ Google Service not available!");
        }
    }

    @Override
    public void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void finishMission() {
        super.finishMission();
    }

    public static FollowMeMissionFragment newInstance() {
        FollowMeMissionFragment followMeMissionFragment = new FollowMeMissionFragment();
        return followMeMissionFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFollowMeMissionFragmentInteractionListener) {
            mListener = (OnFollowMeMissionFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFollowMeMissionFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFollowMeMissionFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
