package com.android;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;

/** Spinner extension that calls onItemSelected even when the selection is the same as its previous value */
public class AttrSpinner extends AppCompatSpinner implements AdapterView.OnItemSelectedListener {
    // member variables
    private OnItemSelectedListener mListener = null;
    private int mIgnoreCntr = 0;

    // listener callback variables
    public View mInRowView = null;
    public int mDefaultPosition = 0;
    public int mAttributesArrayPosition = 0;

    // member constants and other static content
    private static final String _CTAG = "ASV";

    // constructors needed for the UI and Android Studio
    public AttrSpinner(Context context) {
        super(context);
    }
    public AttrSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public AttrSpinner(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    // record a larger set of callback information for OnItemSelectedListener
    public void setListenerInfo(View inRowView, int attributesArrayPosition, int defaultPosition) {
        mAttributesArrayPosition = attributesArrayPosition;
        mInRowView = inRowView;
        mDefaultPosition = defaultPosition;
    }

    // change just the defaultPosition callback information for OnItemSelectedListener
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
        super.setSelection(position, animate);
    }

    // called by the UI framework when a user chooses an item and do want to invoke onItemSelected;
    // also can be called by the App but will trigger an onItemSelected;
    // generally these should immediately invoke onItemSelected
    @Override
    public void setSelection(int position) {
        //Log.d(_CTAG + ".setSelection", this+": Position="+position+"; ignoreCntr="+mIgnoreCntr);
        super.setSelection(position);
    }
}