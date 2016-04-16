package com.freeman.flyshare.yweathergetter4a;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
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

import com.freeman.flyshare.R;
import com.freeman.flyshare.Utils;

import java.util.ArrayList;
import java.util.LinkedList;


public class LocalMissionFragment extends Fragment {
    Activity fragmentActivity;
    View mView;
    ListView localMissionListView;
    Button downloadMissionButton;
    LinkedList<Utils.MissionPack> missionPacks;
    MissionItemAdapter missionItemAdapter;
    private SwipeRefreshLayout swipeContainer;

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
        missionItemAdapter = new MissionItemAdapter(fragmentActivity);
        localMissionListView.setAdapter(missionItemAdapter);
        updateMissionItems();
        swipeContainer = (SwipeRefreshLayout) mView.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateMissionItems();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        downloadMissionButton = (Button) mView.findViewById(R.id.download_mission_button);
        updateThread = new UpdateThread();
        updateThread.start();
        return mView;
    }

    public void updateMissionItems() {
        missionItemAdapter.clearAllItem();
        missionPacks = Utils.getAllMissions(fragmentActivity);
        if (missionPacks != null && missionPacks.size() > 0) {
            for (Utils.MissionPack missionPack : missionPacks) {
                missionItemAdapter.addItem(new MissionSingleRow(missionPack.name, missionPack.desc));
            }

        }
        missionItemAdapter.notifyDataSetChanged();
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
                    Log.e("Upload Click", "Mission " + Integer.toString(position) + " Upload!");
                }
            });
            titleTextView.setText(itemList.get(position).title);
            descTextView.setText(itemList.get(position).description);
            return row;
        }
    }

    @Override
    public void onStop() {
        updateThread.stopRunning();
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

}
