package opensource.zeocompanion.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import com.obscuredPreferences.ObscuredPrefs;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.views.HypnogramView;

// utility class that performs Image file creation, selection of data, and formatting of data
public class ImageExporter {
    // member variables
    private Context mContext = null;
    private String mImageDirectory = null;
    private String mName = null;
    private boolean mAmendedPlaceFirst = false;
    private long mSequenceNumber = 0;

    // member constants and other static content
    private static final String _CTAG = "IEU";

    SimpleDateFormat mSDF1 = new SimpleDateFormat("EEE, MMM d, yyyy");
    SimpleDateFormat mSDF3 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");   // this format is for file names

    public static final int SHARE_WHAT_IMAGE_STANDARD = 100;
    public static final int SHARE_WHAT_IMAGE_COUNT = 1;

    private static final int SHOW_WHICH_NOTHING = 0;
    private static final int SHOW_WHICH_ZEO = 1;
    private static final int SHOW_WHICH_CSE_AMENDED = 2;

    // return class
    public class ReturnResults {
        public File rTheExportFile = null;
        public String rAnErrorMessage = "";

        public ReturnResults (File theExportFile, String anErrorMessage) {
            rTheExportFile = theExportFile;
            rAnErrorMessage = anErrorMessage;
        }
    }

    // constructor; the Activity or ZeoApp context is needed for some methods
    public ImageExporter(Context context) { mContext = context; }

    // create the contents in the export; this method creates an export file for only one record that was selected by the end-user
    public ReturnResults createFileOneRec(JournalDataCoordinator.IntegratedHistoryRec iRec, int shareWhat) {

        // pre-determine what is available to be shown
        boolean isAmended = false;
        if (iRec.theCSErecord != null) {
            iRec.theCSErecord.unpackEventCSVstring();
            isAmended = ((iRec.theCSErecord.rAmendedFlags & CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_AMENDED_FLAGS_AMENDED) != 0);
        }
        int showWhat = showWhich(iRec, isAmended);
        if (showWhat == SHOW_WHICH_NOTHING || (showWhat == SHOW_WHICH_CSE_AMENDED && !isAmended)) { return new ReturnResults(null, "Nothing to export"); }

        // create the empty jpg file
        ReturnResults prepResults = prepAndCreateFile(shareWhat);
        if (prepResults.rTheExportFile == null || !prepResults.rAnErrorMessage.isEmpty()) { return prepResults; }

        // build the hypnogram bitmap
        HypnogramView theHypno = new HypnogramView(mContext);
        String title = "";
        if (mName != null) {
            if (!mName.isEmpty()) { title = title + mName + ": "; }
        }
        if (showWhat == SHOW_WHICH_CSE_AMENDED) { title = title + mSDF1.format(new Date(iRec.theCSErecord.rAmend_StartOfNight)); }
        else { title = title + mSDF1.format(new Date(iRec.theZAH_SleepRecord.rStartOfNight));  }
        theHypno.showAsSharedDetailed(title);

        theHypno.prepDrawToCanvas(1024, 512);
        Bitmap b1 = Bitmap.createBitmap(1024, 512, Bitmap.Config.ARGB_8888);
        Canvas c1 = new Canvas(b1);
        switch (showWhat) {
            case SHOW_WHICH_ZEO:
                // Zeo hypnogram
                long displayStart1 = iRec.theZAH_SleepRecord.rStartOfNight;
                if (iRec.theZAH_SleepRecord.mHasExtended && iRec.theZAH_SleepRecord.rDisplayHypnogramStartTime > 0) {
                    displayStart1 = iRec.theZAH_SleepRecord.rDisplayHypnogramStartTime;
                }
                if (iRec.theCSErecord != null) {
                    theHypno.setDataset(displayStart1, 300, 300, iRec.theZAH_SleepRecord.rDisplay_Hypnogram, false, iRec.theCSErecord.mEvents_array);
                } else {
                    theHypno.setDataset(displayStart1, 300, 300, iRec.theZAH_SleepRecord.rDisplay_Hypnogram, false, null);
                }
                break;
            case SHOW_WHICH_CSE_AMENDED:
                // CSE hypnogram
                long displayStart2 = iRec.theCSErecord.rAmend_StartOfNight;
                if (iRec.theCSErecord.rAmend_Display_Hypnogram_Starttime > 0) {
                    displayStart2 = iRec.theCSErecord.rAmend_Display_Hypnogram_Starttime;
                }
                theHypno.setDataset(displayStart2, 300, 300, iRec.theCSErecord.rAmend_Display_Hypnogram, false, iRec.theCSErecord.mEvents_array);
                break;
        }
        theHypno.doDraw(c1);        // draw once so it can determine all the text sizes
        Paint paint = new Paint();  // erase all the prior bitmap content
        paint.setColor(mContext.getResources().getColor(R.color.colorOffBlack2));
        c1.drawRect(0, 0, 1024, 512, paint);
        theHypno.doDraw(c1);        // draw again with all the proper sizes

        // create the remainder of the larger bitmap
        Bitmap b2 = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888);
        Canvas c2 = new Canvas(b2);

