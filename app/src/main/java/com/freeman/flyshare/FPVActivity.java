package com.freeman.flyshare;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import dji.sdk.AirLink.DJILBAirLink.DJIOnReceivedVideoCallback;
import dji.sdk.Battery.DJIBattery;
import dji.sdk.Camera.DJICamera;
import dji.sdk.Camera.DJICamera.CameraReceivedVideoDataCallback;
import dji.sdk.Codec.DJICodecManager;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.FlightController.DJIFlightControllerDataType;
import dji.sdk.FlightController.DJIFlightControllerDelegate;
import dji.sdk.base.DJIBaseComponent.DJICompletionCallback;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIBaseProduct.Model;
import dji.sdk.base.DJIError;
import dji.sdk.Camera.DJICameraSettingsDef.CameraMode;
import dji.sdk.Camera.DJICameraSettingsDef.CameraShootPhotoMode;

public class FPVActivity extends AppCompatActivity implements View.OnClickListener, TextureView.SurfaceTextureListener {
    String TAG = "FPVActivity";
    boolean isPhotoMode = true;
    boolean cameraConfigIsShow = false;
    ImageButton changeCamModeIB, shootModeIB, camSettingIB;
    Button takePhotoBtn;
    ToggleButton takeVideoBtn;
    LinearLayout camFunctionsLayout, statusBarLayout, recordVideoBarLayout;
    TextView altTV, modeTV, connectionTV, powerTV, satlTV, recordTimeTV;
    ImageView videoDot;

    private DJIFlightController mFlightController;
    protected CameraReceivedVideoDataCallback mReceivedVideoDataCallBack = null;
    protected DJIOnReceivedVideoCallback mOnReceivedVideoCallback = null;

    private DJIBaseProduct mProduct = null;
    private DJICamera mCamera = null;

