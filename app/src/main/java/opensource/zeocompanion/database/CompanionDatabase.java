package opensource.zeocompanion.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import com.myzeo.android.api.data.MyZeoExportDataContract;
import java.io.File;
import java.util.ArrayList;
import opensource.zeocompanion.BuildConfig;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.zeo.ZAH_SleepRecord;

// responsible for all actual data interactions with the ZeoCompanion's database
public class CompanionDatabase extends SQLiteOpenHelper {
    // member variables
    Context mContext = null;
    public int mVersion = 0;
    public boolean mInvalidDB = false;
    public boolean mDefinitionsChanged = false;
    public static int[] mSlot_SleepStages = new int[MyZeoExportDataContract.EXPORT_FIELD_SLOTS_TOTAL];
    public static String[] mSlot_ExportNames = new String[MyZeoExportDataContract.EXPORT_FIELD_SLOTS_TOTAL];
    public static String CompanionSleepEpisodes_TABLE_NAME = CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL;

    // member constants and other static content
    private static final String _CTAG = "DBH";
    public static final String DATABASE_NAME = "ZeoCompanionDatabase.db";
    private static final int DATABASE_VERSION = 4;  // WARNING: changing this value will cause invocation of onUpdate for existing databases in existing Devices

    public static final int DBH_ERROR_NONE = 0;
    public static final int DBH_ERROR_SQL_ERROR = -100;

    /////////////////////////////////////////////////////////////
    // The following methods are used at startup or Factory Reset
    /////////////////////////////////////////////////////////////

    // constructor
    public CompanionDatabase(Context context) {
        // note that when the database somehow gets corrupted, the stupid Android code auto-deletes it; have to override the DatabaseErrorHandler to prevent this
        super(context, DATABASE_NAME, null, DATABASE_VERSION, new DatabaseErrorHandler() {
            @Override
            public void onCorruption(SQLiteDatabase dbObj) {}   // deliberately do nothing
        });
        mContext = context;
    }

