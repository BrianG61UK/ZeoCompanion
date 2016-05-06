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
import android.widget.TextView;
import opensource.zeocompanion.R;

// this Fragment allows the end-user to edit one item of text (either add or change)
public class EditTextDialogFragment extends DialogFragment {
    // member variables
    private View mRootView = null;
    private int mQty_of_Fields = 0;
    private String mTextToEdit1 = null;
    private String mTextToEdit2 = null;
    private String mTitle = null;
    private String mAction = null;  // 'a' or 'c'
    private String mCallbackString0 = null;
    private String mCallbackString1 = null;
    private String mCallbackString2 = null;

    // member constants and other static content
    private static final String _CTAG = "ETF";
    private static final String ARG_PARAM1 = "qtyFields";
    private static final String ARG_PARAM2 = "textToEdit1";
    private static final String ARG_PARAM3 = "textToEdit2";
    private static final String ARG_PARAM4 = "title";
    private static final String ARG_PARAM5 = "action";
    private static final String ARG_PARAM6 = "callbackString0";
    private static final String ARG_PARAM7 = "callbackString1";
    private static final String ARG_PARAM8 = "callbackString2";

    // callback interface to the invoking fragment
    public interface OnExitTextFragListener {
        public void editedText(int qtyOfFields, String theNewText1, String theNewText2, String action, String callbackString0, String callbackString1, String callbackString2);
    }

    // constructor
    public EditTextDialogFragment() {}

    // instanciator for one edit field
    public static EditTextDialogFragment newInstance1(String textToEdit, String title, String action, String callbackString0, String callbackString1) {
        EditTextDialogFragment fragment = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, 1);
        args.putString(ARG_PARAM2, textToEdit);
        args.putString(ARG_PARAM3, "");
        args.putString(ARG_PARAM4, title);
        args.putString(ARG_PARAM5, action);
        args.putString(ARG_PARAM6, callbackString0);
        args.putString(ARG_PARAM7, callbackString1);
        args.putString(ARG_PARAM8, "");
        fragment.setArguments(args);
        return fragment;
    }

    // instanciator for two edit fields
    public static EditTextDialogFragment newInstance2(String textToEdit1, String textToEdit2, String title, String action, String callbackString0, String callbackString1, String callbackString2) {
        EditTextDialogFragment fragment = new EditTextDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, 2);
        args.putString(ARG_PARAM2, textToEdit1);
        args.putString(ARG_PARAM3, textToEdit2);
        args.putString(ARG_PARAM4, title);
        args.putString(ARG_PARAM5, action);
        args.putString(ARG_PARAM6, callbackString0);
        args.putString(ARG_PARAM7, callbackString1);
        args.putString(ARG_PARAM8, callbackString2);
        fragment.setArguments(args);
        return fragment;
    }

    // create the Fragment object
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mQty_of_Fields = getArguments().getInt(ARG_PARAM1);
            mTextToEdit1 = getArguments().getString(ARG_PARAM2);
            mTextToEdit2 = getArguments().getString(ARG_PARAM3);
            mTitle = getArguments().getString(ARG_PARAM4);
            mAction = getArguments().getString(ARG_PARAM5);
            mCallbackString0 = getArguments().getString(ARG_PARAM6);
            mCallbackString1 = getArguments().getString(ARG_PARAM7);
            mCallbackString2 = getArguments().getString(ARG_PARAM8);
        }
    }

    // create the Fragment's view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_edit_text, container, false);

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
        EditText et1 = (EditText)mRootView.findViewById(R.id.editText_theText1);
        EditText et2 = (EditText)mRootView.findViewById(R.id.editText_theText2);
        if (mQty_of_Fields == 2) {
            if (!mTextToEdit1.isEmpty()) { et1.setText(mTextToEdit1); }
            et1.setVisibility(View.VISIBLE);
            et1.setOnEditorActionListener(listener);
            if (!mTextToEdit2.isEmpty()) { et2.setText(mTextToEdit2); }
            et2.setVisibility(View.VISIBLE);
            et2.setOnEditorActionListener(listener);
        } else {
            et1.setVisibility(View.INVISIBLE);
            et1.setOnEditorActionListener(null);
            if (!mTextToEdit1.isEmpty()) { et2.setText(mTextToEdit1); }
            et2.setVisibility(View.VISIBLE);
            et2.setOnEditorActionListener(listener);
        }

        return mRootView;
    }

    // Fragement's View is now created
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().setTitle(mTitle);
    }

    // pass the edited text back to the invoking Fragment via the callback
    private void provideTextToParent() {
        if (mQty_of_Fields == 2) {
            EditText theET1 = (EditText)mRootView.findViewById(R.id.editText_theText1);
            EditText theET2 = (EditText)mRootView.findViewById(R.id.editText_theText2);
            Log.d(_CTAG + ".provideText", "New Text1="+theET1.getText() + ", New Text2="+theET2.getText());
            ((OnExitTextFragListener)getTargetFragment()).editedText(mQty_of_Fields, theET1.getText().toString(), theET2.getText().toString(), mAction, mCallbackString0, mCallbackString1, mCallbackString2);
        } else {
            EditText theET2 = (EditText)mRootView.findViewById(R.id.editText_theText2);
            Log.d(_CTAG + ".provideText", "New Text="+theET2.getText());
            ((OnExitTextFragListener)getTargetFragment()).editedText(mQty_of_Fields, theET2.getText().toString(), null, mAction, mCallbackString0, mCallbackString1, null);
        }
    }
}
