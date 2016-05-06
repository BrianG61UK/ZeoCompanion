package opensource.zeocompanion.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.fragments.SharingActivityDialogFragment;

// this activity manages the sequence of end-user interactions for exporting/sharing content
public class SharingActivity extends AppCompatActivity {
    // note: the Sharing Activity and its Fragments will not be destroyed/recreated upon rotation
    // member variables
    public boolean mShareIntentActive = false;
    private SharingActivityDialogFragment mShareFrag1 = null;

    // member constants and other static content
    private static final String _CTAG = "SA";

    // receive inter-process messages from our fragments to handle Activity termination in parallel with DialogFragment termination
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ZeoCompanionApplication.MESSAGE_SHARING_DIALOG_TERMINATED) {
                //Log.d(_CTAG + ".mHandler", "Received terminate message from our Fragment");
                if (mShareIntentActive) {
                    mShareIntentActive = false;
                    ZeoCompanionApplication.mIrec_SAonly = null;    // do not destroy this as it was passed from the History Tab's cache
                    onBackPressed();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(_CTAG + ".onCreate", "=====ON-CREATE=====");
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_sharing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        // popup a send message dialog;
        mShareFrag1 = new SharingActivityDialogFragment();
        mShareFrag1.show(getSupportFragmentManager(), "DiagSH1");
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
    }

    // called when App first starts, or when App returns to the Foreground
    @Override
    protected void onStart () {
        super.onStart();
        //Log.d(_CTAG+".onStart","=====ON-START=====");
    }

    // called when the App is sent to the Background for another App to run
    @Override
    protected void onStop () {
        super.onStop();
        //Log.d(_CTAG + ".onStop", "=====ON-STOP=====");
    }

    // called when App first starts, or when this Activity returns to the Foreground from one of our other Activities
    @Override
    protected void onResume () {
        super.onResume();
        //Log.d(_CTAG + ".onResume", "=====ON-RESUME=====");
        if (mShareIntentActive) {
            mHandler.obtainMessage(ZeoCompanionApplication.MESSAGE_SHARING_DIALOG_TERMINATED, 0, 0, null).sendToTarget();
        }
    }

    // called when one of our own Activities comes to the Foreground and thus this one moves to the Background
    @Override
    protected void onPause () {
        super.onPause();
        //Log.d(_CTAG + ".onPause", "=====ON-PAUSE=====");
    }
}
