package opensource.zeocompanion.database;

import android.content.ContentValues;
import android.database.Cursor;

// definitional record for the System
public class CompanionSystemRec {
    // record members
    public long rID = -1;
    public String rPrior_to_upgrade_AppVer = null;
    public int rPrior_to_upgrade_DBVer = 0;
    public String rMost_recent_AppVer = null;
    public int rMost_recent_DBVer = 0;
    public String rUserName = null;

    // constructor #1:  by member elements
    public CompanionSystemRec(String priorAppVer, int priorDBver, String currAppVer, int currDBver) {
        rPrior_to_upgrade_AppVer = priorAppVer;
        rPrior_to_upgrade_DBVer = priorDBver;
        rMost_recent_AppVer = currAppVer;
        rMost_recent_DBVer = currDBver;
    }

    // constructor #2:  from a database query
    public CompanionSystemRec(Cursor cursor) {
        rID = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionAlerts._ID));
        rPrior_to_upgrade_AppVer = cursor.getString(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSystem.COLUMN_PRIOR_TO_UPGRADE_APP_VERSION));
        rPrior_to_upgrade_DBVer = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSystem.COLUMN_PRIOR_TO_UPGRADE_DB_VERSION));
        rMost_recent_AppVer = cursor.getString(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSystem.COLUMN_MOST_RECENT_APP_VERSION));
        rMost_recent_DBVer = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSystem.COLUMN_MOST_RECENT_DB_VERSION));
        rUserName = cursor.getString(cursor.getColumnIndex(CompanionDatabaseContract.CompanionSystem.COLUMN_USER_NAME));
    }

    // save the record to the database; if not already existing it will be added; if already existing it will be updated
    public void saveToDB(CompanionDatabase dbh) {
        ContentValues values = saveToDB_internal();
        rID = dbh.insertOrReplaceRecs(CompanionDatabaseContract.CompanionSystem.TABLE_NAME, values);
    }
    public ContentValues saveToDB_internal() {
        ContentValues values = new ContentValues();
        if (rID > 0) { values.put(CompanionDatabaseContract.CompanionSystem._ID, rID); }    // note for this specific record rID should always be 1
        if (rPrior_to_upgrade_AppVer == null) { values.putNull(CompanionDatabaseContract.CompanionSystem.COLUMN_PRIOR_TO_UPGRADE_APP_VERSION); }
        else { values.put(CompanionDatabaseContract.CompanionSystem.COLUMN_PRIOR_TO_UPGRADE_APP_VERSION, rPrior_to_upgrade_AppVer); }
        values.put(CompanionDatabaseContract.CompanionSystem.COLUMN_PRIOR_TO_UPGRADE_DB_VERSION, rPrior_to_upgrade_DBVer);
        if (rMost_recent_AppVer == null) { values.putNull(CompanionDatabaseContract.CompanionSystem.COLUMN_MOST_RECENT_APP_VERSION); }
        else { values.put(CompanionDatabaseContract.CompanionSystem.COLUMN_MOST_RECENT_APP_VERSION, rMost_recent_AppVer); }
        values.put(CompanionDatabaseContract.CompanionSystem.COLUMN_MOST_RECENT_DB_VERSION, rMost_recent_DBVer);
        if (rUserName == null) { values.putNull(CompanionDatabaseContract.CompanionSystem.COLUMN_USER_NAME); }
        else { values.put(CompanionDatabaseContract.CompanionSystem.COLUMN_USER_NAME, rUserName); }
        return values;
    }
}
