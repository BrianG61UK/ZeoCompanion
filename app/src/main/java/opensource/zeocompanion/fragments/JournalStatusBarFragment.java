package opensource.zeocompanion.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.zeo.ZeoAppHandler;

public class JournalStatusBarFragment extends Fragment {
    private View mRootView = null;
    private boolean mBlinkRedZeoApp = false;
    private boolean mViewDisabled = false;
    private Animation mBlinkRedZeoApp_Anim = null;

    private static final String _CTAG = "JSF";

    // constructor
    public JournalStatusBarFragment() {}

    // instanciator
    public static JournalStatusBarFragment newInstance() {
        JournalStatusBarFragment fragment = new JournalStatusBarFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //Log.d(_CTAG + ".onCreateView", "==========FRAG ON-CREATEVIEW=====");
        mRootView = inflater.inflate(R.layout.fragment_journal_status_bar, container, false);

        mRootView.findViewById(R.id.button_yesterday).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ZeoCompanionApplication.mCoordinator.daypointYesterdayButtonPressed();
            }
        });

        mRootView.findViewById(R.id.button_tomorrow).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ZeoCompanionApplication.mCoordinator.daypointTomorrowButtonPressed();
            }
        });

        mViewDisabled = false;
        return mRootView;
    }

    // Called when the fragment's view has been detached from the fragment
    @Override
    public void onDestroyView () {
        mViewDisabled = true;   // prevent the JSB handler methods from altering the JSB when its view is gone which can happen in edge cases
        super.onDestroyView();
        //Log.d(_CTAG + ".onDestroyView", "==========FRAG ON-DESTROYVIEW=====");
    }

    // Called when the fragment is visible to the user and actively running
    @Override
    public void onResume() {
        super.onResume();
        mViewDisabled = false;
        updateAppStatus();  // waiting for most everything in the App to be started
    }

    // called by various handlers if the Zeo App or the Journal status have changed, or if the daypoint has changed
    public void updateAppStatus() {
        if (mViewDisabled) { return; }  // JSB view has been destroyed; ignore this call

        // get current screen orientation information; this call's results change depending upon orientation
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        int screenWidthDp = (int)((float)screenSize.x / ZeoCompanionApplication.mScreenDensity);

        TextView todayTV = (TextView)mRootView.findViewById(R.id.textView_info);
        TextView yesterdayTV = (TextView)mRootView.findViewById(R.id.textView_yesterday);
        TextView tomorrowTV = (TextView)mRootView.findViewById(R.id.textView_tomorrow);
        Button yesterdayBut = (Button)mRootView.findViewById(R.id.button_yesterday);
        Button tomorrowBut = (Button)mRootView.findViewById(R.id.button_tomorrow);
        ImageView zeoDarkIM = (ImageView)mRootView.findViewById(R.id.imageView_zeoState_dark);
        ImageView zeoBrightIM = (ImageView)mRootView.findViewById(R.id.imageView_zeoState_bright);
        ImageView journalDarkIM = (ImageView)mRootView.findViewById(R.id.imageView_journalState_dark);
        ImageView journalBrightIM = (ImageView)mRootView.findViewById(R.id.imageView_journalState_bright);

        int daypoint = ZeoCompanionApplication.mCoordinator.getJournalDaypoint();

        yesterdayTV.setText(ZeoCompanionApplication.mCoordinator.getYesterdayDaypointStateString());
        todayTV.setText(ZeoCompanionApplication.mCoordinator.getTodayDaypointString());
        tomorrowTV.setText(ZeoCompanionApplication.mCoordinator.getTomorrowDaypointStateString());

        switch (daypoint) {
            case -1:
                yesterdayBut.setVisibility(View.INVISIBLE);
                tomorrowBut.setVisibility(View.VISIBLE);
                zeoDarkIM.setImageResource(R.drawable.button_blank_gray_dark_icon);
                zeoBrightIM.setImageResource(R.drawable.button_blank_gray_bright_icon);
                journalDarkIM.setImageResource(R.drawable.button_blank_gray_dark_icon);
                journalBrightIM.setImageResource(R.drawable.button_blank_gray_bright_icon);
                break;
            case 0:
                yesterdayBut.setVisibility(View.VISIBLE);
                tomorrowBut.setVisibility(View.INVISIBLE);
                if (mBlinkRedZeoApp) {
                    zeoDarkIM.setImageResource(R.drawable.button_blank_red_dark_icon);
                    zeoBrightIM.setImageResource(R.drawable.button_blank_red_bright_icon);
                    if (mBlinkRedZeoApp_Anim == null) {
                        Context c = getContext();   // edge case caused an abort because Context went null when JSB detacted from MainActivity
                        if (c != null) {
                            Animation mBlinkRedZeoApp_Anim = AnimationUtils.loadAnimation(c, R.anim.blink_led_continuous);
                            zeoDarkIM.startAnimation(mBlinkRedZeoApp_Anim);
                        }
                    }
                } else {
                    if (mBlinkRedZeoApp_Anim != null) {
                        zeoDarkIM.clearAnimation();
                        mBlinkRedZeoApp_Anim = null;
                    }
                    if (ZeoCompanionApplication.mZeoAppHandler.mZeoApp_State == ZeoAppHandler.ZAH_ZEOAPP_STATE_RECORDING) {
                        zeoDarkIM.setImageResource(R.drawable.button_blank_green_dark_icon);
                        zeoBrightIM.setImageResource(R.drawable.button_blank_green_bright_icon);
                    } else {
                        zeoDarkIM.setImageResource(R.drawable.button_blank_gray_dark_icon);
                        zeoBrightIM.setImageResource(R.drawable.button_blank_gray_bright_icon);
                    }
                }
                int cseState = ZeoCompanionApplication.mCoordinator.getJournalDaypointStateCode();
                if (cseState == CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_RECORDING) {
                    journalDarkIM.setImageResource(R.drawable.button_blank_green_dark_icon);
                    journalBrightIM.setImageResource(R.drawable.button_blank_green_bright_icon);
                } else {
                    journalDarkIM.setImageResource(R.drawable.button_blank_gray_dark_icon);
                    journalBrightIM.setImageResource(R.drawable.button_blank_gray_bright_icon);
                }
                break;
            case 1:
                yesterdayBut.setVisibility(View.VISIBLE);
                tomorrowBut.setVisibility(View.INVISIBLE);
                zeoDarkIM.setImageResource(R.drawable.button_blank_gray_dark_icon);
                zeoBrightIM.setImageResource(R.drawable.button_blank_gray_bright_icon);
                journalDarkIM.setImageResource(R.drawable.button_blank_gray_dark_icon);
                journalBrightIM.setImageResource(R.drawable.button_blank_gray_bright_icon);
                break;
        }

        TextView zs = (TextView)mRootView.findViewById(R.id.textView_zeoState);
        TextView js = (TextView)mRootView.findViewById(R.id.textView_journalState);
        if (screenWidthDp >= 1024) {
            if (daypoint == -1) { zs.setText("ZeoRec: "+ZeoCompanionApplication.mCoordinator.getZeoDaypointStateString());  }
            else { zs.setText("ZeoApp: "+ZeoCompanionApplication.mCoordinator.getZeoDaypointStateString());  }
        }  else { zs.setText(ZeoCompanionApplication.mCoordinator.getZeoDaypointStateString()); }

        if (screenWidthDp >= 1024) { js.setText("Journal: "+ZeoCompanionApplication.mCoordinator.getJournalDaypointStateString()); }
        else { js.setText(ZeoCompanionApplication.mCoordinator.getJournalDaypointStateString()); }
    }

    // set the entire JSB visible or not visible
    public void setVisible(boolean isVisible) {
        if (mViewDisabled) { return; }  // JSB view has been destroyed; ignore this call
        if (isVisible) { getView().setVisibility(View.VISIBLE); }
        else { getView().setVisibility(View.GONE); }
    }

    // pulse the Zeo App LED; gets triggered by every probe of the Zeo App (regardless of change of state or not)
    public void pulseZeoAppLED() {
        if (mViewDisabled) { return; }  // JSB view has been destroyed; ignore this call

        // check for conditions that would trigger a flashing red LED
        boolean lastBlinkRedZeoApp = mBlinkRedZeoApp;
        int result = ZeoCompanionApplication.mZeoAppHandler.checkforZeoAppAlarm();
        if (result != 0) { mBlinkRedZeoApp = true; }
        else { mBlinkRedZeoApp = false; }

        // only play a notification sound if this is at startup (not overnight)
        if (result == -1) {
            Context c = getContext();   // edge case caused an abort because Context went null when JSB detacted from MainActivity
            if (c != null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
                boolean playNotification = sp.getBoolean("journal_notification_if_norecord", true);
                if (playNotification) {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(c, notification);
                    r.play();
                }
            }
        }

        // has the blink condition changed?
        if (mBlinkRedZeoApp != lastBlinkRedZeoApp) { updateAppStatus(); }

        // if not blinking, then do a probe pulse
        if (!mBlinkRedZeoApp) {
            Context c = getContext();   // edge case caused an abort because Context went null when JSB detacted from MainActivity
            if (c != null) {
                Animation anim = AnimationUtils.loadAnimation(c, R.anim.pulse_led_once);
                ImageView iv = (ImageView)mRootView.findViewById(R.id.imageView_zeoState_dark);
                iv.startAnimation(anim);    // this animation is a one-shot and thus does not need to be programatically cancelled
            }
        }
    }
}
