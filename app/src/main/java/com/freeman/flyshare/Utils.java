package com.freeman.flyshare;

/**
 * Created by freeman on 4/1/2016.
 */

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Random;

public class Utils {
    public static final double ONE_METER_OFFSET = 0.00000899322;
    private static long lastClickTime;
    private static Handler mUIHandler = new Handler(Looper.getMainLooper());
    public static final String DATABASE_NAME = "FlyShareDB";
    public static final String MISSION_TABLE = "MissionTable";
    public static final String ONLINE_MISSION_TABLE = "OnlineMissionTable";
    public static final String MISSION_ID = "mId";
    public static final String MISSION_NAME = "mName";
    public static final String MISSION_DESC = "mDesc";
    public static final String MISSION_FILE_NAME = "mFileName";


    public static void setResultToToast(final Context context, final String string) {
        if (context == null || string == null) return;
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean checkGpsCoordinate(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    public static double Degree(double x) {
        return x * 180 / Math.PI;
    }

    public static double cosForDegree(double degree) {
        return Math.cos(degree * Math.PI / 180.0f);
    }

    public static double calcLongitudeOffset(double latitude) {
        return ONE_METER_OFFSET / cosForDegree(latitude);
    }

    public static class MissionPack {
        public int id;
        public String name;
        public String desc;
        public String fileName;

        public MissionPack(int id, String name, String desc, String fileName) {
            this.id = id;
            this.name = name;
            this.desc = desc;
            this.fileName = fileName;
        }
    }

    public static LinkedList<MissionPack> getAllMissions(Activity activity) {
        SQLHelper helper = new SQLHelper(activity);
        SQLiteDatabase db = helper.getWritableDatabase();

        String[] columns = {MISSION_ID, MISSION_NAME, MISSION_DESC, MISSION_FILE_NAME};
        Cursor cursor = db.query(MISSION_TABLE, columns, null, null, null, null, null);
        LinkedList<MissionPack> missionPacks = new LinkedList<>();
        while (cursor.moveToNext()) {
            missionPacks.add(new MissionPack(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)));
        }
        db.close();
        return missionPacks;
    }

    public static LinkedList<MissionPack> getAllOnlineMissions(Activity activity) {
        SQLHelper helper = new SQLHelper(activity);
        SQLiteDatabase db = helper.getWritableDatabase();

        String[] columns = {MISSION_ID, MISSION_NAME, MISSION_DESC, MISSION_FILE_NAME};
        Cursor cursor = db.query(ONLINE_MISSION_TABLE, columns, null, null, null, null, null);
        LinkedList<MissionPack> missionPacks = new LinkedList<>();
        while (cursor.moveToNext()) {
            missionPacks.add(new MissionPack(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)));
        }
        db.close();
        return missionPacks;
    }

    public static MyWaypointMission getMission(Activity activity, String fname) {
        try {
            FileInputStream fileIn = activity.openFileInput(fname);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileIn);
            Object inObj = objectInputStream.readObject();
            objectInputStream.close();
            fileIn.close();
            if (inObj instanceof SavedWaypointMission)
                return MyWaypointMission.restoreWayPointMission((SavedWaypointMission) inObj);
            else {
                Log.e("Utils", "Fail to get mission! Not Mission object!");
                return null;
            }
        } catch (FileNotFoundException e) {
            Log.e("Utils", "Fail to get mission!1 " + e.getMessage());
            return null;
        } catch (StreamCorruptedException e) {
            Log.e("Utils", "Fail to get mission!2 " + e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Utils", "Fail to get mission!3 " + e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            Log.e("Utils", "Fail to get mission!4 " + e.getMessage());
            return null;
        }
    }

    public static boolean deleteMission(Activity activity, int missionID, String fileName) {
        activity.deleteFile(fileName);
        SQLHelper helper = new SQLHelper(activity);
        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        int numRow = sqLiteDatabase.delete(MISSION_TABLE, MISSION_ID + "=" + Integer.toString(missionID), null);
        sqLiteDatabase.close();
        return numRow > 0;
    }

