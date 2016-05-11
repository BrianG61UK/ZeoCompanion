package opensource.zeocompanion.activities;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;

import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.fragments.CustomizeActivityAttribsFragment;
import opensource.zeocompanion.fragments.CustomizeActivityAttribsOrderFragment;
import opensource.zeocompanion.fragments.CustomizeActivityDoingsFragment;

// Activity for managing attributes, values, and event doings;
// utilizes tabs for its various fragments
public class CustomizeActivity extends AppCompatActivity {
    // member variables
    private SectionsPagerAdapter mSectionsPagerAdapter = null;
    private ViewPager mViewPager = null;
    private CustomizeActivityAttribsOrderFragment mAttribsOrderFrag = null;

    // member constants and other static content
    private static final String _CTAG = "CA";

    // called then the Activity is first created or upon return of some other Activity back to this activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // create the adaptor for the UI fragments for this activity
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter
        mViewPager = (ViewPager)findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // link the tabs in the UI with the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Intent intent = getIntent();
        int tab = intent.getIntExtra("startTab", 0);
        mViewPager.setCurrentItem(tab, false);
    }

    // Attributes Fragment informs that it has added, deleted, or changed an attribute name;
    // the Attributes Order Fragment needs to refresh its list
    public void informAttributesChanged() {
        if (mAttribsOrderFrag != null) { mAttribsOrderFrag.refreshList(); }
    }

    // Note: FragmentPagerAdapter will completely activate up to three fragments
    // simultaneously such that two "off screen" fragments "to the right" and "to the left" are up and through onResume()
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private static final int TOTAL_TABS = 4;

        // constructor
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // create the proper fragment for the specified tab
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return CustomizeActivityAttribsFragment.newInstance(0);
                case 1:
                    return CustomizeActivityAttribsFragment.newInstance(1);
                case 2:
                    mAttribsOrderFrag = new CustomizeActivityAttribsOrderFragment();
                    return mAttribsOrderFrag;
                case 3:
                    return new CustomizeActivityDoingsFragment();
            }
            return null;
        }

        // return the current quantity of tabs to be shown
        @Override
        public int getCount() {
            return TOTAL_TABS;
        }

        // return the title to be used for each tab
        @Override
        public CharSequence getPageTitle(int position) {
            Display display = getWindowManager().getDefaultDisplay();
            Point screenSize = new Point();
            display.getSize(screenSize);
            int screenWidthDp = (int)((float)screenSize.x / ZeoCompanionApplication.mScreenDensity);

            switch (position) {
                case 0:
                    if (screenWidthDp < 800) {
                        return "Fixed Attr & Values";
                    }
                    return "Fixed Attributes & Values";
                case 1:
                    if (screenWidthDp < 800) {
                        return "Custom Attr & Values";
                    }
                    return "Custom Attributes & Values";
                case 2:
                    if (screenWidthDp < 800) {
                        return "Before Attr Order";
                    }
                    return "Before Attributes Order";
                case 3:
                    return "Event Doings";
            }
            return null;
        }
    }
}
