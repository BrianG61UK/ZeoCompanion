package opensource.zeocompanion.database;

// a parsed record of Info (Attribute=Value) that is normally stored in string format
public class CompanionSleepEpisodeInfoParsedRec {
    // record fields
    public int rSleepStage = 0;
    public String rAttributeExportName = null;
    public String rValue = null;
    public float rLikert = (float)0.0;

    // optional non-record fields used to assist placement into storage strings
    public int mExportSlot = -1;    // slot#0 is EXPORT_FIELD_SLOT_MORNING_FEEL
    public static final String _CTAG = "SIR";

    // constructor #1:  defined values usually by being set by end-user choice
    // note that attributeExportName and value cannot have a comma or semicolon in them, and will result in a partially empty record
    public CompanionSleepEpisodeInfoParsedRec(int slot, int sleepStage, String attributeExportName, String value, float likert) {
        rSleepStage = sleepStage;
        if (!attributeExportName.contains(",") && !attributeExportName.contains(";")) { rAttributeExportName = attributeExportName; }
        if (!value.contains(",") && !value.contains(";")) { rValue = value; }
        rLikert = likert;
        mExportSlot = slot;
    }

    // constructor #2:  parse a string field from the database
    // note if a damaged fieldStr is passed in, the constructor will build a partially empty record
    public CompanionSleepEpisodeInfoParsedRec(int slot, String fieldStr) {
        mExportSlot = slot;
        String[] splitString = fieldStr.split(";", -1);
        if (splitString.length == 0) { return; }
        if (slot >= 0) {
            // fixed slot entry; this is only supposed to have likert;value in it
            if (splitString.length <= 2) {
                if (splitString.length >= 1) {
                    if (splitString[0] == null) { rLikert = (float)0.0; }
                    else { rLikert = Float.parseFloat(splitString[0]); }
                }
                if (splitString.length >= 2) {
                    if (splitString[1] == null) { rValue = null; }
                    else if (splitString[1].isEmpty()) { rValue = null; }
                    else {  rValue = splitString[1]; }
                }
                rSleepStage = CompanionDatabase.mSlot_SleepStages[slot];
                rAttributeExportName = CompanionDatabase.mSlot_ExportNames[slot];
                return;
            }
        }

        // variable non-slotted (or maybe an overloaded fixed slot
        // it should have sleepStage;attribute;likert;value in it
        if (splitString.length >= 1) {
            if (splitString[0] == null) { rSleepStage = 0; }
            else if (splitString[0].isEmpty()) { rSleepStage = 0; }
            else {  rSleepStage = Integer.parseInt(splitString[0]); }
        }
        if (splitString.length >= 2) {
            if (splitString[1] == null) { rAttributeExportName = null; }
            else if (splitString[1].isEmpty()) { rAttributeExportName = null; }
            else {  rAttributeExportName = splitString[1]; }
        }
        if (splitString.length >= 3) {
            if (splitString[2] == null) { rLikert = (float)0.0; }
            else {rLikert = Float.parseFloat(splitString[2]); }
        }
        if (splitString.length >= 4) {
            if (splitString[3] == null) { rValue = null; }
            else if (splitString[3].isEmpty()) { rValue = null; }
            else {  rValue = splitString[3]; }
        }
    }

    // build the field storage string for this record entry
    public String getStorageString() {
        String str = "";
        if (mExportSlot < 0) {
            str = str + rSleepStage + ";";
            if (rAttributeExportName != null) { str = str + rAttributeExportName; }
            str = str + ";";
        }
        str = str + rLikert + ";";
        if (rValue != null) { str = str + rValue; }
        return str;
    }

    // build a string of the record's contents for the MainSummary tab; do not include the SleepStage
    public String getSummaryString() {
        return rAttributeExportName + ": " + rValue + " (" + rLikert + ")";
    }
}
