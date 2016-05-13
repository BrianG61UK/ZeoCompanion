package opensource.zeocompanion.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import com.myzeo.android.api.data.MyZeoExportDataContract;
import com.myzeo.android.api.data.ZeoDataContract;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import opensource.zeocompanion.BuildConfig;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.activities.SharingActivity;
import opensource.zeocompanion.database.CompanionDatabase;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.database.CompanionSleepEpisodeEventsParsedRec;
import opensource.zeocompanion.database.CompanionSleepEpisodeInfoParsedRec;
import opensource.zeocompanion.zeo.ZAH_SleepRecord;

// utility class that performs CSV file creation, selection of data, and formatting of data
public class CSVexporter {
    // member variables
    private Context mContext = null;
    private boolean mIncludeAmended = true;
    private boolean mAmendedPlaceFirst = false;
    private boolean mUseDBslotHeaders = true;
    private boolean mIncludeValueText = true;
    private String mCSVdirectory = null;
    private String mName = null;
    private long mSequenceNumber = 0;

    // member constants and other static content
    private static final String _CTAG = "CEU";

    public static final int SHARE_WHAT_CSV_EXCEL = 1;
    public static final int SHARE_WHAT_CSV_ZEOVIEWER = 2;
    public static final int SHARE_WHAT_CSV_SLEEPYHEAD = 3;
    public static final int SHARE_WHAT_CSV_ZEORAW = 4;
    public static final int SHARE_WHAT_CSV_ZEORAW_DEAD = 5;
    public static final int SHARE_WHAT_CSV_COUNT = 5;

    SimpleDateFormat mSDF1 = new SimpleDateFormat("MM/dd/yyyy");        // this date format is pre-determined by the existing myZeo export format
    SimpleDateFormat mSDF2 = new SimpleDateFormat("MM/dd/yyyy HH:mm");  // this date format is pre-determined by the existing myZeo export format
    SimpleDateFormat mSDF2s = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    SimpleDateFormat mSDF3 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");   // this format is for file names

    // return class
    public class ReturnResults {
        public File rTheExportFile = null;
        public String rAnErrorMessage = "";

        public ReturnResults (File theExportFile, String anErrorMessage) {
            rTheExportFile = theExportFile;
            rAnErrorMessage = anErrorMessage;
        }
    }

    // column names only for the Zeo Raw export format;
    // data format was originated by the ZeoMobile CSV export example app; however we are adding the "id" column and the extended data columns
    private static final String[] ZEO_COLUMN_NAMES_RAW =  {
            "id",
            "localized_start_of_night", "start_of_night", "end_of_night", "timezone", "zq_score", "qty_awakenings",
            "time_in_deep_30s", "time_in_light_30s", "time_in_rem_30s", "time_in_wake_30s", "time_to_z_30s", "total_z_30s",
            "source", "end_reason", "len_display_hypnogram_5m", "len_base_hypnogram_30s", "display_hypnogram_5m", "base_hypnogram_30s",
            // extended data columns
            "headband_id", "battery_voltage", "sleep_valid", "valid", "valid_for_history", "hidden", "clock_offset", "deep_sum", "light_changed_to_deep",
            "created_on", "modified_on", "uploaded_on", "start_of_night_myZeo", "start_of_night_orig", "start_of_display_hypnogram", "insufficient_data", "insufficient_data_starttime"
    };

    // column names for an amended sleep record section in the main export format
    private static final String[] EXTENDED_SLEEP_RECORD_COLUMN_NAMES =  {
            "Sleep Graphs Starttime", "Start of Night Headband", "Battery Start of Night Highest", "Battery End of Night Lowest",
            "Impedance Start of Night", "Impedance End of Night", "Light Changed to Deep", "Deep Sum"
    };

    // column names for an amended sleep record section in the main export format
    private static final String[] AMENDED_COLUMN_NAMES_SUFFIX =  {
            "ZQ", "Total Z", "Time to Z", "Time in Wake", "Time in REM", "Time in Light", "Time in Deep",
            "Awakenings", "Light Changed to Deep", "Deep Sum", "Sleep Graph", "Detailed Sleep Graph"
    };

    // constructor; the Activity or ZeoApp context is needed for some methods
    public CSVexporter(Context context) { mContext = context; }

