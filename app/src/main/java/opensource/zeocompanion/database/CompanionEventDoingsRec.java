package opensource.zeocompanion.database;

import android.content.ContentValues;
import android.database.Cursor;

// definitional record for the Event Doings allowed within the Sleep Journal
public class CompanionEventDoingsRec {
    // record members
    public String rDoing = null;
    public int rAppliesToStages = 0;
    public int rIsDefaultPriority = 0;

    // constructor #1:  by member elements
    public CompanionEventDoingsRec(String doing, int appliesToStage, int isDefaultPriority) {
        rDoing = doing;
        rAppliesToStages = appliesToStage;
        rIsDefaultPriority = isDefaultPriority;
    }

    // constructor #2:  copy an existing record
    public CompanionEventDoingsRec(CompanionEventDoingsRec sourceRec) {
        rDoing = sourceRec.rDoing;
        rAppliesToStages = sourceRec.rAppliesToStages;
        rIsDefaultPriority = sourceRec.rIsDefaultPriority;
    }

    // constructor #3:  from a database query
    public CompanionEventDoingsRec(Cursor cursor) {
        rDoing = cursor.getString(cursor.getColumnIndex(CompanionDatabaseContract.CompanionEventDoings.COLUMN_DOING));
        rAppliesToStages = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionEventDoings.COLUMN_APPLIES_TO_STAGES));
        rIsDefaultPriority = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionEventDoings.COLUMN_DOING_IS_DEFAULT_PRIORITY));
    }

    // save the record to the database; if not already existing it will be added; if already existing it will be updated;
    public void saveToDB(CompanionDatabase dbh) {
        ContentValues values = new ContentValues();
        values.put(CompanionDatabaseContract.CompanionEventDoings.COLUMN_DOING, rDoing);
        values.put(CompanionDatabaseContract.CompanionEventDoings.COLUMN_APPLIES_TO_STAGES, rAppliesToStages);
        values.put(CompanionDatabaseContract.CompanionEventDoings.COLUMN_DOING_IS_DEFAULT_PRIORITY, rIsDefaultPriority);
        dbh.insertOrReplaceRecs(CompanionDatabaseContract.CompanionEventDoings.TABLE_NAME, values);
        dbh.mDefinitionsChanged = true;
    }

    // remove the indicated Attribute record from the database;
    public static void removeFromDB(CompanionDatabase dbh, String doing) {
        String where = CompanionDatabaseContract.CompanionEventDoings.COLUMN_DOING + "=?";
        String values[] = { doing };
        dbh.deleteRecs(CompanionDatabaseContract.CompanionEventDoings.TABLE_NAME, where, values);
        dbh.mDefinitionsChanged = true;
    }
}