    public static boolean uploadMission(Activity activity, MyWaypointMission mission, String fileName) {
        SQLHelper helper = new SQLHelper(activity);
        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MISSION_NAME, mission.missionName);
        contentValues.put(MISSION_DESC, mission.missionDescription);
        contentValues.put(MISSION_FILE_NAME, fileName);
        long id = sqLiteDatabase.insert(ONLINE_MISSION_TABLE, null, contentValues);
        sqLiteDatabase.close();
        new waitThread(activity).run();
        return id > 0;
    }

    static class waitThread extends Thread {
        private Activity act;
        public waitThread(Activity activity) {
            act = activity;
        }

        public void run() {
            long time = 3000 + (long) Math.random() * 2000;
            try {
                sleep(time);
            } catch (InterruptedException e) {

            }
            Utils.setResultToToast(act, "Timeout!");
        }
    }

    public static boolean downloadMission(Activity activity, MyWaypointMission mission) {
        if (mission == null) return false;
        String fileName = "i" + mission.missionName + Double.toString(Math.random());
        boolean saveSuccess = false;
        try {
            FileOutputStream fileOutputStream = activity.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(mission.prepareSavedMission());
            objectOutputStream.close();
            fileOutputStream.close();
            saveSuccess = true;
        } catch (FileNotFoundException e) {
            Log.e("Utils", "Fail to save mission!1 " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Utils", "Fail to save mission!2 " + e.getMessage());
            e.printStackTrace();
        }

        SQLHelper helper = new SQLHelper(activity);
        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MISSION_NAME, mission.missionName);
        contentValues.put(MISSION_DESC, mission.missionDescription);
        contentValues.put(MISSION_FILE_NAME, fileName);
        long id = sqLiteDatabase.insert(MISSION_TABLE, null, contentValues);
        sqLiteDatabase.close();
        return saveSuccess && id > 0;
    }

    public static boolean saveMission(Activity activity, MyWaypointMission mission) {
        if (mission == null) return false;
        String fileName = mission.missionName + Double.toString(Math.random()) + ".ser";
        boolean saveSuccess = false;
        try {
            FileOutputStream fileOutputStream = activity.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(mission.prepareSavedMission());
            objectOutputStream.close();
            fileOutputStream.close();
            saveSuccess = true;
        } catch (FileNotFoundException e) {
            Log.e("Utils", "Fail to save mission!1 " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Utils", "Fail to save mission!2 " + e.getMessage());
            e.printStackTrace();
        }
//        Utils.setResultToToast(activity,"3Name: " + mission.missionName + "\n3Desc: " + mission.missionDescription);
        SQLHelper helper = new SQLHelper(activity);
        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MISSION_NAME, mission.missionName);
        contentValues.put(MISSION_DESC, mission.missionDescription);
        contentValues.put(MISSION_FILE_NAME, fileName);
        long id = sqLiteDatabase.insert(MISSION_TABLE, null, contentValues);
        sqLiteDatabase.close();
        return saveSuccess && id > 0;
    }

    static class SQLHelper extends SQLiteOpenHelper {
        Context mContext;
        SQLiteDatabase db;
        public static final int DATABASE_VERSION = 2;

        public SQLHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            db = getWritableDatabase();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Utils.setResultToToast(mContext, "SQLHelper onCreate called");
            String onCreateQuery = "CREATE TABLE IF NOT EXISTS " + MISSION_TABLE
                    + " ("
                    + MISSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + MISSION_NAME + " VARCHAR(64), "
                    + MISSION_DESC + " VARCHAR(128), "
                    + MISSION_FILE_NAME + " VARCHAR(128)"
                    + ");";

            String onCreateOnlineDBQuery = "CREATE TABLE IF NOT EXISTS " + ONLINE_MISSION_TABLE
                    + " ("
                    + MISSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + MISSION_NAME + " VARCHAR(64), "
                    + MISSION_DESC + " VARCHAR(128), "
                    + MISSION_FILE_NAME + " VARCHAR(128)"
                    + ");";

            try {
                db.execSQL(onCreateQuery);
                db.execSQL(onCreateOnlineDBQuery);
            } catch (RuntimeException e) {
                Log.e("Utils", "Fail crate database: " + e.getMessage()
                        + "\n SQL: " + onCreateQuery);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Utils.setResultToToast(mContext, "SQLHelper onUpgrade called");
            String dropTableQuery = "DROP TABLE IF EXISTS " + MISSION_TABLE;
            String dropOnlineTableQuery = "DROP TABLE IF EXISTS " + MISSION_TABLE;
            try {
                db.execSQL(dropTableQuery);
                db.execSQL(dropOnlineTableQuery);
                onCreate(db);
            } catch (RuntimeException e) {
                Log.e("Utils", "Fail DROP TABLE: " + e.getMessage());
            }
        }
    }


}
