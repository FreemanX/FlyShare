package com.freeman.flyshare;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.FlightController.DJIFlightControllerDataType;
import dji.sdk.MissionManager.DJIWaypoint;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.base.DJIBaseProduct;


public class GoogleMapsFragment extends Fragment implements GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback,
        FPVActivity.AbleToHandleMarkerOnMap {

    private Timer mTimer;
    private TimerTask mTask;
    MapView mMapView;
    private GoogleMap mMap;
    private AppCompatActivity fragmentActivity;
    double currentLat, currentLng, homeLat, homeLng, droneHeading;
    private Marker droneMarker, homeMarker;
    private MarkerOptions droneMarkerOptions, homeMarkerOptions;
    private Polyline home2Drone = null;
    private LinearLayout markerControlLinearLayout;
    private ToggleButton addSingleMarkerToggleButton, addMarkersToggleButton;
    private Button doneAddButton, clearAllMarkerButton;
    public static boolean isAddSingle, isAddMultiple, isMakingChange = false;
    private ReceiveSingleLocationCallBack receiveSingleLocationCallBack;
    private LatLng resultSingleLocation;
    Marker singleMarker;

    // UI for configure mission point:
    private LinearLayout waypointConfigLayout, actionEditorLayout;
    private TextView pointInfoTextView, altitudeTextView, gimbalPitchTextView, headingTextView;
    private Button pointDeletePointButton, pointAddActionButton, pointSaveButton, pointCancelButton;
    private SeekBar altitudeSeekBar, gimbalPitchSeekBar, headingSeekBar;
    private CheckBox hasActionCheckBox;
    MyWaypoint tmpEditingPoint;
    private int currentEditingWaypointIndex;
    //UI for mission point actions
    private int actionNum = 5;
    private LinearLayout[] actionConfigLinearLayout = new LinearLayout[actionNum];
    private Spinner[] actionSpinner = new Spinner[actionNum];
    private EditText[] actionParamEditText = new EditText[actionNum];
    private Button[] actionDeleteButton = new Button[actionNum];

    //Parameters for waypoint mission
    LinkedList<Marker> multipleMarkers;
    LinkedList<MyWaypoint> missionPoints = null; //use for recovery
    private Polyline missionPath = null;

    private ReceiveMultipleLocationsCallBack receiveMultipleLocationsCallBack;

    DJIBaseProduct mProduct;

    public GoogleMapsFragment() {
        // Required empty public constructor
    }

    void setSingleMarker(Marker savedMarker) {
        this.singleMarker = savedMarker;
    }

    public static GoogleMapsFragment getGoogleMapsFragment(@Nullable Marker savedMarker, @Nullable LinkedList<MyWaypoint> savedMissionPoints) {
        GoogleMapsFragment fragment = new GoogleMapsFragment();
        if (savedMarker != null)
            fragment.setSingleMarker(savedMarker);
        if (savedMissionPoints != null)
            fragment.missionPoints = savedMissionPoints;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_google_maps, container, false);
        fragmentActivity = (AppCompatActivity) getActivity();
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        droneMarkerOptions = new MarkerOptions();
        droneMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_drone));
        droneMarkerOptions.anchor(0.5f, 0.5f).rotation(-90f).flat(true);

        homeMarkerOptions = new MarkerOptions();
        homeMarkerOptions.anchor(0.5f, 0.7f);
        homeMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_home));

        initMarkerControlLayout(view);
        initMissionPointConfigLayout(view);
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);
        updateDroneInfo();
        return view;
    }

    private void clearAllMarkers() {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.clear();
                multipleMarkers = new LinkedList<>();
                missionPath = null;
                missionPoints = new LinkedList<>();
            }
        });
    }

    private void initMissionPointConfigLayout(View v) {
        waypointConfigLayout = (LinearLayout) v.findViewById(R.id.waypoint_config_layout);
        waypointConfigLayout.setVisibility(View.GONE); //TODO debug: uncomment this when not debug
        pointInfoTextView = (TextView) v.findViewById(R.id.point_info_textView);

        pointDeletePointButton = (Button) v.findViewById(R.id.delete_point_button);
        pointDeletePointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                waypointConfigLayout.setVisibility(View.GONE);
                markerControlLinearLayout.setVisibility(View.VISIBLE);
                if (missionPoints.get(currentEditingWaypointIndex) != null) {
                    multipleMarkers.remove(currentEditingWaypointIndex).remove();
                    missionPoints.remove(currentEditingWaypointIndex);
                    LinkedList<LatLng> pointsLocation = new LinkedList<>();
                    for (int i = 0; i < missionPoints.size(); i++) {
                        missionPoints.get(i).setId(i);
                        multipleMarkers.get(i).setTitle("Point " + Integer.toString(i));
                        pointsLocation.add(missionPoints.get(i).getLocation());
                    }
                    missionPath.setPoints(pointsLocation);
                }
                initPointSettings();
            }
        });
        pointSaveButton = (Button) v.findViewById(R.id.save_setting_button);
        pointSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tmpEditingPoint.isHasAction()) {
                    for (int i = 0; i < tmpEditingPoint.getActionLinkedList().size(); i++) {
                        String paramStr = actionParamEditText[i].getText().toString();
                        int param = 0;
                        if (paramStr.length() > 0) {
                            param = Integer.parseInt(paramStr);
                        }
                        tmpEditingPoint.setActionParams(i, param);
                    }
                }
                missionPoints.set(currentEditingWaypointIndex, tmpEditingPoint);
                initPointSettings();
            }
        });

        pointCancelButton = (Button) v.findViewById(R.id.cancel_setting_button);
        pointCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPointSettings();
            }
        });
        initWaypointAltitudeUI(v);
        initWaypointGimbalPitchUI(v);
        initWaypointHeadingUI(v);
        initWaypointActionUI(v);
    }

    private void initPointSettings() {
        for (int i = 0; i < actionNum; i++) {
            actionSpinner[i].setSelection(0);
            actionParamEditText[i].setText("");
            if (i != 0)
                actionConfigLinearLayout[i].setVisibility(View.GONE);
        }
        waypointConfigLayout.setVisibility(View.GONE);
        markerControlLinearLayout.setVisibility(View.VISIBLE);
    }

    private void initWaypointActionUI(View view) {
        actionEditorLayout = (LinearLayout) view.findViewById(R.id.action_editor_layout);
        actionEditorLayout.setVisibility(View.INVISIBLE);
        hasActionCheckBox = (CheckBox) view.findViewById(R.id.has_action_checkBox);
        hasActionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tmpEditingPoint.setHasAction(isChecked);
                if (isChecked) {
                    pointAddActionButton.setVisibility(View.VISIBLE);
                    actionEditorLayout.setVisibility(View.VISIBLE);
                    tmpEditingPoint.removeAllActions();
                    tmpEditingPoint.addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.GimbalPitch, 0));
                } else {
                    pointAddActionButton.setVisibility(View.INVISIBLE);
                    actionEditorLayout.setVisibility(View.GONE);
                    for (int i = 0; i < 5; i++) {
                        actionParamEditText[i].setText("");
                        actionSpinner[i].setSelection(0);
                        if (i != 0)
                            actionConfigLinearLayout[i].setVisibility(View.GONE);
                    }
                }
            }
        });

        pointAddActionButton = (Button) view.findViewById(R.id.add_action_button);
        pointAddActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tmpEditingPoint.getActionLinkedList().size() < 5) {
                    if (tmpEditingPoint.getActionLinkedList().size() > 0) {
                        actionDeleteButton[tmpEditingPoint.getActionLinkedList().size() - 1].setVisibility(View.INVISIBLE);
                        actionConfigLinearLayout[tmpEditingPoint.getActionLinkedList().size()].setVisibility(View.VISIBLE);
                        actionSpinner[tmpEditingPoint.getActionLinkedList().size()].setSelection(0);
                    } else {
                        actionSpinner[0].setSelection(0);
                    }
                    tmpEditingPoint.addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.GimbalPitch, 0));
                }
            }
        });

        initAction0UI(view);
        initAction1UI(view);
        initAction2UI(view);
        initAction3UI(view);
        initAction4UI(view);

        for (int i = 0; i < actionNum; i++) {
            actionSpinner[i].setAdapter(new ActionTypeSpinnerAdapter(getContext()));
            if (i != 0) {
                actionConfigLinearLayout[i].setVisibility(View.GONE);
            }
            actionParamEditText[i].setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
    }

    private DJIWaypoint.DJIWaypointActionType positionToActionType(int position) {
        switch (position) {
            case 0:
                return DJIWaypoint.DJIWaypointActionType.GimbalPitch;
            case 1:
                return DJIWaypoint.DJIWaypointActionType.RotateAircraft;
            case 2:
                return DJIWaypoint.DJIWaypointActionType.StartTakePhoto;
            case 3:
                return DJIWaypoint.DJIWaypointActionType.StartRecord;
            case 4:
                return DJIWaypoint.DJIWaypointActionType.StopRecord;
            case 5:
                return DJIWaypoint.DJIWaypointActionType.Stay;
            default:
                return null;
        }
    }

    class ActionTypeSpinnerAdapter extends BaseAdapter {
        ArrayList<ActionTypeSpinnerSingleRow> itemList;
        Context mContext;

        public ActionTypeSpinnerAdapter(Context context) {
            itemList = new ArrayList<>();
            mContext = context;
            Resources resources = context.getResources();
            String[] titles = resources.getStringArray(R.array.waypoint_action_type);
            String[] descriptions = resources.getStringArray(R.array.waypoint_action_type_description);
            for (int i = 0; i < titles.length; i++) {
                itemList.add(new ActionTypeSpinnerSingleRow(titles[i], descriptions[i]));
            }
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.action_name_des_spinner_item, parent, false);
            TextView titleTextView = (TextView) row.findViewById(R.id.item_title_textView);
            TextView descTextView = (TextView) row.findViewById(R.id.item_desc_textView);
            titleTextView.setText(itemList.get(position).title);
            descTextView.setText(itemList.get(position).description);
            return row;
        }
    }

    class ActionTypeSpinnerSingleRow {
        String title;
        String description;

        public ActionTypeSpinnerSingleRow(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    private void initWaypointHeadingUI(View view) {
        headingTextView = (TextView) view.findViewById(R.id.heading_textView);
        headingSeekBar = (SeekBar) view.findViewById(R.id.heading_seekBar);
        headingSeekBar.setMax(360);
        headingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tmpEditingPoint.setHeading((short) (progress - 180));
                headingTextView.setText(Integer.toString(progress - 180) + " degrees");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initWaypointGimbalPitchUI(View view) {
        gimbalPitchTextView = (TextView) view.findViewById(R.id.gimbal_pitch_textView);
        gimbalPitchSeekBar = (SeekBar) view.findViewById(R.id.gimbal_pitch_seekBar);
        gimbalPitchSeekBar.setMax(90);
        gimbalPitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int pointGimbalPitch = progress - 90;
                tmpEditingPoint.setGimbalPitch((short) pointGimbalPitch);
                gimbalPitchTextView.setText(Integer.toString(pointGimbalPitch) + " degrees");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initWaypointAltitudeUI(View view) {
        altitudeTextView = (TextView) view.findViewById(R.id.altitude_textView);
        altitudeSeekBar = (SeekBar) view.findViewById(R.id.altitude_seekBar);
        altitudeSeekBar.setMax(195);
        altitudeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int pointAltitude = progress + 5;
                tmpEditingPoint.setAltitude(pointAltitude);
                altitudeTextView.setText(Integer.toString(pointAltitude) + " m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initMarkerControlLayout(View v) {
        this.markerControlLinearLayout = (LinearLayout) v.findViewById(R.id.add_marker_layout);
        markerControlLinearLayout.setVisibility(View.GONE);
        this.addSingleMarkerToggleButton = (ToggleButton) v.findViewById(R.id.add_single_marker_toggleButton);
        this.addMarkersToggleButton = (ToggleButton) v.findViewById(R.id.add_markers_toggleButton);

        this.clearAllMarkerButton = (Button) v.findViewById(R.id.clear_markers_button);
        clearAllMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllMarkers();
            }
        });

        this.doneAddButton = (Button) v.findViewById(R.id.done_add_marker_button);
        doneAddButton.setVisibility(View.GONE);
        doneAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMakingChange)
                    return;
                if (receiveSingleLocationCallBack != null) {
                    if (Utils.checkGpsCoordinate(resultSingleLocation.latitude, resultSingleLocation.longitude))
                        receiveSingleLocationCallBack.onLocationReceive(true, resultSingleLocation);
                    else
                        receiveSingleLocationCallBack.onLocationReceive(false, null);
                }

                if (receiveMultipleLocationsCallBack != null) {
                    if (missionPoints.size() > 0)
                        receiveMultipleLocationsCallBack.onLocationReceive(true, missionPoints);
                    else
                        receiveMultipleLocationsCallBack.onLocationReceive(false, null);
                }

                markerControlLinearLayout.setVisibility(View.GONE);
                isMakingChange = false;
            }
        });

        addSingleMarkerToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isAddSingle = true;
                    doneAddButton.setVisibility(View.GONE);
                } else {
                    isAddSingle = false;
                    doneAddButton.setVisibility(View.VISIBLE);
                }
            }
        });

        addMarkersToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isAddMultiple = true;
                    doneAddButton.setVisibility(View.INVISIBLE);
                } else {
                    isAddMultiple = false;
                    doneAddButton.setVisibility(View.VISIBLE);
                }
            }
        });

