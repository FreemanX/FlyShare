package com.freeman.flyshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.Battery.DJIBattery;
import dji.sdk.Camera.DJICamera;
import dji.sdk.Camera.DJICameraSettingsDef;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.FlightController.DJIFlightControllerDataType;
import dji.sdk.FlightController.DJIFlightControllerDelegate;
import dji.sdk.base.DJIBaseComponent.DJICompletionCallback;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIError;
import dji.sdk.Camera.DJICameraSettingsDef.CameraMode;
import dji.sdk.Camera.DJICameraSettingsDef.CameraShootPhotoMode;

public class FPVActivity extends AppCompatActivity implements View.OnClickListener,
        MissionSelectionFragment.OnFragmentInteractionListener,
        MissionFragment.OnCancelClickListener,
        MissionRequestMapHandler {
    String TAG = "FPVActivity";
    public static FragmentManager fragmentManager;
    public boolean isPhotoMode = true;
    public static boolean FPVIsSmall = false;
    private boolean cameraConfigIsShow, cameraSettingIsShow, missionSelected, isTakingIntervalPhoto = false;
    private ImageButton changeCamModeIB, shootModeIB, camSettingIB, swapViewIB, takeOffButton, landingButton;
    private Button takePhotoBtn;
    private ToggleButton takeVideoBtn;
    private LinearLayout camFunctionsLayout, statusBarLayout,
            recordVideoBarLayout, smallFragmentLayout,
            missionSelectionWindowLayout, missionConsoleLayout;
    private TextView altTV, modeTV, connectionTV, powerTV, satlTV, recordTimeTV;
    private ImageView videoDot;
    private MissionFragment currentMissionFragment;

    private DJIFlightController mFlightController;
    private boolean isRecording = false;

    private DJIBaseProduct mProduct = null;
    private DJICamera mCamera = null;
    private CameraMode cameraMode = CameraMode.ShootPhoto;
    private CameraShootPhotoMode shootPhotoMode = CameraShootPhotoMode.Single;

    public void setShootPhotoMode(CameraShootPhotoMode shootPhotoMode) {
        this.shootPhotoMode = shootPhotoMode;
    }

    private AbleToHandleMarkerOnMap googleMapFragment;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private void requestPreconditionHandler() {
        if (!FPVIsSmall) {
            Log.e("FPVActivity", "---------->> requestPreconditionHandler: !FPVIsSmall");
            setFPVFragmentLarge(false);
        }
        googleMapFragment = (AbleToHandleMarkerOnMap) getSupportFragmentManager().findFragmentByTag(GoogleMapsFragment.class.getName());
    }

    @Override
    public void sendCancelSetMarkerRequestToMap() {
        requestPreconditionHandler();
        googleMapFragment.cancelSetMarkerOnMap();
    }

    @Override
    public void sendDropSingleMarkerRequestToMap(LatLng markLocation) {
        requestPreconditionHandler();
        googleMapFragment.dropSingleMarkerOnMap(markLocation);
    }

    @Override
    public void sendDropMultipleMarkersRequestToMap(LinkedList<MyWaypoint> markLocations) {
        requestPreconditionHandler();
        googleMapFragment.dropMultipleMarkersOnMap(markLocations);
    }

    @Override
    public void sendUpdateMultipleMarkerRequestToMap(LinkedList<MyWaypoint> newLocations) {
        requestPreconditionHandler();
        googleMapFragment.updateMultipleMarkerOnMap(newLocations);
    }

    @Override
    public void sendUpdateSingleMakerRequestToMap(LatLng newLocation) {
        requestPreconditionHandler();
        googleMapFragment.updateSingleMakerOnMap(newLocation);
    }

    @Override
    public void sendAddSingleMarkerRequestToMap(ReceiveSingleLocationCallBack receiveLocationCallBack) {
        requestPreconditionHandler();
        googleMapFragment.addSingleMarkerOnMap(receiveLocationCallBack);
    }

    @Override
    public void sendAddMultipleMarkersRequestToMap(ReceiveMultipleLocationsCallBack receiveLocationsCallBack) {
        Log.e("FPVActivity", "---------->> sendAddMultipleMarkersRequestToMap");
        requestPreconditionHandler();
        googleMapFragment.addMultipleMarkersOnMap(receiveLocationsCallBack);
    }

    @Override
    public void sendAlterMarkersRequestToMap(LinkedList<MyWaypoint> markerLocations, ReceiveMultipleLocationsCallBack receiveLocationsCallBack) {
        requestPreconditionHandler();
        googleMapFragment.alterMarkersOnMap(markerLocations, receiveLocationsCallBack);
    }

    @Override
    public void sendCleanMarkersToMap() {
        requestPreconditionHandler();
        googleMapFragment.cleanMarkers();
    }


    public interface AbleToHandleMarkerOnMap {
        void cancelSetMarkerOnMap();

        void dropSingleMarkerOnMap(LatLng markLocation);

        void dropMultipleMarkersOnMap(LinkedList<MyWaypoint> markLocations);

        void updateMultipleMarkerOnMap(LinkedList<MyWaypoint> newLocations);

        void updateSingleMakerOnMap(LatLng newLocation);

        void alterMarkersOnMap(LinkedList<MyWaypoint> markerLocations, ReceiveMultipleLocationsCallBack receiveLocationsCallBack);

        void addSingleMarkerOnMap(ReceiveSingleLocationCallBack receiveLocationCallBack);

        void addMultipleMarkersOnMap(ReceiveMultipleLocationsCallBack receiveLocationsCallBack);

        void cleanMarkers();
    }


    @Override
    public void onMissionTypeSelected(int i) {

        switch (i) {
            case 1:
                missionSelected = true;
                currentMissionFragment = FollowMeMissionFragment.newInstance();
                missionSelectionWindowLayout.setVisibility(View.GONE);
                missionConsoleLayout.setVisibility(View.VISIBLE);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mission_console_window, currentMissionFragment, FollowMeMissionFragment.class.getName())
                        .commit();
                showToast("Follow me Mission selected");
                break;
            case 2:
                missionSelected = true;
                currentMissionFragment = HotPointFragment.newInstance();
                missionSelectionWindowLayout.setVisibility(View.GONE);
                missionConsoleLayout.setVisibility(View.VISIBLE);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mission_console_window, currentMissionFragment, HotPointFragment.class.getName())
                        .commit();
                showToast("Hot point Mission selected");
                break;
            case 3:
                missionSelected = true;
                currentMissionFragment = PanoMissionFragment.newInstance();
                missionSelectionWindowLayout.setVisibility(View.GONE);
                missionConsoleLayout.setVisibility(View.VISIBLE);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mission_console_window, currentMissionFragment, PanoMissionFragment.class.getName())
                        .commit();
                showToast("Panorama Mission selected");
                break;
            case 4:
                requestPreconditionHandler();
                missionSelected = true;
                currentMissionFragment = OwnMissionFragment.newInstance();
                missionSelectionWindowLayout.setVisibility(View.GONE);
                missionConsoleLayout.setVisibility(View.VISIBLE);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mission_console_window, currentMissionFragment, OwnMissionFragment.class.getName())
                        .commit();
                showToast("Your Mission selected");
                break;
            default:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "FPV Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.freeman.flyshare/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onCancelClick() {
        if (currentMissionFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(currentMissionFragment).commit();
            currentMissionFragment = null;
        }
        missionSelected = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                missionSelectionWindowLayout.setVisibility(View.VISIBLE);
                missionConsoleLayout.setVisibility(View.GONE);
            }
        });
    }


    public interface OnReceiverReceiveListener {
        void onReceiverReceive();
    }

    private int i = 0;
    private int TIME = 1000;
    private Handler handlerTimer = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                handlerTimer.postDelayed(this, TIME);
                i++;
                int min = i / 60;
                int min1st = min / 10;
                int min2nd = min % 10;
                int second = i - min * 60;
                int sec1st = second / 10;
                int sec2nd = second % 10;
                recordTimeTV.setText(Integer.toString(min1st) + Integer.toString(min2nd) + ":" + Integer.toString(sec1st) + Integer.toString(sec2nd));
