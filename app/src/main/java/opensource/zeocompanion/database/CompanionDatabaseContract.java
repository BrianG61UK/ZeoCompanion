package opensource.zeocompanion.database;

import android.provider.BaseColumns;
import com.myzeo.android.api.data.MyZeoExportDataContract;
import com.myzeo.android.api.data.ZeoDataContract;

public final class CompanionDatabaseContract {
    ////////////////////////////////////////////////////////////////////
    // all of the following are implemented in version 1 of the database
    ////////////////////////////////////////////////////////////////////
    public static final int SLEEP_EPISODE_STAGE_BEFORE = 1;
    public static final int SLEEP_EPISODE_STAGE_INBED = 2;
    public static final int SLEEP_EPISODE_STAGE_GOING = 4;
    public static final int SLEEP_EPISODE_STAGE_DURING = 8;
    public static final int SLEEP_EPISODE_STAGE_AFTER = 16;

    public CompanionDatabaseContract() {}

    public static final String getSleepStageString(int sleepStage) {
        switch (sleepStage) {
            case SLEEP_EPISODE_STAGE_BEFORE:
                return "Before Bed";
            case SLEEP_EPISODE_STAGE_INBED:
                return "Just In Bed";
            case SLEEP_EPISODE_STAGE_GOING:
                return "Going to Sleep";
            case SLEEP_EPISODE_STAGE_DURING:
                return "During Sleep";
            case SLEEP_EPISODE_STAGE_AFTER:
                return "After Sleep";
            default:
                return "Unknown";
        }
    }

    public static final String MANDATORY_TABLE_NAMES_DBVER1[] = {
                CompanionSleepEpisodes.VER1_TABLE_NAME_INTERNAL,
                CompanionAttributes.TABLE_NAME,
                CompanionAttributeValues.TABLE_NAME,
                CompanionEventDoings.TABLE_NAME
    };

    public static final String MANDATORY_TABLE_NAMES_DBVER2[] = {
            CompanionSleepEpisodes.TABLE_NAME_INTERNAL,
            CompanionAttributes.TABLE_NAME,
            CompanionAttributeValues.TABLE_NAME,
            CompanionEventDoings.TABLE_NAME
    };

    public static final String MANDATORY_TABLE_NAMES_DBVER3_4[] = {
            CompanionSleepEpisodes.TABLE_NAME_INTERNAL,
            CompanionAttributes.TABLE_NAME,
            CompanionAttributeValues.TABLE_NAME,
            CompanionEventDoings.TABLE_NAME,
            CompanionSystem.TABLE_NAME,
            CompanionAlerts.TABLE_NAME
    };

    public static final String ZEO_REPLICATE_TABLE_NAMES[] = {
            ZeoHeadbands.TABLE_NAME,
            ZeoSleepEvents.TABLE_NAME,
            ZeoSleepRecords.TABLE_NAME,
            ZeoAlarmAlertEvents.TABLE_NAME,
            ZeoAlarmSnoozeEvents.TABLE_NAME,
            ZeoAlarmTimeoutEvents.TABLE_NAME,
            ZeoActigraphyRecords.TABLE_NAME,
            ZeoAlarms.TABLE_NAME,
            ZeoHeadbandAsserts.TABLE_NAME,
            ZeoHeadbandResets.TABLE_NAME
    };

    public static abstract class CompanionSleepEpisodes implements BaseColumns {
        public static final String VER1_TABLE_NAME_INTERNAL = "sleep_records";  // database version 1 table name
        public static final String TABLE_NAME_INTERNAL = "sleep_journal_records";
        // _ID long integer will be used for all COLUMN_COMPANION_SLEEP_EPISODE_ID in related tables
        public static final String COLUMN_START_OF_RECORD_TIMESTAMP = "sor_timestamp";  // long integer
        public static final String COLUMN_END_OF_RECORD_TIMESTAMP = "eor_timestamp";  // long integer
        public static final String COLUMN_STATES_FLAG = "state_flags";  // integer
        public static final String COLUMN_ZEO_SLEEP_EPISODE_ID = "zeo_sleep_episode_id";    // long integer
        public static final String COLUMN_EVENT_ZEO_STARTING_TIMESTAMP = "evt_zeoStart_timestamp";  // long integer
        public static final String COLUMN_EVENT_ZEO_RECORDING_TIMESTAMP = "evt_zeoRecord_timestamp";  // long integer
        public static final String COLUMN_HEADBAND_BATTERY_HIGH = "zeo_headband_battery_high";  // integer  // added in database Version 4
        public static final String COLUMN_HEADBAND_BATTERY_LOW = "zeo_headband_battery_low";  // long integer   // added in database Version 4
        public static final String COLUMN_EVENT_ZEO_ENDING_TIMESTAMP = "evt_zeoEnd_timestamp";  // long integer
        public static final String COLUMN_CNT_AWAKENINGS = "cnt_awakenings";  // integer
        public static final String COLUMN_EVENT_GOT_INTO_BED_TIMESTAMP = "evt_GIB_timestamp";  // long integer
        public static final String COLUMN_EVENT_TRYING_TO_SLEEP_TIMESTAMP = "evt_TTS_timestamp";  // long integer
        public static final String COLUMN_EVENT_OUT_OF_BED_DONE_SLEEPING_TIMESTAMP = "evt_OOB_timestamp";  // long integer
        public static final String COLUMN_EVENTS_CSV_STRING = "evt_csv_str";  // string
        public static final String COLUMN_ATTRIBS_FIXED_CSV_STRING = "att_fixed_csv_str";  // string
        public static final String COLUMN_ATTRIBS_VARI_CSV_STRING = "att_vari_csv_str";  // string
        public static final String COLUMN_AMENDED = "amended";  // boolean      // all the COLUMN_AMEND* fields below added in database Version 2
        public static final String COLUMN_AMEND_START_OF_NIGHT = "amend_start_of_night";    // long integer
        public static final String COLUMN_AMEND_END_OF_NIGHT = "amend_end_of_night";    // long integer
        public static final String COLUMN_AMEND_DISPLAY_HYPNOGRAM_STARTTIME = "amend_display_hypnogram_starttime";    // long integer   // added in database Version 4
        public static final String COLUMN_AMEND_AWAKENINGS = "amend_awakenings";    // integer
        public static final String COLUMN_AMEND_TIME_TO_Z = "amend_time_to_z";  // integer
        public static final String COLUMN_AMEND_TOTAL_Z = "amend_total_z";  //  integer
        public static final String COLUMN_AMEND_TIME_IN_WAKE = "amend_time_in_wake";  //  integer
        public static final String COLUMN_AMEND_TIME_IN_REM = "amend_time_in_rem";  //  integer
        public static final String COLUMN_AMEND_TIME_IN_LIGHT = "amend_time_in_light";  //  integer
        public static final String COLUMN_AMEND_TIME_IN_DEEP = "amend_time_in_deep";  // integer
        public static final String COLUMN_AMEND_ZQ_SCORE = "zq_score";  // integer  // note: this column was slightly mis-named, but Android SQLite does not offer a rename column; its not worth the effort to fix
        public static final String COLUMN_AMEND_LIGHT_CHANGED_TO_DEEP = "amend_light_changed_to_deep";  // integer
        public static final String COLUMN_AMEND_DEEP_SUM = "amend_deep_sum";    // integer
        public static final String COLUMN_AMEND_DISPLAY_HYPNOGRAM = "amend_display_hypnogram";  // blob
        public static final String COLUMN_AMEND_BASE_HYPNOGRAM = "amend_base_hypnogram";    // blob

        // Note: COLUMN_EVENTS_CSV_STRING field layout:
        //      sleepStage;timestamp;event#;info
        // Note: COLUMN_ATTRIBS_FIXED_CSV_STRING field layouts:
        //      likert;value
        // Note: COLUMN_ATTRIBS_VARI_CSV_STRING field layouts:
        //      sleepStage;attribute;likert;value

        public static final String SQL_DEFINITION = TABLE_NAME_INTERNAL + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_START_OF_RECORD_TIMESTAMP + " INTEGER, " +
                COLUMN_END_OF_RECORD_TIMESTAMP + " INTEGER, " +
                COLUMN_STATES_FLAG + " INTEGER, " +
                COLUMN_ZEO_SLEEP_EPISODE_ID + " INTEGER, " +
                COLUMN_EVENT_ZEO_STARTING_TIMESTAMP + " INTEGER, " +
                COLUMN_EVENT_ZEO_RECORDING_TIMESTAMP + " INTEGER, " +
                COLUMN_EVENT_ZEO_ENDING_TIMESTAMP +  " INTEGER, " +
                COLUMN_HEADBAND_BATTERY_HIGH + " INTEGER, " +
                COLUMN_HEADBAND_BATTERY_LOW + " INTEGER, " +
                COLUMN_CNT_AWAKENINGS + " INTEGER, " +
                COLUMN_EVENT_GOT_INTO_BED_TIMESTAMP + " INTEGER, " +
                COLUMN_EVENT_TRYING_TO_SLEEP_TIMESTAMP + " INTEGER, " +
                COLUMN_EVENT_OUT_OF_BED_DONE_SLEEPING_TIMESTAMP + " INTEGER, " +
                COLUMN_EVENTS_CSV_STRING + " TEXT, " +
                COLUMN_ATTRIBS_FIXED_CSV_STRING + " TEXT, " +
                COLUMN_ATTRIBS_VARI_CSV_STRING + " TEXT, " +
                COLUMN_AMENDED + " INTEGER DEFAULT 0, " +
                COLUMN_AMEND_AWAKENINGS + " INTEGER, " +
                COLUMN_AMEND_START_OF_NIGHT + " INTEGER, " +
                COLUMN_AMEND_END_OF_NIGHT + " INTEGER, " +
                COLUMN_AMEND_DISPLAY_HYPNOGRAM_STARTTIME + " INTEGER, " +
                COLUMN_AMEND_TIME_TO_Z + " INTEGER, " +
                COLUMN_AMEND_TOTAL_Z + " INTEGER, " +
                COLUMN_AMEND_TIME_IN_WAKE + " INTEGER, " +
                COLUMN_AMEND_TIME_IN_REM + " INTEGER, " +
                COLUMN_AMEND_TIME_IN_LIGHT + " INTEGER, " +
                COLUMN_AMEND_TIME_IN_DEEP + " INTEGER, " +
                COLUMN_AMEND_ZQ_SCORE + " INTEGER, " +
                COLUMN_AMEND_LIGHT_CHANGED_TO_DEEP + " INTEGER, " +
                COLUMN_AMEND_DEEP_SUM + " INTEGER, " +
                COLUMN_AMEND_DISPLAY_HYPNOGRAM + " BLOB, " +
                COLUMN_AMEND_BASE_HYPNOGRAM + " BLOB" +
                ")";