    // one-time creation of the database itself and its initial contents;
    // only called if no database exists at all; this is called before initialize()
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create the tables from scratch; populate the definitions tables with factory defaults;
        // this create-from-scratch is always compliant to the most recent DB version
        Log.d(_CTAG + ".onCreate", "=====ON-CREATE=====");
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + CompanionDatabaseContract.CompanionSystem.SQL_DEFINITION);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + CompanionDatabaseContract.CompanionAlerts.SQL_DEFINITION);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + CompanionDatabaseContract.CompanionSleepEpisodes.SQL_DEFINITION);
            db.execSQL("INSERT OR REPLACE INTO " + CompanionDatabaseContract.CompanionSystem.TABLE_NAME + " VALUES (1,NULL,0,'"+ BuildConfig.VERSION_NAME+"',"+DATABASE_VERSION+",NULL)");
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".onCreate", e, "Failed to create the App database");    // automatically posts a Log.e
            mInvalidDB = true;
            return;
        }

        // now reload the factory defaults for the definitions table
        reloadFactoryDefaults_internal(db);
    }

    // one-time call if an existing database is required to self-update itself;
    // onCreate will not be called; this is called before initialize()
    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(_CTAG + ".onUpgrade", "=====ON-UPGRADE=====");
        Log.i(_CTAG + ".onUpgrade", "Starting upgrade database from version " + oldVersion + " to version " + newVersion);
        mVersion = oldVersion;
        String msg1 = ((ZeoCompanionApplication)mContext).saveCopyOfDB("preUpgradeVer_");
        if (!msg1.isEmpty()) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".onUpgrade", msg1, "Upgrade " + oldVersion + " to " + newVersion + ": saveCopyOfDB failed");    // automatically posts a Log.e
        }

        if (oldVersion <= 1) {
            // upgrade version 1 database to version 2
            // rename the original "sleep_records" table to "sleep_journal_records";
            // if this fails then the rest of the upgrade should not continue and force the database back to version 1
            try {
                db.execSQL("ALTER TABLE "+ CompanionDatabaseContract.CompanionSleepEpisodes.VER1_TABLE_NAME_INTERNAL+" RENAME TO "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL);
            } catch (Exception e) {
                ZeoCompanionApplication.postToErrorLog(_CTAG + ".onUpgrade", e, "Upgrade to 2: failed renaming the sleep_records table to sleep_journal_records");
                db.setVersion(mVersion);
                return;
            }

            // alter the sleep_journal_records table with new columns
            try {
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMENDED+" INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_START_OF_NIGHT+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_END_OF_NIGHT+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_AWAKENINGS+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_TO_Z+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TOTAL_Z+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_IN_WAKE+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_IN_REM+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_IN_LIGHT+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_TIME_IN_DEEP+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_ZQ_SCORE+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_LIGHT_CHANGED_TO_DEEP+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_DEEP_SUM+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_DISPLAY_HYPNOGRAM+" BLOB");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_BASE_HYPNOGRAM+" BLOB");
            } catch (Exception e) {
                ZeoCompanionApplication.postToErrorLog(_CTAG + ".onUpgrade", e, "Upgrade to 2: failed adding new columns to sleep_journal_records table");    // automatically posts a Log.e
                db.setVersion(mVersion);
                return;
            }
            mVersion = 2;
            Log.i(_CTAG + ".onUpgrade", "Database successfully upgraded to version "+mVersion);
        }

        if (oldVersion <= 2) {
            // upgrade version 2 database to version 3
            // add two new tables
            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + CompanionDatabaseContract.CompanionSystem.SQL_DEFINITION);
                db.execSQL("CREATE TABLE IF NOT EXISTS " + CompanionDatabaseContract.CompanionAlerts.SQL_DEFINITION);
            } catch (Exception e) {
                ZeoCompanionApplication.postToErrorLog(_CTAG + ".onUpgrade", e, "Upgrade to 3: failed adding new tables");    // automatically posts a Log.e
                db.setVersion(mVersion);
                return;
            }

            // eliminate empty Zeo App replication tables that were created in an older variation of upgrade to DB version 2;
            // however do not delete these tables if there are Zeo App records present
            int qty = getQtyZeoSleepEpisodeRecs_internal(db);
            if (qty == 0) { // qty == 0 can only be returned if the table is present but there are no rows
                try {
                    db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.ZeoHeadbands.TABLE_NAME);
                    db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.ZeoSleepEvents.TABLE_NAME);
                    db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.ZeoSleepRecords.TABLE_NAME);
                    db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.ZeoAlarmAlertEvents.TABLE_NAME);
                    db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.ZeoAlarmSnoozeEvents.TABLE_NAME);
                    db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.ZeoAlarmTimeoutEvents.TABLE_NAME);
                } catch (Exception e) {
                    ZeoCompanionApplication.postToErrorLog(_CTAG + ".onUpgrade", e, "Upgrade to 3: failed deleting Zeo App replication tables");    // automatically posts a Log.e
                }
            }

            // ensure the COLUMN_AMEND_START_OF_NIGHT and COLUMN_AMEND_END_OF_NIGHT columns are present in the sleep_journal_records;
            // these two columns were not always created in an older version of the upgrade to DB version 2
            try {
                db.execSQL("ALTER TABLE "+CompanionSleepEpisodes_TABLE_NAME+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_START_OF_NIGHT+" INTEGER");
            } catch (Exception e) {
                String msg = e.getMessage();
                if (!msg.contains("duplicate column name")) {   // a "no such table" exception is expected here
                    ZeoCompanionApplication.postToErrorLog(_CTAG + ".onUpgrade", e, "Upgrade to 3: failed adding amend_start_of_night column to table " + CompanionSleepEpisodes_TABLE_NAME);    // automatically posts a Log.e
                }
            }
            try {
                db.execSQL("ALTER TABLE " + CompanionSleepEpisodes_TABLE_NAME + " ADD COLUMN " + CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_END_OF_NIGHT + " INTEGER");
            } catch (Exception e) {
                String msg = e.getMessage();
                if (!msg.contains("duplicate column name")) {   // a "no such table" exception is expected here
                    ZeoCompanionApplication.postToErrorLog(_CTAG + ".onUpgrade", e, "Upgrade to 3: failed failed adding amend_end_of_night column to table " + CompanionSleepEpisodes_TABLE_NAME);    // automatically posts a Log.e
                }
            }

            // initialize the System Record for the first time
            try {
                db.execSQL("INSERT OR REPLACE INTO " + CompanionDatabaseContract.CompanionSystem.TABLE_NAME + " VALUES (1,NULL,"+oldVersion+",'"+ BuildConfig.VERSION_NAME+"',3,NULL)");
            } catch (SQLException e) {
                ZeoCompanionApplication.postToErrorLog(_CTAG + ".onUpgrade", e, "Upgrade to 3: Insert/Replace failed For DB Table " + CompanionDatabaseContract.CompanionSystem.TABLE_NAME);    // automatically posts a Log.e
            }
            mVersion = 3;
            Log.i(_CTAG + ".onUpgrade", "Database successfully upgraded to version "+mVersion);
        }

        if (oldVersion <= 3) {
            // upgrade version 3 database to version 4
            // alter the sleep_journal_records table with new columns
            try {
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_HEADBAND_BATTERY_HIGH+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_HEADBAND_BATTERY_LOW+" INTEGER");
                db.execSQL("ALTER TABLE "+CompanionDatabaseContract.CompanionSleepEpisodes.TABLE_NAME_INTERNAL+" ADD COLUMN "+CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_AMEND_DISPLAY_HYPNOGRAM_STARTTIME+" INTEGER");
            } catch (Exception e) {
                ZeoCompanionApplication.postToErrorLog(_CTAG + ".onUpgrade", e, "Upgrade to 4: failed adding new columns to sleep_journal_records table");    // automatically posts a Log.e
                db.setVersion(mVersion);
                return;
            }

            // reset all the amended flags so all amendments get recomputed
            resetAllAmendedFlags(db);

            mVersion = 4;
            Log.i(_CTAG + ".onUpgrade", "Database successfully upgraded to version "+mVersion);
        }

        // reset the values in the System Record to reflect the successful upgrade(s)
        if (mVersion >= 3) {
            CompanionSystemRec sr = getSystemRec_internal(db);
            if (sr != null) {
                if (oldVersion >= 3) {
                    sr.rPrior_to_upgrade_AppVer = sr.rMost_recent_AppVer;
                    sr.rPrior_to_upgrade_DBVer = sr.rMost_recent_DBVer;
                }
                sr.rMost_recent_AppVer = BuildConfig.VERSION_NAME;
                sr.rMost_recent_DBVer = newVersion;
                ContentValues values = sr.saveToDB_internal();
                long rowID = -1;
                try {
                    rowID = db.replaceOrThrow(CompanionDatabaseContract.CompanionSystem.TABLE_NAME, "", values);
                    if (rowID < 0) {
                        ZeoCompanionApplication.postToErrorLog(_CTAG + ".onUpgrade", "Upgrade to "+newVersion+": SQL Return Error: " + rowID, "For DB Table " + CompanionDatabaseContract.CompanionSystem.TABLE_NAME); // automatically posts a Log.e
                    } else {
                        sr.rID = rowID;
                    }
                } catch (SQLException e) {
                    ZeoCompanionApplication.postToErrorLog(_CTAG + ".onUpgrade", e, "Upgrade to "+newVersion+": For DB Table " + CompanionDatabaseContract.CompanionSystem.TABLE_NAME);    // automatically posts a Log.e
                }
            }
        }
    }

    // delete and reload only the definitional tables
    public String reloadFactoryDefaults() {
        if (mInvalidDB) { return "Database version is invalid and cannot be reloaded"; }
        SQLiteDatabase db = getWritableDatabase();
        reloadFactoryDefaults_internal(db);
        return "";
    }

    // delete and reload only the definitional tables
    private void reloadFactoryDefaults_internal(SQLiteDatabase db) {
        if (mInvalidDB) { return; }
        try {
            db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.CompanionAttributes.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.CompanionAttributeValues.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.CompanionEventDoings.TABLE_NAME);

            db.execSQL("CREATE TABLE IF NOT EXISTS " + CompanionDatabaseContract.CompanionAttributes.SQL_DEFINITION);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + CompanionDatabaseContract.CompanionAttributeValues.SQL_DEFINITION);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + CompanionDatabaseContract.CompanionEventDoings.SQL_DEFINITION);
        } catch (Exception e) {
            mInvalidDB = true;
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".reloadFactoryDefaults_internal", e, "Failed adding or re-adding definitions tables");    // automatically posts a Log.e
            return;
        };

        // load the tables with factory default information
        try {
            for (String str: CompanionDatabaseContract.CompanionAttributes.SQL_INSERTS) {
                db.execSQL("INSERT OR REPLACE INTO " + CompanionDatabaseContract.CompanionAttributes.TABLE_NAME+" VALUES ("+str+")");
            }
            for (String str: CompanionDatabaseContract.CompanionAttributeValues.SQL_INSERTS) {
                db.execSQL("INSERT OR REPLACE INTO " + CompanionDatabaseContract.CompanionAttributeValues.TABLE_NAME+" VALUES ("+str+")");
            }
            for (String str: CompanionDatabaseContract.CompanionEventDoings.SQL_INSERTS) {
                db.execSQL("INSERT OR REPLACE INTO " + CompanionDatabaseContract.CompanionEventDoings.TABLE_NAME+" VALUES ("+str+")");
            }
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".reloadFactoryDefaults_internal", e, "Failed adding definitions to the tables");    // automatically posts a Log.e
            return;
        };
        preload_slotInfo_internal(db);
        mDefinitionsChanged = true;
    }

    // called once at App start (not called by MainActivity); this will not get recalled when App is just reactivated from background;
    // neither onCreate or onUpgrade will have been called yet, so must auto-force their execution
    public String initialize() {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();  // this call will auto-force a database on-create or on-upgrade if needed; those calls will be "blocking calls" so when this call returns they will have been performed
            mVersion = db.getVersion();
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".initialize", e);    // automatically posts a Log.e
            mInvalidDB = true;
            if (db != null) { db.close(); }
            return "EXTREME DATABASE ERROR: THE SLEEP JOURNAL FEATURES OF THIS APP WILL NOT FUNCTION: "+e.getMessage();
        }
        // validate the database's minimally needed tables and version
        ValidateDatabaseResults result = validateDatabase_internal(db);
        if (!result.mResultMsg.isEmpty()) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".initialize", result.mResultMsg, null);
            mInvalidDB = true;
            db.close();
            return "EXTREME DATABASE ERROR: THE SLEEP JOURNAL FEATURES OF THIS APP WILL NOT FUNCTION: "+result.mResultMsg;
        }

        // database version specific logic
        switch (mVersion) {
            case 1:
                // upgrade apparently has failed; this is suriviable by just using the old table name   // TODO V1.1 keep or eliminate this
                Log.e(_CTAG + ".initialize", "ERROR: database remains at version 1; remains usable");
                CompanionSleepEpisodes_TABLE_NAME = CompanionDatabaseContract.CompanionSleepEpisodes.VER1_TABLE_NAME_INTERNAL;
                break;
        }

        // load first factory default export names for the custom slots, then load any overrides from the Attributes definition table
        int n = MyZeoExportDataContract.EXPORT_FIELD_SLOTS_FIRST_WITHIN_COLNAMES +  MyZeoExportDataContract.EXPORT_FIELD_SLOT_DAY_FEEL3;
        for (int i = 0; i < MyZeoExportDataContract.EXPORT_FIELD_SLOTS_TOTAL; i++) {
            mSlot_ExportNames[i] =  MyZeoExportDataContract.EXPORT_COLUMN_ORDER_NAMES[i + MyZeoExportDataContract.EXPORT_FIELD_SLOTS_FIRST_WITHIN_COLNAMES];
            if (i <= n) { mSlot_SleepStages[i] = CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER; }
            else { mSlot_SleepStages[i] = CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE; }
        }
        preload_slotInfo_internal(db);
        return "";
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // The following methods perform database pre-validation during startup and database restore
    ////////////////////////////////////////////////////////////////////////////////////////////

    public static class ValidateDatabaseResults {
        public String mResultMsg = null;
        public CompanionSystemRec mSystemRec = null;

        public ValidateDatabaseResults(String msg) { mResultMsg = msg; }
    }

    // validate a proposed new ZeoCompanion database file
    public ValidateDatabaseResults validateDatabaseFromFile(File source) {
        // note that when a proposed file is not a SQLite database, the stupid Android code auto-deletes it; have to override the DatabaseErrorHandler to prevent this
        SQLiteDatabase db = SQLiteDatabase.openDatabase(source.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY, new DatabaseErrorHandler() {
            @Override
            public void onCorruption(SQLiteDatabase dbObj) {}   // deliberately do nothing
        });
        if (db == null) { return new ValidateDatabaseResults("SQLite database manager cannot open the database file"); }
        ValidateDatabaseResults results = validateDatabase_internal(db);
        db.close();
        return results;
    }

    // validate a ZeoCompanion database (current or proposed)
    private ValidateDatabaseResults validateDatabase_internal(SQLiteDatabase db) {
        int v = 0;
        try {
            v = db.getVersion();
        } catch (SQLException e) {
            return new ValidateDatabaseResults("The specified file is not a valid SQLite database.");
        }
        if (v < 1 || v > DATABASE_VERSION) { return new ValidateDatabaseResults("Database version "+v+" not compatible with this App version"); }

        Cursor cursor = null;
        int found = 0;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null);
            if (cursor == null) { return new ValidateDatabaseResults("SQLite database manager cannot read the database file"); }
            if (!cursor.moveToFirst()) { cursor.close(); return new ValidateDatabaseResults("SQLite database manager has no structure for the database file");  }
            do {
                String name = cursor.getString(0);
                switch (v) {
                    case 1:
                        for (int i = 0; i < CompanionDatabaseContract.MANDATORY_TABLE_NAMES_DBVER1.length; i++) {
                            if (name.equals(CompanionDatabaseContract.MANDATORY_TABLE_NAMES_DBVER1[i])) {
                                found = (found | (1 << i));
                                break;
                            }
                        }
                        break;
                    case 2:
                        for (int i = 0; i < CompanionDatabaseContract.MANDATORY_TABLE_NAMES_DBVER2.length; i++) {
                            if (name.equals(CompanionDatabaseContract.MANDATORY_TABLE_NAMES_DBVER2[i])) {
                                found = (found | (1 << i));
                                break;
                            }
                        }
                        break;
                    case 3:
                    case 4:
                    default:
                        for (int i = 0; i < CompanionDatabaseContract.MANDATORY_TABLE_NAMES_DBVER3_4.length; i++) {
                            if (name.equals(CompanionDatabaseContract.MANDATORY_TABLE_NAMES_DBVER3_4[i])) {
                                found = (found | (1 << i));
                                break;
                            }
                        }
                        break;
                }

            } while (cursor.moveToNext());
        } catch (SQLException e) {
            return new ValidateDatabaseResults("SQLite database manager cannot read the structure of the database file.");
        }
        if (cursor != null) { cursor.close(); }

        if (found == 0) { return new ValidateDatabaseResults("The database does not contain any of the DBver "+v+" ZeoCompanion database tables"); }
        else {
            switch (v) {
                case 1:
                    if (found != 0x0F) { return new ValidateDatabaseResults("The database does not contain all of the necessary DBver "+v+" ZeoCompanion database tables"); }
                    break;
                case 2:
                    if (found != 0x0F) { return new ValidateDatabaseResults("The database does not contain all of the necessary DBver "+v+" ZeoCompanion database tables"); }
                    break;
                case 3:
                case 4:
                default:
                    if (found != 0x3F) { return new ValidateDatabaseResults("The database does not contain all of the necessary DBver "+v+" ZeoCompanion database tables"); }
                    break;
            }
        }

        ValidateDatabaseResults result = new ValidateDatabaseResults("");
        if (v >= 3) {
            result.mSystemRec = getSystemRec_internal(db);
            if (result.mSystemRec != null) {
                if (result.mSystemRec.rUserName != null) {
                    if (!result.mSystemRec.rUserName.isEmpty()) {
                        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                        String settingsName = mPrefs.getString("profile_name", "");
                        if (!settingsName.isEmpty()) {
                            if (!settingsName.equals(result.mSystemRec.rUserName)) {
                                result.mResultMsg = "The profile name within the database \'"+result.mSystemRec.rUserName+"\' does not match the Settings profile name \'"+settingsName+"\'; cannot restore a mismatch";
                            }
                        }
                    }
                }
            }
        } else {
            result.mSystemRec = new CompanionSystemRec(null, 0, null, v);
        }
        return result;
    }

    // close the database (in preparation for a restore operation)
    public boolean closeDatabase() {
        try {
            close();
            return true;
        } catch (Exception e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".closeDatabase", e);    // automatically posts a Log.e
        }
        return false;
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // The following methods perform write or delete record operation for the rest of the App
    /////////////////////////////////////////////////////////////////////////////////////////

    // perform an insert or replace; works for all database tables;
    // returns the rowID that was inserted or replaced
    public long insertOrReplaceRecs(String table, ContentValues values) {
        if (mInvalidDB) { return -1; }
        SQLiteDatabase db = getWritableDatabase();
        return insertOrReplaceRecs(db, table, values, false);
    }
    public long insertOrReplaceRecs(String table, ContentValues values, boolean noAlert) {
        if (mInvalidDB) { return -1; }
        SQLiteDatabase db = getWritableDatabase();
        return insertOrReplaceRecs(db, table, values, noAlert);
    }
    public long insertOrReplaceRecs(SQLiteDatabase db, String table, ContentValues values, boolean noAlert) {
        long rowID = -1;
        try {
            rowID = db.replaceOrThrow(table, "", values);
            if (rowID < 0) {
                ZeoCompanionApplication.postToErrorLog(_CTAG + ".insertOrReplaceRecs", "SQL Return Error: " + rowID, "For DB Table " + table, noAlert); // automatically posts a Log.e
                return DBH_ERROR_SQL_ERROR;
            }
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".insertOrReplaceRecs", e, "For DB Table " + table, null, noAlert);    // automatically posts a Log.e
            return DBH_ERROR_SQL_ERROR;
        }
        return rowID;
    }

    // perform a delete of a record or records; works for all database tables;
    public void deleteRecs(String table, String where, String[] values) {
        if (mInvalidDB) { return; }
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(table, where, values);
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".deleteRecs", e, "For DB Table " + table);     // automatically posts a Log.e
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    // The following methods perform standard read operations for the rest of the App
    /////////////////////////////////////////////////////////////////////////////////
    // get the single system record
    public CompanionSystemRec getSystemRec() {
        SQLiteDatabase db = getReadableDatabase();
        return getSystemRec_internal(db);
    }
    public CompanionSystemRec getSystemRec_internal(SQLiteDatabase db) {
        if (mInvalidDB) { return null; }
        String where = CompanionDatabaseContract.CompanionSystem._ID + "=1";
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionDatabaseContract.CompanionSystem.TABLE_NAME,   // table name
                    CompanionDatabaseContract.CompanionSystem.PROJECTION,   // columns to get
                    where,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    null);    // sort order
            if (cursor == null) { return null; }
            if (!cursor.moveToFirst()) { cursor.close(); return null; }
            CompanionSystemRec rec = new CompanionSystemRec(cursor);
            cursor.close();
            return rec;
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getSystemRec_internal", e);   // automatically posts a Log.e
        }
        if (cursor != null) { cursor.close(); }
        return null;
    }

    // return the quantity of alert records
    public int getQtyCompanionAlertRecs() {
        if (mInvalidDB) { return -1; }
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = { BaseColumns._ID };
        int qty = 0;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionDatabaseContract.CompanionAlerts.TABLE_NAME,   // table name
                    columns,   // columns to get
                    null,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    null);    // sort order

            if (cursor != null){ qty = cursor.getCount(); }
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getQtyCompanionAlertRecs", e);
        }
        if (cursor != null) { cursor.close(); }
        return qty;
    }

    // get all existing alert records, sorted in descending timestamp order (newest to oldest)
    public Cursor getAllAlertRecs() {
        if (mInvalidDB) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        String sortOrder = CompanionDatabaseContract.CompanionAlerts.COLUMN_TIMESTAMP + " DESC";
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionDatabaseContract.CompanionAlerts.TABLE_NAME,   // table name
                    CompanionDatabaseContract.CompanionAlerts.PROJECTION,   // columns to get
                    null,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    sortOrder);    // sort order
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getAllAlertRecs", e);
            if (cursor != null) { cursor.close(); cursor = null; }
        }
        return cursor;
    }

    // return the quantity of sleep journal records
    public int getQtyCompanionSleepEpisodeRecs() {
        if (mInvalidDB) { return -1; }
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = { BaseColumns._ID };
        int qty = 0;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionSleepEpisodes_TABLE_NAME,   // table name
                    columns,   // columns to get
                    null,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    null);    // sort order

            if (cursor != null){ qty = cursor.getCount(); }
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getQtyCompanionSleepEpisodeRecs", e, "For DB Table " + CompanionSleepEpisodes_TABLE_NAME);
        }
        if (cursor != null) { cursor.close(); }
        return qty;
    }

    // get all existing sleep episode records, sorted in descending timestamp order (newest to oldest)
    public Cursor getAllCompanionSleepEpisodesRecs() {
        if (mInvalidDB) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        String sortOrder = CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_START_OF_RECORD_TIMESTAMP + " DESC";
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionSleepEpisodes_TABLE_NAME,   // table name
                    CompanionDatabaseContract.CompanionSleepEpisodes.PROJECTION,   // columns to get
                    null,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    sortOrder);    // sort order
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getAllCompanionSleepEpisodesRecs", e, "For DB Table " + CompanionSleepEpisodes_TABLE_NAME);
            if (cursor != null) { cursor.close(); cursor = null; }
        }
        return cursor;
    }

    // reset all amended flags
    private boolean resetAllAmendedFlags(SQLiteDatabase db) {
        String sortOrder = CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_START_OF_RECORD_TIMESTAMP + " DESC";
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionSleepEpisodes_TABLE_NAME,   // table name
                    CompanionDatabaseContract.CompanionSleepEpisodes.PROJECTION,   // columns to get
                    null,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    sortOrder);    // sort order
            if (cursor == null) {
                ZeoCompanionApplication.postToErrorLog(_CTAG + ".resetAllAmendedFlags", "Cursor==null", "For DB Table " + CompanionSleepEpisodes_TABLE_NAME);
                return false;
            }
            if (cursor.moveToFirst()) {
                do {
                    CompanionSleepEpisodesRec rec = new CompanionSleepEpisodesRec(cursor);
                    if (rec.rAmendedFlags != 0) {
                        rec.rAmendedFlags = 0;
                        rec.saveToDB(db, true);
                    }
                } while (cursor.moveToNext());
                cursor.close();
                return true;
            }
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".resetAllAmendedFlags", e, "For DB Table " + CompanionSleepEpisodes_TABLE_NAME);
            if (cursor != null) { cursor.close(); cursor = null; }
        }
        return false;
    }

    // get existing sleep episode records that are later than the specified timestamp, sorted in descending timestamp order
    public Cursor getAllCompanionSleepEpisodesRecsAfterDate(long fromTimestamp) {
        if (mInvalidDB) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        String sortOrder = CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_START_OF_RECORD_TIMESTAMP + " DESC";
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionSleepEpisodes_TABLE_NAME,   // table name
                    CompanionDatabaseContract.CompanionSleepEpisodes.PROJECTION,   // columns to get
                    CompanionDatabaseContract.CompanionSleepEpisodes.COLUMN_START_OF_RECORD_TIMESTAMP + ">=?",  // columns for optional WHERE clause
                    new String[]{String.valueOf(fromTimestamp)},                                                // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    sortOrder);    // sort order
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getAllCompanionSleepEpisodesRecsAfterDate", e, "For DB Table " + CompanionSleepEpisodes_TABLE_NAME + " Timestamp=" + fromTimestamp);
            if (cursor != null) { cursor.close(); cursor = null; }
        }
        return cursor;
    }

    // get all Attribute definition records; sorted in ascending display order
    public Cursor getAllAttributeRecsSortedInvSleepStageDisplayOrder() {
        if (mInvalidDB) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        String sortOrder = CompanionDatabaseContract.CompanionAttributes.COLUMN_APPLIES_TO_STAGE + " DESC, " + CompanionDatabaseContract.CompanionAttributes.COLUMN_DISPLAY_ORDER + " ASC";
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionDatabaseContract.CompanionAttributes.TABLE_NAME,   // table name
                    CompanionDatabaseContract.CompanionAttributes.PROJECTION,   // columns to get
                    null,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    sortOrder);    // sort order
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getAllAttributeRecsSortedInvSleepStageDisplayOrder", e);
            if (cursor != null) { cursor.close(); cursor = null; }
        }
        return cursor;
    }

    // preload all the custom export header names
    public void preload_slotInfo() {
        SQLiteDatabase db = getReadableDatabase();
        preload_slotInfo_internal(db);
    }

    // preload all the custom export header names
    private void preload_slotInfo_internal(SQLiteDatabase db) {
        if (mInvalidDB) { return; }
        String sortOrder = CompanionDatabaseContract.CompanionAttributes.COLUMN_EXPORT_SLOT + " ASC";
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionDatabaseContract.CompanionAttributes.TABLE_NAME,   // table name
                    CompanionDatabaseContract.CompanionAttributes.PROJECTION,   // columns to get
                    null,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    sortOrder);    // sort order
            if (cursor == null) { return; }
            if (cursor.moveToFirst()) {
                do {
                    CompanionAttributesRec rec = new CompanionAttributesRec(cursor);
                    if (rec.rExportSlot >= 0) {
                        mSlot_ExportNames[rec.rExportSlot] = rec.rExportSlotName;
                        mSlot_SleepStages[rec.rExportSlot] = rec.rAppliesToStage;
                    }
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".preload_slotInfo_internal", e);   // automatically posts a Log.e
        }
        if (cursor != null) { cursor.close(); }
    }

    // get a specific attribute record
    private CompanionAttributesRec getSpecificAttributeRec(String attributeDisplayName) {
        if (mInvalidDB) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        String where = CompanionDatabaseContract.CompanionAttributes.COLUMN_ATTRIBUTE_DISPLAY_NAME + "=?";
        String[] values = { attributeDisplayName };
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionDatabaseContract.CompanionAttributes.TABLE_NAME,   // table name
                    CompanionDatabaseContract.CompanionAttributes.PROJECTION,   // columns to get
                    where,   // columns for optional WHERE clause
                    values,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    null);    // sort order
            if (cursor == null) { return null; }
            if (!cursor.moveToFirst()) { cursor.close(); return null; }
            CompanionAttributesRec rec = new CompanionAttributesRec(cursor);
            cursor.close();
            return rec;
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getSpecificAttributeRec", e, "Attribute="+attributeDisplayName);   // automatically posts a Log.e
        }
        if (cursor != null) { cursor.close(); }
        return null;
    }

    // get all AttributeValue definition records; sorted by ascending Likert value then display name then value
    public Cursor getAllAttributeValuesRecsSortedLikert() {
        if (mInvalidDB) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        String sortOrder = CompanionDatabaseContract.CompanionAttributeValues.COLUMN_VALUE_LIKERT + " ASC, " + CompanionDatabaseContract.CompanionAttributeValues.COLUMN_ATTRIBUTE_DISPLAY_NAME + " ASC, " + CompanionDatabaseContract.CompanionAttributeValues.COLUMN_VALUE + " ASC";
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionDatabaseContract.CompanionAttributeValues.TABLE_NAME,   // table name
                    CompanionDatabaseContract.CompanionAttributeValues.PROJECTION,   // columns to get
                    null,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    sortOrder );    // sort order
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getAttributeValuesRecsForAttributeSortedLikert", e);   // automatically posts a Log.e
            if (cursor != null) { cursor.close(); cursor = null; }
        }
        return cursor;
    }

    // get AttributeValue definition records for a specific Attribute; sorted by ascending Likert value then display name then value
    public Cursor getAttributeValuesRecsForAttributeSortedLikert(String attributeDisplayName) {
        if (mInvalidDB) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        String sortOrder = CompanionDatabaseContract.CompanionAttributeValues.COLUMN_VALUE_LIKERT + " ASC, " + CompanionDatabaseContract.CompanionAttributeValues.COLUMN_VALUE + " ASC";
        String where = CompanionDatabaseContract.CompanionAttributeValues.COLUMN_ATTRIBUTE_DISPLAY_NAME + "=?";
        String values[] = { attributeDisplayName };
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionDatabaseContract.CompanionAttributeValues.TABLE_NAME,   // table name
                    CompanionDatabaseContract.CompanionAttributeValues.PROJECTION,   // columns to get
                    where,   // columns for optional WHERE clause
                    values,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    sortOrder);    // sort order
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getAttributeValuesRecsForAttributeSortedLikert", e, "Attribute="+attributeDisplayName);   // automatically posts a Log.e
            if (cursor != null) { cursor.close(); cursor = null; }
        }
        return cursor;
    }

    // get all EventDoing definition records; sorted by ascending Doing name
    public Cursor getAllEventDoingsRecsSortedDoing() {
        if (mInvalidDB) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        String sortOrder = CompanionDatabaseContract.CompanionEventDoings.COLUMN_DOING + " ASC";
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionDatabaseContract.CompanionEventDoings.TABLE_NAME,   // table name
                    CompanionDatabaseContract.CompanionEventDoings.PROJECTION,   // columns to get
                    null,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    sortOrder );    // sort order
        } catch (SQLException e) {
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getAllEventDoingsRecsSortedDoing", e);   // automatically posts a Log.e
            if (cursor != null) { cursor.close(); cursor = null; }
        }
        return cursor;
    }

    //////////////////////////////////////////////////////////////////////
    // All the below methods are utilized for Zeo App Database replication
    // Many of these methods run in a separate thread as indicated
    //////////////////////////////////////////////////////////////////////

    // Thread context: main thread
    // return the quantity of zeo sleep records present; remember the table may not exist so need to detect that;
    // this gets called during App initialization, and the ZeoAppHandle will not have been instanciated
    public int getQtyZeoSleepEpisodeRecs() {
        SQLiteDatabase db = getReadableDatabase();
        return getQtyZeoSleepEpisodeRecs_internal(db);
    }
    public int getQtyZeoSleepEpisodeRecs_internal(SQLiteDatabase db) {
        if (mInvalidDB) { return -1; }
        String[] columns = { BaseColumns._ID };
        Cursor cursor = null;
        int qty = 0;
        try {
            cursor = db.query(
                    CompanionDatabaseContract.ZeoSleepRecords.TABLE_NAME,   // table name
                    columns,   // columns to get
                    null,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    null);    // sort order
            if (cursor != null) {
                qty = cursor.getCount();
                cursor.close();
            }
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("no such table")) {   // a "no such table" exception is expected here
                qty = -2;
            } else {
                if (ZeoCompanionApplication.mZeoAppHandler != null) { ZeoCompanionApplication.mZeoAppHandler.disableReplication(); }
                ZeoCompanionApplication.postToErrorLog(_CTAG + ".getQtyZeoSleepEpisodeRecs", e);    // automatically posts a Log.e
                qty = -1;
            }
            if (cursor != null) { cursor.close(); }

        }
        return qty;
    }

    // Thread context: main thread
    // get one specific Zeo Sleep record; remember the table may not exist so need to detect that
    public ZAH_SleepRecord getSpecifiedZeoSleepRec(long id) {
        if (mInvalidDB) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        String where = CompanionDatabaseContract.ZeoSleepRecords.COLUMN_SLEEP_EVENT_ID + "=?";
        String values[] = { String.valueOf(id) };
        ZAH_SleepRecord newRec = null;
        Cursor cursor = null;
        try {
            cursor =  db.query(
                    CompanionDatabaseContract.ZeoSleepRecords.TABLE_NAME,    // data manager, database and table name
                    CompanionDatabaseContract.ZeoSleepRecords.PROJECTION_AVAILABLE,          // columns to get
                    where,       // columns for optional WHERE clause
                    values,       // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    null); // sort order
            if (cursor == null) { return null; }
            if (!cursor.moveToFirst()) { cursor.close(); return null; }
            newRec = new ZAH_SleepRecord(cursor);
            cursor.close();
        } catch (SQLException e) {
            ZeoCompanionApplication.mZeoAppHandler.disableReplication();
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getSpecifiedZeoSleepRec", e, _CTAG + "ID="+id);   // automatically posts a Log.e
            if (cursor != null) { cursor.close(); newRec = null; }
        }
        return newRec;
    }

    // Thread context: main thread
    // get all the Zeo Sleep records; remember the table may not exist so need to detect that
    public Cursor getAllZeoSleepRecs() {
        if (mInvalidDB) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        String sortOrder = CompanionDatabaseContract.ZeoSleepRecords.COLUMN_START_OF_NIGHT + " DESC";
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionDatabaseContract.ZeoSleepRecords.TABLE_NAME,   // table name
                    CompanionDatabaseContract.ZeoSleepRecords.PROJECTION_AVAILABLE,   // columns to get
                    null,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    sortOrder );    // sort order
        } catch (SQLException e) {
            ZeoCompanionApplication.mZeoAppHandler.disableReplication();
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getAllZeoSleepRecs", e);  // automatically posts a Log.e
            if (cursor != null) { cursor.close(); }
            return null;
        }
        return cursor;
    }

    // Thread context: main thread
    // get only those Zeo Sleep records after the specified date; remember the table may not exist so need to detect that
    public Cursor getAllZeoSleepRecsAfterDate(long fromTimestamp) {
        if (mInvalidDB) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        String sortOrder = CompanionDatabaseContract.ZeoSleepRecords.COLUMN_START_OF_NIGHT + " DESC";
        String where = CompanionDatabaseContract.ZeoSleepRecords.COLUMN_START_OF_NIGHT + ">=?";
        String[] values = new String[] { String.valueOf(fromTimestamp) };
        Cursor cursor = null;
        try {
            cursor = db.query(
                    CompanionDatabaseContract.ZeoSleepRecords.TABLE_NAME,   // table name
                    CompanionDatabaseContract.ZeoSleepRecords.PROJECTION_AVAILABLE,   // columns to get
                    where,  // columns for optional WHERE clause
                    values, // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    sortOrder);    // sort order
        } catch (SQLException e) {
            ZeoCompanionApplication.mZeoAppHandler.disableReplication();
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getAllZeoSleepRecsAfterDate", e,  "Timestamp="+fromTimestamp); // automatically posts a Log.e
            if (cursor != null) { cursor.close(); cursor = null;}
        }
        return cursor;
    }

    // Thread context: main thread
    // purge all Zeo App replication tables
    public void purgeAllZeoTables() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.ZeoAlarmSnoozeEvents.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.ZeoAlarmTimeoutEvents.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.ZeoAlarmAlertEvents.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.ZeoSleepRecords.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.ZeoSleepEvents.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CompanionDatabaseContract.ZeoHeadbands.TABLE_NAME);
    }

    // Thread context: ReplicateZeoDatabase thread
    // get a list of all Zeo replicated tables currently in the database; can return null
    public String[] getAllZeoTables() {
        if (mInvalidDB) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        ArrayList<String> list = new ArrayList<String>();
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null);
            if (cursor == null) { return null; }
            if (!cursor.moveToFirst()) { cursor.close(); return null;  }
            do {
                String name = cursor.getString(0);
                for (int i = 0; i < CompanionDatabaseContract.ZEO_REPLICATE_TABLE_NAMES.length; i++) {
                    if (name.equals(CompanionDatabaseContract.ZEO_REPLICATE_TABLE_NAMES[i])) {
                        list.add(name);
                    }
                }
            } while (cursor.moveToNext());
        } catch (SQLException e) {
            ZeoCompanionApplication.mZeoAppHandler.disableReplication();
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getAllZeoTables", e);  // automatically posts a Log.e
        }
        if (cursor != null) { cursor.close(); }
        if (list.isEmpty()) { return null; }
        String [] stringsList = new String[list.size()];
        stringsList = list.toArray(stringsList);
        return stringsList;
    }

    // Thread context: ReplicateZeoDatabase thread
    // create a Zeo App data replication table in the ZeoCompanion database
    public boolean createZeoTable(String sqlDefinition) {
        if (mInvalidDB) { return false; }
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS  " + sqlDefinition);
            return true;
        } catch (SQLException e) {
            ZeoCompanionApplication.mZeoAppHandler.disableReplication();
            ZeoCompanionApplication.postToErrorLog( _CTAG + ".createZeoTable", e);   // automatically posts a Log.e
        }
        return false;
    }

    // Thread context: ReplicateZeoDatabase thread
    // get the record ID's of all records in the indicated table from the ZeoCompanion database
    public long[] getAllRecIDsZeoTable(String theTable, String sortOrder) {
        if (mInvalidDB) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = { BaseColumns._ID };
        Cursor cursor = null;
        try {
            cursor = db.query(
                    theTable,   // table name
                    columns,   // columns to get
                    null,   // columns for optional WHERE clause
                    null,   // values for optional WHERE clause
                    null,   // optional row groups
                    null,   // filter by row groups
                    sortOrder);    // sort order
            if (cursor == null) { return null; }
            if (cursor.moveToFirst()) {
                long[] retArray = new long[cursor.getCount()];
                int p = 0;
                do {
                    retArray[p] = cursor.getLong(0);
                    p++;
                } while (cursor.moveToNext());
                cursor.close();
                return retArray;
            }
        } catch (SQLException e) {
            ZeoCompanionApplication.mZeoAppHandler.disableReplication();
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".getAllRecIDsZeoTable", e, "For DB Table: "+theTable);  // automatically posts a Log.e
        }
        if (cursor != null) { cursor.close(); }
        return null;
    }

    // Thread context: ReplicateZeoDatabase thread
    // copy the ZeoCursor's current record content into the ZeoCompanion database
    public void putRecIntoZeoTable(String theTable, Cursor cursorZeo) {
        ContentValues values = new ContentValues();
        try {
            for (int i = 0; i < cursorZeo.getColumnCount(); i++) {
                switch(cursorZeo.getType(i)) {
                    case Cursor.FIELD_TYPE_NULL:
                        values.putNull(cursorZeo.getColumnName(i));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        values.put(cursorZeo.getColumnName(i), cursorZeo.getLong(i));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        values.put(cursorZeo.getColumnName(i), cursorZeo.getDouble(i));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        values.put(cursorZeo.getColumnName(i), cursorZeo.getString(i));
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        values.put(cursorZeo.getColumnName(i), cursorZeo.getBlob(i));
                        break;
                }
            }
            insertOrReplaceRecs(theTable, values);
        } catch (SQLException e) {
            ZeoCompanionApplication.mZeoAppHandler.disableReplication();
            ZeoCompanionApplication.postToErrorLog(_CTAG + ".putRecIntoZeoTable", e, "For DB Table: " + theTable);  // automatically posts a Log.e
        }
    }
}
