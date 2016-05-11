package opensource.zeocompanion;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import opensource.zeocompanion.activities.AlertsActivity;
import opensource.zeocompanion.activities.HeadbandActivity;
import opensource.zeocompanion.activities.CustomizeActivity;
import opensource.zeocompanion.activities.OutboxActivity;
import opensource.zeocompanion.activities.SettingsActivity;
import opensource.zeocompanion.activities.SharingActivity;
import opensource.zeocompanion.database.CompanionDatabase;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.fragments.JournalStatusBarFragment;
import opensource.zeocompanion.fragments.MainAttributesFragment;
import opensource.zeocompanion.fragments.MainDashboardFragment;
import opensource.zeocompanion.fragments.MainDuringFragment;
import opensource.zeocompanion.fragments.MainFragmentWrapper;
import opensource.zeocompanion.fragments.MainGoingFragment;
import opensource.zeocompanion.fragments.MainHistoryFragment;
import opensource.zeocompanion.fragments.MainInbedFragment;
import opensource.zeocompanion.fragments.MainSummaryFragment;
import opensource.zeocompanion.utility.Utilities;
import opensource.zeocompanion.zeo.ZeoAppHandler;

// the first and primary Activity of the App
public class MainActivity extends AppCompatActivity {
    // member variables
    private SectionsPagerAdapter mSectionsPagerAdapter = null;
    private ViewPager mViewPager = null;
    public static SharedPreferences mPrefs = null;
    private JournalStatusBarFragment mJStatusBar = null;
    private boolean mKeepScreenOnEnabled = false;   // note that upon orientation change, keep-screen-on will be auto-turned off

    // member constants and other static content
    private static final String _CTAG = "MA";
    public static MainActivity instance = null;

