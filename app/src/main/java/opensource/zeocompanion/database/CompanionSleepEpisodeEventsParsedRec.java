package opensource.zeocompanion.database;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;

// a parsed record of Event that is normally stored in string format
public class CompanionSleepEpisodeEventsParsedRec {
    // record fields
    public int rSleepStage = 0;
    public long rTimestamp = 0;
    public int rEventNo = 0;
    public String rEventInfo = null;

    // optional non-record fields
    public static final String _CTAG = "SER";

    // constructor #1:  defined values usually by being set by end-user choice
    public CompanionSleepEpisodeEventsParsedRec(int sleepStage, long timestamp, int eventNo, String eventInfo) {
        rSleepStage = sleepStage;
        rTimestamp = timestamp;
        rEventNo = eventNo;
        if (eventInfo == null) { rEventInfo = null; }
        else if (eventInfo.isEmpty()) { rEventInfo = null; }
        else if (!eventInfo.contains(",") && !eventInfo.contains(";")) { rEventInfo = eventInfo; }
    }

    // constructor #2:  parse a string field from the database
    public CompanionSleepEpisodeEventsParsedRec(String fieldStr) {
        String[] splitString = fieldStr.split(";", -1);
        if (splitString.length >= 1) { rSleepStage = Integer.parseInt(splitString[0]); }
        if (splitString.length >= 2) { rTimestamp = Long.parseLong(splitString[1]); }
        if (splitString.length >= 3) { rEventNo = Integer.parseInt(splitString[2]); }
        if (splitString.length >= 4) {
            if (splitString[3] == null) { rEventInfo = null; }
            else if (splitString[3].isEmpty()) { rEventInfo = null; }
            else {  rEventInfo = splitString[3]; }
        }
        else { rEventInfo = null; }
    }

    // build the field storage string for this record entry
    public String getStorageString() {
        String str = rSleepStage + ";" + rTimestamp + ";" + rEventNo + ";";
        if (rEventInfo != null) {
            if (rEventInfo.length() > 0) { str = str  + rEventInfo; }
        }
        return str;
    }

    // build the export string for this record entry; do not include the SleepStage
    public String getExportString(SimpleDateFormat sdf) {
        String str = sdf.format(rTimestamp) + ";" + getEventShortName(rEventNo) + ";";
        if (rEventInfo != null) {
            if (rEventInfo.length() > 0) { str = str  + rEventInfo; }
        }
        return str;
    }
    // build a string of the record's contents for the MainSummary tab and History Detail screen; do not include the SleepStage
    public String getSummaryString() {
        String str = "";
        if (rTimestamp != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
            str = str + sdf.format(new Date(rTimestamp)) + ": ";
        }
        str = str + getEventName(rEventNo);
        if (rEventInfo != null) {
            if (rEventInfo.length() > 0) { str = str + ": " + rEventInfo; }
        }
        return str;
    }

    // get the name for display purposes of an event based upon its event number
    public static String getEventName(int event) {
        switch (event) {
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_GOT_INTO_BED:
                return "Got into bed";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_NOT_YET_SLEEPING:
                return "Not yet sleeping";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_GOING_TO_SLEEP:
                return "Going to sleep";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_STILL_AWAKE:
                return "Still awake";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_WOKEUP:
                return "Woke-up middle of night";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_WOKEUP_DID_SOMETHING:
                return "Woke-up and did something";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_WOKEUP_RETRY_TO_SLEEP:
                return "Retrying to sleep";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_DONE_SLEEPING:
                return "Done sleeping";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_STARTING:
                return "ZeoApp starting";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_RECORDING:
                return "ZeoApp recording";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_ENDING:
                return "ZeoApp ending";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_INJECTED_ZEO_STARTEDSLEEP:
                return "ZeoApp startsleep";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_INJECTED_ZEO_DEEP_SLEEP:
                return "ZeoApp deepSleep";
            default:
                return "Unknown";
        }
    }

    // get the name for export purposes of an event based upon its event number
    public static String getEventShortName(int event) {
        switch (event) {
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_GOT_INTO_BED:
                return "1ToBed";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_NOT_YET_SLEEPING:
                return "1NotTry";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_GOING_TO_SLEEP:
                return "2Going";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_STILL_AWAKE:
                return "2Awake";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_WOKEUP:
                return "3WokeUp";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_WOKEUP_DID_SOMETHING:
                return "3Doing";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_WOKEUP_RETRY_TO_SLEEP:
                return "3Retry";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_DONE_SLEEPING:
                return "4Done";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_STARTING:
                return "1ZeoStart";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_RECORDING:
                return "1ZeoRec";
            case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_ENDING:
                return "4ZeoEnd";
            default:
                return "Unknown";
        }
    }
}
