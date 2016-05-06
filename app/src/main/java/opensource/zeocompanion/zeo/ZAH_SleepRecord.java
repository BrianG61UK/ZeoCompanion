package opensource.zeocompanion.zeo;

import android.database.Cursor;
import com.myzeo.android.api.data.ZeoDataContract;

// ZeoApp Sleep record
public class ZAH_SleepRecord {
    // member variables
    public long rID = -1;                   // note this is NOT the master Sleep Episode ID
    public long rCreated_timestamp = 0;
    public long rUpdated_timestamp = 0;
    public long rSleepEpisodeID = 0;        // note this IS the master Sleep Episode ID that links all Zeo Sleep records together
    public long rLocalizedStartOfNight = 0;
    public long rStartOfNight = 0;
    public long rEndOfNight = 0;
    public String rTimezone = null;
    public int rEndReason = 0;
    public long rHeadbandID;                // note this is the cross-link ID into a ZAH_HeadbandRecord
    public int rCountAwakenings = 0;
    public double rTime_Deep_min = 0.0;
    public double rTime_Light_min = 0.0;
    public double rTime_REM_min = 0.0;
    public double rTime_Awake_min = 0.0;
    public double rTime_to_Z_min = 0.0;
    public double rTime_Total_Z_min = 0.0;
    public int rZQ_Score = 0;
    public int rDataSource = 0;
    public int rDisplay_Hypnogram_Count = 0;
    public int rBase_Hypnogram_Count = 0;
    public byte[] rDisplay_Hypnogram = null;
    public byte[] rBase_Hypnogram = null;

    // the following fields are part of the "extended" and full sleep record stored in the Zep App database
    public boolean mHasExtended = false;
    public long rUploaded_timestamp = 0;
    public long rClockOffset = 0;
    public boolean rHidden = false;
    public long rDeepSum = 0;
    public long rDisplayHypnogramStartTime = 0;
    public int rInsufficientData = 0;
    public long rInsufficientDataStartTime = 0;
    public double rLightChangedToDeep_min = 0.0;
    public int rSleepValid = 0;
    public long rStartOfNightMyZeo = 0;
    public long rStartOfNightOrig = 0;
    public int rValid = 0;
    public int rValidForHistory = 0;
    public double rVoltageBattery = 0.0;

    public static final String UPLOADED_ON = "uploaded_on";
    public static final String CLOCK_OFFSET = "clock_offset";
    public static final String HIDDEN = "hidden";
    public static final String DEEP_SUM = "deep_sum";
    public static final String DISPLAY_HYPNOGRAM_STARTTIME = "display_hypnogram_start_time";
    public static final String INSUFFICIENT_DATA = "insufficient_data";
    public static final String INSUFFICIENT_DATA_STARTIME = "insufficient_data_start_time";
    public static final String LIGHT_CHANGED_TO_DEEP = "light_changed_to_deep";
    public static final String SLEEP_VALID = "sleep_valid";
    public static final String START_OF_NIGHT_MYZEO = "start_of_night_myzeo";
    public static final String START_OF_NIGHT_ORIG = "start_of_night_orig";
    public static final String VALID = "valid";
    public static final String VALID_FOR_HISTORY = "valid_for_history";
    public static final String VOLTAGE_BATTERY = "voltage_battery";

