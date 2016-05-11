package opensource.zeocompanion.zeo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import java.util.ArrayList;
import com.myzeo.android.api.data.ZeoDataContract;
import com.myzeo.android.api.data.ZeoDataContract.Headband;
import com.myzeo.android.api.data.ZeoDataContract.SleepRecord;
import com.obscuredPreferences.ObscuredPrefs;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionDatabaseContract;

// primary Handler for interacting with the Zeo App
public class ZeoAppHandler {
    // member variables
    public int mZeoApp_State = ZAH_ZEOAPP_STATE_UNKNOWN;
    public long mZeoApp_State_timestamp = 0L;
    public long mZeoApp_active_SleepEpisode_ID = 0L;
    public int mZeoHeadband_battery_lastProbed = 0;
    public int mZeoHeadband_battery_maxWhileRecording = 0;
    public int mZeoHeadband_battery_minWhileRecording = 0;
    private Context mContext = null;
    private boolean mContinueZeoAppProbing = false;
    private boolean mAtNonJournal = false;
    private long mZeoAppProbeDelayMS = DEFAULT_ZEOAPP_PROBE_DELAY_MS;
    private long mCurrProbeRunnableIndex = 0L;
    private long mTypicalSleepDurationMin = 0L;
    private long mTimestampLastPollKnownSleepRecordID = 0L;
    private ArrayList<ZAH_Listener> mListeners = null;

    // member constants and other static content
    private static final long DEFAULT_ZEOAPP_PROBE_DELAY_MS = 14000;    // 15 seconds less the 1 second that will get auto-added before the delay call
    public static final String _CTAG = "ZAH";
    public static final int ZAH_ERROR_NONE = 0;
    public static final int ZAH_ERROR_NOT_INSTALLED = 1;
    public static final int ZAH_ERROR_NO_PERMISSION = 2;
    public static final int ZAH_ERROR_NO_DB = 3;
    public static final int ZAH_ERROR_NO_HB_REC = 4;
    public static final int ZAH_ERROR_NO_DATA = 5;

    public static final int ZAH_ZEOAPP_STATE_UNKNOWN = -1;
    public static final int ZAH_ZEOAPP_STATE_IDLE = 0;
    public static final int ZAH_ZEOAPP_STATE_STARTING = 1;
    public static final int ZAH_ZEOAPP_STATE_RECORDING = 2;
    public static final int ZAH_ZEOAPP_STATE_ENDING = 3;

    // interface for any listeners that plan to receive ZeoApp state change notifications;
    public interface ZAH_Listener {
        public void onZeoAppStateChange();
        public void onZeoAppProbedSameState();
    }

