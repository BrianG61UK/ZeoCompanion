package com.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

import com.obscuredPreferences.ObscuredPrefs;

import opensource.zeocompanion.utility.Utilities;

/**
 * A {@link Preference} that allows for string
 * input.
 * <p>
 * It is a subclass of {@link DialogPreference} and shows the {@link EditText}
 * in a dialog. This {@link EditText} can be modified either programmatically
 * via {@link #getEditText()}, or through XML by setting any EditText
 * attributes on the EditTextPreference.
 * <p>
 * This preference will store a string into the SharedPreferences.
 * <p>
 * See android.R.styleable#EditText EditText Attributes.
 */
public class EditTextPreferenceEncrypt extends DialogPreference {
    /**
     * The edit text shown in the dialog.
     */
    private EditText mEditText;

    private String mText;

    public EditTextPreferenceEncrypt(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        //super(context, attrs, defStyleAttr, defStyleRes);
        super(context, attrs, defStyleAttr);

        mEditText = new EditText(context, attrs);

        // Give it an ID so it can be saved/restored
        Resources res = Resources.getSystem();
        int id = res.getIdentifier("edit", "id", "android");
        mEditText.setId(id);
        //mEditText.setId(com.android.internal.R.id.edit);

    /*
     * The preference framework and view framework both have an 'enabled'
     * attribute. Most likely, the 'enabled' specified in this XML is for
     * the preference framework, but it was also given to the view framework.
     * We reset the enabled state.
     */
        mEditText.setEnabled(true);
    }

    public EditTextPreferenceEncrypt(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EditTextPreferenceEncrypt(Context context, AttributeSet attrs) {
        this(context, attrs, Resources.getSystem().getIdentifier("editTextPreferenceStyle", "attr", "android"));
    }

    public EditTextPreferenceEncrypt(Context context) {
        this(context, null);
    }

    /**
     * Saves the text to the {@link SharedPreferences}.
     *
     * @param text The text to save
     */
    public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();

        mText = text;
        String cypherText = ObscuredPrefs.encryptString(mText);
        persistString(cypherText);    // this call invokes the chain that eventually saves text to the SharedPrefs file

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    /**
     * Gets the text from the {@link SharedPreferences}.
     *
     * @return The current preference value.
     */
    public String getText() {
        return mText;
    }

    @Override
    public CharSequence getSummary() {
        CharSequence newSummary = super.getSummary();
        return String.format(newSummary.toString(), mText);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        EditText editText = mEditText;
        editText.setText(getText());
        editText.setTextColor(Color.BLACK);
        ViewParent oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }
    }

    /**
     * Adds the EditText widget of this preference to the dialog's view.
     *
     * @param dialogView The dialog view.
     */
    protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
        Resources res = Resources.getSystem();
        int id = res.getIdentifier("edittext_container", "id", "android");
        ViewGroup container = (ViewGroup) dialogView.findViewById(id);
        //ViewGroup container = (ViewGroup) dialogView.findViewById(com.android.internal.R.id.edittext_container);
        if (container != null) {
            container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            String value = mEditText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String plainText = "";
        if (restoreValue) {
            String cypherText = getPersistedString(ObscuredPrefs.encryptString(mText));
            plainText = ObscuredPrefs.decryptString(cypherText);
        }
        else { plainText = (String)defaultValue; }
        //setText(restoreValue ? getPersistedString(mText) : (String) defaultValue);
        setText(plainText);
    }

    @Override
    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(mText) || super.shouldDisableDependents();
    }

    /**
     * Returns the {@link EditText} widget that will be shown in the dialog.
     *
     * @return The {@link EditText} widget that will be shown in the dialog.
     */
    public EditText getEditText() {
        return mEditText;
    }

    /** @hide */
    //@Override
    protected boolean needInputMethod() {
        // We want the input method to show, if possible, when dialog is displayed
        return true;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.text = getText();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setText(myState.text);
    }

    private static class SavedState extends BaseSavedState {
        String text;

        public SavedState(Parcel source) {
            super(source);
            text = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(text);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
