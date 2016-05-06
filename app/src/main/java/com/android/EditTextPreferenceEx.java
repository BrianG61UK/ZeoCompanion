package com.android;

import android.content.Context;
import android.graphics.Color;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

public class EditTextPreferenceEx extends EditTextPreference {
    public EditTextPreferenceEx(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }

    public EditTextPreferenceEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditTextPreferenceEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreferenceEx(Context context) {
        super(context, null);
    }

    @Override
    public CharSequence getSummary() {
        CharSequence newSummary = super.getSummary();
        return String.format(newSummary.toString(), getText());
    }
}