    // Tread Context: Main Thread
    // internal handler to move state change detections from the Runnable thread into the main thread
    // currently the JDC and MainActivity use this
    private Handler mZeoAppMonitorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ZeoCompanionApplication.MESSAGE_ZAH_ZEO_STATE_CHANGED) {
                // a change in the Zeo App's state has been detected and preserved
                // call all registered listeners
                for (ZAH_Listener listener: mListeners) {
                    listener.onZeoAppStateChange();
                }
            } else if (msg.what == ZeoCompanionApplication.MESSAGE_ZAH_ZEO_PROBED_NO_CHANGE) {
                // a change in the Zeo App's state has been detected and preserved
                // call all registered listeners
                for (ZAH_Listener listener: mListeners) {
                    listener.onZeoAppProbedSameState();
                }
            }
        }
    };

    // Tread Context: Probe Thread
    // internal Runnable "timer" to periodically check whether the ZeoApp's state has changed;
    // this runs in the main thread via the messaging stack; however the flag mContinueZeoAppProbing can be set by
    // any external Utility or Activity even separate threads, so need to constantly re-check the state of the flag
    private class ProbeRunnable implements Runnable {
        private long mIndex = 0;
        private ProbeRunnable(long index) {
            mIndex = index;
        }
        @Override
        public void run() {
            if (!mContinueZeoAppProbing) { return; }    // need to immediately stop probing?
            if (mIndex < mCurrProbeRunnableIndex) { return; }   // has a new probe superseded this one?

            // detect a change if any
            boolean changed = probeAppState();
            Message msg1 = new Message();
            if (changed) { msg1.what = ZeoCompanionApplication.MESSAGE_ZAH_ZEO_STATE_CHANGED; }
            else { msg1.what = ZeoCompanionApplication.MESSAGE_ZAH_ZEO_PROBED_NO_CHANGE; }
            if (mContinueZeoAppProbing) { mZeoAppMonitorHandler.sendMessage(msg1); }     // change occurred and allowed to keep probing?  yes

            // schedule the next probe
            determineNextProbeDelay();
            if (mContinueZeoAppProbing) { mZeoAppMonitorHandler.postDelayed(this, mZeoAppProbeDelayMS); }
        }
    };

    // setup a Listener for changes in the shared preferences
    SharedPreferences.OnSharedPreferenceChangeListener mPrefsChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // note; do not detect nor react to "journal_enable"; MainActivity will handle that preference's change
            if (key.equals("database_replicate_zeo")) {
                // if replication is turned on; then start a replication activity in a separate thread
                Log.d(_CTAG+".prefChgListen","Database Replication preference changed");
                boolean enabled = sharedPreferences.getBoolean("database_replicate_zeo", false);
                if (enabled) { dailyCheck(); }
            }
        }
    };

    // constructor
    public ZeoAppHandler(Context context)
    {
        mContext = context;
        mListeners = new ArrayList<ZAH_Listener>();

        // progressively determine the end-user's typical or expected sleep duration
        mTypicalSleepDurationMin = 465; // 15 min less than 8 hours
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String str = ObscuredPrefs.decryptString(prefs.getString("profile_goal_hours_per_night", "8"));
        if(!str.isEmpty()) {
            double d = Double.parseDouble(str);
            if (d > 0.0) { mTypicalSleepDurationMin = (long)(d * 60.0) - 15L; }
        }
        Long observedSleepDurationMin = getObservedTypicalSleepDurationMin();   // note: the JDC is not yet initialized
        if (observedSleepDurationMin != null) { mTypicalSleepDurationMin = observedSleepDurationMin; }
    }

    // verify the Zeo's API is active and the Zeo has been used;
    // this is performed everytime our App is brought to the foreground
    public int verifyAPI() {
        String[] cols_hb = {
                Headband.BLUETOOTH_ADDRESS,
                Headband.BLUETOOTH_FRIENDLY_NAME,
        };

        // does App still have permission to access the Zeo App's API?
        int permissionCheck = ContextCompat.checkSelfPermission(mContext, "com.myzeo.permission.READ_SLEEP_RECORDS");
        if (permissionCheck == PackageManager.PERMISSION_DENIED) { return ZAH_ERROR_NO_PERMISSION; }

        final Cursor cursor1 = mContext.getContentResolver().query(Headband.CONTENT_URI, cols_hb, null, null, null);
        if (cursor1 == null) { Log.e(_CTAG+".verifyAPI","The ZeoApp Headband Table is inaccessible"); return ZAH_ERROR_NO_DB; }
        if (!cursor1.moveToFirst()) { cursor1.close(); Log.e(_CTAG + ".verifyAPI", "The ZeoApp Headband Table is empty");  return ZAH_ERROR_NO_HB_REC; }
        cursor1.close();

        String[] cols_sleepRec = new String[] {
                SleepRecord.SLEEP_EPISODE_ID,
                SleepRecord.SOURCE,
        };
        final Cursor cursor2 = mContext.getContentResolver().query(SleepRecord.CONTENT_URI, cols_sleepRec, null, null, null);
        if (cursor2 == null) { Log.e(_CTAG+".verifyAPI","The ZeoApp SleepRecord Table is inaccessible"); return ZAH_ERROR_NO_DB; }
        if (!cursor2.moveToFirst()) { cursor2.close(); return ZAH_ERROR_NO_DATA; }
        cursor2.close();

        probeAppState();
        PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(mPrefsChangeListener);
        return ZAH_ERROR_NONE;
    }

    // called upon MainActivity startup if the Journal is enabled; the MainActivity could have been "offline" for milliseconds or even days;
    // this is done before probing so it will not cause all sorts of "changed state" invocations that are meaningless if not done in real-time
    public int resynchronize() {
        // first obtain the active headband record (it must be paired and connected); that record contains the ZeoApp's current state;
        // there may be more than one Zeo Headband record if the App has been paired with other headbands in the past
        int mZeoApp_State = ZAH_ZEOAPP_STATE_UNKNOWN;
        mZeoHeadband_battery_lastProbed = 0;
        mZeoAppProbeDelayMS = DEFAULT_ZEOAPP_PROBE_DELAY_MS;

        final Cursor cursor1 = mContext.getContentResolver().query(
                Headband.CONTENT_URI,   // data manager, database and table name
                ZAH_HeadbandRecord.ZAH_HEADBANDREC_EXTENDED_COLS,    // columns
                null,   // where clause
                null,   // values
                null);  // sort order
        if (cursor1 != null) {
            if (cursor1.moveToFirst()) {
                mZeoApp_State = ZAH_ZEOAPP_STATE_IDLE;
                do {
                    ZAH_HeadbandRecord rec = new ZAH_HeadbandRecord(cursor1);
                    if (rec.rBonded_to_device && rec.rConnected_to_device) {
                        mZeoApp_State = rec.rAlgorithm_mode;
                        mZeoHeadband_battery_lastProbed = rec.rVoltage;
                        break;
                    }
                } while (cursor1.moveToNext());
            } else { Log.e(_CTAG + ".resync", "The ZeoApp Headband Table is empty"); return ZAH_ERROR_NO_HB_REC; }
            cursor1.close();
        } else { Log.e(_CTAG+".resync","The ZeoApp Headband Table is inaccessible"); return ZAH_ERROR_NO_DB; }

        // if the App state is either recording or ending, get the current in-progress Sleep Episode ID
        if (mZeoApp_State > ZAH_ZEOAPP_STATE_STARTING) {
            String selection = SleepRecord.END_REASON + "=1";   // look for a record in "Active" state
            String sort = SleepRecord.START_OF_NIGHT + " DESC";
            final Cursor cursor2 = mContext.getContentResolver().query(
                    SleepRecord.CONTENT_URI,    // data manager, database and table name
                    ZAH_SleepRecord.ZAH_SLEEPREC_COLS,  // columns
                    selection,  // where clause
                    null,   // values
                    sort);  // sort order
            if (cursor2 != null) {
                if (cursor2.moveToFirst()) {
                    do {
                        ZAH_SleepRecord rec = new ZAH_SleepRecord(cursor2);
                        if (rec.rEndReason  == ZAH_SleepRecord.ZAH_ENDREASON_STILL_ACTIVE) {
                            mZeoApp_active_SleepEpisode_ID = rec.rSleepEpisodeID;
                            break;
                        }
                    } while (cursor2.moveToNext());
                }
                cursor2.close();
            } else { Log.e(_CTAG+".resync","The ZeoApp SleepRecord Table is inaccessible"); return ZAH_ERROR_NO_DB; }
        }
        return ZAH_ERROR_NONE;
    }

    // obtain a string explanation of a ZAH error number
    public String getErrorString(int errNo) {
        switch (errNo) {
            case ZAH_ERROR_NONE:
                return "Zeo API no error";
            case ZAH_ERROR_NO_DB:
                return "Zep App is not installed or has not yet been used";
            case ZAH_ERROR_NO_HB_REC:
                return "Zeo App has never been paired with a headband";
            case ZAH_ERROR_NO_DATA:
                return "Zeo App has no stored history data";
            case ZAH_ERROR_NO_PERMISSION:
                return "ZeoCompanion App not granted permission to access Zeo App";
            case ZAH_ERROR_NOT_INSTALLED:
                return "Zeo App is not installed";
        }
        return "Zeo API Unknown error";
    }

    // return a string interpretation of the Zeo App's state
    public String getStateString() {
        switch (mZeoApp_State) {
            case ZAH_ZEOAPP_STATE_UNKNOWN:
                return "INACCESSIBLE!";
            case ZAH_ZEOAPP_STATE_IDLE:
                return "Idle";
            case ZAH_ZEOAPP_STATE_STARTING:
                return "Starting";
            case ZAH_ZEOAPP_STATE_RECORDING:
                return "Record";
            case ZAH_ZEOAPP_STATE_ENDING:
                return "Ending";
        }
        return "Unknown";
    }

    // look for conditions that would trigger a flashing red LED for the Zeo App;
    // called by the Journal Status Bar
    public int checkforZeoAppAlarm() {
        if (ZeoCompanionApplication.mZeoAppHandler.mZeoApp_State == ZeoAppHandler.ZAH_ZEOAPP_STATE_STARTING) {
            long dur = System.currentTimeMillis() - ZeoCompanionApplication.mZeoAppHandler.mZeoApp_State_timestamp;
            if (dur > 300000) {
                // Zeo App has been in Starting state for over five minutes without the headband starting to Record
                if (dur < 900000) { return -1; }   // if within 15 min of start-of-sleep, allow notification sound
                return 1;
            }
        }

        // now check for gaps in sleep record during a live recording
        // TODO V1.1 Look for suddenly missing recording
        return 0;
    }

    /////////////////////////////////////////////////////////////////////////
    // Methods related to probing of the Zeo App
    /////////////////////////////////////////////////////////////////////////

    // add a new listener to our list; currently the JDC and MainActivity use this
    public void setZAH_Listener(ZAH_Listener listener) {
        mListeners.add(listener);
        //Log.d(_CTAG + ".setZAH_Listener", "Added new listener; cnt=" + mListeners.size());
    }

    // remove a listener from our list
    public void removeZAH_Listener(ZAH_Listener listener) {
        mListeners.remove(listener);
        //Log.d(_CTAG + ".remZAH_Listener", "Removed a listener; cnt=" + mListeners.size());
    }

    // MainActivity is indicating to start probing (the Sleep Journal is active and in-use)
    public void activateProbing() {
        mAtNonJournal = false;
        mZeoAppProbeDelayMS = DEFAULT_ZEOAPP_PROBE_DELAY_MS;
        if (mContinueZeoAppProbing) { return; } // already enabled

        mContinueZeoAppProbing = true;
        mCurrProbeRunnableIndex++;
        mZeoAppMonitorHandler.post(new ProbeRunnable(mCurrProbeRunnableIndex));
        //Log.d(_CTAG+".probeActivate","Allocated new ProbeRunnable #"+mCurrProbeRunnableIndex);
    }

    // MainActivity is indicating to stop probing; likely the App is being sent to the background or to one of the App's child Activities
    public void terminateProbing() {
        mContinueZeoAppProbing = false;
    }

    // MainActivity or JDC is indicating human interaction with the Sleep Journal tabs
    public void probing_OnJournalTabs() {
        mAtNonJournal = false;
        long wasDelay = mZeoAppProbeDelayMS;
        mZeoAppProbeDelayMS = DEFAULT_ZEOAPP_PROBE_DELAY_MS;
        if (wasDelay > 30000) {
            mCurrProbeRunnableIndex++;
            mZeoAppMonitorHandler.post(new ProbeRunnable(mCurrProbeRunnableIndex));
            //Log.d(_CTAG+".probeJT","Allocated new ProbeRunnable #"+mCurrProbeRunnableIndex);
        }
    }

    // MainActivity is indicating human interaction with the non-Sleep Journal tabs (History or Dashboard)
    public void probing_OnNonJournalTabs() {
        mAtNonJournal = true;
        long wasDelay = mZeoAppProbeDelayMS;
        mZeoAppProbeDelayMS = DEFAULT_ZEOAPP_PROBE_DELAY_MS * 2;
        if (wasDelay > 60000) {
            mCurrProbeRunnableIndex++;
            mZeoAppMonitorHandler.post(new ProbeRunnable(mCurrProbeRunnableIndex));
            //Log.d(_CTAG+".probeNJT","Allocated new ProbeRunnable #"+mCurrProbeRunnableIndex);
        }
    }

    // An attribute was set or reset (MainActivity)
    public void probing_OnAttribute() {
        probing_OnJournalTabs();
    }

    // An Event was set (DANGER: large infinite loop could result from Probing recording a Zeo event)
    public void probing_OnEvent(int eventNo) {
        if (eventNo < CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_STARTING || eventNo > CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_ENDING) {
            probing_OnJournalTabs();
        }
    }

    // calculate any "intelligent" revision of the probing delay time
    private void determineNextProbeDelay() {
        if (mZeoApp_State == ZAH_ZEOAPP_STATE_STARTING || mZeoApp_State == ZAH_ZEOAPP_STATE_ENDING) {
            // Zeo Headband is going to change state soon; poll rapidly
            mZeoAppProbeDelayMS = 5000;
        } else {
            long durInStateMin = (System.currentTimeMillis() - mZeoApp_State_timestamp) / 60000;
            if (mZeoApp_State == ZAH_ZEOAPP_STATE_RECORDING && durInStateMin > mTypicalSleepDurationMin) {
                // if headband is recording and its been nearly the entire typical sleep duration then increase to default polling
                mZeoAppProbeDelayMS = DEFAULT_ZEOAPP_PROBE_DELAY_MS;
            } else if (mZeoAppProbeDelayMS < 60000) {
                // increase the delay slowly until have a 60-second delay
                mZeoAppProbeDelayMS = mZeoAppProbeDelayMS + 1000;
            } else if (mZeoAppProbeDelayMS < 300000) {
                // increase the delay faster until a max of 5 minute delay
                if (mAtNonJournal) { mZeoAppProbeDelayMS = mZeoAppProbeDelayMS + 15000; }   // faster on non-journal tabs
                else { mZeoAppProbeDelayMS = mZeoAppProbeDelayMS + 5000; }                  // slower on journal tabs
            }
        }
    }

    // probe the Zeo App's state and detect changes;
    // returns true if the ZeoApp's state has changed
    public boolean probeAppState() {
        boolean theReturn = false;
        //Log.d(_CTAG+".probeAppState", "-->Probing the ZeoApp");
        int priorState = mZeoApp_State; // keep for Log.d
        int newState = ZAH_ZEOAPP_STATE_UNKNOWN;
        mZeoHeadband_battery_lastProbed = 0;

        // first obtain the active headband record (it must be paired and connected); that record contains the ZeoApp's current state;
        // there may be more than one Zeo Headband record if the App has been paired with other headbands in the past
        final Cursor cursor1 = mContext.getContentResolver().query(
                Headband.CONTENT_URI,   // data manager, database and table name
                ZAH_HeadbandRecord.ZAH_HEADBANDREC_EXTENDED_COLS,    // columns
                null,   // where clause
                null,   // values
                null);  // sort order
        if (cursor1 != null) {
            if (cursor1.moveToFirst()) {
                ZAH_HeadbandRecord hRec = null;
                newState = ZAH_ZEOAPP_STATE_IDLE;
                do {
                    hRec = new ZAH_HeadbandRecord(cursor1);
                    if (hRec.rBonded_to_device && hRec.rConnected_to_device) {
                        newState = hRec.rAlgorithm_mode;
                        if (hRec.rDocked) { newState = ZAH_ZEOAPP_STATE_IDLE; }  // sometimes the headband gets stuck on Ending state even when its on the charger
                        mZeoHeadband_battery_lastProbed = hRec.rVoltage;
                        break;
                    }
                    hRec = null;  // help with garbage collection
                } while (cursor1.moveToNext());
            } else { Log.e(_CTAG + ".probeAppState", "The ZeoApp Headband Table is empty"); }
            cursor1.close();
        } else { Log.e(_CTAG+".probeAppState","The ZeoApp Headband Table is inaccessible"); }

        // has the ZeoApp's state changed
        if (newState != mZeoApp_State) {
            // yes
            theReturn = true;
            if (newState <= ZAH_ZEOAPP_STATE_IDLE && mZeoApp_State == ZAH_ZEOAPP_STATE_RECORDING) {
                // we missed the ZeoApp's ending state; simulate it then we'll get to IDLE state at the next probe
                newState = ZAH_ZEOAPP_STATE_ENDING;
            }

            if (newState == ZAH_ZEOAPP_STATE_IDLE) {
                // just changed to idle, so reset the observed headband voltage values
                mZeoHeadband_battery_maxWhileRecording = 0;
                mZeoHeadband_battery_minWhileRecording = 0;
            }

            // change to the new state
            mZeoApp_State = newState;
            mZeoApp_State_timestamp = System.currentTimeMillis();
            if (mZeoApp_State <= ZAH_ZEOAPP_STATE_IDLE) {
                // its gone idle, so clear out the *active* sleep episode info
                mZeoApp_active_SleepEpisode_ID = 0;
            }
        }

        // at this point, mZeoApp_State is now the proper current state;
        // look for the current state of the in-progress recording
        if (mZeoApp_State > ZAH_ZEOAPP_STATE_IDLE) {
            // the ZeoApp is not idle, look for the proper or active sleep record
            long ts = System.currentTimeMillis();
            String selection = "";
            String[] values = new String[1];
            if (mZeoApp_active_SleepEpisode_ID == 0) {
                // sleep record is unknown; look for an active one
                selection = SleepRecord.END_REASON + "=?";
                values[0] = "1";
            } else if (ts - mTimestampLastPollKnownSleepRecordID > 290000) {
                // sleep record is known; get the updated copy of it every ~5 minutes as that's the rate the Zeo Headband sends them out (needs to be just slightly less than 5 minutes)
                selection = SleepRecord.SLEEP_EPISODE_ID + "=?";
                values[0] = String.valueOf(mZeoApp_active_SleepEpisode_ID);
            }
            if (!selection.isEmpty()) {
                String sort = SleepRecord.START_OF_NIGHT + " DESC";
                final Cursor cursor2 = mContext.getContentResolver().query(
                        SleepRecord.CONTENT_URI,    // data manager, database and table name
                        ZAH_SleepRecord.ZAH_SLEEPREC_EXTENDED_COLS,  // columns
                        selection,  // where clause
                        values,   // values
                        sort);  // sort order
                if (cursor2 != null) {
                    if (cursor2.moveToFirst()) {
                        do {
                            ZAH_SleepRecord zRec = new ZAH_SleepRecord(cursor2);
                            if (mZeoApp_active_SleepEpisode_ID == 0) {
                                // sleep record is unknown; found a potentially active one
                                if (zRec.rEndReason  == ZAH_SleepRecord.ZAH_ENDREASON_STILL_ACTIVE &&
                                        ts - zRec.rStartOfNight < 36000000L) {   // active and within the last 10 hours?
                                    // record was found
                                    mTimestampLastPollKnownSleepRecordID = ts;
                                    theReturn = true;
                                    mZeoApp_active_SleepEpisode_ID = zRec.rSleepEpisodeID;

                                    if (mZeoHeadband_battery_lastProbed == 0) { mZeoHeadband_battery_lastProbed = zRec.rVoltageBattery;  }

                                    if (mZeoApp_State == ZAH_ZEOAPP_STATE_RECORDING) {
                                        long delta = zRec.rStartOfNight - mZeoApp_State_timestamp;
                                        if (delta > 15000 || delta < -15000) {
                                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                                            boolean doAlert = prefs.getBoolean("journal_notification_hb_clock_outsync", true);
                                            if (doAlert) {
                                                String msg = "Warning: Headband\'s internal clock maybe out-of-sync with the Android device\' clock by ";
                                                if (delta > 0) { msg =  msg + ((delta-5000)/1000) + " sec.";  }
                                                else { msg = msg + ((delta+5000)/1000) + " sec.";  }
                                                msg = msg + " Please go to the Zeo Pro App itself, press the \'wrench\' for its menu page, and select the \'Diagostics\' option.";
                                                msg = msg + " On that page, look for the \'Clock Offset\' row in the \'CURRENT STATE\' section.";
                                                msg = msg + " The bright white number of that row is the difference in milliseconds between the headband\'s clock and the Android device clock.";
                                                msg = msg + " Using your finger, press on the bright white number for that row.";
                                                msg = msg + " You should hear the \'button pressed\' sound, and the number in that row should automatically change to the newly computed offset.";
                                                ZeoCompanionApplication.postAlert(msg);
                                            }
                                        }
                                    }
                                    break;
                                }
                            } else {
                                // sleep record is known; process the latest version of it
                                mTimestampLastPollKnownSleepRecordID = ts;
                                if (mZeoHeadband_battery_lastProbed == 0) { mZeoHeadband_battery_lastProbed = zRec.rVoltageBattery;  }
                                // now check for gaps in sleep record during recording
                                // TODO V1.1 Look for suddenly missing recording
                                break;
                            }
                        } while (cursor2.moveToNext());
                    }
                    cursor2.close();
                } else { Log.e(_CTAG+".probeAppState","The ZeoApp SleepRecord Table is inaccessible"); }
            }
        }

        // preserve the maximum and minimum non-zero headband voltage observed
        if (mZeoApp_State > ZAH_ZEOAPP_STATE_IDLE && mZeoHeadband_battery_lastProbed > 0) {
            if (mZeoHeadband_battery_lastProbed > mZeoHeadband_battery_maxWhileRecording) { mZeoHeadband_battery_maxWhileRecording = mZeoHeadband_battery_lastProbed; }
            if (mZeoHeadband_battery_minWhileRecording == 0) { mZeoHeadband_battery_minWhileRecording = mZeoHeadband_battery_lastProbed; }
            else if (mZeoHeadband_battery_lastProbed < mZeoHeadband_battery_minWhileRecording) { mZeoHeadband_battery_minWhileRecording = mZeoHeadband_battery_lastProbed; }
        }

        if (theReturn) {
            String str = "ZeoApp state changed: State: Prior=" + priorState + ", New=" + mZeoApp_State + "; curr: ID=" + mZeoApp_active_SleepEpisode_ID;
            Log.d(_CTAG+".probeAppState", str);
        }
        return theReturn;
    }

    /////////////////////////////////////////////////////////////////////////
    // Methods related to database reads of the Zeo App's database
    /////////////////////////////////////////////////////////////////////////

    // get the most active headband record (used when the Headband Commander attempts to connect to the headband)
    public ZAH_HeadbandRecord getActiveHeadbandRecord() {
        final Cursor cursor = mContext.getContentResolver().query(Headband.CONTENT_URI, ZAH_HeadbandRecord.ZAH_HEADBANDREC_COLS, null, null, null);
        if (cursor == null) { return null; }
        if (!cursor.moveToFirst()) { cursor.close(); return null; }

        // first pass; get the connected headband
        ZAH_HeadbandRecord rec = null;
        do {
            rec = new ZAH_HeadbandRecord(cursor);
            if (rec.rBonded_to_device && rec.rConnected_to_device) { cursor.close(); return rec; }
            rec = null;  // help with garbage collection
        } while (cursor.moveToNext());

        // second pass; get the first bonded/paired headband found in the DB
        cursor.moveToFirst();
        do {
            rec = new ZAH_HeadbandRecord(cursor);
            if (rec.rBonded_to_device) { cursor.close(); return rec; }
            rec = null;  // help with garbage collection
        } while (cursor.moveToNext());

        // otherwise just return the first record in the Zeo's DB
        cursor.moveToFirst();
        rec = new ZAH_HeadbandRecord(cursor);
        cursor.close();
        return rec;
    }

    // retrieve the specified sleep record by ID from the Zeo App's database, and provide a string of its END_REASON code
    public String getStateStringOfID(long id) {
        String where = ZeoDataContract.SleepRecord.SLEEP_EPISODE_ID + "=?";
        String values[] = { String.valueOf(id) };
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    ZeoDataContract.SleepRecord.CONTENT_URI,    // data manager, database and table name
                    ZAH_SleepRecord.ZAH_SLEEPREC_COLS,          // columns to get
                    where,          // columns for optional WHERE clause
                    values,        // values for optional WHERE clause
                    null); // sort order
            if (cursor == null) { return "None"; }
            if (!cursor.moveToFirst()) { cursor.close(); return "None"; }
            ZAH_SleepRecord zRec = new ZAH_SleepRecord(cursor);
            return zRec.getStatusString();
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getStateStringOfID", e, "ID="+id);   // automatically posts a Log.e
            if (cursor != null) { cursor.close(); }
        }
        return "None";
    }

    // get one specific Zeo Sleep record within the Zeo App's database
    public ZAH_SleepRecord getSpecifiedSleepRecOfID(long id) {
        //return ZeoCompanionApplication.mDatabaseHandler.getSpecifiedZeoSleepRec(id);  // can return null
        String where = ZeoDataContract.SleepRecord.SLEEP_EPISODE_ID + "=?";
        String values[] = { String.valueOf(id) };
        Cursor cursor = null;
        try {
            cursor =  mContext.getContentResolver().query(
                    ZeoDataContract.SleepRecord.CONTENT_URI,    // data manager, database and table name
                    ZAH_SleepRecord.ZAH_SLEEPREC_EXTENDED_COLS,          // columns to get
                    where,       // columns for optional WHERE clause
                    values,       // values for optional WHERE clause
                    null); // sort order
            if (cursor == null) { return null; }
            if (!cursor.moveToFirst()) { cursor.close(); return null; }
            ZAH_SleepRecord newRec = new ZAH_SleepRecord(cursor);
            cursor.close();
            return newRec;
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getSpecifiedSleepRec", e, "ID="+id);   // automatically posts a Log.e
            if (cursor != null) { cursor.close(); cursor = null; }
        }
        return null;
    }

    // get all the Zeo Sleep records within the Zeo App's database
    public Cursor getAllSleepRecs() {
        //return ZeoCompanionApplication.mDatabaseHandler.getAllZeoSleepRecs(); // can return null
        String sortOrder = ZeoDataContract.SleepRecord.START_OF_NIGHT + " DESC";
        Cursor cursor = null;
        try {
            cursor =  mContext.getContentResolver().query(
                ZeoDataContract.SleepRecord.CONTENT_URI,    // data manager, database and table name
                ZAH_SleepRecord.ZAH_SLEEPREC_EXTENDED_COLS,          // columns to get
                null,       // columns for optional WHERE clause
                null,       // values for optional WHERE clause
                sortOrder); // sort order
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getAllZeoSleepRecs", e);   // automatically posts a Log.e
            if (cursor != null) { cursor.close(); cursor = null; }
        }
        return cursor;
    }

    // get only those Zeo Sleep records after the specified date from the Zeo App's database
    public Cursor getAllSleepRecsAfterDate(long fromTimestamp) {
        //return ZeoCompanionApplication.mDatabaseHandler.getAllZeoSleepRecsAfterDate(fromTimestamp);   // can return null
        String sortOrder = ZeoDataContract.SleepRecord.START_OF_NIGHT + " DESC";
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    ZeoDataContract.SleepRecord.CONTENT_URI,    // data manager, database and table name
                    ZAH_SleepRecord.ZAH_SLEEPREC_EXTENDED_COLS,          // columns to get
                    ZeoDataContract.SleepRecord.START_OF_NIGHT+">=?",       // columns for optional WHERE clause
                    new String[] { String.valueOf(fromTimestamp) },         // values for optional WHERE clause
                    sortOrder); // sort order
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getAllSleepRecsAfterDate", e, "For timestamp="+fromTimestamp);   // automatically posts a Log.e
            if (cursor != null) { cursor.close(); cursor = null; }
        }
        return cursor;
    }

    public Long getObservedTypicalSleepDurationMin() {
        String sortOrder = ZeoDataContract.SleepRecord.START_OF_NIGHT + " DESC";
        String[] cols = { ZeoDataContract.SleepRecord.START_OF_NIGHT + "," + ZeoDataContract.SleepRecord.END_OF_NIGHT + "," + ZeoDataContract.SleepRecord.TOTAL_Z };
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    ZeoDataContract.SleepRecord.CONTENT_URI,    // data manager, database and table name
                    cols,   // columns to get
                    null,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    sortOrder);    // sort order
            if (cursor == null) { return null; }
            if (cursor.moveToFirst()) {
                long sumDurMin = 0L;
                int qtyDurMin = 0;
                do {
                    long totZ = cursor.getLong(cursor.getColumnIndex(ZeoDataContract.SleepRecord.TOTAL_Z));
                    if (totZ > 0) {
                        long son = cursor.getLong(cursor.getColumnIndex(ZeoDataContract.SleepRecord.START_OF_NIGHT));
                        long eon = cursor.getLong(cursor.getColumnIndex(ZeoDataContract.SleepRecord.END_OF_NIGHT));
                        if (son > 0L && eon > 0L) {
                            long durMin = (eon - son) / 60000L;
                            sumDurMin = sumDurMin + durMin;
                            qtyDurMin++;
                        }
                        // TODO V1.1 Compute typical end-of-night per day-of-week
                    }
                } while (cursor.moveToNext());
                cursor.close();
                if (qtyDurMin == 0) { return null; }
                Long avgDur = sumDurMin / qtyDurMin;
                return avgDur;
            }
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getObservedTypicalSleepDurationMin", e);   // automatically posts a Log.e
        }
        if (cursor != null) { cursor.close(); }
        return null;
    }

    //////////////////////////////////////////////////////////////////////
    // All the below methods are utilized for Zeo App Database replication;
    // Many of these methods run in a separate thread as indicated
    //////////////////////////////////////////////////////////////////////

    // Thread context: main thread
    // purge all the replicated data be deleting only the Zeo App data tables;
    // trigger re-replication if still enabled
    public void purgeAllReplicated() {
        ZeoCompanionApplication.mDatabaseHandler.purgeAllZeoTables();
        dailyCheck();
    }

    // Thread context: main thread
    // called daily by the AlarmManager to check for newly created records that need replication;
    // will also be called when the replicate preference is turned on
    public void dailyCheck() {
        Log.d(_CTAG+".dailyCheck","Daily check triggered");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean enabled = prefs.getBoolean("database_replicate_zeo", false);
        if (!enabled) { return; }

        // activate a replication process in a separate thread
        Thread thrd = new Thread(new ReplicateZeoDatabaseThread());
        thrd.setName("ReplicateZeoDatabase via "+_CTAG+".dailyCheck");
        thrd.start();
    }

    // Thread context: main or ReplicateZeoDatabase threads
    // disable replication if there are caught errors so the end-user does not get locked out of the application
    public void disableReplication() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("database_replicate_zeo", false);
        editor.commit();
        ZeoCompanionApplication.postAlert("NOTICE: Zeo App Database replication has been auto-disabled due to database errors; contact the Developer");

        // must send the toast indirectly since this is invoked within a utility thread
        Message msg = new Message();
        msg.what = ZeoCompanionApplication.MESSAGE_APP_SEND_TOAST;
        msg.obj = "NOTICE: Zeo App Database replication has been auto-disabled";
        ZeoCompanionApplication.mAppHandler.sendMessage(msg);
    }

    // Thread context: ReplicateZeoDatabase thread
    // class that implements the separate that performs the Zeo App database replication
    private class ReplicateZeoDatabaseThread implements Runnable {
        // Thread context: ReplicateZeoDatabase thread
        @Override
        public void run() {
            Log.d(_CTAG + ".dailyCheck.run", "Starting replication thread");
            Thread.setDefaultUncaughtExceptionHandler(ZeoCompanionApplication.mMasterAbortHandler); // set the master abort handler for this thread
            String[] existingTables = ZeoCompanionApplication.mDatabaseHandler.getAllZeoTables();   // can be null
            if (existingTables == null) {
                Log.d(_CTAG+".ReplZeoDB.run","Existing Zeo tables=null");
            } else {
                String str = "";
                for (String name: existingTables) { str = str + name + ","; }
                Log.d(_CTAG+".ReplZeoDB.run","Existing Zeo tables=" + str);

            }

            replicate_ZeoHeadbands(existingTables);
            replicate_ZeoSleepEvents(existingTables);
            replicate_ZeoSleepRecords(existingTables);
            replicate_ZeoAlarmAlertEvents(existingTables);
            replicate_ZeoAlarmTimeoutEvents(existingTables);
            replicate_ZeoAlarmSnoozeEvents(existingTables);
        }

        // Thread context: ReplicateZeoDatabase thread
        // replicate the Headband Zeo App table
        private void replicate_ZeoHeadbands(String[] existingTables) {
            String sortOrder = ZeoDataContract.Headband._ID + " ASC ";
            doReplicateOneTable(ZeoDataContract.Headband.CONTENT_URI, CompanionDatabaseContract.ZeoHeadbands.TABLE_NAME,
                    CompanionDatabaseContract.ZeoHeadbands.PROJECTION_FULL, sortOrder, CompanionDatabaseContract.ZeoHeadbands.SQL_DEFINITION, existingTables);
        }

        // Thread context: ReplicateZeoDatabase thread
        // replicate the SleepEpisode Zeo App table
        private void replicate_ZeoSleepEvents(String[] existingTables) {
            String sortOrder = ZeoDataContract.SleepEpisode._ID + " ASC ";
            doReplicateOneTable(ZeoDataContract.SleepEpisode.CONTENT_URI, CompanionDatabaseContract.ZeoSleepEvents.TABLE_NAME,
                    CompanionDatabaseContract.ZeoSleepEvents.PROJECTION_FULL, sortOrder, CompanionDatabaseContract.ZeoSleepEvents.SQL_DEFINITION, existingTables);
        }

        // Thread context: ReplicateZeoDatabase thread
        // replicate the SleepRecord Zeo App table
        private void replicate_ZeoSleepRecords(String[] existingTables) {
            String sortOrder = ZeoDataContract.SleepRecord._ID + " ASC ";
            doReplicateOneTable(ZeoDataContract.SleepRecord.CONTENT_URI, CompanionDatabaseContract.ZeoSleepRecords.TABLE_NAME,
                    CompanionDatabaseContract.ZeoSleepRecords.PROJECTION_FULL, sortOrder, CompanionDatabaseContract.ZeoSleepRecords.SQL_DEFINITION, existingTables);
        }

        // Thread context: ReplicateZeoDatabase thread
        // replicate the AlarmAlertEvent Zeo App table
        private void replicate_ZeoAlarmAlertEvents(String[] existingTables) {
            String sortOrder = ZeoDataContract.AlarmAlertEvent._ID + " ASC ";
            doReplicateOneTable(ZeoDataContract.AlarmAlertEvent.CONTENT_URI, CompanionDatabaseContract.ZeoAlarmAlertEvents.TABLE_NAME,
                    CompanionDatabaseContract.ZeoAlarmAlertEvents.PROJECTION_FULL, sortOrder, CompanionDatabaseContract.ZeoAlarmAlertEvents.SQL_DEFINITION, existingTables);
        }

        // Thread context: ReplicateZeoDatabase thread
        // replicate the AlarmTimeoutEvent Zeo App table
        private void replicate_ZeoAlarmTimeoutEvents(String[] existingTables) {
            String sortOrder = ZeoDataContract.AlarmTimeoutEvent._ID + " ASC ";
            doReplicateOneTable(ZeoDataContract.AlarmTimeoutEvent.CONTENT_URI, CompanionDatabaseContract.ZeoAlarmTimeoutEvents.TABLE_NAME,
                    CompanionDatabaseContract.ZeoAlarmTimeoutEvents.PROJECTION_FULL, sortOrder, CompanionDatabaseContract.ZeoAlarmTimeoutEvents.SQL_DEFINITION, existingTables);
        }

        // Thread context: ReplicateZeoDatabase thread
        // replicate the AlarmSnoozeEvent Zeo App table
        private void replicate_ZeoAlarmSnoozeEvents(String[] existingTables) {
            String sortOrder = ZeoDataContract.AlarmSnoozeEvent._ID + " ASC ";
            doReplicateOneTable(ZeoDataContract.AlarmSnoozeEvent.CONTENT_URI, CompanionDatabaseContract.ZeoAlarmSnoozeEvents.TABLE_NAME,
                    CompanionDatabaseContract.ZeoAlarmSnoozeEvents.PROJECTION_FULL, sortOrder, CompanionDatabaseContract.ZeoAlarmSnoozeEvents.SQL_DEFINITION, existingTables);
        }

        // Thread context: ReplicateZeoDatabase thread
        // replicate the indicated table's contents from the Zeo App to the ZeoCompanion's database; existingTables can be null
        private void doReplicateOneTable(Uri zeoContentURI, String neededTable, String[] projection, String sortOrder, String sqlDefinition, String[] existingTables) {
            Cursor cursorZeo = null;
            try {
                // are there any Zeo App records?
                cursorZeo = mContext.getContentResolver().query(
                        zeoContentURI,    // data manager, database and table name
                        projection,          // columns to get
                        null,       // columns for optional WHERE clause
                        null,         // values for optional WHERE clause
                        sortOrder); // sort order
                if (cursorZeo == null) { return; }  // nope
                if (!cursorZeo.moveToFirst()) { cursorZeo.close(); return; } // nope

                // yes; ensure the replicated table exists in our database
                boolean found = false;
                if (existingTables != null) {
                    for (String tableName: existingTables) {
                        if (neededTable.equals(tableName)) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    // Zeo database table does not exist so create it
                    boolean result = ZeoCompanionApplication.mDatabaseHandler.createZeoTable(sqlDefinition);
                    if (!result) { return; }
                }

                // perform the replication of any new records not already copied
                int cntr = 0;
                long[] companionIDs = ZeoCompanionApplication.mDatabaseHandler.getAllRecIDsZeoTable(neededTable, sortOrder);    // can be null
                if (companionIDs == null) {
                    do {
                        ZeoCompanionApplication.mDatabaseHandler.putRecIntoZeoTable(neededTable, cursorZeo);
                        cntr++;
                    } while (cursorZeo.moveToNext());
                } else {
                    do {
                        int col = cursorZeo.getColumnIndex(BaseColumns._ID);
                        if (col >= 0) {
                            long id = cursorZeo.getLong(col);
                            found = false;
                            for (long anID: companionIDs) {
                                if (id == anID) {
                                    found = true;
                                    break;
                                }
                            }
                        } else {
                            disableReplication();
                            ZeoCompanionApplication.postToErrorLog(_CTAG+".doReplicateOneTable", "cursorZeo.getColumnIndex(BaseColumns._ID) returned "+col, "For DB Table: "+neededTable + "and row "+cursorZeo.getPosition());    // automatically posts a Log.e
                            return;
                        }
                        if (!found) {
                            ZeoCompanionApplication.mDatabaseHandler.putRecIntoZeoTable(neededTable, cursorZeo);
                            cntr++;
                        }
                    } while (cursorZeo.moveToNext());
                }
                Log.d(_CTAG + ".doRepl1Tbl", "Added " + cntr + " records to table " + neededTable);
            } catch (Exception e) {
                disableReplication();
                ZeoCompanionApplication.postToErrorLog(_CTAG+".doReplicateOneTable", e, "For DB Table: "+neededTable);    // automatically posts a Log.e
            }
            cursorZeo.close();
        }
    }
}


