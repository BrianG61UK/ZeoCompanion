package opensource.zeocompanion.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// definitional record for the Attribute Values allowed within the Sleep Journal
public class CompanionAttributeValuesRec {
    // record members
    public String rAttributeDisplayName = null;
    public String rValue = null;
    public float rLikert = (float)0.0;

    // constructor #1:  by member elements
    public CompanionAttributeValuesRec(String attributeDisplayName, String value, float likert) {
        rAttributeDisplayName = attributeDisplayName;
        rValue = value;
        rLikert = likert;
    }

    // constructor #2:  copy an existing record
    public CompanionAttributeValuesRec(CompanionAttributeValuesRec sourceRec) {
        rAttributeDisplayName = sourceRec.rAttributeDisplayName;
        rValue = sourceRec.rValue;
        rLikert = sourceRec.rLikert;
    }

    // constructor #3:  from a database query
    public CompanionAttributeValuesRec(Cursor cursor) {
        rAttributeDisplayName = cursor.getString(cursor.getColumnIndex(CompanionDatabaseContract.CompanionAttributeValues.COLUMN_ATTRIBUTE_DISPLAY_NAME));
        rValue = cursor.getString(cursor.getColumnIndex(CompanionDatabaseContract.CompanionAttributeValues.COLUMN_VALUE));
        rLikert = cursor.getFloat(cursor.getColumnIndex(CompanionDatabaseContract.CompanionAttributeValues.COLUMN_VALUE_LIKERT));
    }

    // save the record to the database; if not already existing it will be added; if already existing it will be updated;
    public void saveToDB(CompanionDatabase dbh) {
        ContentValues values = new ContentValues();
        values.put(CompanionDatabaseContract.CompanionAttributeValues.COLUMN_ATTRIBUTE_DISPLAY_NAME, rAttributeDisplayName);
        values.put(CompanionDatabaseContract.CompanionAttributeValues.COLUMN_VALUE, rValue);
        values.put(CompanionDatabaseContract.CompanionAttributeValues.COLUMN_VALUE_LIKERT, rLikert);
        dbh.insertOrReplaceRecs(CompanionDatabaseContract.CompanionAttributeValues.TABLE_NAME, values);
        dbh.mDefinitionsChanged = true;
    }

    // remove the indicated AttributeValues record from the database;
    public static void removeFromDB(CompanionDatabase dbh, String attributeDisplayName, String value) {
        String where = CompanionDatabaseContract.CompanionAttributeValues.COLUMN_ATTRIBUTE_DISPLAY_NAME + "=? AND " + CompanionDatabaseContract.CompanionAttributeValues.COLUMN_VALUE + "=?";
        String values[] = { attributeDisplayName, value };
        dbh.deleteRecs(CompanionDatabaseContract.CompanionAttributeValues.TABLE_NAME, where, values);
        dbh.mDefinitionsChanged = true;
    }

    // delete a set of AttributeValues records with a specified AttributeDisplayName
    public static void removeAllWithAttribute(CompanionDatabase dbh, String attributeDisplayName) {
        String where = CompanionDatabaseContract.CompanionAttributeValues.COLUMN_ATTRIBUTE_DISPLAY_NAME + "=?";
        String values[] = { attributeDisplayName };
        dbh.deleteRecs(CompanionDatabaseContract.CompanionAttributeValues.TABLE_NAME, where, values);
        dbh.mDefinitionsChanged = true;
    }

    // rename a set of AttributeValues records to a new AttributeDisplayName
    public static void renameAllWithAttribute(CompanionDatabase dbh, String oldAttributeDisplayName, String newAttributeDisplayName) {
        SQLiteDatabase db = dbh.getReadableDatabase();
        String where = CompanionDatabaseContract.CompanionAttributeValues.COLUMN_ATTRIBUTE_DISPLAY_NAME + "=?";
        String values[] = { oldAttributeDisplayName };
        Cursor cursor = db.query(
                CompanionDatabaseContract.CompanionAttributeValues.TABLE_NAME,   // table name
                CompanionDatabaseContract.CompanionAttributeValues.PROJECTION,   // columns to get
                where,   // columns for optional WHERE clause
                values,   // values for optional WHERE clause
                null,   // optional row groups
                null,   // filter by row groups
                null );    // sort order
        if (cursor == null) { return; }
        if (cursor.moveToFirst()) {
            do {
                CompanionAttributeValuesRec rec = new CompanionAttributeValuesRec(cursor);
                rec.rAttributeDisplayName = newAttributeDisplayName;
                rec.saveToDB(dbh);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}
