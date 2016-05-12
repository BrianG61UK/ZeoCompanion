package com.github;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import opensource.zeocompanion.R;

public class TimePreference extends DialogPreference implements Preference.OnPreferenceChangeListener {
    private TimePicker picker = null;
    private long mTimestamp = 0;
    public final static long DEFAULT_VALUE = 0;

    public TimePreference(final Context context, final AttributeSet attrs) {
        super(context, attrs, Resources.getSystem().getIdentifier("editTextPreferenceStyle", "attr", "android"));
        this.setOnPreferenceChangeListener(this);
    }

    protected void setTime(final long time) {
        mTimestamp = time;
        persistLong(time);
        notifyDependencyChange(shouldDisableDependents());
        notifyChanged();
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        picker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(getContext()));
        setTimePickertTextColor(picker, Color.WHITE);
        return picker;
    }

    protected Calendar getPersistedTime() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getPersistedLong(DEFAULT_VALUE));

        return c;
    }

    @Override
    public CharSequence getSummary() {
        CharSequence newSummary = super.getSummary();
        final DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(getContext());
        final Date date = new Date(mTimestamp);
        final String formattedDate = dateFormat.format(date.getTime());
        return String.format(newSummary.toString(), formattedDate);
    }

    @Override
    protected void onBindDialogView(final View v) {
        super.onBindDialogView(v);
        final Calendar c = getPersistedTime();

        picker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(c.get(Calendar.MINUTE));
    }

    @Override
    protected void onDialogClosed(final boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.MINUTE, picker.getCurrentMinute());
            c.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());


            if (!callChangeListener(c.getTimeInMillis())) {
                return;
            }

            setTime(c.getTimeInMillis());
        }
    }

    @Override
    protected Object onGetDefaultValue(final TypedArray a, final int index) {
        return a.getInteger(index, 0);
    }

    @Override
    protected void onSetInitialValue(final boolean restorePersistedValue, final Object defaultValue) {
        long time;
        if (defaultValue == null) {
            time = restorePersistedValue ? getPersistedLong(DEFAULT_VALUE) : DEFAULT_VALUE;
        } else if (defaultValue instanceof Long) {
            time = restorePersistedValue ? getPersistedLong((Long) defaultValue) : (Long) defaultValue;
        } else if (defaultValue instanceof Calendar) {
            time = restorePersistedValue ? getPersistedLong(((Calendar)defaultValue).getTimeInMillis()) : ((Calendar)defaultValue).getTimeInMillis();
        } else {
            time = restorePersistedValue ? getPersistedLong(DEFAULT_VALUE) : DEFAULT_VALUE;
        }

        setTime(time);
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        mTimestamp = (Long)newValue;
        return true;
    }

    // note the default time-picker changes in Android 5.x to a clock;
    // appears to be no programmatic support for choosing the mode
    private void setTimePickertTextColor(TimePicker picker, int color) {
        Resources system = Resources.getSystem();
        int hour_numberpicker_id = system.getIdentifier("hour", "id", "android");
        NumberPicker hour_numberpicker = (NumberPicker)picker.findViewById(hour_numberpicker_id);
        if (hour_numberpicker != null) {
            int minute_numberpicker_id = system.getIdentifier("minute", "id", "android");
            int ampm_numberpicker_id = system.getIdentifier("amPm", "id", "android");
            NumberPicker minute_numberpicker = (NumberPicker) picker.findViewById(minute_numberpicker_id);
            NumberPicker ampm_numberpicker = (NumberPicker) picker.findViewById(ampm_numberpicker_id);

            set_NP_textColor(hour_numberpicker, color);
            set_NP_textColor(minute_numberpicker, color);
            set_NP_textColor(ampm_numberpicker, color);
        } else {
            int hour_textView_id = system.getIdentifier("hours", "id", "android");
            TextView hour_textView = (TextView)picker.findViewById(hour_textView_id);
            if (hour_textView != null) {
                int separator_textView_id = system.getIdentifier("separator", "id", "android");
                TextView separator_textView = (TextView)picker.findViewById(separator_textView_id);
                int minute_textView_id = system.getIdentifier("minutes", "id", "android");
                TextView minute_textView = (TextView)picker.findViewById(minute_textView_id);
                int am_textView_id = system.getIdentifier("am_label", "id", "android");
                TextView am_textView = (TextView)picker.findViewById(am_textView_id);
                int pm_textView_id = system.getIdentifier("pm_label", "id", "android");
                TextView pm_textView = (TextView)picker.findViewById(pm_textView_id);

                hour_textView.setTextColor(color);
                separator_textView.setTextColor(color);
                minute_textView.setTextColor(color);
                am_textView.setTextColor(color);
                pm_textView.setTextColor(color);
            }
        }
    }

    private void set_NP_textColor(NumberPicker number_picker, int color){
        final int count = number_picker.getChildCount();

        for(int i = 0; i < count; i++){
            View child = number_picker.getChildAt(i);

            try{
                Field wheelpaint_field = number_picker.getClass().getDeclaredField("mSelectorWheelPaint");
                wheelpaint_field.setAccessible(true);

                ((Paint)wheelpaint_field.get(number_picker)).setColor(color);
                ((EditText)child).setTextColor(color);
                number_picker.invalidate();
            }
            catch(Exception e){
                Log.d("TPV.set_NP_textColor", "Exception "+e.toString());
            }
        }
    }
}