//                viewTimer.setText(Integer.toString(i++));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            OnReceiverReceiveListener fpvFragment = (OnReceiverReceiveListener) getSupportFragmentManager().findFragmentByTag(DJIFPVFragment.class.getName());
            if (fpvFragment != null)
                fpvFragment.onReceiverReceive();
            checkConnectionStatus();
        }
    };

    private boolean checkConnectionStatus() {
        mProduct = FlyShareApplication.getProductInstance();
        boolean isConnected = false;
        if (mProduct != null) {
            if (mProduct.isConnected()) {
                connectionTV.setTextColor(getResources().getColor(R.color.colorGreen));
                connectionTV.setText(mProduct.getModel().toString());
                updateFlightControllerStatus();
//                showMapFragment();
                isConnected = true;
            }
        }

        if (!isConnected) {
            connectionTV.setTextColor(getResources().getColor(R.color.colorAccent));
            connectionTV.setText("Disconnected");
//            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(GoogleMapsFragment.class.getName())).commit();
//            this.finish();
//            System.exit(0);
        }
        return isConnected;
    }

    private void initDroneState() {
        stopRecord();
        setShootPhotoMode(0);
        setShootPhotoMode(0);
        if (mProduct == null || mProduct.getCamera() == null) return;
        mProduct.getCamera().setExposureMode(DJICameraSettingsDef.CameraExposureMode.Program, new DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null)
                    Log.e("FPVActivity", "initDroneState set exposure mode failed: " + djiError.getDescription());
            }
        });

        mProduct.getCamera().setExposureCompensation(DJICameraSettingsDef.CameraExposureCompensation.N_0_0, new DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null)
                    Log.e("FPVActivity", "initDroneState setExposureCompensation failed: " + djiError.getDescription());
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fpv);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.e("FPVActivity", "==================>> onCreate!");

        mProduct = FlyShareApplication.getProductInstance();
        if (mProduct != null) {
            initDroneState();
        }
        /*---------------------------------UI init----------------------------------*/
        initLeftSideButtons();
        initCameraFunctions();
        initStatusBar();
        initRecordVideoBar();

        /*---------------------------------Fragments ----------------------------------*/
        fragmentManager = getSupportFragmentManager();
        showFPVFragment();
        showMapFragment();
        this.missionSelectionWindowLayout = (LinearLayout) findViewById(R.id.mission_window);
        missionSelectionWindowLayout.setVisibility(View.GONE); //TODO for debug only, uncommon this later
        initMissionSelectionFragment();
        this.missionConsoleLayout = (LinearLayout) findViewById(R.id.mission_console_window);
        missionConsoleLayout.setVisibility(View.GONE); //TODO for debug only, uncommon this later


        checkConnectionStatus();
        IntentFilter filter = new IntentFilter();
        filter.addAction(FlyShareApplication.FLAG_CONNECTION_CHANGE);
        if (mReceiver.isInitialStickyBroadcast() || mReceiver.isOrderedBroadcast())
            unregisterReceiver(mReceiver);
        registerReceiver(mReceiver, filter);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void showAlertDialog(final boolean isToTakeOff) {
        String takeoff = "Take Off";
        String land = "Land";
        final String whatToDo;
        if (isToTakeOff) {
            whatToDo = takeoff;
        } else
            whatToDo = land;
        new AlertDialog.Builder(this)
                .setTitle("Are you sure to " + whatToDo + "the drone?")
                .setPositiveButton(whatToDo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mProduct != null && mProduct instanceof DJIAircraft) {
                            if (isToTakeOff) {
                                ((DJIAircraft) mProduct).getFlightController().takeOff(new DJICompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        if (djiError == null) {
                                            showToast("Drone " + whatToDo);
                                        } else {
                                            showToast("Error: " + djiError.getDescription());
                                        }
                                    }
                                });
                            } else {
                                ((DJIAircraft) mProduct).getFlightController().goHome(new DJICompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        if (djiError == null) {
                                            showToast("Drone " + whatToDo);
                                        } else {
                                            showToast("Error: " + djiError.getDescription());
                                        }
                                    }
                                });
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();
    }

    private void initLeftSideButtons() {
        smallFragmentLayout = (LinearLayout) findViewById(R.id.small_window);
        swapViewIB = (ImageButton) findViewById(R.id.swap_button);
        swapViewIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!GoogleMapsFragment.isMakingChange && !GoogleMapsFragment.isAddMultiple && !GoogleMapsFragment.isAddSingle) {
                    Log.e("swapViewIB", "================== >>> OnClickListener");
                    if (FPVIsSmall) {
                        FPVIsSmall = false;
                        setFPVFragmentLarge(true);
                    } else {
                        FPVIsSmall = true;
                        setFPVFragmentLarge(false);
                    }
                }
            }
        });
        takeOffButton = (ImageButton) findViewById(R.id.takeoff_button);
        takeOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(true);
            }
        });
        landingButton = (ImageButton) findViewById(R.id.landing_button);
        landingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(false);
            }
        });
    }

    private void initStatusBar() {
        statusBarLayout = (LinearLayout) findViewById(R.id.statusBar);
        altTV = (TextView) findViewById(R.id.status_Alt);
        modeTV = (TextView) findViewById(R.id.status_flightMode);
        connectionTV = (TextView) findViewById(R.id.status_connection);
        powerTV = (TextView) findViewById(R.id.status_powerLevel);
        satlTV = (TextView) findViewById(R.id.status_satlCount);
    }

    private void initRecordVideoBar() {
        recordVideoBarLayout = (LinearLayout) findViewById(R.id.recordVideoBar);
        recordVideoBarLayout.setVisibility(View.GONE);
        recordTimeTV = (TextView) findViewById(R.id.recordVideoTimer);
        videoDot = (ImageView) findViewById(R.id.recordVideoDot);
        videoDot.setVisibility(View.GONE);
    }

    private void initCameraFunctions() {

        camFunctionsLayout = (LinearLayout) findViewById(R.id.cameraFunctions);
        changeCamModeIB = (ImageButton) findViewById(R.id.camera_mode_imageButton);
        changeCamModeIB.setOnClickListener(this);
        takePhotoBtn = (Button) findViewById(R.id.takePhoto);
        takePhotoBtn.setOnClickListener(this);
        takeVideoBtn = (ToggleButton) findViewById(R.id.recordVideo);
        takeVideoBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    recordAction();
                } else {
                    // The toggle is disabled
                    stopRecord();
                }
            }
        });
        camSettingIB = (ImageButton) findViewById(R.id.cameraConfig);
        camSettingIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Camera Config clicked");
                if (!cameraConfigIsShow && !cameraSettingIsShow)
                    showCameraConfig();
                else
                    hideCameraConfig();
            }
        });

        shootModeIB = (ImageButton) findViewById(R.id.shootingMode);
        shootModeIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cameraConfigIsShow && !cameraSettingIsShow) {
                    showCameraSetting();
                } else
                    hideCameraSetting();
            }
        });
    }


    @Override
    public void onClick(View v) {
        if (mProduct == null) return;

        Log.e("FPVActivity", "===================================> click sth");
        switch (v.getId()) {
            case R.id.camera_mode_imageButton:
                showToast("Camera mode image button clicked!");
                if (isPhotoMode) {
                    setShootPhotoMode(1);
                } else {
                    setShootPhotoMode(0);
                }
                break;
            case R.id.takePhoto:
                Log.e("FPVActivity", "===================================> Take photo");
                captureAction();
                showToast("Take Photo");
                break;
            default:
                break;
        }
    }

    private void initMissionSelectionFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.mission_window, MissionSelectionFragment.newInstance(), MissionSelectionFragment.class.toString())
                .commit();
    }

    private void hideCameraSetting() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CameraSettingFragment.class.getName());
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().hide(fragment).commit();
            cameraSettingIsShow = false;
        }
    }

    private void hideCameraConfig() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CameraConfigFragment.class.getName());
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().hide(fragment).commit();
            cameraConfigIsShow = false;
        }
    }

    private void setFPVFragmentLarge(boolean isLarge) {
        Log.e("FPVActivity", "---------->> setFPVFragmentLarge,start FPVIsSmall: " + Boolean.toString(FPVIsSmall));
        GoogleMapsFragment mapsFragment = (GoogleMapsFragment) getSupportFragmentManager().findFragmentByTag(GoogleMapsFragment.class.getName());
        DJIFPVFragment djifpvFragment = (DJIFPVFragment) getSupportFragmentManager().findFragmentByTag(DJIFPVFragment.class.getName());
        getSupportFragmentManager().beginTransaction().remove(mapsFragment).commit();
        getSupportFragmentManager().beginTransaction().remove(djifpvFragment).commit();
        mapsFragment = GoogleMapsFragment.getGoogleMapsFragment(mapsFragment.singleMarker, mapsFragment.missionPoints);
        djifpvFragment = DJIFPVFragment.getDJIFPVFragment();
        if (isLarge) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_window, djifpvFragment, DJIFPVFragment.class.getName()).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.small_window, mapsFragment, GoogleMapsFragment.class.getName()).commit();
            FPVIsSmall = false;
            Log.e("FPVActivity", "---------->> setFPVFragmentLarge, set to Large");
