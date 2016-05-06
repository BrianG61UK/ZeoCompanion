package opensource.zeocompanion.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import opensource.zeocompanion.MainActivity;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionSleepEpisodesRec;
import opensource.zeocompanion.fragments.HistoryDetailActivityFragment;
import opensource.zeocompanion.utility.JournalDataCoordinator;
import opensource.zeocompanion.utility.Utilities;

// Activity that manages the History Detail page
public class HistoryDetailActivity extends AppCompatActivity {
    // member variable; remember that these will not be preserved across an orientation change

    // member constants and other static content
    public static final String _CTAG = "HDA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(_CTAG + ".onCreate", "=====ON-CREATE=====");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();
    }

    // called when the App is being terminated
    @Override
    protected void onDestroy () {
        super.onDestroy();
        //Log.d(_CTAG + ".onDestroy", "=====ON-DESTROY=====");
    }

    // called only when App returns to the Foreground
    @Override
    protected void onRestart () {
        super.onRestart();
        //Log.d(_CTAG + ".onRestart", "=====ON-RESTART=====");
        ZeoCompanionApplication.mIrec_SAonly = null;
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share:
                ZeoCompanionApplication.mIrec_SAonly = ZeoCompanionApplication.mIrec_HDAonly;
                Intent intent = new Intent(this, SharingActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_delete_journal_entry:
                final Utilities.ShowYesNoDialogInterface yesNoResponseListener = new Utilities.ShowYesNoDialogInterface() {
                    @Override
                    public void onYesNoDialogDone(boolean theResult, int callbackAction, String callbackString1, String ignored) {
                        if (callbackAction == 1 && theResult) {
                            // end-user confrmed yes to deleting the Journal record
                            if (ZeoCompanionApplication.mIrec_HDAonly != null) {
                                if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) {
                                    // delete the record
                                    CompanionSleepEpisodesRec.removeFromDB(ZeoCompanionApplication.mDatabaseHandler, ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rID);

                                    // inform the Journal Data Coordinator
                                    ZeoCompanionApplication.mCoordinator.informCSEdeleted(ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rID);
                                    ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.destroy();
                                    ZeoCompanionApplication.mIrec_HDAonly.theCSErecord = null;

                                    // inform the MainActivity that it must refresh its History tab
                                    if (MainActivity.instance.mHandler != null) {
                                        Message msg = new Message();
                                        msg.what = ZeoCompanionApplication.MESSAGE_MAIN_UPDATE_HISTORY;
                                        MainActivity.instance.mHandler.sendMessage(msg);
                                    }

                                    // decide whether to return to MainActivity or to refresh ourselves
                                    if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord == null) {
                                        // this was a Journal-only record; nothing more to show; return to the MainActivity
                                        finish();
                                    } else {
                                        // inform the fragment to refresh itself
                                        HistoryDetailActivityFragment frag = (HistoryDetailActivityFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_history_detail_container);
                                        frag.refreshDisplay();
                                    }
                                }
                            }
                        }
                    }
                };
                Utilities.showYesNoDialog(this, "Confirm", "You have chosen to permanently delete the Sleep Journal portion of this record.  The Zeo App portion cannot be deleted and will remain in-place.  Are you sure?",
                        "Delete", "Cancel", yesNoResponseListener, 1, "", "");
                return true;
        }

        // if not one of our actions, let the Android defaults handle it
        return super.onOptionsItemSelected(item);
    }

    // force close the Activity and its Fragments upon a caught Exception
    public void goBack() {
        onBackPressed();
    }

}