        public static final String[] PROJECTION = {
                _ID,
                COLUMN_START_OF_RECORD_TIMESTAMP,
                COLUMN_END_OF_RECORD_TIMESTAMP,
                COLUMN_STATES_FLAG,
                COLUMN_ZEO_SLEEP_EPISODE_ID,
                COLUMN_EVENT_ZEO_STARTING_TIMESTAMP,
                COLUMN_EVENT_ZEO_RECORDING_TIMESTAMP,
                COLUMN_EVENT_ZEO_ENDING_TIMESTAMP,
                COLUMN_HEADBAND_BATTERY_HIGH,
                COLUMN_HEADBAND_BATTERY_LOW,
                COLUMN_CNT_AWAKENINGS,
                COLUMN_EVENT_GOT_INTO_BED_TIMESTAMP,
                COLUMN_EVENT_TRYING_TO_SLEEP_TIMESTAMP,
                COLUMN_EVENT_OUT_OF_BED_DONE_SLEEPING_TIMESTAMP,
                COLUMN_EVENTS_CSV_STRING,
                COLUMN_ATTRIBS_FIXED_CSV_STRING,
                COLUMN_ATTRIBS_VARI_CSV_STRING,
                COLUMN_AMENDED,
                COLUMN_AMEND_AWAKENINGS,
                COLUMN_AMEND_START_OF_NIGHT,
                COLUMN_AMEND_END_OF_NIGHT,
                COLUMN_AMEND_DISPLAY_HYPNOGRAM_STARTTIME,
                COLUMN_AMEND_TIME_TO_Z,
                COLUMN_AMEND_TOTAL_Z,
                COLUMN_AMEND_TIME_IN_WAKE,
                COLUMN_AMEND_TIME_IN_REM,
                COLUMN_AMEND_TIME_IN_LIGHT,
                COLUMN_AMEND_TIME_IN_DEEP,
                COLUMN_AMEND_ZQ_SCORE,
                COLUMN_AMEND_LIGHT_CHANGED_TO_DEEP,
                COLUMN_AMEND_DEEP_SUM,
                COLUMN_AMEND_DISPLAY_HYPNOGRAM,
                COLUMN_AMEND_BASE_HYPNOGRAM
        };

        public static final int SLEEP_EPISODE_STATESFLAG_JOURNAL_EXPLICITSTART = 0x0001;
        public static final int SLEEP_EPISODE_STATESFLAG_ZEO_EXPLICITRECORD = 0x0002;
        public static final int SLEEP_EPISODE_STATESFLAG_JOURNAL_EXPLICITEND = 0x0010;
        public static final int SLEEP_EPISODE_STATESFLAG_ZEO_EXPLICITEND = 0x0020;
        public static final int SLEEP_EPISODE_STATESFLAG_ZEO_DEADREC = 0x0100;
        public static final int SLEEP_EPISODE_STATESFLAG_EXCLUDE_FROM_GRAPHS = 0x1000;

        public static final int SLEEP_EPISODE_NONEVENT_HANDLED = -1;
        public static final int SLEEP_EPISODE_EVENT_GOT_INTO_BED = 10;
        public static final int SLEEP_EPISODE_EVENT_NOT_YET_SLEEPING = 11;
        public static final int SLEEP_EPISODE_EVENT_GOING_TO_SLEEP = 12;
        public static final int SLEEP_EPISODE_EVENT_STILL_AWAKE = 13;
        public static final int SLEEP_EPISODE_EVENT_WOKEUP = 20;
        public static final int SLEEP_EPISODE_EVENT_WOKEUP_DID_SOMETHING = 21;
        public static final int SLEEP_EPISODE_EVENT_WOKEUP_RETRY_TO_SLEEP = 22;
        public static final int SLEEP_EPISODE_EVENT_DONE_SLEEPING = 30;
        public static final int SLEEP_EPISODE_EVENT_ZEO_STARTING = 40;
        public static final int SLEEP_EPISODE_EVENT_ZEO_RECORDING = 41;
        public static final int SLEEP_EPISODE_EVENT_ZEO_ENDING = 42;
        public static final int SLEEP_EPISODE_EVENT_INJECTED_ZEO_STARTEDSLEEP = 51;
        public static final int SLEEP_EPISODE_EVENT_INJECTED_ZEO_DEEP_SLEEP = 52;

        public static final int SLEEP_STATUSCODE_EMPTY = 0;
        public static final int SLEEP_STATUSCODE_JUSTLINKED = 1;
        public static final int SLEEP_STATUSCODE_RECORDING = 2;
        public static final int SLEEP_STATUSCODE_SOFTDONE = 3;
        public static final int SLEEP_STATUSCODE_DONE = 4;

        public static final int SLEEP_AMENDED_FLAGS_NONE = 0x0000;
        public static final int SLEEP_AMENDED_FLAGS_CHECKED = 0x0001;
        public static final int SLEEP_AMENDED_FLAGS_AMENDED = 0x0002;
        public static final int SLEEP_AMENDED_FLAGS_AMENDED_V1 = 0x0100;
    }

    public static abstract class CompanionAttributes implements BaseColumns {
        public static final String TABLE_NAME = "attributes";
        public static final String COLUMN_ATTRIBUTE_DISPLAY_NAME = "attribute_display_name";  // string
        public static final String COLUMN_APPLIES_TO_STAGE = "applies_to_stage";  // integer
        public static final String COLUMN_DISPLAY_ORDER= "disp_order";  // integer
        public static final String COLUMN_FLAGS = "flags";  // integer
        public static final String COLUMN_EXPORT_SLOT = "export_slot";  // integer
        public static final String COLUMN_EXPORT_SLOT_NAME = "export_slot_title";  // string

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                COLUMN_ATTRIBUTE_DISPLAY_NAME + " TEXT PRIMARY KEY NOT NULL, " +
                COLUMN_APPLIES_TO_STAGE + " INTEGER, " +
                COLUMN_DISPLAY_ORDER + " INTEGER, " +
                COLUMN_FLAGS + " INTEGER, " +
                COLUMN_EXPORT_SLOT + " INTEGER, " +
                COLUMN_EXPORT_SLOT_NAME + " TEXT" +
                ")";

        public static final String[] PROJECTION = {
                COLUMN_ATTRIBUTE_DISPLAY_NAME,
                COLUMN_APPLIES_TO_STAGE,
                COLUMN_DISPLAY_ORDER,
                COLUMN_FLAGS,
                COLUMN_EXPORT_SLOT,
                COLUMN_EXPORT_SLOT_NAME
        };

        public static final int COMPANION_ATTRIBUTES_FLAG_VISIBLE = 0x01;
        public static final int COMPANION_ATTRIBUTES_FLAG_FIXED_SLOT = 0x02;
        public static final int COMPANION_ATTRIBUTES_FLAG_FACTORY_NORENAME = 0x20;
        public static final int COMPANION_ATTRIBUTES_FLAG_FACTORY_DISABLED = 0x40;
        public static final int COMPANION_ATTRIBUTES_FLAG_FACTORY_DEFAULT = 0x80;

        public static final int COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED     = COMPANION_ATTRIBUTES_FLAG_FACTORY_DEFAULT+COMPANION_ATTRIBUTES_FLAG_FIXED_SLOT+COMPANION_ATTRIBUTES_FLAG_FACTORY_NORENAME+COMPANION_ATTRIBUTES_FLAG_VISIBLE;
        public static final int COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_FIXED   = COMPANION_ATTRIBUTES_FLAG_FACTORY_DEFAULT+COMPANION_ATTRIBUTES_FLAG_FIXED_SLOT+COMPANION_ATTRIBUTES_FLAG_FACTORY_NORENAME;
        public static final int COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_CUSTOM    = COMPANION_ATTRIBUTES_FLAG_FACTORY_DEFAULT+COMPANION_ATTRIBUTES_FLAG_FIXED_SLOT+COMPANION_ATTRIBUTES_FLAG_VISIBLE;
        public static final int COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_CUSTOM  = COMPANION_ATTRIBUTES_FLAG_FACTORY_DEFAULT+COMPANION_ATTRIBUTES_FLAG_FIXED_SLOT;

        public static final String EXPORT_FIELD_EXPORTNAME_DAY_FEEL2 = "Feeling";
        public static final String EXPORT_FIELD_EXPORTNAME_SSCF01 = "Sick";
        public static final String EXPORT_FIELD_EXPORTNAME_SSCF02 = "Pain";
        public static final String EXPORT_FIELD_EXPORTNAME_SSCF03 = "Light";
        public static final String EXPORT_FIELD_EXPORTNAME_SSCF04 = "Humid";
        public static final String EXPORT_FIELD_EXPORTNAME_SSCF11 = "HSF";
        public static final String EXPORT_FIELD_EXPORTNAME_SSCF12 = "Seriphos";
        public static final String EXPORT_FIELD_EXPORTNAME_SSCF13 = "GABA";
        public static final String EXPORT_FIELD_EXPORTNAME_SSCF14 = "Glycine";
        public static final String EXPORT_FIELD_EXPORTNAME_SSCF15 = "L-Theanine";
        public static final String EXPORT_FIELD_EXPORTNAME_SSCF16 = "Taurine";
        public static final String EXPORT_FIELD_EXPORTNAME_SSCF17 = "Melatonin";