//        if (FPVActivity.FPVIsSmall)
//            markerControlLinearLayout.setVisibility(View.VISIBLE);
//        else
//            markerControlLinearLayout.setVisibility(View.GONE);
    }

    @Override
    public void cancelSetMarkerOnMap() {
        isAddSingle = false;
        isAddMultiple = false;
        markerControlLinearLayout.setVisibility(View.GONE);
    }

    @Override
    public void dropSingleMarkerOnMap(LatLng markLocation) {
        Utils.setResultToToast(getContext(), "dropSingleMarkerOnMap: " + Double.toString(markLocation.latitude) + ", " + Double.toString(markLocation.longitude));
        if (singleMarker != null)
            singleMarker.remove();
        MarkerOptions options = new MarkerOptions().title("Mission Mark").position(markLocation);
        singleMarker = mMap.addMarker(options);
    }

    @Override
    public void dropMultipleMarkersOnMap(LinkedList<MyWaypoint> markLocations) {
        isMakingChange = true;
        this.missionPoints = markLocations;
        Utils.setResultToToast(getContext(), "dropMultipleMarkersOnMap");
    }

    @Override
    public void updateMultipleMarkerOnMap(LinkedList<MyWaypoint> newLocations) {
        Utils.setResultToToast(getContext(), "updateMultipleMarkerOnMap");
        this.missionPoints = newLocations;
    }

    @Override
    public void updateSingleMakerOnMap(LatLng newLocation) {
        Utils.setResultToToast(getContext(), "updateSingleMakerOnMap" + Double.toString(newLocation.latitude) + ", " + Double.toString(newLocation.longitude));
        if (singleMarker != null)
            singleMarker.remove();
        MarkerOptions options = new MarkerOptions().title("Mission Mark").position(newLocation);
        singleMarker = mMap.addMarker(options);
    }

    @Override
    public void alterMarkersOnMap(LinkedList<MyWaypoint> markerLocations, ReceiveMultipleLocationsCallBack receiveLocationsCallBack) {
        Utils.setResultToToast(getContext(), "alterMarkersOnMap");
    }

    @Override
    public void addSingleMarkerOnMap(ReceiveSingleLocationCallBack receiveLocationCallBack) {
        isMakingChange = true;
        Utils.setResultToToast(getContext(), "addSingleMarkerOnMap");
        this.receiveSingleLocationCallBack = receiveLocationCallBack;
        showSingleMarkerUI();
        addSingleMarkerToggleButton.setChecked(true);
    }

    @Override
    public void addMultipleMarkersOnMap(ReceiveMultipleLocationsCallBack callBack) {
        Utils.setResultToToast(getContext(), "addMultipleMarkersOnMap");
        this.receiveMultipleLocationsCallBack = callBack;
        if (missionPoints == null)
            missionPoints = new LinkedList<>();
        if (multipleMarkers == null)
            multipleMarkers = new LinkedList<>();
        isMakingChange = true;
        showMultiMarkersUI();
    }

    @Override
    public void cleanMarkers() {
        if (singleMarker != null)
            singleMarker.remove();

        if (multipleMarkers != null && multipleMarkers.size() > 0) {
            for (Marker marker : multipleMarkers) {
                marker.remove();
            }
        }

        if (missionPath != null) {
            missionPath.remove();
        }

        multipleMarkers = null;
        missionPath = null;
        missionPoints = null;
        singleMarker = null;
        receiveSingleLocationCallBack = null;
        isMakingChange = false;
        Utils.setResultToToast(getContext(), "cleanMarkers");
    }

    private void showMultiMarkersUI() {
        addSingleMarkerToggleButton.setVisibility(View.GONE);
        markerControlLinearLayout.setVisibility(View.VISIBLE);
        addMarkersToggleButton.setVisibility(View.VISIBLE);
        clearAllMarkerButton.setVisibility(View.VISIBLE);
        doneAddButton.setVisibility(View.VISIBLE);
        Log.e("GoogleMapsFragment", "================>> showMultiMarkersUI called!");
    }

    private void showSingleMarkerUI() {
        addSingleMarkerToggleButton.setVisibility(View.VISIBLE);
        markerControlLinearLayout.setVisibility(View.VISIBLE);
        addMarkersToggleButton.setVisibility(View.GONE);
        clearAllMarkerButton.setVisibility(View.GONE);
        doneAddButton.setVisibility(View.GONE);
    }

    class SmallMapTask extends TimerTask {
        @Override
        public void run() {
            if (getActivity() == null) return;
            updateDroneInfo();
            drawDroneHomeOnMap();
            fragmentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraUpdate(false);
                }
            });
        }
    }

    class BigMapTask extends TimerTask {
        @Override
        public void run() {
            updateDroneInfo();
            fragmentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drawDroneHomeOnMap();
                }
            });
        }
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        if (isMakingChange && multipleMarkers != null && multipleMarkers.size() > 0 && !isAddMultiple) {
            markerControlLinearLayout.setVisibility(View.GONE);
            pointInfoTextView.setText(marker.getTitle());
            currentEditingWaypointIndex = Integer.parseInt(marker.getTitle().split(" ")[1]);
            waypointConfigLayout.setVisibility(View.VISIBLE);
            initWaypointSetting();
        }
        return false;
    }

    private void initWaypointSetting() {
        tmpEditingPoint = this.missionPoints.get(currentEditingWaypointIndex);
        altitudeSeekBar.setProgress(Math.round(tmpEditingPoint.getAltitude() - 5));
        altitudeTextView.setText(Float.toString(tmpEditingPoint.getAltitude()) + " m");
        gimbalPitchSeekBar.setProgress(tmpEditingPoint.getGimbalPitch() + 90);
        gimbalPitchTextView.setText(Short.toString(tmpEditingPoint.getGimbalPitch()) + " degrees");
        headingTextView.setText(Short.toString(tmpEditingPoint.getHeading()) + " degrees");
        headingSeekBar.setProgress(tmpEditingPoint.getHeading() + 180);
        hasActionCheckBox.setChecked(tmpEditingPoint.isHasAction());
        if (tmpEditingPoint.isHasAction()) {
            actionEditorLayout.setVisibility(View.VISIBLE);
            LinkedList<DJIWaypoint.DJIWaypointAction> pointActionsLinkedList = tmpEditingPoint.getActionLinkedList();
            for (int i = 0; i < pointActionsLinkedList.size(); i++) {
                actionConfigLinearLayout[i].setVisibility(View.VISIBLE);
                actionSpinner[i].setSelection(actionTypeToPosition(pointActionsLinkedList.get(i)));
                actionParamEditText[i].setText(Integer.toString(pointActionsLinkedList.get(i).mActionParam));
            }
        }
    }

    private int actionTypeToPosition(DJIWaypoint.DJIWaypointAction action) {
        if (action.mActionType == DJIWaypoint.DJIWaypointActionType.GimbalPitch)
            return 0;
        else if (action.mActionType == DJIWaypoint.DJIWaypointActionType.RotateAircraft)
            return 1;
        else if (action.mActionType == DJIWaypoint.DJIWaypointActionType.StartTakePhoto)
            return 2;
        else if (action.mActionType == DJIWaypoint.DJIWaypointActionType.StartRecord)
            return 3;
        else if (action.mActionType == DJIWaypoint.DJIWaypointActionType.StopRecord)
            return 4;
        else if (action.mActionType == DJIWaypoint.DJIWaypointActionType.Stay)
            return 5;
        else
            return -1;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(!FPVActivity.FPVIsSmall);
        mMap.getUiSettings().setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                drawDroneHomeOnMap();
                cameraUpdate(true);
                return true;
            }
        });
        mMap.getUiSettings().setAllGesturesEnabled(FPVActivity.FPVIsSmall);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (isAddSingle) {
                    resultSingleLocation = latLng;
                    if (singleMarker == null)
                        singleMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Mission point"));
                    else
                        singleMarker.setPosition(latLng);
                }

                if (isAddMultiple) {
                    if (missionPoints == null)
                        missionPoints = new LinkedList<>();
                    MyWaypoint newPoint = new MyWaypoint(missionPoints.size(), latLng);
                    multipleMarkers.add(newPoint.getId(), mMap.addMarker(newPoint.getWaypointMarkerOptions()));
                    missionPoints.add(newPoint);
                    if (missionPath == null && missionPoints.size() > 1) {
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .add(missionPoints.get(newPoint.getId() - 1).getLocation(), newPoint.getLocation())
                                .width(5).color(Color.BLUE);
                        missionPath = mMap.addPolyline(polylineOptions);
                    } else if (missionPoints.size() > 1) {
                        LinkedList<LatLng> missionPointsPosition = new LinkedList<>();
                        for (int i = 0; i < missionPoints.size(); i++)
                            missionPointsPosition.add(missionPoints.get(i).getLocation());
                        missionPath.setPoints(missionPointsPosition);
                    }
                }

            }
        });
        if (this.singleMarker != null) {
            MarkerOptions markerOptions = new MarkerOptions().position(singleMarker.getPosition()).title(singleMarker.getTitle());
            singleMarker = mMap.addMarker(markerOptions);
        }

        if (this.missionPoints != null) {
            if (multipleMarkers == null)
                multipleMarkers = new LinkedList<>();
            PolylineOptions polylineOptions = new PolylineOptions().width(5).color(Color.BLUE);
            for (MyWaypoint waypoint : missionPoints) {
                this.multipleMarkers.add(mMap.addMarker(waypoint.getWaypointMarkerOptions()));
                if (missionPoints.size() > 1) {
                    polylineOptions.add(waypoint.getLocation());
                }
            }
            if (missionPoints.size() > 1)
                missionPath = mMap.addPolyline(polylineOptions);
            else
                missionPath = null;
        }

        updateDroneInfo();
        drawDroneHomeOnMap();
        cameraUpdate(true);
    }

    private void updateDroneInfo() {
        mProduct = FlyShareApplication.getProductInstance();
        if (mProduct != null && mProduct instanceof DJIAircraft) {
            DJIFlightController djiFlightController = ((DJIAircraft) mProduct).getFlightController();
            if (djiFlightController == null) return;
            DJIFlightControllerDataType.DJIFlightControllerCurrentState currentState = djiFlightController.getCurrentState();
            DJIFlightControllerDataType.DJILocationCoordinate3D currentLocation = currentState.getAircraftLocation();
            currentLat = currentLocation.getLatitude();
            currentLng = currentLocation.getLongitude();
            homeLat = currentState.getHomeLocation().getLatitude();
            homeLng = currentState.getHomeLocation().getLongitude();
            droneHeading = djiFlightController.getCompass().getHeading();
        }
    }


    private void drawDroneHomeOnMap() {

        LatLng currentPosition = new LatLng(currentLat, currentLng);
        droneMarkerOptions.position(currentPosition).rotation((float) (-90f + droneHeading));

        LatLng home = new LatLng(homeLat, homeLng);
        homeMarkerOptions.position(home);

        final PolylineOptions polylineOptions = new PolylineOptions().add(home, currentPosition).width(3).color(Color.RED);


        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }

                try {
                    droneMarker = mMap.addMarker(droneMarkerOptions);
                } catch (NullPointerException e) {
                    Log.e("drawDroneHomeOnMap", "-------------------------> add drone marker failed: " + e.getMessage());
                    Log.e("drawDroneHomeOnMap", "-------------------------> droneLocationLat: " + currentLng);
                    Log.e("drawDroneHomeOnMap", "-------------------------> droneLocationLng: " + currentLng);
                }

                if (home2Drone != null) {
                    home2Drone.remove();
                }
                try {
                    home2Drone = mMap.addPolyline(polylineOptions);
                } catch (NullPointerException e) {
                    Log.e("drawDroneHomeOnMap", "-------------------------> add drone home2Drone failed: " + e.getMessage());
                }

                if (homeMarker != null) {
                    homeMarker.remove();
                }
                try {
                    homeMarker = mMap.addMarker(homeMarkerOptions);
                } catch (NullPointerException e) {
                    Log.e("drawDroneHomeOnMap", "-------------------------> add drone homeMarker failed: " + e.getMessage());
                }

            }
        });
    }

    private void cameraUpdate(boolean isZoomed) {
        CameraUpdate cameraUpdate;
        if (isZoomed)
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat, currentLng), 17);
        else
            cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(currentLat, currentLng));
        try {
            mMap.moveCamera(cameraUpdate);
        } catch (NullPointerException e) {
            Log.e("cameraUpdate", "------------------> move camera failed: " + e.getMessage());
        }
    }

    @Override
    public void onStop() {
        mTask.cancel();
        Log.e("GoogleMapsFragment", "onResume()");
        Log.e("GoogleMapsFragment", "================>> markerControlLinearLayout is visible: " + Boolean.toString(markerControlLinearLayout.getVisibility() == View.VISIBLE));
        super.onStop();
    }

    @Override
    public void onResume() {
        Log.e("GoogleMapsFragment", "onResume()");
        mTimer = new Timer();
        if (FPVActivity.FPVIsSmall)
            mTask = new BigMapTask();
        else
            mTask = new SmallMapTask();
        mTimer.schedule(mTask, 0, 400);
        super.onResume();
    }

    private void initAction0UI(View view) {
        actionConfigLinearLayout[0] = (LinearLayout) view.findViewById(R.id.action0_layout);
        actionSpinner[0] = (Spinner) view.findViewById(R.id.action0_type_spinner);
        actionParamEditText[0] = (EditText) view.findViewById(R.id.action0_param_editText);
        actionDeleteButton[0] = (Button) view.findViewById(R.id.delete_action0_button);
        actionSpinner[0].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    tmpEditingPoint.changeAction(0, positionToActionType(position), 0);
                } catch (Exception e) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initAction1UI(View view) {
        actionConfigLinearLayout[1] = (LinearLayout) view.findViewById(R.id.action1_layout);
        actionSpinner[1] = (Spinner) view.findViewById(R.id.action1_type_spinner);
        actionParamEditText[1] = (EditText) view.findViewById(R.id.action1_param_editText);
        actionDeleteButton[1] = (Button) view.findViewById(R.id.delete_action1_button);

        actionSpinner[1].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    tmpEditingPoint.changeAction(1, positionToActionType(position), 0);
                } catch (Exception e) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        actionDeleteButton[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tmpEditingPoint.removeAction(1);
                actionConfigLinearLayout[1].setVisibility(View.GONE);
                actionDeleteButton[0].setVisibility(View.VISIBLE);
            }
        });
    }

    private void initAction2UI(View view) {
        actionConfigLinearLayout[2] = (LinearLayout) view.findViewById(R.id.action2_layout);
        actionSpinner[2] = (Spinner) view.findViewById(R.id.action2_type_spinner);
        actionParamEditText[2] = (EditText) view.findViewById(R.id.action2_param_editText);
        actionDeleteButton[2] = (Button) view.findViewById(R.id.delete_action2_button);
        actionSpinner[2].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    tmpEditingPoint.changeAction(2, positionToActionType(position), 0);
                } catch (Exception e) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        actionDeleteButton[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tmpEditingPoint.removeAction(2);
                actionConfigLinearLayout[2].setVisibility(View.GONE);
                actionDeleteButton[1].setVisibility(View.VISIBLE);
            }
        });
    }

    private void initAction3UI(View view) {
        actionConfigLinearLayout[3] = (LinearLayout) view.findViewById(R.id.action3_layout);
        actionSpinner[3] = (Spinner) view.findViewById(R.id.action3_type_spinner);
        actionParamEditText[3] = (EditText) view.findViewById(R.id.action3_param_editText);
        actionDeleteButton[3] = (Button) view.findViewById(R.id.delete_action3_button);

        actionSpinner[3].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    tmpEditingPoint.changeAction(3, positionToActionType(position), 0);
                } catch (Exception e) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        actionDeleteButton[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tmpEditingPoint.removeAction(3);
                actionConfigLinearLayout[3].setVisibility(View.GONE);
                actionDeleteButton[2].setVisibility(View.VISIBLE);
            }
        });
    }

    private void initAction4UI(View view) {
        actionConfigLinearLayout[4] = (LinearLayout) view.findViewById(R.id.action4_layout);
        actionSpinner[4] = (Spinner) view.findViewById(R.id.action4_type_spinner);
        actionParamEditText[4] = (EditText) view.findViewById(R.id.action4_param_editText);
        actionDeleteButton[4] = (Button) view.findViewById(R.id.delete_action4_button);
        actionSpinner[4].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    tmpEditingPoint.changeAction(4, positionToActionType(position), 0);
                } catch (Exception e) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        actionDeleteButton[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tmpEditingPoint.removeAction(4);
                actionConfigLinearLayout[4].setVisibility(View.GONE);
                actionDeleteButton[3].setVisibility(View.VISIBLE);
            }
        });
    }

}