    // member constants and other static content
    public static final String[] ZAH_SLEEPREC_COLS = new String[] {
            ZeoDataContract.SleepRecord._ID,
            ZeoDataContract.SleepRecord.CREATED_ON,
            ZeoDataContract.SleepRecord.UPDATED_ON,
            ZeoDataContract.SleepRecord.SLEEP_EPISODE_ID,
            ZeoDataContract.SleepRecord.LOCALIZED_START_OF_NIGHT,
            ZeoDataContract.SleepRecord.START_OF_NIGHT,
            ZeoDataContract.SleepRecord.END_OF_NIGHT,
            ZeoDataContract.SleepRecord.TIMEZONE,
            ZeoDataContract.SleepRecord.HEADBAND_ID,
            ZeoDataContract.SleepRecord.ZQ_SCORE,
            ZeoDataContract.SleepRecord.AWAKENINGS,
            ZeoDataContract.SleepRecord.TIME_IN_DEEP,
            ZeoDataContract.SleepRecord.TIME_IN_LIGHT,
            ZeoDataContract.SleepRecord.TIME_IN_REM,
            ZeoDataContract.SleepRecord.TIME_IN_WAKE,
            ZeoDataContract.SleepRecord.TIME_TO_Z,
            ZeoDataContract.SleepRecord.TOTAL_Z,
            ZeoDataContract.SleepRecord.SOURCE,
            ZeoDataContract.SleepRecord.END_REASON,
            ZeoDataContract.SleepRecord.DISPLAY_HYPNOGRAM_COUNT,
            ZeoDataContract.SleepRecord.BASE_HYPNOGRAM_COUNT,
            ZeoDataContract.SleepRecord.DISPLAY_HYPNOGRAM,
            ZeoDataContract.SleepRecord.BASE_HYPNOGRAM
    };
    public static final String[] ZAH_SLEEPREC_EXTENDED_COLS = new String[] {
            ZeoDataContract.SleepRecord._ID,
            ZeoDataContract.SleepRecord.CREATED_ON,
            ZeoDataContract.SleepRecord.UPDATED_ON,
            ZeoDataContract.SleepRecord.SLEEP_EPISODE_ID,
            ZeoDataContract.SleepRecord.LOCALIZED_START_OF_NIGHT,
            ZeoDataContract.SleepRecord.START_OF_NIGHT,
            ZeoDataContract.SleepRecord.END_OF_NIGHT,
            ZeoDataContract.SleepRecord.TIMEZONE,
            ZeoDataContract.SleepRecord.HEADBAND_ID,
            ZeoDataContract.SleepRecord.ZQ_SCORE,
            ZeoDataContract.SleepRecord.AWAKENINGS,
            ZeoDataContract.SleepRecord.TIME_IN_DEEP,
            ZeoDataContract.SleepRecord.TIME_IN_LIGHT,
            ZeoDataContract.SleepRecord.TIME_IN_REM,
            ZeoDataContract.SleepRecord.TIME_IN_WAKE,
            ZeoDataContract.SleepRecord.TIME_TO_Z,
            ZeoDataContract.SleepRecord.TOTAL_Z,
            ZeoDataContract.SleepRecord.SOURCE,
            ZeoDataContract.SleepRecord.END_REASON,
            ZeoDataContract.SleepRecord.DISPLAY_HYPNOGRAM_COUNT,
            ZeoDataContract.SleepRecord.BASE_HYPNOGRAM_COUNT,
            ZeoDataContract.SleepRecord.DISPLAY_HYPNOGRAM,
            ZeoDataContract.SleepRecord.BASE_HYPNOGRAM,
            UPLOADED_ON,
            CLOCK_OFFSET,
            HIDDEN,
            DEEP_SUM,
            DISPLAY_HYPNOGRAM_STARTTIME,
            INSUFFICIENT_DATA,
            INSUFFICIENT_DATA_STARTIME,
            LIGHT_CHANGED_TO_DEEP,
            SLEEP_VALID,
            START_OF_NIGHT_MYZEO,
            START_OF_NIGHT_ORIG,
            VALID,
            VALID_FOR_HISTORY,
            VOLTAGE_BATTERY
    };

    public static final int ZAH_ENDREASON_COMPLETE = 0;
    public static final int ZAH_ENDREASON_STILL_ACTIVE = 1;
    public static final int ZAH_ENDREASON_BATTERY_DIED = 2;
    public static final int ZAH_ENDREASON_HEADBAND_DISCONNECT = 3;
    public static final int ZAH_ENDREASON_ANDROID_KILLED = 4;