        public static final String SQL_INSERTS[] = {
                "'Notes', "+SLEEP_EPISODE_STAGE_AFTER+", 1, "+0xE2+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_NOTES+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_NOTES+"'",

                "'Sleep Quality', "         +SLEEP_EPISODE_STAGE_AFTER+", 1, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_MORNING_FEEL+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_MORNING_FEEL+"'",
                "'Feeling After Sleep', "   +SLEEP_EPISODE_STAGE_AFTER+", 2, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_DAY_FEEL2+", '"+ EXPORT_FIELD_EXPORTNAME_DAY_FEEL2+"'",
                "'Overnight Dreams', "      +SLEEP_EPISODE_STAGE_AFTER+", 3, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_DREAMS+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_DREAMS+"'",
                "'Overactive Mind', "       +SLEEP_EPISODE_STAGE_AFTER+", 4, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_FALL_ASLEEP+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_FALL_ASLEEP+"'",
                "'Daytime Mood', "          +SLEEP_EPISODE_STAGE_AFTER+", 5, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_DAY_FEEL1+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_DAY_FEEL1+"'",
                "'Daytime Energy', "        +SLEEP_EPISODE_STAGE_AFTER+", 6, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_DAY_FEEL3+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_DAY_FEEL3+"'",

                "'Sleepiness', "            +SLEEP_EPISODE_STAGE_BEFORE+", 1, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SLEEPINESS+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_SLEEPINESS+"'",

                "'Healthy Sleep Formula', " +SLEEP_EPISODE_STAGE_BEFORE+", 2, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF11+", '"+ EXPORT_FIELD_EXPORTNAME_SSCF11+"'",
                "'HSF: Seriphos', "         +SLEEP_EPISODE_STAGE_BEFORE+", 3, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF12+", '"+ EXPORT_FIELD_EXPORTNAME_SSCF12+"'",
                "'HSF: GABA', "             +SLEEP_EPISODE_STAGE_BEFORE+", 4, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF13+", '"+ EXPORT_FIELD_EXPORTNAME_SSCF13+"'",
                "'HSF: Glycine', "          +SLEEP_EPISODE_STAGE_BEFORE+", 5, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF14+", '"+ EXPORT_FIELD_EXPORTNAME_SSCF14+"'",
                "'HSF: L-Theanine', "       +SLEEP_EPISODE_STAGE_BEFORE+", 6, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF15+", '"+ EXPORT_FIELD_EXPORTNAME_SSCF15+"'",
                "'HSF: Taurine', "          +SLEEP_EPISODE_STAGE_BEFORE+", 7, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF16+", '"+ EXPORT_FIELD_EXPORTNAME_SSCF16+"'",
                "'HSF: Melatonin', "        +SLEEP_EPISODE_STAGE_BEFORE+", 8, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF17+", '"+ EXPORT_FIELD_EXPORTNAME_SSCF17+"'",
                "'Caffeine-after 3pm', "    +SLEEP_EPISODE_STAGE_BEFORE+", 9, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_CAFFEINE+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_CAFFEINE+"'",
                "'Alcohol-evening', "       +SLEEP_EPISODE_STAGE_BEFORE+", 10, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_ALCOHOL+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_ALCOHOL+"'",

                "'Online-last hour', "          +SLEEP_EPISODE_STAGE_BEFORE+", 11, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_ACTIVITY_LEVEL+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_ACTIVITY_LEVEL+"'",
                "'Restroom before bed', "       +SLEEP_EPISODE_STAGE_BEFORE+", 12, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_RESTROOM+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_RESTROOM+"'",
                "'Bright Light-last hour', "    +SLEEP_EPISODE_STAGE_BEFORE+", 13, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF03+", '"+ EXPORT_FIELD_EXPORTNAME_SSCF03+"'",

                "'Exercise', "              +SLEEP_EPISODE_STAGE_BEFORE+", 14, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_EXERCISE+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_EXERCISE+"'",
                "'Sickness', "              +SLEEP_EPISODE_STAGE_BEFORE+", 15, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF01+", '"+ EXPORT_FIELD_EXPORTNAME_SSCF01+"'",
                "'Pain', "                  +SLEEP_EPISODE_STAGE_BEFORE+", 16, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF02+", '"+ EXPORT_FIELD_EXPORTNAME_SSCF02+"'",
                "'Hot Flashes', "           +SLEEP_EPISODE_STAGE_BEFORE+", 17, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_HOT_FLASHES+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_HOT_FLASHES+"'",

                "'Mood: Wind Down time', "  +SLEEP_EPISODE_STAGE_BEFORE+", 18, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_WIND_DOWN+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_WIND_DOWN+"'",
                "'Mood: Anticipation', "    +SLEEP_EPISODE_STAGE_BEFORE+", 19, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_ANTICIPATION+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_ANTICIPATION+"'",
                "'Mood: Tension', "         +SLEEP_EPISODE_STAGE_BEFORE+", 20, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_TENSION+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_TENSION+"'",
                "'Mood: Work Day', "        +SLEEP_EPISODE_STAGE_BEFORE+", 21, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_LATE_WORK+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_LATE_WORK+"'",
                "'Mood: Conversations', "   +SLEEP_EPISODE_STAGE_BEFORE+", 22, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_CONVERSATIONS+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_CONVERSATIONS+"'",
                "'Mood: Time before bed', " +SLEEP_EPISODE_STAGE_BEFORE+", 23, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_TIME_BEFORE_BED+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_TIME_BEFORE_BED+"'",

                "'Fullness-before bed', "   +SLEEP_EPISODE_STAGE_BEFORE+", 24, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_FULLNESS+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_FULLNESS+"'",
                "'Hunger-before bed', "     +SLEEP_EPISODE_STAGE_BEFORE+", 25, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_HUNGER+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_HUNGER+"'",
                "'Heartburn-before bed', "  +SLEEP_EPISODE_STAGE_BEFORE+", 26, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_HEARTBURN+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_HEARTBURN+"'",
                "'Thirst-before bed', "     +SLEEP_EPISODE_STAGE_BEFORE+", 27, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_THIRST+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_THIRST+"'",

                "'Bedroom', "               +SLEEP_EPISODE_STAGE_BEFORE+", 28, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_BEDROOM+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_BEDROOM+"'",
                "'Bedroom: Temperature', "  +SLEEP_EPISODE_STAGE_BEFORE+", 29, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_TEMPERATURE+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_TEMPERATURE+"'",
                "'Bedroom: Noise', "        +SLEEP_EPISODE_STAGE_BEFORE+", 30, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_NOISE+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_NOISE+"'",
                "'Bedroom: Light', "        +SLEEP_EPISODE_STAGE_BEFORE+", 31, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_LIGHT+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_LIGHT+"'",
                "'Bedroom: Familiar', "     +SLEEP_EPISODE_STAGE_BEFORE+", 32, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_FAMILIAR+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_FAMILIAR+"'",
                "'Bedroom: Disruptions', "  +SLEEP_EPISODE_STAGE_BEFORE+", 33, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_DISRUPTION+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_DISRUPTION+"'",
                "'Bedroom: Humidity', "     +SLEEP_EPISODE_STAGE_BEFORE+", 34, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF04+", '"+ EXPORT_FIELD_EXPORTNAME_SSCF04+"'",
                "'Bed: Comfort', "          +SLEEP_EPISODE_STAGE_BEFORE+", 35, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_TYPICAL_FIXED+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_COMFORT+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_COMFORT+"'",

                "'Custom5', "               +SLEEP_EPISODE_STAGE_BEFORE+", 36, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF05+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_SSCF05+"'",
                "'Custom6', "               +SLEEP_EPISODE_STAGE_BEFORE+", 37, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF06+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_SSCF06+"'",
                "'Custom7', "               +SLEEP_EPISODE_STAGE_BEFORE+", 38, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF07+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_SSCF07+"'",
                "'Custom8', "               +SLEEP_EPISODE_STAGE_BEFORE+", 39, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF08+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_SSCF08+"'",
                "'Custom9', "               +SLEEP_EPISODE_STAGE_BEFORE+", 40, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF09+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_SSCF09+"'",
                "'Custom10', "              +SLEEP_EPISODE_STAGE_BEFORE+", 41, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF10+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_SSCF10+"'",
                "'Custom18', "              +SLEEP_EPISODE_STAGE_BEFORE+", 42, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF18+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_SSCF18+"'",
                "'Custom19', "              +SLEEP_EPISODE_STAGE_BEFORE+", 43, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF19+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_SSCF19+"'",
                "'Custom20', "              +SLEEP_EPISODE_STAGE_BEFORE+", 44, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF20+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_SSCF20+"'",
                "'Custom21', "              +SLEEP_EPISODE_STAGE_BEFORE+", 45, "+COMPANION_ATTRIBUTES_FLAG_FACTORY_INVISIBLE_CUSTOM+", "+ MyZeoExportDataContract.EXPORT_FIELD_SLOT_SSCF21+", '"+ MyZeoExportDataContract.EXPORT_FIELD_COLNAME_SSCF21+"'"
        };
    }

    public static abstract class CompanionAttributeValues implements BaseColumns {
        public static final String TABLE_NAME = "attribute_values";
        public static final String COLUMN_ATTRIBUTE_DISPLAY_NAME = "attribute_display_name";  // string
        public static final String COLUMN_VALUE = "value";  // string
        public static final String COLUMN_VALUE_LIKERT = "likert_value";  // real

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                COLUMN_ATTRIBUTE_DISPLAY_NAME + " TEXT NOT NULL, " +
                COLUMN_VALUE + " TEXT NOT NULL," +
                COLUMN_VALUE_LIKERT + " REAL," +
                "PRIMARY KEY (" + COLUMN_ATTRIBUTE_DISPLAY_NAME + ", " + COLUMN_VALUE + ")" +
                ")";

        public static final String[] PROJECTION = {
                COLUMN_ATTRIBUTE_DISPLAY_NAME,
                COLUMN_VALUE,
                COLUMN_VALUE_LIKERT,
        };

