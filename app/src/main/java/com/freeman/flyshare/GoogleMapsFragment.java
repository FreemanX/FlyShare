package com.freeman.flyshare;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import dji.sdk.FlightController.DJIFlightControllerDelegate;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.base.DJIBaseProduct;


public class GoogleMapsFragment extends Fragment implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private Timer mTimer;
    private TimerTask mTaskSmallWindow;
    boolean locationInited = false;
    MapView mMapView;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    double currentLat, currentLng, homeLat, homeLng;
    private Marker droneMarker, homeMarker;
    private MarkerOptions droneMarkerOptions, homeMarkerOptions;
    private Polyline home2Drone = null;

    DJIBaseProduct mProduct;

    public GoogleMapsFragment() {
        // Required empty public constructor
    }


    public static GoogleMapsFragment getGoogleMapsFragment() {
        GoogleMapsFragment fragment = new GoogleMapsFragment();

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
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        droneMarkerOptions = new MarkerOptions();
        droneMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_drone));
        homeMarkerOptions = new MarkerOptions();
        homeMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_home));

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);
        updateDroneInfo();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        mTimer = new Timer();
        class Task extends TimerTask {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawDroneHomeOnMap();
                        cameraUpdate();
                    }
                });
            }
        }
        mTaskSmallWindow = new Task();
        mTimer.schedule(mTaskSmallWindow, 0, 500);
        mTaskSmallWindow.run();

    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(false);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
//                updateDroneInfo();
                drawDroneHomeOnMap();
                cameraUpdate();
                return true;
            }
        });
        updateDroneInfo();
        drawDroneHomeOnMap();
        cameraUpdate();
    }

    private void updateDroneInfo() {
        mProduct = FlyShareApplication.getProductInstance();
        if (mProduct != null && mProduct instanceof DJIAircraft) {
            DJIFlightController djiFlightController = ((DJIAircraft) mProduct).getFlightController();
            djiFlightController.setUpdateSystemStateCallback(new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {
                @Override
                public void onResult(DJIFlightControllerDataType.DJIFlightControllerCurrentState currentState) {
                    DJIFlightControllerDataType.DJILocationCoordinate3D currentLocation = currentState.getAircraftLocation();
                    currentLat = currentLocation.getLatitude();
                    currentLng = currentLocation.getLongitude();
                    homeLat = currentState.getHomeLocation().getLatitude();
                    homeLng = currentState.getHomeLocation().getLongitude();
                }
            });
        }
    }

    private void drawDroneHomeOnMap() {
        double offset = 0.0001;
        LatLng currentPosition = new LatLng(currentLat, currentLng);
        droneMarkerOptions.position(new LatLng(currentLat - offset * 2, currentLng));

        LatLng home = new LatLng(homeLat, homeLng);
        homeMarkerOptions.position(new LatLng(homeLat - offset, homeLng));

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

    private void cameraUpdate() {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat, currentLng), 17);
        try {
            mMap.moveCamera(cameraUpdate);
        } catch (NullPointerException e) {
            Log.e("cameraUpdate", "------------------> move camera failed: " + e.getMessage());
        }
    }

}
