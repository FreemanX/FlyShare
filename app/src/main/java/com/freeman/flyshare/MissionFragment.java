package com.freeman.flyshare;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.FlightController.DJIFlightControllerDataType;
import dji.sdk.MissionManager.DJIFollowMeMission;
import dji.sdk.MissionManager.DJIMission;
import dji.sdk.MissionManager.DJIMissionManager;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIError;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class MissionFragment extends Fragment implements View.OnClickListener, DJIMissionManager.MissionProgressStatusCallback {

    LayoutInflater inflater;
    ViewGroup container;
    Bundle savedInstanceState;

    View mView;
    Button uploadButton, startButton, pauseButton, resumeButton, stopButton, cancelButton;
    TextView progressTitleTextView;
    ProgressBar progressBar;
    LinearLayout configLayout, ongoingLayout;

    OnCancelClickListener mFragmentActivity;

    DJIMission mMission;
    DJIMissionManager mMissionManager;
    DJIAircraft mAircraft;
    DJIFlightController mFlightController;


    double homeLat, homeLng;

    public MissionFragment() {
        // Required empty public constructor
    }

    protected abstract DJIMission initMission();

    protected abstract int getFragmentViewResource();

    protected void showMissionOngoingUI() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ongoingLayout.setVisibility(View.VISIBLE);
            }
        });

    }

    protected void hideMissionOngoingUI() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ongoingLayout.setVisibility(View.GONE);
            }
        });

    }

    protected void hideConfigMissionUI() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                configLayout.setVisibility(View.GONE);
            }
        });

    }

    protected void showConfigMissionUI() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                configLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    protected abstract void initMissionOngoingUIComponents();

    protected abstract void initConfigMissionUIComponents();

    protected abstract void initMissionVariables();

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        this.savedInstanceState = savedInstanceState;
        initMissionVariables();
        mView = inflater.inflate(getFragmentViewResource(), container, false);
        if (mView != null) { // init UI
            initBasicViewComponent(mView);
            configLayout = (LinearLayout) mView.findViewById(R.id.config_mission_layout);
            ongoingLayout = (LinearLayout) mView.findViewById(R.id.ongoing_mission_layout);
            initMissionOngoingUIComponents();
            initConfigMissionUIComponents();
        }

        if (getActivity() instanceof OnCancelClickListener)
            mFragmentActivity = (OnCancelClickListener) getActivity();
        else {
            throw new RuntimeException(getContext().toString() + "OnCancelClickListener must be implemented!");
        }
        initBaseMissionVariables();
        updateHomeLocation();
        return mView;
    }

    protected void initBaseMissionVariables() {
        if (FlyShareApplication.getProductInstance() != null
                && FlyShareApplication.getProductInstance() instanceof DJIAircraft
                && FlyShareApplication.getProductInstance().isConnected()) {
            mAircraft = (DJIAircraft) FlyShareApplication.getProductInstance();
            mFlightController = mAircraft.getFlightController();
            mMissionManager = mAircraft.getMissionManager();
            if (mMissionManager != null)
                mMissionManager.setMissionProgressStatusCallback(this);
            updateHomeLocation();
        }

    }

    protected void updateProgressBar(final String progressTitle, final float progress) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressTitleTextView.getVisibility() != View.VISIBLE)
                    progressTitleTextView.setVisibility(View.VISIBLE);
                if (progressBar.getVisibility() != View.VISIBLE)
                    progressBar.setVisibility(View.VISIBLE);
                progressTitleTextView.setText(progressTitle);
                progressBar.setProgress(Math.round(progress));
            }
        });
    }

    protected void hideProgressBar() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                progressTitleTextView.setVisibility(View.INVISIBLE);
            }
        });

    }

    protected void initBasicViewComponent(View view) {
        progressTitleTextView = (TextView) view.findViewById(R.id.progress_bar_textView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        hideProgressBar();
        uploadButton = (Button) view.findViewById(R.id.upload_mission_button);
        startButton = (Button) view.findViewById(R.id.start_mission_button);
        pauseButton = (Button) view.findViewById(R.id.pause_mission_button);
        resumeButton = (Button) view.findViewById(R.id.resume_mission_button);
        stopButton = (Button) view.findViewById(R.id.stop_mission_button);
        cancelButton = (Button) view.findViewById(R.id.cancel_mission_button);
        uploadButton.setOnClickListener(this);
        startButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        resumeButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        uploadButton.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
        resumeButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.GONE);
    }

    protected void updateHomeLocation() {
        if (!(FlyShareApplication.getProductInstance() instanceof DJIAircraft)) return;
        mAircraft = (DJIAircraft) FlyShareApplication.getProductInstance();
        mFlightController = mAircraft.getFlightController();
        if (mFlightController == null) return;
        final CountDownLatch cdl = new CountDownLatch(1);
        mFlightController.getHomeLocation(new DJIBaseComponent.DJICompletionCallbackWith<DJIFlightControllerDataType.DJILocationCoordinate2D>() {
            @Override
            public void onSuccess(DJIFlightControllerDataType.DJILocationCoordinate2D djiLocationCoordinate2D) {
                homeLat = djiLocationCoordinate2D.getLatitude();
                homeLng = djiLocationCoordinate2D.getLongitude();
            }

            @Override
            public void onFailure(DJIError djiError) {
                cdl.countDown();
                Log.e("DJIMission", "Failed in getting home location: " + djiError.getDescription());
            }
        });

        try {
            cdl.await(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!Utils.checkGpsCoordinate(homeLat, homeLng)) {
            Utils.setResultToToast(getContext(), "Cannot start Mission due to invalid home location");
            finishMission();
        }

    }

    protected void startUpdateFollowedObjectLocation() {
        if (!(mMission instanceof DJIFollowMeMission)) return;
        // TODO Follow me mission implement here
    }

    protected void stopUpdateFollowedObjectLocation() {
        if (!(mMission instanceof DJIFollowMeMission)) return;
        // TODO Follow me mission implement here
    }

    protected void setPauseUpdateFollowedObjectLocation(boolean isPaused) {
        if (!(mMission instanceof DJIFollowMeMission)) return;
        // TODO Follow me mission implement here
    }

    protected void finishMission() {
        mFragmentActivity.onCancelClick();
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onClick(View v) {
        mMissionManager = DJIMissionManager.getInstance();
        if (FlyShareApplication.getProductInstance() instanceof DJIAircraft &&
                !Utils.checkGpsCoordinate(homeLat, homeLng) &&
                mFlightController != null) {
            updateHomeLocation();
        }

        switch (v.getId()) {
            case R.id.upload_mission_button:
                onUploadClicked();
                mMission = initMission();
                if (mMission == null)
                    Utils.setResultToToast(getContext(), "Fail to setup mission");
                else {
                    mMissionManager.prepareMission(mMission, new DJIMission.DJIMissionProgressHandler() {
                        @Override
                        public void onProgress(DJIMission.DJIProgressType djiProgressType, float progress) {
                            if (progress >= 0 && progress <= 100)
                                updateProgressBar(djiProgressType.name(), progress);
                            else
                                hideProgressBar();
                        }
                    }, new DJIBaseComponent.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                onUploadClickOperationSuccess();
                                Utils.setResultToToast(getContext(), "Success!");
                            } else {
                                Utils.setResultToToast(getContext(), "Prepare: " + djiError.getDescription());
                            }
                        }
                    });
                }
                break;
            case R.id.start_mission_button:
                onStartClicked();
                if (mMission != null) {
                    mMissionManager.setMissionExecutionFinishedCallback(new DJIBaseComponent.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null)
                                Log.e("onClickStartBtn", "set mission execution finished callback failed: " + djiError.getDescription());
                            else
                                onStopClickOperationSuccess();
                        }
                    });
                    mMissionManager.startMissionExecution(new DJIBaseComponent.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                onStartClickOperationSuccess();
                                Utils.setResultToToast(getContext(), "Mission started");
                                if (mMission instanceof DJIFollowMeMission) {
                                    startUpdateFollowedObjectLocation();
                                }
                            } else {
                                Utils.setResultToToast(getContext(), "Mission failed: " + djiError.getDescription());
                                Log.e("onClickStartBtn", "start mission execution callback failed: " + djiError.getDescription());
                            }
                        }
                    });
                }

                break;
            case R.id.pause_mission_button:
                onPauseClicked();
                mMissionManager.pauseMissionExecution(new DJIBaseComponent.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError mError) {
                        if (mError == null) {
                            onPauseClickOperationSuccess();
                            Utils.setResultToToast(getContext(), "Mission paused!");
                            if (mMission instanceof DJIFollowMeMission) {
                                setPauseUpdateFollowedObjectLocation(true);
                            }
                        } else {
                            Utils.setResultToToast(getContext(), "Pause mission failed");
                            Log.e("PauseMissionClicked", "Failed: " + mError.getDescription());
                        }
                    }
                });
                break;
            case R.id.resume_mission_button:
                onResumeClicked();
                mMissionManager.resumeMissionExecution(new DJIBaseComponent.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError mError) {
                        if (mError == null) {
                            onResumeClickOperationSuccess();
                            Utils.setResultToToast(getContext(), "Resume paused!");
                            if (mMission instanceof DJIFollowMeMission) {
                                setPauseUpdateFollowedObjectLocation(false);
                            }
                        } else {
                            Utils.setResultToToast(getContext(), "Resume mission failed");
                            Log.e("ResumeMissionClicked", "Failed: " + mError.getDescription());
                        }
                    }
                });

                break;
            case R.id.stop_mission_button:
                onStopClicked();
                mMissionManager.stopMissionExecution(new DJIBaseComponent.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError mError) {
                        if (mError == null) {
                            onStopClickOperationSuccess();
                            Utils.setResultToToast(getContext(), "Mission stopped!");
                            if (mMission instanceof DJIFollowMeMission) {
                                stopUpdateFollowedObjectLocation();
                            }
                        } else {
                            Utils.setResultToToast(getContext(), "Stop mission failed");
                            Log.e("StopMissionClicked", "Failed: " + mError.getDescription());
                        }
                    }
                });
                break;
            case R.id.cancel_mission_button:
                finishMission();
                break;
            default:
                break;
        }
    }

    protected void onUploadClickOperationSuccess() {
        hideConfigMissionUI();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uploadButton.setVisibility(View.GONE);
                startButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.GONE);
                stopButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
            }
        });

    }

    protected void onStartClickOperationSuccess() {
        showMissionOngoingUI();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uploadButton.setVisibility(View.GONE);
                startButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                resumeButton.setVisibility(View.GONE);
                stopButton.setVisibility(View.VISIBLE);
            }
        });

    }

    protected void onPauseClickOperationSuccess() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uploadButton.setVisibility(View.GONE);
                startButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.VISIBLE);
            }
        });

    }

    protected void onResumeClickOperationSuccess() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uploadButton.setVisibility(View.GONE);
                startButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                resumeButton.setVisibility(View.GONE);
                stopButton.setVisibility(View.VISIBLE);
            }
        });

    }

    protected void onStopClickOperationSuccess() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uploadButton.setVisibility(View.VISIBLE);
                startButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.GONE);
                stopButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.VISIBLE);
            }
        });
        showConfigMissionUI();
        hideMissionOngoingUI();
        hideProgressBar();

    }


    protected void onUploadClicked() {
        Utils.setResultToToast(getContext(), "onUploadClicked");
        onUploadClickOperationSuccess(); //TODO for debug only, delete this later
    }

    protected void onStartClicked() {
        Utils.setResultToToast(getContext(), "onStartClicked");
        onStartClickOperationSuccess();//TODO for debug only, delete this later
    }

    protected void onPauseClicked() {
        Utils.setResultToToast(getContext(), "onPauseClicked");
        onPauseClickOperationSuccess();//TODO for debug only, delete this later
    }

    protected void onResumeClicked() {
        Utils.setResultToToast(getContext(), "onResumeClicked");
        onResumeClickOperationSuccess();//TODO for debug only, delete this later
    }

    protected void onStopClicked() {
        Utils.setResultToToast(getContext(), "onStopClicked");
        onStopClickOperationSuccess(); //TODO for debug only, delete this later
    }

    public interface OnCancelClickListener {
        void onCancelClick();
    }
}