        public static final String SQL_INSERTS[] = {
                "'Sleep Quality', 'Excellent', 5", "'Sleep Quality', 'Good', 3", "'Sleep Quality', 'Fair', 2", "'Sleep Quality', 'Poor', 1",
                "'Feeling After Sleep', 'Well Rested', 5", "'Feeling After Sleep', 'Awake', 3", "'Feeling After Sleep', 'Groggy', 2", "'Feeling After Sleep', 'Terrible', 1",
                "'Overnight Dreams', 'Lucid', 5", "'Overnight Dreams', 'Normal', 3", "'Overnight Dreams', 'Nightmares', 1",
                "'Overactive Mind', 'Quickly Asleep', 5", "'Overactive Mind', 'Relaxed', 3", "'Overactive Mind', 'Turbulent', 1",
                "'Daytime Mood', 'Blissful', 5", "'Daytime Mood', 'Normal', 3", "'Daytime Mood', 'Irritable', 1",
                "'Daytime Energy', 'Energetic', 5", "'Daytime Energy', 'Normal', 3", "'Daytime Energy', 'Tired', 1",

                "'Sleepiness', 'Exhausted', 5", "'Sleepiness', 'Very', 4", "'Sleepiness', 'Moderately', 3", "'Sleepiness', 'Barely', 1",

                "'Healthy Sleep Formula', 'Super dosed', 5", "'Healthy Sleep Formula', 'Std doses', 3", "'Healthy Sleep Formula', 'Under dosed', 2", "'Healthy Sleep Formula', 'Skipped', 1",
                "'HSF: Seriphos', 'Super dosed', 5", "'HSF: Seriphos', 'Std doses', 3", "'HSF: Seriphos', 'Under dosed', 2", "'HSF: Seriphos', 'Skipped', 1",
                "'HSF: GABA', 'Super dosed', 5", "'HSF: GABA', 'Std doses', 3", "'HSF: GABA', 'Under dosed', 2", "'HSF: GABA', 'Skipped', 1",
                "'HSF: Glycine', 'Super dosed', 5", "'HSF: Glycine', 'Std doses', 3", "'HSF: Glycine', 'Under dosed', 2", "'HSF: Glycine', 'Skipped', 1",
                "'HSF: L-Theanine', 'Super dosed', 5", "'HSF: L-Theanine', 'Std doses', 3", "'HSF: L-Theanine', 'Under dosed', 2", "'HSF: L-Theanine', 'Skipped', 1",
                "'HSF: Taurine', 'Super dosed', 5", "'HSF: Taurine', 'Std doses', 3", "'HSF: Taurine', 'Under dosed', 2", "'HSF: Taurine', 'Skipped', 1",
                "'HSF: Melatonin', 'Super dosed', 5", "'HSF: Melatonin', 'Std doses', 3", "'HSF: Melatonin', 'Under dosed', 2", "'HSF: Melatonin', 'Skipped', 1",
                "'Caffeine-after 3pm', 'None', 5", "'Caffeine-after 3pm', 'Some', 3", "'Caffeine-after 3pm', 'Alot', 1",
                "'Alcohol-late evening', 'None', 5", "'Alcohol-evening', 'Some', 3", "'Alcohol-evening', 'Alot', 2", "'Alcohol-evening', 'Too much', 1",

                "'Computer/Tablet/Phone-last hour', 'Yes', 1", "'Computer/Tablet/Phone-last hour', 'No', 3",
                "'Restroom before bed', 'Yes', 3", "'Restroom before bed', 'No', 1",
                "'Bright Light-last hour', 'Dim no blue light', 5", "'Bright Light-last hour', 'Dim', 4", "'Bright Light-last hour', 'Normal', 3", "'Bright Light-last hour', 'Bright white', 2", "'Bright Light-last hour', 'Bright sunlight', 1",
                "'Online-last hour', 'Yes', 1", "'Online-last hour', 'No', 3",

                "'Exercise-late evening', 'None', 4", "'Exercise', 'Some', 3", "'Exercise', 'Alot', 1",
                "'Sickness', 'None', 5", "'Sickness', 'Slight symptoms', 3", "'Sickness', 'Moderate', 2", "'Sickness', 'Very sick', 1",
                "'Pain', 'None', 5", "'Pain', 'Some', 3", "'Pain', 'Alot', 1",
                "'Hot Flashes', 'None', 5", "'Hot Flashes', 'Some', 3", "'Hot Flashes', 'Alot', 1",

                "'Mood: Wind Down time', 'Alot', 5", "'Mood: Wind Down time', 'Some', 3", "'Mood: Wind Down time', 'None', 1",
                "'Mood: Anticipation', 'None', 5", "'Mood: Anticipation', 'Normal', 3", "'Mood: Anticipation', 'Anxious', 2", "'Mood: Anticipation', 'Stressed', 1",
                "'Mood: Tension', 'None', 5", "'Mood: Tension', 'Some', 3", "'Mood: Tension', 'Alot', 1",
                "'Mood: Work Day', 'Day off', 5", "'Mood: Work Day', 'Easy', 4", "'Mood: Work Day', 'Normal', 3", "'Mood: Work Day', 'Hectic', 1", "'Mood: Work Day', 'Worked all night', 1",
                "'Mood: Conversations', 'Romantic', 5", "'Mood: Conversations', 'Pleasant', 3", "'Mood: Conversations', 'Charged', 1",
                "'Mood: Time before bed', 'Alot', 5", "'Mood: Time before bed', 'Some', 3", "'Mood: Time before bed', 'None', 1",

                "'Fullness-before bed', 'None', 5", "'Fullness-before bed', 'Normal', 3", "'Fullness-before bed', 'Over-full', 1",
                "'Hunger-before bed', 'None', 5", "'Hunger-before bed', 'Slight', 3", "'Hunger-before bed', 'Very', 1",
                "'Heartburn-before bed', 'None', 5", "'Heartburn-before bed', 'Slight', 3", "'Heartburn-before bed', 'Alot', 1",
                "'Thirst-before bed', 'None', 5", "'Thirst-before bed', 'Slight', 3", "'Thirst-before bed', 'Very', 1",

                "'Bedroom', 'Okay', 3", "'Bedroom', 'Not okay', 1",
                "'Bedroom: Temperature', 'Cool', 5", "'Bedroom: Temperature', 'Normal', 3", "'Bedroom: Temperature', 'Hot', 1", "'Bedroom: Temperature', 'Very cold', 1",
                "'Bedroom: Noise', 'White sound', 5", "'Bedroom: Noise', 'None', 4", "'Bedroom: Noise', 'Normal', 3", "'Bedroom: Noise', 'Alot', 1",
                "'Bedroom: Light', 'None', 5", "'Bedroom: Light', 'Some', 3", "'Bedroom: Light', 'Alot', 1",
                "'Bedroom: Familiar', 'Very', 5", "'Bedroom: Familiar', 'Normal', 3", "'Bedroom: Familiar', 'Unfamiliar', 1",
                "'Bedroom: Disruptions', 'None', 5", "'Bedroom: Disruptions', 'Normal', 3", "'Bedroom: Disruptions', 'Alot', 1",
                "'Bedroom: Humidity', 'Low', 5", "'Bedroom: Humidity', 'Normal', 3", "'Bedroom: Humidity', 'Very', 1", "'Bedroom: Humidity', 'Too low', 1",
                "'Bed: Comfort', 'Very', 5", "'Bed: Comfort', 'Normal', 3", "'Bed: Comfort', 'Uncomfortable', 1",

                "'Custom5', 'Good', 5", "'Custom5', 'Normal', 3", "'Custom5', 'Bad', 1",
                "'Custom6', 'Good', 5", "'Custom6', 'Normal', 3", "'Custom6', 'Bad', 1",
                "'Custom7', 'Good', 5", "'Custom7', 'Normal', 3", "'Custom7', 'Bad', 1",
                "'Custom8', 'Good', 5", "'Custom8', 'Normal', 3", "'Custom8', 'Bad', 1",
                "'Custom9', 'Good', 5", "'Custom9', 'Normal', 3", "'Custom9', 'Bad', 1",
                "'Custom10', 'Good', 5", "'Custom10', 'Normal', 3", "'Custom10', 'Bad', 1",
                "'Custom18', 'Good', 5", "'Custom18', 'Normal', 3", "'Custom18', 'Bad', 1",
                "'Custom19', 'Good', 5", "'Custom19', 'Normal', 3", "'Custom19', 'Bad', 1",
                "'Custom20', 'Good', 5", "'Custom20', 'Normal', 3", "'Custom20', 'Bad', 1",
                "'Custom21', 'Good', 5", "'Custom21', 'Normal', 3", "'Custom21', 'Bad', 1"
        };
    }

    public static abstract class CompanionEventDoings implements BaseColumns {
        public static final String TABLE_NAME = "event_doings";
        public static final String COLUMN_DOING = "doing";  // string
        public static final String COLUMN_APPLIES_TO_STAGES = "applies_to_stages";  // integer
        public static final String COLUMN_DOING_IS_DEFAULT_PRIORITY = "is_default";  // integer

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                COLUMN_DOING + " TEXT PRIMARY KEY," +
                COLUMN_APPLIES_TO_STAGES + " INTEGER," +
                COLUMN_DOING_IS_DEFAULT_PRIORITY + " INTEGER" +
                ")";

        public static final String[] PROJECTION = {
                COLUMN_DOING,
                COLUMN_APPLIES_TO_STAGES,
                COLUMN_DOING_IS_DEFAULT_PRIORITY
        };

        public static final String SQL_INSERTS[] = {
                "'Going to Bathroom', " + SLEEP_EPISODE_STAGE_DURING + ", 1",
                "'Got Drink', "+SLEEP_EPISODE_STAGE_DURING+", 0",
                "'Reading', "+(SLEEP_EPISODE_STAGE_INBED+SLEEP_EPISODE_STAGE_DURING)+", 1",
                "'Browsing', "+(SLEEP_EPISODE_STAGE_INBED+SLEEP_EPISODE_STAGE_DURING)+", 0",
                "'Gaming', "+(SLEEP_EPISODE_STAGE_INBED+SLEEP_EPISODE_STAGE_DURING)+", 0",
                "'Sex', "+(SLEEP_EPISODE_STAGE_INBED+SLEEP_EPISODE_STAGE_DURING)+", 0"
        };
    }

