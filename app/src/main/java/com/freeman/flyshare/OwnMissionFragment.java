package com.freeman.flyshare;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import dji.sdk.MissionManager.DJIMission;
import dji.sdk.MissionManager.DJIWaypointMission;


public class OwnMissionFragment extends MissionFragment {
    private LinearLayout enterConfigMissionLinearLayout, selectMissionLinearLayout;
    private Button loadMissionButton, createMissionButton, editPointButton, saveMissionButton;
    private RadioGroup finishActionRadioGroup, headingRadioGroup;
    private RadioButton goHomeRadioButton, noActionRadioButton, stayInFirstRadioButton, landAtLastRadioButton, autoRadioButton, controlByRCRadioButton, useWaypointRadioButton;
    private TextView maxSpeedTextView, allSameAltitudeTextView, allSameAltitudeTitleTextView, repeatTimeTextView;
    private SeekBar maxSpeedSeekBar, allSameAltitudeSeekBar, repeatTimeSeekBar;
    private CheckBox noRCContinueCheckBox, autoPitchGimbalCheckBox, allSameAltitudeCheckBox;
    private ListView missionListView;
    private MyWaypointMission currentMission;
    MissionItemAdapter missionItemAdapter;
    LinkedList<Utils.MissionPack> missionPacks;

    public OwnMissionFragment() {

    }

    @Override
    protected DJIMission initMission() {
        DJIWaypointMission djiWaypointMission = currentMission.initDJIWaypointMission();

        if (mFlightController != null) {
            float currentAlt = mFlightController.getCurrentState().getAircraftLocation().getAltitude();
            if (currentAlt >= currentMission.getFirstAltitude()) {
                currentMission.setGotoWaypointMode(DJIWaypointMission.DJIWaypointMissionGotoWaypointMode.Safely);
            } else {
                currentMission.setGotoWaypointMode(DJIWaypointMission.DJIWaypointMissionGotoWaypointMode.PointToPoint);
            }
        }

        return djiWaypointMission;
    }

    @Override
    protected int getFragmentViewResource() {
        return R.layout.fragment_own_mission;
    }

    @Override
    protected void initMissionOngoingUIComponents() {

    }

