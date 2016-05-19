package opensource.zeocompanion;

/*
All source code of this ZeoCompanion Android Application, except where otherwise encumbered as stated below,
is dedicated to the Public Domain.  All source code of this ZeoCompanion Android Application,
except where otherwise encumbered as stated below, constitutes prior art as of March 2016.

Source code that has known encumberances and therefore CANNOT be dedicated into the Public Domain include:
- GraphView:
    Located within this App: com.jjoe64.graphview
    License: GPLv2 including "GPL linking exception"
    Website: http://www.android-graphview.org/get-support--license.html
    Modifications: changes to provide limited enhanced features of specific use to this app
- ZeoDataContract:
    Located within the App: com.myzeo.android.api.data
    License: Apache License, Version 2.0
    Website: https://github.com/zeoeng/zeo-android-api
    Modifications: none
- MyZeoExportDataContract:
    Located within the App: com.myzeo.android.api.data
    License: unknown
    Website: offline; copy located at: https://www.gwern.net/docs/zeo/2013-zeo-exportdatasheet.pdf
    Modifications: none
- ObscuredPrefs:
    Located within the App: com.obscuredPreferences
    License: multiple sources; unknown which has first provenance; also unknown if the specific encrypt() and decrypt() methods have further earlier provenance
    Probable 1st provenance Website: https://stackoverflow.com/questions/785973/what-is-the-most-appropriate-way-to-store-user-settings-in-android-application/6393502#6393502
    Probable later provenance Website: http://www.codeproject.com/Articles/785925/Obscured-Shared-Preferences-for-Android
    Modifications: none; however only two methods [encrypt() and decrypt()] are utilized
- Modified widgets: EditTextPreference and Spinner
    Located within the App: com.android
    License: Apache License, Version 2.0
    Website: https://source.android.com/source/licenses.html
    Modifications: changes to provide limited enhanced features of specific use to this app
- Modified widgets: TimePreference
    Located within the App: com.github
    License: no stated license
    Website: https://gist.github.com/SamWhited/a2c3c382dcaa3ae17bb4
    Modifications: changes to summary string and default style handling

Other source code within this application could have at present unknown encumberances, and if so
revealed at some future point are therefore not within the scope of this Public Domain dedication.
 */

import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import com.obscuredPreferences.ObscuredPrefs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import opensource.zeocompanion.database.CompanionAlertRec;
import opensource.zeocompanion.database.CompanionDatabase;
import opensource.zeocompanion.database.CompanionSystemRec;
import opensource.zeocompanion.utility.DirectEmailerOutbox;
import opensource.zeocompanion.utility.JournalDataCoordinator;
import opensource.zeocompanion.zeo.ZeoAppHandler;

// main application "global" class
public class ZeoCompanionApplication extends Application {
    // member variables
    public static boolean mFirstTIme = false;
    public static int mFirstTimeHintsShown = 0;
    public static int mMaxBitmapDim = 0;
    public static float mScreenDensity = 0;
    public static boolean mAlreadyWarnedAboutHeadband = false;

    // member constants and other static content
    private static final String _CTAG = "APP";
    private static final SimpleDateFormat mFileDateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    public static ZeoAppHandler mZeoAppHandler = null;
    public static CompanionDatabase mDatabaseHandler = null;
    public static JournalDataCoordinator mCoordinator = null;
    public static DirectEmailerOutbox mEmailOutbox = null;
    private static Context mOurContext = null;
    public static File mBaseExtStorageDir = null;

    // these two are a "cheat" to allow passing of an IntegratedHistoryRec
    // from the MainActivity to either the HistoryDetailActivity or the SharingActivity
    public static JournalDataCoordinator.IntegratedHistoryRec mIrec_HDAonly = null;
    public static JournalDataCoordinator.IntegratedHistoryRec mIrec_SAonly = null;

    // constants for showing one-time hints
    public static final int APP_HINTS_ATTRIBUTES_FRAGMENT_BEFORE = 0x0001;
    public static final int APP_HINTS_ATTRIBUTES_FRAGMENT_AFTER = 0x0002;
    public static final int APP_HINTS_INBED_FRAGMENT = 0x0004;
    public static final int APP_HINTS_GOING_FRAGMENT = 0x0008;
    public static final int APP_HINTS_DURING_FRAGMENT = 0x0010;
    public static final int APP_HINTS_SUMMARY_FRAGMENT = 0x0020;
    public static final int APP_HINTS_HISTORY_FRAGMENT = 0x0040;
    public static final int APP_HINTS_BACKUP = 0x0080;