    ////////////////////////////////////////////////////////////////////
    // all of the following are implemented in version 2 of the database
    ////////////////////////////////////////////////////////////////////
    public static abstract class ZeoHeadbands implements BaseColumns {
        // _ID long integer
        public static final String TABLE_NAME = "headbands";
        public static final String COLUMN_CREATED_ON = ZeoDataContract.Headband.CREATED_ON;
        public static final String COLUMN_UPDATED_ON = ZeoDataContract.Headband.UPDATED_ON;
        public static final String COLUMN_ACTIVE_FORCED = "active_forced";
        public static final String COLUMN_ALGORITHM_MODE = ZeoDataContract.Headband.ALGORITHM_MODE;
        public static final String COLUMN_BLUETOOTH_ADDRESS = ZeoDataContract.Headband.BLUETOOTH_ADDRESS;
        public static final String COLUMN_BLUETOOTH_FRIENDLY_NAME = ZeoDataContract.Headband.BLUETOOTH_FRIENDLY_NAME;
        public static final String COLUMN_BLUETOOTH_LOCKED = "bluetooth_locked";
        public static final String COLUMN_BONDED = ZeoDataContract.Headband.BONDED;
        public static final String COLUMN_CLOCK_OFFSET = ZeoDataContract.Headband.CLOCK_OFFSET;
        public static final String COLUMN_CONNECTED = ZeoDataContract.Headband.CONNECTED;
        public static final String COLUMN_DEMO_MODE = "demo_mode";
        public static final String COLUMN_DOCKED = ZeoDataContract.Headband.DOCKED;
        public static final String COLUMN_FLASH_CALIBRATION_UPDATES = "flash_calibration_updates";
        public static final String COLUMN_FLASH_SAVED_DATE_UPDATES = "flash_saved_data_updates";
        public static final String COLUMN_FLASH_SLEEP_BACKUP_UPDATES = "flash_sleep_backup_updates";
        public static final String COLUMN_HARDWARE_VERSION = "hardware_version";
        public static final String COLUMN_LAST_ALARM_REASON = "last_alarm_reason";
        public static final String COLUMN_LAST_BATTERY_DIED_TIMESPTAMP = "last_battery_died_timestamp";
        public static final String COLUMN_LAST_BOND_TIMESTAMP = "last_bond_timestamp";
        public static final String COLUMN_LAST_CONNECTED_TIMESTAMP = "last_connected_timestamp";
        public static final String COLUMN_LAST_DISCONNECTED_TIMESTAMP = "last_disconneted_timestamp";
        public static final String COLUMN_LAST_DOCKED_TIMESTAMP = "last_docked_timestamp";
        public static final String COLUMN_LAST_FACTORY_RESET_TIMESTAMP = "last_factory_reset_timestamp";
        public static final String COLUMN_LAST_OFFHEAD_TIMESTAMP = "last_offhead_timestamp";
        public static final String COLUMN_LAST_ONHEAD_TIMESTAMP = "last_onhead_timestamp";
        public static final String COLUMN_LAST_SENSOR_USE_TIMESTAMP = "last_sensor_use_reset";
        public static final String COLUMN_LAST_UNBONDED_TIMESTAMP = "last_unbond_timestamp";
        public static final String COLUMN_LAST_UNDOCKED_TIMESTAMP = "last_undocked_timestamp";
        public static final String COLUMN_MODEL = "model";
        public static final String COLUMN_NEED_CLOCK_OFFSET = "need_clock_offset";
        public static final String COLUMN_NEED_TIME_SYNC = "need_time_sync";
        public static final String COLUMN_ON_HEAD = ZeoDataContract.Headband.ON_HEAD;
        public static final String COLUMN_REQUIRES_PIN = "requires_pin";
        public static final String COLUMN_SENSOR_USED = "sensor_used";
        public static final String COLUMN_SERIAL = "serial";
        public static final String COLUMN_SOFTWARE_VERSION = ZeoDataContract.Headband.SW_VERSION;
        public static final String COLUMN_VOLTAGE = "voltage";
        public static final String COLUMN_VOLTAGE_STATUS = "voltage_status";
        public static final String COLUMN_WAS_CHARGED = "was_charged";

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_CREATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_UPDATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_ACTIVE_FORCED + " BOOLEAN," +
                COLUMN_ALGORITHM_MODE + " INTEGER DEFAULT -1," +
                COLUMN_BLUETOOTH_ADDRESS + " TEXT," +
                COLUMN_BLUETOOTH_FRIENDLY_NAME + " TEXT," +
                COLUMN_BLUETOOTH_LOCKED + " BOOLEAN," +
                COLUMN_BONDED + " BOOLEAN DEFAULT 1," +
                COLUMN_CLOCK_OFFSET + " INTEGER DEFAULT 0," +
                COLUMN_CONNECTED + " BOOLEAN," +
                COLUMN_DEMO_MODE + " BOOLEAN," +
                COLUMN_DOCKED + " BOOLEAN," +
                COLUMN_FLASH_CALIBRATION_UPDATES + " INTEGER," +
                COLUMN_FLASH_SAVED_DATE_UPDATES + " INTEGER," +
                COLUMN_FLASH_SLEEP_BACKUP_UPDATES + " INTEGER," +
                COLUMN_HARDWARE_VERSION + " TEXT," +
                COLUMN_LAST_ALARM_REASON + " INTEGER," +
                COLUMN_LAST_BATTERY_DIED_TIMESPTAMP + " INTEGER DEFAULT 0," +
                COLUMN_LAST_BOND_TIMESTAMP + " INTEGER DEFAULT 0," +
                COLUMN_LAST_CONNECTED_TIMESTAMP + " INTEGER DEFAULT 0," +
                COLUMN_LAST_DISCONNECTED_TIMESTAMP + " INTEGER DEFAULT 0," +
                COLUMN_LAST_DOCKED_TIMESTAMP + " INTEGER DEFAULT 0," +
                COLUMN_LAST_FACTORY_RESET_TIMESTAMP + " INTEGER DEFAULT 0," +
                COLUMN_LAST_OFFHEAD_TIMESTAMP + " INTEGER DEFAULT 0," +
                COLUMN_LAST_ONHEAD_TIMESTAMP + " INTEGER DEFAULT 0," +
                COLUMN_LAST_SENSOR_USE_TIMESTAMP + " INTEGER DEFAULT 0," +
                COLUMN_LAST_UNBONDED_TIMESTAMP + " INTEGER DEFAULT 0," +
                COLUMN_LAST_UNDOCKED_TIMESTAMP + " INTEGER DEFAULT 0," +
                COLUMN_MODEL + " TEXT," +
                COLUMN_NEED_CLOCK_OFFSET + " BOOLEAN DEFAULT 1," +
                COLUMN_NEED_TIME_SYNC + " BOOLEAN DEFAULT 1," +
                COLUMN_ON_HEAD + " BOOLEAN," +
                COLUMN_REQUIRES_PIN + " BOOLEAN," +
                COLUMN_SENSOR_USED + " INTEGER," +
                COLUMN_SERIAL + " TEXT," +
                COLUMN_SOFTWARE_VERSION + " TEXT," +
                COLUMN_VOLTAGE + " INTEGER," +
                COLUMN_VOLTAGE_STATUS + " INTEGER," +
                COLUMN_WAS_CHARGED + " BOOLEAN," +
                "UNIQUE ("+COLUMN_BLUETOOTH_ADDRESS+"))";

        public static final String[] PROJECTION_FULL = {
                _ID,
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_ACTIVE_FORCED,
                COLUMN_ALGORITHM_MODE,
                COLUMN_BLUETOOTH_ADDRESS,
                COLUMN_BLUETOOTH_FRIENDLY_NAME,
                COLUMN_BLUETOOTH_LOCKED,
                COLUMN_BONDED,
                COLUMN_CLOCK_OFFSET,
                COLUMN_CONNECTED,
                COLUMN_DEMO_MODE,
                COLUMN_DOCKED,
                COLUMN_FLASH_CALIBRATION_UPDATES,
                COLUMN_FLASH_SAVED_DATE_UPDATES,
                COLUMN_FLASH_SLEEP_BACKUP_UPDATES,
                COLUMN_HARDWARE_VERSION,
                COLUMN_LAST_ALARM_REASON,
                COLUMN_LAST_BATTERY_DIED_TIMESPTAMP,
                COLUMN_LAST_BOND_TIMESTAMP,
                COLUMN_LAST_CONNECTED_TIMESTAMP,
                COLUMN_LAST_DISCONNECTED_TIMESTAMP,
                COLUMN_LAST_DOCKED_TIMESTAMP,
                COLUMN_LAST_FACTORY_RESET_TIMESTAMP,
                COLUMN_LAST_OFFHEAD_TIMESTAMP,
                COLUMN_LAST_ONHEAD_TIMESTAMP,
                COLUMN_LAST_SENSOR_USE_TIMESTAMP,
                COLUMN_LAST_UNBONDED_TIMESTAMP,
                COLUMN_LAST_UNDOCKED_TIMESTAMP,
                COLUMN_MODEL,
                COLUMN_NEED_CLOCK_OFFSET,
                COLUMN_NEED_TIME_SYNC,
                COLUMN_ON_HEAD,
                COLUMN_REQUIRES_PIN,
                COLUMN_SENSOR_USED,
                COLUMN_SERIAL,
                COLUMN_SOFTWARE_VERSION,
                COLUMN_VOLTAGE,
                COLUMN_VOLTAGE_STATUS,
                COLUMN_WAS_CHARGED
        };

