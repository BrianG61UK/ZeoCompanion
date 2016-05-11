package opensource.zeocompanion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.utility.JournalDataCoordinator;

import com.myzeo.android.api.data.MyZeoExportDataContract;

// a ZeoCompanion sleep episode record
public class CompanionSleepEpisodesRec {
    public static final String _CTAG = "SR";

    // record fields as stored in the database
    public long rID = 0;
    public long rStartOfRecord_Timestamp = 0;
    public long rEndOfRecord_Timestamp = 0;
    public int rStatesFlag = 0;
    public long rZeoSleepEpisode_ID = 0;
    public long rZeoEventStarting_Timestamp = 0;
    public long rZeoEventRecording_Timestamp = 0;
    public long rZeoEventEnding_Timestamp = 0;
    public int rZeoHeadbandBattery_High = 0;
    public int rZeoHeadbandBattery_Low = 0;
    //public int rCountAwakenings = 0;  // not used
    public long rEvent_GotIntoBed_Timestamp = 0;
    public long rEvent_TryingToSleep_Timestamp = 0;
    public long rEvent_OutOfBedDoneSleeping_Timestamp = 0;
    public String rEvents_CSV_string = null;            // may be null if none entered, or if unpacked
    public String rAttributes_Fixed_CSV_string = null;  // may be null if none entered, or if unpacked
    public String rAttributes_Vari_CSV_string = null;   // may be null if none entered, or if unpacked
    public int rAmendedFlags = 0;
    public long rAmend_StartOfNight = 0;
    public long rAmend_EndOfNight = 0;
    public long rAmend_Display_Hypnogram_Starttime = 0;
    public int rAmend_CountAwakenings = 0;
    public double rAmend_Time_to_Z_min = 0.0;
    public double rAmend_Time_Total_Z_min = 0.0;
    public double rAmend_Time_Awake_min = 0.0;
    public double rAmend_Time_REM_min = 0.0;
    public double rAmend_Time_Light_min = 0.0;
    public double rAmend_Time_Deep_min = 0.0;
    public int rAmend_ZQ_Score = 0;
    public double rAmend_LightChangedToDeep_min = 0.0;
    public long rAmend_DeepSum = 0;
    public byte[] rAmend_Display_Hypnogram = null;  // may be null if none amended
    public byte[] rAmend_Base_Hypnogram = null;     // may be null if none amended

    // unpacked contents of the CSV strings; may be null if not yet unpacked
    public ArrayList<CompanionSleepEpisodeEventsParsedRec> mEvents_array = null;
    public ArrayList<CompanionSleepEpisodeInfoParsedRec> mAttribs_Fixed_array = null;
    public ArrayList<CompanionSleepEpisodeInfoParsedRec> mAttribs_Vari_array = null;

    // constructor #1:  create a sparsely populated initial record
    public CompanionSleepEpisodesRec(long startOfRecord) {
        rID = 0;
        rStartOfRecord_Timestamp = startOfRecord;
        rEndOfRecord_Timestamp = 0;
        rStatesFlag = 0;
        rZeoSleepEpisode_ID = 0;
        rZeoEventStarting_Timestamp = 0;
        rZeoEventRecording_Timestamp = 0;
        rZeoEventEnding_Timestamp = 0;
        rZeoHeadbandBattery_High = 0;
        rZeoHeadbandBattery_Low = 0;
        //rCountAwakenings = 0; // not used
        rEvent_GotIntoBed_Timestamp = 0;
        rEvent_TryingToSleep_Timestamp = 0;
        rEvent_OutOfBedDoneSleeping_Timestamp = 0;
        rEvents_CSV_string = null;
        rAttributes_Fixed_CSV_string = null;
        rAttributes_Vari_CSV_string = null;
        mEvents_array = null;
        mAttribs_Fixed_array = null;
        mAttribs_Vari_array = null;
        rAmendedFlags = 0;
        rAmend_StartOfNight = 0;
        rAmend_EndOfNight = 0;
        rAmend_Display_Hypnogram_Starttime = 0;
        rAmend_CountAwakenings = 0;
        rAmend_Time_to_Z_min = 0.0;
        rAmend_Time_Total_Z_min = 0.0;
        rAmend_Time_Awake_min = 0.0;
        rAmend_Time_REM_min = 0.0;
        rAmend_Time_Light_min = 0.0;
        rAmend_Time_Deep_min = 0.0;
        rAmend_ZQ_Score = 0;
        rAmend_LightChangedToDeep_min = 0.0;
        rAmend_DeepSum = 0;
        rAmend_Display_Hypnogram = null;
        rAmend_Base_Hypnogram = null;
    }