    @Override
    protected void initConfigMissionUIComponents() {
        initEnterConfigMissionUI();
        configLayout.setVisibility(View.GONE);
        editPointButton = (Button) mView.findViewById(R.id.edit_marks_button);

        headingRadioGroup = (RadioGroup) mView.findViewById(R.id.heading_radio_group);
        controlByRCRadioButton = (RadioButton) mView.findViewById(R.id.control_by_RC_heading_radioButton);
        autoRadioButton = (RadioButton) mView.findViewById(R.id.auto_land_radioButton);
        useWaypointRadioButton = (RadioButton) mView.findViewById(R.id.use_waypoint_heading_radioButton);
        controlByRCRadioButton.setChecked(true);
        headingRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.auto_heading_radioButton:
                        currentMission.setHeadingMode(DJIWaypointMission.DJIWaypointMissionHeadingMode.Auto);
                        break;
                    case R.id.control_by_RC_heading_radioButton:
                        currentMission.setHeadingMode(DJIWaypointMission.DJIWaypointMissionHeadingMode.ControlByRemoteController);
                        break;
                    case R.id.use_waypoint_heading_radioButton:
                        currentMission.setHeadingMode(DJIWaypointMission.DJIWaypointMissionHeadingMode.UsingWaypointHeading);
                        break;
                    default:
                        break;
                }
            }
        });

        this.finishActionRadioGroup = (RadioGroup) mView.findViewById(R.id.finish_action_radio_group);
        noActionRadioButton = (RadioButton) mView.findViewById(R.id.no_action_radioButton);
        goHomeRadioButton = (RadioButton) mView.findViewById(R.id.go_home_radioButton);
        stayInFirstRadioButton = (RadioButton) mView.findViewById(R.id.go_first_point_radioButton);
        landAtLastRadioButton = (RadioButton) mView.findViewById(R.id.auto_land_radioButton);
        noActionRadioButton.setChecked(true);
        finishActionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.go_home_radioButton:
                        currentMission.setFinishedAction(DJIWaypointMission.DJIWaypointMissionFinishedAction.GoHome);
                        break;
                    case R.id.go_first_point_radioButton:
                        currentMission.setFinishedAction(DJIWaypointMission.DJIWaypointMissionFinishedAction.GoFirstWaypoint);
                        break;
                    case R.id.no_action_radioButton:
                        currentMission.setFinishedAction(DJIWaypointMission.DJIWaypointMissionFinishedAction.NoAction);
                        break;
                    case R.id.auto_land_radioButton:
                        currentMission.setFinishedAction(DJIWaypointMission.DJIWaypointMissionFinishedAction.AutoLand);
                    default:
                        break;
                }
            }
        });

        maxSpeedTextView = (TextView) mView.findViewById(R.id.max_speed_textView);
        maxSpeedSeekBar = (SeekBar) mView.findViewById(R.id.max_speed_seekBar);
        maxSpeedSeekBar.setMax(13);
        maxSpeedSeekBar.setProgress((int) (currentMission.getAutoFlightSpeed() - 2));
        maxSpeedTextView.setText(Integer.toString((int) (currentMission.getAutoFlightSpeed())) + " m/s");
        maxSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int speed = progress + 2;
                maxSpeedTextView.setText(Integer.toString(speed) + " m/s");
                currentMission.setAutoFlightSpeed(speed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        noRCContinueCheckBox = (CheckBox) mView.findViewById(R.id.continue_when_RC_lost_checkbox);
        noRCContinueCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentMission.setNeedExitMissionOnRCSignalLost(!isChecked);
            }
        });
        autoPitchGimbalCheckBox = (CheckBox) mView.findViewById(R.id.auto_pitch_checkbox);
        autoPitchGimbalCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentMission.setNeedRotateGimbalPitch(isChecked);
            }
        });

        allSameAltitudeTitleTextView = (TextView) mView.findViewById(R.id.altitude_title_textView);
        allSameAltitudeTextView = (TextView) mView.findViewById(R.id.same_altitude_textView);
        allSameAltitudeSeekBar = (SeekBar) mView.findViewById(R.id.same_altitude_seekBar);
        allSameAltitudeSeekBar.setMax(195);
        allSameAltitudeSeekBar.setProgress((int) currentMission.getSameAltitude() - 5);
        allSameAltitudeTextView.setText(Integer.toString((int) currentMission.getSameAltitude()) + " m");
        allSameAltitudeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int alt = progress + 5;
                allSameAltitudeTextView.setText(Integer.toString(alt) + " m");
                currentMission.setSameAltitude(alt);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        allSameAltitudeCheckBox = (CheckBox) mView.findViewById(R.id.same_altitude_checkbox);
        allSameAltitudeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentMission.setIsAllSameAltitude(isChecked);
                if (!isChecked) {
                    allSameAltitudeTitleTextView.setVisibility(View.GONE);
                    allSameAltitudeTextView.setVisibility(View.GONE);
                    allSameAltitudeSeekBar.setVisibility(View.GONE);
                } else {
                    allSameAltitudeTitleTextView.setVisibility(View.VISIBLE);
                    allSameAltitudeTextView.setVisibility(View.VISIBLE);
                    allSameAltitudeSeekBar.setVisibility(View.VISIBLE);
                }
            }
        });
        allSameAltitudeCheckBox.setChecked(false);
        allSameAltitudeTitleTextView.setVisibility(View.GONE);
        allSameAltitudeTextView.setVisibility(View.GONE);
        allSameAltitudeSeekBar.setVisibility(View.GONE);
        editPointButton = (Button) mView.findViewById(R.id.edit_marks_button);
        editPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkedList<MyWaypoint> locations = currentMission.getMissionPoints();
                if (locations != null && locations.size() > 0)
                    missionRequestMapHandler.sendDropMultipleMarkersRequestToMap(locations);
                missionRequestMapHandler.sendAddMultipleMarkersRequestToMap(new ReceiveMultipleLocationsCallBack() {
                    @Override
                    public void onLocationReceive(boolean isSuccessful, LinkedList<MyWaypoint> locations) {
                        if (isSuccessful) {
                            currentMission.updateLocations(locations);
                        }
                        fragmentActivity.getSupportFragmentManager().beginTransaction().show(getThis()).commit();
                    }
                });
                fragmentActivity.getSupportFragmentManager().beginTransaction().hide(getThis()).commit();

            }
        });

        this.saveMissionButton = (Button) mView.findViewById(R.id.save_mission_button);
        saveMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMission != null) {
                    final LinearLayout missionDescView = (LinearLayout) getLayoutInflater(savedInstanceState).inflate(R.layout.mission_desc_layout, null);
                    new AlertDialog.Builder(fragmentActivity)
                            .setTitle("Config mission")
                            .setView(missionDescView)
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = ((EditText) missionDescView.findViewById(R.id.mission_name_editText)).getText().toString();
                                    String desc = ((EditText) missionDescView.findViewById(R.id.mission_description_editText)).getText().toString();
                                    if (name != null && name.length() > 0)
                                        currentMission.missionName = name;
                                    if (desc != null && desc.length() > 0)
                                        currentMission.missionDescription = desc;
                                    if (Utils.saveMission(fragmentActivity, currentMission)) {
                                        Utils.setResultToToast(fragmentActivity, "Mission saved!");
                                    } else {
                                        Utils.setResultToToast(fragmentActivity, "Failed to save mission!");
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
            }
        });
        this.repeatTimeTextView = (TextView) mView.findViewById(R.id.repeat_time_textView);
        this.repeatTimeSeekBar = (SeekBar) mView.findViewById(R.id.repeat_time_seekBar);

        repeatTimeSeekBar.setMax(24);
        repeatTimeSeekBar.setProgress(currentMission.getRepeatNum() - 1);
        repeatTimeTextView.setText(Integer.toString(currentMission.getRepeatNum()));
        repeatTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                repeatTimeTextView.setText(Integer.toString(progress + 1));
                currentMission.setRepeatNum(progress + 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    class MissionSingleRow {
        String title;
        String description;

        public MissionSingleRow(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    class MissionItemAdapter extends BaseAdapter {
        ArrayList<MissionSingleRow> itemList;
        Context mContext;

        public MissionItemAdapter(Context context) {
            itemList = new ArrayList<>();
            mContext = context;
        }

        public void addItem(MissionSingleRow row) {
            itemList.add(row);
        }

        public void clearAllItem() {
            itemList = new ArrayList<>();
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

    private void updateMissionItems() {
        missionItemAdapter.clearAllItem();
        missionPacks = Utils.getAllMissions(fragmentActivity);
        if (missionPacks != null && missionPacks.size() > 0) {
            for (Utils.MissionPack missionPack : missionPacks) {
                missionItemAdapter.addItem(new MissionSingleRow(missionPack.name, missionPack.desc));
            }
        }
        missionItemAdapter.notifyDataSetChanged();
    }

    private void initEnterConfigMissionUI() {
        uploadButton.setVisibility(View.GONE);
        selectMissionLinearLayout = (LinearLayout) mView.findViewById(R.id.mission_selection_layout);
        selectMissionLinearLayout.setVisibility(View.GONE);
        missionListView = (ListView) mView.findViewById(R.id.mission_listView);
        missionItemAdapter = new MissionItemAdapter(fragmentActivity);
        missionListView.setAdapter(missionItemAdapter);
        missionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentMission = Utils.getMission(fragmentActivity, missionPacks.get(position).fileName);
                if (currentMission != null) {
                    missionRequestMapHandler.sendDropMultipleMarkersRequestToMap(currentMission.getMissionPoints());
                    fragmentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            selectMissionLinearLayout.setVisibility(View.GONE);
                            enterConfigMissionLinearLayout.setVisibility(View.GONE);
                            configLayout.setVisibility(View.VISIBLE);
                            uploadButton.setVisibility(View.VISIBLE);
                            if (currentMission.getFinishedAction() == DJIWaypointMission.DJIWaypointMissionFinishedAction.GoHome) {
                                goHomeRadioButton.setChecked(true);
                            } else if (currentMission.getFinishedAction() == DJIWaypointMission.DJIWaypointMissionFinishedAction.NoAction)
                                noActionRadioButton.setChecked(true);
                            else if (currentMission.getFinishedAction() == DJIWaypointMission.DJIWaypointMissionFinishedAction.GoFirstWaypoint) {
                                stayInFirstRadioButton.setChecked(true);
                            } else if (currentMission.getFinishedAction() == DJIWaypointMission.DJIWaypointMissionFinishedAction.AutoLand) {
                                landAtLastRadioButton.setChecked(true);
                            }

                            if (currentMission.getHeadingMode() == DJIWaypointMission.DJIWaypointMissionHeadingMode.Auto) {
                                autoRadioButton.setChecked(true);
                            } else if (currentMission.getHeadingMode() == DJIWaypointMission.DJIWaypointMissionHeadingMode.ControlByRemoteController)
                                controlByRCRadioButton.setChecked(true);
                            else if (currentMission.getHeadingMode() == DJIWaypointMission.DJIWaypointMissionHeadingMode.UsingWaypointHeading)
                                useWaypointRadioButton.setChecked(true);

                            int flySpeed = (int) currentMission.getAutoFlightSpeed();
                            maxSpeedSeekBar.setProgress(flySpeed - 2);
                            maxSpeedTextView.setText(Integer.toString(flySpeed) + " m/s");

                            noRCContinueCheckBox.setChecked(!currentMission.isNeedExitMissionOnRCSignalLost());
                            autoPitchGimbalCheckBox.setChecked(currentMission.isNeedRotateGimbalPitch());
                            allSameAltitudeCheckBox.setChecked(currentMission.isAllSameAltitude());
                            int alt = (int) currentMission.getSameAltitude();
                            allSameAltitudeSeekBar.setProgress(alt - 5);
                            allSameAltitudeTextView.setText(Integer.toString(alt) + " m");
                            repeatTimeSeekBar.setProgress(currentMission.getRepeatNum() - 1);
                            repeatTimeTextView.setText(Integer.toString(currentMission.getRepeatNum()));
                        }
                    });
                } else {
                    Log.e("OwnMission", "+++++++++=================>>> Get mission failed!");
                    fragmentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            selectMissionLinearLayout.setVisibility(View.GONE);
                            enterConfigMissionLinearLayout.setVisibility(View.VISIBLE);
                            configLayout.setVisibility(View.GONE);
                            uploadButton.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

        this.enterConfigMissionLinearLayout = (LinearLayout) mView.findViewById(R.id.enter_config_mission_layout);
        this.createMissionButton = (Button) mView.findViewById(R.id.create_mission_button);
        this.loadMissionButton = (Button) mView.findViewById(R.id.load_mission_button);
        loadMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMissionItems();
                selectMissionLinearLayout.setVisibility(View.VISIBLE);
                enterConfigMissionLinearLayout.setVisibility(View.GONE);
                configLayout.setVisibility(View.GONE);
                ongoingLayout.setVisibility(View.GONE);
                cancelButton.setVisibility(View.VISIBLE);
            }
        });


        createMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMissionLinearLayout.setVisibility(View.GONE);
                enterConfigMissionLinearLayout.setVisibility(View.GONE);
                configLayout.setVisibility(View.VISIBLE);
                uploadButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onUploadClickOperationSuccess() {
        super.onUploadClickOperationSuccess();
        saveMissionButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStartClickOperationSuccess() {
        super.onStartClickOperationSuccess();
        saveMissionButton.setVisibility(View.GONE);
    }

    @Override
    protected void onStopClickOperationSuccess() {
        super.onStopClickOperationSuccess();
    }

    @Override
    protected void initMissionVariables() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        currentMission = new MyWaypointMission("Mission" + Long.toString(System.currentTimeMillis()),
                "Mission created at " + dateFormat.format(cal.getTime()));
    }


    public static OwnMissionFragment newInstance() {
        OwnMissionFragment fragment = new OwnMissionFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
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
    protected void finishMission() {
        missionRequestMapHandler.sendCleanMarkersToMap();
        super.finishMission();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void missionProgressStatus(DJIMission.DJIMissionProgressStatus djiMissionProgressStatus) {

    }

    public interface OnFragmentInteractionListener {

    }
}
