package com.freeman.flyshare;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import dji.sdk.AirLink.DJILBAirLink;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.RemoteController.DJIRemoteController;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIError;


public class RemoteControllerFragment extends Fragment {
    private Activity fragmentActivity;
    private View mView;
    private RadioGroup styleRadioGroup, channelModeRadioGroup;
    private RadioButton americanRadioButton, chineseRadioButton, japaneseRadioButton, autoRadioButton, manualRadioButton;
    private Spinner channelSpinner;
    private boolean isAuto = false;

    public RemoteControllerFragment() {

    }

    private boolean isReady() {
        return FlyShareApplication.getProductInstance() != null
                && FlyShareApplication.getProductInstance() instanceof DJIAircraft
                && ((DJIAircraft) FlyShareApplication.getProductInstance()).getRemoteController() != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentActivity = getActivity();
        mView = inflater.inflate(R.layout.fragment_remote_controller, container, false);
        styleRadioGroup = (RadioGroup) mView.findViewById(R.id.RC_style_radio_group);
        channelModeRadioGroup = (RadioGroup) mView.findViewById(R.id.RC_channel_mode_radio_group);
        americanRadioButton = (RadioButton) mView.findViewById(R.id.american_radioButton);
        chineseRadioButton = (RadioButton) mView.findViewById(R.id.chinese_radioButton);
        japaneseRadioButton = (RadioButton) mView.findViewById(R.id.japanese_radioButton);
        autoRadioButton = (RadioButton) mView.findViewById(R.id.RC_auto_radioButton);
        manualRadioButton = (RadioButton) mView.findViewById(R.id.RC_manual_radioButton);
        channelSpinner = (Spinner) mView.findViewById(R.id.RC_channel_spinner);
        ArrayAdapter channelItemAdapter = ArrayAdapter.createFromResource(getContext(), R.array.lb_link_channels, R.layout.single_line_layout);
        channelSpinner.setAdapter(channelItemAdapter);
        if (isReady()) {
            FlyShareApplication.getProductInstance().getAirLink().getLBAirLink().getChannelSelectionMode(new DJIBaseComponent.DJICompletionCallbackWith<DJILBAirLink.LBAirLinkChannelSelectionMode>() {
                @Override
                public void onSuccess(final DJILBAirLink.LBAirLinkChannelSelectionMode lbAirLinkChannelSelectionMode) {

                    fragmentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (lbAirLinkChannelSelectionMode.equals(DJILBAirLink.LBAirLinkChannelSelectionMode.Auto)) {
                                autoRadioButton.setChecked(true);
                                manualRadioButton.setChecked(false);
                                channelSpinner.setEnabled(false);
                            } else if (lbAirLinkChannelSelectionMode.equals(DJILBAirLink.LBAirLinkChannelSelectionMode.Manual)) {
                                autoRadioButton.setChecked(false);
                                manualRadioButton.setChecked(true);
                                channelSpinner.setEnabled(true);
                            }
                        }
                    });
                }

                @Override
                public void onFailure(DJIError djiError) {
                    Log.e(RemoteControllerFragment.class.getName(), "get LB link mode failed: " + djiError.getDescription());
                }
            });
            FlyShareApplication.getProductInstance().getAirLink().getLBAirLink().getChannel(new DJIBaseComponent.DJICompletionCallbackWith<Integer>() {
                @Override
                public void onSuccess(final Integer integer) {
                    fragmentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int channel = -(integer + 1);
                            channelSpinner.setSelection(channel);
                        }
                    });
                }

                @SuppressLint("LongLogTag")
                @Override
                public void onFailure(DJIError djiError) {
                    Log.d("RemoteControllerFragment", "--------------->> get Channel failed: " + djiError.getDescription());
                }
            });

            channelModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.RC_auto_radioButton) {
                        FlyShareApplication.getProductInstance().getAirLink().getLBAirLink().setChannelSelectionMode(DJILBAirLink.LBAirLinkChannelSelectionMode.Auto, new DJIBaseComponent.DJICompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError != null)
                                    Utils.setResultToToast(fragmentActivity, "Set channel mode failed: " + djiError.getDescription());
                                else {
                                    fragmentActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            channelSpinner.setEnabled(false);
                                        }
                                    });
                                    FlyShareApplication.getProductInstance().getAirLink().getLBAirLink().getChannel(new DJIBaseComponent.DJICompletionCallbackWith<Integer>() {
                                        @Override
                                        public void onSuccess(final Integer integer) {
                                            fragmentActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    int channel = -(integer + 1);

                                                    channelSpinner.setSelection(channel);
                                                }
                                            });
                                        }

                                        @SuppressLint("LongLogTag")
                                        @Override
                                        public void onFailure(DJIError djiError) {
                                            Log.d("RemoteControllerFragment", "--------------->> get Channel failed: " + djiError.getDescription());
                                        }
                                    });
                                }
                            }
                        });
                    } else if (checkedId == R.id.RC_manual_radioButton) {
                        FlyShareApplication.getProductInstance().getAirLink().getLBAirLink().setChannelSelectionMode(DJILBAirLink.LBAirLinkChannelSelectionMode.Manual, new DJIBaseComponent.DJICompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError != null)
                                    Utils.setResultToToast(fragmentActivity, "Set channel mode failed: " + djiError.getDescription());
                                else {
                                    fragmentActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            channelSpinner.setEnabled(true);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });

            channelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    FlyShareApplication.getProductInstance().getAirLink().getLBAirLink().setChannel(position + 1, new DJIBaseComponent.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null)
                                Utils.setResultToToast(fragmentActivity, "Set channel mode failed: " + djiError.getDescription());
                        }
                    });
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
           /* ((DJIAircraft) FlyShareApplication.getProductInstance()).getRemoteController().getRCControlMode(new DJIBaseComponent.DJICompletionCallbackWith<DJIRemoteController.DJIRCControlMode>() {
                @Override
                public void onSuccess(DJIRemoteController.DJIRCControlMode djircControlMode) {

                }

                @Override
                public void onFailure(DJIError djiError) {

                }
            });

            styleRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.american_radioButton) {
                        ((DJIAircraft) FlyShareApplication.getProductInstance())
                                .getRemoteController()
                                .setRCControlMode(new DJIRemoteController.DJIRCControlMode(DJIRemoteController.DJIRCControlStyle.American, DJIRemoteController.DJIRCControlStyle.American)
                                        , new DJIBaseComponent.DJICompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {

                                    }
                                });
                    }
                }
            });
            */
        }

        return mView;
    }

    public static RemoteControllerFragment newInstance() {
        RemoteControllerFragment fragment = new RemoteControllerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