        paint.setColor(mContext.getResources().getColor(R.color.colorOffBlack2));
        c2.drawRect(0, 0, 1024, 1024, paint);

        c2.drawBitmap(b1, (float)0.0, (float)0.0, null);

        // get user's sleep goals (if any)
        double goalTotalSleepMin = 480.0;
        double goalDeepPct = 15.0;
        double goalREMpct = 20.0;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String wStr = ObscuredPrefs.decryptString(prefs.getString("profile_goal_hours_per_night", "8"));
        if (!wStr.isEmpty()) {
            double d = Double.parseDouble(wStr);
            if (d > 0.0) { goalTotalSleepMin = d * 60.0; }
        }
        wStr = ObscuredPrefs.decryptString(prefs.getString("profile_goal_percent_deep", "15"));
        if (!wStr.isEmpty()) {
            double d = Double.parseDouble(wStr);
            if (d > 0.0 && d <= 100.0) { goalDeepPct = d;  }
        }
        wStr  = ObscuredPrefs.decryptString(prefs.getString("profile_goal_percent_REM", "20"));
        if (!wStr.isEmpty()) {
            double d = Double.parseDouble(wStr);
            if (d > 0.0 && d <= 100.0) { goalREMpct = d;  }
        }

        float x = (float)25.0;
        float y = (float)600.0;
        int textSize = 36;
        float yBarOffset = -((float)textSize / (float)2.0) + (float)2.5;
        drawText(c2, paint, "Zeo Pro Headband", x, y, 48, Typeface.BOLD, Color.WHITE, Paint.Align.LEFT);
        switch (showWhat) {
            case SHOW_WHICH_ZEO:
                // Zeo results
                double goalREMmin1 = iRec.theZAH_SleepRecord.rTime_Total_Z_min * goalREMpct / 100.0;
                double goalDeepMin1 = iRec.theZAH_SleepRecord.rTime_Total_Z_min * goalDeepPct / 100.0;
                double goalLightMin1 = iRec.theZAH_SleepRecord.rTime_Total_Z_min * (100.0 - goalDeepPct - goalREMpct) / 100.0;
                float xZQ1 = (float)640.0;
                drawText(c2, paint, "ZQ", xZQ1, y, 48, Typeface.BOLD, Color.WHITE, Paint.Align.RIGHT);
                drawText(c2, paint, String.valueOf(iRec.theZAH_SleepRecord.rZQ_Score), xZQ1 + (float)4.0, y, 48, Typeface.BOLD, Color.GREEN, Paint.Align.LEFT);
                y += (48 * 1.5);
                drawText(c2, paint, "Total", x, y, textSize, Typeface.BOLD, Color.WHITE, Paint.Align.LEFT);
                drawText(c2, paint, Utilities.showTimeInterval(iRec.theZAH_SleepRecord.rTime_Total_Z_min, false), x + (float)100.0, y, textSize, Typeface.BOLD, Color.WHITE, Paint.Align.LEFT);
                drawBar(c2, paint, x + (float)250.0, y + yBarOffset, Color.WHITE, iRec.theZAH_SleepRecord.rTime_Total_Z_min, goalTotalSleepMin, goalTotalSleepMin * 2.0);
                y += (textSize * 1.1);
                drawText(c2, paint, "REM", x, y, 36, Typeface.BOLD, Color.rgb(0, 153, 0), Paint.Align.LEFT);
                drawText(c2, paint, Utilities.showTimeInterval(iRec.theZAH_SleepRecord.rTime_REM_min, false), x + (float)100.0, y, textSize, Typeface.BOLD, Color.rgb(0, 153, 0), Paint.Align.LEFT);
                drawBar(c2, paint, x + (float)250.0, y + yBarOffset, Color.rgb(0, 153, 0), iRec.theZAH_SleepRecord.rTime_REM_min, goalREMmin1, goalREMmin1 * 2.0);
                y += (textSize * 1.1);
                drawText(c2, paint, "Deep", x, y, 36, Typeface.BOLD, Color.rgb(102, 102, 255), Paint.Align.LEFT);
                drawText(c2, paint, Utilities.showTimeInterval(iRec.theZAH_SleepRecord.rTime_Deep_min, false), x + (float)100.0, y, textSize, Typeface.BOLD, Color.rgb(102, 102, 255), Paint.Align.LEFT);
                drawBar(c2, paint, x + (float)250.0, y + yBarOffset, Color.rgb(102, 102, 255), iRec.theZAH_SleepRecord.rTime_Deep_min, goalDeepMin1, goalDeepMin1 * 2.0);
                y += (textSize * 1.1);
                drawText(c2, paint, "Light", x, y, 36, Typeface.BOLD, Color.rgb(102, 178, 255), Paint.Align.LEFT);
                drawText(c2, paint, Utilities.showTimeInterval(iRec.theZAH_SleepRecord.rTime_Light_min, false), x + (float)100.0, y, textSize, Typeface.BOLD, Color.rgb(102, 178, 255), Paint.Align.LEFT);
                drawBar(c2, paint, x + (float)250.0, y + yBarOffset, Color.rgb(102, 178, 255), iRec.theZAH_SleepRecord.rTime_Light_min, -1.0, goalLightMin1 * 2.0);

                break;
            case SHOW_WHICH_CSE_AMENDED:
                // CSE amended results
                double goalREMmin2 = iRec.theCSErecord.rAmend_Time_Total_Z_min * goalREMpct / 100.0;
                double goalDeepMin2 = iRec.theCSErecord.rAmend_Time_Total_Z_min * goalDeepPct / 100.0;
                double goalLightMin2 = iRec.theCSErecord.rAmend_Time_Total_Z_min * (100.0 - goalDeepPct - goalREMpct) / 100.0;
                float xZQ2 = (float)640.0;
                drawText(c2, paint, "ZQ", xZQ2, y, 48, Typeface.BOLD, Color.WHITE, Paint.Align.RIGHT);
                drawText(c2, paint, String.valueOf(iRec.theCSErecord.rAmend_ZQ_Score), xZQ2 + (float)4.0, y, 48, Typeface.BOLD, Color.GREEN, Paint.Align.LEFT);
                y += (48 * 1.5);
                drawText(c2, paint, "Total", x, y, textSize, Typeface.BOLD, Color.WHITE, Paint.Align.LEFT);
                drawText(c2, paint, Utilities.showTimeInterval(iRec.theCSErecord.rAmend_Time_Total_Z_min, false), x + (float)100.0, y, textSize, Typeface.BOLD, Color.WHITE, Paint.Align.LEFT);
                drawBar(c2, paint, x + (float)250.0, y + yBarOffset, Color.WHITE, iRec.theCSErecord.rAmend_Time_Total_Z_min, goalTotalSleepMin, goalTotalSleepMin * 2.0);
                y += (textSize * 1.1);
                drawText(c2, paint, "REM", x, y, 36, Typeface.BOLD, Color.rgb(0, 153, 0), Paint.Align.LEFT);
                drawText(c2, paint, Utilities.showTimeInterval(iRec.theCSErecord.rAmend_Time_REM_min, false), x + (float)100.0, y, textSize, Typeface.BOLD, Color.rgb(0, 153, 0), Paint.Align.LEFT);
                drawBar(c2, paint, x + (float)250.0, y + yBarOffset, Color.rgb(0, 153, 0), iRec.theCSErecord.rAmend_Time_REM_min, goalREMmin2, goalREMmin2 * 2.0);
                y += (textSize * 1.1);
                drawText(c2, paint, "Deep", x, y, 36, Typeface.BOLD, Color.rgb(102, 102, 255), Paint.Align.LEFT);
                drawText(c2, paint, Utilities.showTimeInterval(iRec.theCSErecord.rAmend_Time_Deep_min, false), x + (float)100.0, y, textSize, Typeface.BOLD, Color.rgb(102, 102, 255), Paint.Align.LEFT);
                drawBar(c2, paint, x + (float)250.0, y + yBarOffset, Color.rgb(102, 102, 255), iRec.theCSErecord.rAmend_Time_Deep_min, goalDeepMin2, goalDeepMin2 * 2.0);
                y += (textSize * 1.1);
                drawText(c2, paint, "Light", x, y, 36, Typeface.BOLD, Color.rgb(102, 178, 255), Paint.Align.LEFT);
                drawText(c2, paint, Utilities.showTimeInterval(iRec.theCSErecord.rAmend_Time_Light_min, false), x + (float)100.0, y, textSize, Typeface.BOLD, Color.rgb(102, 178, 255), Paint.Align.LEFT);
                drawBar(c2, paint, x + (float)250.0, y + yBarOffset, Color.rgb(102, 178, 255), iRec.theCSErecord.rAmend_Time_Light_min, -1.0, goalLightMin2 * 2.0);
                break;
        }

