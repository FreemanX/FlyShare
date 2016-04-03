package com.freeman.flyshare;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.FlightController.DJIFlightControllerDataType;
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


    private ReceiveMultipleLocationsCallBack receiveMultipleLocationsCallBack;
    private LatLng[] resultMultipleLocations;

    DJIBaseProduct mProduct;

    public GoogleMapsFragment() {
        // Required empty public constructor
    }

    void setSingleMarker(Marker savedMarker) {
        this.singleMarker = savedMarker;
    }

    public static GoogleMapsFragment getGoogleMapsFragment(@Nullable Marker savedMarker) {
        GoogleMapsFragment fragment = new GoogleMapsFragment();
        if (savedMarker != null)
            fragment.setSingleMarker(savedMarker);
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

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);
        updateDroneInfo();
        return view;
    }

    private void initMarkerControlLayout(View v) {
        this.markerControlLinearLayout = (LinearLayout) v.findViewById(R.id.add_marker_layout);
        markerControlLinearLayout.setVisibility(View.GONE);
        this.addSingleMarkerToggleButton = (ToggleButton) v.findViewById(R.id.add_single_marker_toggleButton);
        this.addMarkersToggleButton = (ToggleButton) v.findViewById(R.id.add_markers_toggleButton);
        this.doneAddButton = (Button) v.findViewById(R.id.done_add_marker_button);
        this.clearAllMarkerButton = (Button) v.findViewById(R.id.clear_markers_button);
        doneAddButton.setVisibility(View.GONE);

        doneAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (receiveSingleLocationCallBack != null) {
                    if (Utils.checkGpsCoordinate(resultSingleLocation.latitude, resultSingleLocation.longitude))
                        receiveSingleLocationCallBack.onLocationReceive(true, resultSingleLocation);
                    else
                        receiveSingleLocationCallBack.onLocationReceive(false, null);
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
                    doneAddButton.setVisibility(View.GONE);
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
    public void dropMultipleMarkersOnMap(LatLng[] markLocations) {
        isMakingChange = true;
        Utils.setResultToToast(getContext(), "dropMultipleMarkersOnMap");
    }

    @Override
    public void updateMultipleMarkerOnMap(LatLng[] newLocations) {
        Utils.setResultToToast(getContext(), "updateMultipleMarkerOnMap");
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
    public void alterMarkersOnMap(LatLng[] markerLocations, ReceiveMultipleLocationsCallBack receiveLocationsCallBack) {
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
    public void addMultipleMarkersOnMap(ReceiveMultipleLocationsCallBack receiveLocationsCallBack) {
        Utils.setResultToToast(getContext(), "addMultipleMarkersOnMap");
    }

    @Override
    public void cleanMarkers() {
        if (singleMarker != null)
            singleMarker.remove();

        singleMarker = null;
        receiveSingleLocationCallBack = null;
        isMakingChange = false;
        Utils.setResultToToast(getContext(), "cleanMarkers");
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
            getActivity().runOnUiThread(new Runnable() {
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
            getActivity().runOnUiThread(new Runnable() {
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
        return false;
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
            }
        });
        if (this.singleMarker != null) {
            MarkerOptions markerOptions = new MarkerOptions().position(singleMarker.getPosition()).title(singleMarker.getTitle());
            singleMarker = mMap.addMarker(markerOptions);
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


        getActivity().runOnUiThread(new Runnable() {
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
}
