package opensource.zeocompanion.fragments;

import android.support.v4.app.Fragment;

public class MainFragmentWrapper extends Fragment {

    public MainFragmentWrapper() {
        // Required empty public constructor
    }

    // called by the MainActivity to a specific Fragment when it becomes actually shown
    public void fragmentBecameShown() {
        // specific subclasses need to override to add the proper logic
    }

    // called by the MainActivity when handlers or settings have made changes to the database
    // or to settings options, etc
    public void needToRefresh() {
        // specific subclasses need to override to add the proper logic
    }

    // called by the MainActivity at the behest of the Journal Data Coordinator
    public void daypointHasChanged() {
        // specific subclasses need to override to add the proper logic
    }

    // called by the MainActivity to have Fragments dim their controls during sleep
    public void dimControlsForSleep(boolean doDim) {
        // specific subclasses need to override to add the proper logic
    }

}