        public static final String[] PROJECTION_AVAILABLE = {
                _ID,
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_ALGORITHM_MODE,
                COLUMN_BLUETOOTH_ADDRESS,
                COLUMN_BLUETOOTH_FRIENDLY_NAME,
                COLUMN_BONDED,
                COLUMN_CLOCK_OFFSET,
                COLUMN_CONNECTED,
                COLUMN_DOCKED,
                COLUMN_ON_HEAD,
                COLUMN_SOFTWARE_VERSION
        };
    }
    public static abstract class ZeoSleepEvents implements BaseColumns {
        // _ID long integer
        public static final String TABLE_NAME = "sleep_events";
        public static final String COLUMN_CREATED_ON = ZeoDataContract.SleepEpisode.CREATED_ON;
        public static final String COLUMN_UPDATED_ON = ZeoDataContract.SleepEpisode.UPDATED_ON;
        public static final String COLUMN_TIMEZONE = ZeoDataContract.SleepEpisode.TIMEZONE;
        public static final String COLUMN_START_TIMESTAMP = ZeoDataContract.SleepEpisode.START_TIMESTAMP;
        public static final String COLUMN_END_TIMESTAMP = ZeoDataContract.SleepEpisode.END_TIMESTAMP;

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_CREATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_UPDATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_TIMEZONE + " TEXT," +
                COLUMN_START_TIMESTAMP + " INTEGER," +
                COLUMN_END_TIMESTAMP + " INTEGER," +
                "UNIQUE ("+COLUMN_START_TIMESTAMP+"))";

        public static final String[] PROJECTION_FULL = {
                _ID,
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_TIMEZONE,
                COLUMN_START_TIMESTAMP,
                COLUMN_END_TIMESTAMP
        };

        public static final String[] PROJECTION_AVAILABLE = {
                _ID,
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_TIMEZONE,
                COLUMN_START_TIMESTAMP,
                COLUMN_END_TIMESTAMP
        };
    }
    public static abstract class ZeoSleepRecords implements BaseColumns {
        // _ID long integer
        public static final String TABLE_NAME = "sleep_records";
        public static final String COLUMN_HEADBAND_ID = ZeoDataContract.SleepRecord.HEADBAND_ID;
        public static final String COLUMN_SLEEP_EVENT_ID = ZeoDataContract.SleepRecord.SLEEP_EPISODE_ID;
        public static final String COLUMN_CREATED_ON = ZeoDataContract.SleepRecord.CREATED_ON;
        public static final String COLUMN_UPDATED_ON = ZeoDataContract.SleepEpisode.UPDATED_ON;
        public static final String COLUMN_UPLOADED_ON = "uploaded_on";
        public static final String COLUMN_TIMEZONE = ZeoDataContract.SleepRecord.TIMEZONE;
        public static final String COLUMN_CLOCK_OFFSET = "clock_offset";
        public static final String COLUMN_LOCALIZED_START_OF_NIGHT = ZeoDataContract.SleepRecord.LOCALIZED_START_OF_NIGHT;
        public static final String COLUMN_HIDDEN = "hidden";
        public static final String COLUMN_SOURCE = ZeoDataContract.SleepRecord.SOURCE;
        public static final String COLUMN_AWAKENINGS = ZeoDataContract.SleepRecord.AWAKENINGS;
        public static final String COLUMN_BASE_HYPNOGRAM = ZeoDataContract.SleepRecord.BASE_HYPNOGRAM;
        public static final String COLUMN_BASE_HYPNOGRAM_COUNT = ZeoDataContract.SleepRecord.BASE_HYPNOGRAM_COUNT;
        public static final String COLUMN_DEEP_SUM = "deep_sum";
        public static final String COLUMN_DISPLAY_HYPNOGRAM = ZeoDataContract.SleepRecord.DISPLAY_HYPNOGRAM;
        public static final String COLUMN_DISPLAY_HYPNOGRAM_COUNT = ZeoDataContract.SleepRecord.DISPLAY_HYPNOGRAM_COUNT;
        public static final String COLUMN_DISPLAY_HYPNOGRAM_FORCED_INDEX = "display_hypnogram_forced_index";
        public static final String COLUMN_DISPLAY_HYPNOGRAM_FORCED_STAGE = "display_hypnogram_forced_stage";
        public static final String COLUMN_DISPLAY_HYPNOGRAM_START_TIME = "display_hypnogram_start_time";
        public static final String COLUMN_END_OF_NIGHT = ZeoDataContract.SleepRecord.END_OF_NIGHT;
        public static final String COLUMN_END_REASON = ZeoDataContract.SleepRecord.END_REASON;
        public static final String COLUMN_INSUFFICIENT_DATA = "insufficient_data";
        public static final String COLUMN_INSUFFICIENT_DATA_START_TIME = "insufficient_data_start_time";
        public static final String COLUMN_LIGHT_CHANGED_TO_DEEP = "light_changed_to_deep";
        public static final String COLUMN_SLEEP_RATING = "sleep_rating";
        public static final String COLUMN_SLEEP_VALID = "sleep_valid";
        public static final String COLUMN_START_OF_NIGHT = ZeoDataContract.SleepRecord.START_OF_NIGHT;
        public static final String COLUMN_START_OF_NIGHT_MYZEO = "start_of_night_myzeo";
        public static final String COLUMN_START_OF_NIGHT_ORIG = "start_of_night_orig";
        public static final String COLUMN_TIME_IN_DEEP = ZeoDataContract.SleepRecord.TIME_IN_DEEP;
        public static final String COLUMN_TIME_IN_LIGHT = ZeoDataContract.SleepRecord.TIME_IN_LIGHT;
        public static final String COLUMN_TIME_IN_REM = ZeoDataContract.SleepRecord.TIME_IN_REM;
        public static final String COLUMN_TIME_IN_WAKE = ZeoDataContract.SleepRecord.TIME_IN_WAKE;
        public static final String COLUMN_TIME_TO_Z = ZeoDataContract.SleepRecord.TIME_TO_Z;
        public static final String COLUMN_TOTAL_Z = ZeoDataContract.SleepRecord.TOTAL_Z;
        public static final String COLUMN_VALID = "valid";
        public static final String COLUMN_VALID_FOR_HISTORY = "valid_for_history";
        public static final String COLUMN_VOLTAGE_BATTERY = "voltage_battery";
        public static final String COLUMN_WAKE_TONE = "wake_tone";
        public static final String COLUMN_WAKE_WINDOW = "wake_window";
        public static final String COLUMN_ZEO_WAKE_ON = "zeo_wake_on";
        public static final String COLUMN_ZQ_SCORE = ZeoDataContract.SleepRecord.ZQ_SCORE;

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_HEADBAND_ID + " INTEGER," +
                COLUMN_SLEEP_EVENT_ID + " INTEGER," +
                COLUMN_CREATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_UPDATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_UPLOADED_ON + " INTEGER DEFAULT 0," +
                COLUMN_TIMEZONE + " TEXT," +
                COLUMN_CLOCK_OFFSET + " INTEGER DEFAULT 0," +
                COLUMN_LOCALIZED_START_OF_NIGHT + " INTEGER DEFAULT 0," +
                COLUMN_HIDDEN + " BOOLEAN DEFAULT 0," +
                COLUMN_SOURCE + " INTEGER DEFAULT 0," +
                COLUMN_AWAKENINGS + " INTEGER," +
                COLUMN_BASE_HYPNOGRAM + " BLOB," +
                COLUMN_BASE_HYPNOGRAM_COUNT + " INTEGER," +
                COLUMN_DEEP_SUM + " INTEGER," +
                COLUMN_DISPLAY_HYPNOGRAM + " BLOB," +
                COLUMN_DISPLAY_HYPNOGRAM_COUNT + " INTEGER," +
                COLUMN_DISPLAY_HYPNOGRAM_FORCED_INDEX + " INTEGER," +
                COLUMN_DISPLAY_HYPNOGRAM_FORCED_STAGE + " INTEGER," +
                COLUMN_DISPLAY_HYPNOGRAM_START_TIME + " INTEGER," +
                COLUMN_END_OF_NIGHT + " INTEGER," +
                COLUMN_END_REASON + " INTEGER DEFAULT 0," +
                COLUMN_INSUFFICIENT_DATA + " INTEGER," +
                COLUMN_INSUFFICIENT_DATA_START_TIME + " INTEGER," +
                COLUMN_LIGHT_CHANGED_TO_DEEP + " INTEGER," +
                COLUMN_SLEEP_RATING + " INTEGER," +
                COLUMN_SLEEP_VALID + " INTEGER," +
                COLUMN_START_OF_NIGHT + " INTEGER  DEFAULT 0," +
                COLUMN_START_OF_NIGHT_MYZEO + " INTEGER  DEFAULT 0," +
                COLUMN_START_OF_NIGHT_ORIG + " INTEGER  DEFAULT 0," +
                COLUMN_TIME_IN_DEEP + " INTEGER," +
                COLUMN_TIME_IN_LIGHT + " INTEGER," +
                COLUMN_TIME_IN_REM + " INTEGER," +
                COLUMN_TIME_IN_WAKE + " INTEGER," +
                COLUMN_TIME_TO_Z + " INTEGER," +
                COLUMN_TOTAL_Z + " INTEGER," +
                COLUMN_VALID + " INTEGER," +
                COLUMN_VALID_FOR_HISTORY + " INTEGER," +
                COLUMN_VOLTAGE_BATTERY + " INTEGER," +
                COLUMN_WAKE_TONE + " INTEGER," +
                COLUMN_WAKE_WINDOW + " INTEGER," +
                COLUMN_ZEO_WAKE_ON + " INTEGER," +
                COLUMN_ZQ_SCORE + " INTEGER," +
                "UNIQUE ("+COLUMN_START_OF_NIGHT+"))";
                // ,FOREIGN KEY(sleep_event_id) REFERENCES sleep_events(_id) ON DELETE CASCADE ON UPDATE CASCADE)

