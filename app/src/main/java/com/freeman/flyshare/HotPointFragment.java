package com.freeman.flyshare;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import dji.sdk.FlightController.DJIFlightControllerDataType;
import dji.sdk.MissionManager.DJIHotPointMission;
import dji.sdk.MissionManager.DJIMission;
import dji.sdk.MissionManager.DJIMissionManager;

public class HotPointFragment extends MissionFragment {

    private RadioGroup selectHotPointRadioGroup, hotpointDirectionRadioGroup;
    private RadioButton currentLocationRadioButton, setOnMapRadioButton, clockwiseRadioButton, anticlockwiseRadioButton;
    private Button setOnMapButton;
    private Spinner selectHeadingSpinner;
    private TextView altitudeTextView, radiusTextView, angularSpeedTextView;
    private SeekBar altitudeSeekBar, radiusSeekBar, angularSpeedSeekBar;

    private double mAltitude, mLat, mLng, mRadius;
    private int mAngularVelocity;
    private DJIHotPointMission.DJIHotPointHeading mHeading;
    private boolean mIsClockwise;
    private final DJIHotPointMission.DJIHotPointStartPoint mStartPoint = DJIHotPointMission.DJIHotPointStartPoint.Nearest;

    public HotPointFragment() {
        // Required empty public constructor
    }

    @Override
    protected DJIMission initMission() {
        updateHomeLocation();
        if (!Utils.checkGpsCoordinate(mLat, mLng) || !Utils.checkGpsCoordinate(homeLat, homeLng)) {
            Utils.setResultToToast(getContext(), "Invalid location");
            return null;
        }
        DJIHotPointMission hotPointMission = new DJIHotPointMission(mLat, mLng);
        hotPointMission.altitude = mAltitude;
        hotPointMission.radius = mRadius;
        hotPointMission.angularVelocity = mAngularVelocity;
        hotPointMission.isClockwise = mIsClockwise;
        hotPointMission.startPoint = mStartPoint;
        hotPointMission.heading = mHeading;
        return hotPointMission;
    }

    @Override
    protected int getFragmentViewResource() {
        return R.layout.fragment_hot_point;
    }

    @Override
    protected void initMissionOngoingUIComponents() {

    }

    @Override
    protected void initConfigMissionUIComponents() {
        initSetHotPoint();
        initSetDirectionUI();
        initSetAltitudeUI();
        initSetRadiusUI();
        initSetAngularSpeedUI();
        initSelectHeadingUI();
    }

