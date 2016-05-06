package com.android;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import opensource.zeocompanion.fragments.MainAttributesFragment;

/** Spinner extension that calls onItemSelected even when the selection is the same as its previous value */
public class EvtSpinner extends AppCompatSpinner implements AdapterView.OnItemSelectedListener {
    // member variables
    private OnItemSelectedListener mListener = null;
    private int mIgnoreCntr = 0;

    // listener callback variables
    public int mDefaultPosition = 0;

    // member constants and other static content
    private static final String _CTAG = "ESV";

    // constructors needed for the UI and Android Studio
    public EvtSpinner(Context context) {
        super(context);
    }
    public EvtSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public EvtSpinner(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    // record some callback information for OnItemSelectedListener
    public void setListenerInfo_DefaultPosition(int defaultPosition) {
        mDefaultPosition = defaultPosition;
    }

    // called by App code, usually in an onCreateView method to set or reset the desired listener; it can be null
    // this version intercepts and places our own onItemSelected pre-handler before the App's
    @Override
    public void setOnItemSelectedListener(OnItemSelectedListener theListener) {
        //if (theListener == null) { Log.d(_CTAG + ".setListen", this+" set listener = null; " + "ignoreCntr="+mIgnoreCntr); }
        //else { Log.d(_CTAG + ".setListen", this+" set listener active; " + "ignoreCntr="+mIgnoreCntr); }
        mListener = theListener;
        super.setOnItemSelectedListener(this);
    }

    // this method can be invoked either as a delayed message or immediately inline;
    // this method can be triggered: once at class instantiation, or due to a human keypress, or due to an App call
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            if (mIgnoreCntr > 0) {
                mIgnoreCntr--;
                //Log.d(_CTAG + ".onItemSel", this+" Selection is ignored; ignoreCntr--="+mIgnoreCntr);
            } else {
                //Log.d(_CTAG + ".onItemSel", this+" Selection being passed onto the App; ignoreCntr--="+mIgnoreCntr);
                mListener.onItemSelected(parent, view, position, id);
            }
        } else {
            if (mIgnoreCntr > 0) mIgnoreCntr--;
            //Log.d(_CTAG + ".onItemSel", this+" App listener is null; nothing invoked; ignoreCntr="+mIgnoreCntr);
        }
    }

    // this method is only invoked by the UI framework and only in response to a real user interaction;
    // it mearly passed the call onto the App's listener method
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if (mListener != null) mListener.onNothingSelected(parent);
    }

    // called by the UI framework when a user chooses an item and do want to invoke onItemSelected;
    // also can be called by the App but will trigger an onItemSelected;
    // generally these should immediately invoke onItemSelected
    @Override
    public void setSelection(int position, boolean animate) {
        //Log.d(_CTAG + ".setSelectionA", this+": Position="+position+", animate="+animate+"; ignoreCntr="+mIgnoreCntr);
        boolean sameSelected = (position == getSelectedItemPosition());
        super.setSelection(position, animate);
        if (sameSelected) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            AdapterView.OnItemSelectedListener l = getOnItemSelectedListener();
            if (l != null) l.onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

    // called by the UI framework when a user chooses an item and do want to invoke onItemSelected;
    // also can be called by the App but will trigger an onItemSelected;
    // generally these should immediately invoke onItemSelected
    @Override
    public void setSelection(int position) {
        //Log.d(_CTAG + ".setSelection", this+": Position="+position+"; ignoreCntr="+mIgnoreCntr);
        boolean sameSelected = (position == getSelectedItemPosition());
        super.setSelection(position);
        if (sameSelected) {
            // normal Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            AdapterView.OnItemSelectedListener l = getOnItemSelectedListener();
            if (l != null) l.onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

    // called only by App code, usually in an onCreateView method which will utilize postponed messaging for the onItemSelected call;
    // we do not want the App's listener to be called when using this method
    public void setSelectionWithoutCallback(int position) {
        //if (position != getSelectedItemPosition()) mIgnoreCntr++;
        super.setOnItemSelectedListener(null);
        //Log.d(_CTAG + ".setSelWoCB", this+": Position="+position+"; ignoreCntr++="+mIgnoreCntr);
        super.setSelection(position, false);
        super.setOnItemSelectedListener(this);
    }
}