        public static final String[] PROJECTION_FULL = {
                _ID,
                COLUMN_HEADBAND_ID,
                COLUMN_SLEEP_EVENT_ID,
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_UPLOADED_ON,
                COLUMN_TIMEZONE,
                COLUMN_CLOCK_OFFSET,
                COLUMN_LOCALIZED_START_OF_NIGHT,
                COLUMN_HIDDEN,
                COLUMN_SOURCE,
                COLUMN_AWAKENINGS,
                COLUMN_BASE_HYPNOGRAM,
                COLUMN_BASE_HYPNOGRAM_COUNT,
                COLUMN_DEEP_SUM,
                COLUMN_DISPLAY_HYPNOGRAM,
                COLUMN_DISPLAY_HYPNOGRAM_COUNT,
                COLUMN_DISPLAY_HYPNOGRAM_FORCED_INDEX,
                COLUMN_DISPLAY_HYPNOGRAM_FORCED_STAGE,
                COLUMN_DISPLAY_HYPNOGRAM_START_TIME,
                COLUMN_END_OF_NIGHT,
                COLUMN_END_REASON,
                COLUMN_INSUFFICIENT_DATA,
                COLUMN_INSUFFICIENT_DATA_START_TIME,
                COLUMN_LIGHT_CHANGED_TO_DEEP,
                COLUMN_SLEEP_RATING,
                COLUMN_SLEEP_VALID,
                COLUMN_START_OF_NIGHT,
                COLUMN_START_OF_NIGHT_MYZEO,
                COLUMN_START_OF_NIGHT_ORIG,
                COLUMN_TIME_IN_DEEP,
                COLUMN_TIME_IN_LIGHT,
                COLUMN_TIME_IN_REM,
                COLUMN_TIME_IN_WAKE,
                COLUMN_TIME_TO_Z,
                COLUMN_TOTAL_Z,
                COLUMN_VALID,
                COLUMN_VALID_FOR_HISTORY,
                COLUMN_VOLTAGE_BATTERY,
                COLUMN_WAKE_TONE,
                COLUMN_WAKE_WINDOW,
                COLUMN_ZEO_WAKE_ON,
                COLUMN_ZQ_SCORE
        };

        public static final String[] PROJECTION_AVAILABLE = {
                _ID,
                COLUMN_HEADBAND_ID,
                COLUMN_SLEEP_EVENT_ID,
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_TIMEZONE,
                COLUMN_LOCALIZED_START_OF_NIGHT,
                COLUMN_SOURCE,
                COLUMN_AWAKENINGS,
                COLUMN_BASE_HYPNOGRAM,
                COLUMN_BASE_HYPNOGRAM_COUNT,
                COLUMN_DISPLAY_HYPNOGRAM,
                COLUMN_DISPLAY_HYPNOGRAM_COUNT,
                COLUMN_END_OF_NIGHT,
                COLUMN_END_REASON,
                COLUMN_START_OF_NIGHT,
                COLUMN_TIME_IN_DEEP,
                COLUMN_TIME_IN_LIGHT,
                COLUMN_TIME_IN_REM,
                COLUMN_TIME_IN_WAKE,
                COLUMN_TIME_TO_Z,
                COLUMN_TOTAL_Z,
                COLUMN_ZQ_SCORE
        };
    }
    public static abstract class ZeoAlarmAlertEvents implements BaseColumns {
        // _ID long integer
        public static final String TABLE_NAME = "alarm_alert_events";
        public static final String COLUMN_CREATED_ON = ZeoDataContract.AlarmAlertEvent.CREATED_ON;
        public static final String COLUMN_UPDATED_ON = ZeoDataContract.AlarmAlertEvent.UPDATED_ON;
        public static final String COLUMN_SLEEP_EVENT_ID = ZeoDataContract.AlarmAlertEvent.SLEEP_EPISODE_ID;
        public static final String COLUMN_ALARM_RING_REASON = ZeoDataContract.AlarmAlertEvent.REASON;
        public static final String COLUMN_SMART_WAKE = ZeoDataContract.AlarmAlertEvent.SMART_WAKE;
        public static final String COLUMN_ALARM_TIMESTAMP = ZeoDataContract.AlarmAlertEvent.TIMESTAMP;
        public static final String COLUMN_WAKE_TONE = ZeoDataContract.AlarmAlertEvent.WAKE_TONE;
        public static final String COLUMN_WAKE_WINDOW = ZeoDataContract.AlarmAlertEvent.WAKE_WINDOW;

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_CREATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_UPDATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_SLEEP_EVENT_ID + " INTEGER," +
                COLUMN_ALARM_RING_REASON + " INTEGER," +
                COLUMN_SMART_WAKE + " BOOLEAN," +
                COLUMN_ALARM_TIMESTAMP + " INTEGER," +
                COLUMN_WAKE_TONE + " INTEGER," +
                COLUMN_WAKE_WINDOW + " INTEGER" +
                ")";
                // FOREIGN KEY (sleep_event_id) REFERENCES sleep_events(_id) ON DELETE CASCADE ON UPDATE CASCADE)

        public static final String[] PROJECTION_FULL = {
                _ID,
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_SLEEP_EVENT_ID,
                COLUMN_ALARM_RING_REASON,
                COLUMN_SMART_WAKE,
                COLUMN_ALARM_TIMESTAMP,
                COLUMN_WAKE_TONE,
                COLUMN_WAKE_WINDOW
        };

        public static final String[] PROJECTION_AVAILABLE = {
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_SLEEP_EVENT_ID,
                COLUMN_ALARM_RING_REASON,
                COLUMN_SMART_WAKE,
                COLUMN_ALARM_TIMESTAMP,
                COLUMN_WAKE_TONE,
                COLUMN_WAKE_WINDOW
        };
    }
    public static abstract class ZeoAlarmSnoozeEvents implements BaseColumns {
        // _ID long integer
        public static final String TABLE_NAME = "alarm_snooze_events";
        public static final String COLUMN_CREATED_ON = ZeoDataContract.AlarmSnoozeEvent.CREATED_ON;
        public static final String COLUMN_UPDATED_ON = ZeoDataContract.AlarmSnoozeEvent.UPDATED_ON;
        public static final String COLUMN_SLEEP_EVENT_ID = ZeoDataContract.AlarmSnoozeEvent.SLEEP_EPISODE_ID;
        public static final String COLUMN_ALARM_SNOOZE_DURATION = ZeoDataContract.AlarmSnoozeEvent.DURATION;
        public static final String COLUMN_ALARM_SNOOZE_TIMESTAMP = ZeoDataContract.AlarmSnoozeEvent.TIMESTAMP;

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_CREATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_UPDATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_SLEEP_EVENT_ID + " INTEGER," +
                COLUMN_ALARM_SNOOZE_DURATION + " INTEGER," +
                COLUMN_ALARM_SNOOZE_TIMESTAMP + " INTEGER" +
                ")";
                // FOREIGN KEY (sleep_event_id) REFERENCES sleep_events(_id) ON DELETE CASCADE ON UPDATE CASCADE)

        public static final String[] PROJECTION_FULL = {
                _ID,
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_SLEEP_EVENT_ID,
                COLUMN_ALARM_SNOOZE_DURATION,
                COLUMN_ALARM_SNOOZE_TIMESTAMP
        };

        public static final String[] PROJECTION_AVAILABLE = {
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_SLEEP_EVENT_ID,
                COLUMN_ALARM_SNOOZE_DURATION,
                COLUMN_ALARM_SNOOZE_TIMESTAMP
        };

    }
    public static abstract class ZeoAlarmTimeoutEvents implements BaseColumns {
        // _ID long integer
        public static final String TABLE_NAME = "alarm_timeout_events";
        public static final String COLUMN_CREATED_ON = ZeoDataContract.AlarmTimeoutEvent.CREATED_ON;
        public static final String COLUMN_UPDATED_ON = ZeoDataContract.AlarmTimeoutEvent.UPDATED_ON;
        public static final String COLUMN_SLEEP_EVENT_ID = ZeoDataContract.AlarmTimeoutEvent.SLEEP_EPISODE_ID;
        public static final String COLUMN_ALARM_TIMEOUT_DURATION = ZeoDataContract.AlarmTimeoutEvent.DURATION;
        public static final String COLUMN_ALARM_TIMEOUT_TIMESTAMP = ZeoDataContract.AlarmTimeoutEvent.TIMESTAMP;

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_CREATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_UPDATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_SLEEP_EVENT_ID + " INTEGER," +
                COLUMN_ALARM_TIMEOUT_DURATION + " INTEGER," +
                COLUMN_ALARM_TIMEOUT_TIMESTAMP + " INTEGER" +
                ")";
                // FOREIGN KEY (sleep_event_id) REFERENCES sleep_events(_id) ON DELETE CASCADE ON UPDATE CASCADE)

        public static final String[] PROJECTION_FULL = {
                _ID,
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_SLEEP_EVENT_ID,
                COLUMN_ALARM_TIMEOUT_DURATION,
                COLUMN_ALARM_TIMEOUT_TIMESTAMP
        };

        public static final String[] PROJECTION_AVAILABLE = {
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_SLEEP_EVENT_ID,
                COLUMN_ALARM_TIMEOUT_DURATION,
                COLUMN_ALARM_TIMEOUT_TIMESTAMP
        };
    }

    ////////////////////////////////////////////////////////////////////
    // all of the following are implemented in version 3 of the database
    ////////////////////////////////////////////////////////////////////
    public static abstract class CompanionSystem implements BaseColumns {
        public static final String TABLE_NAME = "system_info";
        // _ID long integer
        public static final String COLUMN_PRIOR_TO_UPGRADE_APP_VERSION = "prior_to_upgrade_app_ver";  // string
        public static final String COLUMN_PRIOR_TO_UPGRADE_DB_VERSION = "prior_to_upgrade_db_ver";  // integer
        public static final String COLUMN_MOST_RECENT_APP_VERSION = "most_recent_app_ver";  // string
        public static final String COLUMN_MOST_RECENT_DB_VERSION = "most_recent_db_ver";  // integer
        public static final String COLUMN_USER_NAME = "user_name";  // string

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PRIOR_TO_UPGRADE_APP_VERSION + " TEXT, " +
                COLUMN_PRIOR_TO_UPGRADE_DB_VERSION + " INTEGER, " +
                COLUMN_MOST_RECENT_APP_VERSION + " TEXT, " +
                COLUMN_MOST_RECENT_DB_VERSION + " INTEGER, " +
                COLUMN_USER_NAME + " TEXT" +
                ")";