    public static final int ZAH_HYPNOGRAM_UNDEFINED = 0;
    public static final int ZAH_HYPNOGRAM_WAKE = 1;
    public static final int ZAH_HYPNOGRAM_REM = 2;
    public static final int ZAH_HYPNOGRAM_LIGHT = 3;
    public static final int ZAH_HYPNOGRAM_DEEP = 4;
    public static final int ZAH_HYPNOGRAM_LIGHT_TO_DEEP = 6;

    // constructor
    public ZAH_SleepRecord(Cursor cursor) {
        mHasExtended = false;
        rID = cursor.getInt(cursor.getColumnIndex(ZeoDataContract.SleepRecord._ID));
        rCreated_timestamp = cursor.getLong(cursor.getColumnIndex(ZeoDataContract.SleepRecord.CREATED_ON));
        rUpdated_timestamp = cursor.getLong(cursor.getColumnIndex(ZeoDataContract.SleepRecord.UPDATED_ON));
        rSleepEpisodeID = cursor.getLong(cursor.getColumnIndex(ZeoDataContract.SleepRecord.SLEEP_EPISODE_ID));
        rLocalizedStartOfNight =cursor.getLong(cursor.getColumnIndex(ZeoDataContract.SleepRecord.LOCALIZED_START_OF_NIGHT));
        rStartOfNight = cursor.getLong(cursor.getColumnIndex(ZeoDataContract.SleepRecord.START_OF_NIGHT));
        rEndOfNight = cursor.getLong(cursor.getColumnIndex(ZeoDataContract.SleepRecord.END_OF_NIGHT));
        rTimezone = cursor.getString(cursor.getColumnIndex(ZeoDataContract.SleepRecord.TIMEZONE));
        rEndReason = cursor.getInt(cursor.getColumnIndex(ZeoDataContract.SleepRecord.END_REASON));
        rHeadbandID = cursor.getLong(cursor.getColumnIndex(ZeoDataContract.SleepRecord.HEADBAND_ID));
        rCountAwakenings = cursor.getInt(cursor.getColumnIndex(ZeoDataContract.SleepRecord.AWAKENINGS));
        rTime_Deep_min = ((double)cursor.getInt(cursor.getColumnIndex(ZeoDataContract.SleepRecord.TIME_IN_DEEP))) / 2.0;
        rTime_Light_min = ((double)cursor.getInt(cursor.getColumnIndex(ZeoDataContract.SleepRecord.TIME_IN_LIGHT))) / 2.0;
        rTime_REM_min = ((double)cursor.getInt(cursor.getColumnIndex(ZeoDataContract.SleepRecord.TIME_IN_REM))) / 2.0;
        rTime_Awake_min = ((double)cursor.getInt(cursor.getColumnIndex(ZeoDataContract.SleepRecord.TIME_IN_WAKE))) / 2.0;
        rTime_to_Z_min = ((double)cursor.getInt(cursor.getColumnIndex(ZeoDataContract.SleepRecord.TIME_TO_Z))) / 2.0;
        rTime_Total_Z_min = ((double)cursor.getInt(cursor.getColumnIndex(ZeoDataContract.SleepRecord.TOTAL_Z))) / 2.0;
        rZQ_Score = cursor.getInt(cursor.getColumnIndex(ZeoDataContract.SleepRecord.ZQ_SCORE));
        rDataSource = cursor.getInt(cursor.getColumnIndex(ZeoDataContract.SleepRecord.SOURCE));
        rDisplay_Hypnogram_Count = cursor.getInt(cursor.getColumnIndex(ZeoDataContract.SleepRecord.DISPLAY_HYPNOGRAM_COUNT));
        rBase_Hypnogram_Count = cursor.getInt(cursor.getColumnIndex(ZeoDataContract.SleepRecord.BASE_HYPNOGRAM_COUNT));
        rDisplay_Hypnogram = cursor.getBlob(cursor.getColumnIndex(ZeoDataContract.SleepRecord.DISPLAY_HYPNOGRAM));
        rBase_Hypnogram = cursor.getBlob(cursor.getColumnIndex(ZeoDataContract.SleepRecord.BASE_HYPNOGRAM));

        // the following fields are accessible regardless of the ZeoDataContract
        int i = cursor.getColumnIndex(UPLOADED_ON);
        if (i >= 0) { rUploaded_timestamp = cursor.getLong(i); mHasExtended = true; }
        else { rUploaded_timestamp = -1; }
        i = cursor.getColumnIndex(CLOCK_OFFSET);
        if (i >= 0) { rClockOffset = cursor.getInt(i); mHasExtended = true; }
        else { rClockOffset = 0; }
        i = cursor.getColumnIndex(HIDDEN);
        if (i >= 0) { rHidden = (cursor.getInt(i) != 0); mHasExtended = true; }
        else { rHidden = false; }
        i = cursor.getColumnIndex(DEEP_SUM);
        if (i >= 0) { rDeepSum = cursor.getLong(i); mHasExtended = true; }
        else { rDeepSum = 0L; }
        i = cursor.getColumnIndex(DISPLAY_HYPNOGRAM_STARTTIME);
        if (i >= 0) { rDisplayHypnogramStartTime = cursor.getLong(i); mHasExtended = true; }
        else { rDisplayHypnogramStartTime = 0L; }
        i = cursor.getColumnIndex(INSUFFICIENT_DATA);
        if (i >= 0) { rInsufficientData = cursor.getInt(i); mHasExtended = true; }
        else { rInsufficientData = 0; }
        i = cursor.getColumnIndex(INSUFFICIENT_DATA_STARTIME);
        if (i >= 0) { rInsufficientDataStartTime = cursor.getLong(i); mHasExtended = true; }
        else { rInsufficientDataStartTime = 0L; }
        i = cursor.getColumnIndex(LIGHT_CHANGED_TO_DEEP);
        if (i >= 0) { rLightChangedToDeep_min = ((double)cursor.getInt(i)) / 2.0; mHasExtended = true; }
        else { rLightChangedToDeep_min = 0.0; }
        i = cursor.getColumnIndex(SLEEP_VALID);
        if (i >= 0) { rSleepValid = cursor.getInt(i); mHasExtended = true; }
        else { rSleepValid = 0; }
        i = cursor.getColumnIndex(START_OF_NIGHT_MYZEO);
        if (i >= 0) { rStartOfNightMyZeo = cursor.getLong(i) * 1000L; mHasExtended = true; }
        else { rStartOfNightMyZeo = 0L; }
        i = cursor.getColumnIndex(START_OF_NIGHT_ORIG);
        if (i >= 0) { rStartOfNightOrig = cursor.getLong(i); mHasExtended = true; }
        else { rStartOfNightOrig = 0L; }
        i = cursor.getColumnIndex(VALID);
        if (i >= 0) { rValid = cursor.getInt(i); mHasExtended = true; }
        else { rValid = 0; }
        i = cursor.getColumnIndex(VALID_FOR_HISTORY);
        if (i >= 0) { rValidForHistory = cursor.getInt(i); mHasExtended = true; }
        else { rValidForHistory = 0; }
        i = cursor.getColumnIndex(VOLTAGE_BATTERY);
        if (i >= 0) { rVoltageBattery = ((double)cursor.getInt(i)) / 100.0; mHasExtended = true; }
        else { rVoltageBattery = 0.0; }
    }

    // destructor to assist garbage collection when held in large multi-nested ListArrays
    public void destroy() {
        rTimezone = null;
        rDisplay_Hypnogram = null;
        rBase_Hypnogram = null;
    }

    // translate the EndReason code into a String
    public String getStatusString() {
        switch (rEndReason) {
            case ZeoDataContract.SleepRecord.END_REASON_COMPLETE:
                return "Complete";
            case ZeoDataContract.SleepRecord.END_REASON_ACTIVE:
                return "Still active";
            default:
                return "Terminated";
        }
    }

}
