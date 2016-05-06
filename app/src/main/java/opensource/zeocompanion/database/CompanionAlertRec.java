package opensource.zeocompanion.database;

import android.content.ContentValues;
import android.database.Cursor;

// definitional record for the Alerts
public class CompanionAlertRec {
    // record members
    public long rID = -1;
    public long rTimestamp = 0;
    public String rMessage = null;

    // constructor #1:  by member elements
    public CompanionAlertRec(long timestamp, String message) {
        rTimestamp = timestamp;
        rMessage = message;
    }

    // constructor #2:  from a database query
    public CompanionAlertRec(Cursor cursor) {
        rID = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionAlerts._ID));
        rTimestamp = cursor.getLong(cursor.getColumnIndex(CompanionDatabaseContract.CompanionAlerts.COLUMN_TIMESTAMP));
        rMessage = cursor.getString(cursor.getColumnIndex(CompanionDatabaseContract.CompanionAlerts.COLUMN_MESSAGE));
    }

    // save the record to the database; if not already existing it will be added; if already existing it will be updated
    public void saveToDB(CompanionDatabase dbh) {
        ContentValues values = new ContentValues();
        if (rID > 0) { values.put(CompanionDatabaseContract.CompanionAlerts._ID, rID); }
        values.put(CompanionDatabaseContract.CompanionAlerts.COLUMN_TIMESTAMP, rTimestamp);
        values.put(CompanionDatabaseContract.CompanionAlerts.COLUMN_MESSAGE, rMessage);
        rID = dbh.insertOrReplaceRecs(CompanionDatabaseContract.CompanionAlerts.TABLE_NAME, values, true);  // must suppress further alerts in order to prevent a wide-scale infinite loop
    }

    // remove the indicated Alert record from the database;
    public static void removeFromDB(CompanionDatabase dbh, long id) {
        String where = CompanionDatabaseContract.CompanionAlerts._ID + "=?";
        String values[] = { String.valueOf(id) };
        dbh.deleteRecs(CompanionDatabaseContract.CompanionAlerts.TABLE_NAME, where, values);
    }
}
