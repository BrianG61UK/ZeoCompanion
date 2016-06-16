package opensource.zeocompanion.utility;

import opensource.zeocompanion.database.CompanionSleepEpisodesRec;
import opensource.zeocompanion.zeo.ZAH_SleepRecord;

public class SleepAnalyzer {

    public static void sumEntireNight(CompanionSleepEpisodesRec cseRec, byte[] base_hypnogram) {
        // step through base 30-secomd hypnogram and add up all the component sleep stages
        boolean doingTimeToZ = true;
        cseRec.rAmend_Time_to_Z_min = 0.0;
        cseRec.rAmend_Time_Awake_min = 0.0;
        cseRec.rAmend_Time_REM_min = 0.0;
        cseRec.rAmend_Time_Light_min = 0.0;
        cseRec.rAmend_Time_Deep_min = 0.0;
        double timeUnknown_min = 0.0;
        for (int i = 0; i < base_hypnogram.length; i++) {
            switch (base_hypnogram[i]) {
                case ZAH_SleepRecord.ZAH_HYPNOGRAM_WAKE:
                    if (doingTimeToZ) { cseRec.rAmend_Time_to_Z_min = cseRec.rAmend_Time_to_Z_min + .5; }
                    else { cseRec.rAmend_Time_Awake_min = cseRec.rAmend_Time_Awake_min + .5; }
                    break;
                case ZAH_SleepRecord.ZAH_HYPNOGRAM_REM:
                    cseRec.rAmend_Time_REM_min = cseRec.rAmend_Time_REM_min + .5;
                    doingTimeToZ = false;
                    break;
                case ZAH_SleepRecord.ZAH_HYPNOGRAM_LIGHT:
                    cseRec.rAmend_Time_Light_min = cseRec.rAmend_Time_Light_min + .5;
                    doingTimeToZ = false;
                    break;
                case ZAH_SleepRecord.ZAH_HYPNOGRAM_DEEP:
                    cseRec.rAmend_Time_Deep_min = cseRec.rAmend_Time_Deep_min + .5;
                    doingTimeToZ = false;
                    break;
                case ZAH_SleepRecord.ZAH_HYPNOGRAM_LIGHT_TO_DEEP:
                    cseRec.rAmend_Time_Deep_min = cseRec.rAmend_Time_Deep_min + .5;
                    doingTimeToZ = false;
                    break;
                case ZAH_SleepRecord.ZAH_HYPNOGRAM_UNDEFINED:
                    timeUnknown_min = timeUnknown_min + .5;
                    break;
            }
        }
    }
}