    // inter-process messaging constants used by various Activities and Handlers
    public static final int MESSAGE_HEADBAND_HBFRAG_LOW = 9000;
    public static final int MESSAGE_HEADBAND_RECV_HB_MSG = MESSAGE_HEADBAND_HBFRAG_LOW;
    public static final int MESSAGE_HEADBAND_BLUETOOTH_HNDLR_ERR = 9001;
    public static final int MESSAGE_HEADBAND_HB_CONNECT_OK = 9002;
    public static final int MESSAGE_HEADBAND_HBFRAG_HIGH = MESSAGE_HEADBAND_HB_CONNECT_OK;
    public static final int MESSAGE_APP_SEND_TOAST = 9100;
    public static final int MESSAGE_ZAH_ZEO_STATE_CHANGED = 9110;
    public static final int MESSAGE_ZAH_ZEO_PROBED_NO_CHANGE = 9111;
    public static final int MESSAGE_MAIN_UPDATE_ALL = 9120;
    public static final int MESSAGE_MAIN_UPDATE_JSB = 9121;
    public static final int MESSAGE_MAIN_ZAH_STATE_CHANGE = 9122;
    public static final int MESSAGE_MAIN_ZAH_PROBE_NO_CHANGE = 9123;
    public static final int MESSAGE_MAIN_UPDATE_HISTORY = 9124;
    public static final int MESSAGE_MAIN_UPDATE_MENU = 9125;
    public static final int MESSAGE_SHARING_DIALOG_TERMINATED = 9130;
    public static final int MESSAGE_SETTINGS_EMAILTEST_RESULTS = 9140;
    public static final int MESSAGE_OUTBOX_EMAILRESEND_RESULTS = 9150;

    // application-custom broadcast message actions
    public static final String ACTION_ALARMMGR_WAKEUP_RTC = "opensource.zeocompanion.intent.action.RTC_WAKEUP"; // this must match what is defined in the Manifest

