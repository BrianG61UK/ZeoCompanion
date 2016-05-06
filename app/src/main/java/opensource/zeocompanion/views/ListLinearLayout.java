package opensource.zeocompanion.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import java.util.ArrayList;
import opensource.zeocompanion.utility.LLL_Adapter;

// Alternative to doing a ListView which has considerable problems on the MainAttributes Fragment
public class ListLinearLayout extends LinearLayout {

    // member variables
    private LLL_Adapter mAdapter = null;
    private ArrayList<View> mChildViews = null;

    // member constants and other static content
    private static final String _CTAG = "LLV";

    // constructors
    public ListLinearLayout(Context context)  { super(context); }
    public ListLinearLayout(Context context, AttributeSet attrs)  { super(context, attrs); }
    public ListLinearLayout(Context context, AttributeSet attrs, int defStyle)  { super(context, attrs, defStyle); }

    // set (or reset) the implemented ListLinearLayout Adaptor
    public void setAdapter(LLL_Adapter adapter) {
        //Log.d(_CTAG+".setAdapter","Invoked");
        mAdapter = adapter;
        if (mAdapter != null) {
            buildAllNewViews();
            requestLayout();
        } else {
            clearAllViews();
        }
    }

    // notification from the ListLinearLayout Adaptor that the adaptor's content has changed
    public void onChanged() {
        //Log.d(_CTAG+".onChanged","Invoked");
        buildAllNewViews();
        requestLayout();
    }

    // build all the dynamic child views needed (scrolling is handled by an external ScrollView)
    private void buildAllNewViews() {
        //Log.d(_CTAG+".buildAllNewViews","Invoked");
        if (mAdapter == null) { return; }
        clearAllViews();
        mChildViews = new ArrayList<View>();

        int m = mAdapter.getCount();
        if (m == 0) { return; }

        for (int pos = 0; pos < m; pos++) {
            View newChild = mAdapter.getView(pos, null, this);
            if (newChild != null) { addView(newChild, pos); }
            mChildViews.add(newChild);
        }

        // must only refresh visibilities after layout is done because of typical Android layout bugs that will not layout children properly in a GONE-set view
        ViewTreeObserver.OnGlobalLayoutListener glListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                refreshVisibilities();
            }
        };
        getViewTreeObserver().addOnGlobalLayoutListener(glListener);
    }

    // clear away all the dynamic child views; however do not trigger any re-layout since this is called either at Fragment termination or Fragment refresh
    public void clearAllViews() {
        //Log.d(_CTAG+".clearAllViews","Invoked");
        if (mChildViews != null) {
            removeAllViews();
            for (View view: mChildViews) {
                if (view != null) {
                    if (mAdapter != null) { mAdapter.destroyView(view); }
                    view.setTag(null);
                }
            }
            mChildViews.clear();
            mChildViews = null;
        }
    }

    // refresh the visibility status of each dynamic child view based upon decision by the adapter
    public void refreshVisibilities() {
        //Log.d(_CTAG+".refreshVisibilities","Invoked");

        if (mChildViews == null) { return; }
        boolean anyChanges = false;
        if (mAdapter == null) {
            for (View view: mChildViews) {
                if (view != null) {
                    int viz = view.getVisibility();
                    if (viz != View.VISIBLE) { view.setVisibility(View.VISIBLE); anyChanges = true; }
                }
            }
        } else {
            int pos = 0;
            for (View view: mChildViews) {
                if (view != null) {
                    int viz = view.getVisibility();
                    boolean doShow = mAdapter.getShouldBeVisible(pos, view);
                    if (doShow) {
                        if (viz != View.VISIBLE) { view.setVisibility(View.VISIBLE); anyChanges = true; }
                    } else {
                        if (viz != View.GONE) { view.setVisibility(View.GONE); anyChanges = true; }
                    }
                    pos++;
                }
            }
        }
        if (anyChanges) { invalidate(); requestLayout(); }
    }

    // force the ListLinearLayout to be resized for every child view
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // Calculate entire height by providing a very large height hint.
        // But do not use the highest 2 bits of this integer; those are
        // reserved for the MeasureSpec mode.
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}
