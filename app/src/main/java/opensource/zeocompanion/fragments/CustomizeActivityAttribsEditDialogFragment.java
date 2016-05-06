package opensource.zeocompanion.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import opensource.zeocompanion.R;
import opensource.zeocompanion.database.CompanionDatabaseContract;

public class CustomizeActivityAttribsEditDialogFragment extends DialogFragment {
    // member variables
    private View mRootView = null;
    private String mAttributeText = null;
    private String mShortText = null;
    private int mSleepStage = 0;
    private String mTitle = null;
    private String mAction = null;  // 'a' or 'c'
    private String mOrigAttributeText = null;
    private String mOrigShortText = null;
    private int mOrigSleepStage = 0;

    // member constants and other static content
    private static final String _CTAG = "CEF";
    private static final String ARG_PARAM1 = "attribText";
    private static final String ARG_PARAM2 = "shortText";
    private static final String ARG_PARAM3 = "SleepStage";
    private static final String ARG_PARAM4 = "title";
    private static final String ARG_PARAM5 = "action";
    private static final String ARG_PARAM6 = "origAttribText";
    private static final String ARG_PARAM7 = "origShortText";
    private static final String ARG_PARAM8 = "origSleepStage";

    // callback interface to the invoking fragment
    public interface OnAttribEditFragListener {
        public void editedResults(String theNewAttributeText, String theNewShortText, int theNewSleepStage, String action, String theOrigAttributeText, String theOrigShortText, int theOrigSleepStage);
    }

    // constructor
    public CustomizeActivityAttribsEditDialogFragment() {}

    // instanciator
    public static CustomizeActivityAttribsEditDialogFragment newInstance(String attributeText, String shortText, int sleepMode, String title, String action) {
        CustomizeActivityAttribsEditDialogFragment fragment = new CustomizeActivityAttribsEditDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, attributeText);
        args.putString(ARG_PARAM2, shortText);
        args.putInt(ARG_PARAM3, sleepMode);
        args.putString(ARG_PARAM4, title);
        args.putString(ARG_PARAM5, action);
        args.putString(ARG_PARAM6, attributeText);
        args.putString(ARG_PARAM7, shortText);
        args.putInt(ARG_PARAM8, sleepMode);
        fragment.setArguments(args);
        return fragment;
    }

    // create the Fragment object
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAttributeText = getArguments().getString(ARG_PARAM1);
            mShortText = getArguments().getString(ARG_PARAM2);
            mSleepStage = getArguments().getInt(ARG_PARAM3);
            mTitle = getArguments().getString(ARG_PARAM4);
            mAction = getArguments().getString(ARG_PARAM5);
            mOrigAttributeText = getArguments().getString(ARG_PARAM6);
            mOrigShortText = getArguments().getString(ARG_PARAM7);
            mOrigSleepStage = getArguments().getInt(ARG_PARAM8);
        }
    }

    // create the Fragment's view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_customize_attribs_edit, container, false);

        // setup a listener for end-user press of the cancel button; and indeed just cancel
        mRootView.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        // setup a listener for end-user press of the Add/Change button;
        // if pressed then invoke processing of the text in the EditText widget
        Button b = (Button)mRootView.findViewById(R.id.button_change);
        if (mAction.toCharArray()[0] == 'a') { b.setText("Add"); }
        else { b.setText("Change"); }
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                provideTextToParent();
                dismiss();
            }
        });

        // define a common editor listener to use to all EditText widgets
        TextView.OnEditorActionListener listener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    provideTextToParent();
                    handled = true;
                    dismiss();
                }
                return handled;
            }
        };

        // pre-populate the EditText widget with initial text;
        // then setup a listener for press of the "Done" aka "Carriage Return" button on the keyboard;
        // handle that exactly like a press of the Add/Change button
        EditText et1 = (EditText)mRootView.findViewById(R.id.editText_attribute);
        EditText et2 = (EditText)mRootView.findViewById(R.id.editText_shortAttribute);
        if (!mAttributeText.isEmpty()) { et1.setText(mAttributeText); }
        if (!mShortText.isEmpty()) { et2.setText(mShortText); }
        et1.setOnEditorActionListener(listener);
        et2.setOnEditorActionListener(listener);

        // pre-populate the before/after radio buttons
        RadioButton rb_before = (RadioButton)mRootView.findViewById(R.id.radioButton_before);
        RadioButton rb_after = (RadioButton)mRootView.findViewById(R.id.radioButton_after);
        if (mSleepStage == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER) {
            rb_before.setChecked(false);
            rb_after.setChecked(true);
        } else {
            rb_before.setChecked(true);
            rb_after.setChecked(false);
        }

        return mRootView;
    }

    // Fragment's View is now created
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().setTitle(mTitle);
    }

    // pass the edited text back to the invoking Fragment via the callback
    private void provideTextToParent() {
        EditText theET1 = (EditText)mRootView.findViewById(R.id.editText_attribute);
        EditText theET2 = (EditText)mRootView.findViewById(R.id.editText_shortAttribute);
        RadioButton rb_after = (RadioButton)mRootView.findViewById(R.id.radioButton_after);

        int sleepStage = 0;
        if (rb_after.isChecked()) { sleepStage = CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER; }
        else { sleepStage = CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE; }
        ((OnAttribEditFragListener)getTargetFragment()).editedResults(theET1.getText().toString(), theET2.getText().toString(), sleepStage, mAction, mOrigAttributeText, mOrigShortText, mOrigSleepStage);
    }

}