        public static final String[] PROJECTION = {
                _ID,
                COLUMN_PRIOR_TO_UPGRADE_APP_VERSION,
                COLUMN_PRIOR_TO_UPGRADE_DB_VERSION,
                COLUMN_MOST_RECENT_APP_VERSION,
                COLUMN_MOST_RECENT_DB_VERSION,
                COLUMN_USER_NAME
        };
    }

    public static abstract class CompanionAlerts implements BaseColumns {
        public static final String TABLE_NAME = "system_alerts";
        // _ID long integer
        public static final String COLUMN_TIMESTAMP = "timestamp";  // long integer
        public static final String COLUMN_MESSAGE = "alert_message";  // string

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TIMESTAMP + " INTEGER, " +
                COLUMN_MESSAGE + " TEXT" +
                ")";

        public static final String[] PROJECTION = {
                _ID,
                COLUMN_TIMESTAMP,
                COLUMN_MESSAGE
        };
    }

    ////////////////////////////////////////////////////////////////////
    // all of the following are implemented in version 5 of the database
    ////////////////////////////////////////////////////////////////////
    // Note: it does not appear that the Zeo Android App ever places information into this table;
    // the Bluetooth Protocol does not appear to send this information as a part of normal operation
    public static abstract class ZeoActigraphyRecords implements BaseColumns {
        // _ID long integer
        public static final String TABLE_NAME = "actigraphy_records";
        public static final String COLUMN_HEADBAND_ID = "headband_id";
        public static final String COLUMN_SLEEP_EVENT_ID = "sleep_event_id";
        public static final String COLUMN_CREATED_ON = "created_on";
        public static final String COLUMN_UPDATED_ON = "updated_on";
        public static final String COLUMN_UPLOADED_ON = "uploaded_on";
        public static final String COLUMN_START_OF_NIGHT = "start_of_night";
        public static final String COLUMN_END_OF_NIGHT = "end_of_night";
        public static final String COLUMN_MAGNITUDES = "magnitudes";
        public static final String COLUMN_RESTLESS_TIME = "restless_time";
        public static final String COLUMN_TIMES_DISRUPTED = "times_disrupted";
        public static final String COLUMN_TOTAL_REST = "total_rest";

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_HEADBAND_ID + " INTEGER, " +
                COLUMN_SLEEP_EVENT_ID + " INTEGER, " +
                COLUMN_CREATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_UPDATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_UPLOADED_ON + " INTEGER," +
                COLUMN_START_OF_NIGHT + " INTEGER," +
                COLUMN_END_OF_NIGHT + " INTEGER," +
                COLUMN_MAGNITUDES + " BLOB," +
                COLUMN_RESTLESS_TIME + " INTEGER," +
                COLUMN_TIMES_DISRUPTED + " INTEGER," +
                COLUMN_TOTAL_REST + " INTEGER," +
                "UNIQUE ("+COLUMN_START_OF_NIGHT+"))";
        // FOREIGN KEY(sleep_event_id) REFERENCES sleep_events(_id) ON DELETE CASCADE ON UPDATE CASCADE)

        public static final String[] PROJECTION_FULL = {
                _ID,
                COLUMN_HEADBAND_ID,
                COLUMN_SLEEP_EVENT_ID,
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_UPLOADED_ON,
                COLUMN_START_OF_NIGHT,
                COLUMN_END_OF_NIGHT,
                COLUMN_MAGNITUDES,
                COLUMN_RESTLESS_TIME,
                COLUMN_TIMES_DISRUPTED,
                COLUMN_TOTAL_REST
        };
    }

    public static abstract class ZeoAlarms implements BaseColumns {
        // _ID long integer
        public static final String TABLE_NAME = "alarms";
        public static final String COLUMN_CREATED_ON = "created_on";
        public static final String COLUMN_UPDATED_ON = "updated_on";
        public static final String COLUMN_ALARM_HOUR = "alarm_hour";
        public static final String COLUMN_ALARM_MINUTE = "alarm_minute";
        public static final String COLUMN_ALARM_DAYS_OF_WEEK = "alarm_days_of_week";
        public static final String COLUMN_ALARM_TIMESTAMP = "alarm_timestamp";
        public static final String COLUMN_LAST_ALERT_TIMESTAMP = "last_alert_timestamp";
        public static final String COLUMN_ALARM_ENABLED = "alarm_enabled";
        public static final String COLUMN_ALARM_AUDIO = "alarm_audio";
        public static final String COLUMN_ALARM_SMART_WAKE = "alarm_smart_wake";
        public static final String COLUMN_ALARM_WAKE_WINDOW = "alarm_wake_window";
        public static final String COLUMN_ALARM_SNOOZE_DURATION = "alarm_snooze_duration";
        public static final String COLUMN_ALARM_TIMEOUT = "alarm_timeout";
        public static final String COLUMN_ALARM_VIBRATE = "alarm_vibrate";
        public static final String COLUMN_ALARM_MESSAGE = "alarm_message";

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_CREATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_UPDATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_ALARM_HOUR + " INTEGER," +
                COLUMN_ALARM_MINUTE + " INTEGER," +
                COLUMN_ALARM_DAYS_OF_WEEK + " INTEGER," +
                COLUMN_ALARM_TIMESTAMP + " INTEGER," +
                COLUMN_LAST_ALERT_TIMESTAMP + " INTEGER," +
                COLUMN_ALARM_ENABLED + " BOOLEAN," +
                COLUMN_ALARM_AUDIO + " TEXT," +
                COLUMN_ALARM_SMART_WAKE + " BOOLEAN," +
                COLUMN_ALARM_WAKE_WINDOW + " INTEGER," +
                COLUMN_ALARM_SNOOZE_DURATION + " INTEGER," +
                COLUMN_ALARM_TIMEOUT + " INTEGER," +
                COLUMN_ALARM_VIBRATE + " BOOLEAN," +
                COLUMN_ALARM_MESSAGE + " TEXT" +
                ")";
        // FOREIGN KEY(sleep_event_id) REFERENCES sleep_events(_id) ON DELETE CASCADE ON UPDATE CASCADE)

        public static final String[] PROJECTION_FULL = {
                _ID,
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_ALARM_HOUR,
                COLUMN_ALARM_MINUTE,
                COLUMN_ALARM_DAYS_OF_WEEK,
                COLUMN_ALARM_TIMESTAMP,
                COLUMN_LAST_ALERT_TIMESTAMP,
                COLUMN_ALARM_ENABLED,
                COLUMN_ALARM_AUDIO,
                COLUMN_ALARM_SMART_WAKE,
                COLUMN_ALARM_WAKE_WINDOW,
                COLUMN_ALARM_SNOOZE_DURATION,
                COLUMN_ALARM_TIMEOUT,
                COLUMN_ALARM_VIBRATE,
                COLUMN_ALARM_MESSAGE
        };
    }

    public static abstract class ZeoHeadbandAsserts implements BaseColumns {
        // _ID long integer
        public static final String TABLE_NAME = "headband_asserts";
        public static final String COLUMN_HEADBAND_ID = "headband_id";
        public static final String COLUMN_CREATED_ON = "created_on";
        public static final String COLUMN_UPDATED_ON = "updated_on";
        public static final String COLUMN_ASSERT_FUNCTION = "assert_function";
        public static final String COLUMN_ASSERT_LINE = "assert_line";

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_HEADBAND_ID + " INTEGER, " +
                COLUMN_CREATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_UPDATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_ASSERT_FUNCTION + " TEXT," +
                COLUMN_ASSERT_LINE + " INTEGER," +
                "UNIQUE ("+COLUMN_CREATED_ON+"))";
        // FOREIGN KEY (headband_id) REFERENCES headbands(_id) ON DELETE CASCADE ON UPDATE CASCADE)

        public static final String[] PROJECTION_FULL = {
                _ID,
                COLUMN_HEADBAND_ID,
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_ASSERT_FUNCTION,
                COLUMN_ASSERT_LINE
        };
    }

    // Note: it does not appear that the Zeo Android App ever places information into this table;
    // the Bluetooth protocol does send this information to the App, but the App never actually stores it
    public static abstract class ZeoHeadbandResets implements BaseColumns {
        // _ID long integer
        public static final String TABLE_NAME = "headband_resets";
        public static final String COLUMN_HEADBAND_ID = "headband_id";
        public static final String COLUMN_CREATED_ON = "created_on";
        public static final String COLUMN_UPDATED_ON = "updated_on";
        public static final String COLUMN_RESET_CAUSE = "reset_cause";
        public static final String COLUMN_RESET_COUNT = "reset_count";

        public static final String SQL_DEFINITION = TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_HEADBAND_ID + " INTEGER, " +
                COLUMN_CREATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_UPDATED_ON + " INTEGER DEFAULT 0," +
                COLUMN_RESET_CAUSE + " INTEGER," +
                COLUMN_RESET_COUNT + " INTEGER," +
                "UNIQUE ("+COLUMN_CREATED_ON+"))";
        // FOREIGN KEY (headband_id) REFERENCES headbands(_id) ON DELETE CASCADE ON UPDATE CASCADE)

        public static final String[] PROJECTION_FULL = {
                _ID,
                COLUMN_HEADBAND_ID,
                COLUMN_CREATED_ON,
                COLUMN_UPDATED_ON,
                COLUMN_RESET_CAUSE,
                COLUMN_RESET_COUNT
        };

        public static final int ZEOHEADBAND_RESET_CAUSE_NONE = 0;
        public static final int ZEOHEADBAND_RESET_CAUSE_BROWNOUT = 1;
        public static final int ZEOHEADBAND_RESET_CAUSE_EXTERNAL = 2;
        public static final int ZEOHEADBAND_RESET_CAUSE_INTERNAL = 3;
        public static final int ZEOHEADBAND_RESET_CAUSE_LOCKUP = 4;
        public static final int ZEOHEADBAND_RESET_CAUSE_POWERUP = 5;
        public static final int ZEOHEADBAND_RESET_CAUSE_WATCHDOG = 6;
        public static final int ZEOHEADBAND_RESET_CAUSE_MAX = 7;
    }
}
