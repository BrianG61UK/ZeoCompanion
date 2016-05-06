package opensource.zeocompanion.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import opensource.zeocompanion.R;
import opensource.zeocompanion.fragments.CustomizeActivityAttribsFragment;
import opensource.zeocompanion.fragments.CustomizeActivityDoingsFragment;
import opensource.zeocompanion.fragments.CustomizeActivityValuesFragment;

public class CustomizeActivity extends AppCompatActivity {
    // member variables
    private SectionsPagerAdapter mSectionsPagerAdapter = null;
    private ViewPager mViewPager = null;

    // member constants and other static content
    private static final String _CTAG = "CA";

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
    // Note: all fragments used by this custom adaptor must be subclasses of MainFragmentWrapper
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private static final int TOTAL_TABS = 3;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return CustomizeActivityAttribsFragment.newInstance(0);
                case 1:
                    return CustomizeActivityAttribsFragment.newInstance(1);
                case 2:
                    return new CustomizeActivityDoingsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return TOTAL_TABS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Fixed Attributes & Values";
                case 1:
                    return "Custom Attributes & Values";
                case 2:
                    return "Event Doings";
            }
            return null;
        }
    }
}