//            camFunctionsLayout.setVisibility(View.VISIBLE);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.small_window, djifpvFragment, DJIFPVFragment.class.getName()).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.main_window, mapsFragment, GoogleMapsFragment.class.getName()).commit();
            FPVIsSmall = true;
            Log.e("FPVActivity", "---------->> setFPVFragmentLarge, set to small");
//            camFunctionsLayout.setVisibility(View.INVISIBLE);
        }
        Log.e("FPVActivity", "---------->> setFPVFragmentLarge,end FPVIsSmall: " + Boolean.toString(FPVIsSmall));
    }

    private void showMapFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.small_window, GoogleMapsFragment.getGoogleMapsFragment(null, null), GoogleMapsFragment.class.getName())
                .commit();
    }

    private void showFPVFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_window, DJIFPVFragment.getDJIFPVFragment(), DJIFPVFragment.class.getName())
                .commit();
    }

    private void showCameraSetting() {
        if (isRecording || isTakingIntervalPhoto) return;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CameraSettingFragment.class.getName());
        if (fragment == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.cameraConfigFragment, CameraSettingFragment.newInstance(), CameraSettingFragment.class.getName())
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction().show(fragment).commit();
        }
        cameraSettingIsShow = true;
    }

    private void showCameraConfig() {
        if (isRecording || isTakingIntervalPhoto) return;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(CameraConfigFragment.class.getName());
        if (fragment == null) {
            FragmentManager mManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = mManager.beginTransaction();
            CameraConfigFragment cameraConfigFragment = CameraConfigFragment.getCameraConfigFragment();
            fragmentTransaction.add(R.id.cameraConfigFragment, cameraConfigFragment, CameraConfigFragment.class.getName());
            fragmentTransaction.commit();
        } else {
            getSupportFragmentManager().beginTransaction().show(fragment).commit();
        }
        cameraConfigIsShow = true;

    }

    private void updateFlightControllerStatus() {
        if (mProduct != null && mProduct instanceof DJIAircraft) {
            mFlightController = ((DJIAircraft) mProduct).getFlightController();
            if (mFlightController == null) return;
            mFlightController.setUpdateSystemStateCallback(new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {
                @Override
                public void onResult(DJIFlightControllerDataType.DJIFlightControllerCurrentState djiFlightControllerCurrentState) {
                    DJIFlightControllerDataType.DJILocationCoordinate3D threedee = djiFlightControllerCurrentState.getAircraftLocation();
                    float alt = threedee.getAltitude();
                    String mode = djiFlightControllerCurrentState.getFlightModeString();
                    double satl = djiFlightControllerCurrentState.getSatelliteCount();
                    updateFlightControllerStatusUI(alt, mode, satl);
                }
            });

            mProduct.getBattery().setBatteryStateUpdateCallback(new DJIBattery.DJIBatteryStateUpdateCallback() {
                @Override
                public void onResult(DJIBattery.DJIBatteryState djiBatteryState) {
                    final int batteryPercent = djiBatteryState.getBatteryEnergyRemainingPercent();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            powerTV.setText(batteryPercent + "%");
                        }
                    });
                }
            });

        }
    }

    private void updateFlightControllerStatusUI(final float alt, final String mode, final double satl) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                altTV.setText(Float.toString(alt));
                modeTV.setText(mode);
                satlTV.setText(Double.toString(satl));
                if ((mode.equals("F_GPS") || mode.contains("Navi")) && alt > 5) {
                    if (!missionSelected)
                        missionSelectionWindowLayout.setVisibility(View.VISIBLE);
                    missionConsoleLayout.setVisibility(View.VISIBLE);
                } else {
                    missionSelectionWindowLayout.setVisibility(View.GONE); //TODO debug UI
                    if (currentMissionFragment != null) {
                        getSupportFragmentManager().beginTransaction().remove(currentMissionFragment).commit();
                        currentMissionFragment = null;
                        sendCleanMarkersToMap();
                    }
                    missionConsoleLayout.setVisibility(View.GONE);//TODO debug UI
                    missionSelected = false;
                }
            }
        });
    }

    private void setShootPhotoMode(int cameraMode) // 0 for photo, 1 for video, 2 for Playback, 3 for MediaDownload, 4 for Unknown
    {
        mCamera = mProduct.getCamera();
        if (mCamera == null) return;
        if (cameraMode == 0) {
            mCamera.setCameraMode(CameraMode.ShootPhoto, new DJICompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isPhotoMode = true;
                                changeCamModeIB.setImageResource(R.mipmap.ic_switch_left);
                                takePhotoBtn.setVisibility(View.VISIBLE);
                                takeVideoBtn.setVisibility(View.GONE);
                                recordVideoBarLayout.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        showToast(djiError.getDescription());
                    }
                }
            });
        } else if (cameraMode == 1) {
            mCamera = mProduct.getCamera();
            mCamera.setCameraMode(CameraMode.RecordVideo, new DJICompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isPhotoMode = false;
                                changeCamModeIB.setImageResource(R.mipmap.ic_switch_right);
                                takePhotoBtn.setVisibility(View.GONE);
                                takeVideoBtn.setVisibility(View.VISIBLE);
                                recordVideoBarLayout.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        showToast(djiError.getDescription());
                    }
                }
            });
        } else if (cameraMode == 2) {

        } else if (cameraMode == 3) {

        } else {

        }

    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(FPVActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void stopRecord() {
        mCamera = mProduct.getCamera();
        if (mCamera == null) return;
        mCamera.stopRecordVideo(new DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            videoDot.setVisibility(View.GONE);
                            recordTimeTV.setText("00:00");
                            setIsRecording(false);
                        }
                    });
                } else {
                    showToast(djiError.getDescription());
                }

                handlerTimer.removeCallbacks(runnable);
                i = 0;
            }
        });
    }

    private void recordAction() {
        CameraMode cameraMode = CameraMode.RecordVideo;
        mCamera = mProduct.getCamera();
        mCamera.setCameraMode(cameraMode, new DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    mCamera.startRecordVideo(new DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                setIsRecording(true);
                                handlerTimer.postDelayed(runnable, TIME);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        videoDot.setVisibility(View.VISIBLE);
                                    }
                                });
                            } else {
                                showToast(djiError.getDescription());
                            }
                        }
                    });
                } else {
                    showToast(djiError.getDescription());
                }
            }
        });
    }

    private void captureAction() {
        mCamera = mProduct.getCamera();
        if (!isTakingIntervalPhoto)
            mCamera.setCameraMode(cameraMode, new DJICompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) // if set camera mode success
                    {
                        mCamera.startShootPhoto(shootPhotoMode, new DJICompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError == null) {
                                    if (shootPhotoMode == CameraShootPhotoMode.Interval) {
                                        isTakingIntervalPhoto = true;
                                        showToast("Taking interval photos!");
                                        return;
                                    }
                                    showToast("Take photo success");
                                } else
                                    showToast(djiError.getDescription());
                            }
                        });
                    } else // if set camera mode fail
                    {
                        showToast(djiError.getDescription());
                    }
                }
            });
        else
            mCamera.stopShootPhoto(new DJICompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        showToast("Stop taking interval photos!");
                        isTakingIntervalPhoto = false;
                    }
                }
            });

    }

    private Timer mTimer;

    class UpdateFlightControllerTask extends TimerTask {
        @Override
        public void run() {
            updateFlightControllerStatus();
        }
    }


    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        UpdateFlightControllerTask updateFlightControllerTask = new UpdateFlightControllerTask();
        mTimer = new Timer();
//        mTimer.schedule(updateFlightControllerTask, 0, 500);
        super.onResume();
    }

    @Override
    public void onRestart() {
        Log.e("FPVActivity", "==================>> onRestart!");
        IntentFilter filter = new IntentFilter();
        filter.addAction(FlyShareApplication.FLAG_CONNECTION_CHANGE);
        if (mReceiver.isInitialStickyBroadcast() || mReceiver.isOrderedBroadcast())
            unregisterReceiver(mReceiver);
        registerReceiver(mReceiver, filter);
        super.onRestart();
    }

    @Override
    public void onPause() {
        if (mReceiver.isInitialStickyBroadcast() || mReceiver.isOrderedBroadcast())
            unregisterReceiver(mReceiver);
        Log.e("FPVActivity", "==================>> onPause!");
        mTimer.cancel();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "==================>> onStop");
        mTimer.cancel();

        unregisterReceiver(mReceiver);
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "FPV Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.freeman.flyshare/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "==================>> onDestroy");
        if (mReceiver.isOrderedBroadcast() || mReceiver.isInitialStickyBroadcast())
            unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public void setIsRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }
}
