package com.freeman.flyshare.yweathergetter4a;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.freeman.flyshare.MyWaypointMission;
import com.freeman.flyshare.R;
import com.freeman.flyshare.Utils;

import java.util.ArrayList;
import java.util.LinkedList;


public class LocalMissionFragment extends Fragment {
    Activity fragmentActivity;
    View mView;
    ListView localMissionListView, onlineMissionListView;
    TextView titleTextView;
    Button downloadMissionButton, doneOnlineMissionButton;
    LinkedList<Utils.MissionPack> missionPacks;
    LinkedList<Utils.MissionPack> onlineMissionPacks;
    MissionItemAdapter localMissionItemAdapter;
    OnlineMissionItemAdapter onlineMissionItemAdapter;
    private SwipeRefreshLayout swipeContainer;
    private ProgressDialog mProgressDialog;

    private class UpdateThread extends Thread {
        private boolean isRunning = true;

        public void stopRunning() {
            isRunning = false;
        }

        @Override
        public void run() {
            while (isRunning) {
                try {
                    sleep(10000);
                } catch (InterruptedException e) {

                }
                fragmentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateMissionItems();
                    }
                });
            }
        }
    }

    UpdateThread updateThread;

    public LocalMissionFragment() {
        // Required empty public constructor
    }

    public static LocalMissionFragment newInstance() {
        LocalMissionFragment fragment = new LocalMissionFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentActivity = getActivity();
        mView = inflater.inflate(R.layout.fragment_local_mission, container, false);
        localMissionListView = (ListView) mView.findViewById(R.id.local_mission_listView);
        localMissionItemAdapter = new MissionItemAdapter(fragmentActivity);
        localMissionListView.setAdapter(localMissionItemAdapter);
        updateMissionItems();
        swipeContainer = (SwipeRefreshLayout) mView.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateMissionItems();
            }
        });

        mProgressDialog = new ProgressDialog(fragmentActivity);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        titleTextView = (TextView) mView.findViewById(R.id.mission_fragment_title_textView);
        onlineMissionListView = (ListView) mView.findViewById(R.id.online_mission_listView);
        onlineMissionItemAdapter = new OnlineMissionItemAdapter(fragmentActivity);
        onlineMissionListView.setAdapter(onlineMissionItemAdapter);
        doneOnlineMissionButton = (Button) mView.findViewById(R.id.back_to_local_mission_button);
        doneOnlineMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnToLocalMode();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        downloadMissionButton = (Button) mView.findViewById(R.id.download_mission_button);
        downloadMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                titleTextView.setText("Online missions");
                new loadingThread().start();
            }
        });
        updateThread = new UpdateThread();
        updateThread.start();
        return mView;
    }

    class loadingThread extends Thread {
        public void run() {
            long sleepTime = 1500 + (long) Math.random() * 5000;
            try {
                sleep(sleepTime);
            } catch (InterruptedException e) {
            }

            fragmentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialog();
                    turnToOnlineMode();
                }
            });
        }
    }

    private void turnToOnlineMode() {
        localMissionListView.setVisibility(View.GONE);
        downloadMissionButton.setVisibility(View.GONE);
        onlineMissionListView.setVisibility(View.VISIBLE);
        doneOnlineMissionButton.setVisibility(View.VISIBLE);
    }

    private void turnToLocalMode() {
        titleTextView.setText("Local missions");
        localMissionListView.setVisibility(View.VISIBLE);
        downloadMissionButton.setVisibility(View.VISIBLE);
        onlineMissionListView.setVisibility(View.GONE);
        doneOnlineMissionButton.setVisibility(View.GONE);
    }

    public void updateMissionItems() {
        localMissionItemAdapter.clearAllItem();
        missionPacks = Utils.getAllMissions(fragmentActivity);
        if (missionPacks != null && missionPacks.size() > 0) {
            for (Utils.MissionPack missionPack : missionPacks) {
                localMissionItemAdapter.addItem(new MissionSingleRow(missionPack.name, missionPack.desc));
            }

        }
        localMissionItemAdapter.notifyDataSetChanged();

        onlineMissionItemAdapter.clearAllItem();
        onlineMissionPacks = Utils.getAllOnlineMissions(fragmentActivity);
        if (onlineMissionPacks != null && onlineMissionPacks.size() > 0) {
            for (Utils.MissionPack missionPack : onlineMissionPacks) {
                onlineMissionItemAdapter.addItem(new MissionSingleRow(missionPack.name, missionPack.desc));
            }

        }
        onlineMissionItemAdapter.notifyDataSetChanged();

        if (swipeContainer != null)
            swipeContainer.setRefreshing(false);
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.local_mission_item_view, parent, false);
            TextView titleTextView = (TextView) row.findViewById(R.id.local_mission_name_textView);
            TextView descTextView = (TextView) row.findViewById(R.id.local_mission_desc_textView);
            Button deleteButton = (Button) row.findViewById(R.id.delete_mission_button);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.deleteMission(fragmentActivity, missionPacks.get(position).id, missionPacks.get(position).fileName)) {
                        missionPacks.remove(position);
                        updateMissionItems();

                    }
                    Log.e("Delete Click", "Mission " + Integer.toString(position) + " deleted!");
                }
            });
            Button uploadButton = (Button) row.findViewById(R.id.upload_mission_button);
            uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyWaypointMission tmpMission = Utils.getMission(fragmentActivity, missionPacks.get(position).fileName);
                    Utils.uploadMission(fragmentActivity, tmpMission, missionPacks.get(position).fileName);
                    Log.e("Upload Click", "Mission " + Integer.toString(position) + " Upload!");
                }
            });
            titleTextView.setText(itemList.get(position).title);
            descTextView.setText(itemList.get(position).description);
            return row;
        }
    }

    class OnlineMissionItemAdapter extends BaseAdapter {
        ArrayList<MissionSingleRow> itemList;
        Context mContext;

        public OnlineMissionItemAdapter(Context context) {
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.local_mission_item_view, parent, false);
            TextView titleTextView = (TextView) row.findViewById(R.id.local_mission_name_textView);
            TextView descTextView = (TextView) row.findViewById(R.id.local_mission_desc_textView);
            Button deleteButton = (Button) row.findViewById(R.id.delete_mission_button);
            deleteButton.setVisibility(View.GONE);
            Button uploadButton = (Button) row.findViewById(R.id.upload_mission_button);
            uploadButton.setText("Download");
            uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.downloadMission(fragmentActivity, Utils.getMission(fragmentActivity,onlineMissionPacks.get(position).fileName));
                    Log.e("Download Click", "Mission " + Integer.toString(position) + " Download!");
                }
            });
            titleTextView.setText(itemList.get(position).title);
            descTextView.setText(itemList.get(position).description);
            return row;
        }
    }


    @Override
    public void onStop() {
        this.updateThread.stopRunning();
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

    private void showProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
        mProgressDialog = new ProgressDialog(fragmentActivity);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }

    @Override
    public void onDestroy() {
        hideProgressDialog();
        mProgressDialog = null;
        super.onDestroy();
    }

}
