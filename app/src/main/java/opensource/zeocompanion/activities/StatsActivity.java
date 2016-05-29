package opensource.zeocompanion.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import opensource.zeocompanion.R;
import opensource.zeocompanion.fragments.StatsActivityTrendsFragment;

// Activity for displaying statistical graphs;
// tabs for the various graphs
public class StatsActivity extends AppCompatActivity{
    // member variables
    private SectionsPagerAdapter mSectionsPagerAdapter = null;
    private ViewPager mViewPager = null;

    // member constants and other static content
    private static final String _CTAG = "StatA";

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

    // Note: FragmentPagerAdapter will completely activate up to three fragments
    // simultaneously such that two "off screen" fragments "to the right" and "to the left" are up and through onResume()
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private static final int TOTAL_TABS = 1;

        // constructor
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // create the proper fragment for the specified tab
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new StatsActivityTrendsFragment();
            }
            return null;
        }

        // return the current quantity of tabs to be shown
        @Override
        public int getCount() { return TOTAL_TABS; }

        // return the title to be used for each tab
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Trends";
            }
            return null;
        }
    }
}