    // create the contents in the export; this method performs a query on the database for all records that match fromWhen and other criteria
    public ReturnResults createFile(int shareWhat, Date fromWhen) {
        File theExportFile = null;
        ArrayList<JournalDataCoordinator.IntegratedHistoryRec> selectedIrecs = new ArrayList<JournalDataCoordinator.IntegratedHistoryRec>();
        if (shareWhat == SHARE_WHAT_CSV_ZEORAW || shareWhat == SHARE_WHAT_CSV_ZEORAW_DEAD) {
            boolean includeZeoDead = false;
            if (shareWhat == SHARE_WHAT_CSV_ZEORAW_DEAD) { includeZeoDead = true; }
            ZeoCompanionApplication.mCoordinator.getAllZeoRecsFromDate(selectedIrecs, fromWhen, includeZeoDead);
            //Log.d(_CTAG + ".createFile", "Selected " + selectedIrecs.size() + " zeo-only records for export from date " + fromWhen);
        } else {
            ZeoCompanionApplication.mCoordinator.getAllIntegratedHistoryRecsFromDate(selectedIrecs, fromWhen);
            //Log.d(_CTAG + ".createFile", "Selected " + selectedIrecs.size() + " integrated records for export from date " + fromWhen);
        }

        // generate the proper file
        ReturnResults createResults = createFileFromData(selectedIrecs, shareWhat);

        // though disputed, assist garbage collection by explicitly and entirely destroying the contents and subcontents of each selected IntegratedHistoryRec
        for (JournalDataCoordinator.IntegratedHistoryRec iRec: selectedIrecs) { iRec.destroy(); }
        selectedIrecs.clear();
        return createResults;
    }

    // create the contents in the export based upon the passed set of IntegratedHistoryRecs
    public ReturnResults createFileFromData(ArrayList<JournalDataCoordinator.IntegratedHistoryRec> theIRecs, int shareWhat) {
        String errString = "";
        if (theIRecs.isEmpty()) { new ReturnResults(null, "No records were selected to export"); }

        ReturnResults prepResults = prepAndCreateFile(shareWhat);
        if (prepResults.rTheExportFile == null || !prepResults.rAnErrorMessage.isEmpty()) { return prepResults; }

        if (shareWhat == SHARE_WHAT_CSV_ZEORAW || shareWhat == SHARE_WHAT_CSV_ZEORAW_DEAD) {
            // the end-user wants a limited zeoMobile Raw formatted export
            try {
                FileOutputStream f = new FileOutputStream(prepResults.rTheExportFile);
                OutputStreamWriter wrt = new OutputStreamWriter(f);
                wrt.append(buildZeoRawHeaderLine() + "\r\n");
                for (JournalDataCoordinator.IntegratedHistoryRec iRec : theIRecs) {
                    wrt.append(buildOneZeoRawLine(iRec.theZAH_SleepRecord) + "\r\n");
                }
                wrt.flush();
                wrt.close();
                f.close();
            } catch (Exception e) {
                errString = "File IO error: zeoDB export file ("+prepResults.rTheExportFile.getAbsoluteFile()+"): " + e.getMessage();
                Log.e(_CTAG+".createFileFromData", "File IO error: zeoDB export file ("+prepResults.rTheExportFile.getAbsoluteFile()+"): " + e.toString());
            }
        } else {
            // the end-user wants a fully integrated myZeo formatted export
            try {
                FileOutputStream f = new FileOutputStream(prepResults.rTheExportFile);
                OutputStreamWriter wrt = new OutputStreamWriter(f);
                wrt.append(buildHeaderLine(shareWhat) + "\r\n");
                for (JournalDataCoordinator.IntegratedHistoryRec iRec : theIRecs) {
                    wrt.append(buildOneLine(iRec, shareWhat) + "\r\n");
                }
                wrt.flush();
                wrt.close();
                f.close();
            } catch (Exception e) {
                errString = "File IO error: myZeo+ export file ("+prepResults.rTheExportFile.getAbsoluteFile()+"): " + e.getMessage();
                Log.e(_CTAG+".createFileFromData", "File IO error: myZeo+ export file ("+prepResults.rTheExportFile.getAbsoluteFile()+"): " + e.toString());
            }
        }
        return new ReturnResults(prepResults.rTheExportFile, errString);
    }