    protected DJICodecManager mCodecManager = null;
    protected TextureView mVideoSurface = null;

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

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
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
                isConnected = true;
            }
        }

        if (!isConnected) {
            connectionTV.setTextColor(getResources().getColor(R.color.colorAccent));
            connectionTV.setText("Disconnected");
        }

        return isConnected;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fpv);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mVideoSurface = (TextureView) findViewById(R.id.FPV_textureView);
        if (mVideoSurface != null)
            mVideoSurface.setSurfaceTextureListener(this);
        mProduct = FlyShareApplication.getProductInstance();
        if (mProduct != null) {
            stopRecord();
            setCameraMode(0);
            setCameraMode(0);
        }
        /*---------------------------------UI init----------------------------------*/
        initCameraFunctions();
        initStatusBar();
        initRecordVideoBar();

        mReceivedVideoDataCallBack = new CameraReceivedVideoDataCallback() {
            @Override
            public void onResult(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                } else
                    Log.e(TAG, "mCodecManager is null...");
            }
        };

        mOnReceivedVideoCallback = new DJIOnReceivedVideoCallback() {
            @Override
            public void onResult(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };
        checkConnectionStatus();

        IntentFilter filter = new IntentFilter();
        filter.addAction(FlyShareApplication.FLAG_CONNECTION_CHANGE);
        if (mReceiver.isInitialStickyBroadcast() || mReceiver.isOrderedBroadcast())
            unregisterReceiver(mReceiver);
        registerReceiver(mReceiver, filter);
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
                if (!cameraConfigIsShow)
                    showCameraConfig();
                else
                    hideCameraConfig();
            }
        });

        shootModeIB = (ImageButton) findViewById(R.id.shootingMode);
        shootModeIB.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mProduct == null) return;

        Log.e("FPVActivity", "===================================> click sth");
        switch (v.getId()) {
            case R.id.camera_mode_imageButton:
                showToast("Camera mode image button clicked!");
                if (isPhotoMode) {
                    setCameraMode(1);
                } else {
                    setCameraMode(0);
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

    private void hideCameraConfig() {
        Fragment fragment = getFragmentManager().findFragmentByTag("CameraConfig");
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
            cameraConfigIsShow = false;
        }
    }

    private void showCameraConfig() {
        FragmentManager mManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        CameraConfigFragment cameraConfigFragment = CameraConfigFragment.getCameraConfigFragment();
        fragmentTransaction.add(R.id.cameraConfigFragment, cameraConfigFragment, "CameraConfig");
        fragmentTransaction.commit();
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
                    final float alt = threedee.getAltitude();
                    final String mode = djiFlightControllerCurrentState.getFlightModeString();
                    final double satl = djiFlightControllerCurrentState.getSatelliteCount();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            altTV.setText(Float.toString(alt));
                            modeTV.setText(mode);
                            satlTV.setText(Double.toString(satl));
                        }
                    });
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

    private void setCameraMode(int cameraMode) // 0 for photo, 1 for video, 2 for Playback, 3 for MediaDownload, 4 for Unknown
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

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            Log.e(TAG, "mCodecManager is null 2");
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG, "onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        Log.e(TAG, "onSurfaceTextureUpdated");
    }

    private void initPreviewer() {
        try {
            mProduct = FlyShareApplication.getProductInstance();
        } catch (Exception exception) {
            mProduct = null;
        }

        if (null == mProduct || !mProduct.isConnected()) {
            mCamera = null;
            showToast(getString(R.string.disconnected));
        } else {


            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }

            if (!mProduct.getModel().equals(Model.UnknownAircraft)) {
                mCamera = mProduct.getCamera();
                if (mCamera != null) {
                    // Set the callback
                    mCamera.setDJICameraReceivedVideoDataCallback(mReceivedVideoDataCallBack);

                }
            } else {
                if (null != mProduct.getAirLink()) {
                    if (null != mProduct.getAirLink().getLBAirLink()) {
                        // Set the callback
                        mProduct.getAirLink().getLBAirLink().setDJIOnReceivedVideoCallback(mOnReceivedVideoCallback);
                    }
                }
            }
        }
    }

    private void uninitPreviewer() {
        try {
            mProduct = FlyShareApplication.getProductInstance();
        } catch (Exception exception) {
            mProduct = null;
        }

        if (null == mProduct || !mProduct.isConnected()) {
            mCamera = null;
            showToast(getString(R.string.disconnected));
        } else {
            if (!mProduct.getModel().equals(Model.UnknownAircraft)) {
                mCamera = mProduct.getCamera();
                if (mCamera != null) {
                    // Set the callback
                    mCamera.setDJICameraReceivedVideoDataCallback(null);

                }
            } else {
                if (null != mProduct.getAirLink()) {
                    if (null != mProduct.getAirLink().getLBAirLink()) {
                        // Set the callback
                        mProduct.getAirLink().getLBAirLink().setDJIOnReceivedVideoCallback(null);
                    }
                }
            }
        }
    }

    private void stopRecord() {
        mCamera = mProduct.getCamera();
        if (mCamera == null) return;
        mCamera.stopRecordVideo(new DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                    showToast("Video recording stopped");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            videoDot.setVisibility(View.GONE);
                            recordTimeTV.setText("00:00");
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
                                showToast("Start recording success!");
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
        CameraMode cameraMode = CameraMode.ShootPhoto;
        mCamera = mProduct.getCamera();
        mCamera.setCameraMode(cameraMode, new DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) // if set camera mode success
                {
                    CameraShootPhotoMode shootPhotoMode = CameraShootPhotoMode.Single;
                    mCamera.startShootPhoto(shootPhotoMode, new DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null)
                                showToast("Take photo success");
                            else
                                showToast(djiError.getDescription());
                        }
                    });
                } else // if set camera mode fail
                {
                    showToast(djiError.getDescription());
                }
            }
        });
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        updateFlightControllerStatus();
        if (mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        uninitPreviewer();

        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        unregisterReceiver(mReceiver);
        super.onStop();
    }

    public void onReturn(View view) {
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        uninitPreviewer();
        if (mReceiver.isOrderedBroadcast() || mReceiver.isInitialStickyBroadcast())
            unregisterReceiver(mReceiver);

        super.onDestroy();
    }

}
