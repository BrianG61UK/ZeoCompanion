package opensource.zeocompanion.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.activities.SharingActivity;
import opensource.zeocompanion.utility.CSVexporter;
import opensource.zeocompanion.utility.DirectEmailerThread;
import opensource.zeocompanion.utility.ImageExporter;
import opensource.zeocompanion.utility.Utilities;
import com.android.EvtSpinner;

// primary UI fragment that gathers parameters and performs a manual export of data in CSV or Image formats
public class SharingActivityDialogFragment extends DialogFragment {
    // note: the Sharing Activity and its Fragments will not be destroyed/recreated upon rotation
    // member variables
    private View mRootView = null;
    private SharingActivity mActivity;
    private int[] mShareWhatMap = new int[CSVexporter.SHARE_WHAT_CSV_COUNT + ImageExporter.SHARE_WHAT_IMAGE_COUNT];

    // member constants and other static content
    private static final String _CTAG = "SAF";

    public static final int SHARE_SENDHOW_LEAVEINFILES = 1;
    public static final int SHARE_SENDHOW_DIRECTEMAIL = 2;
    public static final int SHARE_SENDHOW_SHARE = 3;

    // listener that acts as an alert dialog so as to trigger a dismiss if necessary
    private Utilities.ShowYesNoDialogInterface mYesNoResponseListener_alerts = new Utilities.ShowYesNoDialogInterface() {
        @Override
        public void onYesNoDialogDone(boolean theResult, int callbackCode, String ignored1, String ignored2) {
            if (callbackCode != 0) {
                ZeoCompanionApplication.mIrec_SAonly = null;    // do not destroy this as it was passed from the History Tab's cache
                mActivity.onBackPressed();
            }
        }
    };

    // constructor
    public SharingActivityDialogFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(_CTAG + ".onCreateView", "==========FRAG ON-CREATEVIEW=====");
        mRootView = inflater.inflate(R.layout.fragment_sharing, container, false);
        mActivity = (SharingActivity)getActivity();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean directEmailEnabled = prefs.getBoolean("email_enable", false);