    // create the contents in the export; this method creates an export file for only one record that was selected by the end-user
    public ReturnResults createFileOneRec(JournalDataCoordinator.IntegratedHistoryRec iRec, int shareWhat) {
        boolean zeoRaw = false;
        if (shareWhat == SHARE_WHAT_CSV_ZEORAW || shareWhat == SHARE_WHAT_CSV_ZEORAW_DEAD) { zeoRaw = true; }

        String errString = "";
        ReturnResults prepResults = prepAndCreateFile(shareWhat);
        if (prepResults.rTheExportFile == null || !prepResults.rAnErrorMessage.isEmpty()) { return prepResults; }

        try {
            FileOutputStream f = new FileOutputStream(prepResults.rTheExportFile);
            OutputStreamWriter wrt = new OutputStreamWriter(f);
            if (zeoRaw) {
                wrt.append(buildZeoRawHeaderLine() + "\r\n");
                if (iRec.theZAH_SleepRecord != null) {
                    wrt.append(buildOneZeoRawLine(iRec.theZAH_SleepRecord)+"\r\n");
                }
            } else {
                wrt.append(buildHeaderLine(shareWhat) + "\r\n");
                if (iRec.theZAH_SleepRecord != null || iRec.theCSErecord != null) {
                    wrt.append(buildOneLine(iRec, shareWhat) + "\r\n");
                }
            }
            wrt.flush();
            wrt.close();
            f.close();
        } catch (Exception e) {
            errString = "File IO error: myZeo+ export file ("+prepResults.rTheExportFile.getAbsoluteFile()+"): " + e.getMessage();
            Log.e(_CTAG+".createFileOneRec", "File IO error: myZeo+ export file ("+prepResults.rTheExportFile.getAbsoluteFile()+"): " + e.toString());
        }
        // because this IntegratedHistoryRec is ultimately held by the MainHistoryFragment in its larger list, do NOT destroy it
        return new ReturnResults(prepResults.rTheExportFile, errString);
    }

    // ensure external storage is available and R/W, create the new export file, and do other preparations
    // returns NULL if preparations failed, otherwise returns a FILE object for the created file
    private ReturnResults prepAndCreateFile(int shareWhat) {
        // is external storage available, read/write, and App has been granted permission
        int r = ZeoCompanionApplication.checkExternalStorage();
        if (r == -2) { return new ReturnResults(null, "Permission for App to write to external storage has not been granted; please grant the permission"); }
        else if (r != 0) { return new ReturnResults(null, "External Storage is not available; export file not created"); }

        // get all the necessary preferences
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mIncludeAmended = sPrefs.getBoolean("export_amended_include", true);
        mAmendedPlaceFirst = sPrefs.getBoolean("export_amended_placeFirst", false);
        mUseDBslotHeaders = sPrefs.getBoolean("export_attribute_useExportNameInColumnHeader", true);
        mIncludeValueText = sPrefs.getBoolean("export_value_exportIncludeValueText", true);
        mName = sPrefs.getString("profile_name", "");
        mCSVdirectory = sPrefs.getString("export_directory_CSV", "Android/data/opensource.zeocompanion/exports");

        // create the directory path to our exports subdirectory in external storage
        File exportsDir = null;
        if (mCSVdirectory != null) {
            exportsDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + mCSVdirectory);
        }
        else {
            exportsDir = new File(ZeoCompanionApplication.mBaseExtStorageDir + File.separator + "exports");
        }
        exportsDir.mkdirs();

        // compose the export file name
        String str = "ZeoCompanion_CSV_";
        if (mName != null) {
            if (!mName.isEmpty()) { str = str + mName + "_"; }
        }
        switch (shareWhat) {
            case SHARE_WHAT_CSV_EXCEL:
                str = str + "SS-myZeo-Minutes";
                break;
            case SHARE_WHAT_CSV_ZEOVIEWER:
                str = str + "ZeoDataViewer-myZeo-Minutes";
                break;
            case SHARE_WHAT_CSV_SLEEPYHEAD:
                str = str + "Sleepyhead-myZeo-Epochs";
                break;
            case SHARE_WHAT_CSV_ZEORAW:
                str = str + "SS-ZeoDB-Epochs";
                break;
            case SHARE_WHAT_CSV_ZEORAW_DEAD:
                str = str + "SS-ZeoDB-Epochs";
                break;
        }
        str = str + "_" + mSDF3.format(new Date()) + "_" + mSequenceNumber + ".csv";
        File f = new File(exportsDir.getAbsolutePath() + File.separator + str);
        mSequenceNumber++;

        // create the new empty file
        String errString = "";
        try {
            f.createNewFile();
            if (!f.isFile()) {
                errString = "File IO error: could not create export file";
                f = null;
            }
        } catch (Exception e) {
            errString = "File IO error: export file ("+f.getAbsoluteFile()+"): " + e.getMessage();
            Log.e(_CTAG+".prepAndCreateFile", "File IO error: export file ("+f.getAbsoluteFile()+"): " + e.toString());
        }