        // convert the bitmap to PNG compression/file-tyoe
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        b2.compress(Bitmap.CompressFormat.PNG, 100, bytes);

        String errString = "";
        try {
            FileOutputStream fo = new FileOutputStream(prepResults.rTheExportFile);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            errString = "File IO error: PNG export file ("+prepResults.rTheExportFile.getAbsoluteFile()+"): " + e.getMessage();
            Log.e(_CTAG+".createFileOneRec", "File IO error: PNG export file ("+prepResults.rTheExportFile.getAbsoluteFile()+"): " + e.toString());
        }

        // because this IntegratedHistoryRec is ultimately held by the MainHistoryFragment in its larger list, do NOT destroy it
        return new ReturnResults(prepResults.rTheExportFile, errString);
    }

    private void drawText(Canvas c, Paint p, String text, float x, float y, int textSize, int typeface, int textColor, Paint.Align textAlign) {
        p.setColor(textColor);
        p.setTextSize(textSize);
        p.setTextAlign(textAlign);
        p.setTypeface(Typeface.create(Typeface.DEFAULT, typeface));
        c.drawText(text, x, y, p);
    }

    private void drawBar(Canvas c, Paint p, float x, float y, int barColor, double actualValue, double goalValue, double rangeValue) {
        float xMax = (float)1014.0;

        p.setColor(mContext.getResources().getColor(R.color.colorOffBlack3));
        p.setStrokeWidth(10);
        c.drawLine(x, y, xMax, y, p);

        double pct1 = actualValue / rangeValue;
        float len1 = (float)((xMax - x) * pct1);
        p.setColor(barColor);
        c.drawLine(x, y, x + len1, y, p);

        if (goalValue > 0.0) {
            double pct2 = goalValue / rangeValue;
            float len2 = (float)((xMax - x) * pct2);
            p.setColor(Color.YELLOW);
            c.drawLine(x + len2, y - 10, x + len2, y + 10, p);
        }
    }

    // determines what to show (1=Zeo, 2=CSE, or 0=neither)
    private int showWhich(JournalDataCoordinator.IntegratedHistoryRec iRec, boolean isAmended) {
        if (mAmendedPlaceFirst && isAmended) {
            // should show the CSE record
            if (iRec.theCSErecord != null) { return SHOW_WHICH_CSE_AMENDED; }
            if (iRec.theZAH_SleepRecord != null) { return SHOW_WHICH_ZEO; }
        } else {
            // should show the Zeo record
            if (iRec.theZAH_SleepRecord != null) { return SHOW_WHICH_ZEO; }
            if (iRec.theCSErecord != null && isAmended) { return SHOW_WHICH_CSE_AMENDED; }
        }
        return SHOW_WHICH_NOTHING;
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
        mAmendedPlaceFirst = sPrefs.getBoolean("export_amended_placeFirst", false);
        mName = sPrefs.getString("profile_name", "");
        mImageDirectory = sPrefs.getString("export_directory_image", "Android/data/opensource.zeocompanion/exports");

        // create the directory path to our exports subdirectory in external storage
        File exportsDir = null;
        if (mImageDirectory != null) {
            exportsDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + mImageDirectory);
        }
        else {
            exportsDir = new File(ZeoCompanionApplication.mBaseExtStorageDir + File.separator + "exports");
        }
        exportsDir.mkdirs();

        // compose the export file name
        String str = "ZeoCompanion_Image_";
        if (mName != null) {
            if (!mName.isEmpty()) { str = str + mName + "_"; }
        }
        switch (shareWhat) {
            case SHARE_WHAT_IMAGE_STANDARD:
                str = str + "Standard";
                break;
        }
        str = str + "_" + mSDF3.format(new Date()) + "_" + mSequenceNumber + ".png";
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
}