        CheckBox cb = (CheckBox)mRootView.findViewById(R.id.checkBox_exportAll);
        final DatePicker dp = (DatePicker)mRootView.findViewById(R.id.datePicker_start);
        if (ZeoCompanionApplication.mIrec_SAonly != null) { dp.setVisibility(View.GONE); cb.setVisibility(View.GONE); }
        else { dp.setVisibility(View.VISIBLE); cb.setVisibility(View.VISIBLE); }

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { dp.setVisibility(View.INVISIBLE); }
                else { dp.setVisibility(View.VISIBLE); }
            }
        });

        mRootView.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ZeoCompanionApplication.mIrec_SAonly = null;    // do not destroy this as it was passed from the History Tab's cache
                mActivity.onBackPressed();
            }
        });

        mRootView.findViewById(R.id.button_share).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doFinalize(SHARE_SENDHOW_SHARE);
            }
        });

        mRootView.findViewById(R.id.button_leaveInFiles).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doFinalize(SHARE_SENDHOW_LEAVEINFILES);
            }
        });

        Button bt = (Button)mRootView.findViewById(R.id.button_directEmail);
        if (directEmailEnabled) {
            bt.setVisibility(View.VISIBLE);
            bt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    doFinalize(SHARE_SENDHOW_DIRECTEMAIL);
                }
            });
        } else { bt.setVisibility(View.INVISIBLE); }

        // compose the drop-down with applicable exports
        int p = 0;
        ArrayList<String> shareWhat = new ArrayList<String>();
        if (ZeoCompanionApplication.mIrec_SAonly != null) {
            shareWhat.add("Image/ZeoApp");
            mShareWhatMap[p] = ImageExporter.SHARE_WHAT_IMAGE_STANDARD;
            p++;
        }
        shareWhat.add("CSV/SS/myZeo+/minutes");
        mShareWhatMap[p] = CSVexporter.SHARE_WHAT_CSV_EXCEL;
        p++;
        shareWhat.add("CSV/ZeoViewer/myZeo+/minutes");
        mShareWhatMap[p] = CSVexporter.SHARE_WHAT_CSV_ZEOVIEWER;
        p++;
        shareWhat.add("CSV/Sleepyhead/myZeo+/epochs");
        mShareWhatMap[p] = CSVexporter.SHARE_WHAT_CSV_SLEEPYHEAD;
        p++;
        shareWhat.add("CSV/SS/ZeoRaw/epochs");
        mShareWhatMap[p] = CSVexporter.SHARE_WHAT_CSV_ZEORAW;
        p++;
        shareWhat.add("CSV/SS/ZeoRaw+/epochs");
        mShareWhatMap[p] = CSVexporter.SHARE_WHAT_CSV_ZEORAW_DEAD;
        p++;
        EvtSpinner theSpinner = (EvtSpinner)mRootView.findViewById(R.id.spinner_shareWhat);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, shareWhat);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        theSpinner.setAdapter(adapter);

        return mRootView;
    }

    // Called when the fragment's view has been detached from the fragment
    @Override
    public void onDestroyView () {
        super.onDestroyView();
        //Log.d(_CTAG + ".onDestroyView", "==========FRAG ON-DESTROYVIEW=====");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Log.d(_CTAG + ".onViewCreated", "==========FRAG ON-VIEWCREATED=====");
        getDialog().setTitle("Share/Export");
    }

    // Called when the fragment is no longer attached to its activity
    @Override
    public void onDetach () {
        super.onDetach();
        //Log.d(_CTAG + ".onDetach", "==========FRAG ON-DETACH=====");
    }

    // one of the buttons (other than Cancel) was pressed
    private void doFinalize(int sendHow) {
        CheckBox theCheckbox = (CheckBox)mRootView.findViewById(R.id.checkBox_exportAll);
        EvtSpinner theSpinner = (EvtSpinner)mRootView.findViewById(R.id.spinner_shareWhat);
        int position = theSpinner.getSelectedItemPosition();

        Date fromDate = null;
        if (!theCheckbox.isChecked()) {
            DatePicker dp = (DatePicker) mRootView.findViewById(R.id.datePicker_start);
            if (dp.getVisibility() == View.VISIBLE) {
                int day = dp.getDayOfMonth();
                int month = dp.getMonth();
                int year = dp.getYear();
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day, 0, 0, 0);
                fromDate = cal.getTime();
            }
        }
       performSharing(mShareWhatMap[position], sendHow, fromDate);
    }

    // perform the sharing activity; this also needs to decide whether to immediately dismiss() or defer for acknowledgement of a message
    public void performSharing(int shareWhat, int sendHow, Date fromWhen) {
        File exportFile = null;
        switch (shareWhat) {
            case ImageExporter.SHARE_WHAT_IMAGE_STANDARD:
                // create the image file
                exportFile = createImageFile(shareWhat);
                break;

            case CSVexporter.SHARE_WHAT_CSV_EXCEL:
            case CSVexporter.SHARE_WHAT_CSV_ZEOVIEWER:
            case CSVexporter.SHARE_WHAT_CSV_SLEEPYHEAD:
            case CSVexporter.SHARE_WHAT_CSV_ZEORAW:
            case CSVexporter.SHARE_WHAT_CSV_ZEORAW_DEAD:
                // create the CSV file
                exportFile = createCSVFile(shareWhat, fromWhen);
                break;
        }

        if (exportFile == null) {
            // export failed; error messages are being displayed and will auto-dismiss
            return;
        } else if (sendHow == SHARE_SENDHOW_LEAVEINFILES) {
            // just leave the file in the external storage if so directed
            ZeoCompanionApplication.forceShowOnPC(exportFile);
            ZeoCompanionApplication.mIrec_SAonly = null;    // do not destroy this as it was passed from the History Tab's cache
            mActivity.onBackPressed();
            return;
        }

        // sharing or emailing is wanted; compose a subject / body text line
        String str = "ZeoCompanion ";
        switch (shareWhat) {
            case CSVexporter.SHARE_WHAT_CSV_EXCEL:
                str = str + "CSV SS-myZeo-Minutes";
                break;
            case CSVexporter.SHARE_WHAT_CSV_ZEOVIEWER:
                str = str + "CSV ZeoDataViewer-myZeo-Minutes";
                break;
            case CSVexporter.SHARE_WHAT_CSV_SLEEPYHEAD:
                str = str + "CSV Sleepyhead-myZeo-Epochs";
                break;
            case CSVexporter.SHARE_WHAT_CSV_ZEORAW:
                str = str + "CSV SS-ZeoDB-Epochs";
                break;
            case CSVexporter.SHARE_WHAT_CSV_ZEORAW_DEAD:
                str = str + "CSV SS-ZeoDB-Epochs";
                break;
            case ImageExporter.SHARE_WHAT_IMAGE_STANDARD:
                str = str + "Image Standard";
                break;
        }
        str = str + " manual export";

        // start sending the content
        if (sendHow == SHARE_SENDHOW_DIRECTEMAIL) {
            // direct email
            boolean r = emailFile(exportFile, str);
            if (r) {
                // only dismiss if the email thread was successfully started
                ZeoCompanionApplication.mIrec_SAonly = null;    // do not destroy this as it was passed from the History Tab's cache
                mActivity.onBackPressed();
            }
        } else {
            // Android sharing mechanism; do not immediately dismiss; that will occur when the Android Sharing Intent terminates
            shareFile(exportFile, str);
        }
    }

    private File createCSVFile(int shareWhat, Date fromWhen) {
        // TODO Perform the Export process in a separate thread?
        CSVexporter theExporter = new CSVexporter(getContext());
        CSVexporter.ReturnResults exportResults = null;
        if (ZeoCompanionApplication.mIrec_SAonly != null) { exportResults = theExporter.createFileOneRec(ZeoCompanionApplication.mIrec_SAonly, shareWhat); }
        else { exportResults =  theExporter.createFile(shareWhat, fromWhen); }

        if (!exportResults.rAnErrorMessage.isEmpty()) {
            Utilities.showYesNoDialog(getContext(), "Error", "The CSV export failed.  Error:\n"+exportResults.rAnErrorMessage, "Okay", null, mYesNoResponseListener_alerts, 1, null, null); // auto-dismiss
            return null;
        } else if (exportResults.rTheExportFile == null) {
            Utilities.showYesNoDialog(getContext(), "Error", "The CSV export failed for unknown reasons", "Okay", null, mYesNoResponseListener_alerts, 1, null, null);  // auto-dismiss
            return null;
        }
        return exportResults.rTheExportFile;
    }

    private File createImageFile(int shareWhat) {
        if (ZeoCompanionApplication.mIrec_SAonly == null) { return null; }
        ImageExporter theExporter = new ImageExporter(getContext());
        ImageExporter.ReturnResults exportResults = theExporter.createFileOneRec(ZeoCompanionApplication.mIrec_SAonly, shareWhat);

        if (!exportResults.rAnErrorMessage.isEmpty()) {
            Utilities.showYesNoDialog(getContext(), "Error", "The Image export failed.  Error:\n"+exportResults.rAnErrorMessage, "Okay", null, mYesNoResponseListener_alerts, 1, null, null); // auto-dismiss
            return null;
        } else if (exportResults.rTheExportFile == null) {
            Utilities.showYesNoDialog(getContext(), "Error", "The Image export failed for unknown reasons", "Okay", null, mYesNoResponseListener_alerts, 1, null, null);  // auto-dismiss
            return null;
        }
        return exportResults.rTheExportFile;
    }

    private boolean emailFile(File exportedFile, String subject) {
        DirectEmailerThread de = new DirectEmailerThread(getContext());
        de.setName("DirectEmailerThread via "+_CTAG+".emailFile");
        int result = de.configure(subject, subject + "; see attachment.", exportedFile, false);
        if (result < 0) {
            Utilities.showAlertDialog(getContext(), "Settings", "The necessary Settings are not in-place to be able to send a direct email; please configure you email account and destinations in the Settings.", "Okay"); // does not auto-dismiss
            return false;
        }
        de.start();
        return true;
    }

    private void shareFile(File exportedFile, String subject) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        String str = exportedFile.getName();
        if (str.endsWith(".jpg")) { sendIntent.setType("image/jpeg"); }
        else if (str.endsWith(".png")) { sendIntent.setType("image/png"); }
        else { sendIntent.setType("text/plain"); }
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.putExtra(Intent.EXTRA_TEXT, subject+"; see attachment.");
        Uri uri = Uri.parse("file://" + exportedFile);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        mActivity.mShareIntentActive = true;
        startActivity(Intent.createChooser(sendIntent, "Share for..."));
    }
}