        return new ReturnResults(f, errString);
    }

    // build the headers line for the ZeoMobile raw export file
    private String buildZeoRawHeaderLine() {
        String str = "";
        int i = 0;
        int m = ZEO_COLUMN_NAMES_RAW.length - 1;
        while (i < m) {
            str = str + ZEO_COLUMN_NAMES_RAW[i] + ",";
            i++;
        }
        str = str + ZEO_COLUMN_NAMES_RAW[i];
        return str;
    }

    // build one export data line for one ZeoMobile Sleep Record during a ZeoMobile Raw formatted export
    private String buildOneZeoRawLine(ZAH_SleepRecord zRec) {
        String str = zRec.rSleepEpisodeID + "," + zRec.rLocalizedStartOfNight + "," + zRec.rStartOfNight + "," + zRec.rEndOfNight + "," + zRec.rTimezone + ",";
        str = str + zRec.rZQ_Score + "," + zRec.rCountAwakenings + ",";
        str = str + String.format("%.0f", zRec.rTime_Deep_min * 2) + "," + String.format("%.0f", zRec.rTime_Light_min * 2) + "," + String.format("%.0f", zRec.rTime_REM_min * 2) + ",";
        str = str + String.format("%.0f", zRec.rTime_Awake_min * 2) + "," + String.format("%.0f", zRec.rTime_to_Z_min * 2) + "," + String.format("%.0f", zRec.rTime_Total_Z_min * 2) + ",";
        str = str + zRec.rDataSource + "," + zRec.rEndReason + "," + zRec.rDisplay_Hypnogram_Count + "," + zRec.rBase_Hypnogram_Count + ",";

        for (int i = 0; i < zRec.rDisplay_Hypnogram.length; i++) {
            str = str + Byte.toString(zRec.rDisplay_Hypnogram[i]);
        }
        str = str + ",";

        for (int i = 0; i < zRec.rBase_Hypnogram.length; i++) {
            str = str + Byte.toString(zRec.rBase_Hypnogram[i]);
        }
        str = str + "," + zRec.rHeadbandID;

        if (zRec.mHasExtended) {
            str = str + "," + zRec.rVoltageBattery + "," + zRec.rSleepValid + "," + zRec.rValid + "," + zRec.rValidForHistory + "," + zRec.rHidden + ",";
            str = str + zRec.rClockOffset + "," + zRec.rDeepSum + "," + String.format("%.0f", zRec.rLightChangedToDeep_min * 2) + ",";
            str = str + zRec.rCreated_timestamp + "," + zRec.rUpdated_timestamp + "," + zRec.rUploaded_timestamp + ",";
            str = str + zRec.rStartOfNightMyZeo + "," + zRec.rStartOfNightOrig + "," + zRec.rDisplayHypnogramStartTime + ",";
            str = str + zRec.rInsufficientData + "," + zRec.rInsufficientDataStartTime;
        }
        return str;
    }

    // build the headers line for the export file, either as ZeoMobile raw or myZeo
    private String buildHeaderLine(int shareWhat) {
        // Original myZeo section
        boolean useDBslotHeaders = mUseDBslotHeaders;
        if (shareWhat == SHARE_WHAT_CSV_SLEEPYHEAD || shareWhat == SHARE_WHAT_CSV_ZEOVIEWER) { useDBslotHeaders = false; }
        String str = "";
        int i = 0;
        int m = MyZeoExportDataContract.EXPORT_COLUMN_ORDER_NAMES.length;
        int n = MyZeoExportDataContract.EXPORT_FIELD_SLOTS_FIRST_WITHIN_COLNAMES + MyZeoExportDataContract.EXPORT_FIELD_SLOTS_TOTAL;
        while (i < m) {
            if (useDBslotHeaders && (i >= MyZeoExportDataContract.EXPORT_FIELD_SLOTS_FIRST_WITHIN_COLNAMES && i < n)) {
                str = str + CompanionDatabase.mSlot_ExportNames[i - MyZeoExportDataContract.EXPORT_FIELD_SLOTS_FIRST_WITHIN_COLNAMES] + ",";
            } else {
                str = str + MyZeoExportDataContract.EXPORT_COLUMN_ORDER_NAMES[i] + ",";
            }
            i++;
        }

        // Extended Sleep Record columns
        i = 0;
        m = EXTENDED_SLEEP_RECORD_COLUMN_NAMES.length - 1;
        while (i < m) {
            str = str + EXTENDED_SLEEP_RECORD_COLUMN_NAMES[i] + ",";
            i++;
        }
        str = str + EXTENDED_SLEEP_RECORD_COLUMN_NAMES[i];

        // Sleep Cycle Results columns
        // TODO V1.1 Sleep Cycle Results columns

        // Amended Sleep Record columns
        if (mIncludeAmended) {
            for (String name: AMENDED_COLUMN_NAMES_SUFFIX) {
                if (mAmendedPlaceFirst) { str = str + ",Base "; }
                else { str = str + ",Amend "; }
                str = str + name;
            }
        }

        // Free-format Sleep Journal columns do not get headers
        return str;
    }

    // build one export data line for one integrated record during a myZeo formatted export
    public String buildOneLine(JournalDataCoordinator.IntegratedHistoryRec iRec, int shareWhat) {
        String str = "";
        boolean includeValueText = mIncludeValueText;
        boolean includeHypnogramSpaces = false;
        boolean asEpochs = false;
        boolean fakeTheRiseTime = false;
        boolean includeSeconds = false;
        boolean alterStartOfNight = false;
        if (shareWhat == SHARE_WHAT_CSV_EXCEL) {
            includeHypnogramSpaces = true;
            includeSeconds = true;
        } else if (shareWhat == SHARE_WHAT_CSV_ZEOVIEWER) {
            includeSeconds = true;
            alterStartOfNight = true;
        } else if (shareWhat == SHARE_WHAT_CSV_SLEEPYHEAD) {
            includeValueText = false;
            includeHypnogramSpaces = true;
            asEpochs = true;
            fakeTheRiseTime = true;
            alterStartOfNight = true;
        }

        // preparations; does the integrated record contain amended information?
        boolean isAmended = false;
        if (iRec.theCSErecord != null) {
            isAmended = ((iRec.theCSErecord.rAmendedFlags & CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_AMENDED_FLAGS_AMENDED) != 0);
        }

        // the Original myZeo export format section
        long dated = 0;
        if (iRec.theZAH_SleepRecord != null) { dated = iRec.theZAH_SleepRecord.rStartOfNight; }
        if (iRec.theCSErecord != null && (dated == 0.0 || iRec.theCSErecord.rStartOfRecord_Timestamp < dated)) { dated = iRec.theCSErecord.rStartOfRecord_Timestamp; }
        str = str + mSDF1.format(new Date(dated)) + ",";

        // sleep record subsection including start and end date/times
        if (iRec.theZAH_SleepRecord != null) {
            // Zeo App record is present; a matching CSE record may also be present
            if (isAmended && mAmendedPlaceFirst) { str = str + buildAmendedSleepRecord(iRec, asEpochs); }
            else { str = str + buildZeoSleepRecord(iRec, asEpochs); }

            if (alterStartOfNight && iRec.theZAH_SleepRecord.mHasExtended && iRec.theZAH_SleepRecord.rDisplayHypnogramStartTime > 0) {
                if (includeSeconds) { str = str + "," + mSDF2s.format(new Date(iRec.theZAH_SleepRecord.rDisplayHypnogramStartTime)) + ","; }
                else { str = str + "," + mSDF2.format(new Date(iRec.theZAH_SleepRecord.rDisplayHypnogramStartTime)) + ","; }
            } else if (iRec.theZAH_SleepRecord.rStartOfNight > 0) {
                if (includeSeconds) { str = str + "," + mSDF2s.format(new Date(iRec.theZAH_SleepRecord.rStartOfNight)) + ","; }
                else { str = str + "," + mSDF2.format(new Date(iRec.theZAH_SleepRecord.rStartOfNight)) + ","; }
            } else { str = str + ","; }

            if (iRec.theZAH_SleepRecord.rEndOfNight > 0) {
                if (includeSeconds) { str = str + mSDF2s.format(new Date(iRec.theZAH_SleepRecord.rEndOfNight)) + ","; }
                else { str = str + mSDF2.format(new Date(iRec.theZAH_SleepRecord.rEndOfNight)) + ","; }
            } else { str = str + ","; }

            if (fakeTheRiseTime && iRec.theZAH_SleepRecord.rEndOfNight > 0) {
                // this is the "Rise Time" field which SleepyHead software requires to be present
                if (includeSeconds) { str = str + mSDF2s.format(new Date(iRec.theZAH_SleepRecord.rEndOfNight)) + ","; }
                else { str = str + mSDF2.format(new Date(iRec.theZAH_SleepRecord.rEndOfNight)) + ","; }
            } else { str = str + ","; }
        } else if (iRec.theCSErecord != null) {
            // CSE record without a matching Zeo record; cannot contain amended information; only limited information is available
            long starting = iRec.theCSErecord.rEvent_TryingToSleep_Timestamp;
            if (starting == 0) { starting = iRec.theCSErecord.rEvent_GotIntoBed_Timestamp; }
            long ending = iRec.theCSErecord.rEvent_OutOfBedDoneSleeping_Timestamp;

            str = str + ",";    // no ZQ score

            if (starting > 0 && ending > 0) {
                long dur = (ending - starting) / 60000L;
                str = str + dur + ",";  // effective Total Z duration
            } else {
                str = str + ",";        // otherwise no Total Z duration known
            }

            str = str + ",,,,,,"; // no sleep stage totals

            if (starting > 0) {
                if (includeSeconds) { str = str + mSDF2s.format(new Date(starting)) + ","; }
                else { str = str + mSDF2.format(new Date(starting)) + ","; }
            }  else { str = str + ","; }

            if (ending > 0) {
                if (includeSeconds) { str = str + mSDF2s.format(new Date(ending)) + ","; }
                else { str = str + mSDF2.format(new Date(ending)) + ","; }
            } else { str = str + ","; }

            if (fakeTheRiseTime && ending > 0) {
                // this is the "Rise Time" field which SleepyHead software requires to be present
                if (includeSeconds) { str = str + mSDF2s.format(new Date(ending)) + ","; }
                else { str = str + mSDF2.format(new Date(ending)) + ","; }
            } else { str = str + ","; }

        } else { return str + "$ERROR$"; }  // this should not be possible

        // alarms subsection (not used except for the Rise Time which was previously handled)
        str = str + ",,,,,,,,,,"; // no alarms

        // attributes subsection
        if (iRec.theCSErecord != null) {
            if (!includeValueText && iRec.theCSErecord.rAttributes_Fixed_CSV_string != null) { iRec.theCSErecord.unpackInfoCSVstrings(); }
            if (iRec.theCSErecord.rAttributes_Fixed_CSV_string != null) {
                if (!iRec.theCSErecord.rAttributes_Fixed_CSV_string.isEmpty()) {
                    str = str + iRec.theCSErecord.rAttributes_Fixed_CSV_string;
                    str = str + ",";
                    //Log.d(_CTAG+".buildOneLine","Used mAttributes_Fixed_CSV_string");
                } else {
                    str = str + ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                }
            } else if (iRec.theCSErecord.mAttribs_Fixed_array != null) {
                //Log.d(_CTAG+".buildOneLine","Used mAttribs_Fixed_array");
                for (CompanionSleepEpisodeInfoParsedRec piRec: iRec.theCSErecord.mAttribs_Fixed_array) {
                    if (piRec != null) {
                        str = str + String.valueOf(piRec.rLikert);
                        if (includeValueText && piRec.rValue != null) { str = str  + ";" + piRec.rValue; }
                    }
                    str = str + ",";
                }
            } else {
                str = str + ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
            }
        } else {
            str = str + ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
        }

        // hypnograms sub-section
        if (iRec.theZAH_SleepRecord != null) {
            if (isAmended && mAmendedPlaceFirst) { str = str + buildAmendedHypnograms(iRec, includeHypnogramSpaces); }
            else { str = str + buildZeoHypnograms(iRec, includeHypnogramSpaces); }
            str = str + ",";
        } else {
            str = str + ",,";
        }

        // closing columns of the Original myZeo section
        str = str + "unknown,ZC-V"+ BuildConfig.VERSION_NAME;

        // Extended Sleep Record section
        int battLow = 0;
        int battHigh = 0;
        if (iRec.theCSErecord != null) {
            if (iRec.theCSErecord.rZeoHeadbandBattery_Low > 0) { battLow = iRec.theCSErecord.rZeoHeadbandBattery_Low; }
            if (iRec.theCSErecord.rZeoHeadbandBattery_High > 0) { battHigh = iRec.theCSErecord.rZeoHeadbandBattery_High; }
        }
        if (iRec.theZAH_SleepRecord != null) {
            if (iRec.theZAH_SleepRecord.rVoltageBattery > 0) {
                if (iRec.theZAH_SleepRecord.rVoltageBattery > 150) {
                    if (battHigh == 0) { battHigh = iRec.theZAH_SleepRecord.rVoltageBattery; }
                } else {
                    if (battLow == 0) { battLow = iRec.theZAH_SleepRecord.rVoltageBattery; }
                }
            }
        }
        if (isAmended && mAmendedPlaceFirst) { str = str + "," + mSDF2s.format(new Date(iRec.theCSErecord.rAmend_Display_Hypnogram_Starttime)) + ","; }
        else if (iRec.theZAH_SleepRecord != null) { str = str + "," + mSDF2s.format(new Date(iRec.theZAH_SleepRecord.rDisplayHypnogramStartTime)) + ","; }
        else { str = str + ",,"; }

        if (iRec.theZAH_SleepRecord != null) {
            str = str + mSDF2s.format(new Date(iRec.theZAH_SleepRecord.rStartOfNightOrig)) + ",";
        } else { str = str + ","; }

        str = str + battHigh + "," + battLow + ",";
        str = str + ",,";   // no impedance is available at this time

        if (isAmended && mAmendedPlaceFirst) { str = str + buildAmendedSleepExtended(iRec, asEpochs); }
        else if (iRec.theZAH_SleepRecord != null) { str = str + buildZeoSleepExtended(iRec, asEpochs); }
        else { str = str + ","; }

        // the Amended Sleep Record section
        if (mIncludeAmended) {
            if (isAmended) {
                if (mAmendedPlaceFirst) {
                    str = str + "," + buildZeoSleepRecord(iRec, asEpochs);
                    str = str + "," + buildZeoSleepExtended(iRec, asEpochs);
                    str = str + "," + buildZeoHypnograms(iRec, includeHypnogramSpaces);
                } else {
                    str = str + "," + buildAmendedSleepRecord(iRec, asEpochs);
                    str = str + "," + buildAmendedSleepExtended(iRec, asEpochs);
                    str = str + "," + buildAmendedHypnograms(iRec, includeHypnogramSpaces);
                }
            } else {
                str = str + ",,,,,,,,,,";
            }
        }

        // the Free-format Sleep Journal section
        if (iRec.theCSErecord != null) {
            // attributes subsection
            String workStr = "";
            if (iRec.theCSErecord.rAttributes_Vari_CSV_string != null) {
                if (!iRec.theCSErecord.rAttributes_Vari_CSV_string.isEmpty()) {
                    workStr = workStr + "$A," + iRec.theCSErecord.rAttributes_Vari_CSV_string;
                    if (!iRec.theCSErecord.rAttributes_Vari_CSV_string.substring(iRec.theCSErecord.rAttributes_Vari_CSV_string.length() - 1).equals(",")) { workStr = workStr + ","; }
                    workStr = workStr + "$/A,";
                }
            } else if (iRec.theCSErecord.mAttribs_Vari_array != null) {
                if (!iRec.theCSErecord.mAttribs_Vari_array.isEmpty()) {
                    workStr = workStr + "$A,";
                    for (CompanionSleepEpisodeInfoParsedRec piRec : iRec.theCSErecord.mAttribs_Vari_array) {
                        if (piRec.rAttributeExportName != null) {
                            workStr = workStr + String.valueOf(piRec.rSleepStage) + ";" + piRec.rAttributeExportName + ";" + String.valueOf(piRec.rLikert) + ";";
                            if (piRec.rValue != null) { workStr = workStr + piRec.rValue; }
                        }
                    }
                    workStr = workStr + "$/A,";
                }
            }

            // events subsection
            if (iRec.theCSErecord.mEvents_array == null) { iRec.theCSErecord.unpackEventCSVstring(); }
            if (iRec.theCSErecord.mEvents_array != null) {
                if (!iRec.theCSErecord.mEvents_array.isEmpty()) {
                    workStr = workStr + "$E,";
                    for (CompanionSleepEpisodeEventsParsedRec evtRec : iRec.theCSErecord.mEvents_array) {
                        workStr = workStr + evtRec.getExportString(mSDF2s) + ",";
                    }
                    workStr = workStr + "$/E,";
                }
            }

            if (!workStr.isEmpty()) {
                str = str + ",$ZeoCompJournalAddtl," + workStr + "$/ZeoCompJournalAddtl";
            }
        }
        return str;
    }

    // build a zeo-based sleep record subsection; note there is no final comma
    private String buildZeoSleepRecord(JournalDataCoordinator.IntegratedHistoryRec iRec, boolean asEpochs) {
        String str = iRec.theZAH_SleepRecord.rZQ_Score + ",";
        if (asEpochs) {
            str = str + String.format("%.0f", iRec.theZAH_SleepRecord.rTime_Total_Z_min * 2.0) + ","  + String.format("%.0f", iRec.theZAH_SleepRecord.rTime_to_Z_min * 2.0) + ",";
            str = str + String.format("%.0f", iRec.theZAH_SleepRecord.rTime_Awake_min * 2.0) + ","    + String.format("%.0f", iRec.theZAH_SleepRecord.rTime_REM_min * 2.0) + ",";
            str = str + String.format("%.0f", iRec.theZAH_SleepRecord.rTime_Light_min * 2.0) + ","    + String.format("%.0f", iRec.theZAH_SleepRecord.rTime_Deep_min * 2.0) + ",";
        } else {
            str = str + String.format("%.1f", iRec.theZAH_SleepRecord.rTime_Total_Z_min) + ","  + String.format("%.1f", iRec.theZAH_SleepRecord.rTime_to_Z_min) + ",";
            str = str + String.format("%.1f", iRec.theZAH_SleepRecord.rTime_Awake_min) + ","    + String.format("%.1f", iRec.theZAH_SleepRecord.rTime_REM_min) + ",";
            str = str + String.format("%.1f", iRec.theZAH_SleepRecord.rTime_Light_min) + ","    + String.format("%.1f", iRec.theZAH_SleepRecord.rTime_Deep_min) + ",";
        }
        str = str + iRec.theZAH_SleepRecord.rCountAwakenings;
        return str;
    }

    // build an amended-based sleep record subsection; note there is no final comma
    private String buildAmendedSleepRecord(JournalDataCoordinator.IntegratedHistoryRec iRec, boolean asEpochs) {
        String str = iRec.theCSErecord.rAmend_ZQ_Score + ",";
        if (asEpochs) {
            str = str + String.format("%.0f", iRec.theCSErecord.rAmend_Time_Total_Z_min * 2.0) + "," + String.format("%.0f", iRec.theCSErecord.rAmend_Time_to_Z_min * 2.0) + ",";
            str = str + String.format("%.0f", iRec.theCSErecord.rAmend_Time_Awake_min * 2.0) + ","    + String.format("%.0f", iRec.theCSErecord.rAmend_Time_REM_min * 2.0) + ",";
            str = str + String.format("%.0f", iRec.theCSErecord.rAmend_Time_Light_min * 2.0) + ","    + String.format("%.0f", iRec.theCSErecord.rAmend_Time_Deep_min * 2.0) + ",";

        } else {
            str = str + String.format("%.1f", iRec.theCSErecord.rAmend_Time_Total_Z_min) + "," + String.format("%.1f", iRec.theCSErecord.rAmend_Time_to_Z_min) + ",";
            str = str + String.format("%.1f", iRec.theCSErecord.rAmend_Time_Awake_min) + ","    + String.format("%.1f", iRec.theCSErecord.rAmend_Time_REM_min) + ",";
            str = str + String.format("%.1f", iRec.theCSErecord.rAmend_Time_Light_min) + ","    + String.format("%.1f", iRec.theCSErecord.rAmend_Time_Deep_min) + ",";
        }
        str = str + iRec.theCSErecord.rAmend_CountAwakenings;
        return str;
    }

    // build an amended-based sleep extended record subsection; note there is no final comma
    private String buildZeoSleepExtended(JournalDataCoordinator.IntegratedHistoryRec iRec, boolean asEpochs) {
        return iRec.theZAH_SleepRecord.rLightChangedToDeep_min + "," + iRec.theZAH_SleepRecord.rDeepSum;
    }

    // build an amended-based sleep extended record subsection; note there is no final comma
    private String buildAmendedSleepExtended(JournalDataCoordinator.IntegratedHistoryRec iRec, boolean asEpochs) {
        return iRec.theCSErecord.rAmend_LightChangedToDeep_min + "," + iRec.theCSErecord.rAmend_DeepSum;
    }

    // build a zeo-based hypnograms subsection; note there is no final comma
    private String buildZeoHypnograms (JournalDataCoordinator.IntegratedHistoryRec iRec, boolean includeHypnogramSpaces) {
        String str = "";
        for (int i = 0; i < iRec.theZAH_SleepRecord.rDisplay_Hypnogram.length; i++) {
            str = str + Byte.toString(iRec.theZAH_SleepRecord.rDisplay_Hypnogram[i]);
            if (includeHypnogramSpaces) { str = str + " "; }
        }

        int m = iRec.theZAH_SleepRecord.rBase_Hypnogram.length - 1;
        while (m >= 0 && iRec.theZAH_SleepRecord.rBase_Hypnogram[m] == ZeoDataContract.SleepRecord.SLEEP_STAGE_UNDEFINED) { m--; }
        m++;

        str = str + ",";
        for (int i = 0; i < m; i++) {
            str = str + Byte.toString(iRec.theZAH_SleepRecord.rBase_Hypnogram[i]);
            if (includeHypnogramSpaces) { str = str + " "; }
        }
        return str;
    }

    // build an amended-based hypnograms subsection; note there is no final comma
    private String buildAmendedHypnograms (JournalDataCoordinator.IntegratedHistoryRec iRec, boolean includeHypnogramSpaces) {
        String str = "";
        for (int i = 0; i < iRec.theCSErecord.rAmend_Display_Hypnogram.length; i++) {
            str = str + Byte.toString(iRec.theCSErecord.rAmend_Display_Hypnogram[i]);
            if (includeHypnogramSpaces) { str = str + " "; }
        }

        int m = iRec.theCSErecord.rAmend_Base_Hypnogram.length - 1;
        while (m >= 0 && iRec.theCSErecord.rAmend_Base_Hypnogram[m] == ZeoDataContract.SleepRecord.SLEEP_STAGE_UNDEFINED) { m--; }
        m++;

        str = str + ",";
        for (int i = 0; i < m; i++) {
            str = str + Byte.toString(iRec.theCSErecord.rAmend_Base_Hypnogram[i]);
            if (includeHypnogramSpaces) { str = str + " "; }
        }
        return str;
    }
}