    private void initSetHotPoint() {
        selectHotPointRadioGroup = (RadioGroup) mView.findViewById(R.id.hot_point_select_radio_group);
        currentLocationRadioButton = (RadioButton) mView.findViewById(R.id.current_location_radioButton);
        setOnMapRadioButton = (RadioButton) mView.findViewById(R.id.set_on_map_radioButton);
        setOnMapButton = (Button) mView.findViewById(R.id.drop_marker_button);
        setOnMapButton.setVisibility(View.GONE);
        if (mFlightController != null) {
            DJIFlightControllerDataType.DJIFlightControllerCurrentState currentState = mFlightController.getCurrentState();
            updateHotPoint(currentState.getAircraftLocation().getLatitude(), currentState.getAircraftLocation().getLongitude());
            currentLocationRadioButton.setChecked(true);
        }
        selectHotPointRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.current_location_radioButton) {
                    if (mFlightController != null) {
                        DJIFlightControllerDataType.DJIFlightControllerCurrentState currentState = mFlightController.getCurrentState();
                        updateHotPoint(currentState.getAircraftLocation().getLatitude(), currentState.getAircraftLocation().getLongitude());
                    } else
                        Utils.setResultToToast(getContext(), "FlightController is null");
                } else if (checkedId == R.id.set_on_map_radioButton) {
                    missionRequestMapHandler.sendDropSingleMarkerRequestToMap(new LatLng(mLat, mLng));
                    missionRequestMapHandler.sendAddSingleMarkerRequestToMap(new ReceiveSingleLocationCallBack() {
                        @Override
                        public void onLocationReceive(boolean isSuccessful, LatLng location) {
                            getActivity().getSupportFragmentManager().beginTransaction().show(getThis()).commit();
                            if (isSuccessful) {
                                updateHotPoint(location.latitude, location.longitude);
                            } else {
                                Utils.setResultToToast(getContext(), "Add hop point on map failed!");
                            }
                        }
                    });
                    getActivity().getSupportFragmentManager().beginTransaction().hide(getThis()).commit();
                }
            }
        });

        setOnMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                missionRequestMapHandler.sendAddSingleMarkerRequestToMap(new ReceiveSingleLocationCallBack() {
                    @Override
                    public void onLocationReceive(boolean isSuccessful, LatLng location) {
                        if (isSuccessful) {
                            updateHotPoint(location.latitude, location.longitude);
                        } else {
                            Utils.setResultToToast(getContext(), "Add hop point on map failed!");
                        }
                    }
                });
            }
        });

    }

    protected HotPointFragment getThis() {
        return this;
    }

    private void updateHotPoint(double lat, double lng) {
        if (!DJIMissionManager.checkValidGPSCoordinate(lat, lng)) {
            Utils.setResultToToast(getContext(), "Invalid location(" + Double.toString(lat) + "," + Double.toString(lng) + ")");
            return;
        }
        this.mLat = lat;
        this.mLng = lng;
        missionRequestMapHandler.sendUpdateSingleMakerRequestToMap(new LatLng(mLat, mLng));
    }

    private void initSetDirectionUI() {
        hotpointDirectionRadioGroup = (RadioGroup) mView.findViewById(R.id.hot_point_direction_radio_group);
        clockwiseRadioButton = (RadioButton) mView.findViewById(R.id.clockwise_radioButton);
        anticlockwiseRadioButton = (RadioButton) mView.findViewById(R.id.anticlockwise_radioButton);
        clockwiseRadioButton.setChecked(true);
        hotpointDirectionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.clockwise_radioButton) {
                    mIsClockwise = true;
                } else if (checkedId == R.id.anticlockwise_radioButton) {
                    mIsClockwise = false;
                }
            }
        });
    }

    private void initSetAltitudeUI() {
        altitudeTextView = (TextView) mView.findViewById(R.id.altitude_textView);
        altitudeSeekBar = (SeekBar) mView.findViewById(R.id.altitude_seekBar);
        altitudeSeekBar.setMax(115);
        altitudeSeekBar.setProgress((int) Math.round(mAltitude));
        altitudeTextView.setText(Double.toString(mAltitude) + " m");
        altitudeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAltitude = progress + 5;
                altitudeTextView.setText(Double.toString(mAltitude) + " m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initSetRadiusUI() {
        radiusTextView = (TextView) mView.findViewById(R.id.radius_textView);
        radiusSeekBar = (SeekBar) mView.findViewById(R.id.radius_seekBar);
        radiusSeekBar.setMax(95);
        radiusSeekBar.setProgress((int) Math.round(mRadius - 5));
        radiusTextView.setText(Double.toString(mRadius) + " m");
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRadius = progress + 5;
                radiusTextView.setText(Double.toString(mRadius) + " m");
                int maxSpeed = (int) Math.round(DJIHotPointMission.maxAngularVelocityForRadius(mRadius));
                if (angularSpeedSeekBar.getProgress() > maxSpeed) {
                    angularSpeedSeekBar.setProgress(maxSpeed);
                }
                angularSpeedSeekBar.setMax(maxSpeed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initSetAngularSpeedUI() {
        angularSpeedTextView = (TextView) mView.findViewById(R.id.angular_speed_textView);
        angularSpeedSeekBar = (SeekBar) mView.findViewById(R.id.angular_speed_seekBar);

        angularSpeedSeekBar.setMax((int) Math.round(DJIHotPointMission.maxAngularVelocityForRadius(mRadius)));
        angularSpeedSeekBar.setProgress(mAngularVelocity);
        angularSpeedTextView.setText(Integer.toString(mAngularVelocity) + " degrees/s");
        angularSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAngularVelocity = progress;
                angularSpeedTextView.setText(Integer.toString(mAngularVelocity) + " degrees/s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initSelectHeadingUI() {
        selectHeadingSpinner = (Spinner) mView.findViewById(R.id.select_heading_spinner);
        ArrayAdapter headingItemsArrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.hotpoint_heading, R.layout.single_line_layout);
        selectHeadingSpinner.setAdapter(headingItemsArrayAdapter);
        selectHeadingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mHeading = DJIHotPointMission.DJIHotPointHeading.TowardsHotPoint;
                        break;
                    case 1:
                        mHeading = DJIHotPointMission.DJIHotPointHeading.ControlledByRemoteController;
                        break;
                    case 2:
                        mHeading = DJIHotPointMission.DJIHotPointHeading.UsingInitialHeading;
                        break;
                    case 3:
                        mHeading = DJIHotPointMission.DJIHotPointHeading.AwayFromHotPoint;
                        break;
                    case 4:
                        mHeading = DJIHotPointMission.DJIHotPointHeading.AlongCircleLookingForwards;
                        break;
                    case 5:
                        mHeading = DJIHotPointMission.DJIHotPointHeading.AlongCircleLookingBackwards;
                        break;
                    default:
                        mHeading = DJIHotPointMission.DJIHotPointHeading.TowardsHotPoint;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void initMissionVariables() {
        updateHotPoint(homeLat, homeLng);
        mAltitude = 20;
        mRadius = 10;
        mAngularVelocity = 20;
        mIsClockwise = true;
        mHeading = DJIHotPointMission.DJIHotPointHeading.TowardsHotPoint;
    }

    public static HotPointFragment newInstance() {
        HotPointFragment fragment = new HotPointFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    protected void onUploadClickOperationSuccess() {

        missionRequestMapHandler.sendDropSingleMarkerRequestToMap(new LatLng(mLat, mLng));
        super.onUploadClickOperationSuccess();
    }


    @Override
    protected void onStopClickOperationSuccess() {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentLocationRadioButton.setChecked(true);
            }
        });

        super.onStopClickOperationSuccess();
    }

    @Override
    protected void finishMission() {
        missionRequestMapHandler.sendCleanMarkersToMap();
        super.finishMission();
    }

    @Override
    public void onStop() {
        missionRequestMapHandler.sendCleanMarkersToMap();
        super.onStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void missionProgressStatus(DJIMission.DJIMissionProgressStatus djiMissionProgressStatus) {

    }


}
