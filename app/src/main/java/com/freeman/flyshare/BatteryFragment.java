package com.freeman.flyshare;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import dji.sdk.Battery.DJIBattery;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIError;


public class BatteryFragment extends Fragment {

    private View mView;
    private Activity fragmentActivity;

    private SeekBar numDischargeSeekBar;
    private ProgressBar batteryPowerProgressBar, batteryLifeProgressBar;
    private TextView batteryPowerTextView, batteryLifeTextView, temperatureTextView, numDischargeTextView;

    public BatteryFragment() {

    }

    private void initBatteryPower() {
        batteryPowerProgressBar = (ProgressBar) mView.findViewById(R.id.battery_power_progressBar);
        batteryPowerTextView = (TextView) mView.findViewById(R.id.battery_power_textView);
    }

    private void initBatteryLife() {
        batteryLifeProgressBar = (ProgressBar) mView.findViewById(R.id.battery_life_progressBar);
        batteryLifeTextView = (TextView) mView.findViewById(R.id.battery_life_textView);
    }

    private void initBatteryTemperature() {
        temperatureTextView = (TextView) mView.findViewById(R.id.temperature_textView);
    }

    private void initNumOfDischargeDay() {
        numDischargeSeekBar = (SeekBar) mView.findViewById(R.id.number_discharge_day_seekBar);
        numDischargeSeekBar.setMax(9);
        numDischargeTextView = (TextView) mView.findViewById(R.id.number_discharge_day_textView);
        numDischargeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int numOfDays = progress + 1;
                numDischargeTextView.setText(Integer.toString(numOfDays) + " days");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                short numOfDays = (short) (numDischargeSeekBar.getProgress() + 1);
                if (isReady())
                    FlyShareApplication.getProductInstance().getBattery().setSelfDischargeDay(numOfDays, new DJIBaseComponent.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null)
                                Log.e("BatteryFragment", "Set discharge day fails: " + djiError.getDescription());
                        }
                    });
            }
        });

    }

    public static BatteryFragment newInstance() {
        BatteryFragment fragment = new BatteryFragment();

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
        this.mView = inflater.inflate(R.layout.fragment_battery, container, false);
        fragmentActivity = this.getActivity();
        initBatteryLife();
        initBatteryPower();
        initBatteryTemperature();
        initNumOfDischargeDay();
        if (isReady())
            FlyShareApplication.getProductInstance().getBattery().setBatteryStateUpdateCallback(new DJIBattery.DJIBatteryStateUpdateCallback() {
                @Override
                public void onResult(final DJIBattery.DJIBatteryState djiBatteryState) {
                    fragmentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            batteryPowerProgressBar.setProgress(djiBatteryState.getBatteryEnergyRemainingPercent());
                            batteryPowerTextView.setText(Integer.toString(djiBatteryState.getBatteryEnergyRemainingPercent()) + "%");
                            batteryLifeProgressBar.setProgress(djiBatteryState.getLifetimeRemainingPercent());
                            batteryLifeTextView.setText(Integer.toString(djiBatteryState.getLifetimeRemainingPercent()) + "%");
                            temperatureTextView.setText(Integer.toString(djiBatteryState.getBatteryTemperature()) + "°C");
                        }
                    });

                    FlyShareApplication.getProductInstance().getBattery().getSelfDischargeDay(new DJIBaseComponent.DJICompletionCallbackWith<Short>() {
                        @Override
                        public void onSuccess(final Short aShort) {
                            fragmentActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    numDischargeTextView.setText(Short.toString(aShort) + " days");
                                    int progress = aShort - 1;
                                    numDischargeSeekBar.setProgress(progress);
                                }
                            });
                        }

                        @Override
                        public void onFailure(DJIError djiError) {
                            Log.e("BatteryFragment", "get self discharge day failed: " + djiError.getDescription());
                        }
                    });

                }
            });
        return mView;
    }

    private boolean isReady() {
        return FlyShareApplication.getProductInstance() != null
                && FlyShareApplication.getProductInstance().getBattery() != null
                && FlyShareApplication.getProductInstance().getBattery().isConnected()
                && FlyShareApplication.getProductInstance().getBattery().isSmartBattery();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        if (isReady())
            FlyShareApplication.getProductInstance().getBattery().setBatteryStateUpdateCallback(null);
        super.onDetach();
    }

}