    // constructor #2:  parse contents from a database cursor entry
    public CompanionSleepEpisodesRec(Cursor cursor) {
        rID = -1;
        try {   // master Exception catcher
            rID = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes._ID));
            rStartOfRecord_Timestamp = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_START_OF_RECORD_TIMESTAMP));
            rEndOfRecord_Timestamp = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_END_OF_RECORD_TIMESTAMP));
            rStatesFlag = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_STATES_FLAG));
            rZeoSleepEpisode_ID = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_ZEO_SLEEP_EPISODE_ID));
            rZeoEventStarting_Timestamp = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENT_ZEO_STARTING_TIMESTAMP));
            rZeoEventRecording_Timestamp = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENT_ZEO_RECORDING_TIMESTAMP));
            rZeoEventEnding_Timestamp = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENT_ZEO_ENDING_TIMESTAMP));
            //rCountAwakenings = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_CNT_AWAKENINGS));  // not used
            rEvent_GotIntoBed_Timestamp = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENT_GOT_INTO_BED_TIMESTAMP));
            rEvent_TryingToSleep_Timestamp = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENT_TRYING_TO_SLEEP_TIMESTAMP));
            rEvent_OutOfBedDoneSleeping_Timestamp = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENT_OUT_OF_BED_DONE_SLEEPING_TIMESTAMP));

            if (cursor.isNull(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENTS_CSV_STRING))) { rEvents_CSV_string = null; }
            else { rEvents_CSV_string = cursor.getString(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENTS_CSV_STRING)); }
            if (cursor.isNull(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_ATTRIBS_FIXED_CSV_STRING))) { rAttributes_Fixed_CSV_string = null; }
            else { rAttributes_Fixed_CSV_string = cursor.getString(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_ATTRIBS_FIXED_CSV_STRING)); }
            if (cursor.isNull(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_ATTRIBS_VARI_CSV_STRING))) { rAttributes_Vari_CSV_string = null; }
            else { rAttributes_Vari_CSV_string = cursor.getString(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_ATTRIBS_VARI_CSV_STRING)); }

            mEvents_array = null;
            mAttribs_Fixed_array = null;
            mAttribs_Vari_array = null;

            if (ZeoCompanionApplication.mDatabaseHandler.mVersion >= 2) {
                rAmendedFlags = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMENDED));
                rAmend_StartOfNight = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_START_OF_NIGHT));
                rAmend_EndOfNight = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_END_OF_NIGHT));
                rAmend_CountAwakenings = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_AWAKENINGS));
                rAmend_Time_to_Z_min = ((double) cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_TO_Z))) / 2.0;
                rAmend_Time_Total_Z_min = ((double) cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TOTAL_Z))) / 2.0;
                rAmend_Time_Awake_min = ((double) cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_IN_WAKE))) / 2.0;
                rAmend_Time_REM_min = ((double) cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_IN_REM))) / 2.0;
                rAmend_Time_Light_min = ((double) cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_IN_LIGHT))) / 2.0;
                rAmend_Time_Deep_min = ((double) cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_IN_DEEP))) / 2.0;
                rAmend_ZQ_Score = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_ZQ_SCORE));
                rAmend_LightChangedToDeep_min = ((double)cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_LIGHT_CHANGED_TO_DEEP))) / 2.0;
                rAmend_DeepSum = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_DEEP_SUM));

                if (cursor.isNull(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_DISPLAY_HYPNOGRAM))) { rAmend_Display_Hypnogram = null; }
                else { rAmend_Display_Hypnogram = cursor.getBlob(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_DISPLAY_HYPNOGRAM)); }
                if (cursor.isNull(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_BASE_HYPNOGRAM))) { rAmend_Base_Hypnogram = null; }
                else { rAmend_Base_Hypnogram = cursor.getBlob(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_BASE_HYPNOGRAM)); }
            }
            if (ZeoCompanionApplication.mDatabaseHandler.mVersion >= 4) {
                rZeoHeadbandBattery_High = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_HEADBAND_BATTERY_HIGH));
                rZeoHeadbandBattery_Low = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_HEADBAND_BATTERY_LOW));
                rAmend_Display_Hypnogram_Starttime = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_DISPLAY_HYPNOGRAM_STARTTIME));
            }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".CompanionSleepEpisodesRec", e, "CSE ID=" + rID); // automatically posts a Log.e
        }
    }

    // destroy the contents of this record;
    // although highly disputed: assist garbage collection since this class contains large strings and arrays;
    // running memory profiles of the App clearly demonstrates the need and advantage of doing this
    public void destroy() {
        try {   // master Exception catcher
            if (mEvents_array != null) {
                for (CompanionSleepEpisodeEventsParsedRec rec1: mEvents_array) { rec1.rEventInfo = null; }
                mEvents_array.clear();
                mEvents_array = null;
            }
            if (mAttribs_Fixed_array != null) {
                for (CompanionSleepEpisodeInfoParsedRec rec2: mAttribs_Fixed_array) { if (rec2 != null) { rec2.rAttributeExportName = null; rec2.rValue = null; } }
                mAttribs_Fixed_array.clear();
                mAttribs_Fixed_array = null;
            }
            if (mAttribs_Vari_array != null) {
                for (CompanionSleepEpisodeInfoParsedRec rec3: mAttribs_Vari_array) { rec3.rAttributeExportName = null; rec3.rValue = null; }
                mAttribs_Vari_array.clear();
                mAttribs_Vari_array = null;
            }
            rEvents_CSV_string = null;
            rAttributes_Fixed_CSV_string = null;
            rAttributes_Vari_CSV_string = null;
            rAmend_Display_Hypnogram = null;
            rAmend_Base_Hypnogram = null;
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".destroy", e, "CSE ID="+rID); // automatically posts a Log.e
        }
    }

    // save the record to the database; if not already existing it will be added; if already existing it will be updated;
    // if the CSV strings have been unpacked, they will be automatically repacked
    public void saveToDB() {
        try {   // master Exception catcher
            if (mEvents_array != null) { packEventCSVstring(); }
            if (mAttribs_Fixed_array != null || mAttribs_Vari_array != null) { packInfoCSVstrings(); }
            ContentValues values = new ContentValues();
            if (rID != 0)  values.put(CompanionDatabaseContract.CompanionSleepEpisodes._ID, rID);
            values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_START_OF_RECORD_TIMESTAMP, rStartOfRecord_Timestamp);
            values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_END_OF_RECORD_TIMESTAMP, rEndOfRecord_Timestamp);
            values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_STATES_FLAG, rStatesFlag);
            values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_ZEO_SLEEP_EPISODE_ID, rZeoSleepEpisode_ID);
            values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENT_ZEO_STARTING_TIMESTAMP, rZeoEventStarting_Timestamp);
            values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENT_ZEO_RECORDING_TIMESTAMP, rZeoEventRecording_Timestamp);
            values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENT_ZEO_ENDING_TIMESTAMP, rZeoEventEnding_Timestamp);
            //values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_CNT_AWAKENINGS, rCountAwakenings); // not used
            values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENT_GOT_INTO_BED_TIMESTAMP, rEvent_GotIntoBed_Timestamp);
            values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENT_TRYING_TO_SLEEP_TIMESTAMP, rEvent_TryingToSleep_Timestamp);
            values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENT_OUT_OF_BED_DONE_SLEEPING_TIMESTAMP, rEvent_OutOfBedDoneSleeping_Timestamp);

            if (rEvents_CSV_string == null) { values.putNull(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENTS_CSV_STRING); }
            else { values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_EVENTS_CSV_STRING, rEvents_CSV_string); }
            if (rAttributes_Fixed_CSV_string == null) { values.putNull(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_ATTRIBS_FIXED_CSV_STRING); }
            else { values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_ATTRIBS_FIXED_CSV_STRING, rAttributes_Fixed_CSV_string); }
            if (rAttributes_Vari_CSV_string == null) { values.putNull(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_ATTRIBS_VARI_CSV_STRING); }
            else { values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_ATTRIBS_VARI_CSV_STRING, rAttributes_Vari_CSV_string); }

            if (ZeoCompanionApplication.mDatabaseHandler.mVersion > 1) {
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMENDED, rAmendedFlags);
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_START_OF_NIGHT, rAmend_StartOfNight);
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_END_OF_NIGHT, rAmend_EndOfNight);
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_AWAKENINGS, rAmend_CountAwakenings);
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_TO_Z, (int)(rAmend_Time_to_Z_min * 2.0));
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TOTAL_Z, (int)(rAmend_Time_Total_Z_min * 2.0));
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_IN_WAKE, (int)(rAmend_Time_Awake_min * 2.0));
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_IN_REM, (int)(rAmend_Time_REM_min * 2.0));
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_IN_LIGHT, (int)(rAmend_Time_Light_min * 2.0));
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_IN_DEEP, (int)(rAmend_Time_Deep_min * 2.0));
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_ZQ_SCORE, rAmend_ZQ_Score);
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_LIGHT_CHANGED_TO_DEEP, (int)(rAmend_LightChangedToDeep_min * 2.0));
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_DEEP_SUM, rAmend_DeepSum);

                if (rAmend_Display_Hypnogram == null) { values.putNull(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_DISPLAY_HYPNOGRAM);
                } else { values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_DISPLAY_HYPNOGRAM, rAmend_Display_Hypnogram); }
                if (rAmend_Base_Hypnogram == null) { values.putNull(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_BASE_HYPNOGRAM); }
                else { values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_BASE_HYPNOGRAM, rAmend_Base_Hypnogram); }
            }
            if (ZeoCompanionApplication.mDatabaseHandler.mVersion >= 4) {
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_HEADBAND_BATTERY_HIGH, rZeoHeadbandBattery_High);
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_HEADBAND_BATTERY_LOW, rZeoHeadbandBattery_Low);
                values.put(CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_DISPLAY_HYPNOGRAM_STARTTIME, rAmend_Display_Hypnogram_Starttime);
            }

            long result = ZeoCompanionApplication.mDatabaseHandler.insertOrReplaceRecs(CompanionDatabase.CompanionSleepEpisodes_TABLE_NAME, values);
            if (result > 0) {   // errors are already handled by insertOrReplaceRecs
                rID = result;
                Log.d(_CTAG + ".saveToDB", "ID=" + rID + ", ZeoID=" + rZeoSleepEpisode_ID);
                Log.d(_CTAG + ".saveToDB", "Events Zs="+rZeoEventStarting_Timestamp+"; In="+rEvent_GotIntoBed_Timestamp+"; Zr="+rZeoEventRecording_Timestamp+"; Go="+rEvent_GotIntoBed_Timestamp+"; Ze="+rZeoEventEnding_Timestamp+"; Do="+rEvent_OutOfBedDoneSleeping_Timestamp+"; String=" + rEvents_CSV_string);
                Log.d(_CTAG + ".saveToDB", "FixedAttrStr=" + rAttributes_Fixed_CSV_string);
            }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".saveToDB", e, "CSE ID="+rID);    // automatically posts a Log.e
        }
    }

    // remove the indicated SleepEpisode record from the database
    public static void removeFromDB(CompanionDatabase dbh, long id) {
        String where = CompanionDatabaseContract.CompanionSleepEpisodes._ID + "=?";
        String values[] = { String.valueOf(id) };
        dbh.deleteRecs(CompanionDatabase.CompanionSleepEpisodes_TABLE_NAME, where, values);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Record Status methods
    ////////////////////////////////////////////////////////////////////////////

    // get a status code of the CSE record's state
    public int getStatusCode() {
        try {   // master Exception catcher
            if (rEndOfRecord_Timestamp != 0) {
                if ((rStatesFlag & CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_STATESFLAG_JOURNAL_EXPLICITEND) != 0) {
                    return CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_DONE;
                }
                return CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_SOFTDONE;
            }
            int endingStatesFlags = (CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_STATESFLAG_JOURNAL_EXPLICITEND |
                                    CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_STATESFLAG_ZEO_EXPLICITEND);
            if ((rStatesFlag & endingStatesFlags) != 0) { return CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_SOFTDONE; }
            if (doEventsExist()) { return CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_RECORDING; }
            if (doAttributesExist()) { return CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_RECORDING; }
            if (rZeoSleepEpisode_ID != 0) { return CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_JUSTLINKED; }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".getStatusCode", e, "CSE ID=" + rID); // automatically posts a Log.e
        }
        return CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_EMPTY;
    }

    // return a bitmap of the various contents of the record
    public long getContentsBitmap() {
        boolean checkDeeper = true;
        long bits = 0x00000000;
        if (rID > 0L) { bits = (bits | 0x00000001); }      // has been saved to DB
        if (rZeoSleepEpisode_ID > 0L) { bits = (bits | 0x00000002); }  // is linked to a Zeo Sleep Record
        if ((rAmendedFlags & CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_AMENDED_FLAGS_AMENDED) != 0) { bits = (bits | 0x00000004); }  // is amended
        if (doAttributesExist()) {  bits = (bits | 0x10); }  // has one or more attributes
        if (doEventsExist()) { bits = (bits | 0x00000100); }  // has one or more events
        if (rZeoEventStarting_Timestamp > 0L) { bits = (bits | 0x00001000); }
        if (rZeoEventRecording_Timestamp > 0L) { bits = (bits | 0x00002000); }
        if (rZeoEventEnding_Timestamp > 0L) { bits = (bits | 0x00004000); }
        if (rEvent_GotIntoBed_Timestamp > 0L && rEvent_OutOfBedDoneSleeping_Timestamp > 0L && rEvent_TryingToSleep_Timestamp > 0L) { bits = (bits | 0x00010000); checkDeeper = false; }
        if (rEvents_CSV_string != null) {
            if (!rEvents_CSV_string.isEmpty()) { bits = (bits | 0x00010000); checkDeeper = false; }
        }
        if (mEvents_array != null && checkDeeper) {
           for ( CompanionSleepEpisodeEventsParsedRec eRec: mEvents_array) {
               if (eRec.rEventNo != CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_STARTING &&
                       eRec.rEventNo != CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_RECORDING &&
                       eRec.rEventNo != CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_ENDING) {
                   bits = (bits | 0x00010000);
                   break;
               }
           }
        }
        return bits;
    }

    // get a status code string of the CSE record's state
    public String getStatusString() {
        switch (getStatusCode()) {
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_EMPTY:
                return "Empty";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_JUSTLINKED:
                return "Just linked";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_RECORDING:
                return "Record";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_SOFTDONE:
                return "Soft done";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_DONE:
                return "Done";
        }
        return "Unknown";
    }

    ////////////////////////////////////////////////////////////////////////////
    // Events management methods
    ////////////////////////////////////////////////////////////////////////////

    // unpack the Events from String to ArrayList; events are automatically added for those that just recorded into the record proper;
    // note that if there are no events, the unpack process will still leave a non-null yet empty mEvents_array to signal an unpack was performed
    public  void unpackEventCSVstring() {
        try {   // master Exception catcher
            if (mEvents_array != null) { return; }  // already unpacked

            mEvents_array = new ArrayList<CompanionSleepEpisodeEventsParsedRec>();
            if (rZeoEventStarting_Timestamp != 0) { mEvents_array.add(new CompanionSleepEpisodeEventsParsedRec(CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED, rZeoEventStarting_Timestamp,
                                                                    CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_STARTING, "")); }
            if (rEvent_GotIntoBed_Timestamp != 0) { mEvents_array.add(new CompanionSleepEpisodeEventsParsedRec(CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED, rEvent_GotIntoBed_Timestamp,
                                                                    CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_GOT_INTO_BED, "")); }
            if (rZeoEventRecording_Timestamp != 0) { mEvents_array.add(new CompanionSleepEpisodeEventsParsedRec(CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED, rZeoEventRecording_Timestamp,
                                                                    CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_RECORDING, "")); }
            if (rEvent_TryingToSleep_Timestamp != 0) { mEvents_array.add(new CompanionSleepEpisodeEventsParsedRec(CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED, rEvent_TryingToSleep_Timestamp,
                                                                    CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_GOING_TO_SLEEP, "")); }
            if (rEvent_OutOfBedDoneSleeping_Timestamp != 0) { mEvents_array.add(new CompanionSleepEpisodeEventsParsedRec(CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER, rEvent_OutOfBedDoneSleeping_Timestamp,
                                                                    CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_DONE_SLEEPING, "")); }
            if (rZeoEventEnding_Timestamp != 0) { mEvents_array.add(new CompanionSleepEpisodeEventsParsedRec(CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER, rZeoEventEnding_Timestamp,
                                                                    CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_ENDING, "")); }

            if (rEvents_CSV_string != null) {
                String parsedStrings[] = rEvents_CSV_string.split(",", -1);
                for (String field : parsedStrings) {
                    if (!field.isEmpty()) {
                        CompanionSleepEpisodeEventsParsedRec newRec = new CompanionSleepEpisodeEventsParsedRec(field);
                        mEvents_array.add(newRec);
                    }
                }
                rEvents_CSV_string = null;
            }

            // sort the events array in ascending timestamp order (oldest to newest)
            if (!mEvents_array.isEmpty()) {
                Collections.sort(mEvents_array, new Comparator<CompanionSleepEpisodeEventsParsedRec>() {
                    @Override
                    public int compare(CompanionSleepEpisodeEventsParsedRec o1, CompanionSleepEpisodeEventsParsedRec o2) {
                        if (o1.rTimestamp > o2.rTimestamp) {
                            return 1;
                        }
                        if (o1.rTimestamp < o2.rTimestamp) {
                            return -1;
                        }
                        return 0;
                    }
                });
            }
        } catch (Exception e) {
                ZeoCompanionApplication.postToErrorLog(_CTAG+".unpackEventCSVstring", e, "CSE ID=" + rID);  // automatically posts a Log.e
        }
    }

    // repack the Events from ArrayList into String; this is normally called only by saveToDB();
    // selected events that are only allowed to occur once are stored in the main record rather than the CSV string
    private void packEventCSVstring() {
        try {   // master Exception catcher
            if (mEvents_array == null) { return; }  // no prior unpack has been performed

            // sort the events array in ascending timestamp order (oldest to newest)
            if (!mEvents_array.isEmpty()) {
                Collections.sort(mEvents_array, new Comparator<CompanionSleepEpisodeEventsParsedRec>() {
                    @Override
                    public int compare(CompanionSleepEpisodeEventsParsedRec o1, CompanionSleepEpisodeEventsParsedRec o2) {
                        if (o1.rTimestamp > o2.rTimestamp) {
                            return 1;
                        }
                        if (o1.rTimestamp < o2.rTimestamp) {
                            return -1;
                        }
                        return 0;
                    }
                });
            }

            // reset the six fixed timestamps
            rZeoEventStarting_Timestamp = 0;
            rEvent_GotIntoBed_Timestamp = 0;
            rZeoEventRecording_Timestamp = 0;
            rEvent_TryingToSleep_Timestamp = 0;
            rEvent_OutOfBedDoneSleeping_Timestamp = 0;
            rZeoEventEnding_Timestamp = 0;

            // are there actually no events?
            if (mEvents_array.isEmpty()) {
                rEvents_CSV_string = null;
                mEvents_array = null;
                return;
            }

            // first pull out the use-earliest fixed timestamps
            for (int pos = 0; pos < mEvents_array.size(); pos++) {
                CompanionSleepEpisodeEventsParsedRec existing = mEvents_array.get(pos);
                switch (existing.rEventNo) {
                    case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_STARTING:
                        if (rZeoEventStarting_Timestamp == 0) {
                            rZeoEventStarting_Timestamp = existing.rTimestamp;
                            existing.rEventNo = CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_NONEVENT_HANDLED;
                        }
                        break;
                    case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_GOT_INTO_BED:
                        if (rEvent_GotIntoBed_Timestamp == 0) {
                            rEvent_GotIntoBed_Timestamp = existing.rTimestamp;
                            existing.rEventNo = CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_NONEVENT_HANDLED;
                        }
                        break;
                    case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_RECORDING:
                        if (rZeoEventRecording_Timestamp == 0) {
                            rZeoEventRecording_Timestamp = existing.rTimestamp;
                            existing.rEventNo = CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_NONEVENT_HANDLED;
                        }
                        break;
                    case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_GOING_TO_SLEEP:
                        if (rEvent_TryingToSleep_Timestamp == 0) {
                            rEvent_TryingToSleep_Timestamp = existing.rTimestamp;
                            existing.rEventNo = CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_NONEVENT_HANDLED;
                        }
                        break;
                }
            }

            // now pull out the use-latest fixed timestamps
            for (int pos = mEvents_array.size() - 1; pos >= 0 ; pos--) {
                CompanionSleepEpisodeEventsParsedRec existing = mEvents_array.get(pos);
                switch (existing.rEventNo) {
                    case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_DONE_SLEEPING:
                        if (rEvent_OutOfBedDoneSleeping_Timestamp == 0) {
                            rEvent_OutOfBedDoneSleeping_Timestamp = existing.rTimestamp;
                            existing.rEventNo = CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_NONEVENT_HANDLED;
                        }
                        break;
                    case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_ENDING:
                        if (rZeoEventEnding_Timestamp == 0) {
                            rZeoEventEnding_Timestamp = existing.rTimestamp;
                            existing.rEventNo = CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_NONEVENT_HANDLED;
                        }
                        break;
                }
            }

            // process all the remaining
            String str = "";
            for (int pos = 0; pos < mEvents_array.size(); pos++) {
                CompanionSleepEpisodeEventsParsedRec existing = mEvents_array.get(pos);
                if (existing.rEventNo != CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_NONEVENT_HANDLED) {
                    if (!str.isEmpty()) { str = str + ","; }
                    str = str + existing.getStorageString();
                }
            }
            if (str.isEmpty()) { rEvents_CSV_string = null; }
            else { rEvents_CSV_string = str;  }
            mEvents_array = null;
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".packEventCSVstring", e, "CSE ID=" + rID);    // automatically posts a Log.e
        }
    }

    // store a new event; this is normally called by the JournalDataCoordinator
    public void storeEvent(CompanionSleepEpisodeEventsParsedRec newRec) {
        try {   // master Exception catcher
            if (mEvents_array == null) { unpackEventCSVstring(); }
            mEvents_array.add(newRec);
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".storeEvent", e, "CSE ID=" + rID + " new Event#=" + newRec.rEventNo); // automatically posts a Log.e
        }
    }

    // check if a matching event already exists; this is necessary due to long-term bugs in Android's pathetic Spinner API
    // this is normally called by the JournalDataCoordinator
    public boolean isNearDuplicateBeforeStoring(CompanionSleepEpisodeEventsParsedRec recToCheck) {
        try {   // master Exception catcher
            if (mEvents_array == null) { unpackEventCSVstring(); }
            for (CompanionSleepEpisodeEventsParsedRec existing: mEvents_array) {
                if (existing.rEventNo == recToCheck.rEventNo) {
                    if (existing.rEventInfo == null) {
                        if (recToCheck.rEventInfo != null) { return false; }
                    } else if (recToCheck.rEventInfo == null) {
                        return false;
                    } else {
                        if (!existing.rEventInfo.equals(recToCheck.rEventInfo)) { return false; }
                    }
                    long deltaTime = recToCheck.rTimestamp - existing.rTimestamp;
                    if (deltaTime > 300000L) { return false; }  // more than 5 minute ago?
                    return true;
                }
            }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".isNearDuplicateBeforeStoring", e, "CSE ID=" + rID + " & new Event#="+recToCheck.rEventNo);   // automatically posts a Log.e
        }
        return false;
    }

    // locate the timestamp of the lastmost event of the sleepStage; if none found then return 0
    public long getSleepStageLastEventTimestamp(int sleepStage) {
        long ts = 0;
        try {   // master Exception catcher
            if (mEvents_array == null) { unpackEventCSVstring(); }
            for (CompanionSleepEpisodeEventsParsedRec existing: mEvents_array) {
                if (existing.rSleepStage == sleepStage) {
                    if (existing.rTimestamp > ts) { ts = existing.rTimestamp; }
                }
            }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".getLastEventTimestamp", e, "CSE ID=" + rID + " & sleepStage=" + sleepStage); // automatically posts a Log.e
        }
        return ts;
    }

    // get the indicated (oldest) event if present
    public CompanionSleepEpisodeEventsParsedRec getEventOldest(int sleepStage, int eventNo) {
        long ts = 0;
        try {   // master Exception catcher
            if (mEvents_array == null) { unpackEventCSVstring(); }
            for (CompanionSleepEpisodeEventsParsedRec existing: mEvents_array) {
                if (existing.rSleepStage == sleepStage && existing.rEventNo == eventNo) {
                    return existing;
                }
            }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".getEventFirst", e, "CSE ID=" + rID + " & sleepStage=" + sleepStage+" & event=" + eventNo); // automatically posts a Log.e
        }
        return null;
    }

    // determine whether the record contains any event information in its various formats
    public boolean doEventsExist() {
        try {   // master Exception catcher
            if (rEvents_CSV_string != null) {
                if (!rEvents_CSV_string.isEmpty()) { return true; }
            }
            if (mEvents_array != null) {
                if (!mEvents_array.isEmpty()) { return true; }
            }
            if (rZeoEventStarting_Timestamp != 0 || rEvent_GotIntoBed_Timestamp != 0 ||
                    rZeoEventRecording_Timestamp != 0 || rEvent_TryingToSleep_Timestamp != 0 ||
                    rEvent_OutOfBedDoneSleeping_Timestamp != 0 || rZeoEventEnding_Timestamp != 0) { return true; }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".doEventsExist", e, "CSE ID=" + rID); // automatically posts a Log.e
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Attributes management methods
    ////////////////////////////////////////////////////////////////////////////

    // unpack the two Info CSV Strings to ArrayLists; for Fixed (slotted) attributes the missing AttributeExportName is automatically inserted
    public void unpackInfoCSVstrings() {
        try {   // master Exception catcher
            if (rAttributes_Fixed_CSV_string == null) {
                // either the info string has already been unpacked, or there are no fixed-slot attributes
                if (mAttribs_Fixed_array == null) {
                    // not yet unpacked and therefore there are no fixed-slot attributes; create an all-null array
                    mAttribs_Fixed_array = new ArrayList<CompanionSleepEpisodeInfoParsedRec>();
                    for (int slot = 0; slot < MyZeoExportDataContract.EXPORT_FIELD_SLOTS_TOTAL; slot++) { mAttribs_Fixed_array.add(null); }
                }
            } else {
                // add attribute records or nulls into the array from the parsed string; add content they contain proper content; add null if not
                mAttribs_Fixed_array = new ArrayList<CompanionSleepEpisodeInfoParsedRec>();
                String parsedStrings[] = rAttributes_Fixed_CSV_string.split(",", -1);
                for (int slot = 0; slot < MyZeoExportDataContract.EXPORT_FIELD_SLOTS_TOTAL; slot++) {
                    if (slot >= parsedStrings.length) {
                        mAttribs_Fixed_array.add(null);
                    } else if (parsedStrings[slot] == null) {
                        mAttribs_Fixed_array.add(null);
                    } else if (parsedStrings[slot].isEmpty()) {
                        mAttribs_Fixed_array.add(null);
                    } else {
                        CompanionSleepEpisodeInfoParsedRec newRec = new CompanionSleepEpisodeInfoParsedRec(slot, parsedStrings[slot]);
                        if (newRec.rLikert != 0.0 || newRec.rValue != null) { mAttribs_Fixed_array.add(newRec); }
                        else { mAttribs_Fixed_array.add(null); }
                    }
                }
               rAttributes_Fixed_CSV_string = null;
            }

            if (rAttributes_Vari_CSV_string == null) {
                // either the info string has already been unpacked, or there are no custom non-fixed-slot attributes
                if (mAttribs_Vari_array == null) {
                    // not yet unpacked and therefore there are no custom non-fixed-slot attributes; create an empty array
                    mAttribs_Vari_array = new ArrayList<CompanionSleepEpisodeInfoParsedRec>();
                }
            } else {
                // there are custom non-slot attributes that have not yet been unpacked; create the empty array and parse the string
                mAttribs_Vari_array = new ArrayList<CompanionSleepEpisodeInfoParsedRec>();
                String parsedStrings[] = rAttributes_Vari_CSV_string.split(",", -1);

                // add attribute records into the array from the parsed string if they contain proper content
                for (String field: parsedStrings) {
                    if (field != null) {
                        if (!field.isEmpty()) {
                            CompanionSleepEpisodeInfoParsedRec newRec = new CompanionSleepEpisodeInfoParsedRec(-1, field);
                            if (newRec.rAttributeExportName != null && (newRec.rLikert != 0.0 || newRec.rValue != null)) { mAttribs_Vari_array.add(newRec); }
                        }
                    }
                }
                rAttributes_Vari_CSV_string = null;
            }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".unpackInfoCSVstrings", e, "CSE ID=" + rID);  // automatically posts a Log.e
        }
    }

    // repack the Infos from their ArrayLists into Strings; this is normally called only by saveToDB();
    // need to make sure to not include a final trailing comma
    public void packInfoCSVstrings() {
        try {   // master Exception catcher
            if (mAttribs_Fixed_array != null) {
                String str = "";
                int m = mAttribs_Fixed_array.size();
                int n = MyZeoExportDataContract.EXPORT_FIELD_SLOTS_TOTAL - 1;
                for (int slot = 0; slot < MyZeoExportDataContract.EXPORT_FIELD_SLOTS_TOTAL; slot++) {
                    if (slot >= m) {
                        if (slot <  n) { str = str + ","; }
                    } else {
                        CompanionSleepEpisodeInfoParsedRec existing = mAttribs_Fixed_array.get(slot);
                        if (existing == null) {
                            if (slot <  n) { str = str + ","; }
                        } else {
                            str = str + existing.getStorageString();
                            if (slot <  n) { str = str + ","; }
                        }
                    }
                }
                if (str.isEmpty()) { rAttributes_Fixed_CSV_string = null; }
                else {rAttributes_Fixed_CSV_string = str;  }
                mAttribs_Fixed_array = null;
            }

            if (mAttribs_Vari_array != null) {
                String str = "";
                for (int pos = 0; pos < mAttribs_Vari_array.size(); pos++) {
                    CompanionSleepEpisodeInfoParsedRec existing = mAttribs_Vari_array.get(pos);
                    if (existing != null) {
                        if (!str.isEmpty()) { str = str + ","; }
                        str = str + existing.getStorageString();
                    }
                }
                if (str.isEmpty()) { rAttributes_Vari_CSV_string = null; }
                else {rAttributes_Vari_CSV_string = str;  }
                mAttribs_Vari_array = null;
            }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".packInfoCSVstrings", e, "CSE ID=" + rID);    // automatically posts a Log.e
        }
    }

    // determine whether the record contains any attribute information in its various formats
    public boolean doAttributesExist() {
        try {   // master Exception catcher
            if (mAttribs_Vari_array != null) {
                if (!mAttribs_Vari_array.isEmpty()) { return true; }
            }
            if (rAttributes_Vari_CSV_string != null) {
                if (!rAttributes_Vari_CSV_string.isEmpty()) { return true; }
            }

            if (mAttribs_Fixed_array != null) {
                if (!mAttribs_Fixed_array.isEmpty()) {
                    for (CompanionSleepEpisodeInfoParsedRec aRec: mAttribs_Fixed_array) {
                        if (aRec != null) { return true; }
                    }
                }
            }
            if (rAttributes_Fixed_CSV_string != null) {
                if (!rAttributes_Fixed_CSV_string.isEmpty()) {
                    String splitStr[] = rAttributes_Fixed_CSV_string.split(",");
                    if (splitStr.length > 0) {
                        for (String str: splitStr) {
                            if (str != null) {
                                if (!str.isEmpty()) { return true; }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".doAttributesExist", e, "CSE ID=" + rID); // automatically posts a Log.e
        }
        return false;
    }

    // get a list of all Attribute/Value pairings stored in the record; used by the Summary Fragment
    public ArrayList<String> getAttribsSummaryStrings(int sleepStage) {
        ArrayList<String> theList = new ArrayList<String>();
        try {   // master Exception catcher
            if (mAttribs_Fixed_array == null || mAttribs_Vari_array == null ) { unpackInfoCSVstrings(); }
            if (mAttribs_Fixed_array != null) {
                for (CompanionSleepEpisodeInfoParsedRec infoRec: mAttribs_Fixed_array) {
                    if (infoRec != null) {
                        if (infoRec.rSleepStage == sleepStage) {
                            theList.add(infoRec.getSummaryString());
                        }
                    }
                }
            }
            if (mAttribs_Vari_array != null) {
                for (CompanionSleepEpisodeInfoParsedRec infoRec: mAttribs_Vari_array) {
                    if (infoRec != null) {
                        if (infoRec.rSleepStage == sleepStage) {
                            theList.add(infoRec.getSummaryString());
                        }
                    }
                }
            }

        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".getAttribsSummaryStrings", e, "CSE ID=" + rID + " & SleepStage="+sleepStage);    // automatically posts a Log.e
        }
        return theList;
    }

    // get the currently stored value for an Attribute; returns an empty string if the attribute has no value yet
    public String getExistingAtrributeValue(int sleepStage, CompanionAttributesRec typeRec) {
        try {   // master Exception catcher
            if ((typeRec.rFlags & CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FIXED_SLOT) != 0) {
                // look in fixed-slot attributes area
                if (mAttribs_Fixed_array != null) {
                    if (mAttribs_Fixed_array.size() <= typeRec.rExportSlot) { return ""; }
                    CompanionSleepEpisodeInfoParsedRec infoRec = mAttribs_Fixed_array.get(typeRec.rExportSlot);
                    if (infoRec == null) { return ""; }
                    if (infoRec.rValue == null) { return ""; }
                    return infoRec.rValue;
                } else {
                    if (rAttributes_Fixed_CSV_string != null) {
                        if (!rAttributes_Fixed_CSV_string.isEmpty()) {
                            String splitString[] = rAttributes_Fixed_CSV_string.split(",", -1);
                            if (splitString.length < typeRec.rExportSlot) { return ""; }
                            if (splitString[typeRec.rExportSlot] == null) { return ""; }
                            if (splitString[typeRec.rExportSlot].isEmpty()) { return ""; }
                            CompanionSleepEpisodeInfoParsedRec newRec = new CompanionSleepEpisodeInfoParsedRec(0, splitString[typeRec.rExportSlot]);
                            if (newRec.rValue == null) { return ""; }
                            return newRec.rValue;
                        }
                    }
                }
            } else {
                // look in custom non-fixed-slot attributes area
                if (mAttribs_Vari_array != null) {
                    for (CompanionSleepEpisodeInfoParsedRec infoRec: mAttribs_Vari_array) {
                        if (infoRec.rAttributeExportName.equals(typeRec.rExportSlotName)) {
                            if (infoRec.rValue == null) { return ""; }
                            return infoRec.rValue;
                        }
                    }
                } else {
                    if (rAttributes_Vari_CSV_string != null) {
                        if (!rAttributes_Vari_CSV_string.isEmpty()) {
                            int pos = rAttributes_Vari_CSV_string.indexOf(";"+typeRec.rExportSlotName+";", 0);
                            if (pos > 0) {
                                pos = pos + typeRec.rExportSlotName.length() + 2;
                                int end = rAttributes_Vari_CSV_string.indexOf(";", pos);
                                if (end < 0) { return  rAttributes_Vari_CSV_string.substring(pos); }
                                if (end == 0) { return ""; }
                                return  rAttributes_Vari_CSV_string.substring(pos, end);   // remember 'end' is really 'end-1'
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".getExistingAtrributeValue", e, "CSE ID=" + rID + " & Attribute="+typeRec.rAttributeDisplayName); // automatically posts a Log.e
        }
        return "";
    }

    // store an attribute=value pair; if the attribute already exists, it is replaced with this new value;
    // method handles placement of the attribute/value pair into either Fixed or Vari sections depending upon the attribute's definitions
    public void storeAttributeValue(int sleepStage, CompanionAttributesRec typeRec, String value, float likert) {
        try {   // master Exception catcher
            if ((typeRec.rFlags & CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FACTORY_DISABLED) != 0) { return; }
            if (mAttribs_Fixed_array == null || mAttribs_Vari_array == null ) { unpackInfoCSVstrings(); }

            if ((typeRec.rFlags & CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FIXED_SLOT) != 0) {
                // fixed-slot attribute
                CompanionSleepEpisodeInfoParsedRec newRec = new CompanionSleepEpisodeInfoParsedRec(typeRec.rExportSlot, sleepStage, typeRec.rExportSlotName, value, likert);
                mAttribs_Fixed_array.set(typeRec.rExportSlot, newRec);
            } else {
                // custom non-fixed-slot attribute
                CompanionSleepEpisodeInfoParsedRec newRec = new CompanionSleepEpisodeInfoParsedRec(-1, sleepStage, typeRec.rExportSlotName, value, likert);
                for (int pos = 0; pos < mAttribs_Vari_array.size(); pos++) {
                    CompanionSleepEpisodeInfoParsedRec existingRec = mAttribs_Vari_array.get(pos);
                    if (existingRec != null) {
                        if (existingRec.rAttributeExportName != null) {
                            if (existingRec.rAttributeExportName.equals(typeRec.rExportSlotName)) {
                                mAttribs_Vari_array.set(pos, newRec);
                                return;
                            }
                        }
                    }
                }
                mAttribs_Vari_array.add(newRec);
            }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".storeAttributeValue", e, "CSE ID=" + rID + " & Attribute="+typeRec.rAttributeDisplayName);   // automatically posts a Log.e
        }
    }

    // remove an attribute
    public void removeAttribute(int sleepStage, CompanionAttributesRec typeRec) {
        try {   // master Exception catcher
            if (mAttribs_Fixed_array == null || mAttribs_Vari_array == null ) { unpackInfoCSVstrings(); }
            if ((typeRec.rFlags & CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FIXED_SLOT) != 0) {
                // fixed-slot attribute
                mAttribs_Fixed_array.set(typeRec.rExportSlot, null);
            } else {
                // custom non-fixed-slot attribute
                for (int pos = 0; pos < mAttribs_Vari_array.size(); pos++) {
                    CompanionSleepEpisodeInfoParsedRec existingRec = mAttribs_Vari_array.get(pos);
                    if (existingRec != null) {
                        if (existingRec.rAttributeExportName != null) {
                            if (existingRec.rSleepStage == sleepStage && existingRec.rAttributeExportName.equals(typeRec.rExportSlotName)) {
                                mAttribs_Vari_array.remove(pos);
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG+".removeAttribute", e, "CSE ID=" + rID + " & Attribute="+typeRec.rAttributeDisplayName);   // automatically posts a Log.e
        }
    }
}