    // receive inter-process messages to ensure that no inter-nested UI updates cause issues
    // generally this is called by the Direct Email subsystem to display Toasts
    public static Handler mAppHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ZeoCompanionApplication.MESSAGE_APP_SEND_TOAST:
                    Toast.makeText(getContext(), (String)msg.obj, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    // setup a Listener for changes in the shared preferences
    SharedPreferences.OnSharedPreferenceChangeListener mPrefsChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("email_auto_enable") || key.equals("email_auto_send_time") || key.equals("database_replicate_zeo")) {
                // preferences that affect the use of the AlarmManager have changed
                Log.d(_CTAG+".prefChgListen","Configure Alarm Manager needed");
                configAlarmManagerToPrefs();
            } else if (key.equals("profile_name")) {
                // profile name has changed; need to change it in the DB too
                CompanionSystemRec sRec = mDatabaseHandler.getSystemRec();
                if (sRec != null) {
                    String newName = sharedPreferences.getString(key, "");
                    if (newName.isEmpty()) { sRec.rUserName = null; }
                    else { sRec.rUserName = newName; }
                    sRec.saveToDB(mDatabaseHandler);
                }
            }
        }
    };

    // receiver for timeouts of recurring daily Alarm for automatic emailing;
    // these run in the main thread
    public static class AlarmReceiver extends BroadcastReceiver {
        // constructor
        public AlarmReceiver() { super(); }

        // receive a filtered message (the filtered actions are in the Manifest)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) { return; }
            Log.d("APP.AR.onReceive","Action="+action);
            if (action.equals(ACTION_ALARMMGR_WAKEUP_RTC)) {
                // Alarm Manager has given the daily wakeup
                if (mEmailOutbox != null) { mEmailOutbox.dailyCheck(); }
                if (mZeoAppHandler != null) { mZeoAppHandler.dailyCheck(); }
            }
        }
    }

    // receive system-wide broadcasts about changes in Zeo Headband state
    private final BroadcastReceiver mZeoAppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) { return; }
            Log.d("APP.ZAR.onReceive","Action="+action);
            mZeoAppHandler.zeoAppBroadcastReceived(intent);
        }
    };

    // Thread Context: can be called from utility threads, so cannot perform UI actions like Toast
    // setup a master application-wide abort handler usable by all threads
    public static Thread.UncaughtExceptionHandler mMasterAbortHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Log.e(_CTAG + ".mstrAbortHdlr", "=====!!!!!=====Unhandled Abort Captured=====!!!!!=====");
            postToErrorLog(null, e, "*UNHANDLED*", t.getName());   // automatically posts a Log.e
            System.exit(0); // force the entire App to terminate else it goes into "ANR" limbo
        }
    };

    // called upon App invocation; however remember that Apps are usually just suspended in the background;
    // so this callback cannot be expected to be invoked every time the end-user brings up the App;
    // also note that Android does NOT provide any application onTerminate or onDestroy callbacks so
    // there is no concept of a "clean termination" that saves state at the Application object level
    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d(_CTAG + ".onCreate", "=====ON-CREATE=====");
        // activate the global unhandled exception handler in this main thread
        Thread.setDefaultUncaughtExceptionHandler(mMasterAbortHandler);

        // initializations
        mOurContext = this;
        ObscuredPrefs.init(this);

        // pre-create empty external storage folders
        mBaseExtStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + getPackageName());
        createExternalStorageFolders(); // note if there are external storage problems, the Application Object does not have a UI Activity in which to report it; MainActivity will re-detect this

        // startup all the global application handlers; the order of these startups is important
        mDatabaseHandler = new CompanionDatabase(this);     // database handler must be first and must be initialized
        String msg = mDatabaseHandler.initialize();
        //if (!msg.isEmpty()) { Utilities.showAlertDialog(this, "Error", msg, "Okay"); }      TODO V1.1 error reporting but only Toast is available at this stage
        mZeoAppHandler = new ZeoAppHandler(this);
        mCoordinator = new JournalDataCoordinator(this);    // ZeoAppHandler must be instantiated first
        mEmailOutbox = new DirectEmailerOutbox(this);       // JournalDataCoordinator must be instantiated first

        // detect whether the App is being run the first time after an install (or a data clear from the App Manager)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mFirstTIme = prefs.getBoolean("app_firstTime", true);
        mFirstTimeHintsShown = prefs.getInt("app_firstTime_HintsShown", 0);
        if (mFirstTIme) {
            // these obscured preference default values need to be pre-encrypted
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("profile_goal_hours_per_night", ObscuredPrefs.encryptString("8"));
            editor.putString("profile_goal_percent_deep", ObscuredPrefs.encryptString("15"));
            editor.putString("profile_goal_percent_REM", ObscuredPrefs.encryptString("20"));
            editor.putBoolean("app_firstTime", false);
            editor.commit();
        }

        // setup to receive preference changes that affect the AlarmManager; then configure the AlarmManager
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mPrefsChangeListener);
        configAlarmManagerToPrefs();

        // listen to broadcast messages from the Zeo Android App
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.myzeo.android.headband.action.HEADBAND_UNDOCKED");
        filter.addAction("com.myzeo.android.headband.action.HEADBAND_DOCKED");
        filter.addAction("com.myzeo.android.headband.action.HEADBAND_BATTERY_DEAD");
        filter.addAction("com.myzeo.android.headband.action.HEADBAND_BUTTON_PRESS");
        filter.addAction("com.myzeo.android.headband.action.HEADBAND_DISCONNECTED");
        filter.addAction("com.myzeo.android.headband.action.HEADBAND_CONNECTED");
        registerReceiver(mZeoAppReceiver, filter);
    }

    // return the App's context
    public static Context getContext() {
        return mOurContext;
    }

    // close the database; this is only performed for a reload database
    private static boolean closeDatabase() {
        boolean done = mDatabaseHandler.closeDatabase();
        if (done) { mDatabaseHandler = null; }
        return done;
    }

    // reopen the database; this is only performed for a reload database
    private static String reopenDatabase() {
        mDatabaseHandler = new CompanionDatabase(mOurContext);
        return mDatabaseHandler.initialize();
    }

    // used by various Fragments to indicate that their one-time hint has been shown
    public static void hintShown(int shown) {
        mFirstTimeHintsShown = mFirstTimeHintsShown | shown;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mOurContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("app_firstTime_HintsShown", mFirstTimeHintsShown);
        editor.commit();
    }

    // creates the App's external storage folders
    private void createExternalStorageFolders() {
        // is external storage available, read/write, and App has been granted permission
        int r = checkExternalStorage();
        if (r != 0) { return; }

        // external storage is available and read/write
        mBaseExtStorageDir.mkdirs();
        File myExtFilesInternalsDir = new File(mBaseExtStorageDir + File.separator + "internals");
        myExtFilesInternalsDir.mkdirs();
        File myExtFilesOutboxDir = new File(mBaseExtStorageDir + File.separator + "outbox");
        myExtFilesOutboxDir.mkdirs();
        File myExtFilesExportsDir = new File(mBaseExtStorageDir + File.separator + "exports");
        myExtFilesExportsDir.mkdirs();
    }

    // properly configure the Android AlarmManager depending on the preferences of the end-user;
    // used by both the DirectEmailerOutbox and the ZeoAppHandler
    private void configAlarmManagerToPrefs() {
        // setup a daily alarm if auto-emailing is enabled
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabledAutoEmail = prefs.getBoolean("email_auto_enable", false);
        boolean enabledDatabaseReplicate = prefs.getBoolean("database_replicate_zeo", false);
        long desiredTOD = prefs.getLong("email_auto_send_time", 0); // will default to midnight
        long configuredTOD = prefs.getLong("email_auto_send_time_configured", 0);   // will default to midnight

        // determine whether there is an active AlarmManager entry that we have established
        AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intentCheck = new Intent(this, ZeoCompanionApplication.AlarmReceiver.class);
        intentCheck.setAction(ZeoCompanionApplication.ACTION_ALARMMGR_WAKEUP_RTC);
        PendingIntent existingPi = PendingIntent.getBroadcast(this, 0, intentCheck, PendingIntent.FLAG_NO_CREATE);

        if (enabledAutoEmail || enabledDatabaseReplicate) {
            // Daily AlarmManager is needed
            if (existingPi != null && desiredTOD != configuredTOD) {
                // there is an existing AlarmManager entry, but it has the incorrect starting time-of-day;
                // so cancel it, and rebuild a new one
                Intent intent1 = new Intent(this, ZeoCompanionApplication.AlarmReceiver.class);
                intent1.setAction(ZeoCompanionApplication.ACTION_ALARMMGR_WAKEUP_RTC);
                PendingIntent pi1 = PendingIntent.getBroadcast(this, 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
                am.cancel(pi1);
                pi1.cancel();
                existingPi = null;
            }
            if (existingPi == null) {
                // there is no existing AlarmManager entry, so create it
                Date dt = new Date(desiredTOD);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, dt.getHours());
                calendar.set(Calendar.MINUTE, dt.getMinutes());
                calendar.set(Calendar.SECOND, dt.getSeconds());
                Intent intent2   = new Intent(this, ZeoCompanionApplication.AlarmReceiver.class);
                intent2.setAction(ZeoCompanionApplication.ACTION_ALARMMGR_WAKEUP_RTC);
                PendingIntent pi2 = PendingIntent.getBroadcast(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi2);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("email_auto_send_time_configured", desiredTOD);
                editor.commit();
            }

        } else {
            // Daily AlarmManager is not needed
            if (existingPi != null) {
                // there is an AlarmManager entry pending; need to cancel it
                Intent intent3 = new Intent(this, ZeoCompanionApplication.AlarmReceiver.class);
                intent3.setAction(ZeoCompanionApplication.ACTION_ALARMMGR_WAKEUP_RTC);
                PendingIntent pi3 = PendingIntent.getBroadcast(this, 0, intent3, PendingIntent.FLAG_CANCEL_CURRENT);
                am.cancel(pi3);
                pi3.cancel();
            }
        }
    }

    // copies the ZeoCompanion database to external storage
    public String saveCopyOfDB(String includePrefix) {
        // is external storage available, read/write, and App has been granted permission
        int r = checkExternalStorage();
        if (r == -2) { return "Permission for App to write to external storage has not been granted; please grant the permission"; }
        else if (r != 0) { return "No writable external storage is available"; }

        // get the name and folder preference
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sPrefs.getString("profile_name", "");
        String folder = sPrefs.getString("backup_directory", "Android/data/opensource.zeocompanion/internals");

        // ensure the destination directory structure is present
        File backupsDir = null;
        if (folder != null) {
            backupsDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + folder);
        }
        else {
            backupsDir = new File(mBaseExtStorageDir + File.separator + "internals");
        }
        backupsDir.mkdirs();

        // compose source and destination File instances
        File source = getDatabasePath(CompanionDatabase.DATABASE_NAME);
        String newName = FilenameUtils.removeExtension(CompanionDatabase.DATABASE_NAME);
        if (name != null) {
            if (!name.isEmpty()) { newName = newName + "_" + name ; }
        }
        newName = newName + "_DBVer" + mDatabaseHandler.mVersion +
                "_AppVer" + BuildConfig.VERSION_NAME + "_" +
                mFileDateFormatter.format(new Date()) + ".db";
        File dest = new File(backupsDir.getPath() + File.separator + includePrefix + newName);
        Log.d(_CTAG+".saveCopyOfDB","Dest="+dest.getAbsolutePath());

        // perform the copy
        try {
            FileUtils.copyFile(source, dest);
            ZeoCompanionApplication.forceShowOnPC(dest);
        } catch (Exception e) {
            return "Failed to backup database because of filesystem error: " + e.getMessage();
        }
        return "";
    }

    // determines whether a restorage of the ZeoCompanion database from external storage is possible
    public CompanionDatabase.ValidateDatabaseResults restoreCopyOfDB_prep(String theSourceFile) {
        // is external storage available, read/write, and App has been granted permission
        int r = checkExternalStorage();
        if (r == -2) { return new CompanionDatabase.ValidateDatabaseResults("Permission for App to write to external storage has not been granted; please grant the permission"); }
        else if (r != 0) { return new CompanionDatabase.ValidateDatabaseResults("No writable external storage is available"); }

        // get the folder preference
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String folder = sPrefs.getString("backup_directory", "Android/data/opensource.zeocompanion/internals");

        // compose source File instances and ensure it actually exists on-disk
        File backupsDir = null;
        if (folder != null) {
            backupsDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + folder);
        }
        else {
            backupsDir = new File(mBaseExtStorageDir + File.separator + "internals");
        }
        backupsDir.mkdirs();

        File source = new File(backupsDir + File.separator + theSourceFile);
        if (!source.exists()) { return new CompanionDatabase.ValidateDatabaseResults("File does not exist or the folder path does not exist: " + source.getAbsolutePath()); }

        // perform validation of the database itself
        return ZeoCompanionApplication.mDatabaseHandler.validateDatabaseFromFile(source);
    }

    // restores the ZeoCompanion database from external storage
    public String restoreCopyOfDB(String theSourceFile) {
        // is external storage available, read/write, and App has been granted permission
        int r = checkExternalStorage();
        if (r == -2) { return "Permission for App to write to external storage has not been granted; please grant the permission"; }
        else if (r != 0) { return "No writable external storage is available"; }

        // get the folder preference
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String folder = sPrefs.getString("backup_directory", "Android/data/opensource.zeocompanion/internals");

        // compose source File instances and ensure it actually exists on-disk
        File backupsDir = null;
        if (folder != null) {
            backupsDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + folder);
        }
        else {
            backupsDir = new File(mBaseExtStorageDir + File.separator + "internals");
        }
        backupsDir.mkdirs();
        File source = new File(backupsDir + File.separator + theSourceFile);
        File dest = getDatabasePath(CompanionDatabase.DATABASE_NAME);

        // perform the copy
        boolean okay = closeDatabase();
        if (!okay) { return "Could not close the current active database; see error.log"; }
        try {
            FileUtils.copyFile(source, dest);
        } catch (Exception e) {
            String msg = reopenDatabase();  // need to reopen the database first to regain access to SystemAlerts; if there is a problem postToErrorLog will have already been called for the database issues
            String eMsg = "Failed to restore database because of filesystem error: " + e.getMessage();
            postToErrorLog(_CTAG + ".restoreCopyOfDB", e, "Failed to restore database from " + source.getAbsoluteFile() + " to " + dest.getAbsoluteFile());     // automatically posts a Log.e
            if (!msg.isEmpty()) { return msg + "AND " + eMsg; }
            return eMsg;
        }
        Log.i(_CTAG + ".restoreCopyOfDB", "Restored database from " + source.getAbsoluteFile() + " to " + dest.getAbsoluteFile());
        String msg = reopenDatabase();
        if (!msg.isEmpty()) { return msg; }
        return "";
    }

    // force Android to let an attached PC know the file has been created
    public static void forceShowOnPC(File theFile) {
        MediaScannerConnection.scanFile(mOurContext, new String[]{theFile.getPath()}, null, null);
    }

    // determine if the external storage is accesible
    public static int checkExternalStorage() {
        // does App still have permission to write to external storage?
        int permissionCheck = ContextCompat.checkSelfPermission(mOurContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) { return -2; }  // nope

        // is external storage is available and read-write?
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) { return -1; }     // not mounted or is MEDIA_MOUNTED_READ_ONLY or is in some other non-usable condition
        return 0;
    }

    // Thread Context: can be called from utility threads, so cannot perform UI actions like Toast
    // write detailed exception information into an error log that the end-user can email to the developer
    public static void postToErrorLog(String method, Throwable theException) {
        postToErrorLog(method, theException, null, null, false);
    }
    public static void postToErrorLog(String method, Throwable theException, String extra) {
        postToErrorLog(method, theException, extra, null, false);
    }
    public static void postToErrorLog(String method, Throwable theException, String extra, String threadName) {
        postToErrorLog(method, theException, extra, threadName, false);
    }
    public static void postToErrorLog(String method, Throwable theException, String extra, String threadName, boolean noAlert) {
        String eMsg = "Exception";
        if (method != null) {
            if (!method.isEmpty()) { eMsg = eMsg + " in " + method; }
        }
        if (threadName != null) {
            if (!threadName.isEmpty()) { eMsg = eMsg + " in Thread " + threadName; }
        }
        if (extra != null) {
            if (!extra.isEmpty()) {  eMsg = eMsg + " (" + extra + ")"; }
        }
        eMsg = eMsg + ": " + theException.toString();
        Log.e(_CTAG + ".postToErrorLog", eMsg);
        theException.printStackTrace();

        int r = checkExternalStorage();
        if (r != 0) {
            Log.e(_CTAG+".postToErrorLog", "Cannot write to external storage code " + r);
            return;
        }

        FileWriter wrt = null;
        try {
            // pull the stack trace
            StackTraceElement[] traces = theException.getStackTrace();

            // ensure the directory structure is present and compose the file name
            File internalsDir = new File(mBaseExtStorageDir + File.separator + "internals");
            internalsDir.mkdirs();
            File errLogFile = new File(internalsDir + File.separator + "error.log");

            // create and append to the file
            wrt = new FileWriter(errLogFile, true);  // append if file already exists
            wrt.write(new Date().toString() + "\n");
            if (threadName != null) {
                if (!threadName.isEmpty()) { wrt.write(" in Thread "+threadName + " "); }
            }
            wrt.write("AppVerName " + BuildConfig.VERSION_NAME + " AppVerCode " + BuildConfig.VERSION_CODE);
            if (mDatabaseHandler != null) { wrt.write(" with DBver " +mDatabaseHandler.mVersion); }
            wrt.write("\n");
            wrt.write("Android Version " + android.os.Build.VERSION.RELEASE + " API " + android.os.Build.VERSION.SDK_INT + "\n");
            wrt.write("Platform Manf " + Build.MANUFACTURER + " Model " + Build.MODEL + "\n");
            if (method != null) {
                if (!method.isEmpty()) {  wrt.write(method + "\n"); }
            }
            if (extra != null) {
                if (!extra.isEmpty()) {  wrt.write(extra + "\n"); }
            }
            wrt.write(theException.toString() + "\n");
            for (StackTraceElement st: traces) { wrt.write(st.toString() + "\n"); }
            wrt.write("=====\n");
            wrt.write("=====\n");
            wrt.flush();
            wrt.close();

            // force it to be shown and post an alert
            forceShowOnPC(errLogFile);
            if (!noAlert) { postAlert("An abort occured; details are in \'internals/error.log\'; contact the Developer"); } // noAlert is only needed when an Alert itself was being posted to the database and it failed to post

            // must send the toast indirectly in case this is being called from a utility thread
            Message msg = new Message();
            msg.what = ZeoCompanionApplication.MESSAGE_APP_SEND_TOAST;
            msg.obj = "Abort successfully logged to \'internals/error.log\'";
            mAppHandler.sendMessage(msg);
        } catch (Exception e) {
            if (wrt != null) { try { wrt.close(); } catch (Exception ignored) { } }
            Log.e(_CTAG + ".postToErrLog", "Cannot write to error.log: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void postToErrorLog(String method, String errorMessage, String extra) {
        postToErrorLog(method, errorMessage, extra, false);
    }
    public static void postToErrorLog(String method, String errorMessage, String extra, boolean noAlert) {
        String eMsg = "Severe Error";
        if (method != null) {
            if (!method.isEmpty()) { eMsg = eMsg + " in " + method; }
        }
        if (extra != null) {
            if (!extra.isEmpty()) {  eMsg = eMsg + " (" + extra + ")"; }
        }
        eMsg = eMsg + ": " + errorMessage;
        Log.e(_CTAG + ".postToErrorLog", eMsg);

        int r = checkExternalStorage();
        if (r != 0) {
            Log.e(_CTAG+".postToErrorLog", "Cannot write to external storage code " + r);
            return;
        }

        FileWriter wrt = null;
        try {
            // ensure the directory structure is present and compose the file name
            File internalsDir = new File(mBaseExtStorageDir + File.separator + "internals");
            internalsDir.mkdirs();
            File errLogFile = new File(internalsDir + File.separator + "error.log");

            // create and append to the file
            wrt = new FileWriter(errLogFile, true);  // append if file already exists
            wrt.write(new Date().toString() + "\n");
            wrt.write("Appver " + BuildConfig.VERSION_NAME);
            if (mDatabaseHandler != null) { wrt.write(" with DBver " +mDatabaseHandler.mVersion); }
            wrt.write("\n");
            wrt.write("Android Version " + android.os.Build.VERSION.RELEASE + " API " + android.os.Build.VERSION.SDK_INT + "\n");
            wrt.write("Platform Manf " + Build.MANUFACTURER + " Model " + Build.MODEL + "\n");
            if (method != null) {
                if (!method.isEmpty()) {  wrt.write(method + "\n"); }
            }
            if (extra != null) {
                if (!extra.isEmpty()) {  wrt.write(extra + "\n"); }
            }
            wrt.write(errorMessage + "\n");
            wrt.write("=====\n");
            wrt.write("=====\n");
            wrt.flush();
            wrt.close();

            // force it to be shown and post an alert
            forceShowOnPC(errLogFile);
            if (!noAlert) { postAlert("An abort occured; details are in \'internals/error.log\'; contact the Developer"); } // noAlert is only needed when an Alert itself was being posted to the database and it failed to post

            // must send the toast indirectly in case this is being called from a utility thread
            Message msg = new Message();
            msg.what = ZeoCompanionApplication.MESSAGE_APP_SEND_TOAST;
            msg.obj = "Abort successfully logged to \'internals/error.log\'";
            mAppHandler.sendMessage(msg);
        } catch (Exception e) {
            if (wrt != null) { try { wrt.close(); } catch (Exception ignored) { } }
            Log.e(_CTAG + ".postToErrLog", "Cannot write to error.log: " + e.toString());
            e.printStackTrace();
        }
    }

    // Thread Context: may be called from utility threads, so cannot perform UI actions like Toast
    // write an alert message into the alerts.log file;
    // warning the database manager may be closed and therefore NULL
    public static void postAlert(String message) {
        if (ZeoCompanionApplication.mDatabaseHandler != null) {
            CompanionAlertRec aRec = new CompanionAlertRec(System.currentTimeMillis(), message);
            aRec.saveToDB(ZeoCompanionApplication.mDatabaseHandler);

            // inform the Main Activity so it can changes its menus; must be done via messaging
            if (MainActivity.instance != null) {
                if (MainActivity.instance.mHandler != null) {
                    Message msg = new Message();
                    msg.what = ZeoCompanionApplication.MESSAGE_MAIN_UPDATE_MENU;
                    MainActivity.instance.mHandler.sendMessage(msg);
                }
            }
        }
    }

    // are there any alerts present?
    public static int getQtyAlerts() {
        return ZeoCompanionApplication.mDatabaseHandler.getQtyCompanionAlertRecs();
    }

    // read all alerts in the indicate ArrayList
    public static void getAllAlerts(ArrayList<CompanionAlertRec> list) {
        Cursor cursor = ZeoCompanionApplication.mDatabaseHandler.getAllAlertRecs();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    CompanionAlertRec aRec = new CompanionAlertRec(cursor);
                    list.add(aRec);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    // delete one line in the alerts.log file
    public static void deleteAlertLine(long id) {
        CompanionAlertRec.removeFromDB(ZeoCompanionApplication.mDatabaseHandler, id);

        // inform the Main Activity so it can changes its menus; must be done via messaging
        if (MainActivity.instance != null) {
            Message msg = new Message();
            msg.what = ZeoCompanionApplication.MESSAGE_MAIN_UPDATE_MENU;
            MainActivity.instance.mHandler.sendMessage(msg);
        }
    }
}