    // receive inter-process messages to ensure that no inter-nested UI updates cause issues
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ZeoCompanionApplication.MESSAGE_MAIN_UPDATE_JSB:
                    // the JDC wants just the JSB Fragment to refresh itself
                    //Log.d(_CTAG + ".mHandler", "Received message to update JSB Zeo and App Status");
                    mJStatusBar.updateAppStatus();
                    mSectionsPagerAdapter.informOneFragmentsBySSToRefresh(TAB_HISTORY);
                    break;
                case ZeoCompanionApplication.MESSAGE_MAIN_ZAH_STATE_CHANGE:
                    // the ZAH indicates that the Zeo App has state changed
                    //Log.d(_CTAG + ".mHandler", "Received message that ZAH has detected a Zeo App state change");
                    mJStatusBar.updateAppStatus();
                    mJStatusBar.pulseZeoAppLED();
                    break;
                case ZeoCompanionApplication.MESSAGE_MAIN_ZAH_PROBE_NO_CHANGE:
                    // ZAH indicates that a probe has occurred that did not result in any changes to Zeo App state
                    //Log.d(_CTAG + ".mHandler", "Received message that ZAH has probed with no changes detected");
                    mJStatusBar.pulseZeoAppLED();
                    break;
                case ZeoCompanionApplication.MESSAGE_MAIN_UPDATE_ALL:
                    // the JDC has shifted daypoints, and needs the MainActivity fragments to re-assess whether they are read/write or now read-only
                    //Log.d(_CTAG + ".mHandler", "Received message to update All from a handler");
                    mSectionsPagerAdapter.informAllFragmentsDaypointChanged();
                    mJStatusBar.updateAppStatus();
                    break;
                case ZeoCompanionApplication.MESSAGE_MAIN_UPDATE_HISTORY:
                    // HistoryDetailActivity deleted a CompanionSleepEpisodesRec, and the History tab needs to refresh itself
                    //Log.d(_CTAG + ".mHandler", "Received message to refresh History tab from a child Activity");
                    mSectionsPagerAdapter.informOneFragmentsBySSToRefresh(TAB_HISTORY);
                    break;
                case ZeoCompanionApplication.MESSAGE_MAIN_UPDATE_MENU:
                    // Either the Alerting subsystem or the Outbox subsystem wants the menu to refresh due to content changes
                    //Log.d(_CTAG + ".mHandler", "Received message to refresh menus");
                    adjustMenus();
                    break;
            }
        }
    };

    // Thread context: Main Thread
    // the Zeo App Handler callback that the Zeo App status has changed
    // use a message to push the UI updates into the UI refresh loop rather than overloading the probe results processing in the JDC
    private ZeoAppHandler.ZAH_Listener ourZAH_Listener = new ZeoAppHandler.ZAH_Listener() {
        @Override
        public void onZeoAppStateChange() {
            Message msg = new Message();
            msg.what = ZeoCompanionApplication.MESSAGE_MAIN_ZAH_STATE_CHANGE;
            mHandler.sendMessage(msg);
        }
        @Override
        public void onZeoAppProbedSameState() {
            Message msg = new Message();
            msg.what = ZeoCompanionApplication.MESSAGE_MAIN_ZAH_PROBE_NO_CHANGE;
            mHandler.sendMessage(msg);
        }
    };

    // called then the Activity is first created or upon return of some other Activity back to this activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(_CTAG + ".onCreate", "=====ON-CREATE=====");
        instance = this;

        // get access to our preferences
        mPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        // now build the layout for the Activity
        setContentView(R.layout.activity_main);
        ZeoCompanionApplication.mScreenDensity  = getResources().getDisplayMetrics().density;

        // setup the AppBar's Toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get a handle to the journal status bar fragment
        mJStatusBar = (JournalStatusBarFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_journal_status_bar);

        // create the adaptor for the UI fragments for this activity
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter
        mViewPager = (ViewPager)findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //Log.d(_CTAG+".onPageSelected","=====PAGE-SELECTED=====");
                // do not show the Journal Status Bar for the Dashboard or History tabs
                if (mSectionsPagerAdapter.mPositionMap[position] == TAB_DASHBOARD || mSectionsPagerAdapter.mPositionMap[position] == TAB_HISTORY) {
                    mJStatusBar.setVisible(false);
                } else {
                    mJStatusBar.setVisible(true);
                }

                // inform a Fragment that it has become shown
                mSectionsPagerAdapter.informFragmentItsShowing(position);
                mSectionsPagerAdapter.informZeoAppHandler(position);

                // for the Going and During Fragments, when showing do not allow the screen to sleep or become locked
                int dp = ZeoCompanionApplication.mCoordinator.getJournalDaypoint();
                if (dp == 0 && (mSectionsPagerAdapter.mPositionMap[position] == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_GOING ||
                                mSectionsPagerAdapter.mPositionMap[position] == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_DURING)) {
                    toggleKeepScreenOn(position, true);
                } else {
                    toggleKeepScreenOn(position, false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // nothing needed here but method must be present
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // nothing needed here but method must be present
            }
        });

        // link the tabs in the UI with the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // set the proper starting tab
        mSectionsPagerAdapter.gotoStartingTab();

        // ensure we can write to external storage
        int r = ZeoCompanionApplication.checkExternalStorage();
        switch (r) {
            case -1:
                Utilities.showAlertDialog(this, "Warning", "No writable external storage is available; exports, database backups, and error logging will fail", "Okay");
                break;
            case -2:
                managePermissionActivation(1, Manifest.permission.WRITE_EXTERNAL_STORAGE, "External Storage", "Permission for Writable External Storage is needed by this App for exports, database backups, and error logging");
                break;
        }

        // validate we are interacting with the Zeo App
        int result = ZeoCompanionApplication.mZeoAppHandler.verifyAPI();
        if (result != ZeoAppHandler.ZAH_ERROR_NONE) {
            if (result == ZeoAppHandler.ZAH_ERROR_NO_PERMISSION) {
                try {
                    // force check whether the Zeo App is installed
                    boolean shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(this, "com.myzeo.permission.READ_SLEEP_RECORDS");
                    managePermissionActivation(2, "com.myzeo.permission.READ_SLEEP_RECORDS", "Zeo App", "Permission for access to the Zeo App is necessary to read its database for display, exporting, backup, and sleep journal coordination");
                } catch (Exception e) {
                    Toast.makeText(this, "ERROR: Zeo App is not installed; this App may show nothing.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "ERROR: "+ZeoCompanionApplication.mZeoAppHandler.getErrorString(result)+"; this App may show nothing.", Toast.LENGTH_LONG).show();
            }
        }

        // check if allowed to access the internet
        boolean de_enabled = mPrefs.getBoolean("email_enable", false);
        if (de_enabled) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                managePermissionActivation(3, Manifest.permission.INTERNET, "Internet", "Permission for access to the Internet is necessary to send exports via direct email");
            }
        }

        // perform journal resync and register callbacks with the ZeoAppHandler if the Sleep Journal is enabled
        if (mPrefs.getBoolean("journal_enable", true)) {
            result = ZeoCompanionApplication.mZeoAppHandler.resynchronize();
            if (result != ZeoAppHandler.ZAH_ERROR_NONE) {
                Toast.makeText(this, "ERROR: "+ZeoCompanionApplication.mZeoAppHandler.getErrorString(result)+"; this App may show nothing.", Toast.LENGTH_LONG).show();
            }

            ZeoCompanionApplication.mCoordinator.setMsgHandler(mHandler);
            ZeoCompanionApplication.mCoordinator.resynchronize();
            ZeoCompanionApplication.mCoordinator.registerWithZeoAppHandler();
            ZeoCompanionApplication.mZeoAppHandler.setZAH_Listener(ourZAH_Listener);
            ZeoCompanionApplication.mZeoAppHandler.dailyCheck();
        }

        // try to obtain the maximum bitmap texture size; it must be called after the view is fully displayed; this does not work on all hardware platforms;
        // the Samsung Galaxy Tab Pro properly returns its maximum (8192); a Amazon Fire 7 always returns 0 (and should be 4096);
        // this runs in the main thread via the messaging stack
        View v = findViewById(R.id.main_content);
        v.post(new Runnable() {
            @Override
            public void run() {
                int[] maxTextureSize = new int[1];
                GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
                Log.d(_CTAG + ".onCreate.view.run", "maxTextureSize for bitmaps =" + maxTextureSize[0]);
                if(maxTextureSize[0] > 0) {ZeoCompanionApplication.mMaxBitmapDim = maxTextureSize[0]; }
                else { ZeoCompanionApplication.mMaxBitmapDim = 4096; }
            }
        });
    }

    // called only when the Activity returns to the Foreground
    @Override
    protected void onRestart () {
        super.onRestart();
        //Log.d(_CTAG + ".onRestart", "=====ON-RESTART=====");
        // in this case we do not want to destroy the stored IntegratedHistoryRec (which should be held with others by MainHistoryFragment);
        // that fragment will do its own destruction of its IntegratedHistoryRecs
        ZeoCompanionApplication.mIrec_HDAonly = null;
        ZeoCompanionApplication.mIrec_SAonly = null;

        // check if settings were changed that require a complete redrawing of the fragments that are active
        if (mSectionsPagerAdapter.checkSettingsForPageAlteration()) {
            // indeed the layout needs changing; must flush out all the fragments to get a proper rebuild
            FragmentManager fm = getSupportFragmentManager();
            List<Fragment> al = fm.getFragments();
            if (al != null) {
                for (Fragment frag: al) {
                    fm.beginTransaction().remove(frag).commitAllowingStateLoss();
                }
            }
            recreate();     // force the Activity to recreate itself
        } else if (ZeoCompanionApplication.mDatabaseHandler.mDefinitionsChanged) {
            // otherwise, database definitional records have been changed, and Fragments with database driven lists need to refresh themselves
            mSectionsPagerAdapter.informAllFragmentsToRefresh();
            ZeoCompanionApplication.mDatabaseHandler.mDefinitionsChanged = false;
        }

        // re-check if allowed to access the internet if the direct-email setting was changed
        boolean de_enabled = mPrefs.getBoolean("email_enable", false);
        if (de_enabled) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                managePermissionActivation(3, Manifest.permission.INTERNET, "Internet", "Permission for access to the Internet is necessary to send exports via direct email");
            }
        }
    }

    // called when Activity first starts, or when Activity returns to the Foreground
    @Override
     protected void onStart () {
        super.onStart();
        //Log.d(_CTAG + ".onStart", "=====ON-START=====");

        // launch the Zeo App probe
        if (mPrefs.getBoolean("journal_enable", true)) {
            ZeoCompanionApplication.mZeoAppHandler.activateProbing();
        }
    }

    // paired with onStart and onRestart
    @Override
    protected void onStop () {
        //Log.d(_CTAG + ".onStop", "=====ON-STOP=====");
        ZeoCompanionApplication.mZeoAppHandler.terminateProbing();
        super.onStop();
    }

    // called when the Activity is being sent to the background or terminated
    // paired with onCreate
    @Override
    protected void onDestroy () {
        //Log.d(_CTAG + ".onDestroy", "=====ON-DESTROY=====");
        ZeoCompanionApplication.mCoordinator.setMsgHandler(null);
        ZeoCompanionApplication.mZeoAppHandler.terminateProbing();
        ZeoCompanionApplication.mZeoAppHandler.removeZAH_Listener(ourZAH_Listener);
        super.onDestroy();
    }

    // inflate the menu items in the Action Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // prepare the menu items in the Action Bar
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item1 = menu.findItem(R.id.action_alerts);
        int cnt1 = ZeoCompanionApplication.getQtyAlerts();
        if (cnt1 > 0) { item1.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM); }
        else { item1.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER); }

        MenuItem item2 = menu.findItem(R.id.action_email_outbox);
        int cnt2 = ZeoCompanionApplication.mEmailOutbox.getQtyEntries();
        if (cnt2 > 0) { item2.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM); }
        else { item2.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER); }
        return true;
    }

    // have seslected memu items "pop-out" if they have content
    public void adjustMenus() {
        invalidateOptionsMenu();
    }

    // handle presses of the menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_headband:
                // activate the headband commander activity, but first over-warn the end-user about even attempting this
                if (!(ZeoCompanionApplication.mAlreadyWarnedAboutHeadband)) {
                    // define a callback listener for a response to the YesNoDialog which is used when the end-user signals to delete an entry;
                    // if the confirmation answer was Yes, then delete the record from the database, and from the ListView

                    final Utilities.ShowYesNoDialogInterface yesNoResponseListener1 = new Utilities.ShowYesNoDialogInterface() {
                        @Override
                        public void onYesNoDialogDone(boolean theResult, int callbackAction, String callbackString1, String ignored) {
                            if (callbackAction == 1 && theResult) {
                                // end-user confrmed yes to utilize the Headband Commander
                                ZeoCompanionApplication.mAlreadyWarnedAboutHeadband = true;
                                Intent intent1 = new Intent(MainActivity.this, HeadbandActivity.class);
                                startActivity(intent1);
                            }
                        }
                    };
                    Utilities.showYesNoDialog(this, "DANGER!", "Using the Headband Commander is DANGEROUS. Improper commands can brick your Headband permanently. The Zeo App will also constantly regain control of the Headband; you have to keep doing Force Stops of the Zeo App from the Application Manager.  There are NO online or in-APP helps on using the Headband Commander. \n\nIf you brick your Headband you have only YOURSELF to blame.",
                            "Continue", "Cancel", yesNoResponseListener1, 1, "", "");

                } else {
                    int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
                    int permissionCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN);
                    if (permissionCheck1 == PackageManager.PERMISSION_DENIED || permissionCheck2 == PackageManager.PERMISSION_DENIED) {
                        managePermissionActivation(4, Manifest.permission.BLUETOOTH+","+Manifest.permission.BLUETOOTH_ADMIN, "Bluetooth", "Permission for access to Bluetooth is necessary to utilize the Headband Commander");
                    }


                    Intent intent1 = new Intent(MainActivity.this, HeadbandActivity.class);
                    startActivity(intent1);
                }
                return true;

            case R.id.action_settings:
                // invoke the Settings Activity
                Intent intent2 = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent2);
                return true;

            case R.id.action_maintAttribs:
                // invoke the Customize Activity starting at the Attributes tab
                if (ZeoCompanionApplication.mDatabaseHandler.mInvalidDB) {
                    Utilities.showAlertDialog(MainActivity.this, "Error", "Database is invalid; this facility cannot be utilized", "Okay");
                    return true;
                }
                Intent intent3 = new Intent(MainActivity.this, CustomizeActivity.class);
                intent3.putExtra("startTab", 0);
                startActivity(intent3);
                return true;

            case R.id.action_maintDoings:
                // invoke the Customize Activity starting at the Doings tab
                if (ZeoCompanionApplication.mDatabaseHandler.mInvalidDB) {
                    Utilities.showAlertDialog(MainActivity.this, "Error", "Database is invalid; this facility cannot be utilized", "Okay");
                    return true;
                }
                Intent intent4 = new Intent(MainActivity.this, CustomizeActivity.class);
                intent4.putExtra("startTab", 3);
                startActivity(intent4);
                return true;

            case R.id.action_share:
                // invoke the Sharing Activity
                ZeoCompanionApplication.mIrec_SAonly = null;
                Intent intent5 = new Intent(MainActivity.this, SharingActivity.class);
                startActivity(intent5);
                return true;

            case R.id.action_email_outbox:
                // invoke the Outbox Activity
                Intent intent6 = new Intent(MainActivity.this, OutboxActivity.class);
                startActivity(intent6);
                return true;

            case R.id.action_alerts:
                // invoke the Outbox Activity
                Intent intent7 = new Intent(MainActivity.this,AlertsActivity.class);
                startActivity(intent7);
                return true;

            case R.id.action_backupDB:
                // copy the ZeoCompanion's database to external storage for use by the end-user
                String msg1 = ((ZeoCompanionApplication)getApplication()).saveCopyOfDB("");
                if (msg1.isEmpty()) { Toast.makeText(this, "ZeoCompanion DB successfully backed up to Device External Storage", Toast.LENGTH_LONG).show(); }
                else { Utilities.showAlertDialog(this, "Error", msg1, "Okay"); }
                return true;

            case R.id.action_restoreDB:
                final Utilities.ShowYesNoDialogInterface yesNoResponseListener2 = new Utilities.ShowYesNoDialogInterface() {
                    @Override
                    public void onYesNoDialogDone(boolean theResult, int callbackAction, String theFileName, String ignored) {
                        if (callbackAction == 2 && theResult) {
                            // end-user confirmed yes; restore the ZeoCompanion's database from external storage
                            String msg3 = ((ZeoCompanionApplication)getApplication()).restoreCopyOfDB(theFileName);
                            if (msg3.isEmpty()) {
                                Toast.makeText(MainActivity.this, "ZeoCompanion DB successfully restored from Device External Storage", Toast.LENGTH_LONG).show();
                                ZeoCompanionApplication.mCoordinator.resynchronize();
                                if (mJStatusBar != null) { mJStatusBar.updateAppStatus(); }
                                mSectionsPagerAdapter.informAllFragmentsToRefresh();
                                ZeoCompanionApplication.mDatabaseHandler.mDefinitionsChanged = false;
                            } else {
                                Utilities.showAlertDialog(MainActivity.this, "Error", msg3, "Okay");
                            }
                        }
                    }
                };

                final Utilities.ShowFileSelectDialogInterface selectedFileListener = new Utilities.ShowFileSelectDialogInterface() {
                    @Override
                    public void showFileSelectDialogChosenFile(String theFileName, String subfolder) {
                        CompanionDatabase.ValidateDatabaseResults result = ((ZeoCompanionApplication)getApplication()).restoreCopyOfDB_prep(theFileName);
                        if (!result.mResultMsg.isEmpty()) {
                            Utilities.showAlertDialog(MainActivity.this, "Error", result.mResultMsg, "Okay");
                        } else {
                            String msg = "You are attempting to restore a ";
                            if (result.mSystemRec != null) {
                                msg = msg + "DBver " + result.mSystemRec.rMost_recent_DBVer + " ";
                            }
                            msg = msg + "ZeoCompanion database";
                            if (result.mSystemRec != null) {
                                if (result.mSystemRec.rUserName != null) {
                                    if (!result.mSystemRec.rUserName.isEmpty()) {
                                        msg = msg + " for \'" + result.mSystemRec.rUserName + "\'";
                                    }
                                }
                                if (result.mSystemRec.rMost_recent_AppVer != null) {
                                    if (!result.mSystemRec.rMost_recent_AppVer.isEmpty()) {
                                        msg = msg + " from AppVer " + result.mSystemRec.rMost_recent_AppVer;
                                    }
                                }
                            }
                            msg = msg +  " file name \'" + theFileName + "\'.";
                            msg = msg + "\nThis action is NON-REVERSABLE! All new data collected in the current database on your device since this backup was made will be lost. This restore will have no effect on the Zeo App's database.";
                            Utilities.showYesNoDialog(MainActivity.this, "Confirm", msg, "Restore", "Cancel", yesNoResponseListener2, 2, theFileName, subfolder);
                        }
                    }
                };

                Utilities.showFileSelectDialog(this, "Pick Database Name To Restore", "internals", selectedFileListener);
                return true;

            case R.id.action_factoryDefaults:
                if (ZeoCompanionApplication.mDatabaseHandler.mInvalidDB) {
                    Utilities.showAlertDialog(MainActivity.this, "Error", "Database is invalid; restore factory defaults cannot be utilized", "Okay");
                    return true;
                }
                final Utilities.ShowYesNoDialogInterface yesNoResponseListener3 = new Utilities.ShowYesNoDialogInterface() {
                    @Override
                    public void onYesNoDialogDone(boolean theResult, int callbackAction, String callbackString1, String ignored) {
                        if (callbackAction == 3 && theResult) {
                            // end-user confirmed yes to realoding the factory defaults
                            String msg4 = ZeoCompanionApplication.mDatabaseHandler.reloadFactoryDefaults();
                            if (!msg4.isEmpty()) {
                                Utilities.showAlertDialog(MainActivity.this, "Error", msg4, "Okay");
                            } else {
                                mSectionsPagerAdapter.informAllFragmentsToRefresh();
                            }
                        }
                    }
                };
                Utilities.showYesNoDialog(this, "Confirm", "Are you sure you want to reload the Factory Defaults for all Attribute, Value, and Event Doing tables?  You will lose any Customizations you may have made.",
                        "Continue", "Cancel", yesNoResponseListener3, 3, "", "");
                return true;

            case R.id.action_user_guide:
                String url = "https://github.com/azmikemm/ZeoCompanion/raw/master/Resources/Users Guide/ForVersions/Users_Guide_" + BuildConfig.VERSION_NAME + ".pdf";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                break;

            case R.id.action_about:
                // show an "About" alert dialog
                String str = "ZeoCompanion App Version " + BuildConfig.VERSION_NAME + " Build " + BuildConfig.VERSION_CODE;
                str = str + "\nDatabase Version " + ZeoCompanionApplication.mDatabaseHandler.mVersion;
                int qty = ZeoCompanionApplication.mEmailOutbox.getQtyEntries();
                if (qty > 0) { str = str + "\nEmail Outbox unsent emails=" + qty; }
                qty = ZeoCompanionApplication.mDatabaseHandler.getQtyCompanionSleepEpisodeRecs();
                if (qty >= 0) { str = str + "\nDatabase recs=" + ZeoCompanionApplication.mDatabaseHandler.getQtyCompanionSleepEpisodeRecs(); }
                else { str = str + "\nDATABASE IS INVALID!"; }
                qty = ZeoCompanionApplication.mDatabaseHandler.getQtyZeoSleepEpisodeRecs();
                if (qty >= 0) {
                    str = str + "\nZeo Replicated recs=" + qty;
                    boolean enabled = mPrefs.getBoolean("database_replicate_zeo", false);
                    if (!enabled) { str = str + " (DISABLED!)"; }
                }
                str = str + "\nLimited support: zeocompanion@mail.com";
                Utilities.showAlertDialog(this, "About", str, "Okay");
                return true;
        }

        // if not one of our actions, let the Android defaults handle it
        return super.onOptionsItemSelected(item);
    }

    // the device's orientation has changed
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mSectionsPagerAdapter.resetAllTabTitles();
    }

    // handle permission granting in Android 6.x and beyond; by definition the necessary permission is not granted
    private void managePermissionActivation(int callbackCode, String permissions, String permissionHumanReadable, String reasonText) {
        final Utilities.ShowYesNoDialogInterface yesNoResponseListener_permissions = new Utilities.ShowYesNoDialogInterface() {
            @Override
            public void onYesNoDialogDone(boolean theResult, int callbackCode, String permissions, String ignored) {
                if (theResult) {
                    String[] permissionsSplit2 = permissions.split(",");
                    ActivityCompat.requestPermissions(MainActivity.this, permissionsSplit2, callbackCode);
                }
            }
        };

        String[] permissionsSplit = permissions.split(",");
        boolean rationaleNeeded = false;
        for (String permit: permissionsSplit) {
            rationaleNeeded = (rationaleNeeded | ActivityCompat.shouldShowRequestPermissionRationale(this, permit));
        }
        if (rationaleNeeded) {
            // Show an explanation to the user
            Utilities.showYesNoDialog(this, "Permission: "+permissionHumanReadable, reasonText, "Okay", null, yesNoResponseListener_permissions, callbackCode, permissions, null);
        } else {
            // No explanation needed, we can request the permission
            ActivityCompat.requestPermissions(this, permissionsSplit, callbackCode);
        }
    }

    // for selected Fragments, when actually showing, do not allow the screen to turn off while charging and auto-dim the display
    public void toggleKeepScreenOn(final int position, boolean enabled) {
        if (enabled && !mKeepScreenOnEnabled) {
            // want to turn on KEEP_SCREEN_ON and it is not already enabled
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mKeepScreenOnEnabled = true;
            //Log.d(_CTAG + ".togKeepScreenOn", "FLAG_KEEP_SCREEN_ON is set");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mKeepScreenOnEnabled) {
                        //Log.d(_CTAG + ".togKeepScreenOn", "Auto dimming the display");
                        WindowManager.LayoutParams lparams = getWindow().getAttributes();
                        lparams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
                        lparams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
                        getWindow().setAttributes(lparams);
                        mSectionsPagerAdapter.informFragmentToDimControls(position);
                    }
                }
            }, 30000);
        } else if (enabled && mKeepScreenOnEnabled) {
            // want to turn on KEEP_SCREEN_ON and it is already enabled but we may be tabbing
            WindowManager.LayoutParams lparams = getWindow().getAttributes();
            if (lparams.screenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF) {
                mSectionsPagerAdapter.informFragmentToDimControls(position);
            }
        } else if (!enabled && mKeepScreenOnEnabled) {
            // want to turn off KEEP_SCREEN_ON and it is currently enabled
            mKeepScreenOnEnabled = false;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            WindowManager.LayoutParams lparams = getWindow().getAttributes();
            lparams.screenBrightness =  WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            lparams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            getWindow().setAttributes(lparams);
            mSectionsPagerAdapter.informAllFragmentsToUndimControls();
            //Log.d(_CTAG+".togKeepScreenOn","FLAG_KEEP_SCREEN_ON is removed");
        }
    }

    // Note: FragmentPagerAdapter will completely activate up to three fragments
    // simultaneously such that two "off screen" fragments "to the right" and "to the left" are up and through onResume()
    // Note: all fragments used by this custom adaptor must be subclasses of MainFragmentWrapper
    private static final int TAB_HISTORY = -2;
    private static final int TAB_SUMMARY = -3;
    private static final int TAB_DASHBOARD = -1;
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private static final int TOTAL_TABS = 8;
        private int mActiveTabsCnt = TOTAL_TABS;
        private Fragment[] mFragList = new Fragment[TOTAL_TABS];
        public int[] mPositionMap = {TAB_DASHBOARD, CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE, CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED,
                CompanionDatabaseContract.SLEEP_EPISODE_STAGE_GOING, CompanionDatabaseContract.SLEEP_EPISODE_STAGE_DURING, CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER,
                TAB_SUMMARY, TAB_HISTORY};

        // constructor:  determine which tabs are displayed based upon the end-user's preferences
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            if (MainActivity.mPrefs.getBoolean("journal_enable", true) && !ZeoCompanionApplication.mDatabaseHandler.mInvalidDB) {
                // the Sleep Journal system is enabled
                int p = 0;
                //mPositionMap[p] = TAB_DASHBOARD;   // dashboard fragment      // TODO V1.1 Dashboard Tab
                //p++;
                if (MainActivity.mPrefs.getBoolean("journal_enable_before", true)) {
                    mPositionMap[p] = CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE;
                    p++;
                }
                if (MainActivity.mPrefs.getBoolean("journal_enable_inbed", true)) {
                    mPositionMap[p] = CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED;
                    p++;
                }
                if (MainActivity.mPrefs.getBoolean("journal_enable_going", true)) {
                    mPositionMap[p] = CompanionDatabaseContract.SLEEP_EPISODE_STAGE_GOING;
                    p++;
                }
                if (MainActivity.mPrefs.getBoolean("journal_enable_during", true)) {
                    mPositionMap[p] = CompanionDatabaseContract.SLEEP_EPISODE_STAGE_DURING;
                    p++;
                }
                if (MainActivity.mPrefs.getBoolean("journal_enable_after", true)) {
                    mPositionMap[p] = CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER;
                    p++;
                }
                mPositionMap[p] = TAB_SUMMARY;   // summary fragment
                p++;
                mPositionMap[p] = TAB_HISTORY;   // history fragment
                p++;
                mActiveTabsCnt = p;
            } else {
                // the Sleep Journal system is disabled
                /*mPositionMap[0] = TAB_DASHBOARD;   // dashboard fragment    // TODO V1.1 Dashboard Tab
                mPositionMap[1] = TAB_HISTORY;   // history fragment
                mActiveTabsCnt = 2;*/
                mPositionMap[1] = TAB_HISTORY;   // history fragment
                mActiveTabsCnt = 1;
            }
        }

        // call via Activity's onRestart when Settings may have changed the end-user's preferences
        public boolean checkSettingsForPageAlteration() {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            if (MainActivity.mPrefs.getBoolean("journal_enable", true)) {
                // the Sleep Journal system is enabled
                int p = 0;
                /*if (mPositionMap[p] != TAB_DASHBOARD) {    // dashboard fragment  // TODO V1.1 Dashboard Tab
                    return true;
                }
                p++;*/
                if (MainActivity.mPrefs.getBoolean("journal_enable_before", true)) {
                    if (mPositionMap[p] != CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE) {
                        return true;
                    } else {
                        p++;
                    }
                }
                if (MainActivity.mPrefs.getBoolean("journal_enable_inbed", true)) {
                    if (mPositionMap[p] != CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED) {
                        return true;
                    } else {
                        p++;
                    }
                }
                if (MainActivity.mPrefs.getBoolean("journal_enable_going", true)) {
                    if (mPositionMap[p] != CompanionDatabaseContract.SLEEP_EPISODE_STAGE_GOING) {
                        return true;
                    } else {
                        p++;
                    }
                }
                if (MainActivity.mPrefs.getBoolean("journal_enable_during", true)) {
                    if (mPositionMap[p] != CompanionDatabaseContract.SLEEP_EPISODE_STAGE_DURING) {
                        return true;
                    } else {
                        p++;
                    }
                }
                if (MainActivity.mPrefs.getBoolean("journal_enable_after", true)) {
                    if (mPositionMap[p] != CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER) {
                        return true;
                    } else {
                        p++;
                    }
                }
                if (mPositionMap[p] != TAB_SUMMARY) {    // summary fragment
                    return true;
                }
                p++;
                if (mPositionMap[p] != TAB_HISTORY) {    // history fragment
                    return true;
                }
            } else {
                // the Sleep Journal system is disabled
                //if (mActiveTabsCnt != 2) return true;     // TODO V1.1 Dashboard Tab
                if (mActiveTabsCnt != 1) return true;
            }
            return false;
        }

        // goto the end-user's preferred initial tab
        public void gotoStartingTab() {
            String firstTabStr = MainActivity.mPrefs.getString("main_first_tab", "History Tab");
            int seekTab = TAB_HISTORY;
            //if (firstTabStr.equals("Dashboard Tab")) { seekTab = TAB_DASHBOARD; } // TODO V1.1 Dashboard Tab
            if (firstTabStr.equals("Dashboard Tab")) { seekTab = TAB_HISTORY; }
            else if (firstTabStr.equals("1st Journal Tab")) {
                for (int i = 0; i < mActiveTabsCnt; i++) {
                    if (mPositionMap[i] > 0) { mViewPager.setCurrentItem(i, false);  informFragmentItsShowing(i); return; }
                }
            }

            for (int i = 0; i < mActiveTabsCnt; i++) {
                if (mPositionMap[i] == seekTab) { mViewPager.setCurrentItem(i, false); informFragmentItsShowing(i); return; }
            }
        }

        // create the proper fragment for the specified tab;
        // retain these Fragment objects in our own array
        @Override
        public Fragment getItem(int position) {
            switch (mPositionMap[position]) {
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE:
                    mFragList[position] = MainAttributesFragment.newInstance(CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE);
                    return mFragList[position];
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED:
                    mFragList[position] = MainInbedFragment.newInstance(CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED);
                    return mFragList[position];
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_GOING:
                    mFragList[position] = MainGoingFragment.newInstance(CompanionDatabaseContract.SLEEP_EPISODE_STAGE_GOING);
                    return mFragList[position];
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_DURING:
                    mFragList[position] = MainDuringFragment.newInstance(CompanionDatabaseContract.SLEEP_EPISODE_STAGE_DURING);
                    return mFragList[position];
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER:
                    mFragList[position] = MainAttributesFragment.newInstance(CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER);
                    return mFragList[position];
                case TAB_SUMMARY:
                    mFragList[position] = MainSummaryFragment.newInstance();
                    return mFragList[position];
                case TAB_HISTORY:
                    mFragList[position] = MainHistoryFragment.newInstance();
                    return mFragList[position];
                case TAB_DASHBOARD:
                    mFragList[position] = MainDashboardFragment.newInstance();
                    return mFragList[position];
            }
            return null;
        }

        // called when a Fragment is being put into place (it could be a reused fragment and hence getItem() will not get called)
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            mFragList[position] = fragment;
            return fragment;
        }

        // called when a Fragment is being destroyed by the framework
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragList[position] = null;
            super.destroyItem(container, position, object);
        }

        // return the current quantity of tabs to be shown
        @Override
        public int getCount() {
            return mActiveTabsCnt;
        }

        // reset all the tab titles (usually because device orientation has changed)
        public void resetAllTabTitles() {
            TabLayout tabs = (TabLayout)findViewById(R.id.tabs);
            for (int i = 0; i < mFragList.length; i++)      {
                tabs.getTabAt(i).setText(getPageTitle(i));
            }
        };

        // return the title to be used for each tab
        @Override
        public CharSequence getPageTitle(int position) {
            // get current screen orientation information; this call's results change depending upon orientation
            Display display = getWindowManager().getDefaultDisplay();
            Point screenSize = new Point();
            display.getSize(screenSize);
            int screenWidthDp = (int)((float)screenSize.x / ZeoCompanionApplication.mScreenDensity);

            // determine whether "large fonts" is enabled in the Android Accessibility Settings
            Configuration c = getResources().getConfiguration();
            boolean largeText = false;
            if (c.fontScale > 1.0) { largeText = true; }

            switch (mPositionMap[position]) {
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE:
                    if (screenWidthDp < 600) {
                        return "Pre Bed";
                    }
                    return "Prior To Bed";
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED:
                    return "In Bed";
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_GOING:
                    if (screenWidthDp < 600) {
                        return "Fall Z";
                    } else if (screenWidthDp < 800) {
                        return "Going sleep";
                    } else if (screenWidthDp < 1000 && largeText && mActiveTabsCnt == TOTAL_TABS) {
                        return "Going sleep";
                    }
                    return "Going to sleep";
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_DURING:
                    if (screenWidthDp < 600) {
                        return "In Z";
                    }
                    return "While sleep";
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER:
                    if (screenWidthDp < 600) {
                        return "End Z";
                    } else if (screenWidthDp < 800) {
                        return "After sleep";
                    }
                    return "After sleep";
                case TAB_SUMMARY:
                    if (screenWidthDp < 600) {
                        return "Sum Z";
                    } else if (screenWidthDp < 800) {
                        return "Jour Summ";
                    } else if (screenWidthDp < 1000 && largeText && mActiveTabsCnt == TOTAL_TABS) {
                        return "Journal Summ";
                    }
                    return "Journal Summary";
                case TAB_HISTORY:
                    if (screenWidthDp < 800) {
                        return "Hist";
                    } else if (screenWidthDp < 1000 && largeText && mActiveTabsCnt == TOTAL_TABS) {
                        return "Hist";
                    }
                    return "History";
                case TAB_DASHBOARD:
                    if (screenWidthDp < 800) {
                        return "Dash board";
                    } else if (screenWidthDp < 1000 && largeText && mActiveTabsCnt == TOTAL_TABS) {
                        return "Dash board";
                    }
                    return "Dashboard";
            }
            return null;
        }

        // inform the ZeoAppHandler about transitions between tabs in order to proper control Zeo App probing
        public void informZeoAppHandler(int position) {
            switch (mPositionMap[position]) {
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE:
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED:
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_DURING:
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_GOING:
                case CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER:
                case TAB_SUMMARY:
                    ZeoCompanionApplication.mZeoAppHandler.probing_OnJournalTabs();
                    break;
                case TAB_HISTORY:
                case TAB_DASHBOARD:
                    ZeoCompanionApplication.mZeoAppHandler.probing_OnNonJournalTabs();
                    break;
            }
        }

        // inform a Fragment that it has become shown due to tabbing or swiping
        public void informFragmentItsShowing(int position) {
            if (mFragList[position] != null) {
                ((MainFragmentWrapper) mFragList[position]).fragmentBecameShown();
            }
        }

        // inform all fragments to undim their controls
        public void informAllFragmentsToUndimControls() {
            for (int i = 0; i < mFragList.length; i++) {
                if (mFragList[i] != null) {
                    ((MainFragmentWrapper) mFragList[i]).dimControlsForSleep(false);
                }
            }
        }

        // inform the specified active fragment to dim its controls
        public void informFragmentToDimControls(int position) {
            if (mFragList[position] != null) {
                ((MainFragmentWrapper) mFragList[position]).dimControlsForSleep(true);
            }
        }

        // inform one Fragments to refresh itself
        public void informOneFragmentsBySSToRefresh(int sleepStage) {
            for (int i = 0; i < mFragList.length; i++) {
                if (mPositionMap[i] == sleepStage) {
                    if (mFragList[i] != null) {
                        ((MainFragmentWrapper) mFragList[i]).needToRefresh();
                    }
                }
            }
        }

        // inform all Fragments that database definition record changes have occurred
        public void informAllFragmentsToRefresh() {
            for (int i = 0; i < mFragList.length; i++) {
                if (mFragList[i] != null) {
                    ((MainFragmentWrapper) mFragList[i]).needToRefresh();
                }
            }
        }

        // inform all Fragments that database definition record changes have occurred
        public void informAllFragmentsDaypointChanged() {
            for (int i = 0; i < mFragList.length; i++) {
                if (mFragList[i] != null) {
                    ((MainFragmentWrapper) mFragList[i]).daypointHasChanged();
                }

            }
        }
    }
}
