package opensource.zeocompanion.database;

import android.content.ContentValues;
import android.database.Cursor;

// definitional record for the Attributes allowed within the Sleep Journal
public class CompanionAttributesRec {
    // record members
    public String rAttributeDisplayName = null;
    public int rAppliesToStage = 0;
    public int rDisplay_order = 0;
    public int rFlags = 0;
    public int rExportSlot = 0;
    public String rExportSlotName = null;

    // constructor #1:  by member elements
    public CompanionAttributesRec(String attributeDisplayName, int appliesToStage, int displayOrder, int flags, int exportSlot, String exportSlotName) {
        rAttributeDisplayName = attributeDisplayName;
        rAppliesToStage = appliesToStage;
        rDisplay_order = displayOrder;
        rFlags = flags;
        rExportSlot = exportSlot;
        rExportSlotName = exportSlotName;
    }

    // constructor #2:  copy an existing record
    public CompanionAttributesRec(CompanionAttributesRec sourceRec) {
        rAttributeDisplayName = sourceRec.rAttributeDisplayName;
        rAppliesToStage = sourceRec.rAppliesToStage;
        rDisplay_order = sourceRec.rDisplay_order;
        rFlags = sourceRec.rFlags;
        rExportSlot = sourceRec.rExportSlot;
        rExportSlotName = sourceRec.rExportSlotName;
    }

    // constructor #3:  from a database query
    public CompanionAttributesRec(Cursor cursor) {
        rAttributeDisplayName = cursor.getString(cursor.getColumnIndex(CompanionDatabaseContract.CompanionAttributes.COLUMN_ATTRIBUTE_DISPLAY_NAME));
        rAppliesToStage = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionAttributes.COLUMN_APPLIES_TO_STAGE));
        rDisplay_order = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionAttributes.COLUMN_DISPLAY_ORDER));
        rFlags = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionAttributes.COLUMN_FLAGS));
        rExportSlot = cursor.getInt(cursor.getColumnIndex(CompanionDatabaseContract.CompanionAttributes.COLUMN_EXPORT_SLOT));
        rExportSlotName = cursor.getString(cursor.getColumnIndex(CompanionDatabaseContract.CompanionAttributes.COLUMN_EXPORT_SLOT_NAME));
    }

    // save the record to the database; if not already existing it will be added; if already existing it will be updated;
    public void saveToDB(CompanionDatabase dbh) {
        ContentValues values = new ContentValues();
        values.put(CompanionDatabaseContract.CompanionAttributes.COLUMN_ATTRIBUTE_DISPLAY_NAME, rAttributeDisplayName);
        values.put(CompanionDatabaseContract.CompanionAttributes.COLUMN_APPLIES_TO_STAGE, rAppliesToStage);
        values.put(CompanionDatabaseContract.CompanionAttributes.COLUMN_DISPLAY_ORDER, rDisplay_order);
        values.put(CompanionDatabaseContract.CompanionAttributes.COLUMN_FLAGS, rFlags);
        values.put(CompanionDatabaseContract.CompanionAttributes.COLUMN_EXPORT_SLOT, rExportSlot);
        if (rExportSlotName == null) { values.put(CompanionDatabaseContract.CompanionAttributes.COLUMN_EXPORT_SLOT_NAME, ""); }
        else { values.put(CompanionDatabaseContract.CompanionAttributes.COLUMN_EXPORT_SLOT_NAME, rExportSlotName); }
        dbh.insertOrReplaceRecs(CompanionDatabaseContract.CompanionAttributes.TABLE_NAME, values);
        dbh.mDefinitionsChanged = true;
        dbh.preload_slotInfo();
    }

    // remove the indicated Attribute record from the database;
    // note:  higher level methods MUST also remove corresponding CompanionAttributeValues records
    public static void removeFromDB(CompanionDatabase dbh, String attributeDisplayName) {
        String where = CompanionDatabaseContract.CompanionAttributes.COLUMN_ATTRIBUTE_DISPLAY_NAME + "=?";
        String values[] = { attributeDisplayName };
        dbh.deleteRecs(CompanionDatabaseContract.CompanionAttributes.TABLE_NAME, where, values);
        dbh.mDefinitionsChanged = true;
    }
}
